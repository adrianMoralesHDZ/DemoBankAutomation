package co.com.demobank.projec.utils;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

/*
 * ============================================================================
 * UTIL: DriverFactory
 * ============================================================================
 *
 * Configuración actual: DemoBank
 * (https://github.com/.../DemoBank - APK mockeada standalone)
 *
 * CAPABILITIES:
 *   - platformName: Android
 *   - deviceName: ZY22KVZPQ4 (Motorola Edge 50 Pro)
 *   - platformVersion: 16 (Android 16)
 *   - appPackage: com.demobank.app
 *   - appActivity: com.demobank.app.MainActivity
 *   - automationName: UiAutomator2
 *   - autoGrantPermissions: true
 *
 * CREDENCIALES DE PRUEBA (del PDF):
 *   - Email: demo@demo.com
 *   - Password: 1234
 *
 * CUENTAS MOCKEADAS (del PDF):
 *   - Cuenta Corriente: $1,500,000.00
 *   - Cuenta Ahorros: $955,450.00
 *   - Saldo consolidado: $2,455,450.00
 * ============================================================================
 */
public class DriverFactory {

    private static AndroidDriver driver;
    private static WebDriverWait wait;

    private static final String SERVER_URL = "http://127.0.0.1:4723/";
    private static final String DEVICE_NAME = "ZY22KVZPQ4";
    private static final String APP_PACKAGE = "com.demobank.app";
    private static final String APP_ACTIVITY = "com.demobank.app.MainActivity";

    public static AndroidDriver getDriver() {
        if (driver == null) {
            driver = createDriver();
            wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        }
        return driver;
    }

    public static WebDriverWait getWait() {
        getDriver();
        return wait;
    }

    private static AndroidDriver createDriver() {
        UiAutomator2Options options = new UiAutomator2Options()
                .setDeviceName(DEVICE_NAME)
                .setAutomationName("UiAutomator2")
                .setAppPackage(APP_PACKAGE)
                .setAppActivity(APP_ACTIVITY)
                .setNoReset(false)
                // Esperar a que la app se abra antes de continuar
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

    public static void quitDriver() {
        if (driver != null) {
            try { driver.quit(); } catch (Exception ignored) {}
            driver = null;
            wait = null;
        }
    }
}
