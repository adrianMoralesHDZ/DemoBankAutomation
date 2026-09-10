package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import java.util.List;

/*
 * ============================================================================
 * PAGE OBJECT: PayPage
 * ============================================================================
 *
 * REPRESENTA: La pantalla modal de Pago de Servicios (3 pasos) de DemoBank.
 *
 * FLUJO DE 3 PASOS:
 *   Paso 1: Seleccionar servicio (empresa prestadora) + ver monto sugerido
 *   Paso 2: Confirmar monto (puede ser editable o venir pre-llenado)
 *   Paso 3: Confirmar pago
 *   → Después: PaySuccessPage
 *
 * CASOS DE PRUEBA DEL PDF (Módulo 5):
 *   - Flujo feliz de pago (2 empresas diferentes)
 *   - Precarga dinámica del monto sugerido
 *   - Validación de saldo insuficiente
 *   - Validación de monto inválido
 *   - Auditoría: aparece en Movimientos bajo categoría "Servicios"
 * ============================================================================
 */
public class PayPage {

    private final AndroidDriver driver;

    // ========================================================================
    // LOCALIZADORES
    // ========================================================================
    private final By serviceList =
            By.id("com.demobank.app:id/list_services");

    private final By serviceItem =
            By.id("com.demobank.app:id/item_service");

    private final By serviceName =
            By.id("com.demobank.app:id/txt_service_name");

    // Monto sugerido (pre-cargado desde el servicio seleccionado)
    private final By suggestedAmount =
            By.id("com.demobank.app:id/txt_suggested_amount");

    // Campo editable del monto (puede modificarse o quedar pre-llenado)
    private final By amountField =
            By.id("com.demobank.app:id/edit_pay_amount");

    private final By confirmButton =
            By.id("com.demobank.app:id/btn_confirm_pay");

    private final By continueButton =
            By.id("com.demobank.app:id/btn_continue_pay");

    private final By insufficientError =
            By.id("com.demobank.app:id/txt_pay_error");

    public PayPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // ========================================================================
    // ACCIONES
    // ========================================================================

    /**
     * Selecciona el primer servicio disponible.
     */
    public void selectFirstService() {
        WaitUtils.safeClick(serviceItem);
    }

    /**
     * Selecciona un servicio por nombre (ej: "Energía").
     */
    public void selectServiceByName(String name) {
        List<WebElement> services = driver.findElements(serviceItem);
        for (WebElement service : services) {
            if (service.getText().contains(name)) {
                service.click();
                return;
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
     * Pago completo: selecciona servicio (con su monto precargado) y paga.
     */
    public void completePayment() {
        selectFirstService();
        tapConfirmPay();
    }

    // ========================================================================
    // MÉTODOS DE ESTADO
    // ========================================================================

    /**
     * Verifica si estamos en pantalla de pago.
     */
    public boolean isOnPayScreen() {
        try {
            return driver.findElement(serviceList).isDisplayed()
                    || driver.findElement(amountField).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el monto sugerido del servicio seleccionado.
     * (Para validar la "precarga dinámica" del PDF).
     */
    public String getSuggestedAmount() {
        return WaitUtils.waitForVisibility(suggestedAmount).getText();
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
}
