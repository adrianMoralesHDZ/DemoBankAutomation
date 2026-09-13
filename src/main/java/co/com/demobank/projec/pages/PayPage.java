package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Page Object del modal de Pago de Servicios de DemoBank (3 pasos).
 * <p>
 * Paso 1: Seleccionar servicio.
 * Paso 2: Ingresar monto (precargado automaticamente).
 * Paso 3: Confirmar pago.
 */
public class PayPage {

    private final AndroidDriver driver;

    // =========================================================================
    // Localizadores
    // =========================================================================

    private final By payTitle =
            By.xpath("//*[contains(@text,'deseas pagar')]");

    private final By firstService =
            By.xpath("(//android.view.ViewGroup[@clickable='true'])[1]");

    private final By amountField =
            By.xpath("(//android.widget.EditText)[1]");

    private final By continueButton =
            By.xpath("//*[@text='Continuar']");

    private final By confirmButton =
            By.xpath("//*[@text='Confirmar pago']");

    private final By insufficientError =
            By.xpath("//*[contains(@text,'Saldo insuficiente')]");

    private final By invalidAmountError =
            By.xpath("//*[contains(@text,'monto válido')]");

    // =========================================================================
    // Constructor
    // =========================================================================

    public PayPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // Acciones
    // =========================================================================

    /**
     * Selecciona un servicio por nombre. Si es null o vacio, selecciona
     * el primer servicio disponible.
     *
     * @param serviceName nombre parcial del servicio (ej: "Agua", "Energia")
     */
    public void selectService(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            selectFirstService();
        } else {
            selectServiceByName(serviceName);
        }
    }

    /**
     * Selecciona el primer servicio disponible.
     */
    public void selectFirstService() {
        WaitUtils.safeClick(firstService);
    }

    /**
     * Toca el boton "Continuar".
     */
    public void tapContinue() {
        WaitUtils.safeClick(continueButton);
    }

    /**
     * Toca el boton "Confirmar pago".
     */
    public void tapConfirmPay() {
        WaitUtils.safeClick(confirmButton);
    }

    /**
     * Reemplaza el monto sugerido por un valor personalizado.
     *
     * @param amount nuevo monto a ingresar
     */
    public void customAmount(String amount) {
        WebElement field = WaitUtils.waitForVisibility(amountField);
        field.clear();
        field.sendKeys(amount);
    }

    /**
     * Ejecuta el pago completo: seleccionar servicio, continuar y confirmar.
     *
     * @param serviceName nombre parcial del servicio (null para el primero)
     */
    public void completePayment(String serviceName) {
        selectService(serviceName);
        tapContinue();
        tapConfirmPay();
    }

    // =========================================================================
    // Metodos de estado
    // =========================================================================

    /**
     * Verifica si estamos en la pantalla de lista de servicios.
     *
     * @return true si el titulo esta visible
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
     *
     * @return texto del monto precargado
     */
    public String getSuggestedAmount() {
        return WaitUtils.waitForVisibility(amountField).getText();
    }

    /**
     * Verifica si el monto esta precargado en el campo.
     *
     * @return true si el campo tiene un valor no vacio
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
     * Verifica si hay error de saldo insuficiente.
     *
     * @return true si el mensaje de error esta visible
     */
    public boolean isInsufficientBalanceErrorDisplayed() {
        try {
            return driver.findElement(insufficientError).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si hay error de monto invalido (cero o vacio).
     *
     * @return true si el mensaje "monto valido" esta visible
     */
    public boolean isInvalidAmountErrorDisplayed() {
        try {
            return WaitUtils.fluentWait(invalidAmountError).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // =========================================================================
    // Metodos privados
    // =========================================================================

    /**
     * Selecciona un servicio buscando su nombre entre los TextViews hijos
     * de cada ViewGroup clickeable.
     *
     * @param name nombre parcial del servicio a buscar
     */
    private void selectServiceByName(String name) {
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
}
