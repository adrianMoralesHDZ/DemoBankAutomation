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

    // Campo de búsqueda (filtro de transacciones).
    // Segun dump real: TextView con texto "Buscar movimiento", sin resource-id.
    private final By searchField =
            By.xpath("//*[contains(@text,'Buscar movimiento')]");

    // Botón para limpiar búsqueda (aparece cuando hay texto en el search).
    // Segun dump: no tiene resource-id, buscar por content-desc o icono X.
    private final By clearSearchButton =
            By.xpath("//*[contains(@content-desc,'limpiar') or contains(@content-desc,'clear')]");

    // Cada item de movimiento (se repite en lista).
    // Segun dump: los items son ViewGroup que contienen TextViews con
   // titulo, categoria y monto. No hay resource-id, usar estructura.
    private final By movementItem =
            By.xpath("//android.view.ViewGroup[.//android.widget.TextView[contains(@text,'jul') or contains(@text,'jun')]]");

    // Título del movimiento (descripción, ej: "Transferencia a María López")
    // Segun dump: es el primer TextView del item que no es fecha ni monto.
    private final By movementTitle =
            By.xpath("(//android.widget.TextView[contains(@text,'jul') or contains(@text,'jun')])[1]/preceding::android.widget.TextView[1]");

    // Monto del movimiento (puede ser "+$500.000" ingresos o "-$200.000" gastos)
    // Segun dump: TextView que contiene "$" en el lado derecho del item.
    private final By movementAmount =
            By.xpath("//android.widget.TextView[contains(@text,'$')]");

    // Categoría del movimiento (ej: "Servicios", "Transferencia")
    // Segun dump: TextView debajo del titulo con "·" (ej: "Compras · 05 jul")
    private final By movementCategory =
            By.xpath("//android.widget.TextView[contains(@text,'·')]");

    // Filtro "Ingresos" (tab superior)
    private final By incomeFilter =
            By.xpath("//*[@text='Ingresos']");

    // Filtro "Gastos" (tab superior)
    private final By expenseFilter =
            By.xpath("//*[@text='Gastos']");

    // Filtro "Todos" (tab superior)
    private final By allFilter =
            By.xpath("//*[@text='Todos']");

    // Empty state (cuando no hay resultados).
    // Segun dump: buscar texto que indique sin resultados.
    private final By emptyState =
            By.xpath("//*[contains(@text,'Sin resultados') or contains(@text,'sin resultados')]");

    // Empty state texto
    private final By emptyStateText =
            By.xpath("//*[contains(@text,'Sin resultados') or contains(@text,'sin resultados')]");

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
