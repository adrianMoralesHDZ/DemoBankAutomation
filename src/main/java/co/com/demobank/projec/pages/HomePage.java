package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

/**
 * Page Object de la pantalla Home de DemoBank.
 * <p>
 * Muestra el saldo consolidado, tabs de Cuenta Corriente/Ahorros,
 * accesos rapidos (Transferir, Movimientos, Pagar) y boton de logout.
 */
public class HomePage {

    private final AndroidDriver driver;

    // =========================================================================
    // Localizadores
    // =========================================================================

    private final By balanceForOCR =
            By.xpath("//*[contains(@text,'$') and string-length(@text) > 6]");

    private final By currentAccountTab =
            By.xpath("//android.view.ViewGroup[@content-desc='Cuenta Corriente']");

    private final By savingsAccountTab =
            By.xpath("//android.view.ViewGroup[@content-desc='Cuenta Ahorros']");

    private final By accountInfoLine =
            By.xpath("//*[contains(@text,'****')]");

    private final By quickTransfer =
            By.xpath("(//android.view.ViewGroup[contains(@content-desc,'Transferir')])[1]");

    private final By quickMovements =
            By.xpath("(//android.view.ViewGroup[contains(@content-desc,'Movimientos')])[1]");

    private final By quickPay =
            By.xpath("(//android.view.ViewGroup[contains(@content-desc,'Pagar')])[1]");

    // =========================================================================
    // Constructor
    // =========================================================================

    public HomePage(AndroidDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // Acciones
    // =========================================================================

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
     * Toca el boton de cerrar sesion (icono esquina superior derecha).
     * <p>
     * El icono de logout esta en bounds=[1051,44][1164,156] pero el centro
     * exacto (1108,100) cae en el area del status bar y no registra el touch.
     * Se usa y=120 (un poco mas abajo) para que el touch landing sea correcto.
     * <p>
     * Usa W3C Actions (PointerInput) que simula un touch real en React Native.
     */
    public void tapLogout() {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger1");
        Sequence tap = new Sequence(finger, 0);
        tap.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), 1108, 120));
        tap.addAction(finger.createPointerDown(0));
        tap.addAction(new Pause(finger, Duration.ofMillis(300)));
        tap.addAction(finger.createPointerUp(0));
        driver.perform(Collections.singletonList(tap));
        System.out.println("[Logout] Tap W3C en (1108, 120) realizado");
    }

    // =========================================================================
    // Metodos de estado
    // =========================================================================

    /**
     * Verifica si estamos en la pantalla Home buscando multiples indicadores.
     *
     * @return true si al menos un indicador de Home esta visible
     */
    public boolean isOnHomeScreen() {
        String[] indicators = {
                "Saldo total", "$2455450", "$1500000",
                "Transferir", "Movimientos", "Pagar",
                "Cuenta Corriente", "Cuenta Ahorros"
        };

        for (String indicator : indicators) {
            try {
                WebElement el = driver.findElement(
                        By.xpath("//*[contains(@text,'" + indicator + "')]"));
                if (el.isDisplayed()) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    /**
     * Obtiene el texto del saldo consolidado mostrado en Home.
     *
     * @return texto del saldo (ej: "$2455450.00")
     */
    public String getConsolidatedBalanceText() {
        return WaitUtils.waitForVisibility(balanceForOCR).getText();
    }

    /**
     * Obtiene el saldo consolidado y lo convierte a double.
     *
     * @return saldo consolidado como valor numerico
     */
    public double getConsolidatedBalanceTextAsAmount() {
        return parseAmount(getConsolidatedBalanceText());
    }

    /**
     * Obtiene el texto con numero de cuenta y saldo de la cuenta activa.
     *
     * @return texto de info de cuenta (ej: "**** 4821 - $1500000.00")
     */
    public String getAccountInfoText() {
        return WaitUtils.waitForVisibility(accountInfoLine).getText();
    }

    /**
     * Obtiene el saldo de la cuenta activa y lo convierte a double.
     *
     * @return saldo de la cuenta activa como valor numerico
     */
    public double getAccountBalanceAsAmount() {
        return parseAmount(getAccountInfoText());
    }

    // =========================================================================
    // Metodos privados
    // =========================================================================

    /**
     * Convierte un texto con monto a double.
     * Maneja separadores de miles (puntos) y decimales.
     *
     * @param text texto con monto (ej: "$2455450.00" o "**** 4821 - $1500000.00")
     * @return valor numerico del monto
     */
    private double parseAmount(String text) {
        if (text == null || text.isEmpty()) return 0;

        String amountStr = text;
        int dollarIdx = amountStr.lastIndexOf('$');
        if (dollarIdx >= 0) {
            amountStr = amountStr.substring(dollarIdx + 1);
        }

        amountStr = amountStr.replaceAll("[^0-9.]", "").replace(",", "").trim();

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
}
