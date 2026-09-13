package tests;

import co.com.demobank.projec.utils.AllureHelper;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Listener de TestNG que gestiona el ciclo de vida de los tests y el
 * reporte de Allure.
 * <p>
 * RESPONSABILIDADES:
 * <ul>
 *   <li>Registrar el inicio/fin de cada test en consola.</li>
 *   <li>Capturar screenshot + stack trace cuando un test FALLA.
 *       Esta captura ocurre SIEMPRE, sin importar el modo configurado.</li>
 * </ul>
 * <p>
 * MODOS DE CAPTURA (configurados en AllureHelper):
 * <ul>
 *   <li><b>MODO FLUJO COMPLETO</b> ({@code -Dallure.screenshots.everyStep=true}):
 *       AllureHelper captura en cada paso + este listener captura en fallos.</li>
 *   <li><b>MODO SOLO FALLOS</b> ({@code -Dallure.screenshots.everyStep=false}):
 *       AllureHelper NO captura en pasos. Solo este listener captura en fallos.</li>
 * </ul>
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("[TestListener] INICIO test: " + result.getMethod().getMethodName());
    }

    /**
     * Captura screenshot del estado de error, adjunta el stack trace
     * y metadatos de trazabilidad al reporte de Allure.
     * <p>
     * Esta captura SIEMPRE se ejecuta, sin importar el modo configurado.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getName();
        long duration = result.getEndMillis() - result.getStartMillis();

        AllureHelper.screenshot("FALLO - " + testName);

        Throwable throwable = result.getThrowable();
        String errorMessage = throwable != null ? throwable.getMessage() : "N/A";

        AllureHelper.attachText(
                "Detalles del Fallo",
                "Test: " + testName + "\n" +
                "Clase: " + result.getTestClass().getName() + "\n" +
                "Estado: FAILED\n" +
                "Duracion: " + duration + " ms\n" +
                "Excepcion: " + (throwable != null ? throwable.getClass().getName() : "N/A") + "\n" +
                "Mensaje: " + errorMessage + "\n\n" +
                "Stack Trace:\n" + getStackTrace(throwable)
        );

        AllureHelper.attachPageInfo();

        System.out.println("[TestListener] FALLO capturado: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        System.out.println("[TestListener] EXITO test: " + result.getName()
                + " | Duracion: " + duration + " ms");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("[TestListener] SKIPPED test: " + result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("[TestListener] INICIO suite: " + context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        int passed = context.getPassedTests().size();
        int failed = context.getFailedTests().size();
        int skipped = context.getSkippedTests().size();
        System.out.println("[TestListener] FIN suite: " + context.getName()
                + " | Pasaron=" + passed + " | Fallaron=" + failed
                + " | Omitidos=" + skipped);
    }

    /**
     * Convierte el stack trace de una excepcion a String.
     */
    private String getStackTrace(Throwable throwable) {
        if (throwable == null) return "N/A";
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
