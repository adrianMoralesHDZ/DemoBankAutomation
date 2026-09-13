package co.com.demobank.projec.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;

/**
 * Helper para adjuntar evidencias y narrar el flujo de ejecucion
 * en el reporte de Allure.
 * <p>
 * Cada metodo produce texto estructurado que describe que se hizo,
 * que valor se envio, que valor devolvio la app y cual fue el resultado.
 * Adicionalmente, cada metodo captura un screenshot del estado actual
 * de la app y lo adjunta al step correspondiente.
 * <p>
 * Esto genera un reporte narrativo con evidencia visual en cada paso,
 * similar a Serenity.
 */
public class AllureHelper {

    // ========================================================================
    // SCREENSHOT BASE
    // ========================================================================

    /**
     * Captura un screenshot del driver actual y lo adjunta al reporte.
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

    // ========================================================================
    // METODOS DE NARRATIVA DE FLUJO CON CAPTURA AUTOMATICA (estilo Serenity)
    // ========================================================================
    // Cada metodo:
    //   1. Adjunta texto estructurado con el detalle de la accion/validacion
    //   2. Captura un screenshot del estado actual de la app
    // ========================================================================

    /**
     * Reporta una accion realizada sobre la app y captura screenshot.
     *
     * @param action  que se hizo (ej: "Escribir", "Tap", "Seleccionar")
     * @param element sobre que elemento (ej: "Campo de email", "Boton 'Iniciar sesion'")
     * @param input   que valor se envio (ej: "demo@demo.com")
     * @param result  que resultado se obtuvo (ej: "Texto escrito correctamente")
     */
    public static void reportAction(String action, String element, String input, String result) {
        StringBuilder sb = new StringBuilder();
        sb.append("ACCION    : ").append(action).append("\n");
        sb.append("ELEMENTO  : ").append(element).append("\n");
        if (input != null && !input.isEmpty()) {
            sb.append("VALOR ENVIADO: ").append(input).append("\n");
        }
        sb.append("RESULTADO : ").append(result);
        Allure.addAttachment("Detalle de la accion", "text/plain", sb.toString(), "txt");
        screenshot(action + " - " + element);
    }

    /**
     * Reporta un valor leido de la app y captura screenshot.
     *
     * @param label     que se leyo (ej: "Saldo consolidado", "Numero de cuenta")
     * @param rawValue  valor crudo obtenido (ej: "$2,455,450.00")
     * @param parsed    valor parseado si aplica (ej: "2455450.00")
     */
    public static void reportRead(String label, String rawValue, String parsed) {
        StringBuilder sb = new StringBuilder();
        sb.append("LECTURA        : ").append(label).append("\n");
        sb.append("VALOR EN PANTALLA: ").append(rawValue).append("\n");
        if (parsed != null && !parsed.isEmpty()) {
            sb.append("VALOR PROCESADO  : ").append(parsed);
        }
        Allure.addAttachment("Valor leido de la app", "text/plain", sb.toString(), "txt");
        screenshot("Lectura: " + label);
    }

    /**
     * Reporta una validacion con comparacion de valores y captura screenshot.
     *
     * @param checkName nombre de la validacion
     * @param actual    valor actual obtenido de la app
     * @param expected  valor esperado
     * @param passed    si paso o no
     * @param detail    detalle adicional del contexto
     */
    public static void reportValidation(String checkName, String actual, String expected,
                                        boolean passed, String detail) {
        StringBuilder sb = new StringBuilder();
        sb.append("VALIDACION    : ").append(checkName).append("\n");
        sb.append("VALOR OBTENIDO: ").append(actual).append("\n");
        sb.append("VALOR ESPERADO: ").append(expected).append("\n");
        if (detail != null && !detail.isEmpty()) {
            sb.append("DETALLE       : ").append(detail).append("\n");
        }
        sb.append("RESULTADO     : ").append(passed ? "PASS" : "FAIL");
        Allure.addAttachment("Resultado de validacion", "text/plain", sb.toString(), "txt");
        screenshot("Validacion: " + checkName + " [" + (passed ? "PASS" : "FAIL") + "]");
    }

    /**
     * Reporta una navegacion entre pantallas y captura screenshot.
     *
     * @param from    pantalla de origen
     * @param to      pantalla de destino
     * @param success si la navegacion fue exitosa
     */
    public static void reportNavigation(String from, String to, boolean success) {
        StringBuilder sb = new StringBuilder();
        sb.append("NAVEGACION    : ").append(from).append(" -> ").append(to).append("\n");
        sb.append("RESULTADO     : ").append(success ? "Navegacion exitosa" : "Navegacion fallida");
        Allure.addAttachment("Navegacion entre pantallas", "text/plain", sb.toString(), "txt");
        screenshot("Navegacion: " + from + " -> " + to);
    }

    /**
     * Reporta el estado de una pantalla y captura screenshot.
     *
     * @param screenName  nombre de la pantalla
     * @param indicators  elementos detectados
     */
    public static void reportScreenState(String screenName, String indicators) {
        StringBuilder sb = new StringBuilder();
        sb.append("PANTALLA      : ").append(screenName).append("\n");
        sb.append("INDICADORES   : ").append(indicators);
        Allure.addAttachment("Estado de pantalla", "text/plain", sb.toString(), "txt");
        screenshot("Pantalla: " + screenName);
    }

    // ========================================================================
    // METODOS DE ERROR (usados por TestListener)
    // ========================================================================

    /**
     * Adjunta texto plano al reporte.
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
}
