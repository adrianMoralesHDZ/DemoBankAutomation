package co.com.demobank.projec.utils;

import org.testng.annotations.DataProvider;

/*
 * ============================================================================
 * UTIL: TestDataProvider
 * ============================================================================
 *
 * ¿QUÉ HACE ESTA CLASE?
 * Centraliza TODOS los datos de prueba del framework.
 * Evita el "hardcodeo" (tener valores fijos dentro de los métodos de test).
 *
 * -----------------------------------------------------------------------
 * REGLA DEL PDF: EXTERNALIZACIÓN DE DATOS
 * -----------------------------------------------------------------------
 * El PDF exige que "Todos los valores de prueba fijos, credenciales y
 * montos lógicos deben extraerse a través del TestDataProvider, evitando
 * el hardcodeo de datos en los métodos de prueba."
 *
 * Esto significa que NO debes escribir cosas como:
 *   homePage.typeSearchTerm("Xbox One");  // ❌ HARDCODEADO
 *
 * En su lugar:
 *   homePage.typeSearchTerm(TestDataProvider.BUSCAR_XBOX_ONE);  // ✅ EXTERNALIZADO
 *
 * -----------------------------------------------------------------------
 * ¿POR QUÉ EXTERNALIZAR LOS DATOS?
 * -----------------------------------------------------------------------
 *   1. Mantenibilidad: si cambias un valor, lo cambias en un solo lugar
 *   2. Legibilidad: el test lee "BUSCAR_XBOX_ONE" no "Xbox One"
 *   3. Reutilización: múltiples tests pueden usar el mismo dato
 *   4. Data-Driven Testing: con @DataProvider, un test se ejecuta
 *      múltiples veces con datos diferentes
 *
 * -----------------------------------------------------------------------
 * DOS TIPOS DE DATOS AQUÍ:
 * -----------------------------------------------------------------------
 *   1. CONSTANTES (static final String) → valores fijos que no cambian
 *   2. @DataProvider → generan matrices de datos para TestNG
 *
 *   Constante:   TestDataProvider.BUSCAR_XBOX_ONE
 *   DataProvider: @Test(dataProvider = "terminosDeBusqueda", ...)
 *
 * -----------------------------------------------------------------------
 * PARA DEMOBANK (cuando tengas el APK):
 * -----------------------------------------------------------------------
 * Cambia los datos por los de DemoBank:
 *   VALID_EMAIL     → "demo@demo.com"
 *   VALID_PASSWORD  → "1234"
 *   CUENTA_CORRIENTE → 1500000.00
 *   CUENTA_AHORROS  → 955450.00
 *   Y agrega DataProviders para contactos, montos, etc.
 * ============================================================================
 */
public class TestDataProvider {

    // ========================================================================
    // SECCIÓN 1: CONSTANTES DE DEMOBANK (las que pide el PDF)
    // ========================================================================
    // Estos son los valores que el PDF dice que están mockeados en DemoBank.
    // Cuando tengas el APK, estos serán los datos reales que uses.
    // Por ahora quedan aquí listos para cuando los necesites.
    // ========================================================================

    // Credenciales válidas (del PDF)
    public static final String VALID_EMAIL         = "demo@demo.com";
    public static final String VALID_PASSWORD      = "1234";

    // String vacío para pruebas negativas (email vacío, password vacío)
    public static final String EMPTY_STRING        = "";

    // Cuentas mockeadas (del PDF)
    // Cuenta Corriente: $1,500,000.00
    // Cuenta Ahorros: $955,450.00
    // Saldo consolidado: $1,500,000 + $955,450 = $2,455,450.00
    public static final double SALDO_CORRIENTE     = 1500000.00;
    public static final double SALDO_AHORROS       = 955450.00;
    public static final double SALDO_CONSOLIDADO   = SALDO_CORRIENTE + SALDO_AHORROS;

    // ========================================================================
    // SECCIÓN 2: MONTOS DE PRUEBA PARA TRANSFERENCIAS Y PAGOS
    // ========================================================================
    // Montos válidos para tests de transferencias y pagos
    // ========================================================================

    // Monto válido de transferencia
    public static final double MONTO_TRANSFER_VALIDO    = 100000.00;   // $100.000

    // Monto para prueba de descuento (pre/post en saldo)
    public static final double MONTO_DESCUENTO_TEST      = 200000.00;   // $200.000

    // Monto para auditoría
    public static final double MONTO_AUDITORIA           = 50000.00;    // $50.000

    // Monto inválido (mayor al saldo disponible)
    public static final double MONTO_INSUFICIENTE       = 3000000.00;  // $3.000.000 (mayor a $1.500.000)

    // Monto exagerado para pago (mayor al saldo)
    public static final double MONTO_PAGO_INSUFICIENTE = 9999999999.0;

    // Texto que NO debería existir (para empty state)
    public static final String BUSQUEDA_NO_EXISTE      = "xyzzz_no_existe_12345";

    // Strings vacíos para validación
    public static final String EMAIL_VACIO             = "";
    public static final String PASSWORD_VACIO          = "";

    // ========================================================================
    // SECCIÓN 3: DATAPROVIDERS
    // ========================================================================
    // Un @DataProvider es un método que devuelve una matriz (Object[][]).
    // TestNG lo usa para ejecutar el test una vez por cada fila.
    //
    // Ejemplo: si el DataProvider tiene 5 filas, el @Test se ejecuta 5 veces,
    // recibiendo cada fila como parámetro.
    //
    // USO EN EL TEST:
    //   @Test(dataProvider = "montosTransferencia",
    //         dataProviderClass = TestDataProvider.class)
    //   public void testMultiplesMontos(double monto) {
    //       transferPage.typeAmount(String.valueOf(monto));
    //   }
    // ========================================================================

    /**
     * DataProvider: múltiples montos válidos para transferencias.
     * Cada fila es un caso de prueba con un monto diferente.
     */
    @DataProvider(name = "montosTransferencia")
    public static Object[][] montosTransferencia() {
        return new Object[][] {
                {100000.0},    // $100.000
                {200000.0},    // $200.000
                {50000.0},     // $50.000
                {75000.0},     // $75.000
                {150000.0}     // $150.000
        };
    }

    /**
     * DataProvider: montos que NO deberían procesarse.
     * Útil para validar validaciones de fondos/formatos.
     */
    @DataProvider(name = "montosInvalidos")
    public static Object[][] montosInvalidos() {
        return new Object[][] {
                {0.0},            // Cero
                {-100.0},         // Negativo
                {9999999999.0}    // Exagerado
        };
    }

    /**
     * DataProvider: múltiples contactos para transferir.
     */
    @DataProvider(name = "contactos")
    public static Object[][] contactos() {
        return new Object[][] {
                {"Juan Pérez"},
                {"María López"},
                {"Carlos García"}
        };
    }
}
