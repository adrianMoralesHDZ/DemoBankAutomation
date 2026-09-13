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
│   │   │   ├── LoginPage.java              # Pantalla de Login
│   │   │   ├── HomePage.java               # Home con saldos y accesos rapidos
│   │   │   ├── MovementsPage.java          # Lista de movimientos
│   │   │   ├── TransferPage.java           # Modal de transferencia (3 pasos)
│   │   │   ├── TransferSuccessPage.java    # Pantalla de exito transferencia
│   │   │   ├── PayPage.java               # Modal de pago de servicios (3 pasos)
│   │   │   └── PaySuccessPage.java         # Pantalla de exito pago
│   │   └── utils/                          # Utilidades
│   │       ├── ConfigReader.java            # Lector de config.properties
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
│       │   └── TestListener.java             # Captura screenshots en fallos
│       └── resources/
│           ├── testng.xml                    # Configuracion de la suite
│           └── baselines/                    # Imagenes base para OpenCV
├── src/main/resources/
│   └── config.properties                     # Configuracion del entorno
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

### Paso 5: Configurar el dispositivo

Edita `src/main/resources/config.properties` con el ID de tu dispositivo:

```properties
device.name=TU_ID_AQUI
```

Obtener el ID con: `adb devices`

### Paso 6: Iniciar Appium Server

```bash
appium
```

Debes ver: `Appium REST http interface listener started on http://127.0.0.1:4723`

---

## Configuracion

Toda la configuracion del entorno esta centralizada en un archivo `.properties`:

```
src/main/resources/config.properties
```

Para ejecutar el framework en otro dispositivo o con otra configuracion, **solo edita este archivo** sin tocar codigo Java.

### Propiedades disponibles

```properties
# Appium Server
appium.server.url=http://127.0.0.1:4723/

# Dispositivo / Emulador (obtener ID con: adb devices)
device.name=ZY22KVZPQ4

# App DemoBank
app.package=com.demobank.app
app.activity=com.demobank.app.MainActivity

# Credenciales de DemoBank
credentials.email=demo@demo.com
credentials.password=1234

# Tesseract OCR
tesseract.datapath=C:\\Program Files\\Tesseract-OCR\\tessdata
tesseract.language=spa

# Reportes Allure - modo de captura de screenshots
# true  = capturar en cada paso (flujo completo)
# false = capturar solo en fallos
allure.screenshots.everyStep=true
```

### Como funciona

```
config.properties
       ↓
  ConfigReader.java  (lee el .properties una sola vez)
       ↓
  DriverFactory      (URL Appium, dispositivo, app package/activity)
  TestDataProvider   (credenciales, saldos)
  OCRUtils           (ruta tessdata, idioma)
  AllureHelper       (modo de captura de screenshots)
```

Cualquier propiedad puede ser sobreescrita editando directamente
`config.properties`:

```bash
# Cambiar dispositivo:
#   editar config.properties -> device.name=emulator-5554

# Cambiar modo de captura:
#   editar config.properties -> allure.screenshots.everyStep=false
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
- **Narrativa estilo Serenity**: Cada paso reporta que accion se realizo,
  sobre que elemento, que valor se envio, que valor devolvio la app y cual
  fue el resultado (PASS/FAIL).
- **Validaciones documentadas**: Cada asercion registra el valor actual vs
  esperado, el detalle del calculo y el resultado como texto adjunto.
- **Detalles del error**: En caso de fallo, se adjunta el stack trace completo,
  el mensaje de excepcion y la duracion del test.
- **Metadatos de trazabilidad**: Informacion de pagina, URL y timestamp.

### Modos de captura de screenshots

El framework soporta **dos modos** de captura de screenshots configurables
mediante la propiedad del sistema `allure.screenshots.everyStep`:

#### Modo 1: Flujo completo (por defecto)

Captura un screenshot en **cada paso** del test, sin importar si pasa o falla.
Genera un reporte visual paso a paso, similar a Serenity.

En `config.properties`:
```properties
allure.screenshots.everyStep=true
```

```bash
mvn test
```

**Que se obtiene**: Cada `@Step` del reporte tiene su screenshot del estado
de la app en ese momento. Si el test falla, se agrega un screenshot
adicional del estado de error + stack trace.

#### Modo 2: Solo fallos

Captura screenshots **unicamente cuando un test falla**. Los tests que pasan
no generan capturas, produciendo un reporte mas liviano. Cumple el requisito
del PDF: *"capturas unicamente ante la ocurrencia de fallas"*.

En `config.properties`:
```properties
allure.screenshots.everyStep=false
```

```bash
mvn test
```

**Que se obtiene**: Los tests exitosos solo tienen la narrativa textual
(que se envio, que devolvio, resultado). Los tests fallidos tienen
screenshot del error + stack trace + detalles del fallo.

#### Comparacion de modos

| Caracteristica | Flujo completo | Solo fallos |
|---|---|---|
| Screenshot en cada paso | SI | NO |
| Screenshot en fallo | SI | SI |
| Narrativa textual en cada paso | SI | SI |
| Stack trace en fallo | SI | SI |
| Valor en config.properties | `true` | `false` |
| Tamaño del reporte | Mayor | Menor |
| Cumple requisito PDF | - | SI |

### Estructura del reporte por test

```
[TEST] testTransferenciaExitosa
  │
  ├── @Step: Paso 1 - Seleccionar primer contacto (Maria Lopez)
  │     ├── Detalle de la accion:
  │     │     ACCION    : Seleccionar
  │     │     ELEMENTO  : Primer contacto de la lista (Maria Lopez)
  │     │     RESULTADO : Contacto seleccionado, pasando a pantalla de monto
  │     └── Screenshot: Seleccionar - Primer contacto de la lista
  │
  ├── @Step: Paso 2 - Ingresar monto: $100000 y tap Continuar
  │     ├── Detalle de la accion:
  │     │     ACCION    : Escribir
  │     │     ELEMENTO  : Campo de monto (EditText)
  │     │     VALOR ENVIADO: $100000
  │     │     RESULTADO : Monto escrito, ocultando teclado
  │     └── Screenshot: Escribir - Campo de monto (EditText)
  │
  ├── @Step: Paso 3 - Confirmar transferencia
  │     └── Screenshot: Tap - Boton 'Confirmar transferencia'
  │
  ├── @Step: Paso 4 - Verificar pantalla de exito
  │     ├── Validacion: Pantalla de exito visible
  │     │     VALOR OBTENIDO: Pantalla de exito detectada
  │     │     VALOR ESPERADO: Pantalla con titulo 'Transferencia exitosa'
  │     │     RESULTADO     : PASS
  │     ├── Validacion: Titulo de la pantalla de exito
  │     │     VALOR OBTENIDO: 'Transferencia exitosa!'
  │     │     VALOR ESPERADO: Debe contener 'exitosa'
  │     │     RESULTADO     : PASS
  │     └── Screenshot: Validacion: Pantalla de exito [PASS]
  │
  ├── [SI FALLA] Screenshot: FALLO - testTransferenciaExitosa
  └── [SI FALLA] Detalles del Fallo (stack trace, excepcion, duracion)
```

> **Nota:** Los screenshots de cada paso solo aparecen en modo "flujo completo".
> En modo "solo fallos", los pasos solo tienen la narrativa textual.

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

Las credenciales se leen desde `config.properties`:

```java
TestDataProvider.getValidEmail()        // "demo@demo.com" (de config.properties)
TestDataProvider.getValidPassword()     // "1234"          (de config.properties)
```

Los saldos mockeados, montos de prueba y textos de busqueda son constantes del framework:

```java
TestDataProvider.SALDO_CORRIENTE        // 1500000.00
TestDataProvider.SALDO_AHORROS          // 955450.00
TestDataProvider.SALDO_CONSOLIDADO      // 2455450.00
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
