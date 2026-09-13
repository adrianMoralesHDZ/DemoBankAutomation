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
 * Requisito: Tesseract OCR instalado en C:\Program Files\Tesseract-OCR\
 */
public class OCRUtils {

    private static final String TESSDATA_PATH = "C:\\Program Files\\Tesseract-OCR\\tessdata";

    private static final Tesseract tesseract = new Tesseract();

    static {
        tesseract.setDatapath(TESSDATA_PATH);
        tesseract.setLanguage("spa");
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
            ocr.setDatapath(TESSDATA_PATH);
            ocr.setLanguage("spa");
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
        String text = extractTextFromElement(element)
                .replace("$", "")
                .replaceAll("\\s+", "")
                .trim();

        String normalized;
        if (text.contains(",") && text.lastIndexOf(",") > text.lastIndexOf(".")) {
            normalized = text.replace(".", "").replace(",", ".");
        } else if (text.contains(".") && text.indexOf(".") == text.lastIndexOf(".")) {
            int lastDot = text.lastIndexOf(".");
            String afterDot = text.substring(lastDot + 1);
            if (afterDot.length() <= 2) {
                normalized = text;
            } else {
                normalized = text.replace(".", "");
            }
        } else {
            normalized = text.replaceAll("[.,]", "");
        }

        if (normalized.isEmpty() || !normalized.matches(".*[0-9].*")) {
            return 0;
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new RuntimeException("No se pudo parsear el monto OCR: '" + text
                    + "' (normalizado: '" + normalized + "')", e);
        }
    }
}
