package co.com.demobank.projec.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/**
 * Factory centralizada para la creacion y gestion del AndroidDriver.
 * <p>
 * Las capabilities se leen desde {@code config.properties} via
 * {@link ConfigReader}. Para cambiar el dispositivo, URL de Appium
 * o datos de la app, edita {@code src/main/resources/config.properties}.
 */
public class DriverFactory {

    private static AndroidDriver driver;
    private static WebDriverWait wait;

    /**
     * Obtiene la instancia singleton del driver. Si no existe, la crea.
     *
     * @return instancia de AndroidDriver
     */
    public static AndroidDriver getDriver() {
        if (driver == null) {
            driver = createDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        }
        return driver;
    }

    /**
     * Obtiene el WebDriverWait asociado al driver actual.
     *
     * @return instancia de WebDriverWait con timeout de 20 segundos
     */
    public static WebDriverWait getWait() {
        getDriver();
        return wait;
    }

    /**
     * Crea el AndroidDriver con las capabilities leidas de config.properties.
     *
     * @return nueva instancia de AndroidDriver
     */
    private static AndroidDriver createDriver() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(ConfigReader.getDeviceName())
                .setAutomationName("UiAutomator2")
                .setAppPackage(ConfigReader.getAppPackage())
                .setAppActivity(ConfigReader.getAppActivity())
                .setNoReset(false)
                .setAppWaitActivity(ConfigReader.getAppActivity())
                .setAppWaitPackage(ConfigReader.getAppPackage())
                .setAppWaitDuration(Duration.ofSeconds(30))
                .autoGrantPermissions();

        try {
            return new AndroidDriver(new URL(ConfigReader.getAppiumServerUrl()), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL Appium invalida: "
                    + ConfigReader.getAppiumServerUrl(), e);
        }
    }

    /**
     * Cierra el driver actual y libera los recursos.
     */
    public static void quitDriver() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
            driver = null;
            wait = null;
        }
    }
}
