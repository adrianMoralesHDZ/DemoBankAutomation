package co.com.demobank.projec.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.Arrays;

/**
 * Centraliza todas las esperas explicitas del framework.
 * <p>
 * Provee metodos estaticos para esperar condiciones de UI sin utilizar
 * Thread.sleep(). Usa WebDriverWait y FluentWait con manejo de excepciones
 * para mitigar el flakiness propio de React Native.
 */
public class WaitUtils {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    private static final Duration POLLING = Duration.ofMillis(500);

    /**
     * Espera hasta que un elemento sea visible en pantalla.
     *
     * @param locator localizador By del elemento
     * @return el WebElement visible
     */
    public static WebElement waitForVisibility(By locator) {
        return DriverFactory.getWait()
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Espera hasta que un elemento desaparezca de la pantalla.
     *
     * @param locator localizador By del elemento
     */
    public static void waitForInvisibility(By locator) {
        DriverFactory.getWait()
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Espera con FluentWait: ignora StaleElementReferenceException y
     * NoSuchElementException, reintentando cada 500ms hasta 20 segundos.
     * <p>
     * Util en pantallas React Native donde los elementos pueden aparecer
     * y desaparecer entre frames.
     *
     * @param locator localizador By del elemento
     * @return el WebElement visible y habilitado
     */
    public static WebElement fluentWait(By locator) {
        return new FluentWait<>(DriverFactory.getDriver())
                .withTimeout(DEFAULT_TIMEOUT)
                .pollingEvery(POLLING)
                .ignoreAll(Arrays.asList(
                        StaleElementReferenceException.class,
                        NoSuchElementException.class))
                .until(d -> {
                    WebElement el = d.findElement(locator);
                    return (el.isDisplayed() && el.isEnabled()) ? el : null;
                });
    }

    /**
     * Click con reintentos automaticos (hasta 3 intentos).
     * Usa FluentWait para localizar el elemento y reintenta si ocurre
     * StaleElementReferenceException.
     *
     * @param locator localizador By del elemento a clickear
     */
    public static void safeClick(By locator) {
        int intentos = 3;
        while (intentos-- > 0) {
            try {
                WebElement el = fluentWait(locator);
                el.click();
                return;
            } catch (StaleElementReferenceException e) {
                if (intentos == 0) throw e;
            }
        }
    }
}
