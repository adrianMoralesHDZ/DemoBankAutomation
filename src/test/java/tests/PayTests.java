package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/*
 * ============================================================================
 * MODULO 5: PAGOS DE SERVICIOS (5 casos - requisito del PDF)
 * ============================================================================
 *
 * Cubre:
 *   TC18 - Pago exitoso - servicio Energia
 *   TC19 - Pago exitoso - servicio Agua
 *   TC20 - Precarga de monto automatica
 *   TC21 - Saldo insuficiente
 *   TC22 - Monto invalido (cero)
 *   TC23 - Auditoria en Movimientos (categoria Servicios)
 *
 * REPORTES ALLURE:
 *   - Cada paso del test esta anotado con @Step para que el reporte
 *     muestre el detalle paso a paso de la ejecucion.
 *   - Los screenshots se capturan AUTOMATICAMENTE solo en caso de fallo
 *     (gestionado por TestListener.onTestFailure).
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

    // ========================================================================
    // TC18 - PAGO EXITOSO: SERVICIO ENERGIA
    // ========================================================================

    /**
     * TC18: Pago exitoso del servicio de Energia.
     * Incluye validacion de precarga dinamica del monto sugerido.
     */
    @Test(priority = 1, groups = {"pay", "happy-path"})
    @Description("TC18 - Pago exitoso de servicio de Energia. "
            + "Incluye validacion de monto precargado automaticamente.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoServicioEnergia() {
        stepSelectService("Energia");
        stepVerifyAmountPreloaded();
        stepContinueAndConfirm();
        stepVerifyPaySuccess("Energ");
    }

    @Step("Paso 1 - Seleccionar servicio: {0}")
    private void stepSelectService(String serviceName) {
        payPage.selectService(serviceName);
        AllureHelper.logAction("Seleccionar", "Servicio: " + serviceName);
    }

    @Step("Paso 2 - Verificar que el monto esta precargado automaticamente")
    private void stepVerifyAmountPreloaded() {
        boolean preloaded = payPage.isAmountPreloaded();
        String monto = payPage.getSuggestedAmount();
        AllureHelper.logValidation("Monto precargado", preloaded, true, preloaded);
        AllureHelper.logStep("Monto sugerido mostrado: " + monto);
        Assert.assertTrue(preloaded, "El monto sugerido debe estar precargado en el campo");
    }

    @Step("Paso 3 - Tap 'Continuar' y luego 'Confirmar pago'")
    private void stepContinueAndConfirm() {
        payPage.tapContinue();
        AllureHelper.logAction("Tap", "Boton 'Continuar'");
        payPage.tapConfirmPay();
        AllureHelper.logAction("Tap", "Boton 'Confirmar pago'");
    }

    @Step("Paso 4 - Verificar pantalla de exito y nombre del servicio contiene '{0}'")
    private void stepVerifyPaySuccess(String expectedServiceFragment) {
        PaySuccessPage successPage = new PaySuccessPage(DriverFactory.getDriver());

        boolean onSuccess = successPage.isOnSuccessScreen();
        AllureHelper.logValidation("Pantalla de exito pago", onSuccess, true, onSuccess);
        Assert.assertTrue(onSuccess, "Pago exitoso debe llevar a pantalla Success");

        String detalle = successPage.getServiceName();
        boolean serviceOk = detalle.contains(expectedServiceFragment);
        AllureHelper.logValidation("Servicio pagado", detalle,
                "contiene '" + expectedServiceFragment + "'", serviceOk);
        Assert.assertTrue(serviceOk,
                "El servicio pagado debe contener '" + expectedServiceFragment
                        + "'. Texto encontrado: " + detalle);
    }

    // ========================================================================
    // TC19 - PAGO EXITOSO: SERVICIO AGUA
    // ========================================================================

    /**
     * TC19: Pago exitoso del servicio de Agua.
     * Incluye validacion de precarga dinamica del monto sugerido.
     */
    @Test(priority = 2, groups = {"pay", "happy-path"})
    @Description("TC19 - Pago exitoso de servicio de Agua. "
            + "Incluye validacion de monto precargado automaticamente.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoServicioAgua() {
        stepSelectService("Agua");
        stepVerifyAmountPreloaded();
        stepContinueAndConfirm();
        stepVerifyPaySuccess("Agua");
    }

    // ========================================================================
    // TC20 - PRECARGA DE MONTO
    // ========================================================================

    /**
     * TC20: Validar que al seleccionar un servicio, el monto se precarga
     * automaticamente en el campo editable.
     */
    @Test(priority = 3, groups = {"pay"})
    @Description("TC20 - Precarga de monto: al seleccionar un servicio el monto se carga automaticamente.")
    @Severity(SeverityLevel.NORMAL)
    public void testPrecargaMonto() {
        stepSelectFirstService();
        stepVerifyAmountPreloaded();
    }

    @Step("Seleccionar el primer servicio disponible")
    private void stepSelectFirstService() {
        payPage.selectFirstService();
        AllureHelper.logAction("Seleccionar", "Primer servicio disponible");
    }

    // ========================================================================
    // TC21 - SALDO INSUFICIENTE
    // ========================================================================

    /**
     * TC21: Pago con monto mayor al saldo muestra error.
     */
    @Test(priority = 4, groups = {"pay", "negative"})
    @Description("TC21 - Pago con monto mayor al saldo muestra 'Saldo insuficiente'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoSaldoInsuficiente() {
        stepSelectFirstService();
        stepEnterExcessiveAmount();
        stepVerifyInsufficientBalanceError();
    }

    @Step("Ingresar monto excesivo: $9,999,999,999")
    private void stepEnterExcessiveAmount() {
        payPage.customAmount(String.valueOf((long) TestDataProvider.MONTO_PAGO_INSUFICIENTE));
        AllureHelper.logAction("Escribir", "Monto excesivo");
        payPage.tapContinue();
        AllureHelper.logAction("Tap", "Boton 'Continuar'");
    }

    @Step("Verificar mensaje 'Saldo insuficiente' visible")
    private void stepVerifyInsufficientBalanceError() {
        boolean errorShown = payPage.isInsufficientBalanceErrorDisplayed();
        AllureHelper.logValidation("Mensaje 'Saldo insuficiente'", errorShown, true, errorShown);
        Assert.assertTrue(errorShown, "Debe mostrar error de saldo insuficiente");
    }

    // ========================================================================
    // TC22 - MONTO INVALIDO (CERO)
    // ========================================================================

    /**
     * TC22: Monto cero debe mostrar "monto valido".
     */
    @Test(priority = 5, groups = {"pay", "negative"})
    @Description("TC22 - Monto cero muestra error 'monto valido'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testMontoInvalido() {
        stepSelectFirstService();
        stepEnterZeroAmount();
        stepVerifyInvalidAmountError();
    }

    @Step("Ingresar monto cero")
    private void stepEnterZeroAmount() {
        payPage.customAmount(TestDataProvider.MONTO_CERO);
        AllureHelper.logAction("Escribir", "Monto: $0");
        payPage.tapContinue();
        AllureHelper.logAction("Tap", "Boton 'Continuar'");
    }

    @Step("Verificar mensaje 'monto valido' visible")
    private void stepVerifyInvalidAmountError() {
        boolean errorShown = payPage.isInvalidAmountErrorDisplayed();
        AllureHelper.logValidation("Mensaje 'monto valido'", errorShown, true, errorShown);
        Assert.assertTrue(errorShown, "Debe mostrar mensaje de error por monto invalido");
    }

    // ========================================================================
    // TC23 - AUDITORIA EN MOVIMIENTOS (CATEGORIA SERVICIOS)
    // ========================================================================

    /**
     * TC23: Auditoria - el pago aparece en Movimientos bajo categoria Servicios.
     *
     * Valida:
     *   1. El pago se proceso (pantalla de exito)
     *   2. Al ir a Movimientos y buscar "Servicio", aparece el registro
     *   3. El monto debitado empieza con "-" y coincide con el pagado
     */
    @Test(priority = 6, groups = {"pay", "audit"})
    @Description("TC23 - Auditoria: pago aparece en Movimientos con monto debitado (-) igual al pagado.")
    @Severity(SeverityLevel.CRITICAL)
    public void testAuditoriaEnMovimientos() {
        stepExecutePaymentForAudit();
        String montoPagado = stepVerifySuccessAndCaptureAmount();
        stepGoToMovementsFromSuccess();
        stepSearchServiceInMovements();
        stepVerifyDebitedAmount(montoPagado);
    }

    @Step("Paso 1 - Ejecutar pago del primer servicio")
    private void stepExecutePaymentForAudit() {
        payPage.completePayment(null);
        AllureHelper.logAction("Pagar", "Primer servicio (completo)");
    }

    @Step("Paso 2 - Verificar exito y capturar monto pagado")
    private String stepVerifySuccessAndCaptureAmount() {
        PaySuccessPage successPage = new PaySuccessPage(DriverFactory.getDriver());
        boolean onSuccess = successPage.isOnSuccessScreen();
        AllureHelper.logValidation("Pantalla de exito pago", onSuccess, true, onSuccess);
        Assert.assertTrue(onSuccess, "Pago exitoso debe llevar a pantalla Success");

        String montoPagado = successPage.getAmountText();
        AllureHelper.logStep("Monto pagado capturado: " + montoPagado);
        return montoPagado;
    }

    @Step("Paso 3 - Volver al Home y navegar a Movimientos")
    private void stepGoToMovementsFromSuccess() {
        new PaySuccessPage(DriverFactory.getDriver()).tapBackToHome();
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());
        AllureHelper.logAction("Navegar", "Home -> Movimientos");
    }

    @Step("Paso 4 - Buscar 'Servicio' en Movimientos")
    private void stepSearchServiceInMovements() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_SERVICIO);
        boolean has = movementsPage.hasMovements();
        AllureHelper.logValidation("Movimientos con categoria Servicio", has, true, has);
        Assert.assertTrue(has, "Debe haber movimientos con categoria Servicio");
    }

    @Step("Paso 5 - Validar monto debitado (-{0}) en Movimientos")
    private void stepVerifyDebitedAmount(String montoPagado) {
        List<WebElement> montos = movementsPage.getAllMovementAmounts();
        Assert.assertFalse(montos.isEmpty(), "Debe haber al menos un monto en movimientos");

        boolean encontrado = false;
        for (WebElement monto : montos) {
            String textoMonto = monto.getText();
            AllureHelper.logStep("Movimiento encontrado: " + textoMonto);
            if (textoMonto.startsWith("-") && textoMonto.contains(montoPagado)) {
                encontrado = true;
                break;
            }
        }

        AllureHelper.logValidation(
                "Monto debitado en Movimientos",
                encontrado,
                true,
                encontrado
        );
        Assert.assertTrue(encontrado,
                "Debe haber un movimiento con monto -" + montoPagado
                        + " (debitado). Montos encontrados: " + montos.size());
    }
}
