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

import java.io.File;

/*
 * ============================================================================
 * TEST DE REGRESIÓN VISUAL CON OPENCV (requisito del PDF)
 * ============================================================================
 *
 * Compara un screenshot actual del Home contra una imagen base (baseline)
 * para detectar cambios visuales (regresiones de UI).
 *
 * UMBRAL: Score >= 0.95 (95%)
 *
 * PRE-REQUISITO: Tener home_baseline.png en src/test/resources/baselines/
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

    @Test(priority = 1, groups = {"visual-regression"})
    @Description("Regresion visual del Home - OpenCV contra baseline")
    @Severity(SeverityLevel.NORMAL)
    public void testRegresionVisualHome() {
        TestListener.captureAndAttachScreenshot("Home actual para comparar");

        File screenshot =
                ((org.openqa.selenium.TakesScreenshot) DriverFactory.getDriver())
                        .getScreenshotAs(org.openqa.selenium.OutputType.FILE);

        // NOTA: la ruta del baseline la defines tu segun donde generes la imagen
        double score = ImageMatchUtils.compareImages(
                "src/test/resources/baselines/home_baseline.png",
                screenshot.getAbsolutePath()
        );

        System.out.println("Match Score OpenCV: " + score);
        Assert.assertTrue(score >= 0.95,
                "Regresion visual detectada. Score: " + score + " < 0.95");
    }
}
