import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;

public class CropIcon {
    public static void main(String[] args) {
        // Cargar el screenshot completo del Home
        Mat full = opencv_imgcodecs.imread("D:\\reto\\DemoBankAutomation\\src\\test\\resources\\baselines\\home_full.png");
        if (full.empty()) {
            System.out.println("ERROR: No se pudo cargar home_full.png");
            return;
        }
        System.out.println("Imagen completa: " + full.cols() + "x" + full.rows());

        // Recortar la zona del icono de logout: [1051,44][1164,156]
        // Rect(x, y, width, height)
        int x = 1051;
        int y = 44;
        int width = 1164 - 1051;   // 113
        int height = 156 - 44;    // 112
        Rect roi = new Rect(x, y, width, height);
        Mat icon = new Mat(full, roi);

        // Guardar el icono recortado
        String outputPath = "D:\\reto\\DemoBankAutomation\\src\\test\\resources\\baselines\\logout_icon.png";
        opencv_imgcodecs.imwrite(outputPath, icon);
        System.out.println("Icono de logout guardado en: " + outputPath);
        System.out.println("Tamano del icono: " + icon.cols() + "x" + icon.rows());
    }
}
