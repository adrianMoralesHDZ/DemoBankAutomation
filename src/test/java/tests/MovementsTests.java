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
 * MODULO 3: MOVIMIENTOS (4 casos - requisito del PDF)
 * ============================================================================
 *
 * Cubre:
 *   TC09 - Busqueda parcial case-insensitive
 *   TC10 - Filtro Ingresos (montos positivos)
 *   TC11 - Filtro Gastos (montos negativos)
 *   TC12 - Empty state (sin resultados)
 *
 * REPORTES ALLURE:
 *   - Cada paso del test esta anotado con @Step para que el reporte
 *     muestre el detalle paso a paso de la ejecucion.
 *   - Los screenshots se capturan AUTOMATICAMENTE solo en caso de fallo
 *     (gestionado por TestListener.onTestFailure).
 * ============================================================================
 */
public class MovementsTests {

    private HomePage homePage;
    private MovementsPage movementsPage;
    private LoginPage loginPage;

    @BeforeMethod
    public void setUp() {
        DriverFactory.getDriver();
        loginPage = new LoginPage(DriverFactory.getDriver());
        homePage = new HomePage(DriverFactory.getDriver());
        loginPage.loginAs(TestDataProvider.VALID_EMAIL, TestDataProvider.VALID_PASSWORD);
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    // ========================================================================
    // TC09 - BUSQUEDA PARCIAL CASE-INSENSITIVE
    // ========================================================================

    /**
     * TC09: Busqueda parcial funciona de forma case-insensitive.
     *
     * Busca "transfe" (minusculas) y valida que encuentra movimientos
     * que contienen "Transferencia" (con mayuscula inicial).
     */
    @Test(priority = 1, groups = {"movements", "search"})
    @Description("TC09 - Busqueda parcial: buscar 'transfe' debe filtrar movimientos con 'Transferencia'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testBusquedaParcial() {
        stepVerifyMovementsExist();
        stepSearchPartialTerm();
        stepVerifySearchHasResults();
    }

    @Step("Verificar que hay movimientos visibles antes de buscar")
    private void stepVerifyMovementsExist() {
        boolean has = movementsPage.hasMovements();
        AllureHelper.logValidation("Movimientos visibles (inicial)", has, true, has);
        Assert.assertTrue(has, "Debe haber movimientos visibles antes de buscar");
    }

    @Step("Buscar termino parcial: '{0}' (minusculas)")
    private void stepSearchPartialTerm() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_PARCIAL);
        AllureHelper.logAction("Buscar", "Termino: '" + TestDataProvider.BUSQUEDA_PARCIAL + "'");
    }

    @Step("Verificar que la busqueda parcial devolvio resultados")
    private void stepVerifySearchHasResults() {
        boolean has = movementsPage.hasMovements();
        AllureHelper.logValidation("Resultados busqueda parcial", has, true, has);
        Assert.assertTrue(has, "Debe haber resultados al buscar '" + TestDataProvider.BUSQUEDA_PARCIAL + "'");
    }

    // ========================================================================
    // TC10 - FILTRO INGRESOS
    // ========================================================================

    /**
     * TC10: Filtro Ingresos muestra solo movimientos con monto positivo (+).
     */
    @Test(priority = 2, groups = {"movements", "filter"})
    @Description("TC10 - Filtro Ingresos: todos los montos visibles deben empezar con '+'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testFiltroIngresos() {
        stepTapIncomeFilter();
        stepVerifyMovementsExistAfterFilter();
        stepVerifyAllAmountsPositive();
    }

    @Step("Tap en filtro 'Ingresos'")
    private void stepTapIncomeFilter() {
        movementsPage.tapIncomeFilter();
        AllureHelper.logAction("Tap", "Filtro 'Ingresos'");
    }

    @Step("Verificar que hay movimientos despues del filtro Ingresos")
    private void stepVerifyMovementsExistAfterFilter() {
        boolean has = movementsPage.hasMovements();
        AllureHelper.logValidation("Movimientos en filtro Ingresos", has, true, has);
        Assert.assertTrue(has, "Filtro Ingresos debe mostrar movimientos");
    }

    @Step("Validar que todos los montos visibles empiezan con '+' (positivos)")
    private void stepVerifyAllAmountsPositive() {
        boolean allPositive = movementsPage.allAmountsArePositive();
        int count = movementsPage.getAllMovementAmounts().size();
        AllureHelper.logValidation("Todos los montos son positivos (+)", allPositive, true, allPositive);
        AllureHelper.logStep("Cantidad de movimientos validados: " + count);
        Assert.assertTrue(allPositive,
                "Todos los montos con filtro Ingresos deben empezar con '+'. "
                        + "Montos encontrados: " + count);
    }

    // ========================================================================
    // TC11 - FILTRO GASTOS
    // ========================================================================

    /**
     * TC11: Filtro Gastos muestra solo movimientos con monto negativo (-).
     */
    @Test(priority = 3, groups = {"movements", "filter"})
    @Description("TC11 - Filtro Gastos: todos los montos visibles deben empezar con '-'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testFiltroGastos() {
        stepTapExpenseFilter();
        stepVerifyMovementsExistAfterExpenseFilter();
        stepVerifyAllAmountsNegative();
    }

    @Step("Tap en filtro 'Gastos'")
    private void stepTapExpenseFilter() {
        movementsPage.tapExpenseFilter();
        AllureHelper.logAction("Tap", "Filtro 'Gastos'");
    }

    @Step("Verificar que hay movimientos despues del filtro Gastos")
    private void stepVerifyMovementsExistAfterExpenseFilter() {
        boolean has = movementsPage.hasMovements();
        AllureHelper.logValidation("Movimientos en filtro Gastos", has, true, has);
        Assert.assertTrue(has, "Filtro Gastos debe mostrar movimientos");
    }

    @Step("Validar que todos los montos visibles empiezan con '-' (negativos)")
    private void stepVerifyAllAmountsNegative() {
        boolean allNegative = movementsPage.allAmountsAreNegative();
        int count = movementsPage.getAllMovementAmounts().size();
        AllureHelper.logValidation("Todos los montos son negativos (-)", allNegative, true, allNegative);
        AllureHelper.logStep("Cantidad de movimientos validados: " + count);
        Assert.assertTrue(allNegative,
                "Todos los montos con filtro Gastos deben empezar con '-'. "
                        + "Montos encontrados: " + count);
    }

    // ========================================================================
    // TC12 - EMPTY STATE
    // ========================================================================

    /**
     * TC12: Buscar un criterio inexistente muestra el empty state.
     */
    @Test(priority = 4, groups = {"movements", "negative"})
    @Description("TC12 - Empty state: buscar texto inexistente muestra 'Sin resultados'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testEmptyState() {
        stepSearchNonExistentTerm();
        stepVerifyEmptyStateDisplayed();
    }

    @Step("Buscar termino inexistente: '{0}'")
    private void stepSearchNonExistentTerm() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_NO_EXISTE);
        AllureHelper.logAction("Buscar", "Termino inexistente: '" + TestDataProvider.BUSQUEDA_NO_EXISTE + "'");
    }

    @Step("Verificar que el empty state 'Sin resultados' esta visible")
    private void stepVerifyEmptyStateDisplayed() {
        boolean empty = movementsPage.isEmptyStateDisplayed();
        AllureHelper.logValidation("Empty state visible", empty, true, empty);
        Assert.assertTrue(empty,
                "Empty state debe estar visible tras buscar '" + TestDataProvider.BUSQUEDA_NO_EXISTE + "'");
    }
}
