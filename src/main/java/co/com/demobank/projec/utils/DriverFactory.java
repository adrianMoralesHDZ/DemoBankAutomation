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
 * Configura las capabilities de UiAutomator2 para la app DemoBank
 * y proporciona acceso singleton al driver y al WebDriverWait.
 */
public class DriverFactory {

    private static AndroidDriver driver;
    private static WebDriverWait wait;

    private static final String SERVER_URL = "http://127.0.0.1:4723/";
    private static final String DEVICE_NAME = "ZY22KVZPQ4";
    private static final String APP_PACKAGE = "com.demobank.app";
    private static final String APP_ACTIVITY = "com.demobank.app.MainActivity";

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
     * Crea el AndroidDriver con las capabilities de DemoBank.
     *
     * @return nueva instancia de AndroidDriver
     */
    private static AndroidDriver createDriver() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(DEVICE_NAME)
                .setAutomationName("UiAutomator2")
                .setAppPackage(APP_PACKAGE)
                .setAppActivity(APP_ACTIVITY)
                .setNoReset(false)
                .setAppWaitActivity(APP_ACTIVITY)
                .setAppWaitPackage(APP_PACKAGE)
                .setAppWaitDuration(Duration.ofSeconds(30))
                .autoGrantPermissions();

        try {
            return new AndroidDriver(new URL(SERVER_URL), options);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL Appium invalida: " + SERVER_URL, e);
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
