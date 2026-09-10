package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.AllureHelper;
import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * ============================================================================
 * PAGE OBJECT: TransferPage (DemoBank) - Modal de 3 pasos
 * ============================================================================
 *
 * Flujo:
 *   Paso 1: Seleccionar contacto
 *   Paso 2: Ingresar monto (con selector de cuenta origen)
 *   Paso 3: Confirmar transferencia
 *
 * Localizadores basados en adb shell uiautomator dump:
 *   - Sin resource-id, usa content-desc y text
 * ============================================================================
 */
public class TransferPage {

    private final AndroidDriver driver;

    // ========================================================================
    // PASO 1: SELECCIONAR CONTACTO
    // ========================================================================

    private final By contactsTitle =
            By.xpath("//*[@text and contains(@text,'transfieres')]");

    private final By mariaContact =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'MarÃ­a')]");
    private final By carlosContact =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Carlos')]");
    private final By sofiaContact =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Sof')]");
    private final By diegoContact =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Diego')]");

    private final By contactsList =
            By.xpath("//*[@class='android.widget.ScrollView']//android.view.ViewGroup[@clickable='true']");

    // ========================================================================
    // PASO 2: INGRESAR MONTO
    // ========================================================================

    // EditText del monto (es el primero EditText con texto "0.00")
    private final By amountField =
            By.xpath("//android.widget.EditText[@text='0.00' or @text='0' or contains(@text,'.')][1]");

    // Cualquier EditText que sea el campo de monto (primer EditText)
    private final By amountFieldAlt =
            By.xpath("//android.widget.EditText[1]");

    // EditText de nota (es el segundo EditText)
    private final By noteField =
            By.xpath("//android.widget.EditText[@text='Ej. Pago de renta']");

    // Selector cuenta origen - "Cuenta Corriente, $1500000.00"
    private final By cuentaCorrienteOption =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Cuenta Corriente')]");

    private final By cuentaAhorrosOption =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Cuenta Ahorros')]");

    // Boton Continuar (ViewGroup con content-desc exacto)
    private final By continueButton =
            By.xpath("//android.view.ViewGroup[@content-desc='Continuar']");

    // Boton Continuar (TextView con texto "Continuar") - fallback
    private final By continueTextView =
            By.xpath("//android.widget.TextView[@text='Continuar']");

    // ========================================================================
    // PASO 3: CONFIRMAR
    // ========================================================================

    // Boton "Confirmar transferencia" (ViewGroup con content-desc)
    private final By confirmButton =
            By.xpath("//android.view.ViewGroup[@content-desc='Confirmar transferencia']");

    // Boton "Confirmar transferencia" (TextView por texto)
    private final By confirmButtonAlt =
            By.xpath("//android.widget.TextView[@text='Confirmar transferencia']");

    // Cualquier elemento clickable que contenga "Confirmar"
    private final By confirmButtonFallBack =
            By.xpath("//android.view.ViewGroup[contains(@content-desc,'Confirmar')]");

    // Errores
    private final By insufficientBalanceError =
            By.xpath("//*[contains(@text,'Saldo insuficiente') or contains(@text,'insuficiente')]");

    // ========================================================================
    public TransferPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // ========================================================================
    // ACCIONES
    // ========================================================================

    // === PASO 1: Contacto ===
    public void selectFirstContact() {
        try {
            // 1. Intentar por content-desc con "Maria"
            WebElement btn = driver.findElement(mariaContact);
            btn.click();
            System.out.println("[OK] Paso 1: Contacto Maria Lopez seleccionado (por content-desc)");
        } catch (Exception e1) {
            try {
                // 2. Fallback: por texto del primer TextView "ML" que es el avatar
                WebElement btn = driver.findElement(
                        By.xpath("(//android.widget.TextView[@text='ML'])[1]")
                );
                btn.click();
                System.out.println("[OK] Paso 1: Contacto seleccionado (por TextView 'ML')");
            } catch (Exception e2) {
                // 3. Por bounds: el primer contacto esta en y=159 (del dump)
                //    tap directo por coordenadas
                try {
                    org.openqa.selenium.JavascriptExecutor js =
                            (org.openqa.selenium.JavascriptExecutor) driver;
                    java.util.Map<String, Object> args = new java.util.HashMap<>();
                    args.put("x", 610);
                    args.put("y", 254);   // centro vertical del primer contacto
                    js.executeScript("mobile: tap", args);
                    System.out.println("[OK] Paso 1: tap directo por coordenadas (610, 254)");
                } catch (Exception e3) {
                    System.out.println("[FAIL] selectFirstContact: " + e3.getMessage());
                    throw e3;
                }
            }
        }
    }

    public void selectContactByName(String name) {
        By by = By.xpath("//android.view.ViewGroup[contains(@content-desc,'" + name + "')]");
        WaitUtils.safeClick(by);
    }

    // === PASO 2: Monto ===
    public void typeAmount(String amount) {
        // 1. Tap en el campo monto
        WebElement field = driver.findElement(amountFieldAlt);
        field.click();

        // 2. Escribir el monto directo (sin BACKSPACE ni field.clear().
        //    field.clear() en DemoBank concatena con "0.00" existente.
        //    Solo tap + sendKeys ya funciona correctamente)
        field.sendKeys(amount);

        // 3. Verificar lo escrito
        String valorEscrito = field.getText();
        System.out.println("Paso 2: Monto escrito: " + amount + " (campo dice: '" + valorEscrito + "')");

        // 4. CRITICO: Ocultar teclado virtual antes de tap Continuar
        //    Sin esto el teclado TAPA el botÃ³n Continuar
        hideKeyboard();

        if (!valorEscrito.contains(amount)) {
            System.out.println("[WARN] El campo no contiene el monto esperado!");
        }
    }

    /**
     * Oculta el teclado virtual de Android.
     * Espera EXPLÃCITA: el teclado desaparece.
     */
    private void hideKeyboard() {
        try {
            driver.hideKeyboard();
            org.openqa.selenium.support.ui.WebDriverWait wait =
                    new org.openqa.selenium.support.ui.WebDriverWait(driver,
                            java.time.Duration.ofSeconds(5));
            wait.until(d -> !driver.isKeyboardShown());
            System.out.println("[OK] Teclado ocultado");
        } catch (Exception e) {
            System.out.println("[WARN] No se pudo ocultar teclado");
        }
    }

    public void selectSourceAccount(String accountShortName) {
        if (accountShortName.toLowerCase().contains("ahorro")) {
            WaitUtils.safeClick(cuentaAhorrosOption);
        } else {
            WaitUtils.safeClick(cuentaCorrienteOption);
        }
        System.out.println("Paso 2: Cuenta origen: " + accountShortName);
    }

    /**
     * Tap en "Continuar" usando el localizador basado en texto (no coord).
     * Hace scroll hacia abajo si el boton no esta visible.
     */
    public void tapContinue() {
        try {
            // 1. Buscar por content-desc (mas confiable)
            WebElement btn = driver.findElement(continueButton);
            btn.click();
            System.out.println("[OK] tapContinue: click via content-desc");
        } catch (Exception e1) {
            try {
                // 2. Buscar por TextView con texto "Continuar"
                WebElement btn = driver.findElement(continueTextView);
                btn.click();
                System.out.println("[OK] tapContinue: click via TextView");
            } catch (Exception e2) {
                try {
                    // 3. Si no esta visible, hacer scroll y reintentar
                    System.out.println("[INFO] tapContinue: scroll + retry");
                    scrollDown();
                    // Espera EXPLICITA: el boton Continuar debe aparecer de nuevo
                    // despues del scroll. Sin Thread.sleep().
                    org.openqa.selenium.support.ui.WebDriverWait wait =
                            new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(10));
                    wait.until(d -> {
                        try {
                            return d.findElement(continueButton).isDisplayed()
                                    || d.findElement(continueTextView).isDisplayed();
                        } catch (Exception e) {
                            return false;
                        }
                    });
                    WebElement btn = driver.findElement(continueTextView);
                    btn.click();
                    System.out.println("[OK] tapContinue: scroll + click");
                } catch (Exception e3) {
                    System.out.println("[FAIL] tapContinue: tampoco despues de scroll");
                    throw e3;
                }
            }
        }
    }

    /**
     * Scroll hacia abajo en la pantalla actual.
     */
    private void scrollDown() {
        try {
            org.openqa.selenium.JavascriptExecutor js =
                    (org.openqa.selenium.JavascriptExecutor) driver;
            Map<String, Object> args = new HashMap<>();
            args.put("startX", 610);
            args.put("startY", 1500);
            args.put("endX", 610);
            args.put("endY", 500);
            args.put("duration", 300);
            js.executeScript("mobile: dragGesture", args);
            // SIN Thread.sleep(): esperamos a que la animacion del scroll termine
            // usando condicion explicita: el ScrollView debe estar scrolleable de nuevo
            waitForScrollAnimation();
        } catch (Exception e) {
            System.out.println("scrollDown fallo: " + e.getMessage());
        }
    }

    /**
     * Espera explicita: la animacion del scroll termina cuando el ScrollView
     * estÃ¡ listo para nueva interaccion. Esto evita el Thread.sleep().
     * Usa WebDriverWait con ExpectedConditions en lugar de tiempo fijo.
     */
    private void waitForScrollAnimation() {
        try {
            org.openqa.selenium.support.ui.WebDriverWait wait =
                    new org.openqa.selenium.support.ui.WebDriverWait(driver, java.time.Duration.ofSeconds(5));
            wait.pollingEvery(java.time.Duration.ofMillis(200));
            wait.until(d -> {
                // Verificar que el ScrollView acepta interaccion
                return !d.findElement(org.openqa.selenium.By.xpath("//android.widget.ScrollView"))
                        .getAttribute("scrollable").equals("false");
            });
            System.out.println("[OK] Scroll animation finished");
        } catch (Exception e) {
            // Si falla la condicion, ignorar (es solo para estabilizar la animacion)
            System.out.println("[INFO] waitForScrollAnimation: " + e.getMessage());
        }
    }

// === PASO 3: Confirmar ===
    public void tapConfirm() {
        try {
            WebElement btn = driver.findElement(confirmButton);
            btn.click();
            System.out.println("[OK] tapConfirm: click via content-desc");
        } catch (Exception e1) {
            try {
                WebElement btn = driver.findElement(confirmButtonAlt);
                btn.click();
                System.out.println("[OK] tapConfirm: click via TextView");
            } catch (Exception e2) {
                try {
                    // 3. Tap directo por coordenadas (centro del boton: 610, 793)
                    org.openqa.selenium.JavascriptExecutor js =
                            (org.openqa.selenium.JavascriptExecutor) driver;
                    java.util.Map<String, Object> args = new java.util.HashMap<>();
                    args.put("x", 610);
                    args.put("y", 793);  // coords exactas del dump
                    js.executeScript("mobile: tap", args);
                    System.out.println("[OK] tapConfirm: tap por coordenadas (610, 793)");
                } catch (Exception e3) {
                    System.out.println("[FAIL] tapConfirm: todos los intentos fallaron");
                    throw e3;
                }
            }
        }
    }

    /**
     * Flujo completo: contacto, monto, confirmar.
     */
    public void completeTransfer(String monto) {
        AllureHelper.screenshot("[Antes] Modal Transferir abierto (Paso 1)");
        selectFirstContact();
        AllureHelper.screenshot("[Paso 1 OK] Contacto seleccionado");

        AllureHelper.screenshot("[Paso 2] Pantalla de monto");
        typeAmount(monto);
        AllureHelper.screenshot("[Paso 2 OK] Monto escrito: $" + monto);

        tapContinue();
        AllureHelper.screenshot("[Paso 3] Pantalla de confirmacion");

        tapConfirm();
        AllureHelper.screenshot("[Paso 3 OK] Confirmar presionado - esperando exito");
    }

    // ========================================================================
    // MÃ‰TODOS DE ESTADO
    // ========================================================================

    public boolean isOnContactScreen() {
        try {
            return driver.findElement(contactsTitle).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnAmountScreen() {
        try {
            return driver.findElement(amountFieldAlt).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isInsufficientBalanceErrorDisplayed() {
        try {
            return driver.findElement(insufficientBalanceError).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
