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

/**
 * Modulo 1: Login (4 casos).
 * <p>
 * TC01 - Login exitoso con credenciales validas
 * TC02 - Email vacio muestra error
 * TC03 - Password vacio muestra error
 * TC04 - Toggle mostrar/ocultar password
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

    @Step("Verificar que la app abrio en la pantalla de Login")
    private void stepVerifyOnLoginScreen() {
        boolean onLogin = loginPage.isOnLoginScreen();
        AllureHelper.reportScreenState(
                "Login",
                onLogin ? "Logo 'DB' detectado, texto 'Bienvenido de nuevo' visible"
                        : "No se detectaron indicadores de Login");
        Assert.assertTrue(onLogin, "Debe estar en la pantalla de Login al iniciar");
    }

    @Step("Escribir email '{0}' en campo de email")
    private void stepTypeEmail(String email) {
        loginPage.typeEmail(email);
        AllureHelper.reportAction(
                "Escribir", "Campo de email (primer EditText)",
                email, "Texto escrito correctamente");
    }

    @Step("Escribir password '{0}' en campo de password")
    private void stepTypePassword(String password) {
        loginPage.typePassword(password);
        AllureHelper.reportAction(
                "Escribir", "Campo de password (segundo EditText)",
                "**** (oculto)", "Password escrito correctamente");
    }

    @Step("Tap en boton 'Iniciar sesion'")
    private void stepTapLogin() {
        loginPage.tapLoginButton();
        AllureHelper.reportAction(
                "Tap", "Boton 'Iniciar sesion' (ViewGroup con content-desc)",
                null, "Tap realizado, esperando cambio de pantalla");
    }

    @Step("Verificar navegacion a Home despues del login")
    private void stepVerifyOnHome() {
        boolean onHome = homePage.isOnHomeScreen();
        String saldoTexto = "";
        try {
            saldoTexto = homePage.getConsolidatedBalanceText();
        } catch (Exception ignored) {}

        AllureHelper.reportValidation(
                "Navegacion a Home",
                onHome ? "Home detectado - Saldo visible: " + saldoTexto
                        : "Home NO detectado",
                "Pantalla Home con 'Saldo total' y accesos rapidos",
                onHome,
                "Indicadores buscados: Saldo total, $2455450, Transferir, Movimientos, Pagar");
        Assert.assertTrue(onHome, "Debe estar en Home despues de login exitoso");
    }

    // ========================================================================
    // TC02 - EMAIL VACIO
    // ========================================================================

    @Test(priority = 2, groups = {"login", "negative"})
    @Description("TC02 - Email vacio: la app debe mostrar mensaje de error y no permitir continuar.")
    @Severity(SeverityLevel.CRITICAL)
    public void testEmailVacio() {
        stepClearEmail();
        stepTypePasswordValid();
        stepTapLoginExpectingError();
        stepVerifyErrorMessage("email vacio");
    }

    @Step("Limpiar campo de email para dejarlo vacio")
    private void stepClearEmail() {
        loginPage.clearEmail();
        AllureHelper.reportAction(
                "Limpiar", "Campo de email", "",
                "Campo vacio, sin texto");
    }

    @Step("Escribir password valido '{0}'")
    private void stepTypePasswordValid() {
        loginPage.typePassword(TestDataProvider.VALID_PASSWORD);
        AllureHelper.reportAction(
                "Escribir", "Campo de password",
                TestDataProvider.VALID_PASSWORD + " (oculto)",
                "Password escrito correctamente");
    }

    @Step("Tap en boton 'Iniciar sesion' (se espera error por email vacio)")
    private void stepTapLoginExpectingError() {
        loginPage.tapLoginButton();
        AllureHelper.reportAction(
                "Tap", "Boton 'Iniciar sesion'",
                null, "Tap realizado con email vacio, esperando mensaje de error");
    }

    @Step("Verificar que la app muestra mensaje de error por: {0}")
    private void stepVerifyErrorMessage(String motivo) {
        boolean errorShown = loginPage.isErrorMessageDisplayed();
        String errorMsg = "";
        try {
            errorMsg = loginPage.getErrorMessageText();
        } catch (Exception ignored) {}

        AllureHelper.reportValidation(
                "Mensaje de error por " + motivo,
                errorShown ? "Error visible: '" + errorMsg + "'" : "No se detecto mensaje de error",
                "Mensaje de error visible en pantalla",
                errorShown,
                "El login debe bloquearse cuando el email esta vacio");
        Assert.assertTrue(errorShown, "Debe mostrar error por " + motivo);
    }

    // ========================================================================
    // TC03 - PASSWORD VACIO
    // ========================================================================

    @Test(priority = 3, groups = {"login", "negative"})
    @Description("TC03 - Password vacio: la app debe mostrar mensaje de error y no permitir continuar.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPasswordVacio() {
        stepTypeEmailValid();
        stepClearPassword();
        stepTapLoginExpectingError();
        stepVerifyErrorMessage("password vacio");
    }

    @Step("Escribir email valido '{0}'")
    private void stepTypeEmailValid() {
        loginPage.typeEmail(TestDataProvider.VALID_EMAIL);
        AllureHelper.reportAction(
                "Escribir", "Campo de email",
                TestDataProvider.VALID_EMAIL, "Email escrito correctamente");
    }

    @Step("Limpiar campo de password para dejarlo vacio")
    private void stepClearPassword() {
        loginPage.clearPassword();
        AllureHelper.reportAction(
                "Limpiar", "Campo de password", "",
                "Campo vacio, sin texto");
    }

    // ========================================================================
    // TC04 - TOGGLE PASSWORD
    // ========================================================================

    @Test(priority = 4, groups = {"login"})
    @Description("TC04 - Toggle de password: mostrar y ocultar la contrasena con el boton ojito.")
    @Severity(SeverityLevel.NORMAL)
    public void testTogglePassword() {
        stepVerifyPasswordHidden();
        stepTapToggleAndVerifyVisible();
        stepTapToggleAndVerifyHidden();
    }

    @Step("Verificar estado inicial: password oculto")
    private void stepVerifyPasswordHidden() {
        boolean visible = loginPage.isPasswordVisible();
        AllureHelper.reportValidation(
                "Estado inicial del password",
                visible ? "VISIBLE (texto plano)" : "OCULTO (puntos)",
                "OCULTO por defecto",
                !visible,
                "El password debe estar oculto al abrir la pantalla de Login");
        Assert.assertFalse(visible, "El password debe estar oculto por defecto");
    }

    @Step("Tap en toggle (ojito) y verificar password se hace visible")
    private void stepTapToggleAndVerifyVisible() {
        loginPage.tapPasswordToggle();
        boolean visible = loginPage.isPasswordVisible();
        AllureHelper.reportAction(
                "Tap", "Toggle de password (ojito)", null,
                "Tap realizado, verificando visibilidad");
        AllureHelper.reportValidation(
                "Password despues de toggle ON",
                visible ? "VISIBLE - texto plano mostrado" : "OCULTO - sigue oculto",
                "VISIBLE (texto plano)",
                visible,
                "Al activar el toggle, el password debe mostrarse en texto plano");
        Assert.assertTrue(visible, "El password debe ser visible despues del primer tap");
    }

    @Step("Tap en toggle (ojito) nuevamente y verificar password se oculta")
    private void stepTapToggleAndVerifyHidden() {
        loginPage.tapPasswordToggle();
        boolean visible = loginPage.isPasswordVisible();
        AllureHelper.reportAction(
                "Tap", "Toggle de password (ojito)", null,
                "Segundo tap realizado, verificando visibilidad");
        AllureHelper.reportValidation(
                "Password despues de toggle OFF",
                visible ? "VISIBLE - sigue en texto plano" : "OCULTO - texto enmascarado",
                "OCULTO (puntos)",
                !visible,
                "Al desactivar el toggle, el password debe volver a ocultarse");
        Assert.assertFalse(visible, "El password debe estar oculto despues del segundo tap");
    }
}
