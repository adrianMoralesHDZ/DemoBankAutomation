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

/*
 * ============================================================================
 * TEST DE REGRESION VISUAL CON OPENCV (requisito del PDF)
 * ============================================================================
 *
 * Compara un screenshot actual del Home contra una imagen base (baseline)
 * para detectar cambios visuales (regresiones de UI).
 *
 * UMBRAL: Score >= 0.95 (95%)
 *
 * PRE-REQUISITO: Tener home_baseline.png en src/test/resources/baselines/
 *
 * REPORTES ALLURE:
 *   - Cada paso del test esta anotado con @Step.
 *   - Screenshot solo en caso de fallo (TestListener).
 * ============================================================================
 */
public class OpenCVRegressionTest {

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

    /**
     * Regresion visual del Home: comparar screenshot actual contra baseline.
     */
    @Test(priority = 1, groups = {"visual-regression"})
    @Description("Regresion visual del Home - OpenCV contra baseline (Score >= 95%).")
    @Severity(SeverityLevel.NORMAL)
    public void testRegresionVisualHome() {
        stepTakeScreenshot();
        double score = stepCompareWithBaseline();
        stepValidateScore(score);
    }

    @Step("Tomar screenshot del Home actual")
    private void stepTakeScreenshot() {
        AllureHelper.logAction("Capturar", "Screenshot del Home actual");
    }

    @Step("Comparar screenshot actual contra baseline con OpenCV")
    private double stepCompareWithBaseline() {
        File screenshot =
                ((TakesScreenshot) DriverFactory.getDriver())
                        .getScreenshotAs(OutputType.FILE);

        double score = ImageMatchUtils.compareImages(
                "src/test/resources/baselines/home_baseline.png",
                screenshot.getAbsolutePath()
        );
        AllureHelper.logStep("Match Score OpenCV: " + String.format("%.4f", score));
        return score;
    }

    @Step("Validar que el Score >= 0.95 (95%)")
    private void stepValidateScore(double score) {
        boolean passed = score >= 0.95;
        AllureHelper.logValidation("Score >= 95%", score, 0.95, passed);
        Assert.assertTrue(passed,
                "Regresion visual detectada. Score: " + score + " < 0.95 (95%)");
    }
}
