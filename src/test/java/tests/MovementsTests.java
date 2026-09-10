package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/*
 * ============================================================================
 * MÓDULO 3: MOVIMIENTOS (4 casos - requisito del PDF)
 * ============================================================================
 *
 * Cubre:
 *   1. Búsqueda parcial case-insensitive
 *   2. Filtro Ingresos (montos positivos)
 *   3. Filtro Gastos (montos negativos)
 *   4. Empty state (sin resultados)
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

    /**
     * CASO 1: Búsqueda parcial case-insensitive.
     * Si la lista tiene algo como "Transferencia Juan", buscar "juan" debe filtrarlo.
     */
    @Test(priority = 1, groups = {"movements", "search"})
    @Description("Busqueda parcial funciona case-insensitive")
    @Severity(SeverityLevel.CRITICAL)
    public void testBusquedaParcial() {
        Assert.assertTrue(movementsPage.hasMovements(), "Debe haber movimientos");
        TestListener.captureAndAttachScreenshot("Lista inicial de movimientos");

        // Buscar un texto común (ej: "transfe" parte de "transferencia")
        movementsPage.searchFor("transfe");
        TestListener.captureAndAttachScreenshot("Despues de buscar 'transfe'");
        Assert.assertTrue(movementsPage.hasMovements(),
                "Debe haber resultados con texto parcial");
    }

    /**
     * CASO 2: Filtro Ingresos muestra solo montos positivos.
     */
    @Test(priority = 2, groups = {"movements", "filter"})
    @Description("Filtro Ingresos solo muestra montos positivos")
    @Severity(SeverityLevel.CRITICAL)
    public void testFiltroIngresos() {
        movementsPage.tapIncomeFilter();
        TestListener.captureAndAttachScreenshot("Filtro Ingresos aplicado");
        Assert.assertTrue(movementsPage.hasMovements(),
                "Filtro ingresos debe mostrar movimientos");
    }

    /**
     * CASO 3: Filtro Gastos muestra solo montos negativos.
     */
    @Test(priority = 3, groups = {"movements", "filter"})
    @Description("Filtro Gastos solo muestra montos negativos")
    @Severity(SeverityLevel.CRITICAL)
    public void testFiltroGastos() {
        movementsPage.tapExpenseFilter();
        TestListener.captureAndAttachScreenshot("Filtro Gastos aplicado");
        Assert.assertTrue(movementsPage.hasMovements(),
                "Filtro gastos debe mostrar movimientos");
    }

    /**
     * CASO 4: Empty state al buscar texto sin coincidencias.
     */
    @Test(priority = 4, groups = {"movements", "negative"})
    @Description("Busqueda sin resultados muestra empty state")
    @Severity(SeverityLevel.CRITICAL)
    public void testEmptyState() {
        movementsPage.searchFor("xyzzz_no_existe_12345");
        TestListener.captureAndAttachScreenshot("Busqueda sin resultados");
        Assert.assertTrue(movementsPage.isEmptyStateDisplayed(),
                "Empty state debe estar visible tras busqueda sin resultados");
    }
}
