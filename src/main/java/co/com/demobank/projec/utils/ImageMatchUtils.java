package co.com.demobank.projec.utils;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;

/**
 * Utilidad de comparacion visual mediante OpenCV (JavaCV).
 * <p>
 * Compara un screenshot actual contra una imagen baseline y devuelve
 * un score de similitud. El umbral de aprobacion es 0.95 (95%).
 * <p>
 * Si la baseline no existe, se captura automaticamente desde la
 * ejecucion actual y se guarda para futuras comparaciones.
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
     * Toma un screenshot del driver actual. Si la baseline existe, la compara
     * mediante OpenCV y valida que el Match Score >= 95%. Si la baseline no
     * existe, la captura y la guarda para futuras comparaciones.
     * <p>
     * Reporta el resultado en Allure mediante AllureHelper.
     *
     * @param baselineDir  directorio donde estan las baselines
     * @param baselineName nombre del archivo baseline (ej: "01_home.png")
     * @param stepName     descripcion del paso para el reporte
     */
    public static void assertScreenMatches(String baselineDir, String baselineName, String stepName) {
        File screenshot = ((TakesScreenshot) DriverFactory.getDriver())
                .getScreenshotAs(OutputType.FILE);

        String baselinePath = baselineDir + baselineName;
        File baselineFile = new File(baselinePath);

        if (!baselineFile.exists()) {
            captureBaseline(screenshot, baselineFile, stepName);
            return;
        }

        double score = compareImages(baselinePath, screenshot.getAbsolutePath());
        boolean passed = score >= MATCH_THRESHOLD;

        AllureHelper.reportValidation(
                "OpenCV - " + stepName,
                String.format("Match Score: %.2f%%", score * 100),
                "Score >= 95.00%",
                passed,
                "Baseline: " + baselinePath + " | Si el score es < 95%, hay una "
                        + "regresion visual en esta pantalla del flujo.");
        Assert.assertTrue(passed,
                "Regresion visual en '" + stepName + "': Score " + score + " < 0.95 (95%)");
    }

    /**
     * Guarda el screenshot actual como baseline para futuras comparaciones.
     *
     * @param screenshot   screenshot capturado por el driver
     * @param baselineFile archivo destino donde se guarda la baseline
     * @param stepName      descripcion del paso para el reporte
     */
    private static void captureBaseline(File screenshot, File baselineFile, String stepName) {
        try {
            File dir = baselineFile.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            Files.copy(screenshot.toPath(), baselineFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            AllureHelper.reportValidation(
                    "OpenCV - " + stepName,
                    "Baseline capturada automaticamente",
                    "Score >= 95.00% (en futuras ejecuciones)",
                    true,
                    "La baseline no existia. Se capturo el screenshot actual "
                            + "y se guardo en " + baselineFile.getPath() + ". "
                            + "En la proxima ejecucion se comparara contra esta imagen.");
        } catch (IOException e) {
            Assert.fail("No se pudo guardar la baseline en " + baselineFile.getPath()
                    + ": " + e.getMessage());
        }
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
