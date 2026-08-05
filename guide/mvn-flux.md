# Comando `mvn-flux`

El comando `mvn-flux` es la herramienta de línea de comandos integrada en el ecosistema Jettra (ejecutada mediante `io.jettra.server.cli.FluxCLI`). Permite automatizar la creación de código, inicialización de estructuras front-end y, más recientemente, la generación de plugins de temas.

## Nuevo Comando: `-generate-theme-project`

Para facilitar la creación de temas dinámicos que JettraFlux detectará automáticamente a través de la arquitectura de plugins (`theme.json`), puedes utilizar el comando `-generate-theme-project`.

### Sintaxis

```bash
./mvn-flux -generate-theme-project <nombre-proyecto-plugin> -path <path-donde-se-creara el proyecto>  -url-source <url-template-example>
```

### Parámetros

- `<nombre-proyecto-plugin>`: El nombre de tu nuevo proyecto (ej. `SkyRed`). Esto creará una carpeta con el mismo nombre en tu espacio de trabajo.
- `-url-source`: (Opcional) Una URL de referencia que sirvió de inspiración para el diseño (ej. `https://primeui.store/templates/angular/freya`).

### Ejemplo de Uso

```bash
./mvn-flux -generate-theme-project SkyRed -path ~/Descargas -url-source https://primeui.store/templates/angular/freya 
```

Al finalizar la ejecución, este comando creará un proyecto Maven independiente, empaquetado como `jar`, y con la carpeta `src/main/resources/META-INF/` conteniendo el archivo descriptor base **`theme.json`**. 

Luego, solo tendrás que entrar a la carpeta, modificar el `theme.json` para definir tus estilos, y compilar:

```bash
cd SkyRed
mvn clean install
```

Para más detalles sobre la estructura del descriptor de temas, consulta [createplugin.md](createplugin.md).
