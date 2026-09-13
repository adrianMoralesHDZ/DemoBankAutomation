# DemoBankAutomation - Framework de Automatizacion Mobile

Framework de pruebas automatizadas para la aplicacion Android **DemoBank** usando **Appium + Java + TestNG**, siguiendo el patron **Page Object Model (POM)**.

> **App bajo prueba:** DemoBank (APK standalone, 100% mockeada, sin backend real).

---

## Tabla de Contenidos

1. [Stack Tecnologico](#stack-tecnologico)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [Casos de Prueba](#casos-de-prueba)
4. [Prerrequisitos](#prerrequisitos)
5. [Instalacion Paso a Paso](#instalacion-paso-a-paso)
6. [Configuracion](#configuracion)
7. [Ejecutar los Tests](#ejecutar-los-tests)
8. [Reportes de Allure](#reportes-de-allure)
9. [Arquitectura y Reglas](#arquitectura-y-reglas)

---

## Stack Tecnologico

| Herramienta | Version | Para que sirve |
|---|---|---|
| Java JDK | 11+ | Lenguaje de programacion |
| Maven | 3.8+ | Gestor de dependencias y build |
| Appium Server | 2.x | Motor de automatizacion mobile |
| Appium Java Client | 8.6.0 | API Java para Appium |
| Selenium WebDriver | 4.18.1 | API base de automatizacion |
| TestNG | 7.10.2 | Test runner y aserciones |
| Tess4J (Tesseract OCR) | 5.11.0 | Lectura de texto en imagenes |
| JavaCV (OpenCV) | 1.5.10 | Comparacion visual pixel a pixel |
| Allure | 2.29.0 | Reportes HTML interactivos |

---

## Estructura del Proyecto

```
DemoBankAutomation/
├── src/
│   ├── main/java/co/com/demobank/projec/
│   │   ├── pages/                          # Page Objects (POM)
│   │   │   ├── SplashPage.java             # Pantalla Splash inicial
│   │   │   ├── LoginPage.java              # Pantalla de Login
│   │   │   ├── HomePage.java               # Home con saldos y accesos rapidos
│   │   │   ├── MovementsPage.java          # Lista de movimientos
│   │   │   ├── TransferPage.java           # Modal de transferencia (3 pasos)
│   │   │   ├── TransferSuccessPage.java    # Pantalla de exito transferencia
│   │   │   ├── PayPage.java               # Modal de pago de servicios (3 pasos)
│   │   │   └── PaySuccessPage.java         # Pantalla de exito pago
│   │   └── utils/                          # Utilidades
│   │       ├── DriverFactory.java           # Crea y gestiona el driver
│   │       ├── WaitUtils.java               # Esperas explicitas (sin Thread.sleep)
│   │       ├── OCRUtils.java                # Tesseract OCR (lectura de texto)
│   │       ├── ImageMatchUtils.java         # OpenCV (comparacion visual)
│   │       ├── AllureHelper.java            # Helper de reportes Allure
│   │       └── TestDataProvider.java        # Datos de prueba centralizados
│   └── test/
│       ├── java/tests/
│       │   ├── LoginTests.java              # TC01-TC04: Login
│       │   ├── HomeTests.java               # TC05-TC10: Home
│       │   ├── MovementsTests.java          # TC09-TC12: Movimientos
│       │   ├── TransferTests.java           # TC13-TC17: Transferencias
│       │   ├── PayTests.java                # TC18-TC23: Pagos
│       │   ├── OpenCVRegressionTest.java     # Regresion visual
│       │   ├── TestListener.java             # Captura screenshots en fallos
│       │   └── StepValidation.java           # Helper de pasos Allure
│       └── resources/
│           ├── testng.xml                    # Configuracion de la suite
│           └── baselines/                    # Imagenes base para OpenCV
└── pom.xml                                  # Dependencias Maven
```

---

## Casos de Prueba

El framework implementa **20+ casos funcionales** distribuidos en 5 modulos:

### Modulo 1: Login (4 casos)

| # | Test | Grupo | Que valida |
|---|------|-------|------------|
| TC01 | testLoginExitoso | login, smoke | Login con credenciales validas -> Home |
| TC02 | testEmailVacio | login, negative | Email vacio muestra error |
| TC03 | testPasswordVacio | login, negative | Password vacio muestra error |
| TC04 | testTogglePassword | login | Toggle mostrar/ocultar password |

### Modulo 2: Home (6 casos)

| # | Test | Grupo | Que valida |
|---|------|-------|------------|
| TC05 | testSaldoConsolidado | home | Saldo total = Corriente + Ahorros |
| TC06 | testSaldoCuentaCorriente | home | Saldo Cuenta Corriente = $1,500,000 |
| TC07 | testInteractividadCuentas | home | Cambio entre tabs actualiza pantalla |
| TC08 | testAccesoRapidoTransferir | home, navigation | Boton Transferir abre modal |
| TC09 | testAccesoRapidoPagar | home, navigation | Boton Pagar abre pantalla |
| TC10 | testAccesoRapidoMovimientos | home, navigation | Boton Movimientos abre lista |

### Modulo 3: Movimientos (4 casos)

| # | Test | Grupo | Que valida |
|---|------|-------|------------|
| TC09 | testBusquedaParcial | movements, search | Busqueda parcial case-insensitive |
| TC10 | testFiltroIngresos | movements, filter | Filtro Ingresos: montos positivos (+) |
| TC11 | testFiltroGastos | movements, filter | Filtro Gastos: montos negativos (-) |
| TC12 | testEmptyState | movements, negative | Busqueda sin resultados -> empty state |

### Modulo 4: Transferencias (5 casos)

| # | Test | Grupo | Que valida |
|---|------|-------|------------|
| TC13 | testTransferenciaExitosa | transfer, happy-path | Transferencia completa -> exito |
| TC14 | testSaldoInsuficiente | transfer, negative | Monto > saldo -> error |
| TC15 | testMontoInvalido | transfer, negative | Monto cero -> error |
| TC16 | testImpactoSaldoOrigen | transfer | Saldo despues = antes - monto |
| TC17 | testAuditoriaEnMovimientos | transfer, audit | Transferencia registrada en Movimientos |

### Modulo 5: Pagos de Servicios (6 casos)

| # | Test | Grupo | Que valida |
|---|------|-------|------------|
| TC18 | testPagoServicioEnergia | pay, happy-path | Pago exitoso + precarga monto |
| TC19 | testPagoServicioAgua | pay, happy-path | Pago exitoso + precarga monto |
| TC20 | testPrecargaMonto | pay | Monto se precarga automaticamente |
| TC21 | testPagoSaldoInsuficiente | pay, negative | Monto > saldo -> error |
| TC22 | testMontoInvalido | pay, negative | Monto cero -> error |
| TC23 | testAuditoriaEnMovimientos | pay, audit | Pago en Movimientos con categoria Servicios |

### Regresion Visual (1 caso)

| # | Test | Grupo | Que valida |
|---|------|-------|------------|
| - | testRegresionVisualHome | visual-regression | OpenCV: Score >= 95% vs baseline |

---

## Credenciales y Datos Mockeados

```
Email: demo@demo.com
Password: 1234

Cuenta Corriente: $1,500,000.00
Cuenta Ahorros:   $955,450.00
Saldo Consolidado: $2,455,450.00
```

Todos los datos estan centralizados en `TestDataProvider.java`.

---

## Prerrequisitos

### 1. Java JDK 11+

```bash
java -version
```

Descargar de: https://adoptium.net/

### 2. Maven

```bash
mvn -version
```

Descargar de: https://maven.apache.org/download.cgi

### 3. Appium Server 2.x

```bash
npm install -g appium
appium driver install uiautomator2
appium --version
```

### 4. Android SDK (ADB)

```bash
adb devices
```

Instalar Android Studio: https://developer.android.com/studio

### 5. Tesseract OCR

1. Descargar desde: https://github.com/UB-Mannheim/tesseract/wiki
2. Instalar en: `C:\Program Files\Tesseract-OCR\`
3. Para espanol, descargar `spa.traineddata` y ponerlo en `tessdata/`

### 6. Appium Inspector (recomendado)

Descargar desde: https://github.com/appium/appium-inspector/releases

---

## Instalacion Paso a Paso

### Paso 1: Clonar el proyecto

```bash
git clone <url-del-repo>
cd DemoBankAutomation
```

### Paso 2: Instalar dependencias de Maven

```bash
mvn clean install -DskipTests
```

### Paso 3: Configurar dispositivo

**Opcion A: Emulador**
1. Android Studio -> Device Manager -> Create Device
2. Crear emulador (ej: Pixel 5, Android 13+)
3. Iniciar el emulador

**Opcion B: Dispositivo real**
1. Activar "Depuracion USB" en Opciones de desarrollador
2. Conectar via cable USB
3. Verificar: `adb devices`

### Paso 4: Instalar APK de DemoBank

```bash
adb install DemoBank.apk
```

### Paso 5: Configurar DriverFactory

Editar `DriverFactory.java` con el ID de tu dispositivo:

```java
private static final String DEVICE_NAME = "TU_ID_AQUI";
```

### Paso 6: Iniciar Appium Server

```bash
appium
```

Debes ver: `Appium REST http interface listener started on http://127.0.0.1:4723`

---

## Configuracion

### DriverFactory.java

```java
private static final String SERVER_URL = "http://127.0.0.1:4723/";
private static final String DEVICE_NAME = "ZY22KVZPQ4";
private static final String APP_PACKAGE = "com.demobank.app";
private static final String APP_ACTIVITY = "com.demobank.app.MainActivity";
```

### OCRUtils.java

```java
tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
```

---

## Ejecutar los Tests

### Ejecutar toda la suite

```bash
mvn test
```

### Ejecutar un grupo especifico

```bash
mvn test -Dgroups="login"
mvn test -Dgroups="home"
mvn test -Dgroups="transfer"
mvn test -Dgroups="pay"
mvn test -Dgroups="negative"
mvn test -Dgroups="visual-regression"
```

---

## Reportes de Allure

### Generar reporte HTML

```bash
# 1. Los tests generan archivos en allure-results/ automaticamente
# 2. Levantar el servidor de Allure:
allure serve allure-results/
```

Esto abre el navegador con un reporte HTML interactivo.

### Caracteristicas del reporte

- **Paso a paso detallado**: Cada test usa anotaciones `@Step` que muestran
  exactamente que se hizo en cada paso (seleccionar contacto, escribir monto,
  tap continuar, validar resultado, etc.).
- **Screenshots automaticos en fallos**: Las capturas de pantalla se adjuntan
  **unicamente** cuando un test falla (via `TestListener.onTestFailure`).
  Los tests exitosos no generan capturas innecesarias.
- **Validaciones documentadas**: Cada asercion registra el valor actual vs
  esperado y el resultado (PASS/FAIL) como texto adjunto.
- **Detalles del error**: En caso de fallo, se adjunta el stack trace completo,
  el mensaje de excepcion y la duracion del test.
- **Metadatos de trazabilidad**: Informacion de pagina, URL y timestamp.

### Estructura del reporte por test

```
[TEST] testTransferenciaExitosa
  ├── @Step: Paso 1 - Seleccionar primer contacto (Maria Lopez)
  ├── @Step: Paso 2 - Ingresar monto: $100000 y tap Continuar
  ├── @Step: Paso 3 - Confirmar transferencia
  ├── @Step: Paso 4 - Verificar pantalla de exito
  │     ├── Validacion: Pantalla de exito = PASS
  │     ├── Validacion: Titulo contiene 'exitosa' = PASS
  │     ├── Validacion: Monto en pantalla = PASS
  │     └── Validacion: Destinatario = PASS
  │
  ├── [SI FALLA] Screenshot: FALLO - testTransferenciaExitosa
  └── [SI FALLA] Detalles del Fallo (stack trace, excepcion, duracion)
```

---

## Arquitectura y Reglas

### Patron Page Object Model (POM)

```
Tests (con Asserts)  ->  Pages (con localizadores y acciones)  ->  App
     ↑                           ↑
  Validan resultados        Interactuan con la app
```

**Reglas:**
- Los **Pages** NUNCA tienen `Assert`.
- Los **Pages** NUNCA tienen logica de reporte (Allure).
- Los **Tests** NUNCA tienen localizadores.
- Los datos fijos estan en `TestDataProvider`.

### Sincronizacion

- **Prohibido** `Thread.sleep()`.
- Se usan esperas explicitas via `WaitUtils` (WebDriverWait / FluentWait).
- `FluentWait` ignora `StaleElementReferenceException` para React Native.

### Datos Externalizados

Todos los datos de prueba estan en `TestDataProvider.java`:

```java
TestDataProvider.VALID_EMAIL         // "demo@ao.com"
TestDataProvider.VALID_PASSWORD      // "1234"
TestDataProvider.SALDO_CORRIENTE     // 1500000.00
TestDataProvider.SALDO_AHORROS       // 955450.00
TestDataProvider.SALDO_CONSOLIDADO   // 2455450.00
TestDataProvider.MONTO_TRANSFER_VALIDO  // 100000.00
TestDataProvider.BUSQUEDA_NO_EXISTE     // "xyzzz_no_existe_12345"
```

### OCR (Tesseract)

Se usa OCR cuando los selectores nativos no son confiables:
- Saldos en tarjetas con gradiente
- Montos con fuentes personalizadas
- Texto embebido en componentes graficos

### OpenCV (Comparacion Visual)

- Compara screenshot actual vs baseline.
- Score >= 95% = PASS.
- Baselines en `src/test/resources/baselines/`.

---

## Troubleshooting

### "Cannot connect to Appium Server"
- Verifica que Appium Server este corriendo: `appium`
- Verifica el puerto: `http://127.0.0.1:4723`

### "Device not found"
- Verifica con `adb devices`
- Asegurate de que el dispositivo/emulador este conectado

### "Tesseract not found"
- Verifica la ruta en OCRUtils.java
- Debe existir: `C:\Program Files\Tesseract-OCR\tessdata\`

### "Baseline image not found"
- Crea la carpeta `src/test/resources/baselines/`
- Toma un screenshot de referencia:
```bash
adb shell screencap -p /sdcard/home.png
adb pull /sdcard/home.png src/test/resources/baselines/home_baseline.png
```

### Los tests fallan por flakiness
- Aumenta el timeout en WaitUtils: `Duration.ofSeconds(30)`
- Usa `WaitUtils.fluentWait()` en lugar de `waitForVisibility()`
