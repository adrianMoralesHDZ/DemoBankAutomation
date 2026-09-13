package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;

/*
 * ============================================================================
 * PAGE OBJECT: PayPage (DemoBank)
 * ============================================================================
 *
 * LOCALIZADORES BASADOS EN DUMP REAL (adb uiautomator dump):
 *
 * Pantalla 1 - Lista de servicios:
 *   "¿Qué deseas pagar?"      → titulo
 *   "Energía Eléctrica"        → TextView (dentro de ViewGroup clickable)
 *   "Agua Potable"             → TextView
 *   "Continuar"               → NO existe aqui, se tap el servicio directamente
 *
 * Pantalla 2 - Monto:
 *   "Cambiar servicio"         → volver a lista
 *   "Monto a pagar"            → etiqueta
 *   EditText con "65.40"      → monto precargado (editable)
 *   "Continuar"               → TextView (boton)
 *
 * Pantalla 3 - Confirmacion:
 *   "Confirma tu pago"         → titulo
 *   "Confirmar pago"           → TextView (boton final)
 *
 * Pantalla 4 - Exito:
 *   "¡Pago exitoso!"           → titulo
 *   "Pagaste $X de Servicio"   → detalle
 *   "Volver al inicio"         → boton
 * ============================================================================
 */
public class PayPage {

    private final AndroidDriver driver;

    // ========================================================================
    // LOCALIZADORES (basados en dump real)
    // ========================================================================

    // Pantalla 1: Lista de servicios
    private final By payTitle =
            By.xpath("//*[contains(@text,'deseas pagar')]");

    // Cada servicio es un ViewGroup clickable que contiene TextViews
    // Primer servicio = primer ViewGroup clickable
    private final By firstService =
            By.xpath("(//android.view.ViewGroup[@clickable='true'])[1]");

    // Pantalla 2: Monto
    private final By amountLabel =
            By.xpath("//*[@text='Monto a pagar']");

    // Campo editable del monto (EditText con el valor precargado)
    private final By amountField =
            By.xpath("(//android.widget.EditText)[1]");

    // Boton "Continuar" (pantalla de monto)
    private final By continueButton =
            By.xpath("//*[@text='Continuar']");

    // Pantalla 3: Confirmacion
    private final By confirmTitle =
            By.xpath("//*[@text='Confirma tu pago']");

    // Boton "Confirmar pago"
    private final By confirmButton =
            By.xpath("//*[@text='Confirmar pago']");

    // Error de saldo insuficiente
    private final By insufficientError =
            By.xpath("//*[contains(@text,'Saldo insuficiente')]");

    // Error de monto inválido (vacío o cero)
    private final By invalidAmountError =
            By.xpath("//*[contains(@text,'monto válido')]");

    public PayPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // ========================================================================
    // ACCIONES
    // ========================================================================

    /**
     * Selecciona un servicio por nombre (ej: "Agua", "Energ", "Internet").
     * Si el nombre es null o vacio, selecciona el primer servicio.
     */
    public void selectService(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            selectFirstService();
        } else {
            selectServiceByName(serviceName);
        }
    }

    /**
     * Selecciona el primer servicio disponible (Energía Eléctrica).
     */
    public void selectFirstService() {
        WaitUtils.safeClick(firstService);
    }

    /**
     * Selecciona un servicio por nombre (ej: "Agua").
     */
    public void selectServiceByName(String name) {
        List<WebElement> services = driver.findElements(
                By.xpath("//android.view.ViewGroup[@clickable='true']"));
        for (WebElement service : services) {
            List<WebElement> children = service.findElements(
                    By.xpath(".//android.widget.TextView"));
            for (WebElement child : children) {
                if (child.getText().contains(name)) {
                    service.click();
                    return;
                }
            }
        }
        throw new RuntimeException("Servicio no encontrado: " + name);
    }

    /**
     * Toca continuar al siguiente paso.
     */
    public void tapContinue() {
        WaitUtils.safeClick(continueButton);
    }

    /**
     * Confirma el pago.
     */
    public void tapConfirmPay() {
        WaitUtils.safeClick(confirmButton);
    }

    /**
     * Cambia el monto sugerido por uno custom.
     */
    public void customAmount(String amount) {
        WebElement field = WaitUtils.waitForVisibility(amountField);
        field.clear();
        field.sendKeys(amount);
    }

    /**
     * Pago completo: selecciona, continua y confirma.
     *
     * @param serviceName nombre parcial del servicio ("Agua", "Energ", "Internet").
     *                    Si es null o vacio, selecciona el primer servicio.
     */
    public void completePayment(String serviceName) {
        selectService(serviceName);
        tapContinue();
        tapConfirmPay();
    }

    // ========================================================================
    // METODOS DE ESTADO
    // ========================================================================

    /**
     * Verifica si estamos en pantalla de lista de servicios.
     */
    public boolean isOnPayScreen() {
        try {
            return driver.findElement(payTitle).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el monto sugerido del servicio seleccionado.
     */
    public String getSuggestedAmount() {
        return WaitUtils.waitForVisibility(amountField).getText();
    }

    /**
     * Verifica si el monto está pre-cargado en el campo.
     */
    public boolean isAmountPreloaded() {
        try {
            WebElement field = WaitUtils.waitForVisibility(amountField);
            String text = field.getText();
            return text != null && !text.trim().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si hay error de fondos insuficientes.
     */
    public boolean isInsufficientBalanceErrorDisplayed() {
        try {
            return driver.findElement(insufficientError).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si hay error de monto inválido (vacío o cero).
     * El mensaje esperado contiene "monto válido".
     */
    public boolean isInvalidAmountErrorDisplayed() {
        try {
            return WaitUtils.fluentWait(invalidAmountError).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

}
