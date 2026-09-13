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
6. [Configuracion de Appium Inspector](#configuracion-de-appium-inspector)
7. [Configuracion del Framework](#configuracion-del-framework)
8. [Ejecutar los Tests](#ejecutar-los-tests)
9. [Reportes de Allure](#reportes-de-allure)
10. [Arquitectura y Reglas](#arquitectura-y-reglas)
11. [Troubleshooting](#troubleshooting)

---

## Stack Tecnologico

| Herramienta | Version instalada | Para que sirve |
|---|---|---|
| Java JDK | OpenJDK 11.0.0.2 | Lenguaje de programacion |
| Maven | 3.9.9 | Gestor de dependencias y build |
| Node.js | 22.22.0 | Runtime para Appium Server |
| Appium Server | 3.7.0 | Motor de automatizacion mobile |
| Appium Driver | UiAutomator2 8.6.1 | Driver de Android para Appium |
| Appium Java Client | 8.6.0 | API Java para Appium |
| Selenium WebDriver | 4.18.1 | API base de automatizacion |
| TestNG | 7.10.2 | Test runner y aserciones |
| Tess4J (Tesseract OCR) | 5.5.3 | Lectura de texto en imagenes |
| JavaCV (OpenCV) | 1.5.10 | Comparacion visual pixel a pixel |
| Allure | 2.43.0 | Reportes HTML interactivos |
| Android SDK (ADB) | 1.0.41 | Puente de depuracion Android |

---

## Estructura del Proyecto

```
DemoBankAutomation/
├── src/
│   ├── main/java/co/com/demobank/projec/
│   │   ├── pages/                          # Page Objects (POM)
│   │   │   ├── LoginPage.java              # Pantalla de Login
│   │   │   ├── HomePage.java               # Home con saldos, accesos rapidos y logout
│   │   │   ├── MovementsPage.java          # Lista de movimientos con filtros
│   │   │   ├── TransferPage.java           # Modal de transferencia (3 pasos)
│   │   │   ├── TransferSuccessPage.java    # Pantalla de exito transferencia
│   │   │   ├── PayPage.java               # Modal de pago de servicios (3 pasos)
│   │   │   └── PaySuccessPage.java         # Pantalla de exito pago
│   │   └── utils/                          # Utilidades
│   │       ├── ConfigReader.java            # Lector de config.properties
│   │       ├── DriverFactory.java           # Crea y gestiona el driver
│   │       ├── WaitUtils.java               # Esperas explicitas (sin Thread.sleep)
│   │       ├── OCRUtils.java                # Tesseract OCR (lectura de texto)
│   │       ├── ImageMatchUtils.java         # OpenCV (comparacion visual + auto-baseline)
│   │       ├── AllureHelper.java            # Helper de reportes Allure
│   │       └── TestDataProvider.java        # Datos de prueba centralizados
│   └── test/
│       ├── java/tests/
│       │   ├── LoginTests.java              # TC01-TC05: Login + Logout
│       │   ├── HomeTests.java               # TC05-TC10: Home
│       │   ├── MovementsTests.java          # TC09-TC12: Movimientos
│       │   ├── TransferTests.java           # TC13-TC17: Transferencias (OCR + OpenCV)
│       │   ├── PayTests.java                # TC18-TC23: Pagos
│       │   ├── MovementsVisualRegressionTest.java  # Regresion visual paso a paso (OpenCV)
│       │   └── TestListener.java             # Captura screenshots en fallos
│       └── resources/
│           ├── testng.xml                    # Configuracion de la suite
│           └── baselines/                    # Imagenes base para OpenCV
│               ├── transfer_success_baseline.png
│               └── movements_search/         # Baselines del flujo TC09
│                   ├── 01_home.png           # Pantalla Home (regresion visual)
│                   ├── 02_movements_list.png  # Lista de movimientos
│                   └── 03_search_results.png  # Resultados filtrados
├── src/main/resources/
│   └── config.properties                     # Configuracion del entorno
└── pom.xml                                  # Dependencias Maven
```

---

## Casos de Prueba

El framework implementa **22 casos funcionales** distribuidos en 5 modulos + 2 tests de regresion visual:

### Modulo 1: Login (5 casos)

| # | Test | Grupo | Que valida |
|---|------|-------|------------|
| TC01 | testLoginExitoso | login, smoke | Login con credenciales validas -> Home |
| TC02 | testEmailVacio | login, negative | Email vacio muestra error |
| TC03 | testPasswordVacio | login, negative | Password vacio muestra error |
| TC04 | testTogglePassword | login | Toggle mostrar/ocultar password |
| TC05 | testCierreSesion | login, logout | Logout retorna a Login (OCR + nativo) |

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
| TC13 | testTransferenciaExitosa | transfer, happy-path, ocr, opencv | Transferencia + OCR monto + OpenCV |
| TC14 | testSaldoInsuficiente | transfer, negative | Monto > saldo -> error |
| TC15 | testMontoInvalido | transfer, negative | Monto cero -> error |
| TC16 | testImpactoSaldoOrigen | transfer, ocr | Saldo antes/despues + OCR saldo tarjeta |
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

### Regresion Visual (1 test, 4 comparaciones)

| # | Test | Grupo | Que valida |
|---|------|-------|------------|
| - | testRegresionVisualBusquedaParcial | visual-regression, movements | OpenCV: 4 pantallas del flujo TC09 vs baselines (Home, Movimientos, Resultados) |

### Validaciones OCR (3 implementadas)

| # | Test | Que lee OCR | Justificacion |
|---|------|-------------|---------------|
| OCR #1 | TC05 LoginTests | Retorno a Login post-logout | Icono de logout sin content-id (emoji), tap por coordenadas |
| OCR #2 | TC13 TransferTests | Monto en pantalla de exito | Componente grafico personalizado (card con check verde) |
| OCR #3 | TC16 TransferTests | Saldo de tarjeta antes/despues | Tarjeta con fondo gradiente y fuente personalizada |

### Validaciones OpenCV (2 tests, 4 comparaciones)

| # | Test | Flujo | Baselines |
|---|------|-------|-----------|
| OpenCV #1 | TransferTests TC13 | Pantalla exito transferencia | 1 imagen |
| OpenCV #2 | MovementsVisualRegressionTest | Busqueda parcial paso a paso | 3 imagenes (Home, Movimientos, Resultados) |

---

## Credenciales y Datos Mockeados

```
Email: demo@demo.com
Password: 1234

Cuenta Corriente: $1,500,000.00
Cuenta Ahorros:   $955,450.00
Saldo Consolidado: $2,455,450.00
```

Las credenciales se leen de `config.properties` y los saldos son constantes en `TestDataProvider.java`.

---

## Prerrequisitos

### 1. Java JDK 11+

```bash
java -version
# Salida esperada: openjdk version "11.0.0.2"
```

Descargar de: https://adoptium.net/

### 2. Maven 3.9+

```bash
mvn -version
# Salida esperada: Apache Maven 3.9.9
```

Descargar de: https://maven.apache.org/download.cgi

### 3. Node.js 22+

```bash
node --version
# Salida esperada: v22.22.0
```

Descargar de: https://nodejs.org/

### 4. Appium Server 3.7+

```bash
npm install -g appium
appium driver install uiautomator2
appium --version
# Salida esperada: 3.7.0
```

Verificar driver instalado:
```bash
appium driver list
# Debe mostrar: uiautomator2@8.6.1 [installed (npm)]
```

### 5. Android SDK (ADB)

```bash
adb version
# Salida esperada: Android Debug Bridge version 1.0.41
```

Instalar Android Studio: https://developer.android.com/studio

### 6. Tesseract OCR 5.5+

1. Descargar desde: https://github.com/UB-Mannheim/tesseract/wiki
2. Instalar en: `C:\Program Files\Tesseract-OCR\`
3. Verificar:
```bash
tesseract --version
# Salida esperada: tesseract v5.5.3
```
4. Para espanol, descargar `spa.traineddata` y ponerlo en `tessdata/`

### 7. Allure 2.43+

```bash
allure --version
# Salida esperada: 2.43.0
```

Instalacion via npm:
```bash
npm install -g allure-commandline
```

### 8. Appium Inspector

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

Esto descarga todas las dependencias (Appium, Selenium, TestNG, Tess4J, OpenCV, Allure).

### Paso 3: Configurar el emulador o dispositivo real

**Opcion A: Emulador de Android Studio**
1. Abrir Android Studio
2. Tools -> Device Manager -> Create Virtual Device
3. Crear emulador (ej: Pixel 5, Android 13+)
4. Iniciar el emulador
5. Verificar:
```bash
adb devices
# Debe mostrar:
# List of devices attached
# emulator-5554    device
```

**Opcion B: Dispositivo real (Motorola Edge 50 Pro o similar)**
1. Activar "Depuracion USB" en Opciones de desarrollador
2. Conectar via cable USB al PC
3. Verificar:
```bash
adb devices
# Debe mostrar:
# List of devices attached
# ZY22KVZPQ4    device
```

### Paso 4: Descargar e instalar manualmente la APK dummy de DemoBank

La APK de DemoBank es una app standalone mockeada que **no depende de un backend real**.
Debe instalarse manualmente en el dispositivo/emulador antes de ejecutar las pruebas.

1. Obtener el archivo `DemoBank.apk` (APK dummy proporcionada para el reto)
2. Verificar que el dispositivo este conectado:
```bash
adb devices
```
3. Instalar la APK:
```bash
adb install DemoBank.apk
```
4. Verificar que la app se instalo correctamente:
```bash
adb shell pm list packages | findstr demobank
# Salida esperada: package:com.demobank.app
```
5. Abrir la app manualmente para verificar que inicia:
```bash
adb shell am start -n com.demobank.app/.MainActivity
```

> **Nota:** El framework NO instala la APK automaticamente. Usa `setAppPackage` y
> `setAppActivity` en las capabilities, lo que significa que asume que la app
> ya esta instalada en el dispositivo. Si la app no esta instalada, Appium
> lanzara un error `Activity not found`.

### Paso 5: Configurar el dispositivo en el framework

Edita `src/main/resources/config.properties` con el ID de tu dispositivo:

```properties
# Obtener el ID con: adb devices
device.name=TU_ID_AQUI
```

Ejemplo para el dispositivo usado en este proyecto:
```properties
device.name=ZY22KVZPQ4
```

Ejemplo para un emulador:
```properties
device.name=emulator-5554
```

### Paso 6: Iniciar Appium Server

```bash
appium
```

Debes ver:
```
Appium REST http interface listener started on http://127.0.0.1:4723
```

Verificar que el servidor responde:
```bash
curl http://127.0.0.1:4723/status
# Salida esperada: {"value":{"ready":true,...}}
```

---

## Configuracion de Appium Inspector

Appium Inspector permite inspeccionar el arbol de accesibilidad de la app
para obtener localizadores (content-desc, resource-id, XPath).

### Pasos para configurar

1. Abrir Appium Inspector
2. Configurar las siguientes capabilities (iguales a las del `config.properties`):

```json
{
  "platformName": "Android",
  "appium:deviceName": "ZY22KVZPQ4",
  "appium:automationName": "UiAutomator2",
  "appium:appPackage": "com.demobank.app",
  "appium:appActivity": "com.demobank.app.MainActivity",
  "appium:noReset": false,
  "appium:autoGrantPermissions": true
}
```

3. En Appium Inspector:
   - **Host:** `127.0.0.1`
   - **Port:** `4723`
   - **Path:** `/`
4. Hacer clic en **Start Session**
5. La app DemoBank se abrira en el dispositivo y Appium Inspector mostrara el arbol de elementos

### Como usar Appium Inspector para obtener localizadores

1. Navegar por el arbol hasta encontrar el elemento deseado
2. Revisar los atributos del elemento:
   - `content-desc` -> usar como Accessibility ID (prioridad 1)
   - `resource-id` -> usar como ID (prioridad 2)
   - `text` -> usar como XPath `//*[@text='valor']` (prioridad 3)
   - `class` + posicion -> usar como XPath (ultima opcion)
3. Probar el localizador en el campo **Tap** de Appium Inspector antes de agregarlo al codigo

---

## Configuracion del Framework

Toda la configuracion del entorno esta centralizada en:

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
       |
  ConfigReader.java  (lee el .properties una sola vez)
       |
  +---> DriverFactory      (URL Appium, dispositivo, app package/activity)
  +---> TestDataProvider   (credenciales)
  +---> OCRUtils            (ruta tessdata, idioma)
  +---> AllureHelper        (modo de captura de screenshots)
```

---

## Ejecutar los Tests

### Ejecutar toda la suite

Asegurate de que Appium Server este corriendo y el dispositivo conectado:

```bash
appium
```

En otra terminal:

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
mvn test -Dgroups="ocr"
mvn test -Dgroups="opencv"
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
en `config.properties`:

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
| Tamano del reporte | Mayor | Menor |
| Cumple requisito PDF | - | SI |

### Estructura del reporte por test

```
[TEST] testTransferenciaExitosa
  |
  |-- @Step: Paso 1 - Seleccionar primer contacto (Maria Lopez)
  |     |-- Detalle de la accion:
  |     |     ACCION    : Seleccionar
  |     |     ELEMENTO  : Primer contacto de la lista (Maria Lopez)
  |     |     RESULTADO : Contacto seleccionado, pasando a pantalla de monto
  |     |-- Screenshot: Seleccionar - Primer contacto de la lista
  |
  |-- @Step: Paso 2 - Ingresar monto: $100000 y tap Continuar
  |     |-- Detalle de la accion:
  |     |     ACCION    : Escribir
  |     |     ELEMENTO  : Campo de monto (EditText)
  |     |     VALOR ENVIADO: $100000
  |     |     RESULTADO : Monto escrito, ocultando teclado
  |     |-- Screenshot: Escribir - Campo de monto (EditText)
  |
  |-- @Step: Paso 3 - Confirmar transferencia
  |     |-- Screenshot: Tap - Boton 'Confirmar transferencia'
  |
  |-- @Step: Paso 4 - Verificar pantalla de exito
  |     |-- Validacion: Pantalla de exito visible = PASS
  |     |-- Validacion: Titulo contiene 'exitosa' = PASS
  |     |-- Validacion: Monto contiene '100' = PASS
  |     |-- Validacion: Destinatario contiene 'Maria' = PASS
  |     |-- Screenshot: Validacion: Pantalla de exito [PASS]
  |
  |-- @Step: Paso 5 - OCR: validar monto en pantalla de exito mediante Tesseract
  |     |-- Validacion: OCR #2 - Monto (Tesseract) = PASS
  |     |-- Screenshot: Validacion: OCR #2 [PASS]
  |
  |-- @Step: Paso 6 - OpenCV: regresion visual de pantalla de exito
  |     |-- Validacion: OpenCV - Match Score: 97.32% = PASS
  |     |-- Screenshot: Validacion: OpenCV [PASS]
  |
  |-- [SI FALLA] Screenshot: FALLO - testTransferenciaExitosa
  |-- [SI FALLA] Detalles del Fallo (stack trace, excepcion, duracion)
```

> **Nota:** Los screenshots de cada paso solo aparecen en modo "flujo completo".
> En modo "solo fallos", los pasos solo tienen la narrativa textual.

---

## Arquitectura y Reglas

### Patron Page Object Model (POM)

```
Tests (con Asserts)  ->  Pages (con localizadores y acciones)  ->  App
      ^                           ^
   Validan resultados        Interactuan con la app
```

**Reglas:**
- Los **Pages** NUNCA tienen `Assert`.
- Los **Pages** NUNCA tienen logica de reporte (Allure).
- Los **Tests** NUNCA tienen localizadores.
- Los datos fijos estan en `TestDataProvider`.
- La configuracion del entorno esta en `config.properties`.

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

Se usa OCR cuando los selectores nativos no son confiables. Tres validaciones implementadas:

| Caso | Componente | Por que OCR |
|------|-----------|--------------|
| TC05 Logout | Icono de logout (emoji sin content-id) | Tap por coordenadas, OCR valida retorno a Login |
| TC13 Transferencia | Monto en card de exito (texto estilizado) | Componente grafico, OCR valida independiente del arbol |
| TC16 Saldo | Saldo de tarjeta (fondo gradiente) | Fuente personalizada, OCR valida valor visual |

### OpenCV (Comparacion Visual)

- Compara screenshot actual vs baseline.
- Score >= 95% = PASS.
- Si la baseline no existe, se captura automaticamente en la primera ejecucion.
- Baselines en `src/test/resources/baselines/`.

---

## Troubleshooting

### "Cannot connect to Appium Server"
- Verifica que Appium Server este corriendo: `appium`
- Verifica el puerto: `http://127.0.0.1:4723/status`

### "Device not found"
- Verifica con `adb devices`
- Asegurate de que el dispositivo/emulador este conectado
- Si es dispositivo real, verifica que la depuracion USB este activada

### "Activity not found" o la app no se abre
- La APK no esta instalada en el dispositivo
- Instalar manualmente: `adb install DemoBank.apk`
- Verificar: `adb shell pm list packages | findstr demobank`

### "Tesseract not found"
- Verifica la ruta en `config.properties` -> `tesseract.datapath`
- Debe existir: `C:\Program Files\Tesseract-OCR\tessdata\`

### "Baseline image not found" (OpenCV)
- Si la baseline no existe, el framework la captura automaticamente en la primera ejecucion
- Para regenerar baselines manualmente, elimina los archivos `.png` en `src/test/resources/baselines/`
  y vuelve a ejecutar el test

### "No SLF4J providers were found" (warning)
- Este warning ya esta silenciado con la dependencia `slf4j-nop` en el `pom.xml`
- Si aparece, ejecuta `mvn clean install -DskipTests` para refrescar dependencias

### Los tests fallan por flakiness
- Aumenta el timeout en `WaitUtils`: `Duration.ofSeconds(30)`
- Usa `WaitUtils.fluentWait()` en lugar de `waitForVisibility()`
- Verifica que la app no este en un estado inconsistente (reinicia con `adb shell am force-stop com.demobank.app`)
