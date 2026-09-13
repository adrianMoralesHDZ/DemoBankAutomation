package co.com.demobank.projec.utils;

/**
 * Centraliza todos los datos de prueba del framework.
 * <p>
 * Evita el hardcodeo de valores en los metodos de test y permite
 * mantener los datos en un unico punto.
 */
public class TestDataProvider {

    // =========================================================================
    // Credenciales
    // =========================================================================

    public static final String VALID_EMAIL    = "demo@demo.com";
    public static final String VALID_PASSWORD = "1234";

    // =========================================================================
    // Saldos mockeados de DemoBank
    // =========================================================================

    public static final double SALDO_CORRIENTE   = 1500000.00;
    public static final double SALDO_AHORROS     = 955450.00;
    public static final double SALDO_CONSOLIDADO = SALDO_CORRIENTE + SALDO_AHORROS;

    // =========================================================================
    // Montos de prueba para transferencias y pagos
    // =========================================================================

    public static final double MONTO_TRANSFER_VALIDO     = 100000.00;
    public static final double MONTO_DESCUENTO_TEST      = 200000.00;
    public static final String MONTO_AUDITORIA_TRANSFER  = "250000";
    public static final double MONTO_INSUFICIENTE        = 3000000.00;
    public static final double MONTO_PAGO_INSUFICIENTE   = 9999999999.0;
    public static final String MONTO_CERO                = "0";

    // =========================================================================
    // Textos de busqueda para Movimientos
    // =========================================================================

    public static final String BUSQUEDA_NO_EXISTE      = "xyzzz_no_existe_12345";
    public static final String BUSQUEDA_PARCIAL        = "transfe";
    public static final String BUSQUEDA_TRANSFERENCIA  = "Transferencia";
    public static final String BUSQUEDA_SERVICIO       = "Servicio";
}
