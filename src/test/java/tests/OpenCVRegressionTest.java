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
 * Test de regresion visual con OpenCV.
 * <p>
 * Compara un screenshot actual del Home contra una imagen baseline.
 * Umbral de aprobacion: Score >= 0.95 (95%).
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

    @Test(priority = 1, groups = {"visual-regression"})
    @Description("Regresion visual del Home - OpenCV contra baseline (Score >= 95%).")
    @Severity(SeverityLevel.NORMAL)
    public void testRegresionVisualHome() {
        double score = stepCompareWithBaseline();
        stepValidateScore(score);
    }

    @Step("Tomar screenshot del Home actual y comparar contra baseline con OpenCV")
    private double stepCompareWithBaseline() {
        File screenshot =
                ((TakesScreenshot) DriverFactory.getDriver())
                        .getScreenshotAs(OutputType.FILE);

        String baselinePath = "src/test/resources/baselines/home_baseline.png";
        double score = ImageMatchUtils.compareImages(baselinePath, screenshot.getAbsolutePath());

        AllureHelper.reportRead(
                "Comparacion visual OpenCV",
                "Baseline: " + baselinePath + " | Actual: " + screenshot.getName(),
                String.format("Score: %.4f (%.2f%%)", score, score * 100));
        return score;
    }

    @Step("Validar que el Score >= 0.95 (95%)")
    private void stepValidateScore(double score) {
        boolean passed = score >= 0.95;
        AllureHelper.reportValidation(
                "Score de similitud >= 95%",
                String.format("%.2f%%", score * 100),
                ">= 95.00%",
                passed,
                "Si el score es menor a 95%, hay una regresion visual en el Home");
        Assert.assertTrue(passed,
                "Regresion visual detectada. Score: " + score + " < 0.95 (95%)");
    }
}
