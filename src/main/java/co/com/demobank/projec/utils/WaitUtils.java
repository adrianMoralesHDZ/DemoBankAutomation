package co.com.demobank.projec.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.Arrays;

/*
 * ============================================================================
 * UTIL: WaitUtils
 * ============================================================================
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Centraliza todas las esperas explícitas del framework.
 * Es un "wrapper" (envoltorio) alrededor de WebDriverWait y FluentWait.
 *
 * -----------------------------------------------------------------------
 * REGLA DE ORO DEL PDF: NO USAR Thread.sleep()
 * -----------------------------------------------------------------------
 * Thread.sleep(5000) es una mala práctica porque:
 *   - Espera SIEMPRE 5 segundos, sin importar si el elemento ya apareció
 *   - Hace los tests lentos
 *   - Causa flakiness (a veces 5s es suficiente, a veces no)
 *
 * En su lugar, usamos ESPERAS EXPLÍCITAS:
 *   - WebDriverWait: espera hasta que una condición se cumpla (máx 20s)
 *   - FluentWait: igual pero con configuración más fina (polling, excepciones)
 *
 * -----------------------------------------------------------------------
 * TIPOS DE ESPERA QUE OFRECE ESTA CLASE:
 * -----------------------------------------------------------------------
 *   1. waitForVisibility  → espera a que un elemento sea VISIBLE
 *   2. waitForClickable   → espera a que un elemento sea CLICKEABLE
 *   3. waitForInvisibility→ espera a que un elemento DESAPAREZCA
 *   4. fluentWait          → espera con reintentos (resiliencia React Native)
 *   5. safeClick           → click con reintentos automáticos
 *   6. safeSendKeys        → escribir texto de forma segura
 *
 * -----------------------------------------------------------------------
 * ¿POR QUÉ FLUENT WAIT PARA REACT NATATIVE?
 * -----------------------------------------------------------------------
 * React Native usa requestAnimationFrame que es asíncrono.
 * Esto significa que los elementos pueden aparecer y desaparecer
 * rápidamente entre frames (StaleElementReferenceException).
 *
 * FluentWait maneja esto:
 *   - Ignora StaleElementReferenceException y NoSuchElementException
 *   - Reintenta cada 500ms
 *   - Hasta un máximo de 20 segundos
 * ============================================================================
 */
public class WaitUtils {

    // ========================================================================
    // CONFIGURACIÓN DE TIEMPOS
    // ========================================================================
    // Timeout máximo: cuánto esperar antes de fallar (20 segundos)
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(20);
    // Frecuencia de polling: cada cuánto reintentar (500 milisegundos)
    private static final Duration POLLING = Duration.ofMillis(500);

    // ========================================================================
    // ESPERA SIMPLE: elemento visible
    // ========================================================================
    // waitForVisibility(By.id("com.app:id/boton"))
    //   → Espera hasta que el elemento sea visible en pantalla.
    //   → Si aparece en 1 segundo, continúa en 1 segundo (no espera 20).
    //   → Si no aparece en 20 segundos, lanza TimeoutException.
    // ========================================================================
    public static WebElement waitForVisibility(By locator) {
        return DriverFactory.getWait()
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // ========================================================================
    // ESPERA SIMPLE: elemento clickeable
    // ========================================================================
    // Igual que waitForVisibility pero espera a que el elemento esté
    // HABILITADO y CLICKEABLE (no solo visible).
    // Útil para botones que pueden estar disabled temporalmente.
    // ========================================================================
    public static WebElement waitForClickable(By locator) {
        return DriverFactory.getWait()
                .until(ExpectedConditions.elementToBeClickable(locator));
    }

    // ========================================================================
    // ESPERA SIMPLE: elemento invisible
    // ========================================================================
    // Espera a que un elemento DESAPAREZCA de la pantalla.
    // Útil para:
    //   - Esperar que termine un loading spinner
    //   - Esperar que se cierre un modal
    //   - Esperar que desaparezca un toast/banner
    // ========================================================================
    public static void waitForInvisibility(By locator) {
        DriverFactory.getWait()
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // ========================================================================
    // ESPERA FLUIDA: con manejo de excepciones (RESILIENCIA)
    // ========================================================================
    // Esta es la espera más robusta. Combina:
    //   - Timeout de 20 segundos
    //   - Polling cada 500ms
    //   - Ignora StaleElementReferenceException (elemento stale)
    //   - Ignora NoSuchElementException (elemento no encontrado)
    //   - Solo retorna si el elemento está visible Y habilitado
    //
    // ¿CUÁNDO USARLA?
    //   - Cuando la app es React Native (animaciones asíncronas)
    //   - Cuando un elemento aparece/desaparece rápidamente
    //   - Cuando un click falla por el elemento estar en "stale" state
    //
    // El lambda (d -> {...}) significa:
    //   "Para cada driver (d), intenta encontrar el elemento.
    //    Si está visible y habilitado, devuélvelo.
    //    Si no, devuelve null y FluentWait reintenta en 500ms."
    // ========================================================================
    public static WebElement fluentWait(By locator) {
        return new FluentWait<>(DriverFactory.getDriver())
                .withTimeout(DEFAULT_TIMEOUT)           // Máximo 20 segundos
                .pollingEvery(POLLING)                  // Reintentar cada 500ms
                .ignoreAll(Arrays.asList(              // Ignorar estas excepciones
                        StaleElementReferenceException.class,
                        NoSuchElementException.class))
                .until(d -> {
                    // Buscar el elemento
                    WebElement el = d.findElement(locator);
                    // Solo devolverlo si está visible Y habilitado
                    return (el.isDisplayed() && el.isEnabled()) ? el : null;
                });
    }

    // ========================================================================
    // CLICK SEGURO: con reintentos automáticos
    // ========================================================================
    // safeClick(By.id("com.app:id/boton"))
    //   → Usa fluentWait para encontrar el elemento
    //   → Intenta hacer click
    //   → Si falla por StaleElementReferenceException, reintenta (hasta 3 veces)
    //
    // ¿POR QUÉ REINTENTOS?
    //   En React Native, entre el momento en que encontramos el elemento
    //   y el momento en que hacemos click, el elemento puede haber cambiado
    //   de estado (re-renderizado). El reintento soluciona esto.
    // ========================================================================
    public static void safeClick(By locator) {
        int intentos = 3;
        while (intentos-- > 0) {
            try {
                WebElement el = fluentWait(locator);
                el.click();
                return;  // Si el click funciona, salimos del método
            } catch (StaleElementReferenceException e) {
                // Si el elemento estaba stale, reintentamos
                if (intentos == 0) throw e;  // Si era el último intento, lanzamos
            }
        }
    }

    // ========================================================================
    // ESCRITURA SEGURA: clear + sendKeys
    // ========================================================================
    // safeSendKeys(By.id("com.app:id/input"), "texto a escribir")
    //   → Espera a que el elemento sea visible
    //   → Limpia cualquier texto previo
    //   → Escribe el texto nuevo
    // ========================================================================
    public static void safeSendKeys(By locator, String text) {
        WebElement el = waitForVisibility(locator);
        el.clear();
        el.sendKeys(text);
    }
}
