package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test de regresion visual del flujo de busqueda parcial en Movimientos (TC09).
 * <p>
 * Reproduce el mismo flujo que TC09 (Login -> Home -> Movimientos -> Buscar "transfe")
 * pero en cada paso toma un screenshot y lo compara contra una imagen baseline
 * mediante OpenCV.
 * <p>
 * Las baselines estan en:
 *   src/test/resources/baselines/movements_search/
 *     01_home.png             - Pantalla Home despues de login
 *     02_movements_list.png   - Lista de movimientos completa
 *     03_search_results.png   - Resultados filtrados por "transfe"
 * <p>
 * La prueba pasa en cada paso si el Match Score >= 95%.
 * <p>
 * Requisito PDF: "Validacion Visual mediante OpenCV (Minimo 1). Se debe
 * ejecutar una comprobacion de regresion estetica comparando un screenshot
 * en tiempo real contra una imagen base de referencia almacenada en
 * src/test/resources/baselines/. La prueba se considerara aprobada si el
 * algoritmo de correlacion visual arroja un umbral de similitud (Match Score)
 * igual o superior al 95%."
 */
public class MovementsVisualRegressionTest {

    private static final String BASELINE_DIR = "src/test/resources/baselines/movements_search/";
    private static final double MATCH_THRESHOLD = 0.95;

    private LoginPage loginPage;
    private HomePage homePage;
    private MovementsPage movementsPage;

    @BeforeMethod
    public void setUp() {
        DriverFactory.getDriver();
        loginPage = new LoginPage(DriverFactory.getDriver());
        homePage = new HomePage(DriverFactory.getDriver());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    /**
     * TC09-Visual: Regresion visual paso a paso del flujo de busqueda parcial.
     * <p>
     * Flujo:
     *   1. Login -> capturar Home -> comparar contra 01_home.png
     *   2. Tap Movimientos -> capturar lista -> comparar contra 02_movements_list.png
     *   3. Buscar "transfe" -> capturar resultados -> comparar contra 03_search_results.png
     */
    @Test(priority = 1, groups = {"visual-regression", "movements"})
    @Description("TC09-Visual - Regresion visual paso a paso: Login -> Home -> Movimientos -> Buscar 'transfe'. "
            + "Compara cada pantalla contra baseline con OpenCV (Score >= 95%).")
    @Severity(SeverityLevel.CRITICAL)
    public void testRegresionVisualBusquedaParcial() {
        stepLoginAndCaptureHome();
        stepOpenMovementsAndCaptureList();
        stepSearchAndCaptureResults();
    }

    /**
     * Paso 1: Login y captura del Home.
     * Compara contra baseline 01_home.png.
     */
    @Step("Paso 1 - Login y capturar pantalla Home para comparar contra baseline")
    private void stepLoginAndCaptureHome() {
        loginPage.loginAs(TestDataProvider.getValidEmail(), TestDataProvider.getValidPassword());
        AllureHelper.reportAction(
                "Login", "Credenciales validas",
                TestDataProvider.getValidEmail(), "Login realizado, capturando Home");

        compareScreenshotWithBaseline("01_home.png", "Pantalla Home despues de login");
    }

    /**
     * Paso 2: Abrir Movimientos y capturar la lista.
     * Compara contra baseline 02_movements_list.png.
     */
    @Step("Paso 2 - Abrir Movimientos y capturar lista para comparar contra baseline")
    private void stepOpenMovementsAndCaptureList() {
        homePage.tapQuickMovements();
        movementsPage = new MovementsPage(DriverFactory.getDriver());
        AllureHelper.reportAction(
                "Tap", "Boton 'Movimientos'",
                null, "Pantalla de Movimientos abierta, capturando lista");

        compareScreenshotWithBaseline("02_movements_list.png", "Lista de movimientos completa");
    }

    /**
     * Paso 3: Buscar "transfe" y capturar los resultados.
     * Compara contra baseline 03_search_results.png.
     */
    @Step("Paso 3 - Buscar 'transfe' y capturar resultados para comparar contra baseline")
    private void stepSearchAndCaptureResults() {
        movementsPage.searchFor(TestDataProvider.BUSQUEDA_PARCIAL);
        AllureHelper.reportAction(
                "Escribir", "Campo de busqueda",
                TestDataProvider.BUSQUEDA_PARCIAL, "Busqueda realizada, capturando resultados");

        compareScreenshotWithBaseline("03_search_results.png",
                "Resultados filtrados por '" + TestDataProvider.BUSQUEDA_PARCIAL + "'");
    }

    // ========================================================================
    // Metodos privados
    // ========================================================================

    /**
     * Toma un screenshot del estado actual, lo compara contra la baseline
     * indicada mediante OpenCV y valida que el Match Score >= 95%.
     *
     * @param baselineName nombre del archivo baseline (ej: "01_home.png")
     * @param stepName     descripcion del paso para el reporte
     */
    private void compareScreenshotWithBaseline(String baselineName, String stepName) {
        File screenshot = ((TakesScreenshot) DriverFactory.getDriver())
                .getScreenshotAs(OutputType.FILE);

        String baselinePath = BASELINE_DIR + baselineName;
        double score = ImageMatchUtils.compareImages(baselinePath, screenshot.getAbsolutePath());

        boolean passed = score >= MATCH_THRESHOLD;

        AllureHelper.reportValidation(
                "OpenCV - " + stepName,
                String.format("Match Score: %.2f%%", score * 100),
                "Score >= 95.00%",
                passed,
                "Baseline: " + baselinePath + " | Si el score es < 95%, hay una "
                        + "regresion visual en esta pantalla del flujo.");
        Assert.assertTrue(passed,
                "Regresion visual en '" + stepName + "': Score " + score + " < 0.95 (95%)");
    }
}
