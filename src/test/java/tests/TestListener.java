package tests;

import co.com.demobank.projec.utils.AllureHelper;
import co.com.demobank.projec.utils.DriverFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

/*
 * ============================================================================
 * LISTENER: TestListener
 * ============================================================================
 *
 * Captura screenshots automaticamente:
 *   - En FALLO: captura el estado de error
 *   - En EXITO: captura el estado final del test
 *
 * Usa AllureHelper que:
 *   - Adjunta el screenshot al step activo en Allure
 *   - Adjunta metadata (URL, title)
 *   - Maneja errores sin romper el test
 * ============================================================================
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        AllureHelper.screenshot("❌ FALLO - " + result.getName());
        AllureHelper.attachText(
                "Test Failure Details",
                "Test: " + result.getName() + "\n" +
                "Status: FAILED\n" +
                "Exception: " + (result.getThrowable() != null ?
                        result.getThrowable().getMessage() : "N/A")
        );
        System.out.println("[TestListener] ❌ Captura de FALLO adjuntada: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        AllureHelper.screenshot("✅ EXITO - " + result.getName());
        System.out.println("[TestListener] ✅ Captura de EXITO adjuntada: " + result.getName());
    }

    /**
     * Captura al iniciar el test (en setUp).
     */
    @Override
    public void onTestStart(ITestResult result) {
        AllureHelper.screenshot("▶ INICIO - " + result.getName());
    }

    /**
     * Helper estatico para capturas intermedias (llamar desde tests).
     */
    public static void captureAndAttachScreenshot(String description) {
        AllureHelper.screenshot(description);
    }
}
