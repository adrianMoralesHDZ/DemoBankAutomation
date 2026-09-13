package co.com.demobank.projec.utils;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;

/**
 * Utilidad de comparacion visual mediante OpenCV (JavaCV).
 * <p>
 * Compara un screenshot actual contra una imagen baseline y devuelve
 * un score de similitud. El umbral de aprobacion es 0.95 (95%).
 */
public class ImageMatchUtils {

    private static final double MATCH_THRESHOLD = 0.95;

    /**
     * Compara dos imagenes y devuelve un score de similitud entre 0.0 y 1.0.
     * <p>
     * Si las imagenes tienen dimensiones diferentes, la actual se
     * redimensiona al tamano de la baseline.
     *
     * @param baselinePath ruta de la imagen de referencia
     * @param actualPath   ruta del screenshot tomado durante el test
     * @return score de similitud (1.0 = identicas, 0.0 = diferentes)
     */
    public static double compareImages(String baselinePath, String actualPath) {
        Mat baseline = opencv_imgcodecs.imread(baselinePath);
        Mat actual = opencv_imgcodecs.imread(actualPath);

        if (baseline.empty()) {
            throw new RuntimeException("Baseline no encontrada: " + baselinePath);
        }
        if (actual.empty()) {
            throw new RuntimeException("Imagen actual invalida: " + actualPath);
        }

        if (baseline.rows() != actual.rows() || baseline.cols() != actual.cols()) {
            opencv_imgproc.resize(actual, actual, baseline.size());
        }

        return computeSimilarity(baseline, actual);
    }

    /**
     * Algoritmo interno: calcula la diferencia absoluta pixel a pixel
     * y la convierte en un score de similitud.
     *
     * @param img1 primera imagen (Mat de OpenCV)
     * @param img2 segunda imagen (Mat de OpenCV)
     * @return score entre 0.0 y 1.0
     */
    private static double computeSimilarity(Mat img1, Mat img2) {
        Mat diff = new Mat();
        opencv_core.absdiff(img1, img2, diff);

        Mat diffFloat = new Mat();
        diff.convertTo(diffFloat, CV_32F);

        double meanDiff = opencv_core.mean(diffFloat).get(0);
        return 1.0 - (meanDiff / 255.0);
    }
}
