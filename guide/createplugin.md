# Creación de Plugins de Temas para JettraFlux

JettraFlux soporta la carga dinámica de temas a través de una arquitectura de plugins descentralizada. Esto significa que puedes crear un proyecto totalmente independiente, compilarlo en un `.jar`, y con tan solo añadirlo como dependencia, JettraFlux lo detectará automáticamente y expondrá tu tema en la UI de la aplicación (por ejemplo, en el menú selector de temas de `ThemeChanged`).

## 1. El Archivo Descriptor (`theme.json`)

El mecanismo central que permite a JettraFlux detectar tu plugin es el archivo **`theme.json`**. Este archivo debe estar ubicado estrictamente en el directorio `src/main/resources/META-INF/theme.json` de tu proyecto.

Al arrancar, `ThemeRegistry` escanea el *classpath* de Java buscando cualquier archivo que coincida con esa ruta.

### Estructura de `theme.json`

El archivo es un JSON simple con pares clave-valor que describen tu tema.
```json
{
  "name": "SkyRed",
  "primary": "#d32f2f",
  "secondary": "#f44336",
  "background": "#ffebee",
  "surface": "#ffffff",
  "onPrimary": "#ffffff",
  "onSurface": "#212121",
  "buttonStyle": "border: none; border-radius: 4px; padding: 10px 20px; font-weight: 500; cursor: pointer; transition: background 0.3s; background-color: #d32f2f; color: #ffffff;",
  "cardStyle": "border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 16px; background-color: #ffffff;",
  "containerStyle": "padding: 16px; border-radius: 4px;",
  "textStyle": "font-size: 16px; color: #212121;",
  "customCss": "/* CSS Global para inyectar en la página */",
  "customJs": "console.log('Tema cargado');"
}
```

> **Nota:** La propiedad `name` es obligatoria y define el identificador del tema. Las propiedades `customCss` y `customJs` te permiten inyectar código de diseño y lógica global en las páginas (por ejemplo, para modificar el `sidebar` o componentes de diseño).

## 2. Crear un Plugin de Tema Usando CLI

Para facilitar el proceso, hemos incorporado un generador dentro del CLI de `JettraAppServer`. 
Desde la raíz de un proyecto de servidor que utilice `JettraAppServer` (o directamente compilando el servidor), ejecuta el siguiente comando para generar un esqueleto de plugin de tema:

```bash
./mvn-flux -generate-theme-project NombreTema -url-source https://url-de-referencia
```
*(Si no tienes un archivo sh creado, puedes invocar la clase `io.jettra.server.cli.FluxCLI`)*

Este comando generará un directorio `NombreTema` con la estructura Maven (`pom.xml`) y el archivo `META-INF/theme.json` pre-creado y enlazado.

## 3. Integración en tu Proyecto Final

Una vez generado y personalizado el tema, compila tu proyecto plugin:

```bash
cd NombreTema
mvn clean install
```

Luego, en el proyecto final (por ejemplo, `FacturaWeb`), agrega la dependencia en el `pom.xml`:

```xml
<dependency>
    <groupId>com.jettra.theme</groupId>
    <artifactId>nombretema</artifactId>
    <version>1.0.0</version>
</dependency>
```

Al levantar tu aplicación `FacturaWeb`, JettraFlux leerá automáticamente el archivo `theme.json` de la nueva dependencia instalada y agregará el tema a las opciones, permitiendo que tu UI lo utilice.
