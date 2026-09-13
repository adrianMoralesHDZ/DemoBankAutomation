package tests;

import co.com.demobank.projec.pages.*;
import co.com.demobank.projec.utils.*;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Step;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test de regresion visual del Home con OpenCV.
 * <p>
 * Compara un screenshot actual del Home contra una imagen baseline.
 * Umbral de aprobacion: Score >= 0.95 (95%).
 */
public class OpenCVRegressionTest {

    private static final String BASELINE_DIR = "src/test/resources/baselines/";

    private LoginPage loginPage;
    private HomePage homePage;

    @BeforeMethod
    public void setUp() {
        DriverFactory.getDriver();
        loginPage = new LoginPage(DriverFactory.getDriver());
        homePage = new HomePage(DriverFactory.getDriver());
        loginPage.loginAs(TestDataProvider.getValidEmail(), TestDataProvider.getValidPassword());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    @Test(priority = 1, groups = {"visual-regression"})
    @Description("Regresion visual del Home - OpenCV contra baseline (Score >= 95%).")
    @Severity(SeverityLevel.NORMAL)
    public void testRegresionVisualHome() {
        stepCompareHomeWithBaseline();
    }

    @Step("Comparar screenshot del Home actual contra baseline con OpenCV")
    private void stepCompareHomeWithBaseline() {
        ImageMatchUtils.assertScreenMatches(
                BASELINE_DIR, "home_baseline.png", "Pantalla Home");
    }
}
