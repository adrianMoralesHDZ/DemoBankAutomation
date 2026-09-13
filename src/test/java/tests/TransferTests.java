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
 * Los saldos se leen con getText() sobre TextView nativo (localizadores
 * confiables confirmados con dump real). No se requiere OCR en este modulo
 * porque los textos de saldo y error son TextView accesibles.
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

    /**
     * CASO 1: Flujo feliz - transferir $10000 al primer contacto.
     */
    @Test(priority = 1, groups = {"transfer", "happy-path"})
    @Description("Transferencia exitosa de $100.000 al primer contacto (Maria Lopez)")
    @Severity(SeverityLevel.CRITICAL)
    public void testTransferenciaExitosa() {
        double monto = 100000;

        stepSelectContact();
        AllureHelper.screenshot("[Paso 1] Contacto seleccionado (Maria Lopez)");

        stepEnterAmount(String.valueOf((long) monto));
        AllureHelper.screenshot("[Paso 2] Monto escrito: $" + monto);

        stepConfirmTransfer();
        AllureHelper.screenshot("[PASO 3 OK] Transferencia confirmada - esperando pantalla de exito");

        // ====== PANTALLA DE EXITO: Validaciones ======

        stepVerifySuccessScreen();
        AllureHelper.screenshot("[Paso 4] Pantalla de exito con check verde");
    }

    @Step("Verificar pantalla de exito: titulo, monto via OCR y OpenCV")
    private void stepVerifySuccessScreen() {
        TransferSuccessPage successPage = new TransferSuccessPage(DriverFactory.getDriver());

        // 1. Validar que estamos en pantalla de exito (localizador)
        boolean onSuccess = successPage.isOnSuccessScreen();
        Assert.assertTrue(onSuccess, "Debe estar en pantalla de exito despues de confirmar");

        // 2. OCR: leer titulo "¡Transferencia exitosa!"
        String titulo = successPage.readSuccessTitle();
        Assert.assertTrue(titulo.toLowerCase().contains("exitosa") ||
                        titulo.toLowerCase().contains("éxito"),
                "El titulo OCR debe contener 'exitosa'. Leido: " + titulo);

        // 3. OCR: leer el monto mostrado en pantalla
        String montoEnPantalla = successPage.getDisplayedAmountText();
        Assert.assertTrue(montoEnPantalla.contains("$100") || montoEnPantalla.contains("100000") ||
                        montoEnPantalla.contains("100000.00"),
                "El monto OCR en pantalla debe ser 100000. Leido: '" + montoEnPantalla + "'");

        // 4. OCR: leer destinatario
        String destinatario = successPage.getRecipientNameShown();
        Assert.assertTrue(destinatario.toLowerCase().contains("maria") ||
                        destinatario.toLowerCase().contains("lópez") ||
                        destinatario.toLowerCase().contains("lopez") ||
                        destinatario.toLowerCase().contains("mara") ||
                        destinatario.toLowerCase().contains("lpez"),
                "El destinatario debe ser Maria Lopez. Leido: '" + destinatario + "'");

        // 5. Validacion EXTRA: tap BackToHome
        // (solo lo hace si lo llamaramos desde fuera, no aqui)
    }

    @Step("Seleccionar primer contacto")
    private void stepSelectContact() {
        transferPage.selectFirstContact();
    }

    @Step("Ingresar monto: {0}")
    private void stepEnterAmount(String amount) {
        transferPage.typeAmount(amount);
        transferPage.tapContinue();
    }

    @Step("Confirmar transferencia")
    private void stepConfirmTransfer() {
        transferPage.tapConfirm();
    }

    /**
     * CASO 2: Saldo insuficiente.
     */
    @Test(priority = 2, groups = {"transfer", "negative"})
    @Description("Transferencia mayor al saldo muestra error 'Saldo insuficiente'")
    @Severity(SeverityLevel.CRITICAL)
    public void testSaldoInsuficiente() {
        transferPage.selectFirstContact();
        AllureHelper.screenshot("[Paso 1] Contacto seleccionado");

        transferPage.typeAmount("3000000");
        AllureHelper.screenshot("[Paso 2] Monto excesivo escrito");

        transferPage.tapContinue();
        AllureHelper.screenshot("[Paso 3] Resultado");

        Assert.assertTrue(transferPage.isInsufficientBalanceErrorDisplayed(),
                "Debe mostrar mensaje de saldo insuficiente");
    }

    /**
     * CASO 3: Monto inválido (cero).
     */
    @Test(priority = 3, groups = {"transfer", "negative"})
    @Description("Monto cero debe mostrar error 'Ingresa un monto válido'")
    @Severity(SeverityLevel.CRITICAL)
    public void testMontoInvalido() {
        transferPage.selectFirstContact();
        AllureHelper.screenshot("[Paso 1] Contacto seleccionado");

        transferPage.typeAmount("0");
        AllureHelper.screenshot("[Paso 2] Monto cero escrito");

        transferPage.tapContinue();
        AllureHelper.screenshot("[Paso 3] Resultado");

        Assert.assertTrue(transferPage.isInvalidAmountErrorDisplayed(),
                "Debe mostrar mensaje 'Ingresa un monto válido'");
    }

    /**
     * CASO 4: Impacto en saldo (pre/post).
     *
     * Valida dos cosas:
     *   A. Saldo CONSOLIDADO (total) se descuenta exactamente
     *   B. Saldo de CUENTA CORRIENTE (tarjeta origen) se descuenta exactamente
     *
     * Flujo:
     *   1. Volver al Home → leer saldo consolidado ANTES
     *   2. Abrir Transferir → seleccionar contacto → leer saldo Cuenta Corriente ANTES
     *   3. Transferir $200.000
     *   4. Volver al Home → leer saldo consolidado DESPUES
     *   5. Tap Cuenta Corriente → leer saldo Cuenta Corriente DESPUES
     *   6. Validar: ambas diferencias == monto transferido
     *
     * Los saldos se leen con getText() sobre TextView nativo (no OCR).
     */
    @Test(priority = 4, groups = {"transfer"})
    @Description("Validar descuento exacto en saldo consolidado y en Cuenta Corriente")
    @Severity(SeverityLevel.CRITICAL)
    public void testImpactoSaldoOrigen() {
        double montoTest = 200000.0;

        // 1. Volver al Home y leer saldo CONSOLIDADO antes
        driver().navigate().back();
        double saldoConsolidadoAntes = homePage.getConsolidatedBalanceTextAsAmount();
        AllureHelper.screenshot("[Pre] Saldo CONSOLIDADO antes: $" + String.format("%.2f", saldoConsolidadoAntes));

        // 2. Abrir Transferir y leer saldo CUENTA CORRIENTE antes
        homePage.tapQuickTransfer();
        transferPage.selectFirstContact();
        double saldoCuentaAntes = transferPage.getSourceAccountBalance();
        AllureHelper.screenshot("[Pre] Saldo Cuenta Corriente antes: $" + String.format("%.2f", saldoCuentaAntes));

        // 3. Realizar transferencia
        transferPage.typeAmount(String.valueOf((long) montoTest));
        transferPage.tapContinue();
        transferPage.tapConfirm();
        AllureHelper.screenshot("[Transfer] Transferencia de $" + String.format("%.2f", montoTest));

        // 4. Volver al Home y leer saldo CONSOLIDADO despues
        new TransferSuccessPage(DriverFactory.getDriver()).tapBackToHome();
        double saldoConsolidadoDespues = homePage.getConsolidatedBalanceTextAsAmount();
        AllureHelper.screenshot("[Post] Saldo CONSOLIDADO despues: $" + String.format("%.2f", saldoConsolidadoDespues));

        // 5. Leer saldo CUENTA CORRIENTE despues
        homePage.tapCurrentAccountTab();
        double saldoCuentaDespues = homePage.getAccountBalanceAsAmount();
        AllureHelper.screenshot("[Post] Saldo Cuenta Corriente despues: $" + String.format("%.2f", saldoCuentaDespues));

        // 6. Validar descuento en saldo CONSOLIDADO
        double diferenciaConsolidado = saldoConsolidadoAntes - saldoConsolidadoDespues;
        Assert.assertEquals(diferenciaConsolidado, montoTest, 0.01,
                "Saldo CONSOLIDADO debe descontar $" + String.format("%.2f", montoTest)
                        + ". Antes=$" + String.format("%.2f", saldoConsolidadoAntes)
                        + ", Despues=$" + String.format("%.2f", saldoConsolidadoDespues)
                        + ", Diferencia=$" + String.format("%.2f", diferenciaConsolidado));

        // 7. Validar descuento en saldo CUENTA CORRIENTE
        double diferenciaCuenta = saldoCuentaAntes - saldoCuentaDespues;
        Assert.assertEquals(diferenciaCuenta, montoTest, 0.01,
                "Saldo CUENTA CORRIENTE debe descontar $" + String.format("%.2f", montoTest)
                        + ". Antes=$" + String.format("%.2f", saldoCuentaAntes)
                        + ", Despues=$" + String.format("%.2f", saldoCuentaDespues)
                        + ", Diferencia=$" + String.format("%.2f", diferenciaCuenta));
    }

    /**
     * CASO 5: Auditoría - la transferencia aparece en Movimientos.
     *
     * Valida que después de transferir $250.000:
     *   1. Al ir a Movimientos, aparece el registro de la transferencia
     *   2. El monto del registro empieza con "-" (débito)
     *   3. El monto debitado coincide con el valor transferido
     */
    @Test(priority = 5, groups = {"transfer", "audit"})
    @Description("Auditoria: transferencia registrada en Movimientos con monto debitado (-)")
    @Severity(SeverityLevel.CRITICAL)
    public void testAuditoriaEnMovimientos() {
        String montoTransferido = "250000";

        // 1. Hacer transferencia
        transferPage.completeTransfer(montoTransferido);
        AllureHelper.screenshot("[Transfer] Transferencia de $" + montoTransferido);

        // 2. Volver al Home
        new TransferSuccessPage(DriverFactory.getDriver()).tapBackToHome();

        // 3. Ir a Movimientos
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());
        Assert.assertTrue(movementsPage.isOnMovementsScreen(),
                "Debe estar en pantalla de Movimientos");

        // 4. Buscar "Transferencia" para filtrar
        movementsPage.searchFor("Transferencia");
        Assert.assertTrue(movementsPage.hasMovements(),
                "Debe haber movimientos de transferencia");
        AllureHelper.screenshot("[Movimientos] Filtrados por Transferencia");

        // 5. Validar que existe un movimiento con monto negativo igual al transferido
        List<WebElement> montos = movementsPage.getAllMovementAmounts();
        Assert.assertFalse(montos.isEmpty(),
                "Debe haber al menos un monto en movimientos");

        boolean encontrado = false;
        for (WebElement monto : montos) {
            String textoMonto = monto.getText();
            System.out.println("[AUDITORIA] Movimiento: " + textoMonto);
            // El monto debitado debe empezar con "-" y contener el valor transferido
            if (textoMonto.startsWith("-") && textoMonto.contains(montoTransferido)) {
                encontrado = true;
                break;
            }
        }

        Assert.assertTrue(encontrado,
                "Debe haber un movimiento con monto -$" + montoTransferido
                        + " (debitado). Montos encontrados: " + montos.size());
        AllureHelper.screenshot("[Auditoria OK] Transferencia registrada con monto -$" + montoTransferido);
    }


    // Helper para tomar el driver
    private WebDriver driver() {
        return DriverFactory.getDriver();
    }
}
