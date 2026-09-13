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

/*
 * ============================================================================
 * MODULO 4: TRANSFERENCIAS (5 casos - requisito del PDF)
 * ============================================================================
 *
 * Cubre:
 *   TC13 - Transferencia exitosa
 *   TC14 - Saldo insuficiente
 *   TC15 - Monto invalido (cero)
 *   TC16 - Impacto en saldo (saldo antes/después)
 *   TC17 - Auditoria en Movimientos
 *
 * REPORTES ALLURE:
 *   - Cada paso del test esta anotado con @Step para que el reporte
 *     muestre el detalle paso a paso de la ejecucion.
 *   - Los screenshots se capturan AUTOMATICAMENTE solo en caso de fallo
 *     (gestionado por TestListener.onTestFailure).
 * ============================================================================
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

    /**
     * TC13: Transferencia exitosa de $100,000 al primer contacto.
     *
     * Flujo:
     *   1. Seleccionar contacto (Maria Lopez)
     *   2. Ingresar monto: $100,000
     *   3. Confirmar transferencia
     *   4. Validar pantalla de exito (titulo, monto, destinatario)
     */
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

    @Step("Paso 1 - Seleccionar primer contacto (Maria Lopez)")
    private void stepSelectContact() {
        transferPage.selectFirstContact();
        AllureHelper.logAction("Seleccionar", "Primer contacto (Maria Lopez)");
    }

    @Step("Paso 2 - Ingresar monto: ${0} y tap Continuar")
    private void stepEnterAmount(double monto) {
        transferPage.typeAmount(String.valueOf((long) monto));
        transferPage.tapContinue();
        AllureHelper.logAction("Escribir", "Monto: $" + (long) monto);
        AllureHelper.logAction("Tap", "Boton 'Continuar'");
    }

    @Step("Paso 3 - Confirmar transferencia")
    private void stepConfirmTransfer() {
        transferPage.tapConfirm();
        AllureHelper.logAction("Tap", "Boton 'Confirmar transferencia'");
    }

    @Step("Paso 4 - Verificar pantalla de exito: titulo, monto y destinatario")
    private void stepVerifySuccessScreen() {
        TransferSuccessPage successPage = new TransferSuccessPage(DriverFactory.getDriver());

        // 1. Validar pantalla de exito
        boolean onSuccess = successPage.isOnSuccessScreen();
        AllureHelper.logValidation("Pantalla de exito", onSuccess, true, onSuccess);
        Assert.assertTrue(onSuccess, "Debe estar en pantalla de exito despues de confirmar");

        // 2. Validar titulo contiene "exitosa"
        String titulo = successPage.readSuccessTitle();
        boolean tituloOk = titulo.toLowerCase().contains("exitosa") ||
                titulo.toLowerCase().contains("éxito");
        AllureHelper.logValidation("Titulo contiene 'exitosa'", titulo, "contiene 'exitosa'", tituloOk);
        Assert.assertTrue(tituloOk, "El titulo debe contener 'exitosa'. Leido: " + titulo);

        // 3. Validar monto mostrado
        String montoEnPantalla = successPage.getDisplayedAmountText();
        boolean montoOk = montoEnPantalla.contains("100") || montoEnPantalla.contains("100000");
        AllureHelper.logValidation("Monto en pantalla", montoEnPantalla,
                "contiene '100000'", montoOk);
        Assert.assertTrue(montoOk,
                "El monto en pantalla debe ser $100,000. Leido: '" + montoEnPantalla + "'");

        // 4. Validar destinatario
        String destinatario = successPage.getRecipientNameShown();
        boolean destOk = destinatario.toLowerCase().contains("maria") ||
                destinatario.toLowerCase().contains("lopez");
        AllureHelper.logValidation("Destinatario", destinatario, "contiene 'Maria' o 'Lopez'", destOk);
        Assert.assertTrue(destOk,
                "El destinatario debe ser Maria Lopez. Leido: '" + destinatario + "'");
    }

    // ========================================================================
    // TC14 - SALDO INSUFICIENTE
    // ========================================================================

    /**
     * TC14: Transferencia mayor al saldo muestra "Saldo insuficiente".
     */
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
        AllureHelper.logAction("Seleccionar", "Primer contacto");
    }

    @Step("Paso 2 - Ingresar monto excesivo: $3,000,000 y tap Continuar")
    private void stepEnterExcessiveAmount() {
        transferPage.typeAmount(String.valueOf((long) TestDataProvider.MONTO_INSUFICIENTE));
        transferPage.tapContinue();
        AllureHelper.logAction("Escribir", "Monto excesivo: $" + (long) TestDataProvider.MONTO_INSUFICIENTE);
        AllureHelper.logAction("Tap", "Boton 'Continuar'");
    }

    @Step("Paso 3 - Verificar mensaje 'Saldo insuficiente' visible")
    private void stepVerifyInsufficientBalanceError() {
        boolean errorShown = transferPage.isInsufficientBalanceErrorDisplayed();
        AllureHelper.logValidation("Mensaje 'Saldo insuficiente'", errorShown, true, errorShown);
        Assert.assertTrue(errorShown, "Debe mostrar mensaje de saldo insuficiente");
    }

    // ========================================================================
    // TC15 - MONTO INVALIDO (CERO)
    // ========================================================================

    /**
     * TC15: Monto cero debe mostrar "Ingresa un monto valido".
     */
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
        transferPage.tapContinue();
        AllureHelper.logAction("Escribir", "Monto: $0");
        AllureHelper.logAction("Tap", "Boton 'Continuar'");
    }

    @Step("Paso 3 - Verificar mensaje 'Ingresa un monto valido' visible")
    private void stepVerifyInvalidAmountError() {
        boolean errorShown = transferPage.isInvalidAmountErrorDisplayed();
        AllureHelper.logValidation("Mensaje 'monto valido'", errorShown, true, errorShown);
        Assert.assertTrue(errorShown, "Debe mostrar mensaje 'Ingresa un monto valido'");
    }

    // ========================================================================
    // TC16 - IMPACTO EN SALDO (ANTES/DESPUES)
    // ========================================================================

    /**
     * TC16: Impacto en saldo despues de transferencia.
     *
     * Valida:
     *   A. Saldo CONSOLIDADO se descuenta exactamente el monto transferido.
     *   B. Saldo de CUENTA CORRIENTE se descuenta exactamente el monto transferido.
     *
     * Flujo:
     *   1. Volver al Home, leer saldo consolidado ANTES
     *   2. Abrir Transferir, leer saldo Cuenta Corriente ANTES
     *   3. Transferir $200,000
     *   4. Volver al Home, leer saldo consolidado DESPUES
     *   5. Leer saldo Cuenta Corriente DESPUES
     *   6. Validar: ambas diferencias == monto transferido
     */
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
        double saldo = homePage.getConsolidatedBalanceTextAsAmount();
        AllureHelper.logStep("[ANTES] Saldo consolidado: $" + String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Abrir Transferir, seleccionar contacto y leer saldo Cuenta Corriente antes")
    private double stepReadCurrentAccountBalanceBefore() {
        homePage.tapQuickTransfer();
        transferPage.selectFirstContact();
        double saldo = transferPage.getSourceAccountBalance();
        AllureHelper.logStep("[ANTES] Saldo Cuenta Corriente: $" + String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Ejecutar transferencia de ${0}")
    private void stepExecuteTransfer(double monto) {
        transferPage.typeAmount(String.valueOf((long) monto));
        transferPage.tapContinue();
        transferPage.tapConfirm();
        AllureHelper.logAction("Transferir", "$" + (long) monto);
    }

    @Step("Volver al Home y leer saldo CONSOLIDADO despues de transferir")
    private double stepReadConsolidatedBalanceAfter() {
        new TransferSuccessPage(DriverFactory.getDriver()).tapBackToHome();
        double saldo = homePage.getConsolidatedBalanceTextAsAmount();
        AllureHelper.logStep("[DESPUES] Saldo consolidado: $" + String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Leer saldo CUENTA CORRIENTE despues de transferir")
    private double stepReadCurrentAccountBalanceAfter() {
        homePage.tapCurrentAccountTab();
        double saldo = homePage.getAccountBalanceAsAmount();
        AllureHelper.logStep("[DESPUES] Saldo Cuenta Corriente: $" + String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Validar descuento en saldo CONSOLIDADO == ${2}")
    private void stepValidateConsolidatedDiscount(double antes, double despues, double monto) {
        double diferencia = antes - despues;
        boolean passed = Math.abs(diferencia - monto) < 0.01;
        AllureHelper.logValidation(
                "Descuento saldo consolidado",
                diferencia,
                monto,
                passed
        );
        Assert.assertEquals(diferencia, monto, 0.01,
                "Saldo CONSOLIDADO debe descontar $" + String.format("%.2f", monto)
                        + ". Antes=$" + String.format("%.2f", antes)
                        + ", Despues=$" + String.format("%.2f", despues)
                        + ", Diferencia=$" + String.format("%.2f", diferencia));
    }

    @Step("Validar descuento en saldo CUENTA CORRIENTE == ${2}")
    private void stepValidateAccountDiscount(double antes, double despues, double monto) {
        double diferencia = antes - despues;
        boolean passed = Math.abs(diferencia - monto) < 0.01;
        AllureHelper.logValidation(
                "Descuento saldo Cuenta Corriente",
                diferencia,
                monto,
                passed
        );
        Assert.assertEquals(diferencia, monto, 0.01,
                "Saldo CUENTA CORRIENTE debe descontar $" + String.format("%.2f", monto)
                        + ". Antes=$" + String.format("%.2f", antes)
                        + ", Despues=$" + String.format("%.2f", despues)
                        + ", Diferencia=$" + String.format("%.2f", diferencia));
    }

    // ========================================================================
    // TC17 - AUDITORIA EN MOVIMIENTOS
    // ========================================================================

    /**
     * TC17: Auditoria - la transferencia aparece en Movimientos con monto debitado.
     *
     * Valida:
     *   1. La transferencia se proceso correctamente
     *   2. En Movimientos, buscando "Transferencia", aparece el registro
     *   3. El monto del registro empieza con "-" (debito) y coincide con el transferido
     */
    @Test(priority = 5, groups = {"transfer", "audit"})
    @Description("TC17 - Auditoria: transferencia registrada en Movimientos con monto debitado (-).")
    @Severity(SeverityLevel.CRITICAL)
    public void testAuditoriaEnMovimientos() {
        stepExecuteTransferForAudit();
        stepGoToMovementsFromSuccess();
        stepSearchTransferInMovements();
        stepVerifyDebitedAmount();
    }

    @Step("Paso 1 - Ejecutar transferencia de ${0} (auditoria)")
    private void stepExecuteTransferForAudit() {
        transferPage.completeTransfer(TestDataProvider.MONTO_AUDITORIA_TRANSFER);
        AllureHelper.logAction("Transferir", "$" + TestDataProvider.MONTO_AUDITORIA_TRANSFER);
    }

    @Step("Paso 2 - Volver al Home y navegar a Movimientos")
    private void stepGoToMovementsFromSuccess() {
        new TransferSuccessPage(DriverFactory.getDriver()).tapBackToHome();
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());
        boolean onMovements = movementsPage.isOnMovementsScreen();
        AllureHelper.logValidation("Navegacion a Movimientos", onMovements, true, onMovements);
        Assert.assertTrue(onMovements, "Debe estar en pantalla de Movimientos");
    }

    @Step("Paso 3 - Buscar 'Transferencia' en Movimientos")
    private void stepSearchTransferInMovements() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_TRANSFERENCIA);
        boolean has = movementsPage.hasMovements();
        AllureHelper.logValidation("Movimientos de transferencia encontrados", has, true, has);
        Assert.assertTrue(has, "Debe haber movimientos de transferencia");
    }

    @Step("Paso 4 - Validar monto debitado (-${0}) en Movimientos")
    private void stepVerifyDebitedAmount() {
        List<WebElement> montos = movementsPage.getAllMovementAmounts();
        Assert.assertFalse(montos.isEmpty(), "Debe haber al menos un monto en movimientos");

        boolean encontrado = false;
        for (WebElement monto : montos) {
            String textoMonto = monto.getText();
            AllureHelper.logStep("Movimiento encontrado: " + textoMonto);
            if (textoMonto.startsWith("-") && textoMonto.contains(TestDataProvider.MONTO_AUDITORIA_TRANSFER)) {
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
                "Debe haber un movimiento con monto -$" + TestDataProvider.MONTO_AUDITORIA_TRANSFER
                        + " (debitado). Montos encontrados: " + montos.size());
    }

    // ========================================================================
    // HELPER
    // ========================================================================

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }
}
