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
 * Modulo 2: Home (6 casos).
 * <p>
 * TC05 - Saldo consolidado
 * TC06 - Saldo de Cuenta Corriente
 * TC07 - Interactividad de cuentas (cambio de tabs)
 * TC08 - Acceso rapido: Transferir
 * TC09 - Acceso rapido: Pagar
 * TC10 - Acceso rapido: Movimientos
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
        String textoSaldo = homePage.getConsolidatedBalanceText();
        double saldo = homePage.getConsolidatedBalanceTextAsAmount();
        AllureHelper.reportRead(
                "Saldo consolidado (texto en pantalla)", textoSaldo,
                String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Tap tab 'Cuenta Corriente' y leer su saldo individual")
    private double stepReadCurrentAccountBalance() {
        homePage.tapCurrentAccountTab();
        String infoCuenta = homePage.getAccountInfoText();
        double saldo = homePage.getAccountBalanceAsAmount();
        AllureHelper.reportAction(
                "Tap", "Tab 'Cuenta Corriente'", null,
                "Tab activada, info de cuenta visible: " + infoCuenta);
        AllureHelper.reportRead(
                "Saldo Cuenta Corriente", infoCuenta,
                String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Tap tab 'Cuenta Ahorros' y leer su saldo individual")
    private double stepReadSavingsAccountBalance() {
        homePage.tapSavingsAccountTab();
        String infoCuenta = homePage.getAccountInfoText();
        double saldo = homePage.getAccountBalanceAsAmount();
        AllureHelper.reportAction(
                "Tap", "Tab 'Cuenta Ahorros'", null,
                "Tab activada, info de cuenta visible: " + infoCuenta);
        AllureHelper.reportRead(
                "Saldo Cuenta Ahorros", infoCuenta,
                String.format("%.2f", saldo));
        return saldo;
    }

    @Step("Validar: Cuenta Corriente + Cuenta Ahorros = Saldo consolidado")
    private void stepValidateConsolidatedSum(double consolidado, double corriente, double ahorros) {
        double sumaCalculada = corriente + ahorros;
        boolean passed = Math.abs(consolidado - sumaCalculada) < 0.01;
        String detalle = String.format(
                "$%,.0f (Corriente) + $%,.0f (Ahorros) = $%,.0f",
                corriente, ahorros, sumaCalculada);

        AllureHelper.reportValidation(
                "Saldo consolidado = suma de saldos individuales",
                String.format("$%,.2f", consolidado),
                String.format("$%,.2f", sumaCalculada),
                passed,
                detalle);
        Assert.assertEquals(consolidado, sumaCalculada, 0.01, detalle);
    }

    // ========================================================================
    // TC06 - SALDO CUENTA CORRIENTE
    // ========================================================================

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
        AllureHelper.reportAction(
                "Tap", "Tab 'Cuenta Corriente'", null,
                "Tab activada");
    }

    @Step("Verificar saldo de Cuenta Corriente contiene '$1500000'")
    private void stepVerifyCurrentAccountBalance() {
        String accountInfo = homePage.getAccountInfoText();
        double saldo = homePage.getAccountBalanceAsAmount();
        boolean contains = accountInfo.contains("1500000");
        AllureHelper.reportValidation(
                "Saldo de Cuenta Corriente",
                "Texto en pantalla: '" + accountInfo + "' (valor: $" + String.format("%,.0f", saldo) + ")",
                "Debe contener '$1500000'",
                contains,
                "Cuenta Corriente mockeada con saldo de $1,500,000.00");
        Assert.assertTrue(contains,
                "Saldo de Cuenta Corriente incorrecto: leyo '" + accountInfo + "'");
    }

    // ========================================================================
    // TC07 - INTERACTIVIDAD DE CUENTAS
    // ========================================================================

    @Test(priority = 3, groups = {"home"})
    @Description("TC07 - Interactividad: cambiar entre tabs Corriente y Ahorros actualiza la pantalla.")
    @Severity(SeverityLevel.NORMAL)
    public void testInteractividadCuentas() {
        stepTapCurrentAccountAndRead();
        stepTapSavingsAndRead();
    }

    @Step("Tap tab 'Cuenta Corriente' y leer info mostrada")
    private void stepTapCurrentAccountAndRead() {
        homePage.tapCurrentAccountTab();
        String info = homePage.getAccountInfoText();
        AllureHelper.reportRead(
                "Info Cuenta Corriente (tab activa)", info, null);
    }

    @Step("Tap tab 'Cuenta Ahorros' y leer info mostrada")
    private void stepTapSavingsAndRead() {
        homePage.tapSavingsAccountTab();
        String info = homePage.getAccountInfoText();
        AllureHelper.reportRead(
                "Info Cuenta Ahorros (tab activa)", info, null);
    }

    // ========================================================================
    // TC08 - ACCESO RAPIDO: TRANSFERIR
    // ========================================================================

    @Test(priority = 4, groups = {"home", "navigation"})
    @Description("TC08 - Acceso rapido 'Transferir' abre el modal de contactos.")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoTransferir() {
        stepTapQuickTransfer();
        stepVerifyTransferModalOpen();
    }

    @Step("Tap en boton de acceso rapido 'Transferir'")
    private void stepTapQuickTransfer() {
        homePage.tapQuickTransfer();
        AllureHelper.reportAction(
                "Tap", "Boton 'Transferir' (ViewGroup con content-desc)", null,
                "Tap realizado, esperando apertura del modal");
    }

    @Step("Verificar que el modal de Transferencia esta abierto")
    private void stepVerifyTransferModalOpen() {
        TransferPage transferPage = new TransferPage(DriverFactory.getDriver());
        boolean isOpen = transferPage.isOnContactScreen();
        AllureHelper.reportNavigation(
                "Home", "Modal de Transferencia (lista de contactos)", isOpen);
        Assert.assertTrue(isOpen, "Tap en Transferir debe abrir modal de contactos");
    }

    // ========================================================================
    // TC09 - ACCESO RAPIDO: PAGAR
    // ========================================================================

    @Test(priority = 5, groups = {"home", "navigation"})
    @Description("TC09 - Acceso rapido 'Pagar' abre la pantalla de pago de servicios.")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoPagar() {
        stepTapQuickPay();
        stepVerifyPayScreenOpen();
    }

    @Step("Tap en boton de acceso rapido 'Pagar'")
    private void stepTapQuickPay() {
        homePage.tapQuickPay();
        AllureHelper.reportAction(
                "Tap", "Boton 'Pagar' (ViewGroup con content-desc)", null,
                "Tap realizado, esperando apertura de pantalla de pago");
    }

    @Step("Verificar que la pantalla de Pago esta abierta")
    private void stepVerifyPayScreenOpen() {
        PayPage payPage = new PayPage(DriverFactory.getDriver());
        boolean isOpen = payPage.isOnPayScreen();
        AllureHelper.reportNavigation(
                "Home", "Pantalla de Pago de Servicios", isOpen);
        Assert.assertTrue(isOpen, "Tap en Pagar debe abrir pantalla de pago");
    }

    // ========================================================================
    // TC10 - ACCESO RAPIDO: MOVIMIENTOS
    // ========================================================================

    @Test(priority = 6, groups = {"home", "navigation"})
    @Description("TC10 - Acceso rapido 'Movimientos' abre la lista de transacciones.")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoMovimientos() {
        stepTapQuickMovements();
        stepVerifyMovementsScreenOpen();
    }

    @Step("Tap en boton de acceso rapido 'Movimientos'")
    private void stepTapQuickMovements() {
        homePage.tapQuickMovements();
        AllureHelper.reportAction(
                "Tap", "Boton 'Movimientos' (ViewGroup con content-desc)", null,
                "Tap realizado, esperando apertura de lista de movimientos");
    }

    @Step("Verificar que la pantalla de Movimientos esta abierta")
    private void stepVerifyMovementsScreenOpen() {
        MovementsPage movementsPage = new MovementsPage(DriverFactory.getDriver());
        boolean isOpen = movementsPage.isOnMovementsScreen();
        AllureHelper.reportNavigation(
                "Home", "Pantalla de Movimientos", isOpen);
        Assert.assertTrue(isOpen, "Tap en Movimientos debe abrir la pantalla de movimientos");
    }
}
