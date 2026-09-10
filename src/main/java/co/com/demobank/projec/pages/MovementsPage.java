package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.DriverFactory;
import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/*
 * ============================================================================
 * PAGE OBJECT: MovementsPage
 * ============================================================================
 *
 * REPRESENTA: La pantalla de Movimientos (lista de transacciones) de DemoBank.
 *
 * CASOS DE PRUEBA DEL PDF (Módulo 3):
 *   - Buscador de Transacciones (filtro parcial case-insensitive)
 *   - Filtro Avanzado de Ingresos (montos positivos)
 *   - Filtro Avanzado de Gastos (montos negativos)
 *   - Manejo de Estado Vacío (sin resultados)
 * ============================================================================
 */
public class MovementsPage {

    private final AndroidDriver driver;

    // ========================================================================
    // LOCALIZADORES
    // ========================================================================
    // ⚠️ COMPLETAR con los IDs reales de DemoBank cuando tengas la APK
    // ========================================================================

    // Campo de búsqueda (filtro de transacciones)
    private final By searchField =
            By.id("com.demobank.app:id/edit_search");

    // Botón para limpiar búsqueda
    private final By clearSearchButton =
            By.id("com.demobank.app:id/btn_clear_search");

    // Cada item de movimiento (se repite en lista)
    // ⚠️ Lista dinámica - usa XPath porque no hay ID específico por item
    private final By movementItem =
            By.xpath("//android.widget.LinearLayout[@resource-id='com.demobank.app:id/item_movement_container']");

    // Título del movimiento (descripción, ej: "Transferencia a Juan")
    private final By movementTitle =
            By.id("com.demobank.app:id/txt_movement_title");

    // Monto del movimiento (puede ser "+$500.000" ingresos o "-$200.000" gastos)
    // ⚠️ Justificación OCR: el monto es un componente con formato custom
    private final By movementAmount =
            By.xpath("//*[@resource-id='current amount']");

    // Categoría del movimiento (ej: "Servicios", "Transferencia")
    private final By movementCategory =
            By.id("com.demobank.app:id/txt_movement_category");

    // Filtro "Ingresos"
    private final By incomeFilter =
            By.id("com.demobank.app:id/btn_filter_income");

    // Filtro "Gastos"
    private final By expenseFilter =
            By.id("com.demobank.app:id/btn_filter_expense");

    // Filtro "Todos"
    private final By allFilter =
            By.id("com.demobank.app:id/btn_filter_all");

    // Empty state (cuando no hay resultados)
    private final By emptyState =
            By.id("com.demobank.app:id/empty_state_container");

    // Empty state texto
    private final By emptyStateText =
            By.id("com.demobank.app:id/txt_empty_state");

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================
    public MovementsPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // ========================================================================
    // ACCIONES
    // ========================================================================

    /**
     * Escribe en el campo de búsqueda (case-insensitive).
     */
    public void searchFor(String transaction) {
        WebElement field = WaitUtils.waitForVisibility(searchField);
        field.clear();
        field.sendKeys(transaction);
    }

    /**
     * Limpia el campo de búsqueda.
     */
    public void clearSearch() {
        WebElement field = WaitUtils.waitForVisibility(searchField);
        field.clear();
    }

    /**
     * Activa el filtro de Ingresos (mostrar solo movimientos positivos).
     */
    public void tapIncomeFilter() {
        WaitUtils.safeClick(incomeFilter);
    }

    /**
     * Activa el filtro de Gastos (mostrar solo movimientos negativos).
     */
    public void tapExpenseFilter() {
        WaitUtils.safeClick(expenseFilter);
    }

    /**
     * Quita todos los filtros (mostrar todo).
     */
    public void tapAllFilter() {
        WaitUtils.safeClick(allFilter);
    }

    // ========================================================================
    // MÉTODOS DE ESTADO
    // ========================================================================

    /**
     * Verifica si estamos en la pantalla de Movimientos.
     */
    public boolean isOnMovementsScreen() {
        try {
            return driver.findElement(searchField).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si hay al menos un movimiento visible.
     * Espera hasta que aparezca al menos uno.
     */
    public boolean hasMovements() {
        try {
            WaitUtils.waitForVisibility(movementTitle);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cuenta cuántos movimientos hay visibles.
     */
    public int getMovementsCount() {
        List<WebElement> items = driver.findElements(movementItem);
        return items.size();
    }

    /**
     * Verifica si el empty state está visible.
     */
    public boolean isEmptyStateDisplayed() {
        try {
            return driver.findElement(emptyState).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene el texto del empty state.
     */
    public String getEmptyStateText() {
        return WaitUtils.waitForVisibility(emptyStateText).getText();
    }

    /**
     * Obtiene los montos de TODOS los movimientos visibles.
     * Útil para validar que filtró correctamente.
     */
    public List<WebElement> getAllMovementAmounts() {
        return driver.findElements(movementAmount);
    }
}
