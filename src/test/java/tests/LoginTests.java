package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Allure;
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
 * Cada accion importante tiene una captura con descripcion clara para
 * trazabilidad en el reporte de Allure.
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
        // Captura inicial del app recien abierta
        AllureHelper.screenshot("[INICIO] App DemoBank recien abierta");
        AllureHelper.attachPageInfo();
    }

    @AfterMethod
    public void tearDown() {
        // Captura del estado final antes de cerrar
        AllureHelper.screenshot("[FIN] Estado antes de cerrar sesion");
        DriverFactory.quitDriver();
    }

    /**
     * CASO 1: Autenticacion Exitosa con credenciales validas.
     */
    @Test(priority = 1, groups = {"login", "smoke"})
    @Description("Login exitoso con credenciales demo@demo.com / 1234. "
            + "Capturas paso a paso agregadas al reporte Allure.")
    @Severity(SeverityLevel.CRITICAL)
    public void testLoginExitoso() {
        AllureHelper.screenshot("[PASO 1] Pantalla de Login antes de escribir");

        stepTypeEmail(TestDataProvider.VALID_EMAIL);
        AllureHelper.screenshot("[PASO 2] Email escrito: " + TestDataProvider.VALID_EMAIL);

        stepTypePassword(TestDataProvider.VALID_PASSWORD);
        AllureHelper.screenshot("[PASO 3] Password escrito");

        stepTapLogin();
        AllureHelper.screenshot("[PASO 4] Boton 'Iniciar sesion' presionado");

        // Verificar redireccion al Home
        stepVerifyOnHome();
        AllureHelper.screenshot("[PASO 5] Redirigido al Home EXITOSO ✅");
    }

    @Step("Escribir email: {0}")
    private void stepTypeEmail(String email) {
        loginPage.typeEmail(email);
    }

    @Step("Escribir password")
    private void stepTypePassword(String password) {
        loginPage.typePassword(password);
    }

    @Step("Tap en boton 'Iniciar sesion'")
    private void stepTapLogin() {
        loginPage.tapLoginButton();
    }

    @Step("Verificar que estamos en el Home")
    private void stepVerifyOnHome() {
        Assert.assertTrue(homePage.isOnHomeScreen(), "Debe estar en Home despues de login");
    }

    /**
     * CASO 2: Email vacio -> error.
     */
    @Test(priority = 2, groups = {"login", "negative"})
    @Description("Validacion: email vacio muestra error. Capturas paso a paso.")
    @Severity(SeverityLevel.CRITICAL)
    public void testEmailVacio() {
        loginPage.clearEmail();
        loginPage.typePassword(TestDataProvider.VALID_PASSWORD);
        AllureHelper.screenshot("[PASO 1] Email vacio, password lleno");

        loginPage.tapLoginButton();
        AllureHelper.screenshot("[PASO 2] Despues de tap login con email vacio");

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
                "Debe mostrar error por email vacio");
        AllureHelper.screenshot("[PASO 3] Mensaje de error visible");
    }

    /**
     * CASO 3: Password vacio -> error.
     */
    @Test(priority = 3, groups = {"login", "negative"})
    @Description("Validacion: password vacio muestra error. Capturas paso a paso.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPasswordVacio() {
        loginPage.typeEmail(TestDataProvider.VALID_EMAIL);
        loginPage.clearPassword();
        AllureHelper.screenshot("[PASO 1] Email lleno, password vacio");

        loginPage.tapLoginButton();
        AllureHelper.screenshot("[PASO 2] Despues de tap login con password vacio");

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
                "Debe mostrar error por password vacio");
    }

    /**
     * CASO 4: Toggle del campo password.
     */
    @Test(priority = 4, groups = {"login"})
    @Description("Toggle de password: mostrar/ocultar texto. Capturas del antes/despues.")
    @Severity(SeverityLevel.NORMAL)
    public void testTogglePassword() {
        AllureHelper.screenshot("[PASO 1] Password inicialmente oculto (oculto por defecto)");

        loginPage.tapPasswordToggle();
        AllureHelper.screenshot("[PASO 2] Password visible despues de tap 1");

        loginPage.tapPasswordToggle();
        AllureHelper.screenshot("[PASO 3] Password oculto de nuevo despues de tap 2");
    }
}
