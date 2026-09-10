package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.OCRUtils;
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
 * ⚠️ LOCALIZADORES BASADOS EN DUMP REAL (adb shell uiautomator dump):
 *   La app NO usa resource-id, usa solo content-desc y text.
 * ============================================================================
 */
public class HomePage {

    private final AndroidDriver driver;

    // ========================================================================
    // LOCALIZADORES REALES (basados en dump)
    // ========================================================================

    // Saldo total "$2455450.00" (TextView) — se lee con OCR
    private final By consolidatedBalanceText =
            By.xpath("//*[@text and contains(@text,'$') and contains(@text,'2455450')]");

    /** Texto literal del saldo — patrón regular para OCR */
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

    // WebElement para OCR del AccountInfo
    public org.openqa.selenium.WebElement getAccountInfoElement() {
        return WaitUtils.waitForVisibility(accountInfoLine);
    }

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

    // Boton logout (icono arriba-derecha, content-desc parece estar vacio)
    // Es el primero ViewGroup con desc que tiene bounds pequeños arriba
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
     * Verifica si estamos en la pantalla Home buscando "Hola," o "Saldo total".
     */
    public boolean isOnHomeScreen() {
        try {
            return driver.findElement(userGreeting).isDisplayed()
                    || driver.findElement(balanceLabel).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lee el saldo total mediante OCR.
     * Estrategia:
     *   1. Intentar leer el TextView del saldo directamente
     *   2. Si no, tomar screenshot del WebElement y aplicar OCR
     */
    public double getConsolidatedBalanceByOCR() {
        try {
            // Primero intentar leer con el text directo
            WebElement element = driver.findElement(balanceForOCR);
            String text = element.getText();
            System.out.println("Saldo (text directo): '" + text + "'");
            return parseAmount(text);
        } catch (Exception e) {
            System.out.println("Fallo lectura directa, usando OCR sobre elemento");
            // Fallback: usar OCRUtils
            WebElement element = WaitUtils.waitForVisibility(balanceForOCR);
            return OCRUtils.extractCurrencyAmount(element);
        }
    }

    /**
     * Convierte texto tipo "$2455450.00" o "1.050.000" a double.
     */
    private double parseAmount(String text) {
        if (text == null || text.isEmpty()) return 0;
        String cleaned = text
                .replace("$", "")
                .replaceAll("\\s+", "")
                .replace(",", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            // formato con puntos como miles (1.050.000,50)
            if (cleaned.contains(".")) {
                int lastDot = cleaned.lastIndexOf('.');
                String decimal = cleaned.substring(lastDot);
                if (decimal.length() <= 3) {
                    String integer = cleaned.substring(0, lastDot).replace(".", "");
                    String result = integer + decimal;
                    try { return Double.parseDouble(result); } catch (Exception x) { return 0; }
                }
            }
            return 0;
        }
    }

    /**
     * Obtiene el texto del saldo (para OCR o logs).
     */
    public String getConsolidatedBalanceText() {
        WebElement element = WaitUtils.waitForVisibility(balanceForOCR);
        return element.getText();
    }

    /**
     * Verifica si el saldo coincide con el esperado.
     */
    public boolean isBalanceCorrect(double expectedBalance) {
        double actual = getConsolidatedBalanceByOCR();
        return Math.abs(actual - expectedBalance) < 1.0;
    }
}
