package co.com.demobank.projec.utils;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;

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
}
