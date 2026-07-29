# JettraFlux ViewModel Generator

JettraFlux incluye una potente herramienta de CLI integrada a través de `mvn-flux` que permite generar de forma autónoma clases `ViewModel` a partir de `Records` (entidades).

## Uso de la CLI

Para generar un ViewModel a partir de una entidad existente, ejecuta el siguiente comando en la terminal desde la raíz de tu proyecto:

```bash
./mvn-flux -create-code -source-record com.miempresa.proyecto.entity.MiEntidad -model
```

### Parámetros:
- `-create-code`: Indica que el CLI debe entrar en el modo de generación de código.
- `-source-record` (o `-from-record`): Recibe como parámetro la ruta absoluta (Fully Qualified Name) de la clase `record` de la entidad.
- `-model`: Indica que el objetivo es generar una clase ViewModel para la interfaz web.

## Qué hace el generador

Al invocar este comando, JettraFlux analiza sintácticamente el archivo `.java` original del record y genera una nueva clase en el paquete homólogo `.model` (es decir, reemplaza `.entity` por `.model` en la ruta del paquete).

### Características Generadas
1. **Clase y Anotaciones Principales**: Se crea la clase `<NombreRecord>Model` y se anota con:
   ```java
   @JettraViewModel
   @FluxModelToRecordConversor(goal = MiEntidad.class)
   ```
2. **Propiedades de UI**: Cada campo del record incluye:
   ```java
   @PropertiesInRecord
   @PropertiesLabel(value = "mientidad.nombreCampo", label = "Nombrecampo")
   ```
3. **Manejo de Relaciones (Selects y Tablas)**: 
   Si tu record tiene relaciones hacia otros objetos o colecciones, el CLI infiere automáticamente la ruta hacia los *Services* (cambiando `.entity` por `.services`) para poder integrarse con los componentes web de JettraFlux.
   - Para un único objeto referenciado: 
     ```java
     @ViewSelectOne(label = "name", source = "com.miempresa.proyecto.services.EntidadRelacionadaService", method = "findAll")
     ```
   - Para una lista/conjunto de objetos:
     ```java
     @ViewSelectMany(label = "name", source = "com.miempresa.proyecto.services.OtraEntidadService", method = "findAll")
     @TableColumnField(field = "name")
     ```
4. **Reglas Básicas de Validación**: A los tipos de datos requeridos por la vista (como `String`) se les inyecta por defecto la validación `@NotNull` proveniente de `io.jettra.rules.validations.NotNull`.

## Ejemplo de conversión

Si tienes el siguiente record:
```java
package com.example.entity;

public record Person(String name, Department department, List<Role> roles) {}
```

El CLI generará un `PersonModel` ubicado en `com.example.model` con la siguiente estructura:

```java
package com.example.model;

import io.jettra.flux.annotations.JettraViewModel;
import io.jettra.core.flux.FluxModelToRecordConversor;
import io.jettra.flux.annotations.PropertiesInRecord;
import io.jettra.flux.annotations.PropertiesLabel;
// ... imports

@JettraViewModel
@FluxModelToRecordConversor(goal = Person.class)
public class PersonModel {

    @PropertiesInRecord
    @PropertiesLabel(value = "person.name", label = "Name")
    @NotNull
    private String name;

    @PropertiesInRecord
    @PropertiesLabel(value = "person.department", label = "Department")
    @ViewSelectOne(label = "name", source = "com.example.services.DepartmentService", method = "findAll")
    private Department department;

    @PropertiesInRecord
    @PropertiesLabel(value = "person.roles", label = "Roles")
    @ViewSelectMany(label = "name", source = "com.example.services.RoleService", method = "findAll")
    @TableColumnField(field = "name")
    private List<Role> roles;

    // Getters and Setters...
}
```

De esta manera, en apenas unos segundos puedes tener preparada tu vista en el frontend sincronizada perfectamente con el `record` del servidor.
