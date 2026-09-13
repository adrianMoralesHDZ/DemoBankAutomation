package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Modulo 4: Transferencias (5 casos).
 * <p>
 * TC13 - Transferencia exitosa
 * TC14 - Saldo insuficiente
 * TC15 - Monto invalido (cero)
 * TC16 - Impacto en saldo (saldo antes/después)
 * TC17 - Auditoria en Movimientos
 */
public class TransferTests {

    private HomePage homePage;
    private TransferPage transferPage;
    private MovementsPage movementsPage;
    private LoginPage loginPage;

    @BeforeMethod
    public void setUp() {
        DriverFactory.getDriver();
        loginPage = new LoginPage(DriverFactory.getDriver());
        homePage = new HomePage(DriverFactory.getDriver());
        loginPage.loginAs(TestDataProvider.VALID_EMAIL, TestDataProvider.VALID_PASSWORD);
        homePage.tapQuickTransfer();
        transferPage = new TransferPage(DriverFactory.getDriver());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    // ========================================================================
    // TC13 - TRANSFERENCIA EXITOSA
    // ========================================================================

    @Test(priority = 1, groups = {"transfer", "happy-path"})
    @Description("TC13 - Transferencia exitosa de $100,000 al primer contacto (Maria Lopez). "
            + "Valida pantalla de exito: titulo, monto y destinatario.")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferenciaExitosa() {
        stepSelectContact();
        stepEnterAmount(TestDataProvider.MONTO_TRANSFER_VALIDO);
        stepConfirmTransfer();
        stepVerifySuccessScreen();
    }

    @Step("Paso 1 - Seleccionar primer contacto (Maria Lopez) de la lista")
    private void stepSelectContact() {
        transferPage.selectFirstContact();
        AllureHelper.reportAction(
                "Seleccionar", "Primer contacto de la lista (Maria Lopez)",
                null, "Contacto seleccionado, pasando a pantalla de monto");
    }

    @Step("Paso 2 - Ingresar monto ${0} y tap Continuar")
    private void stepEnterAmount(double monto) {
        String montoStr = String.valueOf((long) monto);
        transferPage.typeAmount(montoStr);
        AllureHelper.reportAction(
                "Escribir", "Campo de monto (EditText)",
                "$" + montoStr, "Monto escrito, ocultando teclado");
        transferPage.tapContinue();
        AllureHelper.reportAction(
                "Tap", "Boton 'Continuar' (ViewGroup con content-desc)",
                null, "Tap realizado, pasando a pantalla de confirmacion");
    }

    @Step("Paso 3 - Confirmar transferencia")
    private void stepConfirmTransfer() {
        transferPage.tapConfirm();
        AllureHelper.reportAction(
                "Tap", "Boton 'Confirmar transferencia' (ViewGroup con content-desc)",
                null, "Tap realizado, esperando pantalla de exito");
    }

    @Step("Paso 4 - Verificar pantalla de exito: titulo, monto y destinatario")
    private void stepVerifySuccessScreen() {
        TransferSuccessPage successPage = new TransferSuccessPage(DriverFactory.getDriver());

        boolean onSuccess = successPage.isOnSuccessScreen();
        AllureHelper.reportValidation(
                "Pantalla de exito visible",
                onSuccess ? "Pantalla de exito detectada" : "No se detecto pantalla de exito",
                "Pantalla con titulo 'Transferencia exitosa'",
                onSuccess,
                "Despues de confirmar, debe aparecer la pantalla de exito");
        Assert.assertTrue(onSuccess, "Debe estar en pantalla de exito");

        String titulo = successPage.readSuccessTitle();
        boolean tituloOk = titulo.toLowerCase().contains("exitosa");
        AllureHelper.reportValidation(
                "Titulo de la pantalla de exito",
                "Texto leido: '" + titulo + "'",
                "Debe contener 'exitosa'",
                tituloOk,
                "El titulo confirma que la transferencia fue procesada");
        Assert.assertTrue(tituloOk, "El titulo debe contener 'exitosa'. Leido: " + titulo);

        String montoEnPantalla = successPage.getDisplayedAmountText();
        boolean montoOk = montoEnPantalla.contains("100");
        AllureHelper.reportValidation(
                "Monto mostrado en pantalla de exito",
                "Texto leido: '" + montoEnPantalla + "'",
                "Debe contener '100' (de $100,000)",
                montoOk,
                "El monto en pantalla debe coincidir con el transferido");
        Assert.assertTrue(montoOk, "El monto debe ser $100,000. Leido: '" + montoEnPantalla + "'");

        String destinatario = successPage.getRecipientNameShown();
        boolean destOk = destinatario.toLowerCase().contains("maria");
        AllureHelper.reportValidation(
                "Destinatario mostrado en pantalla de exito",
                "Texto leido: '" + destinatario + "'",
                "Debe contener 'Maria'",
                destOk,
                "El destinatario debe coincidir con el contacto seleccionado");
        Assert.assertTrue(destOk, "El destinatario debe ser Maria Lopez. Leido: '" + destinatario + "'");
    }

    // ========================================================================
    // TC14 - SALDO INSUFICIENTE
    // ========================================================================

    @Test(priority = 2, groups = {"transfer", "negative"})
    @Description("TC14 - Transferencia mayor al saldo ($3,000,000) muestra error 'Saldo insuficiente'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testSaldoInsuficiente() {
        stepSelectContactForInsufficient();
        stepEnterExcessiveAmount();
        stepVerifyInsufficientBalanceError();
    }

    @Step("Paso 1 - Seleccionar primer contacto")
    private void stepSelectContactForInsufficient() {
        transferPage.selectFirstContact();
        AllureHelper.reportAction(
                "Seleccionar", "Primer contacto de la lista", null,
                "Contacto seleccionado");
    }

    @Step("Paso 2 - Ingresar monto excesivo ${0} (mayor al saldo) y tap Continuar")
    private void stepEnterExcessiveAmount() {
        String montoStr = String.valueOf((long) TestDataProvider.MONTO_INSUFICIENTE);
        transferPage.typeAmount(montoStr);
        AllureHelper.reportAction(
                "Escribir", "Campo de monto (EditText)",
                "$" + montoStr, "Monto excesivo escrito (mayor al saldo disponible)");
        transferPage.tapContinue();
        AllureHelper.reportAction(
                "Tap", "Boton 'Continuar'", null,
                "Tap realizado, esperando mensaje de saldo insuficiente");
    }

    @Step("Paso 3 - Verificar mensaje 'Saldo insuficiente' visible")
    private void stepVerifyInsufficientBalanceError() {
        boolean errorShown = transferPage.isInsufficientBalanceErrorDisplayed();
        AllureHelper.reportValidation(
                "Mensaje 'Saldo insuficiente'",
                errorShown ? "Mensaje de error visible en pantalla" : "No se detecto mensaje de error",
                "Mensaje 'Saldo insuficiente' visible",
                errorShown,
                "Cuando el monto excede el saldo, la app debe bloquear y mostrar error");
        Assert.assertTrue(errorShown, "Debe mostrar mensaje de saldo insuficiente");
    }

    // ========================================================================
    // TC15 - MONTO INVALIDO (CERO)
    // ========================================================================

    @Test(priority = 3, groups = {"transfer", "negative"})
    @Description("TC15 - Monto cero muestra error 'Ingresa un monto valido'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testMontoInvalido() {
        stepSelectContact();
        stepEnterAmountZero();
        stepVerifyInvalidAmountError();
    }

    @Step("Paso 2 - Ingresar monto cero y tap Continuar")
    private void stepEnterAmountZero() {
        transferPage.typeAmount(TestDataProvider.MONTO_CERO);
        AllureHelper.reportAction(
                "Escribir", "Campo de monto (EditText)",
                "$0", "Monto cero escrito");
        transferPage.tapContinue();
        AllureHelper.reportAction(
                "Tap", "Boton 'Continuar'", null,
                "Tap realizado, esperando mensaje de monto invalido");
    }

    @Step("Paso 3 - Verificar mensaje 'Ingresa un monto valido' visible")
    private void stepVerifyInvalidAmountError() {
        boolean errorShown = transferPage.isInvalidAmountErrorDisplayed();
        AllureHelper.reportValidation(
                "Mensaje 'Ingresa un monto valido'",
                errorShown ? "Mensaje de error visible" : "No se detecto mensaje de error",
                "Mensaje 'Ingresa un monto valido' visible",
                errorShown,
                "El monto cero no es valido para una transferencia");
        Assert.assertTrue(errorShown, "Debe mostrar mensaje 'Ingresa un monto valido'");
    }

    // ========================================================================
    // TC16 - IMPACTO EN SALDO (ANTES/DESPUES)
    // ========================================================================

    @Test(priority = 4, groups = {"transfer"})
    @Description("TC16 - Impacto en saldo: validar descuento exacto en saldo consolidado y Cuenta Corriente.")
    @Severity(SeverityLevel.CRITICAL)
    public void testImpactoSaldoOrigen() {
        double montoTest = TestDataProvider.MONTO_DESCUENTO_TEST;

        double saldoConsolidadoAntes = stepReadConsolidatedBalanceBefore();
        double saldoCuentaAntes = stepReadCurrentAccountBalanceBefore();
        stepExecuteTransfer(montoTest);
        double saldoConsolidadoDespues = stepReadConsolidatedBalanceAfter();
        double saldoCuentaDespues = stepReadCurrentAccountBalanceAfter();

        stepValidateConsolidatedDiscount(saldoConsolidadoAntes, saldoConsolidadoDespues, montoTest);
        stepValidateAccountDiscount(saldoCuentaAntes, saldoCuentaDespues, montoTest);
    }

    @Step("Volver al Home y leer saldo CONSOLIDADO antes de transferir")
    private double stepReadConsolidatedBalanceBefore() {
        driver().navigate().back();
        String texto = homePage.getConsolidatedBalanceText();
        double saldo = homePage.getConsolidatedBalanceTextAsAmount();
        AllureHelper.reportRead(
                "Saldo CONSOLIDADO antes de transferir", texto,
                String.format("$%,.2f", saldo));
        return saldo;
    }

    @Step("Abrir Transferir, seleccionar contacto y leer saldo Cuenta Corriente antes")
    private double stepReadCurrentAccountBalanceBefore() {
        homePage.tapQuickTransfer();
        transferPage.selectFirstContact();
        double saldo = transferPage.getSourceAccountBalance();
        AllureHelper.reportRead(
                "Saldo Cuenta Corriente antes de transferir",
                String.format("$%,.2f", saldo),
                String.format("$%,.2f", saldo));
        return saldo;
    }

    @Step("Ejecutar transferencia de ${0}")
    private void stepExecuteTransfer(double monto) {
        String montoStr = String.valueOf((long) monto);
        transferPage.typeAmount(montoStr);
        transferPage.tapContinue();
        transferPage.tapConfirm();
        AllureHelper.reportAction(
                "Transferir", "Flujo completo: contacto -> monto -> confirmar",
                "$" + montoStr, "Transferencia ejecutada por $" + montoStr);
    }

    @Step("Volver al Home y leer saldo CONSOLIDADO despues de transferir")
    private double stepReadConsolidatedBalanceAfter() {
        new TransferSuccessPage(DriverFactory.getDriver()).tapBackToHome();
        String texto = homePage.getConsolidatedBalanceText();
        double saldo = homePage.getConsolidatedBalanceTextAsAmount();
        AllureHelper.reportRead(
                "Saldo CONSOLIDADO despues de transferir", texto,
                String.format("$%,.2f", saldo));
        return saldo;
    }

    @Step("Tap tab 'Cuenta Corriente' y leer saldo despues de transferir")
    private double stepReadCurrentAccountBalanceAfter() {
        homePage.tapCurrentAccountTab();
        String info = homePage.getAccountInfoText();
        double saldo = homePage.getAccountBalanceAsAmount();
        AllureHelper.reportRead(
                "Saldo Cuenta Corriente despues de transferir", info,
                String.format("$%,.2f", saldo));
        return saldo;
    }

    @Step("Validar: Saldo consolidado antes - despues = ${2} (monto transferido)")
    private void stepValidateConsolidatedDiscount(double antes, double despues, double monto) {
        double diferencia = antes - despues;
        boolean passed = Math.abs(diferencia - monto) < 0.01;
        String detalle = String.format(
                "Antes: $%,.0f - Despues: $%,.0f = Diferencia: $%,.0f (esperado: $%,.0f)",
                antes, despues, diferencia, monto);
        AllureHelper.reportValidation(
                "Descuento en saldo CONSOLIDADO",
                String.format("$%,.2f (diferencia real)", diferencia),
                String.format("$%,.2f (monto transferido)", monto),
                passed, detalle);
        Assert.assertEquals(diferencia, monto, 0.01, detalle);
    }

    @Step("Validar: Saldo Cuenta Corriente antes - despues = ${2} (monto transferido)")
    private void stepValidateAccountDiscount(double antes, double despues, double monto) {
        double diferencia = antes - despues;
        boolean passed = Math.abs(diferencia - monto) < 0.01;
        String detalle = String.format(
                "Antes: $%,.0f - Despues: $%,.0f = Diferencia: $%,.0f (esperado: $%,.0f)",
                antes, despues, diferencia, monto);
        AllureHelper.reportValidation(
                "Descuento en saldo CUENTA CORRIENTE",
                String.format("$%,.2f (diferencia real)", diferencia),
                String.format("$%,.2f (monto transferido)", monto),
                passed, detalle);
        Assert.assertEquals(diferencia, monto, 0.01, detalle);
    }

    // ========================================================================
    // TC17 - AUDITORIA EN MOVIMIENTOS
    // ========================================================================

    @Test(priority = 5, groups = {"transfer", "audit"})
    @Description("TC17 - Auditoria: transferencia registrada en Movimientos con monto debitado (-).")
    @Severity(SeverityLevel.CRITICAL)
    public void testAuditoriaEnMovimientos() {
        stepExecuteTransferForAudit();
        stepGoToMovementsFromSuccess();
        stepSearchTransferInMovements();
        stepVerifyDebitedAmount();
    }

    @Step("Paso 1 - Ejecutar transferencia de ${0} para auditoria")
    private void stepExecuteTransferForAudit() {
        transferPage.completeTransfer(TestDataProvider.MONTO_AUDITORIA_TRANSFER);
        AllureHelper.reportAction(
                "Transferir", "Flujo completo automatico",
                "$" + TestDataProvider.MONTO_AUDITORIA_TRANSFER,
                "Transferencia de $" + TestDataProvider.MONTO_AUDITORIA_TRANSFER + " ejecutada");
    }

    @Step("Paso 2 - Volver al Home y navegar a Movimientos")
    private void stepGoToMovementsFromSuccess() {
        new TransferSuccessPage(DriverFactory.getDriver()).tapBackToHome();
        AllureHelper.reportNavigation(
                "Pantalla de exito", "Home", true);
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());
        boolean onMovements = movementsPage.isOnMovementsScreen();
        AllureHelper.reportNavigation(
                "Home", "Pantalla de Movimientos", onMovements);
        Assert.assertTrue(onMovements, "Debe estar en pantalla de Movimientos");
    }

    @Step("Paso 3 - Buscar 'Transferencia' en Movimientos")
    private void stepSearchTransferInMovements() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_TRANSFERENCIA);
        boolean has = movementsPage.hasMovements();
        int count = movementsPage.getAllMovementAmounts().size();
        AllureHelper.reportValidation(
                "Busqueda de 'Transferencia' en Movimientos",
                has ? count + " movimientos encontrados" : "No se encontraron movimientos",
                "Al menos 1 movimiento de tipo Transferencia",
                has,
                "La transferencia realizada debe aparecer registrada en Movimientos");
        Assert.assertTrue(has, "Debe haber movimientos de transferencia");
    }

    @Step("Paso 4 - Validar monto debitado (-${0}) en Movimientos")
    private void stepVerifyDebitedAmount() {
        List<WebElement> montos = movementsPage.getAllMovementAmounts();
        Assert.assertFalse(montos.isEmpty(), "Debe haber al menos un monto");

        boolean encontrado = false;
        String montoEncontrado = "";
        for (WebElement monto : montos) {
            String textoMonto = monto.getText();
            if (textoMonto.startsWith("-") && textoMonto.contains(TestDataProvider.MONTO_AUDITORIA_TRANSFER)) {
                encontrado = true;
                montoEncontrado = textoMonto;
                break;
            }
        }

        AllureHelper.reportValidation(
                "Monto debitado en Movimientos",
                encontrado ? "Movimiento encontrado: '" + montoEncontrado + "'" : "No se encontro el monto debitado",
                "Movimiento con monto '-$" + TestDataProvider.MONTO_AUDITORIA_TRANSFER + "'",
                encontrado,
                "La transferencia debe registrarse como debito (signo negativo) por el monto exacto");
        Assert.assertTrue(encontrado,
                "Debe haber un movimiento con monto -$" + TestDataProvider.MONTO_AUDITORIA_TRANSFER);
    }

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }
}
