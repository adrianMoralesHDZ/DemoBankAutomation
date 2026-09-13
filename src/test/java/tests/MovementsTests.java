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
 * Modulo 3: Movimientos (4 casos).
 * <p>
 * TC09 - Busqueda parcial case-insensitive
 * TC10 - Filtro Ingresos (montos positivos)
 * TC11 - Filtro Gastos (montos negativos)
 * TC12 - Empty state (sin resultados)
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
        loginPage.loginAs(TestDataProvider.getValidEmail(), TestDataProvider.getValidPassword());
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
        int count = movementsPage.getAllMovementAmounts().size();
        AllureHelper.reportValidation(
                "Movimientos visibles antes de buscar",
                has ? count + " movimientos con monto '$' detectados" : "No hay movimientos visibles",
                "Al menos 1 movimiento visible",
                has,
                "La lista de movimientos debe cargar con datos al abrir la pantalla");
        Assert.assertTrue(has, "Debe haber movimientos visibles antes de buscar");
    }

    @Step("Buscar termino parcial '{0}' (en minusculas)")
    private void stepSearchPartialTerm() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_PARCIAL);
        AllureHelper.reportAction(
                "Escribir", "Campo de busqueda 'Buscar movimiento'",
                TestDataProvider.BUSQUEDA_PARCIAL,
                "Termino escrito, esperando filtrado de la lista (case-insensitive)");
    }

    @Step("Verificar que la busqueda parcial devolvio resultados")
    private void stepVerifySearchHasResults() {
        boolean has = movementsPage.hasMovements();
        int count = movementsPage.getAllMovementAmounts().size();
        AllureHelper.reportValidation(
                "Resultados de busqueda parcial '" + TestDataProvider.BUSQUEDA_PARCIAL + "'",
                has ? count + " movimientos encontrados" : "No se encontraron resultados",
                "Al menos 1 movimiento que contenga 'Transferencia'",
                has,
                "La busqueda debe ser case-insensitive: 'transfe' debe encontrar 'Transferencia'");
        Assert.assertTrue(has, "Debe haber resultados al buscar '" + TestDataProvider.BUSQUEDA_PARCIAL + "'");
    }

    // ========================================================================
    // TC10 - FILTRO INGRESOS
    // ========================================================================

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
        AllureHelper.reportAction(
                "Tap", "Filtro 'Ingresos' (tab superior)", null,
                "Filtro activado, esperando solo movimientos con monto positivo (+)");
    }

    @Step("Verificar que hay movimientos despues del filtro Ingresos")
    private void stepVerifyMovementsExistAfterFilter() {
        boolean has = movementsPage.hasMovements();
        int count = movementsPage.getAllMovementAmounts().size();
        AllureHelper.reportValidation(
                "Movimientos visibles con filtro Ingresos",
                has ? count + " movimientos mostrados" : "No hay movimientos",
                "Al menos 1 movimiento de ingreso",
                has,
                "El filtro Ingresos debe mostrar movimientos con monto > 0");
        Assert.assertTrue(has, "Filtro Ingresos debe mostrar movimientos");
    }

    @Step("Validar que todos los montos visibles empiezan con '+' (ingresos)")
    private void stepVerifyAllAmountsPositive() {
        boolean allPositive = movementsPage.allAmountsArePositive();
        int count = movementsPage.getAllMovementAmounts().size();

        StringBuilder montosDetalle = new StringBuilder();
        for (int i = 0; i < movementsPage.getAllMovementAmounts().size(); i++) {
            montosDetalle.append(movementsPage.getAllMovementAmounts().get(i).getText());
            if (i < count - 1) montosDetalle.append(", ");
        }

        AllureHelper.reportValidation(
                "Todos los montos son positivos (+)",
                allPositive ? "Todos los " + count + " montos empiezan con '+'" : "Hay montos que NO empiezan con '+'",
                "Todos los montos empiezan con '+'",
                allPositive,
                "Montos detectados: " + montosDetalle);
        Assert.assertTrue(allPositive,
                "Todos los montos con filtro Ingresos deben empezar con '+'. Montos: " + count);
    }

    // ========================================================================
    // TC11 - FILTRO GASTOS
    // ========================================================================

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
        AllureHelper.reportAction(
                "Tap", "Filtro 'Gastos' (tab superior)", null,
                "Filtro activado, esperando solo movimientos con monto negativo (-)");
    }

    @Step("Verificar que hay movimientos despues del filtro Gastos")
    private void stepVerifyMovementsExistAfterExpenseFilter() {
        boolean has = movementsPage.hasMovements();
        int count = movementsPage.getAllMovementAmounts().size();
        AllureHelper.reportValidation(
                "Movimientos visibles con filtro Gastos",
                has ? count + " movimientos mostrados" : "No hay movimientos",
                "Al menos 1 movimiento de gasto",
                has,
                "El filtro Gastos debe mostrar movimientos con monto < 0");
        Assert.assertTrue(has, "Filtro Gastos debe mostrar movimientos");
    }

    @Step("Validar que todos los montos visibles empiezan con '-' (gastos)")
    private void stepVerifyAllAmountsNegative() {
        boolean allNegative = movementsPage.allAmountsAreNegative();
        int count = movementsPage.getAllMovementAmounts().size();

        StringBuilder montosDetalle = new StringBuilder();
        for (int i = 0; i < movementsPage.getAllMovementAmounts().size(); i++) {
            montosDetalle.append(movementsPage.getAllMovementAmounts().get(i).getText());
            if (i < count - 1) montosDetalle.append(", ");
        }

        AllureHelper.reportValidation(
                "Todos los montos son negativos (-)",
                allNegative ? "Todos los " + count + " montos empiezan con '-'" : "Hay montos que NO empiezan con '-'",
                "Todos los montos empiezan con '-'",
                allNegative,
                "Montos detectados: " + montosDetalle);
        Assert.assertTrue(allNegative,
                "Todos los montos con filtro Gastos deben empezar con '-'. Montos: " + count);
    }

    // ========================================================================
    // TC12 - EMPTY STATE
    // ========================================================================

    @Test(priority = 4, groups = {"movements", "negative"})
    @Description("TC12 - Empty state: buscar texto inexistente muestra 'Sin resultados'.")
    @Severity(SeverityLevel.CRITICAL)
    public void testEmptyState() {
        stepSearchNonExistentTerm();
        stepVerifyEmptyStateDisplayed();
    }

    @Step("Buscar termino inexistente '{0}'")
    private void stepSearchNonExistentTerm() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_NO_EXISTE);
        AllureHelper.reportAction(
                "Escribir", "Campo de busqueda",
                TestDataProvider.BUSQUEDA_NO_EXISTE,
                "Termino inexistente escrito, esperando empty state");
    }

    @Step("Verificar que el empty state 'Sin resultados' esta visible")
    private void stepVerifyEmptyStateDisplayed() {
        boolean empty = movementsPage.isEmptyStateDisplayed();
        AllureHelper.reportValidation(
                "Empty state visible tras busqueda sin resultados",
                empty ? "Mensaje 'Sin resultados' visible en pantalla" : "No se detecto el empty state",
                "Mensaje de 'Sin resultados' o 'No hay movimientos que coincidan'",
                empty,
                "Cuando la busqueda no tiene coincidencias, la app debe mostrar un empty state");
        Assert.assertTrue(empty,
                "Empty state debe estar visible tras buscar '" + TestDataProvider.BUSQUEDA_NO_EXISTE + "'");
    }
}
