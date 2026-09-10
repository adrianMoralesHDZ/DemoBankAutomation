package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.OCRUtils;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/*
 * ============================================================================
 * PAGE OBJECT: TransferSuccessPage
 * ============================================================================
 *
 * REPRESENTA: La pantalla de éxito después de una transferencia exitosa.
 *
 * CARACTERÍSTICAS:
 *   - Muestra un check animado
 *   - Muestra el nombre del beneficiario (confirma que se envío a X)
 *   - Muestra el monto transferido (CRÍTICO: este monto debe coincidir con el ingresado)
 *   - Botón "Volver al inicio"
 *
 * JUSTIFICACIÓN OCR (PDF Módulo 4):
 *   El monto mostrado es un componente con formato custom sin testID.
 *   Por eso se valida con OCR (debe coincidir con el monto ingresado).
 * ============================================================================
 */
public class TransferSuccessPage {

    private final AndroidDriver driver;

    // ========================================================================
    // LOCALIZADORES
    // ========================================================================
    private final By successIcon =
            By.id("com.demobank.app:id/img_success_check");

    private final By successTitle =
            By.id("com.demobank.app:id/txt_success_title");

    private final By recipientName =
            By.id("com.demobank.app:id/txt_recipient_name");

    /** Monto mostrado en success - se lee con OCR */
    private final By displayedAmount =
            By.xpath("//*[@resource-id='current amount']");

    private final By backToHomeButton =
            By.id("com.demobank.app:id/btn_back_to_home");

    public TransferSuccessPage(AndroidDriver driver) {
        this.driver = driver;
    }

    /**
     * Toca el botón "Volver al inicio".
     */
    public void tapBackToHome() {
        WaitUtils.safeClick(backToHomeButton);
    }

    /**
     * Verifica si estamos en la pantalla de éxito.
     */
    public boolean isOnSuccessScreen() {
        try {
            return driver.findElement(successIcon).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el nombre del beneficiario mostrado.
     */
    public String getRecipientName() {
        return WaitUtils.waitForVisibility(recipientName).getText();
    }

    /**
     * Lee el monto mostrado en pantalla con OCR.
     * Debe coincidir con el monto que se transfirió.
     */
    public double getDisplayedAmount() {
        return OCRUtils.extractCurrencyAmount(
                WaitUtils.waitForVisibility(displayedAmount)
        );
    }

    /**
     * Obtiene el título de éxito (ej: "¡Transferencia exitosa!").
     */
    public String getSuccessTitle() {
        return WaitUtils.waitForVisibility(successTitle).getText();
    }
}
