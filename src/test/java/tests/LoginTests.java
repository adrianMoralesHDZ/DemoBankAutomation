package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/*
 * ============================================================================
 * MODULO 1: LOGIN (4 casos - requisito del PDF)
 * ============================================================================
 *
 * Cubre:
 *   TC01 - Login exitoso con credenciales validas
 *   TC02 - Email vacio muestra error
 *   TC03 - Password vacio muestra error
 *   TC04 - Toggle mostrar/ocultar password
 *
 * REPORTES ALLURE:
 *   - Cada paso del test esta anotado con @Step para que el reporte
 *     muestre el detalle paso a paso de la ejecucion.
 *   - Los screenshots se capturan AUTOMATICAMENTE solo en caso de fallo
 *     (gestionado por TestListener.onTestFailure).
 *   - No se adjuntan screenshots en cada paso ni en caso de exito.
 * ============================================================================
 */
public class LoginTests {

    private LoginPage loginPage;
    private HomePage homePage;

    @BeforeMethod
    public void setUp() {
        DriverFactory.getDriver();
        loginPage = new LoginPage(DriverFactory.getDriver());
        homePage = new HomePage(DriverFactory.getDriver());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    // ========================================================================
    // TC01 - LOGIN EXITOSO
    // ========================================================================

    /**
     * TC01: Autenticacion Exitosa con credenciales validas.
     *
     * Flujo:
     *   1. Verificar que estamos en pantalla de Login
     *   2. Escribir email valido
     *   3. Escribir password valido
     *   4. Tap en boton "Iniciar sesion"
     *   5. Verificar redireccion al Home
     */
    @Test(priority = 1, groups = {"login", "smoke"})
    @Description("TC01 - Login exitoso con credenciales demo@demo.com / 1234. "
            + "Valida la navegacion hacia Home despues de la autenticacion.")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginExitoso() {
        stepVerifyOnLoginScreen();
        stepTypeEmail(TestDataProvider.VALID_EMAIL);
        stepTypePassword(TestDataProvider.VALID_PASSWORD);
        stepTapLogin();
        stepVerifyOnHome();
    }

    @Step("Verificar que estamos en la pantalla de Login")
    private void stepVerifyOnLoginScreen() {
        boolean onLogin = loginPage.isOnLoginScreen();
        Assert.assertTrue(onLogin, "Debe estar en la pantalla de Login al iniciar");
        AllureHelper.logAction("Verificar", "Pantalla de Login visible");
    }

    @Step("Escribir email: {0}")
    private void stepTypeEmail(String email) {
        loginPage.typeEmail(email);
        AllureHelper.logAction("Escribir", "Campo email con valor: " + email);
    }

    @Step("Escribir password")
    private void stepTypePassword(String password) {
        loginPage.typePassword(password);
        AllureHelper.logAction("Escribir", "Campo password (oculto)");
    }

    @Step("Tap en boton 'Iniciar sesion'")
    private void stepTapLogin() {
        loginPage.tapLoginButton();
        AllureHelper.logAction("Tap", "Boton 'Iniciar sesion'");
    }

    @Step("Verificar que estamos en Home despues del login")
    private void stepVerifyOnHome() {
        boolean onHome = homePage.isOnHomeScreen();
        AllureHelper.logValidation("Navegacion a Home", onHome, true, onHome);
        Assert.assertTrue(onHome, "Debe estar en Home despues de login exitoso");
    }

    // ========================================================================
    // TC02 - EMAIL VACIO
    // ========================================================================

    /**
     * TC02: Email vacio debe mostrar mensaje de error y bloquear navegacion.
     */
    @Test(priority = 2, groups = {"login", "negative"})
    @Description("TC02 - Email vacio: la app debe mostrar mensaje de error y no permitir continuar.")
    @Severity(SeverityLevel.CRITICAL)
    public void testEmailVacio() {
        stepClearEmail();
        stepTypePasswordValid();
        stepTapLoginExpectingError();
        stepVerifyErrorMessage("email vacio");
    }

    @Step("Limpiar campo de email")
    private void stepClearEmail() {
        loginPage.clearEmail();
        AllureHelper.logAction("Limpiar", "Campo email");
    }

    @Step("Escribir password valido")
    private void stepTypePasswordValid() {
        loginPage.typePassword(TestDataProvider.VALID_PASSWORD);
        AllureHelper.logAction("Escribir", "Campo password con valor valido");
    }

    @Step("Tap en boton 'Iniciar sesion' (se espera error)")
    private void stepTapLoginExpectingError() {
        loginPage.tapLoginButton();
        AllureHelper.logAction("Tap", "Boton 'Iniciar sesion' (esperando error)");
    }

    @Step("Verificar mensaje de error visible por: {0}")
    private void stepVerifyErrorMessage(String motivo) {
        boolean errorShown = loginPage.isErrorMessageDisplayed();
        AllureHelper.logValidation("Mensaje de error por " + motivo, errorShown, true, errorShown);
        Assert.assertTrue(errorShown, "Debe mostrar error por " + motivo);
    }

    // ========================================================================
    // TC03 - PASSWORD VACIO
    // ========================================================================

    /**
     * TC03: Password vacio debe mostrar mensaje de error y bloquear navegacion.
     */
    @Test(priority = 3, groups = {"login", "negative"})
    @Description("TC03 - Password vacio: la app debe mostrar mensaje de error y no permitir continuar.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPasswordVacio() {
        stepTypeEmailValid();
        stepClearPassword();
        stepTapLoginExpectingError();
        stepVerifyErrorMessage("password vacio");
    }

    @Step("Escribir email valido")
    private void stepTypeEmailValid() {
        loginPage.typeEmail(TestDataProvider.VALID_EMAIL);
        AllureHelper.logAction("Escribir", "Campo email con valor valido");
    }

    @Step("Limpiar campo de password")
    private void stepClearPassword() {
        loginPage.clearPassword();
        AllureHelper.logAction("Limpiar", "Campo password");
    }

    // ========================================================================
    // TC04 - TOGGLE PASSWORD
    // ========================================================================

    /**
     * TC04: Toggle mostrar/ocultar password.
     *
     * Valida que el toggle cambia la visibilidad de la contraseña:
     *   1. Estado inicial: password oculto
     *   2. Tap toggle: password visible
     *   3. Tap toggle: password oculto de nuevo
     */
    @Test(priority = 4, groups = {"login"})
    @Description("TC04 - Toggle de password: mostrar y ocultar la contraseña con el boton ojito.")
    @Severity(SeverityLevel.NORMAL)
    public void testTogglePassword() {
        stepVerifyPasswordHidden();
        stepTapToggleAndVerifyVisible();
        stepTapToggleAndVerifyHidden();
    }

    @Step("Verificar que el password esta inicialmente oculto")
    private void stepVerifyPasswordHidden() {
        boolean visible = loginPage.isPasswordVisible();
        AllureHelper.logValidation("Password oculto (inicial)", !visible, true, !visible);
        Assert.assertFalse(visible, "El password debe estar oculto por defecto");
    }

    @Step("Tap en toggle y verificar que el password se hace visible")
    private void stepTapToggleAndVerifyVisible() {
        loginPage.tapPasswordToggle();
        boolean visible = loginPage.isPasswordVisible();
        AllureHelper.logValidation("Password visible (toggle ON)", visible, true, visible);
        Assert.assertTrue(visible, "El password debe ser visible despues del primer tap");
    }

    @Step("Tap en toggle y verificar que el password se oculta de nuevo")
    private void stepTapToggleAndVerifyHidden() {
        loginPage.tapPasswordToggle();
        boolean visible = loginPage.isPasswordVisible();
        AllureHelper.logValidation("Password oculto (toggle OFF)", !visible, true, !visible);
        Assert.assertFalse(visible, "El password debe estar oculto despues del segundo tap");
    }
}
