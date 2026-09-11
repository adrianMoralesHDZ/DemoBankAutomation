package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/*
 * ============================================================================
 * PAGE OBJECT: HomePage (DemoBank)
 * ============================================================================
 *
 * Pantalla principal con saldos mockeados:
 *   - Saldo total: $2.455.450.00  (debajo: Cuenta Corriente / Ahorros tabs)
 *   - Accesos rapidos: Transferir, Movimientos, Pagar, Mas
 *   - Boton logout (icono arriba a la derecha)
 *
 * CASOS DE PRUEBA DEL PDF (Modulo 2):
 *   - Consistencia del Saldo Consolidado
 *   - Interactividad de Cuentas (cambio entre tabs)
 *   - Accesos rapidos (Transferir, Pagar, Movimientos)
 *   - Cierre de Sesion Seguro (Logout)
 *
 * LOCALIZADORES REALES (adb uiautomator dump):
 *   El dump confirma que TODOS los textos del Home son android.widget.TextView
 *   con atributo @text perfectamente accesible. No hay tarjetas con gradiente
 *   ni componentes graficos sin accesibilidad. Los localizadores nativos
 *   son confiables en este modulo.
 *
 *   Los OCR justificados del PDF se implementan en otros modulos donde
 *   los localizadores nativos NO son confiables (ver TransferTests y
 *   PayTests).
 * ============================================================================
 */
public class HomePage {

    private final AndroidDriver driver;

    // ========================================================================
    // LOCALIZADORES (basados en dump real)
    // ========================================================================

    // Saldo total "$2455450.00" (TextView)
    // Para OCR usamos cualquier TextView con $ para leer el monto
    private final By balanceForOCR =
            By.xpath("//*[contains(@text,'$') and string-length(@text) > 6]");

    // Etiqueta "Saldo total"
    private final By balanceLabel =
            By.xpath("//*[@text='Saldo total']");

    // Boton tab "Cuenta Corriente"
    private final By currentAccountTab =
            By.xpath("//android.view.ViewGroup[@content-desc='Cuenta Corriente']");

    // Boton tab "Cuenta Ahorros"
    private final By savingsAccountTab =
            By.xpath("//android.view.ViewGroup[@content-desc='Cuenta Ahorros']");

    // Linea con numero de cuenta y saldo: "**** 4821 · $1500000.00"
    private final By accountInfoLine =
            By.xpath("//*[contains(@text,'****')]");

    // Accesos rapidos (por content-desc)
    private final By quickTransfer =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Transferir')]");

    private final By quickMovements =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Movimientos')]");

    private final By quickPay =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Pagar')]");

    private final By quickMore =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Más')]");

    // Link "Ver todos" (movimientos)
    private final By viewAllMovements =
            By.xpath("//android.view.ViewGroup[@content-desc='Ver todos']");

    // Boton logout (icono arriba-derecha)
    private final By logoutButton =
            By.xpath("//android.view.ViewGroup[@bounds='[1051,44][1164,156]']");

    // Saludo "Hola, Demo"
    private final By userGreeting =
            By.xpath("//*[@text='Hola,']");

    // ========================================================================
    public HomePage(AndroidDriver driver) {
        this.driver = driver;
    }

    // ========================================================================
    // ACCIONES
    // ========================================================================

    public void tapCurrentAccountTab() {
        WaitUtils.safeClick(currentAccountTab);
    }

    public void tapSavingsAccountTab() {
        WaitUtils.safeClick(savingsAccountTab);
    }

    public void tapQuickTransfer() {
        WaitUtils.safeClick(quickTransfer);
    }

    public void tapQuickMovements() {
        WaitUtils.safeClick(quickMovements);
    }

    public void tapQuickPay() {
        WaitUtils.safeClick(quickPay);
    }

    /**
     * Toca el boton de logout (icono arriba-derecha).
     */
    public void logout() {
        try {
            WaitUtils.safeClick(logoutButton);
            System.out.println("Tap en boton logout");
        } catch (Exception e) {
            System.out.println("No se pudo tap logout por bounds: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÉTODOS DE ESTADO
    // ========================================================================

    /**
     * Verifica si estamos en la pantalla Home.
     * Estrategia multiple: busca varios indicadores que SOLO existen en Home.
     */
    public boolean isOnHomeScreen() {
        String[] indicators = {
                "Saldo total",
                "$2455450",
                "$1500000",
                "Transferir",
                "Movimientos",
                "Pagar",
                "Cuenta Corriente",
                "Cuenta Ahorros"
        };

        for (String indicator : indicators) {
            try {
                WebElement el = driver.findElement(
                        By.xpath("//*[contains(@text,'" + indicator + "')]")
                );
                if (el.isDisplayed()) {
                    System.out.println("[OK] isOnHomeScreen: encontrado indicador '"
                            + indicator + "'");
                    return true;
                }
            } catch (Exception ignored) {}
        }
        System.out.println("[FAIL] isOnHomeScreen: ningun indicador encontrado");
        return false;
    }

    /**
     * Lee el saldo total del TextView nativo y lo convierte a double.
     *
     * El dump real confirma que "$2455450.00" es un TextView con @text.
     * No requiere OCR.
     *
     * @return saldo consolidado como double (ej: 2455450.00)
     */
    public double getConsolidatedBalanceTextAsAmount() {
        String text = getConsolidatedBalanceText();
        return parseAmount(text);
    }

    /**
     * Convierte texto con monto a double.
     *
     * Acepta formatos como:
     *   "$2455450.00"                → 2455450.00
     *   "**** 4821 · $1500000.00"   → 1500000.00  (extrae solo el monto)
     *   "+$1800.00"                  → 1800.00
     *   "-$54.20"                    → 54.20
     *
     * Estrategia: extrae el ultimo numero con decimales del texto,
     * eliminando separadores de miles (puntos) y signos no numericos.
     */
    private double parseAmount(String text) {
        if (text == null || text.isEmpty()) return 0;

        // Extraer la parte del monto: buscar "$" y tomar todo despues
        // Si no hay "$", buscar el ultimo numero con punto decimal
        String amountStr = text;
        int dollarIdx = amountStr.lastIndexOf('$');
        if (dollarIdx >= 0) {
            amountStr = amountStr.substring(dollarIdx + 1);
        }

        // Limpiar: espacios, comas, signos
        amountStr = amountStr
                .replaceAll("[^0-9.]", "")
                .replace(",", "")
                .trim();

        // Si hay multiples puntos (ej: "1.500.000.00"), interpretar
        // los primeros como separadores de miles y el ultimo como decimal
        if (amountStr.contains(".")) {
            int lastDot = amountStr.lastIndexOf('.');
            String decimal = amountStr.substring(lastDot);
            if (decimal.length() <= 3) {
                String integer = amountStr.substring(0, lastDot).replace(".", "");
                amountStr = integer + decimal;
            }
        }

        try {
            return Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Obtiene el texto del saldo total ("$2455450.00").
     */
    public String getConsolidatedBalanceText() {
        return WaitUtils.waitForVisibility(balanceForOCR).getText();
    }

    /**
     * Obtiene el numero de cuenta y saldo en pantalla.
     * Ej: "**** 4821 · $1500000.00"
     */
    public String getAccountInfoText() {
        return WaitUtils.waitForVisibility(accountInfoLine).getText();
    }

    /**
     * Devuelve el WebElement de la linea de cuenta (usado en test OCR).
     */
    public WebElement getAccountInfoElement() {
        return WaitUtils.waitForVisibility(accountInfoLine);
    }

    /**
     * Lee el saldo individual de la cuenta activa y lo convierte a double.
     *
     * La linea de cuenta tiene el formato: "**** 4821 · $1500000.00"
     * Se extrae solo la parte del monto y se parsea a double.
     *
     * @return saldo de la cuenta activa como double (ej: 1500000.00)
     */
    public double getAccountBalanceAsAmount() {
        String info = getAccountInfoText();
        return parseAmount(info);
    }

    /**
     * Devuelve el saludo del usuario (ej: "Hola, Demo").
     */
    public String getUserGreetingText() {
        return WaitUtils.waitForVisibility(userGreeting).getText();
    }

    /**
     * Devuelve el texto del label "Saldo total".
     */
    public String getBalanceLabelText() {
        return WaitUtils.waitForVisibility(balanceLabel).getText();
    }
}
