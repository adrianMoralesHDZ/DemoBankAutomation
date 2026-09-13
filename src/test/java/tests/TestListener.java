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
 * Los screenshots de cada paso del flujo son capturados automaticamente
 * por AllureHelper (reportAction, reportValidation, reportRead, etc.).
 * <p>
 * Adicionalmente, este listener captura un screenshot extra del estado
 * de error cuando un test falla, junto con el stack trace completo.
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("[TestListener] INICIO test: " + result.getMethod().getMethodName());
    }

    /**
     * Captura screenshot del estado de error, adjunta el stack trace
     * y metadatos de trazabilidad al reporte de Allure.
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
