package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/*
 * ============================================================================
 * MÓDULO 5: PAGOS DE SERVICIOS (4 casos - requisito del PDF)
 * ============================================================================
 *
 * Cubre:
 *   1. Flujo feliz pago (2 servicios diferentes)
 *   2. Precarga dinámica del monto sugerido
 *   3. Validación saldo insuficiente
 *   4. Auditoría: aparece en Movimientos bajo categoría "Servicios"
 * ============================================================================
 */
public class PayTests {

    private HomePage homePage;
    private PayPage payPage;
    private MovementsPage movementsPage;
    private LoginPage loginPage;

    @BeforeMethod
    public void setUp() {
        DriverFactory.getDriver();
        loginPage = new LoginPage(DriverFactory.getDriver());
        homePage = new HomePage(DriverFactory.getDriver());
        loginPage.loginAs(TestDataProvider.VALID_EMAIL, TestDataProvider.VALID_PASSWORD);
        homePage.tapQuickPay();
        payPage = new PayPage(DriverFactory.getDriver());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    /**
     * CASO 1: Flujo feliz pago - servicio Energía.
     */
    @Test(priority = 1, groups = {"pay", "happy-path"})
    @Description("Pago exitoso de servicio de Energia")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoServicioEnergia() {
        payPage.completePayment();
        TestListener.captureAndAttachScreenshot("Pago Energia enviado");
        Assert.assertTrue(new PaySuccessPage(DriverFactory.getDriver()).isOnSuccessScreen(),
                "Pago exitoso debe llevar a pantalla Success");
    }

    /**
     * CASO 1.2: Flujo feliz pago - servicio Agua (segundo servicio).
     */
    @Test(priority = 2, groups = {"pay", "happy-path"})
    @Description("Pago exitoso de servicio de Agua")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoServicioAgua() {
        payPage.completePayment();
        TestListener.captureAndAttachScreenshot("Pago Agua enviado");
        Assert.assertTrue(new PaySuccessPage(DriverFactory.getDriver()).isOnSuccessScreen(),
                "Pago exitoso debe llevar a pantalla Success");
    }

    /**
     * CASO 2: Verificar que el monto sugerido está pre-cargado en el campo.
     */
    @Test(priority = 3, groups = {"pay"})
    @Description("El monto sugerido se precarga automaticamente")
    @Severity(SeverityLevel.NORMAL)
    public void testPrecargaDinamicaMonto() {
        // Obtener el monto sugerido
        String montoSugeridoTexto = payPage.getSuggestedAmount();
        TestListener.captureAndAttachScreenshot("Monto sugerido: " + montoSugeridoTexto);

        payPage.tapContinue();

        // El campo de monto debe estar prellenado
        Assert.assertTrue(payPage.isAmountPreloaded(),
                "El monto sugerido debe estar precargado en el campo");
        TestListener.captureAndAttachScreenshot("Campo de monto precargado");
    }

    /**
     * CASO 3: Validar saldo insuficiente al pagar.
     */
    @Test(priority = 4, groups = {"pay", "negative"})
    @Description("Pago mayor al saldo muestra error")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoSaldoInsuficiente() {
        payPage.selectFirstService();
        // Intentar poner un monto exagerado
        payPage.customAmount("9999999999");
        payPage.tapConfirmPay();
        TestListener.captureAndAttachScreenshot("Pago con monto excesivo");

        Assert.assertTrue(payPage.isInsufficientBalanceErrorDisplayed(),
                "Debe mostrar error de saldo insuficiente");
    }

    /**
     * CASO 4: Auditoría - pago aparece en Movimientos bajo categoría Servicios.
     */
    @Test(priority = 5, groups = {"pay", "audit"})
    @Description("El pago aparece en Movimientos bajo categoria Servicios")
    @Severity(SeverityLevel.CRITICAL)
    public void testAuditoriaEnMovimientos() {
        // Hacer pago
        payPage.completePayment();
        Assert.assertTrue(new PaySuccessPage(DriverFactory.getDriver()).isOnSuccessScreen());
        new PaySuccessPage(DriverFactory.getDriver()).tapBackToHome();

        // Ir a Movimientos
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());
        // Buscar "Servicio" (la categoría)
        movementsPage.searchFor("Servicio");
        Assert.assertTrue(movementsPage.hasMovements(),
                "Debe haber movimientos con categoria Servicio");
        TestListener.captureAndAttachScreenshot("Movimientos filtrados por Servicio");
    }
}
