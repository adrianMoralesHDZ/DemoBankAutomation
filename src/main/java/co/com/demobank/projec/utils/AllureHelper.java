package co.com.demobank.projec.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

/**
 * Helper para adjuntar evidencias al reporte de Allure.
 * <p>
 * Los screenshots se capturan automaticamente solo ante fallos
 * (gestionado por TestListener). El reporte paso a paso se logra
 * con anotaciones @Step en los metodos de los tests.
 */
public class AllureHelper {

    /**
     * Captura un screenshot del driver actual y lo adjunta al reporte.
     * Debe llamarse solo en caso de fallo (via TestListener).
     *
     * @param description descripcion de la captura
     */
    public static void screenshot(String description) {
        try {
            Object driver = DriverFactory.getDriver();
            if (driver == null) return;

            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.BYTES);

                if (screenshot.length > 0) {
                    Allure.addAttachment(
                            description, "image/png",
                            new ByteArrayInputStream(screenshot), "png");
                }
            }
        } catch (Exception e) {
            System.err.println("[Allure] Error al capturar screenshot: " + e.getMessage());
        }
    }

    /**
     * Adjunta texto plano al reporte (logs, valores intermedios, stack traces).
     *
     * @param title   titulo del attachment
     * @param content contenido textual
     */
    public static void attachText(String title, String content) {
        Allure.addAttachment(title, "text/plain", content, "txt");
    }

    /**
     * Adjunta metadatos de trazabilidad de la pagina actual.
     */
    public static void attachPageInfo() {
        try {
            Object driver = DriverFactory.getDriver();
            if (driver == null) return;
            WebDriver wd = (WebDriver) driver;
            String info = "Current URL: " + wd.getCurrentUrl() + "\n" +
                    "Window handle: " + wd.getWindowHandle() + "\n" +
                    "Title: " + wd.getTitle() + "\n" +
                    "Timestamp: " + System.currentTimeMillis();
            attachText("Page Info (trazabilidad)", info);
        } catch (Exception ignored) {
        }
    }

    /**
     * Registra un mensaje informativo en el reporte (sin screenshot).
     *
     * @param message mensaje a registrar
     */
    public static void logStep(String message) {
        Allure.addAttachment("Paso", "text/plain", message, "txt");
    }

    /**
     * Registra el resultado de una validacion en el reporte (sin screenshot).
     *
     * @param checkName nombre de la validacion
     * @param actual     valor actual obtenido
     * @param expected   valor esperado
     * @param passed     resultado de la validacion
     */
    public static void logValidation(String checkName, Object actual, Object expected, boolean passed) {
        String status = passed ? "PASS" : "FAIL";
        String content = "Validacion: " + checkName + "\n" +
                "  Valor actual:   " + actual + "\n" +
                "  Valor esperado: " + expected + "\n" +
                "  Resultado:      " + status;
        Allure.addAttachment("Validacion - " + checkName, "text/plain", content, "txt");
    }

    /**
     * Registra informacion de una accion realizada en el reporte (sin screenshot).
     *
     * @param action tipo de accion (Tap, Escribir, Seleccionar, etc.)
     * @param target elemento objetivo de la accion
     */
    public static void logAction(String action, String target) {
        String content = action + " \u2192 " + target;
        Allure.addAttachment("Accion", "text/plain", content, "txt");
    }
}
