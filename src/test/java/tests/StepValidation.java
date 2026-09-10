package tests;

import io.qameta.allure.Step;

/*
 * ============================================================================
 * UTIL: StepValidation
 * ============================================================================
 *
 * Métodos auxiliares para registrar pasos de validación en Allure.
 * Cada método público está marcado con @Step para que aparezca como
 * un step separado en el reporte de Allure, con su propio screenshot
 * y logs.
 *
 * Usar en tests para:
 *   - Loggear valores intermedios durante la ejecución
 *   - Validar condiciones que no justifican un Assert completo
 *   - Agregar contexto al reporte
 * ============================================================================
 */
public class StepValidation {

    /**
     * Registra un mensaje en el log de Allure como un step independiente.
     */
    @Step("Log: {0}")
    public static void log(String message) {
        System.out.println("[StepValidation] " + message);
    }

    /**
     * Registra una validación booleana (para reportes).
     */
    @Step("Verificar: {0}")
    public static void verify(String checkName, boolean condition) {
        System.out.println("[StepValidation] " + checkName + " = " + condition);
        if (!condition) {
            System.out.println("[StepValidation] ⚠️ Verificación falló");
        }
    }

    /**
     * Registra una comparación de valores.
     */
    @Step("Comparar: {0}")
    public static void compare(String label, Object actual, Object expected) {
        boolean matches = (actual == null && expected == null) ||
                (actual != null && actual.equals(expected));
        System.out.println("[StepValidation] " + label +
                " | actual=" + actual + " | esperado=" + expected +
                " | resultado=" + (matches ? "OK" : "DIFERENTE"));
    }

    /**
     * Registra el valor de una variable.
     */
    @Step("Valor: {0} = {1}")
    public static void valueOf(String name, Object value) {
        System.out.println("[StepValidation] " + name + " = " + value);
    }
}
