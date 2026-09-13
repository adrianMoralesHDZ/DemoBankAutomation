package co.com.demobank.projec.utils;

import io.appium.java_client.android.AndroidDriver;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.io.File;

/**
 * Utilidad de OCR mediante Tesseract (Tess4J).
 * <p>
 * Permite leer texto de imagenes cuando los selectores nativos no son
 * confiables (componentes graficos, tarjetas con gradiente, fuentes
 * personalizadas, etc.).
 * <p>
 * La ruta de tessdata y el idioma se configuran en {@code config.properties}.
 */
public class OCRUtils {

    private static final Tesseract tesseract = new Tesseract();

    static {
        tesseract.setDatapath(ConfigReader.getTessdataPath());
        tesseract.setLanguage(ConfigReader.getTessLanguage());
        tesseract.setTessVariable("tessedit_char_whitelist", "0123456789$,. ");
    }

    /**
     * Toma un screenshot del elemento y extrae el texto mediante OCR.
     *
     * @param element WebElement del cual extraer texto
     * @return texto reconocido (sin espacios al inicio/fin)
     */
    public static String extractTextFromElement(WebElement element) {
        try {
            File image = element.getScreenshotAs(OutputType.FILE);
            String text = tesseract.doOCR(image).trim();
            System.out.println("[OCR] Texto leido: \"" + text + "\"");
            return text;
        } catch (TesseractException e) {
            throw new RuntimeException("Error OCR al procesar elemento", e);
        }
    }

    /**
     * Toma un screenshot de la pantalla completa y extrae el texto mediante OCR.
     *
     * @param driver AndroidDriver actual
     * @return texto reconocido de toda la pantalla
     */
    public static String extractTextFromScreen(AndroidDriver driver) {
        try {
            File image = driver.getScreenshotAs(OutputType.FILE);
            return tesseract.doOCR(image).trim();
        } catch (TesseractException e) {
            throw new RuntimeException("Error OCR al procesar pantalla", e);
        }
    }

    /**
     * Extrae todo el texto de la pantalla sin restriccion de caracteres
     * (whitelist deshabilitada). Permite leer palabras, no solo numeros.
     *
     * @param driver AndroidDriver actual
     * @return texto completo reconocido
     */
    public static String extractFullTextFromScreen(AndroidDriver driver) {
        try {
            Tesseract ocr = new Tesseract();
            ocr.setDatapath(ConfigReader.getTessdataPath());
            ocr.setLanguage(ConfigReader.getTessLanguage());
            File image = driver.getScreenshotAs(OutputType.FILE);
            String text = ocr.doOCR(image).trim();
            System.out.println("[OCR Full] Texto de pantalla: \"" + text + "\"");
            return text;
        } catch (TesseractException e) {
            throw new RuntimeException("Error OCR al procesar pantalla completa", e);
        }
    }

    /**
     * Extrae un monto monetario de un elemento mediante OCR y lo convierte
     * a double. Maneja formatos colombianos y estadounidenses.
     * <p>
     * Ejemplo: "$ 1,500,000.00" -> 1500000.00
     *
     * @param element WebElement que contiene el monto
     * @return valor numerico del monto
     */
    public static double extractCurrencyAmount(WebElement element) {
        String text = extractTextFromElement(element).trim();

        int indicePeso = text.indexOf("$");

        if (indicePeso != -1) {
            text = text.substring(indicePeso + 1).trim();
        }

        text = text
                .replaceAll("\\s+", "")
                .replace(",", "");

        if (text.isEmpty() || !text.matches(".*[0-9].*")) {
            return 0;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "No se pudo parsear el monto OCR: '" + text + "'", e);
        }
    }
}
