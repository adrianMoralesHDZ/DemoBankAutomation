package co.com.demobank.projec.pages;

import co.com.demobank.projec.utils.WaitUtils;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object de la pantalla de Movimientos de DemoBank.
 * <p>
 * Contiene el buscador, filtros (Ingresos/Gastos/Todos) y la lista
 * de transacciones.
 */
public class MovementsPage {

    private final AndroidDriver driver;

    // =========================================================================
    // Localizadores
    // =========================================================================

    private final By searchField =
            By.xpath("//*[contains(@text,'Buscar movimiento')]");

    private final By movementAmount =
            By.xpath("//android.widget.TextView[contains(@text,'$')]");

    private final By incomeFilter =
            By.xpath("//*[@text='Ingresos']");

    private final By expenseFilter =
            By.xpath("//*[@text='Gastos']");

    private final By emptyState =
            By.xpath("//*[contains(@text,'No hay movimientos que coincidan')]");

    // =========================================================================
    // Constructor
    // =========================================================================

    public MovementsPage(AndroidDriver driver) {
        this.driver = driver;
    }

    // =========================================================================
    // Acciones
    // =========================================================================

    /**
     * Escribe en el campo de busqueda y espera a que la lista se filtre.
     *
     * @param transaction texto a buscar
     */
    public void searchFor(String transaction) {
        WebElement field = WaitUtils.waitForVisibility(searchField);
        field.clear();
        field.sendKeys(transaction);

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(d -> {
                List<WebElement> montos = d.findElements(movementAmount);
                List<WebElement> empty = d.findElements(emptyState);
                return !montos.isEmpty() || !empty.isEmpty();
            });
        } catch (Exception e) {
            System.out.println("[INFO] searchFor: la lista tardo en filtrar");
        }
    }

    /**
     * Activa el filtro de Ingresos (montos positivos).
     */
    public void tapIncomeFilter() {
        WaitUtils.safeClick(incomeFilter);
    }

    /**
     * Activa el filtro de Gastos (montos negativos).
     */
    public void tapExpenseFilter() {
        WaitUtils.safeClick(expenseFilter);
    }

    // =========================================================================
    // Metodos de estado
    // =========================================================================

    /**
     * Verifica si estamos en la pantalla de Movimientos.
     *
     * @return true si el campo de busqueda esta presente
     */
    public boolean isOnMovementsScreen() {
        try {
            WaitUtils.fluentWait(searchField);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si hay al menos un movimiento visible.
     *
     * @return true si hay un monto con "$" en pantalla
     */
    public boolean hasMovements() {
        try {
            WaitUtils.fluentWait(movementAmount);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el empty state esta visible.
     *
     * @return true si el mensaje de sin resultados esta desplegado
     */
    public boolean isEmptyStateDisplayed() {
        try {
            return driver.findElement(emptyState).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtiene los WebElements de todos los montos visibles.
     *
     * @return lista de WebElements con monto
     */
    public List<WebElement> getAllMovementAmounts() {
        return driver.findElements(movementAmount);
    }

    /**
     * Verifica que todos los montos visibles empiecen con "+" (ingresos).
     *
     * @return true si todos los montos son positivos
     */
    public boolean allAmountsArePositive() {
        List<WebElement> amounts = getAllMovementAmounts();
        if (amounts.isEmpty()) return false;
        for (WebElement amount : amounts) {
            String text = amount.getText();
            if (text == null || !text.startsWith("+")) return false;
        }
        return true;
    }

    /**
     * Verifica que todos los montos visibles empiecen con "-" (gastos).
     *
     * @return true si todos los montos son negativos
     */
    public boolean allAmountsAreNegative() {
        List<WebElement> amounts = getAllMovementAmounts();
        if (amounts.isEmpty()) return false;
        for (WebElement amount : amounts) {
            String text = amount.getText();
            if (text == null || !text.startsWith("-")) return false;
        }
        return true;
    }
}
