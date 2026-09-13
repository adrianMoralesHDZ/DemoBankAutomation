package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/*
 * ============================================================================
 * MÓDULO 5: PAGOS DE SERVICIOS (requisito del PDF)
 * ============================================================================
 *
 * Cubre:
 *   1. Flujo feliz pago (2 servicios diferentes)
 *      - Incluye validación de precarga dinámica del monto sugerido
 *   2. Validación saldo insuficiente
 *   3. Validación monto inválido
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
     * Incluye validación de precarga dinámica del monto sugerido.
     */
    @Test(priority = 1, groups = {"pay", "happy-path"})
    @Description("Pago exitoso de servicio de Energia. "
            + "Incluye validacion de monto precargado automaticamente.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoServicioEnergia() {
        // Paso 1: Seleccionar servicio
        payPage.selectService("Energía");
        TestListener.captureAndAttachScreenshot("Servicio Energía seleccionado");

        // Paso 2: Validar que el monto está precargado automáticamente
        Assert.assertTrue(payPage.isAmountPreloaded(), "El monto sugerido debe estar precargado en el campo");
        String monto = payPage.getSuggestedAmount();
        TestListener.captureAndAttachScreenshot("Monto precargado: " + monto);

        // Paso 3: Continuar y confirmar
        payPage.tapContinue();
        payPage.tapConfirmPay();
        TestListener.captureAndAttachScreenshot("Pago Energía enviado");

        // Paso 4: Validar pantalla de éxito y nombre del servicio
        PaySuccessPage successPage = new PaySuccessPage(DriverFactory.getDriver());
        Assert.assertTrue(successPage.isOnSuccessScreen(),
                "Pago exitoso debe llevar a pantalla Success");

        String detalle = successPage.getServiceName();
        TestListener.captureAndAttachScreenshot("Detalle pago: " + detalle);
        Assert.assertTrue(detalle.contains("Energ"),
                "El servicio pagado debe ser Energia. Texto encontrado: " + detalle);
    }

    /**
     * CASO 1.2: Flujo feliz pago - servicio Agua (segundo servicio).
     * Incluye validación de precarga dinámica del monto sugerido.
     */
    @Test(priority = 2, groups = {"pay", "happy-path"})
    @Description("Pago exitoso de servicio de Agua. "
            + "Incluye validacion de monto precargado automaticamente.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoServicioAgua() {
        // Paso 1: Seleccionar servicio
        payPage.selectService("Agua");
        TestListener.captureAndAttachScreenshot("Servicio Agua seleccionado");

        // Paso 2: Validar que el monto está precargado automáticamente
        Assert.assertTrue(payPage.isAmountPreloaded(),
                "El monto sugerido debe estar precargado en el campo");
        String monto = payPage.getSuggestedAmount();
        TestListener.captureAndAttachScreenshot("Monto precargado: " + monto);

        // Paso 3: Continuar y confirmar
        payPage.tapContinue();
        payPage.tapConfirmPay();
        TestListener.captureAndAttachScreenshot("Pago Agua enviado");

        // Paso 4: Validar pantalla de éxito y nombre del servicio
        PaySuccessPage successPage = new PaySuccessPage(DriverFactory.getDriver());
        Assert.assertTrue(successPage.isOnSuccessScreen(),
                "Pago exitoso debe llevar a pantalla Success");

        String detalle = successPage.getServiceName();
        TestListener.captureAndAttachScreenshot("Detalle pago: " + detalle);
        Assert.assertTrue(detalle.contains("Agua"),
                "El servicio pagado debe ser Agua. Texto encontrado: " + detalle);
    }

    /**
     * CASO 2: Validar saldo insuficiente al pagar.
     */
    @Test(priority = 3, groups = {"pay", "negative"})
    @Description("Pago mayor al saldo muestra error")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoSaldoInsuficiente() {
        payPage.selectFirstService();
        payPage.customAmount("9999999999");
        payPage.tapContinue();
        TestListener.captureAndAttachScreenshot("Pago con monto excesivo");

        Assert.assertTrue(payPage.isInsufficientBalanceErrorDisplayed(), "Debe mostrar error de saldo insuficiente");
    }

    /**
     * CASO 3: Validar monto inválido (vacío o cero).
     */
    @Test(priority = 4, groups = {"pay", "negative"})
    @Description("Monto vacio o cero debe mostrar error 'monto válido'")
    @Severity(SeverityLevel.CRITICAL)
    public void testMontoInvalido() {
        payPage.selectFirstService();
        payPage.customAmount("0");
        payPage.tapContinue();
        TestListener.captureAndAttachScreenshot("Monto cero ingresado");

        Assert.assertTrue(payPage.isInvalidAmountErrorDisplayed(),
                "Debe mostrar mensaje de error por monto invalido");
    }

    /**
     * CASO 4: Auditoría - pago aparece en Movimientos bajo categoría Servicios.
     *
     * Valida que:
     *   1. El pago se procesó (pantalla de éxito)
     *   2. Captura el monto pagado en la pantalla de éxito
     *   3. Al ir a Movimientos y buscar el servicio, aparece el registro
     *   4. El monto debitado en Movimientos empieza con "-" y es igual al pagado
     */
    @Test(priority = 5, groups = {"pay", "audit"})
    @Description("El pago aparece en Movimientos con monto debitado (-) igual al pagado")
    @Severity(SeverityLevel.CRITICAL)
    public void testAuditoriaEnMovimientos() {
        // 1. Hacer pago
        payPage.completePayment(null);

        // 2. Validar éxito y capturar monto pagado
        PaySuccessPage successPage = new PaySuccessPage(DriverFactory.getDriver());
        Assert.assertTrue(successPage.isOnSuccessScreen(),
                "Pago exitoso debe llevar a pantalla Success");

        String montoPagado = successPage.getAmountText();
        TestListener.captureAndAttachScreenshot("Monto pagado: " + montoPagado);

        // 3. Volver al inicio
        successPage.tapBackToHome();

        // 4. Ir a Movimientos
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());

        // 5. Buscar "Servicio" para filtrar
        movementsPage.searchFor("Servicio");
        Assert.assertTrue(movementsPage.hasMovements(),
                "Debe haber movimientos con categoria Servicio");
        TestListener.captureAndAttachScreenshot("Movimientos filtrados por Servicio");

        // 6. Validar que el monto debitado sea negativo y coincida con el pagado
        List<WebElement> montos = movementsPage.getAllMovementAmounts();
        Assert.assertFalse(montos.isEmpty(),
                "Debe haber al menos un monto en movimientos");

        boolean encontrado = false;
        for (WebElement monto : montos) {
            String textoMonto = monto.getText();
            System.out.println("[AUDITORIA] Movimiento: " + textoMonto);
            // El monto debitado debe empezar con "-" y contener el valor pagado
            if (textoMonto.startsWith("-") && textoMonto.contains(montoPagado)) {
                encontrado = true;
                break;
            }
        }

        Assert.assertTrue(encontrado,
                "Debe haber un movimiento con monto -" + montoPagado
                        + " (debitado). Montos encontrados: " + montos.size());
        TestListener.captureAndAttachScreenshot("Auditoria OK: monto debitado encontrado");
    }
}
