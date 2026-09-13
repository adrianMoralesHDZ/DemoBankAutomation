package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object de la pantalla de Login de DemoBank.
 * <p>
 * Contiene los localizadores y metodos de interaccion para:
 * email, password, boton de login, toggle de password y mensajes de error.
 */
public class LoginPage {

    private final AndroidDriver driver;

    // =========================================================================
    // Localizadores
    // =========================================================================

    private final By emailField =
            By.xpath("(//android.widget.EditText)[1]");

    private final By passwordField =
            By.xpath("(//android.widget.EditText)[2]");

    private final By loginButton =
            By.xpath("//android.view.ViewGroup[@content-desc='Iniciar sesión']");

    private final By passwordToggle =
            By.xpath("(//android.widget.EditText)[2]/following-sibling::android.view.ViewGroup[@clickable='true'][1]");

    private final By errorMessage =
            By.xpath("//*[contains(@text,'Ingresa tu correo')]");

    // =========================================================================
    // Constructor
    // =========================================================================

    public LoginPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // Acciones
    // =========================================================================

    /**
     * Escribe el email en el campo correspondiente.
     *
     * @param email valor a escribir
     */
    public void typeEmail(String email) {
        WaitUtils.waitForVisibility(emailField).sendKeys(email);
    }

    /**
     * Escribe la contrasena en el campo correspondiente.
     *
     * @param password valor a escribir
     */
    public void typePassword(String password) {
        WaitUtils.waitForVisibility(passwordField).sendKeys(password);
    }

    /**
     * Toca el boton "Iniciar sesion" y espera el cambio de pantalla.
     */
    public void tapLoginButton() {
        WaitUtils.safeClick(loginButton);
        waitForScreenChange();
    }

    /**
     * Realiza el login completo: email, password y tap en boton.
     *
     * @param email    correo valido
     * @param password contrasena valida
     */
    public void loginAs(String email, String password) {
        typeEmail(email);
        typePassword(password);
        tapLoginButton();
    }

    /**
     * Toca el toggle para mostrar/ocultar la contrasena.
     */
    public void tapPasswordToggle() {
        WaitUtils.safeClick(passwordToggle);
    }

    /**
     * Limpia el campo de email.
     */
    public void clearEmail() {
        WaitUtils.waitForVisibility(emailField).clear();
    }

    /**
     * Limpia el campo de contrasena.
     */
    public void clearPassword() {
        WaitUtils.waitForVisibility(passwordField).clear();
    }

    // =========================================================================
    // Metodos de estado
    // =========================================================================

    /**
     * Verifica si estamos en la pantalla de Login.
     *
     * @return true si el logo o el texto de bienvenida estan visibles
     */
    public boolean isOnLoginScreen() {
        try {
            return driver.findElement(
                    By.xpath("//*[@text='DB' or @text='Bienvenido de nuevo']")
            ).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si hay un mensaje de error visible.
     *
     * @return true si el mensaje de error esta desplegado
     */
    public boolean isErrorMessageDisplayed() {
        try {
            WebElement msg = driver.findElement(errorMessage);
            return msg.isDisplayed() && !msg.getText().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si la contrasena esta visible (texto plano) u oculta.
     *
     * @return true si la contrasena es visible
     */
    public boolean isPasswordVisible() {
        WebElement field = driver.findElement(passwordField);
        String type = field.getAttribute("password");
        return "false".equals(type);
    }

    // =========================================================================
    // Metodos privados
    // =========================================================================

    /**
     * Espera explicita a que el boton de login desaparezca
     * (indica que la pantalla cambio).
     */
    private void waitForScreenChange() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(d -> {
                try {
                    List<WebElement> btns = d.findElements(
                            By.xpath("//*[@text='Iniciar sesión']"));
                    return btns.isEmpty() || !btns.get(0).isDisplayed();
                } catch (Exception e) {
                    return true;
                }
            });
        } catch (Exception e) {
            System.out.println("[WARN] timeout esperando cambio de pantalla: " + e.getMessage());
        }
    }
}
