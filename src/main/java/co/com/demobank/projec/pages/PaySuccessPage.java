package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;

/*
 * ============================================================================
 * PAGE OBJECT: PaySuccessPage (DemoBank)
 * ============================================================================
 *
 * LOCALIZADORES BASADOS EN DUMP REAL (adb uiautomator dump):
 *
 *   "¡Pago exitoso!"                          → titulo (TextView)
 *   "Pagaste $65.40 de Energía Eléctrica"      → detalle (TextView)
 *   "Volver al inicio"                        → boton (TextView)
 *   "Hacer otro pago"                         → boton alternativo
 * ============================================================================
 */
public class PaySuccessPage {

    private final AndroidDriver driver;

    // Titulo "¡Pago exitoso!"
    private final By successTitle =
            By.xpath("//*[contains(@text,'Pago exitoso')]");

    // Detalle "Pagaste $X de Servicio"
    private final By successDetail =
            By.xpath("//*[contains(@text,'Pagaste')]");

    // Boton "Volver al inicio"
    private final By backToHomeButton =
            By.xpath("//*[@text='Volver al inicio']");

    public PaySuccessPage(AndroidDriver driver) {
        this.driver = driver;
    }

    public void tapBackToHome() {
        WaitUtils.safeClick(backToHomeButton);
    }

    public boolean isOnSuccessScreen() {
        try {
            return driver.findElement(successTitle).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el texto completo del detalle.
     * Ej: "Pagaste $65.40 de Energía Eléctrica"
     */
    public String getSuccessDetailText() {
        return WaitUtils.waitForVisibility(successDetail).getText();
    }

    /**
     * Obtiene el nombre del servicio pagado.
     * Extrae de "Pagaste $65.40 de Energía Eléctrica" → "Energía Eléctrica"
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
     * Extrae de "Pagaste $65.40 de Energía Eléctrica" → "$65.40"
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
