package co.com.demobank.projec.utils;

import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/*
 * ============================================================================
 * UTIL: AllureHelper
 * ============================================================================
 *
 * Helper para adjuntar screenshots y datos al reporte de Allure.
 *
 * Método principal:
 *   - captureScreenshot("descripción") → adjunta al reporte paso a paso
 *
 * Para llamar desde tests:
 *   - AllureHelper.screenshot("Antes de login");   ← captura + adjunta
 *
 * Tipos de archivos adjuntos:
 *   1. Screenshots (PNG) - captura por elemento o pantalla completa
 *   2. Texto (TXT/JSON)  - para OCR raw text, URLs, etc
 *
 * Cada captura se asocia al step actual (gracias a @Step de Allure).
 * ============================================================================
 */
public class AllureHelper {

    /**
     * Captura screenshot del driver actual y la adjunta al reporte Allure
     * con una descripción específica.
     *
     * @param description Texto descriptivo de la captura
     */
    public static void screenshot(String description) {
        try {
            Object driver = DriverFactory.getDriver();

            if (driver == null) {
                System.out.println("[Allure] No se pudo capturar: driver nulo");
                return;
            }

            if (driver instanceof TakesScreenshot) {
                byte[] screenshot = ((TakesScreenshot) driver)
                        .getScreenshotAs(OutputType.BYTES);

                if (screenshot.length > 0) {
                    Allure.addAttachment(
                            description,
                            "image/png",
                            new ByteArrayInputStream(screenshot),
                            "png"
                    );
                    System.out.println("[Allure] 📸 Captura: " + description +
                            " (" + screenshot.length + " bytes)");
                }
            }
        } catch (Exception e) {
            // No rompemos el test si falla la captura
            System.err.println("[Allure] Error al capturar screenshot '" +
                    description + "': " + e.getMessage());
        }
    }

    /**
     * Captura screenshot de un WebElement específico.
     */
    public static void screenshotOf(org.openqa.selenium.WebElement element, String description) {
        try {
            if (element == null) return;
            byte[] screenshot = element.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(
                    description,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    "png"
            );
            System.out.println("[Allure] 📸 Captura elemento: " + description);
        } catch (Exception e) {
            System.err.println("[Allure] Error al capturar elemento: " + e.getMessage());
        }
    }

    /**
     * Adjunta texto plano como attachment del reporte Allure.
     * Útil para OCR raw text o logs.
     */
    public static void attachText(String title, String content) {
        Allure.addAttachment(
                title,
                "text/plain",
                content,
                "txt"
        );
        System.out.println("[Allure] 📄 Texto adjunto: " + title);
    }

    /**
     * Adjunta el resultado de OCR como texto.
     */
    public static void attachOCR(String elementName, String rawText, double parsedAmount) {
        String content = "Elemento: " + elementName + "\n" +
                "Texto crudo OCR: \"" + rawText + "\"\n" +
                "Valor numerico parseado: " + parsedAmount + "\n" +
                "Timestamp: " + System.currentTimeMillis();
        attachText("OCR - " + elementName, content);
    }

    /**
     * Adjunta metadata sobre el estado actual del test (pagina, URL, etc).
     */
    public static void attachPageInfo() {
        try {
            Object driver = DriverFactory.getDriver();
            if (driver == null) return;
            org.openqa.selenium.WebDriver wd = (org.openqa.selenium.WebDriver) driver;
            String info = "Current URL: " + wd.getCurrentUrl() + "\n" +
                    "Window handle: " + wd.getWindowHandle() + "\n" +
                    "Title: " + wd.getTitle() + "\n" +
                    "Timestamp: " + System.currentTimeMillis();
            attachText("Page Info (trazabilidad)", info);
        } catch (Exception e) {
            // Ignorar
        }
    }
}
