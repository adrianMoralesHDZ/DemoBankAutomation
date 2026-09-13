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
 * Modulo 1: Login (5 casos).
 * <p>
 * TC01 - Login exitoso con credenciales validas
 * TC02 - Email vacio muestra error
 * TC03 - Password vacio muestra error
 * TC04 - Toggle mostrar/ocultar password
 * TC05 - Cierre de sesion (logout) retorna a Login
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
        stepTypeEmail(TestDataProvider.getValidEmail());
        stepTypePassword(TestDataProvider.getValidPassword());
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
        loginPage.typePassword(TestDataProvider.getValidPassword());
        AllureHelper.reportAction(
                "Escribir", "Campo de password",
                TestDataProvider.getValidPassword() + " (oculto)",
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
        loginPage.typeEmail(TestDataProvider.getValidEmail());
        AllureHelper.reportAction(
                "Escribir", "Campo de email",
                TestDataProvider.getValidEmail(), "Email escrito correctamente");
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

    // ========================================================================
    // TC05 - CIERRE DE SESION (LOGOUT)
    // ========================================================================

    /**
     * TC05: Cierre de sesion seguro (logout).
     * <p>
     * Valida que despues de hacer login y estar en Home, al tocar el boton de
     * cerrar sesion (icono esquina superior derecha) la app retorna a la
     * pantalla de Login.
     * <p>
     * Flujo:
     *   1. Login con credenciales validas
     *   2. Verificar que estamos en Home
     *   3. Tap en boton de cerrar sesion
     *   4. Verificar retorno a Login (localizador nativo + OCR)
     */
    @Test(priority = 5, groups = {"login", "logout"})
    @Description("TC05 - Cierre de sesion: desde Home, tap en icono de logout retorna a pantalla de Login.")
    @Severity(SeverityLevel.CRITICAL)
    public void testCierreSesion() {
        stepLoginPrecondition();
        stepVerifyOnHomeForLogout();
        stepTapLogout();
        stepVerifyReturnedToLogin();
    }

    @Step("Precondicion: hacer login con credenciales validas")
    private void stepLoginPrecondition() {
        loginPage.loginAs(TestDataProvider.getValidEmail(), TestDataProvider.getValidPassword());
        AllureHelper.reportAction(
                "Login", "Credenciales demo@demo.com / ****",
                TestDataProvider.getValidEmail(), "Login enviado, esperando Home");
    }

    @Step("Verificar que estamos en Home antes del logout")
    private void stepVerifyOnHomeForLogout() {
        boolean onHome = homePage.isOnHomeScreen();
        String saldo = "";
        try { saldo = homePage.getConsolidatedBalanceText(); } catch (Exception ignored) {}
        AllureHelper.reportValidation(
                "Estamos en Home antes del logout",
                onHome ? "Home detectado - Saldo: " + saldo : "Home NO detectado",
                "Pantalla Home visible",
                onHome,
                "Debe estar en Home para poder hacer logout");
        Assert.assertTrue(onHome, "Debe estar en Home antes de hacer logout");
    }

    @Step("Tap en boton de cerrar sesion (icono esquina superior derecha)")
    private void stepTapLogout() {
        homePage.tapLogout();
        AllureHelper.reportAction(
                "Tap", "Boton cerrar sesion (ViewGroup junto a texto 'Demo')",
                null, "Tap realizado, esperando retorno a Login");
    }

    /**
     * Verifica el retorno a Login con doble estrategia de validacion.
     * <p>
     * Estrategia 1 - Localizador nativo: busca el texto "Bienvenido de nuevo"
     * mediante XPath en el arbol de accesibilidad. Es la validacion primaria.
     * <p>
     * Estrategia 2 - OCR (Tesseract): lee el texto completo de la pantalla
     * y busca "Bienvenido" o "Iniciar sesion". Se usa como validacion
     * secundaria por dos motivos:
     *   a) El icono de logout no tiene content-desc ni resource-id
     *      localizables (es un emoji), por lo que el tap se hace por
     *      coordenadas calculadas dinamicamente. Si el tap fallara por
     *      un cambio de resolucion, el localizador nativo podria no
     *      encontrarse. OCR garantiza una validacion independiente del
     *      arbol de accesibilidad.
     *   b) Cumple el requisito del PDF de implementar validaciones
     *      mediante OCR cuando los selectores nativos pueden no ser
     *      confiables (componentes con emojis sin content-id).
     */
    @Step("Verificar que la app retorno a la pantalla de Login")
    private void stepVerifyReturnedToLogin() {
        boolean onLogin = loginPage.isOnLoginScreen();

        String ocrText = "";
        boolean ocrDetectedLogin = false;
        try {
            ocrText = OCRUtils.extractFullTextFromScreen(DriverFactory.getDriver());
            ocrDetectedLogin = ocrText.toLowerCase().contains("bienvenido")
                    || ocrText.toLowerCase().contains("iniciar sesi");
        } catch (Exception e) {
            System.out.println("[Logout] OCR no disponible: " + e.getMessage());
        }

        boolean returnedToLogin = onLogin || ocrDetectedLogin;

        AllureHelper.reportValidation(
                "Retorno a Login despues de logout",
                onLogin ? "Login detectado por localizador nativo"
                        : (ocrDetectedLogin ? "Login detectado por OCR: '" + ocrText + "'"
                        : "No se detecto pantalla de Login"),
                "Pantalla de Login con 'Bienvenido de nuevo' y 'Iniciar sesion'",
                returnedToLogin,
                "Justificacion OCR: el icono de logout no tiene content-desc ni "
                        + "resource-id localizable (es un emoji), por lo que el tap "
                        + "se hace por coordenadas. OCR valida el retorno a Login de "
                        + "forma independiente al arbol de accesibilidad. Cumple "
                        + "requisito del PDF de validaciones OCR cuando los "
                        + "selectores nativos no son confiables.");
        Assert.assertTrue(returnedToLogin,
                "Despues de logout debe estar en pantalla de Login");
    }
}
