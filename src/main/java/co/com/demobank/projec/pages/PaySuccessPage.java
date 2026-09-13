package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/**
 * Page Object de la pantalla de exito de pago de DemoBank.
 * <p>
 * Muestra el titulo "Pago exitoso", el detalle del pago y
 * el boton para volver al Home.
 */
public class PaySuccessPage {

    private final AndroidDriver driver;

    // =========================================================================
    // Localizadores
    // =========================================================================

    private final By successTitle =
            By.xpath("//*[contains(@text,'Pago exitoso')]");

    private final By successDetail =
            By.xpath("//*[contains(@text,'Pagaste')]");

    private final By backToHomeButton =
            By.xpath("//*[@text='Volver al inicio']");

    // =========================================================================
    // Constructor
    // =========================================================================

    public PaySuccessPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // Acciones
    // =========================================================================

    /**
     * Toca el boton "Volver al inicio".
     */
    public void tapBackToHome() {
        WaitUtils.safeClick(backToHomeButton);
    }

    // =========================================================================
    // Metodos de estado
    // =========================================================================

    /**
     * Verifica si estamos en la pantalla de exito de pago.
     *
     * @return true si el titulo "Pago exitoso" esta visible
     */
    public boolean isOnSuccessScreen() {
        try {
            return driver.findElement(successTitle).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el texto completo del detalle del pago.
     *
     * @return detalle (ej: "Pagaste $65.40 de Energia Electrica")
     */
    public String getSuccessDetailText() {
        return WaitUtils.waitForVisibility(successDetail).getText();
    }

    /**
     * Obtiene el nombre del servicio pagado.
     * Extrae del texto "Pagaste $X de {servicio}".
     *
     * @return nombre del servicio
     */
    public String getServiceName() {
        String full = getSuccessDetailText();
        if (full.contains(" de ")) {
            return full.substring(full.lastIndexOf(" de ") + 4).trim();
        }
        return full;
    }

    /**
     * Obtiene el monto pagado como texto.
     * Extrae del texto "Pagaste {monto} de ...".
     *
     * @return monto pagado (ej: "$65.40")
     */
    public String getAmountText() {
        String full = getSuccessDetailText();
        int start = full.indexOf('$');
        int end = full.indexOf(" de ");
        if (start >= 0 && end > start) {
            return full.substring(start, end).trim();
        }
        return full;
    }
}
