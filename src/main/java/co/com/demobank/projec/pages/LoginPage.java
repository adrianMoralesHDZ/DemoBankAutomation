package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/*
 * ============================================================================
 * PAGE OBJECT: LoginPage (DemoBank)
 * ============================================================================
 *
 * Página de Login de DemoBank con campos pre-llenados:
 *   - Email precargado: demo@demo.com
 *   - Password precargado: •••• (4 caracteres)
 *
 * ⚠️ LOCALIZADORES BASADOS EN DUMP REAL DE LA APP:
 * Esta versión de DemoBank NO usa resource-id estándar, usa coordenadas
 * (bounds) y descripciones de accessibility (content-desc).
 *
 * Dump realizado con: adb shell uiautomator dump
 * ============================================================================
 */
public class LoginPage {

    private final AndroidDriver driver;

    // ========================================================================
    // LOCALIZADORES (basados en dump real de DemoBank)
    // ========================================================================

    // Campo de email (por posición visual: ~Y=1297, ~X medio)
    // El campo es el único EditText superior
    private final By emailField =
            By.xpath("(//android.widget.EditText)[1]");

    // Campo de password (segundo EditText)
    private final By passwordField =
            By.xpath("(//android.widget.EditText)[2]");

    // Botón "Iniciar sesión" (identificado por desc)
    private final By loginButton =
            By.xpath("//android.view.ViewGroup[@content-desc='Iniciar sesión']");

    // Toggle de contraseña (ojito) - identificado por desc
    private final By passwordToggle =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'contrase')]");

    // Logo "DemoBank"
    private final By loginLogo =
            By.xpath("//*[@text='DB']");

    // Mensaje de error (busca el primer TextView con "Error" o similar)
    private final By errorMessage =
            By.xpath("//*[contains(@text,'Error') or contains(@text,'inv') or contains(@text,'vaci')]");

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================
    public LoginPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // ========================================================================
    // ACCIONES
    // ========================================================================

    /**
     * Escribe el email en el campo de email.
     * Limpia primero para partir de cero.
     */
    public void typeEmail(String email) {
        WebElement field = WaitUtils.waitForVisibility(emailField);
        field.clear();
        field.sendKeys(email);
    }

    /**
     * Escribe la contraseña en el campo de password.
     */
    public void typePassword(String password) {
        WebElement field = WaitUtils.waitForVisibility(passwordField);
        field.clear();
        field.sendKeys(password);
    }

    /**
     * Toca el botón "Iniciar sesión".
     */
    public void tapLoginButton() {
        WaitUtils.safeClick(loginButton);
    }

    /**
     * Login en una sola operación.
     * Si los campos ya están prellenados, limpia y vuelve a llenar.
     */
    public void loginAs(String email, String password) {
        typeEmail(email);
        typePassword(password);
        tapLoginButton();
    }

    /**
     * Solo presiona el botón de login (los campos ya están prellenados).
     * Es el método MÁS USADO en DemoBank porque los campos tienen valores por defecto.
     */
    public void tapLogin() {
        WaitUtils.safeClick(loginButton);
    }

    /**
     * Toggle para mostrar/ocultar contraseña (ojito junto al campo password).
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
     * Limpia el campo de password.
     */
    public void clearPassword() {
        WaitUtils.waitForVisibility(passwordField).clear();
    }

    // ========================================================================
    // MÉTODOS DE ESTADO
    // ========================================================================

    /**
     * Verifica si estamos en la pantalla de Login.
     * Busca el logo "DB" o el texto "Bienvenido".
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
     * Verifica si hay mensaje de error visible.
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
     * Devuelve el texto del mensaje de error.
     */
    public String getErrorMessageText() {
        return WaitUtils.waitForVisibility(errorMessage).getText();
    }

    /**
     * Verifica si la contraseña está visible (tipo text) u oculta (tipo password).
     */
    public boolean isPasswordVisible() {
        WebElement field = driver.findElement(passwordField);
        String type = field.getAttribute("password");
        return "false".equals(type);
    }
}
