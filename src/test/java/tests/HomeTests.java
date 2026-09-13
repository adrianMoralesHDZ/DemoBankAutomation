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
 * MODULO 2: HOME (6 casos - requisito del PDF)
 * ============================================================================
 *
 * Cubre:
 *   TC05 - Saldo consolidado (OCR o texto nativo)
 *   TC06 - Saldo de Cuenta Corriente
 *   TC07 - Interactividad de cuentas (cambio de tabs)
 *   TC08 - Acceso rapido: Transferir
 *   TC09 - Acceso rapido: Pagar
 *   TC10 - Acceso rapido: Movimientos
 *
 * REPORTES ALLURE:
 *   - Cada paso del test esta anotado con @Step para que el reporte
 *     muestre el detalle paso a paso de la ejecucion.
 *   - Los screenshots se capturan AUTOMATICAMENTE solo en caso de fallo
 *     (gestionado por TestListener.onTestFailure).
 * ============================================================================
 */
public class HomeTests {

    private HomePage homePage;
    private LoginPage loginPage;

    @BeforeMethod
    public void setUp() {
        DriverFactory.getDriver();
        loginPage = new LoginPage(DriverFactory.getDriver());
        homePage = new HomePage(DriverFactory.getDriver());
        loginPage.loginAs(TestDataProvider.VALID_EMAIL, TestDataProvider.VALID_PASSWORD);
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    // ========================================================================
    // TC05 - SALDO CONSOLIDADO
    // ========================================================================

    /**
     * TC05: Consistencia del saldo consolidado.
     *
     * Valida que el saldo total mostrado en Home sea igual a la suma de los
     * saldos individuales de Cuenta Corriente + Cuenta Ahorros.
     *
     * Flujo:
     *   1. Leer saldo consolidado del Home
     *   2. Tap tab Cuenta Corriente, leer saldo individual
     *   3. Tap tab Cuenta Ahorros, leer saldo individual
     *   4. Sumar ambos saldos
     *   5. Validar: suma == saldo consolidado
     */
    @Test(priority = 1, groups = {"home"})
    @Description("TC05 - Saldo consolidado: Cuenta Corriente + Cuenta Ahorros = saldo total en Home.")
    @Severity(SeverityLevel.CRITICAL)
    public void testSaldoConsolidado() {
        double saldoConsolidado = stepReadConsolidatedBalance();
        double saldoCorriente = stepReadCurrentAccountBalance();
        double saldoAhorros = stepReadSavingsAccountBalance();
        stepValidateConsolidatedSum(saldoConsolidado, saldoCorriente, saldoAhorros);
    }

    @Step("Leer saldo consolidado del Home")
    private double stepReadConsolidatedBalance() {
        double saldo = homePage.getConsolidatedBalanceTextAsAmount();
        AllureHelper.logStep("Saldo consolidado leido: $" + String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Tap tab 'Cuenta Corriente' y leer su saldo individual")
    private double stepReadCurrentAccountBalance() {
        homePage.tapCurrentAccountTab();
        double saldo = homePage.getAccountBalanceAsAmount();
        AllureHelper.logStep("Saldo Cuenta Corriente: $" + String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Tap tab 'Cuenta Ahorros' y leer su saldo individual")
    private double stepReadSavingsAccountBalance() {
        homePage.tapSavingsAccountTab();
        double saldo = homePage.getAccountBalanceAsAmount();
        AllureHelper.logStep("Saldo Cuenta Ahorros: $" + String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Validar que suma de saldos individuales = saldo consolidado")
    private void stepValidateConsolidatedSum(double consolidado, double corriente, double ahorros) {
        double sumaEsperada = corriente + ahorros;
        boolean passed = Math.abs(consolidado - sumaEsperada) < 0.01;
        AllureHelper.logValidation(
                "Saldo consolidado",
                consolidado,
                sumaEsperada,
                passed
        );
        Assert.assertEquals(consolidado, sumaEsperada, 0.01,
                "Saldo consolidado incorrecto: $" + String.format("%.2f", consolidado)
                        + " != Corriente ($" + String.format("%.2f", corriente)
                        + ") + Ahorros ($" + String.format("%.2f", ahorros)
                        + ") = $" + String.format("%.2f", sumaEsperada));
    }

    // ========================================================================
    // TC06 - SALDO CUENTA CORRIENTE
    // ========================================================================

    /**
     * TC06: Saldo individual de Cuenta Corriente.
     */
    @Test(priority = 2, groups = {"home"})
    @Description("TC06 - Saldo de Cuenta Corriente: $1,500,000.00.")
    @Severity(SeverityLevel.NORMAL)
    public void testSaldoCuentaCorriente() {
        stepTapCurrentAccountTab();
        stepVerifyCurrentAccountBalance();
    }

    @Step("Tap tab 'Cuenta Corriente'")
    private void stepTapCurrentAccountTab() {
        homePage.tapCurrentAccountTab();
        AllureHelper.logAction("Tap", "Tab Cuenta Corriente");
    }

    @Step("Verificar que el saldo de Cuenta Corriente contiene '$1500000'")
    private void stepVerifyCurrentAccountBalance() {
        String accountInfo = homePage.getAccountInfoText();
        boolean contains = accountInfo.contains("1500000") || accountInfo.contains("1500000.00");
        AllureHelper.logValidation("Saldo Cuenta Corriente", accountInfo, "contiene 1500000", contains);
        Assert.assertTrue(contains,
                "Saldo de Cuenta Corriente incorrecto: deberia contener '$1500000' "
                        + "pero leyo: '" + accountInfo + "'");
    }

    // ========================================================================
    // TC07 - INTERACTIVIDAD DE CUENTAS
    // ========================================================================

    /**
     * TC07: Cambio entre tabs Corriente y Ahorros actualiza dinamicamente.
     */
    @Test(priority = 3, groups = {"home"})
    @Description("TC07 - Interactividad: cambiar entre tabs Corriente y Ahorros actualiza la pantalla.")
    @Severity(SeverityLevel.NORMAL)
    public void testInteractividadCuentas() {
        stepTapCurrentAccountAndRead();
        stepTapSavingsAndRead();
    }

    @Step("Tap tab 'Cuenta Corriente' y leer info de cuenta")
    private void stepTapCurrentAccountAndRead() {
        homePage.tapCurrentAccountTab();
        String info = homePage.getAccountInfoText();
        AllureHelper.logStep("Tab Cuenta Corriente activa. Info: " + info);
    }

    @Step("Tap tab 'Cuenta Ahorros' y leer info de cuenta")
    private void stepTapSavingsAndRead() {
        homePage.tapSavingsAccountTab();
        String info = homePage.getAccountInfoText();
        AllureHelper.logStep("Tab Cuenta Ahorros activa. Info: " + info);
    }

    // ========================================================================
    // TC08 - ACCESO RAPIDO: TRANSFERIR
    // ========================================================================

    /**
     * TC08: Boton Transferir abre el modal de transferencia.
     */
    @Test(priority = 4, groups = {"home", "navigation"})
    @Description("TC08 - Acceso rapido 'Transferir' abre el modal de contactos.")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoTransferir() {
        stepTapQuickTransfer();
        stepVerifyTransferModalOpen();
    }

    @Step("Tap en acceso rapido 'Transferir'")
    private void stepTapQuickTransfer() {
        homePage.tapQuickTransfer();
        AllureHelper.logAction("Tap", "Boton 'Transferir'");
    }

    @Step("Verificar que el modal de Transferencia esta abierto")
    private void stepVerifyTransferModalOpen() {
        TransferPage transferPage = new TransferPage(DriverFactory.getDriver());
        boolean isOpen = transferPage.isOnContactScreen();
        AllureHelper.logValidation("Modal Transferir abierto", isOpen, true, isOpen);
        Assert.assertTrue(isOpen, "Tap en Transferir debe abrir modal de contactos");
    }

    // ========================================================================
    // TC09 - ACCESO RAPIDO: PAGAR
    // ========================================================================

    /**
     * TC09: Boton Pagar abre el modal de pago de servicios.
     */
    @Test(priority = 5, groups = {"home", "navigation"})
    @Description("TC09 - Acceso rapido 'Pagar' abre la pantalla de pago de servicios.")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoPagar() {
        stepTapQuickPay();
        stepVerifyPayScreenOpen();
    }

    @Step("Tap en acceso rapido 'Pagar'")
    private void stepTapQuickPay() {
        homePage.tapQuickPay();
        AllureHelper.logAction("Tap", "Boton 'Pagar'");
    }

    @Step("Verificar que la pantalla de Pago esta abierta")
    private void stepVerifyPayScreenOpen() {
        PayPage payPage = new PayPage(DriverFactory.getDriver());
        boolean isOpen = payPage.isOnPayScreen();
        AllureHelper.logValidation("Pantalla Pagar abierta", isOpen, true, isOpen);
        Assert.assertTrue(isOpen, "Tap en Pagar debe abrir pantalla de pago");
    }

    // ========================================================================
    // TC10 - ACCESO RAPIDO: MOVIMIENTOS
    // ========================================================================

    /**
     * TC10: Boton Movimientos abre la lista de movimientos.
     */
    @Test(priority = 6, groups = {"home", "navigation"})
    @Description("TC10 - Acceso rapido 'Movimientos' abre la lista de transacciones.")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoMovimientos() {
        stepTapQuickMovements();
        stepVerifyMovementsScreenOpen();
    }

    @Step("Tap en acceso rapido 'Movimientos'")
    private void stepTapQuickMovements() {
        homePage.tapQuickMovements();
        AllureHelper.logAction("Tap", "Boton 'Movimientos'");
    }

    @Step("Verificar que la pantalla de Movimientos esta abierta")
    private void stepVerifyMovementsScreenOpen() {
        MovementsPage movementsPage = new MovementsPage(DriverFactory.getDriver());
        boolean isOpen = movementsPage.isOnMovementsScreen();
        AllureHelper.logValidation("Pantalla Movimientos abierta", isOpen, true, isOpen);
        Assert.assertTrue(isOpen, "Tap en Movimientos debe abrir la pantalla de movimientos");
    }
}
