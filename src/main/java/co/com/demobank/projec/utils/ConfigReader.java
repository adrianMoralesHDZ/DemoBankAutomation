package co.com.demobank.projec.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lector centralizado de configuracion desde {@code config.properties}.
 * <p>
 * Carga el archivo una sola vez al iniciar y expone metodos estaticos
 * para obtener cada valor. Si una propiedad del sistema overridea
 * el valor del archivo, el del sistema tiene prioridad.
 * <p>
 * Archivo: {@code src/main/resources/config.properties}
 */
public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException(
                        "No se encontro config.properties en src/main/resources/");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer config.properties", e);
        }
    }

    // ========================================================================
    // Metodos publicos
    // ========================================================================

    /**
     * Obtiene un valor String del archivo de configuracion.
     * Si existe una propiedad del sistema con el mismo nombre, esta tiene prioridad.
     *
     * @param key          clave de la propiedad
     * @param defaultValue valor por defecto si la clave no existe
     * @return valor de la propiedad
     */
    public static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Obtiene un valor String del archivo de configuracion.
     *
     * @param key clave de la propiedad
     * @return valor de la propiedad o null si no existe
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * Obtiene un valor numerico (double) del archivo de configuracion.
     *
     * @param key          clave de la propiedad
     * @param defaultValue valor por defecto si la clave no existe o no es numerico
     * @return valor numerico de la propiedad
     */
    public static double getDouble(String key, double defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Obtiene un valor booleano del archivo de configuracion.
     *
     * @param key          clave de la propiedad
     * @param defaultValue valor por defecto si la clave no existe
     * @return valor booleano de la propiedad
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim());
    }

    // ========================================================================
    // Accesos directos (constants de claves para evitar typos)
    // ========================================================================

    public static String getAppiumServerUrl() {
        return get("appium.server.url", "http://127.0.0.1:4723/");
    }

    public static String getDeviceName() {
        return get("device.name");
    }

    public static String getAppPackage() {
        return get("app.package");
    }

    public static String getAppActivity() {
        return get("app.activity");
    }

    public static String getEmail() {
        return get("credentials.email");
    }

    public static String getPassword() {
        return get("credentials.password");
    }

    public static String getTessdataPath() {
        return get("tesseract.datapath", "C:\\Program Files\\Tesseract-OCR\\tessdata");
    }

    public static String getTessLanguage() {
        return get("tesseract.language", "spa");
    }

    public static boolean captureEveryStep() {
        return getBoolean("allure.screenshots.everyStep", true);
    }
}
