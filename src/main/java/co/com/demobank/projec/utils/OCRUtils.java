package co.com.demobank.projec.utils;

import io.appium.java_client.android.AndroidDriver;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;

import java.io.File;

/*
 * ============================================================================
 * UTIL: OCRUtils
 * ============================================================================
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Es un "wrapper" (envoltorio) de Tesseract OCR (vía Tess4J).
 * Permite LEER TEXTO DE IMÁGENES cuando los elementos no tienen un
 * identificador de accesibilidad confiable.
 *
 * -----------------------------------------------------------------------
 * ¿QUÉ ES OCR?
 * -----------------------------------------------------------------------
 * OCR = Optical Character Recognition (Reconocimiento Óptico de Caracteres).
 * Tesseract toma una imagen y "lee" el texto que hay en ella.
 * Es como si la computadora "viera" la imagen y transcribiera lo que dice.
 *
 * -----------------------------------------------------------------------
 * ¿CUÁNDO USAR OCR? (justificación técnica del PDF)
 * -----------------------------------------------------------------------
 * El PDF exige mínimo 3 validaciones con OCR. Se usa cuando:
 *
 *   1. El elemento es un componente GRÁFICO (no un TextView accesible)
 *      Ej: saldo sobre una tarjeta con gradiente de fondo
 *
 *   2. El accessibility-id está vacío o no expone el valor
 *      Ej: montos renderizados con fuentes personalizadas
 *
 *   3. El texto está embebido en una imagen (Canvas, WebView, etc.)
 *      Ej: gráficos de saldo, totales personalizados
 *
 * En MercadoLibre (práctica):
 *   - Precios de productos que pueden estar en componentes gráficos
 *   - Totales del carrito
 *   - Precios en el detalle del producto
 *
 * En DemoBank (cuando tengas el APK):
 *   - Saldo consolidado en Home (tarjeta con gradiente)
 *   - Saldos individuales de cuentas Corriente/Ahorros
 *   - Monto de éxito en TransferSuccess/PaySuccess
 *
 * -----------------------------------------------------------------------
 * REQUISITO: INSTALAR TESSERACT OCR EN WINDOWS
 * -----------------------------------------------------------------------
 * 1. Descargar desde: https://github.com/UB-Mannheim/tesseract/wiki
 * 2. Instalar en: C:\Program Files\Tesseract-OCR\
 * 3. Los datos de idioma (tessdata) vienen incluidos
 * 4. Para español, descargar "spa.traineddata" y ponerlo en tessdata/
 *
 * Si no lo tienes instalado, los tests con OCR FALLARÁN.
 * ============================================================================
 */
public class OCRUtils {

    // ========================================================================
    // INSTANCIA DE TESSERACT (Singleton)
    // ========================================================================
    // Se crea una sola vez y se reutiliza en todas las llamadas.
    // ========================================================================
    private static final Tesseract tesseract = new Tesseract();

    // ========================================================================
    // BLOQUE ESTÁTICO: configuración de Tesseract
    // ========================================================================
    // Se ejecuta una sola vez cuando la clase se carga en memoria.
    // Configura:
    //   1. Ruta a los datos de entrenamiento (tessdata)
    //   2. Idioma (spa = español, eng = inglés)
    //   3. Caracteres permitidos (solo números y símbolos monetarios)
    // ========================================================================
    static {
        // Ruta donde están los archivos .traineddata (datos de idioma)
        // ⚠️ CAMBIA ESTA RUTA si instalaste Tesseract en otro lugar
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");

        // Idioma: "spa" para español, "eng" para inglés
        // Si tu app está en español, usa "spa"
        tesseract.setLanguage("spa");

        // Filtro de caracteres: solo permite números, $, comas, puntos, espacios
        // Esto mejora la precisión porque Tesseract no intenta leer letras
        tesseract.setTessVariable("tessedit_char_whitelist", "0123456789$,. ");
    }

    // ========================================================================
    // MÉTODO: extractTextFromElement
    // ========================================================================
    // Toma un WebElement, le hace un screenshot, y Tesseract lee el texto.
    //
    // USO:
    //   WebElement elemento = resultsPage.getPriceSectionElement();
    //   String texto = OCRUtils.extractTextFromElement(elemento);
    //   // texto = "$ 1,299.00"
    //
    // CÓMO FUNCIONA:
    //   1. element.getScreenshotAs(FILE) → captura el elemento como imagen
    //   2. tesseract.doOCR(image)        → lee el texto de la imagen
    //   3. trim()                        → elimina espacios sobrantes
    // ========================================================================
    public static String extractTextFromElement(WebElement element) {
        try {
            // Tomar screenshot del elemento (no de toda la pantalla)
            File image = element.getScreenshotAs(OutputType.FILE);
            // Tesseract lee el texto de la imagen
            String text = tesseract.doOCR(image).trim();
            System.out.println("[OCR] Texto leido: \"" + text + "\"");
            return text;
        } catch (TesseractException e) {
            throw new RuntimeException("Error OCR al procesar elemento", e);
        }
    }

    /**
     * Lee el texto Y luego lo convierte a número, retornando ambos.
     * Útil para logging/reporte de Allure.
     *
     * @return Array con [texto crudo OCR, valor numérico parseado]
     */
    public static Object[] extractTextAndAmount(WebElement element) {
        String rawText = extractTextFromElement(element);
        double amount = extractCurrencyAmount(element);
        return new Object[] { rawText, amount };
    }

    // ========================================================================
    // MÉTODO: extractTextFromScreen
    // ========================================================================
    // Igual que el anterior pero toma un screenshot de TODA la pantalla.
    // Útil cuando el texto está disperso por la pantalla.
    // ========================================================================
    public static String extractTextFromScreen(AndroidDriver driver) {
        try {
            File image = driver.getScreenshotAs(OutputType.FILE);
            return tesseract.doOCR(image).trim();
        } catch (TesseractException e) {
            throw new RuntimeException("Error OCR al procesar pantalla", e);
        }
    }

    // ========================================================================
    // MÉTODO: extractFullTextFromScreen (sin whitelist de números)
    // ========================================================================
    // Lee TODO el texto de la pantalla SIN restricción de caracteres.
    // Permite leer palabras como "Bienvenido", "Iniciar sesión", etc.
    //
    // NECESARIO porque el OCR configurado por defecto tiene whitelist
    // de solo números y símbolos monetarios. Este método crea una
    // instancia temporal sin esa restricción.
    //
    // USO:
    //   String texto = OCRUtils.extractFullTextFromScreen(driver);
    //   if (texto.contains("Bienvenido")) { ... }
    // ========================================================================
    public static String extractFullTextFromScreen(AndroidDriver driver) {
        try {
            Tesseract ocr = new Tesseract();
            ocr.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
            ocr.setLanguage("spa");
            // Sin whitelist: permite leer cualquier caracter
            File image = driver.getScreenshotAs(OutputType.FILE);
            String text = ocr.doOCR(image).trim();
            System.out.println("[OCR Full] Texto de pantalla: \"" + text + "\"");
            return text;
        } catch (TesseractException e) {
            throw new RuntimeException("Error OCR al procesar pantalla completa", e);
        }
    }

    // ========================================================================
    // MÉTODO: extractCurrencyAmount — CONVIERTE TEXTO A NÚMERO
    // ========================================================================
    // Este es el método más usado en los tests.
    // Toma un WebElement, lee su texto con OCR, y lo convierte a double.
    //
    // EJEMPLO:
    //   OCR lee: "$ 1,500,000.00"
    //   Limpia:  "1500000.00"  (quita $, comas, espacios)
    //   Devuelve: 1500000.00 (double)
    //
    // USO EN TESTS:
    //   double saldo = OCRUtils.extractCurrencyAmount(homePage.getBalanceElement());
    //   Assert.assertTrue(saldo > 0);
    // ========================================================================
    public static double extractCurrencyAmount(WebElement element) {
        // 1. Leer el texto con OCR
        String text = extractTextFromElement(element)
                .replace("$", "")       // Quitar el símbolo de pesos
                .replaceAll("\\s+", "") // Quitar espacios en blanco
                .trim();

        // Formato colombiano/europeo: punto=miles, coma=decimales
        // Si tiene COMA como decimal (1.050.000,50):
        //   - quitar todos los puntos (miles)
        //   - cambiar coma por punto (decimal)
        String normalized;
        if (text.contains(",") && text.lastIndexOf(",") > text.lastIndexOf(".")) {
            // Formato: 1.000.000,50 → 1000000.50
            normalized = text.replace(".", "").replace(",", ".");
        } else if (text.contains(".") && text.indexOf(".") == text.lastIndexOf(".")) {
            // Solo UN punto: puede ser decimal (1050.50) o miles (1050)
            // Si tiene menos de 3 dígitos después del punto, es decimal
            int lastDot = text.lastIndexOf(".");
            String afterDot = text.substring(lastDot + 1);
            if (afterDot.length() <= 2) {
                // Es decimal: 1050.50 → 1050.50
                normalized = text;
            } else {
                // Es separador de miles: 1.050 → 1050
                normalized = text.replace(".", "");
            }
        } else {
            // Solo números enteros
            normalized = text.replaceAll("[.,]", "");
        }

        // Si quedó vacío o no es un número válido, devolver 0
        if (normalized.isEmpty() || !normalized.matches(".*[0-9].*")) {
            return 0;
        }

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new RuntimeException("No se pudo parsear el precio OCR: '" + text + "' (normalizado: '" + normalized + "')", e);
        }
    }
}
