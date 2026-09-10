# DemoBankAutomation - Framework de Automatización Mobile

Framework de pruebas automatizadas para aplicaciones Android usando **Appium + Java + TestNG**, siguiendo el patrón **Page Object Model (POM)**.

> **Nota:** Este proyecto está configurado para practicar con **MercadoLibre** como app de prueba. El framework está estructurado para migrar fácilmente a **DemoBank** cuando se obtenga el APK.

---

## Tabla de Contenidos

1. [Stack Tecnológico](#stack-tecnológico)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [Prerrequisitos](#prerrequisitos)
4. [Instalación Paso a Paso](#instalación-paso-a-paso)
5. [Configuración](#configuración)
6. [Ejecutar los Tests](#ejecutar-los-tests)
7. [Entender el Código](#entender-el-código)
8. [Migrar a DemoBank](#migrar-a-demobank)

---

## Stack Tecnológico

| Herramienta | Versión | Para qué sirve |
|---|---|---|
| Java JDK | 11+ | Lenguaje de programación |
| Maven | 3.8+ | Gestor de dependencias y build |
| Appium Server | 2.x | Motor de automatización mobile |
| Appium Java Client | 8.6.0 | API Java para Appium |
| Selenium WebDriver | 4.18.1 | API base de automatización |
| TestNG | 7.10.2 | Test runner y aserciones |
| Tess4J (Tesseract OCR) | 5.11.0 | Lectura de texto en imágenes |
| JavaCV (OpenCV) | 1.5.10 | Comparación visual píxel a píxel |
| Allure | 2.29.0 | Reportes HTML interactivos |

---

## Estructura del Proyecto

```
DemoBankAutomation/
├── src/
│   ├── main/java/co/com/demobank/projec/
│   │   ├── pages/                          # Page Objects (POM)
│   │   │   ├── MercadoLibreHomePage.java   # Pantalla principal (búsqueda)
│   │   │   ├── SearchResultsPage.java       # Resultados de búsqueda
│   │   │   ├── ProductDetailsPage.java      # Detalle de producto
│   │   │   ├── CartPage.java                # Carrito de compras
│   │   │   └── ConfirmationPage.java        # Confirmación de compra
│   │   └── utils/                          # Utilidades
│   │       ├── DriverFactory.java           # Crea y gestiona el driver
│   │       ├── WaitUtils.java               # Esperas explícitas
│   │       ├── OCRUtils.java                # Tesseract OCR (lectura de texto)
│   │       ├── ImageMatchUtils.java         # OpenCV (comparación visual)
│   │       └── TestDataProvider.java        # Datos de prueba centralizados
│   └── test/
│       ├── java/tests/
│       │   ├── MercadoLibreTests.java        # 11 casos de prueba
│       │   └── TestListener.java             # Captura screenshots en fallos
│       └── resources/
│           ├── testng.xml                    # Configuración de la suite
│           └── baselines/                    # Imágenes base para OpenCV
```

---

## Prerrequisitos

### 1. Java JDK 11+

Verificar:
```bash
java -version
```

Si no lo tienes, descárgalo de: https://adoptium.net/

### 2. Maven

Verificar:
```bash
mvn -version
```

Si no lo tienes:
1. Descargar de: https://maven.apache.org/download.cgi
2. Extraer en `C:\Program Files\apache-maven-3.9.x\`
3. Agregar al PATH: `C:\Program Files\apache-maven-3.9.x\bin`
4. Configurar variable de entorno `MAVEN_HOME`

### 3. Appium Server 2.x

Instalar via npm:
```bash
npm install -g appium
```

Instalar el driver UiAutomator2:
```bash
appium driver install uiautomator2
```

Verificar:
```bash
appium --version
```

### 4. Android SDK (ADB)

Verificar:
```bash
adb devices
```

Si no lo tienes, instalar Android Studio: https://developer.android.com/studio

### 5. Tesseract OCR

1. Descargar desde: https://github.com/UB-Mannheim/tesseract/wiki
2. Instalar en: `C:\Program Files\Tesseract-OCR\`
3. Para español, descargar `spa.traineddata` y ponerlo en `tessdata/`

### 6. Appium Inspector (opcional pero recomendado)

Descargar desde: https://github.com/appium/appium-inspector/releases

Sirve para inspeccionar la app y obtener los localizadores (IDs, XPaths).

---

## Instalación Paso a Paso

### Paso 1: Clonar o descargar el proyecto

```bash
# Si usas git:
git clone <url-del-repo>

# Si lo tienes como ZIP, descomprimir en D:\reto\DemoBankAutomation
```

### Paso 2: Instalar dependencias de Maven

```bash
cd D:\reto\DemoBankAutomation
mvn clean install -DskipTests
```

Esto descarga todas las dependencias (Appium, Selenium, TestNG, etc.).

### Paso 3: Configurar tu dispositivo

**Opción A: Emulador**
1. Abrir Android Studio → Device Manager → Create Device
2. Crear un emulador (ej: Pixel 5, Android 13)
3. Iniciar el emulador

**Opción B: Dispositivo real**
1. Activar "Depuración USB" en opciones de desarrollador
2. Conectar via cable USB
3. Verificar con `adb devices` (debe aparecer el dispositivo)

### Paso 4: Instalar MercadoLibre en el dispositivo

Descargar e instalar MercadoLibre desde la Play Store del dispositivo/emulador.

### Paso 5: Verificar conexión ADB

```bash
adb devices
# Debe mostrar algo como:
# List of devices attached
# ZY22KVZPQ4    device
```

Copia el ID de tu dispositivo y ponlo en `DriverFactory.java`:
```java
private static final String DEVICE_NAME = "TU_ID_AQUI";
```

### Paso 6: Iniciar Appium Server

```bash
appium
```

Debes ver: `Appium REST http interface listener started on http://127.0.0.1:4723`

---

## Configuración

### DriverFactory.java

Cambia estos valores según tu entorno:

```java
// URL de Appium Server (por defecto)
private static final String SERVER_URL = "http://127.0.0.1:4723/";

// ID de tu dispositivo (de "adb devices")
private static final String DEVICE_NAME = "ZY22KVZPQ4";

// App de prueba (MercadoLibre)
private static final String APP_PACKAGE = "com.mercadolibre";
private static final String APP_ACTIVITY = "com.mercadolibre.navigation.activities.BottomBarActivity";
```

### OCRUtils.java

Verifica la ruta de Tesseract:
```java
tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
```

---

## Ejecutar los Tests

### Ejecutar toda la suite

```bash
mvn test
```

### Ejecutar un grupo específico

```bash
# Solo tests de búsqueda
mvn test -Dgroups="search"

# Solo tests con OCR
mvn test -Dgroups="ocr"

# Solo tests negativos
mvn test -Dgroups="negative"

# Solo tests end-to-end
mvn test -Dgroups="end-to-end"
```

### Generar reporte de Allure

```bash
# 1. Los tests ya generan archivos en allure-results/
# 2. Levantar el servidor de Allure:
allure serve allure-results/
```

Esto abre el navegador con un reporte HTML interactivo.

---

## Entender el Código

### Patrón Page Object Model (POM)

```
Tests (con Asserts)  →  Pages (con localizadores y acciones)  →  App
     ↑                           ↑
  Validan resultados        Interactúan con la app
```

**Regla:** Los Pages NUNCA tienen `Assert`. Los Tests NUNCA tienen localizadores.

### Flujo de un test

```java
@Test
public void testBuscarProductoExitoso() {
    // GIVEN: precondiciones (setUp ya creó el driver y la página)

    // WHEN: acción
    homePage.tapOnSearchBar();
    homePage.typeSearchTerm(TestDataProvider.BUSCAR_XBOX_ONE);
    homePage.confirmSearch();

    // THEN: validación
    resultsPage = new SearchResultsPage(DriverFactory.getDriver());
    Assert.assertTrue(resultsPage.hasResults());
}
```

### Casos de prueba (11 total)

| # | Test | Grupo | Qué valida |
|---|------|-------|------------|
| 1 | testBuscarProductoExitoso | smoke, search | Búsqueda exitosa |
| 2 | testMultiplesBusquedas | data-driven | 5 búsquedas con DataProvider |
| 3 | testBusquedaSinResultados | negative | Empty state |
| 4 | testMultiplesBusquedasSinResultados | negative, data-driven | 3 empty states con DataProvider |
| 5 | testContarResultados | search | Contar productos visibles |
| 6 | testLecturaPrecioPorOCR | ocr | [OCR #1] Precio de resultado |
| 7 | testLecturaPrecioDetallePorOCR | ocr | [OCR #2] Precio de detalle |
| 8 | testRegresionVisualHomeScreen | visual-regression | [OPENCV #1] Regresión visual |
| 9 | testFlujoCompletoBusquedaACarrito | end-to-end | Flujo completo |
| 10 | testLecturaTotalCarritoPorOCR | ocr, end-to-end | [OCR #3] Total carrito |
| 11 | testInicioEnHomeScreen | navigation | Estado inicial |

---

## Migrar a DemoBank

Cuando obtengas el APK de DemoBank, sigue estos pasos:

### 1. Guardar el APK

```
src/test/resources/apps/DemoBank.apk
```

### 2. Actualizar DriverFactory.java

```java
// Cambiar de:
private static final String APP_PACKAGE = "com.mercadolibre";
private static final String APP_ACTIVITY = "com.mercadolibre.navigation.activities.BottomBarActivity";

// A:
private static final String APP_PATH = "src/test/resources/apps/DemoBank.apk";

// Y en createDriver():
UiAutomator2Options options = new UiAutomator2Options()
        .setDeviceName(DEVICE_NAME)
        .setApp(APP_PATH)  // ← Usar setApp en lugar de appPackage/appActivity
        .setAutomationName("UiAutomator2")
        .setNoReset(false)
        .autoGrantPermissions();
```

### 3. Crear Pages de DemoBank

Crear estas clases en `pages/`:

| Clase | Pantalla | Localizadores |
|-------|---------|---------------|
| SplashPage.java | Splash inicial | Esperar que desaparezca |
| LoginPage.java | Login | email, password, toggle, botón login |
| HomePage.java | Home con saldos | tarjetas de cuenta, accesos rápidos, logout |
| MovementsPage.java | Movimientos | buscador, filtros, lista, empty state |
| TransferPage.java | Transferencia (modal 3 pasos) | contacto, monto, confirmar |
| TransferSuccessPage.java | Éxito transferencia | nombre, monto |
| PayPage.java | Pago servicios (modal 3 pasos) | servicio, monto, confirmar |
| PaySuccessPage.java | Éxito pago | nombre, monto |

Usar Appium Inspector para obtener los localizadores reales.

### 4. Crear Tests de DemoBank

Crear estas clases en `tests/`:

| Clase | Casos mínimos |
|-------|--------------|
| LoginTests.java | Login exitoso, email vacío, password vacío, toggle |
| HomeTests.java | Saldo consolidado (OCR), interactividad, accesos, logout |
| MovementsTests.java | Búsqueda, filtro ingresos, filtro gastos, empty state |
| TransferTests.java | Flujo feliz, saldo insuficiente, monto inválido, impacto saldo (OCR), auditoría |
| PayTests.java | Flujo feliz (2 servicios), precarga monto, validaciones, auditoría |

### 5. Actualizar testng.xml

```xml
<test name="Login Tests">
    <classes><class name="tests.LoginTests"/></classes>
</test>
<test name="Home Tests">
    <classes><class name="tests.HomeTests"/></classes>
</test>
<!-- etc. -->
```

### 6. Tomar baseline para OpenCV

```bash
# Con la app abierta en la pantalla de Login:
adb shell screencap -p /sdcard/login.png
adb pull /sdcard/login.png src/test/resources/baselines/login_baseline.png
```

---

## Troubleshooting

### "Cannot connect to Appium Server"
- Verifica que Appium Server esté corriendo: `appium`
- Verifica el puerto: `http://127.0.0.1:4723`

### "Device not found"
- Verifica con `adb devices`
- Asegúrate de que el dispositivo/emulador esté conectado

### "Tesseract not found"
- Verifica la ruta en OCRUtils.java
- Debe existir: `C:\Program Files\Tesseract-OCR\tessdata\`

### "Baseline image not found"
- Crea la carpeta `src/test/resources/baselines/`
- Toma un screenshot de referencia y guárdalo ahí

### Los tests fallan por flakiness
- Aumenta el timeout en WaitUtils: `Duration.ofSeconds(30)`
- Usa `WaitUtils.fluentWait()` en lugar de `waitForVisibility()`
```
