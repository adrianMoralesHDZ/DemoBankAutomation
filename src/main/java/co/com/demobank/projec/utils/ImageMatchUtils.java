package co.com.demobank.projec.utils;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;

/*
 * ============================================================================
 * UTIL: ImageMatchUtils
 * ============================================================================
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Es un "wrapper" de OpenCV (vía JavaCV) para comparar imágenes.
 * Compara dos imágenes píxel a píxel y devuelve un "score de similitud".
 *
 * -----------------------------------------------------------------------
 * ¿QUÉ ES OPENCV?
 * -----------------------------------------------------------------------
 * OpenCV es una librería de visión computacional.
 * Puede: detectar rostros, comparar imágenes, detectar movimiento, etc.
 * Aquí la usamos para: comparar un screenshot actual contra una imagen
 * base (baseline) y detectar regresiones visuales.
 *
 * -----------------------------------------------------------------------
 * ¿QUÉ ES UNA REGRESIÓN VISUAL?
 * -----------------------------------------------------------------------
 * Si la app se ve diferente a como se veía antes (baseline), es una
 * regresión visual. Puede ser:
 *   - Un botón cambió de color
 *   - Un texto cambió de tamaño
 *   - Un elemento se movió de posición
 *   - Un ícono cambió
 *
 * -----------------------------------------------------------------------
 * ¿CÓMO FUNCIONA LA COMPARACIÓN?
 * -----------------------------------------------------------------------
 *   1. Carga la imagen base (baseline) con imread
 *   2. Carga la imagen actual (screenshot del test)
 *   3. Si tienen tamaños diferentes, redimensiona la actual
 *   4. Calcula la diferencia absoluta píxel a píxel (absdiff)
 *   5. Convierte a números decimales (CV_32F)
 *   6. Calcula el promedio de diferencia
 *   7. Convierte a score: 1.0 - (diferencia / 255)
 *
 *   Score = 1.0 → imágenes idénticas
 *   Score = 0.95 → 95% similares (umbral mínimo aceptable)
 *   Score = 0.0 → imágenes completamente diferentes
 *
 * -----------------------------------------------------------------------
 * REQUISITO DEL PDF: Score >= 95%
 * -----------------------------------------------------------------------
 * La prueba pasa si el score es >= 0.95 (95%).
 * Esto permite pequeñas variaciones (anti-aliasing, rendering) sin fallar.
 *
 * -----------------------------------------------------------------------
 * ¿CÓMO PREPARAR EL BASELINE?
 * -----------------------------------------------------------------------
 * 1. Ejecuta la app manualmente hasta la pantalla que quieres validar
 * 2. Toma un screenshot y guárdalo en:
 *    src/test/resources/baselines/home_baseline.png
 * 3. El test comparará este baseline contra el screenshot en runtime
 *
 * Para tomar un screenshot con ADB:
 *   adb shell screencap -p /sdcard/screen.png
 *   adb pull /sdcard/screen.png home_baseline.png
 * ============================================================================
 */
public class ImageMatchUtils {

    // ========================================================================
    // UMBRAL DE SIMILITUD
    // ========================================================================
    // 0.95 = 95%. Si el score es mayor o igual, la prueba pasa.
    // El PDF exige este umbral.
    // ========================================================================
    private static final double MATCH_THRESHOLD = 0.95;

    // ========================================================================
    // MÉTODO: compareImages
    // ========================================================================
    // Compara dos imágenes y devuelve un score de 0.0 a 1.0.
    //
    // USO:
    //   double score = ImageMatchUtils.compareImages(
    //       "src/test/resources/baselines/home_baseline.png",  // base
    //       screenshot.getAbsolutePath()                        // actual
    //   );
    //   Assert.assertTrue(score >= 0.95);
    //
    // PARÁMETROS:
    //   baselinePath → ruta de la imagen de referencia (guardada previamente)
    //   actualPath   → ruta del screenshot tomado durante el test
    // ========================================================================
    public static double compareImages(String baselinePath, String actualPath) {
        // 1. Cargar imágenes con OpenCV (formato Mat = matriz de píxeles)
        Mat baseline = opencv_imgcodecs.imread(baselinePath);
        Mat actual   = opencv_imgcodecs.imread(actualPath);

        // 2. Validar que las imágenes se cargaron correctamente
        if (baseline.empty()) {
            throw new RuntimeException("Baseline no encontrada: " + baselinePath);
        }
        if (actual.empty()) {
            throw new RuntimeException("Imagen actual invalida: " + actualPath);
        }

        // 3. Si tienen tamaños diferentes, redimensionar la actual
        //    (necesario porque el emulador puede tener otra resolución)
        if (baseline.rows() != actual.rows() || baseline.cols() != actual.cols()) {
            opencv_imgproc.resize(actual, actual, baseline.size());
        }

        // 4. Calcular similitud
        return computeSimilarity(baseline, actual);
    }

    // ========================================================================
    // MÉTODO: computeSimilarity — privado
    // ========================================================================
    // Algoritmo de comparación:
    //   1. absdiff → diferencia absoluta entre cada píxel
    //   2. convertTo → convierte a decimales (CV_32F)
    //   3. mean → promedio de todas las diferencias
    //   4. score = 1.0 - (promedio / 255)
    //
    // Si todos los píxeles son iguales → diff = 0 → score = 1.0
    // Si todos los píxeles son opuestos → diff = 255 → score = 0.0
    // ========================================================================
    private static double computeSimilarity(Mat img1, Mat img2) {
        // Matriz para guardar la diferencia
        Mat diff = new Mat();
        // Diferencia absoluta: |img1 - img2| para cada píxel
        opencv_core.absdiff(img1, img2, diff);

        // Convertir a decimales (CV_32F = float32)
        Mat diffFloat = new Mat();
        diff.convertTo(diffFloat, CV_32F);

        // Calcular el promedio de la diferencia
        double meanDiff = opencv_core.mean(diffFloat).get(0);

        // Convertir a score (1.0 = idénticas, 0.0 = diferentes)
        return 1.0 - (meanDiff / 255.0);
    }

    // ========================================================================
    // MÉTODO: isMatch — verificar si el score supera el umbral
    // ========================================================================
    // Método de conveniencia para usar en los tests:
    //   boolean passed = ImageMatchUtils.isMatch(score);
    //   Assert.assertTrue(passed);
    // ========================================================================
    public static boolean isMatch(double score) {
        return score >= MATCH_THRESHOLD;
    }

    // ========================================================================
    // MÉTODO: findIconOnScreen — TEMPLATE MATCHING
    // ========================================================================
    // Busca un icono (template) dentro de un screenshot completo y devuelve
    // las coordenadas (x, y) del centro donde se encontro el mejor match.
    //
    // USO:
    //   File screenshot = driver.getScreenshotAs(OutputType.FILE);
    //   int[] coords = ImageMatchUtils.findIconOnScreen(
    //       screenshot.getAbsolutePath(),
    //       "src/test/resources/baselines/logout_icon.png");
    //   if (coords != null) {
    //       // coords[0] = x, coords[1] = y
    //       driver.executeScript("mobile: clickGesture",
    //           Map.of("x", coords[0], "y", coords[1]));
    //   }
    //
    // ALGORITMO:
    //   1. Carga el screenshot completo y el icono baseline
    //   2. Convierte ambos a escala de grises
    //   3. Aplica matchTemplate (TM_CCOEFF_NORMED)
    //   4. Busca el punto con mayor score
    //   5. Devuelve el centro del area matcheada
    //
    // RETORNO:
    //   int[2] = {x, y} del centro del icono encontrado, o null si no hay match
    // ========================================================================
    public static int[] findIconOnScreen(String screenPath, String templatePath) {
        // 1. Cargar imagenes
        Mat screen = opencv_imgcodecs.imread(screenPath);
        Mat template = opencv_imgcodecs.imread(templatePath);

        if (screen.empty()) {
            throw new RuntimeException("Screenshot no encontrado: " + screenPath);
        }
        if (template.empty()) {
            throw new RuntimeException("Template (icono baseline) no encontrado: " + templatePath);
        }

        // 2. Convertir a escala de grises (mejora el matching)
        Mat screenGray = new Mat();
        Mat templateGray = new Mat();
        opencv_imgproc.cvtColor(screen, screenGray, opencv_imgproc.COLOR_BGR2GRAY);
        opencv_imgproc.cvtColor(template, templateGray, opencv_imgproc.COLOR_BGR2GRAY);

        // 3. Template matching
        Mat result = new Mat();
        opencv_imgproc.matchTemplate(screenGray, templateGray, result,
                opencv_imgproc.TM_CCOEFF_NORMED);

        // 4. Encontrar el mejor match
        Point minLoc = new Point();
        Point maxLoc = new Point();
        double[] minVal = new double[1];
        double[] maxVal = new double[1];
        opencv_core.minMaxLoc(result, minVal, maxVal, minLoc, maxLoc, new Mat());

        double score = maxVal[0];
        System.out.println("[OpenCV] Template matching score: " + score);

        // 5. Umbral: 0.8 (80%) para considerar que el icono fue encontrado
        if (score < 0.8) {
            System.out.println("[OpenCV] No se encontro el icono (score < 0.8)");
            return null;
        }

        // 6. Calcular el centro del area matcheada
        int centerX = maxLoc.x() + template.cols() / 2;
        int centerY = maxLoc.y() + template.rows() / 2;
        System.out.println("[OpenCV] Icono encontrado en centro: (" + centerX + ", " + centerY + ")");

        return new int[] { centerX, centerY };
    }
}
