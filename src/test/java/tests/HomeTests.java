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
// CASO 1: SALDO CONSOLIDADO
// ========================================================================

    /**
     * CASO 1: Consistencia del saldo consolidado.
     * <p>
     * Valida que el saldo total mostrado en Home sea igual a la suma de los
     * saldos individuales de Cuenta Corriente + Cuenta Ahorros.
     * <p>
     * Flujo:
     * 1. Leer saldo consolidado de la pantalla Home
     * 2. Tap tab Cuenta Corriente → leer saldo individual
     * 3. Tap tab Cuenta Ahorros → leer saldo individual
     * 4. Sumar ambos saldos
     * 5. Assert: suma de saldos individuales == saldo consolidado
     * <p>
     * Todos los valores se obtienen de la app en tiempo de ejecucion.
     */
    @Test(priority = 1, groups = {"home"})
    @Description("Validar saldo consolidado: saldo Cuenta Corriente + saldo Cuenta Ahorros "
            + "= saldo total mostrado en Home.")
    @Severity(SeverityLevel.CRITICAL)
    public void testSaldoConsolidado() {
        TestListener.captureAndAttachScreenshot("Home - Pantalla inicial");

        // 1. Leer saldo consolidado del Home
        double saldoConsolidado = homePage.getConsolidatedBalanceTextAsAmount();
        TestListener.captureAndAttachScreenshot(
                "Saldo consolidado: $" + String.format("%.2f", saldoConsolidado));

        // 2. Tap Cuenta Corriente y leer su saldo
        homePage.tapCurrentAccountTab();
        double saldoCorriente = homePage.getAccountBalanceAsAmount();
        TestListener.captureAndAttachScreenshot(
                "Cuenta Corriente: $" + String.format("%.2f", saldoCorriente));

        // 3. Tap Cuenta Ahorros y leer su saldo
        homePage.tapSavingsAccountTab();
        double saldoAhorros = homePage.getAccountBalanceAsAmount();
        TestListener.captureAndAttachScreenshot(
                "Cuenta Ahorros: $" + String.format("%.2f", saldoAhorros));

        // 4. Sumar los saldos individuales
        double sumaEsperada = saldoCorriente + saldoAhorros;

        // 5. Validar: la suma debe ser igual al saldo consolidado
        Assert.assertEquals(saldoConsolidado, sumaEsperada, 0.00,
                "Saldo consolidado incorrecto: $" + String.format("%.2f", saldoConsolidado)
                        + " != Cuenta Corriente ($" + String.format("%.2f", saldoCorriente)
                        + ") + Cuenta Ahorros ($" + String.format("%.2f", saldoAhorros)
                        + ") = $" + String.format("%.2f", sumaEsperada));
    }

// ========================================================================
// CASO 2: SALDO CUENTA CORRIENTE
// ========================================================================

    /**
     * CASO 2: Saldo individual de Cuenta Corriente.
     * <p>
     * Valida que la linea de cuenta muestre el saldo de Cuenta Corriente
     * ($1.500.000.00). El texto es un TextView con @text accesible, se lee
     * con getText()
     */
    @Test(priority = 2, groups = {"home"})
    @Description("Validar saldo de Cuenta Corriente: $1.500.000. "
            + "Localizador nativo confiable (TextView con @text).")
    @Severity(SeverityLevel.NORMAL)
    public void testSaldoCuentaCorriente() {
        TestListener.captureAndAttachScreenshot("Home (tab por defecto)");

        homePage.tapCurrentAccountTab();
        TestListener.captureAndAttachScreenshot("Tab Cuenta Corriente activa");

        String accountInfo = homePage.getAccountInfoText();

        Assert.assertTrue(accountInfo.contains("1500000") || accountInfo.contains("1500000.00"),
                "Saldo de Cuenta Corriente incorrecto: deberia contener '$1.500.000' "
                        + "pero leyo: '" + accountInfo + "'");
    }

    // ========================================================================
    // CASO 3: INTERACTIVIDAD DE CUENTAS
    // ========================================================================

    /**
     * CASO 3: Cambio entre tabs Corriente y Ahorros.
     */
    @Test(priority = 3, groups = {"home"})
    @Description("Interactividad: cambiar entre tabs Corriente y Ahorros")
    @Severity(SeverityLevel.NORMAL)
    public void testInteractividadCuentas() {
        homePage.tapCurrentAccountTab();
        TestListener.captureAndAttachScreenshot("Tab Cuenta Corriente");

        homePage.tapSavingsAccountTab();
        TestListener.captureAndAttachScreenshot("Tab Cuenta Ahorros");
    }

    // ========================================================================
    // CASO 4: ACCESOS RAPIDOS
    // ========================================================================

    /**
     * CASO 4: Boton Transferir abre modal de TransferPage.
     */
    @Test(priority = 4, groups = {"home", "navigation"})
    @Description("Acceso rapido Transferir abre el modal")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoTransferir() {
        homePage.tapQuickTransfer();
        Assert.assertTrue(new TransferPage(DriverFactory.getDriver()).isOnContactScreen(),
                "Tap en Transferir debe abrir modal de contactos");
        TestListener.captureAndAttachScreenshot("Modal Transferir abierto");
    }


    /**
     * CASO 4.2: Boton Pagar abre modal de PayPage.
     */
    @Test(priority = 5, groups = {"home", "navigation"})
    @Description("Acceso rapido Pagar abre el modal")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoPagar() {
        homePage.tapQuickPay();
        Assert.assertTrue(new PayPage(DriverFactory.getDriver()).isOnPayScreen(),
                "Tap en Pagar debe abrir pantalla de pago");
        TestListener.captureAndAttachScreenshot("Modal Pagar abierto");
    }

    /**
     * CASO 4.3: Boton Movimientos abre lista.
     */
    @Test(priority = 6, groups = {"home", "navigation"})
    @Description("Acceso rapido Movimientos abre la lista de movimientos")
    @Severity(SeverityLevel.NORMAL)
    public void testAccesoRapidoMovimientos() {
        homePage.tapQuickMovements();
        Assert.assertTrue(new MovementsPage(DriverFactory.getDriver()).isOnMovementsScreen(),
                "Tap en Movimientos debe abrir la pantalla de movimientos");
        TestListener.captureAndAttachScreenshot("Pantalla Movimientos abierta");
    }

}
