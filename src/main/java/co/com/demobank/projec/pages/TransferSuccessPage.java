package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page Object de la pantalla de exito de transferencia de DemoBank.
 * <p>
 * Muestra el titulo "Transferencia exitosa", el monto enviado y
 * el nombre del destinatario.
 */
public class TransferSuccessPage {

    private final AndroidDriver driver;

    // =========================================================================
    // Localizadores
    // =========================================================================

    private final By successTitle =
            By.xpath("//*[contains(@text,'exitosa')]");

    private final By recipientNameText =
            By.xpath("//*[@text='María López' or @text='Maria Lopez' or @text='Mara Lpez']");

    private final By backToHomeButton =
            By.xpath("//android.widget.TextView[@text='Volver al inicio']");

    private final By amountTextView =
            By.xpath("//android.widget.TextView[contains(@text,'$') and string-length(@text) > 4]");

    // =========================================================================
    // Constructor
    // =========================================================================

    public TransferSuccessPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // Acciones
    // =========================================================================

    /**
     * Toca el boton "Volver al inicio" para regresar al Home.
     */
    public void tapBackToHome() {
        try {
            WaitUtils.waitForVisibility(backToHomeButton).click();
        } catch (Exception e) {
            try {
                driver.findElement(
                        By.xpath("//android.view.ViewGroup[@content-desc='Volver al inicio']")
                ).click();
            } catch (Exception e2) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Map<String, Object> args = new HashMap<>();
                args.put("x", 610);
                args.put("y", 1577);
                js.executeScript("mobile: tap", args);
            }
        }
    }

    // =========================================================================
    // Metodos de estado
    // =========================================================================

    /**
     * Verifica si estamos en la pantalla de exito.
     *
     * @return true si el titulo "exitosa" esta visible
     */
    public boolean isOnSuccessScreen() {
        try {
            return driver.findElement(successTitle).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lee el titulo de la pantalla de exito.
     *
     * @return texto del titulo (ej: "Transferencia exitosa!")
     */
    public String readSuccessTitle() {
        return WaitUtils.waitForVisibility(successTitle).getText();
    }

    /**
     * Obtiene el nombre del destinatario mostrado en pantalla.
     * Extrae el nombre del subtitulo "Enviaste $X a {nombre}".
     *
     * @return nombre del destinatario
     */
    public String getRecipientNameShown() {
        try {
            List<WebElement> allTexts =
                    driver.findElements(By.className("android.widget.TextView"));
            for (WebElement el : allTexts) {
                String text = el.getText();
                if (text.startsWith("Enviaste") && text.contains(" a ")) {
                    int idx = text.lastIndexOf(" a ");
                    if (idx > 0) {
                        return text.substring(idx + 3);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            return WaitUtils.waitForVisibility(recipientNameText).getText();
        } catch (Exception e) {
            return "<no encontrado>";
        }
    }

    /**
     * Obtiene el monto mostrado en la pantalla de exito.
     *
     * @return texto del monto (ej: "$100000.00")
     */
    public String getDisplayedAmountText() {
        try {
            return WaitUtils.waitForVisibility(amountTextView).getText();
        } catch (Exception e) {
            try {
                List<WebElement> allTexts = driver.findElements(
                        By.xpath("//android.widget.TextView[contains(@text,'$')]"));
                for (WebElement el : allTexts) {
                    String txt = el.getText();
                    if (txt.contains("$") && txt.length() > 4) return txt;
                }
            } catch (Exception ignored) {
            }
            return "";
        }
    }
}
