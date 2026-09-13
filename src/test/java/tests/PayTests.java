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

/**
 * Modulo 5: Pagos de Servicios (6 casos).
 * <p>
 * TC18 - Pago exitoso - servicio Energia
 * TC19 - Pago exitoso - servicio Agua
 * TC20 - Precarga de monto automatica
 * TC21 - Saldo insuficiente
 * TC22 - Monto invalido (cero)
 * TC23 - Auditoria en Movimientos (categoria Servicios)
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
        loginPage.loginAs(TestDataProvider.getValidEmail(), TestDataProvider.getValidPassword());
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

    @Test(priority = 1, groups = {"pay", "happy-path"})
    @Description("TC18 - Pago exitoso de servicio de Energia. "
            + "Incluye validacion de monto precargado automaticamente.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoServicioEnergia() {
        stepSelectService("Energ");
        stepVerifyAmountPreloaded();
        stepContinueAndConfirm();
        stepVerifyPaySuccess("Energ");
    }

    @Step("Paso 1 - Seleccionar servicio '{0}' de la lista")
    private void stepSelectService(String serviceName) {
        payPage.selectService(serviceName);
        AllureHelper.reportAction(
                "Seleccionar", "Servicio '" + serviceName + "' (ViewGroup clickable)",
                serviceName, "Servicio seleccionado, pasando a pantalla de monto");
    }

    @Step("Paso 2 - Verificar que el monto esta precargado automaticamente")
    private void stepVerifyAmountPreloaded() {
        boolean preloaded = payPage.isAmountPreloaded();
        String monto = payPage.getSuggestedAmount();
        AllureHelper.reportValidation(
                "Monto precargado automaticamente",
                preloaded ? "Campo con valor: '" + monto + "'" : "Campo vacio, sin monto precargado",
                "Campo de monto con valor sugerido por el servicio",
                preloaded,
                "Al seleccionar un servicio, la app debe precargar el monto sugerido");
        Assert.assertTrue(preloaded, "El monto sugerido debe estar precargado");
    }

    @Step("Paso 3 - Tap 'Continuar' y luego 'Confirmar pago'")
    private void stepContinueAndConfirm() {
        payPage.tapContinue();
        AllureHelper.reportAction(
                "Tap", "Boton 'Continuar'", null,
                "Tap realizado, pasando a pantalla de confirmacion");
        payPage.tapConfirmPay();
        AllureHelper.reportAction(
                "Tap", "Boton 'Confirmar pago'", null,
                "Tap realizado, esperando pantalla de exito");
    }

    @Step("Paso 4 - Verificar pantalla de exito y nombre del servicio contiene '{0}'")
    private void stepVerifyPaySuccess(String expectedServiceFragment) {
        PaySuccessPage successPage = new PaySuccessPage(DriverFactory.getDriver());

        boolean onSuccess = successPage.isOnSuccessScreen();
        AllureHelper.reportValidation(
                "Pantalla de exito de pago",
                onSuccess ? "Pantalla de exito detectada" : "No se detecto pantalla de exito",
                "Pantalla con titulo 'Pago exitoso'",
                onSuccess,
                "Despues de confirmar, debe aparecer la pantalla de exito");
        Assert.assertTrue(onSuccess, "Pago exitoso debe llevar a pantalla Success");

        String detalle = successPage.getSuccessDetailText();
        boolean serviceOk = detalle.contains(expectedServiceFragment);
        AllureHelper.reportValidation(
                "Servicio pagado coincide",
                "Texto en pantalla: '" + detalle + "'",
                "Debe contener '" + expectedServiceFragment + "'",
                serviceOk,
                "El detalle del pago debe mostrar el nombre del servicio seleccionado");
        Assert.assertTrue(serviceOk,
                "El servicio pagado debe contener '" + expectedServiceFragment + "'. Leido: " + detalle);
    }

    // ========================================================================
    // TC19 - PAGO EXITOSO: SERVICIO AGUA
    // ========================================================================

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

    @Test(priority = 3, groups = {"pay"})
    @Description("TC20 - Precarga de monto: al seleccionar un servicio el monto se carga automaticamente.")
    @Severity(SeverityLevel.NORMAL)
    public void testPrecargaMonto() {
        stepSelectFirstService();
        stepVerifyAmountPreloaded();
    }

    @Step("Seleccionar el primer servicio disponible (Energia Electrica)")
    private void stepSelectFirstService() {
        payPage.selectFirstService();
        AllureHelper.reportAction(
                "Seleccionar", "Primer servicio de la lista (Energia Electrica)",
                null, "Servicio seleccionado");
    }

    // ========================================================================
    // TC21 - SALDO INSUFICIENTE
    // ========================================================================

    @Test(priority = 4, groups = {"pay", "negative"})
    @Description("TC21 - Pago con monto mayor al saldo muestra 'Saldo insuficiente'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testPagoSaldoInsuficiente() {
        stepSelectFirstService();
        stepEnterExcessiveAmount();
        stepVerifyInsufficientBalanceError();
    }

    @Step("Ingresar monto excesivo ${0} (mayor al saldo) y tap Continuar")
    private void stepEnterExcessiveAmount() {
        String montoStr = String.valueOf((long) TestDataProvider.MONTO_PAGO_INSUFICIENTE);
        payPage.customAmount(montoStr);
        AllureHelper.reportAction(
                "Escribir", "Campo de monto (EditText)",
                "$" + montoStr, "Monto excesivo escrito (mayor al saldo disponible)");
        payPage.tapContinue();
        AllureHelper.reportAction(
                "Tap", "Boton 'Continuar'", null,
                "Tap realizado, esperando mensaje de saldo insuficiente");
    }

    @Step("Verificar mensaje 'Saldo insuficiente' visible")
    private void stepVerifyInsufficientBalanceError() {
        boolean errorShown = payPage.isInsufficientBalanceErrorDisplayed();
        AllureHelper.reportValidation(
                "Mensaje 'Saldo insuficiente'",
                errorShown ? "Mensaje de error visible" : "No se detecto mensaje de error",
                "Mensaje 'Saldo insuficiente' visible",
                errorShown,
                "Cuando el monto del pago excede el saldo, la app debe bloquear y mostrar error");
        Assert.assertTrue(errorShown, "Debe mostrar error de saldo insuficiente");
    }

    // ========================================================================
    // TC22 - MONTO INVALIDO (CERO)
    // ========================================================================

    @Test(priority = 5, groups = {"pay", "negative"})
    @Description("TC22 - Monto cero muestra error 'monto valido'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testMontoInvalido() {
        stepSelectFirstService();
        stepEnterZeroAmount();
        stepVerifyInvalidAmountError();
    }

    @Step("Ingresar monto cero y tap Continuar")
    private void stepEnterZeroAmount() {
        payPage.customAmount(TestDataProvider.MONTO_CERO);
        AllureHelper.reportAction(
                "Escribir", "Campo de monto (EditText)",
                "$0", "Monto cero escrito");
        payPage.tapContinue();
        AllureHelper.reportAction(
                "Tap", "Boton 'Continuar'", null,
                "Tap realizado, esperando mensaje de monto invalido");
    }

    @Step("Verificar mensaje 'monto valido' visible")
    private void stepVerifyInvalidAmountError() {
        boolean errorShown = payPage.isInvalidAmountErrorDisplayed();
        AllureHelper.reportValidation(
                "Mensaje 'monto valido'",
                errorShown ? "Mensaje de error visible" : "No se detecto mensaje de error",
                "Mensaje 'Ingresa un monto valido' visible",
                errorShown,
                "El monto cero no es valido para un pago");
        Assert.assertTrue(errorShown, "Debe mostrar mensaje de error por monto invalido");
    }

    // ========================================================================
    // TC23 - AUDITORIA EN MOVIMIENTOS (CATEGORIA SERVICIOS)
    // ========================================================================

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
        AllureHelper.reportAction(
                "Pagar", "Flujo completo automatico: servicio -> continuar -> confirmar",
                null, "Pago ejecutado");
    }

    @Step("Paso 2 - Verificar exito y capturar monto pagado")
    private String stepVerifySuccessAndCaptureAmount() {
        PaySuccessPage successPage = new PaySuccessPage(DriverFactory.getDriver());
        boolean onSuccess = successPage.isOnSuccessScreen();
        AllureHelper.reportValidation(
                "Pantalla de exito de pago",
                onSuccess ? "Pantalla de exito detectada" : "No se detecto pantalla de exito",
                "Pantalla con 'Pago exitoso'",
                onSuccess,
                "El pago debe procesarse correctamente");
        Assert.assertTrue(onSuccess, "Pago exitoso debe llevar a pantalla Success");

        String detalle = successPage.getSuccessDetailText();
        String montoPagado = successPage.getAmountText();
        AllureHelper.reportRead(
                "Detalle del pago y monto pagado", detalle, montoPagado);
        return montoPagado;
    }

    @Step("Paso 3 - Volver al Home y navegar a Movimientos")
    private void stepGoToMovementsFromSuccess() {
        new PaySuccessPage(DriverFactory.getDriver()).tapBackToHome();
        AllureHelper.reportNavigation("Pantalla de exito", "Home", true);
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());
        AllureHelper.reportNavigation("Home", "Pantalla de Movimientos", true);
    }

    @Step("Paso 4 - Buscar 'Servicio' en Movimientos")
    private void stepSearchServiceInMovements() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_SERVICIO);
        boolean has = movementsPage.hasMovements();
        int count = movementsPage.getAllMovementAmounts().size();
        AllureHelper.reportValidation(
                "Busqueda de 'Servicio' en Movimientos",
                has ? count + " movimientos encontrados" : "No se encontraron movimientos",
                "Al menos 1 movimiento con categoria Servicio",
                has,
                "El pago realizado debe aparecer registrado en Movimientos bajo categoria Servicios");
        Assert.assertTrue(has, "Debe haber movimientos con categoria Servicio");
    }

    @Step("Paso 5 - Validar monto debitado (-{0}) en Movimientos")
    private void stepVerifyDebitedAmount(String montoPagado) {
        List<WebElement> montos = movementsPage.getAllMovementAmounts();
        Assert.assertFalse(montos.isEmpty(), "Debe haber al menos un monto");

        boolean encontrado = false;
        String montoEncontrado = "";
        for (WebElement monto : montos) {
            String textoMonto = monto.getText();
            if (textoMonto.startsWith("-") && textoMonto.contains(montoPagado)) {
                encontrado = true;
                montoEncontrado = textoMonto;
                break;
            }
        }

        AllureHelper.reportValidation(
                "Monto debitado en Movimientos",
                encontrado ? "Movimiento encontrado: '" + montoEncontrado + "'" : "No se encontro el monto",
                "Movimiento con monto '-" + montoPagado + "'",
                encontrado,
                "El pago debe registrarse como debito (signo negativo) por el monto exacto pagado");
        Assert.assertTrue(encontrado,
                "Debe haber un movimiento con monto -" + montoPagado);
    }
}
