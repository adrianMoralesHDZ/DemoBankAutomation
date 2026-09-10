package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.OCRUtils;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/*
 * ============================================================================
 * PAGE OBJECT: PaySuccessPage
 * ============================================================================
 *
 * Pantalla de éxito después de pagar un servicio.
 * Muestra el nombre del servicio pagado y el monto.
 * ============================================================================
 */
public class PaySuccessPage {

    private final AndroidDriver driver;

    private final By successIcon =
            By.id("com.demobank.app:id/img_pay_success");

    private final By recipientName =
            By.id("com.demobank.app:id/txt_pay_recipient_name");

    /** Monto extraído con OCR */
    private final By displayedAmount =
            By.xpath("//*[@resource-id='current amount']");

    private final By backToHomeButton =
            By.id("com.demobank.app:id/btn_pay_back_home");

    public PaySuccessPage(AndroidDriver driver) {
        this.driver = driver;
    }

    public void tapBackToHome() {
        WaitUtils.safeClick(backToHomeButton);
    }

    public boolean isOnSuccessScreen() {
        try {
            return driver.findElement(successIcon).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getRecipientName() {
        return WaitUtils.waitForVisibility(recipientName).getText();
    }

    public String getDisplayedAmountText() {
        return OCRUtils.extractTextFromElement(
                WaitUtils.waitForVisibility(displayedAmount)
        );
    }

    public double getDisplayedAmount() {
        return OCRUtils.extractCurrencyAmount(
                WaitUtils.waitForVisibility(displayedAmount)
        );
    }
}
