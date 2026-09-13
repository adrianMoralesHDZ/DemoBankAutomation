package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Page Object del modal de Transferencia de DemoBank (3 pasos).
 * <p>
 * Paso 1: Seleccionar contacto.
 * Paso 2: Ingresar monto y seleccionar cuenta origen.
 * Paso 3: Confirmar transferencia.
 */
public class TransferPage {

    private final AndroidDriver driver;

    // =========================================================================
    // Paso 1: Seleccionar contacto
    // =========================================================================

    private final By contactsTitle =
            By.xpath("//*[@text and contains(@text,'transfieres')]");

    private final By mariaContact =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Mar')]");

    // =========================================================================
    // Paso 2: Ingresar monto
    // =========================================================================

    private final By amountFieldAlt =
            By.xpath("//android.widget.EditText[1]");

    private final By cuentaCorrienteOption =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Cuenta Corriente')]");

    private final By cuentaCorrienteBalance =
            By.xpath("//*[@text='Cuenta Corriente']/following::android.widget.TextView[contains(@text,'$')][1]");

    private final By continueButton =
            By.xpath("//android.view.ViewGroup[@content-desc='Continuar']");

    private final By continueTextView =
            By.xpath("//android.widget.TextView[@text='Continuar']");

    // =========================================================================
    // Paso 3: Confirmar
    // =========================================================================

    private final By confirmButton =
            By.xpath("//android.view.ViewGroup[@content-desc='Confirmar transferencia']");

    private final By confirmButtonAlt =
            By.xpath("//android.widget.TextView[@text='Confirmar transferencia']");

    // =========================================================================
    // Errores
    // =========================================================================

    private final By insufficientBalanceError =
            By.xpath("//*[contains(@text,'Saldo insuficiente')]");

    private final By invalidAmountError =
            By.xpath("//*[contains(@text,'monto válido') or contains(@text,'monto v')]");

    // =========================================================================
    // Constructor
    // =========================================================================

    public TransferPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // Acciones - Paso 1: Contacto
    // =========================================================================

    /**
     * Selecciona el primer contacto de la lista (Maria Lopez).
     * Usa estrategia con fallbacks: content-desc, TextView y coordenadas.
     */
    public void selectFirstContact() {
        try {
            driver.findElement(mariaContact).click();
        } catch (Exception e1) {
            try {
                driver.findElement(By.xpath("(//android.widget.TextView[@text='ML'])[1]")).click();
            } catch (Exception e2) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Map<String, Object> args = new HashMap<>();
                args.put("x", 610);
                args.put("y", 254);
                js.executeScript("mobile: tap", args);
            }
        }
    }

    // =========================================================================
    // Acciones - Paso 2: Monto
    // =========================================================================

    /**
     * Escribe el monto de la transferencia y oculta el teclado.
     *
     * @param amount monto como String (ej: "100000")
     */
    public void typeAmount(String amount) {
        WebElement field = driver.findElement(amountFieldAlt);
        field.click();
        field.sendKeys(amount);
        hideKeyboard();
    }

    /**
     * Obtiene el saldo de la cuenta origen mostrado en pantalla.
     *
     * @return saldo como double (ej: 1500000.00)
     */
    public double getSourceAccountBalance() {
        try {
            WebElement balance = WaitUtils.waitForVisibility(cuentaCorrienteBalance);
            return parseAmount(balance.getText());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Toca el boton "Continuar". Hace scroll si el boton no esta visible.
     */
    public void tapContinue() {
        try {
            driver.findElement(continueButton).click();
        } catch (Exception e1) {
            try {
                driver.findElement(continueTextView).click();
            } catch (Exception e2) {
                scrollDown();
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(d -> {
                    try {
                        return d.findElement(continueButton).isDisplayed()
                                || d.findElement(continueTextView).isDisplayed();
                    } catch (Exception e) {
                        return false;
                    }
                });
                driver.findElement(continueTextView).click();
            }
        }
    }

    // =========================================================================
    // Acciones - Paso 3: Confirmar
    // =========================================================================

    /**
     * Toca el boton "Confirmar transferencia".
     */
    public void tapConfirm() {
        try {
            driver.findElement(confirmButton).click();
        } catch (Exception e1) {
            try {
                driver.findElement(confirmButtonAlt).click();
            } catch (Exception e2) {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                Map<String, Object> args = new HashMap<>();
                args.put("x", 610);
                args.put("y", 793);
                js.executeScript("mobile: tap", args);
            }
        }
    }

    /**
     * Ejecuta el flujo completo: contacto, monto, continuar, confirmar.
     *
     * @param monto monto a transferir como String
     */
    public void completeTransfer(String monto) {
        selectFirstContact();
        typeAmount(monto);
        tapContinue();
        tapConfirm();
    }

    // =========================================================================
    // Metodos de estado
    // =========================================================================

    /**
     * Verifica si estamos en la pantalla de seleccion de contacto.
     *
     * @return true si el titulo de contactos esta visible
     */
    public boolean isOnContactScreen() {
        try {
            return driver.findElement(contactsTitle).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el mensaje "Saldo insuficiente" esta visible.
     *
     * @return true si el error esta desplegado
     */
    public boolean isInsufficientBalanceErrorDisplayed() {
        try {
            return WaitUtils.fluentWait(insufficientBalanceError).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el mensaje "Ingresa un monto valido" esta visible.
     *
     * @return true si el error esta desplegado
     */
    public boolean isInvalidAmountErrorDisplayed() {
        try {
            return WaitUtils.fluentWait(invalidAmountError).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // Metodos privados
    // =========================================================================

    private void hideKeyboard() {
        try {
            driver.hideKeyboard();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(d -> !driver.isKeyboardShown());
        } catch (Exception ignored) {
        }
    }

    private void scrollDown() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Map<String, Object> args = new HashMap<>();
            args.put("startX", 610);
            args.put("startY", 1500);
            args.put("endX", 610);
            args.put("endY", 500);
            args.put("duration", 300);
            js.executeScript("mobile: dragGesture", args);
            waitForScrollAnimation();
        } catch (Exception e) {
            System.out.println("scrollDown fallo: " + e.getMessage());
        }
    }

    private void waitForScrollAnimation() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.pollingEvery(Duration.ofMillis(200));
            wait.until(d -> !d.findElement(By.xpath("//android.widget.ScrollView"))
                    .getAttribute("scrollable").equals("false"));
        } catch (Exception ignored) {
        }
    }

    private double parseAmount(String text) {
        if (text == null || text.isEmpty()) return 0;
        String cleaned = text.replace("$", "").replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
