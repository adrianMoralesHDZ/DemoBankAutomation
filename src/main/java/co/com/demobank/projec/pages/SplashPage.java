package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/*
 * ============================================================================
 * PAGE OBJECT: SplashPage
 * ============================================================================
 *
 * REPRESENTA: La pantalla de Splash inicial de DemoBank.
 *
 * FLUJO:
 *   Splash → Login (después de la animación de bienvenida)
 *
 * Esta pantalla típicamente muestra el logo/nombre de la app durante 2-3 segundos.
 * No tiene elementos interactivos, solo hay que esperar a que desaparezca.
 *
 * NOTA SOBRE LOCALIZADORES:
 * Los IDs se obtuvieron con: adb shell uiautomator dump
 * mientras la app DemoBank estaba abierta en cada pantalla.
 * ============================================================================
 */
public class SplashPage {

    private final AndroidDriver driver;

    // ========================================================================
    // LOCALIZADORES
    // ========================================================================
    // Aquí van los IDs de los elementos visibles en Splash.
    // ⚠️ COMPLETAR con Appium Inspector o adb dump cuando tengas la APK.
    // ========================================================================

    // Texto del logo/nombre de la app (ej: "DemoBank")
    private final By logoName =
            By.id("com.demobank.app:id/txt_logo_name");

    // Indicador de carga mientras la app inicia
    private final By loadingIndicator =
            By.id("com.demobank.app:id/progress_loading");

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================
    public SplashPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // ========================================================================
    // ACCIONES
    // ========================================================================

    /**
     * Espera a que el splash desaparezca y redirija automaticamente
     * a la pantalla de Login.
     */
    public void waitForSplashToFinish() {
        // Esperar a que el indicador de carga desaparezca
        WaitUtils.waitForInvisibility(loadingIndicator);
    }

    /**
     * Verifica si estamos en la pantalla de Splash.
     */
    public boolean isOnSplashScreen() {
        try {
            return driver.findElement(logoName).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
