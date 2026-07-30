package io.jettra.flux.cli;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FluxCLI {

    public static void main(String[] args) {
        if (args.length == 0) {
            showHelp();
            return;
        }

        List<String> argList = new ArrayList<>();
        for (String arg : args) {
            argList.add(arg);
        }

        for (String arg : argList) {
            if ("-help".equalsIgnoreCase(arg) || "--help".equalsIgnoreCase(arg) || "help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                showHelp();
                return;
            }
        }

        String command = argList.get(0);
        String sourceRecord = null;
        String sourcePackageRecord = null;
        boolean isModel = false;
        boolean isProperties = false;
        boolean isRest = false;
        boolean isServices = false;
        boolean isConverter = false;
        boolean isPage = false;
        boolean isPageCrud = false;
        boolean isTestRest = false;
        boolean isTestService = false;
        boolean isTestPage = false;
        boolean isRepository = false;
        boolean isController = false;

        for (int i = 0; i < argList.size(); i++) {
            String arg = argList.get(i);
            String nextArg = (i + 1 < argList.size()) ? argList.get(i + 1) : null;

            if ("-source-record".equalsIgnoreCase(arg) || "-from-record".equalsIgnoreCase(arg) || "source-record".equalsIgnoreCase(arg) || "from-record".equalsIgnoreCase(arg)) {
                if (nextArg != null) sourceRecord = nextArg;
            } else if ("-source-package-record".equalsIgnoreCase(arg) || "-from-package-record".equalsIgnoreCase(arg) || "source-package-record".equalsIgnoreCase(arg) || "from-package-record".equalsIgnoreCase(arg)) {
                if (nextArg != null) sourcePackageRecord = nextArg;
            } else if ("-model".equalsIgnoreCase(arg) || "model".equalsIgnoreCase(arg)) {
                isModel = true;
            } else if ("-properties".equalsIgnoreCase(arg) || "properties".equalsIgnoreCase(arg)) {
                isProperties = true;
            } else if ("-rest".equalsIgnoreCase(arg) || "rest".equalsIgnoreCase(arg)) {
                isRest = true;
            } else if ("-services".equalsIgnoreCase(arg) || "services".equalsIgnoreCase(arg)) {
                isServices = true;
            } else if ("-converter".equalsIgnoreCase(arg) || "converter".equalsIgnoreCase(arg)) {
                isConverter = true;
            } else if ("-page".equalsIgnoreCase(arg) || "page".equalsIgnoreCase(arg)) {
                isPage = true;
            } else if ("-page-crud".equalsIgnoreCase(arg) || "page-crud".equalsIgnoreCase(arg)) {
                isPageCrud = true;
            } else if ("-test-rest".equalsIgnoreCase(arg) || "test-rest".equalsIgnoreCase(arg)) {
                isTestRest = true;
            } else if ("-test-service".equalsIgnoreCase(arg) || "test-service".equalsIgnoreCase(arg)) {
                isTestService = true;
            } else if ("-repository".equalsIgnoreCase(arg) || "repository".equalsIgnoreCase(arg)) {
                isRepository = true;
            } else if ("-controller".equalsIgnoreCase(arg) || "controller".equalsIgnoreCase(arg)) {
                isController = true;
            } else if ("-test-page".equalsIgnoreCase(arg) || "test-page".equalsIgnoreCase(arg)) {
                isTestPage = true;
            }
        }

        List<String> sourceRecords = new ArrayList<>();
        if (sourceRecord != null) {
            sourceRecords.add(sourceRecord);
        }
        if (sourcePackageRecord != null) {
            String relativePath = "src/main/java/" + sourcePackageRecord.replace(".", "/");
            Path packagePath = Paths.get(relativePath);
            if (!Files.exists(packagePath) || !Files.isDirectory(packagePath)) {
                System.out.println("Error: Source package directory not found at " + relativePath);
            } else {
                try (java.util.stream.Stream<Path> stream = Files.list(packagePath)) {
                    List<Path> javaFiles = stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                            .sorted()
                            .collect(java.util.stream.Collectors.toList());
                    java.util.regex.Pattern recordPattern = java.util.regex.Pattern.compile("\\brecord\\s+\\w+");
                    for (Path file : javaFiles) {
                        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                        if (recordPattern.matcher(content).find()) {
                            String fileName = file.getFileName().toString();
                            String className = fileName.substring(0, fileName.length() - 5);
                            sourceRecords.add(sourcePackageRecord + "." + className);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error reading package directory: " + e.getMessage());
                }
            }
        }

        switch (command.toLowerCase()) {
            case "-create-code":
            case "create-code":
                if (!sourceRecords.isEmpty()) {
                    for (String rec : sourceRecords) {
                        List<String[]> parsedFields = parseRecordFields(rec);
                        if (parsedFields != null) {
                            if (isModel) {
                                generateViewModel(rec, parsedFields, isProperties);
                            }
                            if (isConverter) {
                                generateConverter(rec, parsedFields);
                            }
                            if (isRest) {
                                generateRestClient(rec, parsedFields);
                            }
                            if (isServices) {
                                generateService(rec, parsedFields);
                            }
                            if (isPage) {
                                generatePage(rec, parsedFields, false);
                            }
                            if (isPageCrud) {
                                generatePage(rec, parsedFields, true);
                            }
                            if (isTestRest) {
                                generateTestRest(rec, parsedFields);
                            }
                            if (isTestService) {
                                generateTestService(rec, parsedFields);
                            }
                            if (isTestPage) {
                                generateTestPage(rec, parsedFields);
                            }
                            if (isRepository) {
                                generateRepository(rec, parsedFields);
                            }
                            if (isController) {
                                generateController(rec, parsedFields);
                            }
                        }
                    }
                } else {
                    System.out.println("Missing arguments. Usage:");
                    System.out.println("  ./mvn-flux -create-code -source-record <Paquete.Record> -model [-properties] [-converter] [-rest] [-services] [-page] [-page-crud] [-test-rest] [-test-service] [-test-page] [-repository] [-controller]");
                    System.out.println("  ./mvn-flux -create-code -source-package-record <Paquete> -model [-properties] [-converter] [-rest] [-services] [-page] [-page-crud] [-test-rest] [-test-service] [-test-page] [-repository] [-controller]");
                }
                break;
            default:
                System.out.println("Unknown command: " + command);
                System.out.println();
                showHelp();
        }
    }

    private static void showHelp() {
        System.out.println("====================================================================================================");
        System.out.println("                            JettraFlux CLI - Menú de Ayuda");
        System.out.println("====================================================================================================");
        System.out.println("Uso: ./mvn-flux <comando> [parámetros/opciones]\n");
        System.out.println("COMANDOS DISPONIBLES:");
        System.out.println("  -create-code           Genera código fuente automáticamente a partir de entidades (records).");
        System.out.println("  -help                  Muestra este menú de ayuda explicativo en la consola.\n");
        System.out.println("PARÁMETROS Y OPCIONES PARA -create-code:");
        System.out.println("  -source-record <FQN>            Especifica la ruta completa (Fully Qualified Name) de un record.");
        System.out.println("                                  (Ejemplo: com.example.entity.Person)");
        System.out.println("                                  Alias: -from-record, source-record, from-record\n");
        System.out.println("  -source-package-record <Pkg>    Especifica el paquete para procesar masivamente todos sus records.");
        System.out.println("                                  (Ejemplo: com.example.entity)");
        System.out.println("                                  Alias: -from-package-record, source-package-record, from-package-record\n");
        System.out.println("  -model                          [Requerido] Genera la clase ViewModel (<Nombre>Model.java).");
        System.out.println("  -properties                     Escanea y actualiza los archivos messages*.properties con las etiquetas.");
        System.out.println("  -converter                      Genera la clase conversora (<Nombre>ModelConverter.java).");
        System.out.println("  -rest                           Genera la interfaz cliente REST (<Nombre>RestClient.java).");
        System.out.println("  -services                       Genera la clase de servicio (<Nombre>Service.java).");
        System.out.println("  -page                           Genera la página básica (<Nombre>Page.java).");
        System.out.println("  -page-crud                      Genera la página CRUD completa (<Nombre>CrudPage.java).");
        System.out.println("  -test-rest                      Genera las pruebas para los clientes REST.");
        System.out.println("  -test-service                   Genera las pruebas para los servicios.");
        System.out.println("  -test-page                      Genera las pruebas para las páginas.");
        System.out.println("  -repository                     Genera el repositorio (Interface e Impl) para un record.");
        System.out.println("  -controller                     Genera el controlador REST para un record.\n");
        System.out.println("EJEMPLOS DE USO:");
        System.out.println("  1. Por un Record individual:");
        System.out.println("     ./mvn-flux -create-code -source-record com.example.entity.Person -model -properties -converter -rest -services\n");
        System.out.println("  2. Por un paquete completo de Records:");
        System.out.println("     ./mvn-flux -create-code -source-package-record com.example.entity -model -properties -converter -rest -services");
        System.out.println("====================================================================================================");
    }

    private static List<String> extractRecordImports(String sourceRecord, List<String[]> parsedFields) {
        List<String> imports = new ArrayList<>();
        int lastDot = sourceRecord.lastIndexOf('.');
        String originalPackage = (lastDot != -1) ? sourceRecord.substring(0, lastDot) : "";

        try {
            String relativePath = "src/main/java/" + sourceRecord.replace(".", "/") + ".java";
            Path recordPath = Paths.get(relativePath);
            if (Files.exists(recordPath)) {
                String content = new String(Files.readAllBytes(recordPath), StandardCharsets.UTF_8);
                java.util.regex.Matcher importMatcher = java.util.regex.Pattern.compile("import\\s+([^;]+);").matcher(content);
                while (importMatcher.find()) {
                    String imp = importMatcher.group(1).trim();
                    if (!imports.contains(imp)) {
                        imports.add(imp);
                    }
                }
            }
        } catch (Exception ignored) {}

        if (parsedFields != null) {
            for (String[] field : parsedFields) {
                String type = field[0];
                String coreType = type;
                if (type.startsWith("List<") || type.startsWith("Set<") || type.startsWith("Collection<")) {
                    coreType = type.substring(type.indexOf('<') + 1, type.indexOf('>')).trim();
                }
                if (coreType.contains("<")) {
                    coreType = coreType.substring(0, coreType.indexOf('<')).trim();
                }

                addStandardImport(imports, coreType, originalPackage);
            }
        }

        return imports;
    }

    private static boolean isBasicType(String type) {
        if (type == null) return false;
        if (type.startsWith("List<") || type.startsWith("Set<") || type.startsWith("Collection<") || type.startsWith("Map<")) {
            return false;
        }
        return type.equals("String") || type.equals("Character") || type.equals("char")
            || type.equals("Integer") || type.equals("int")
            || type.equals("Long") || type.equals("long")
            || type.equals("Double") || type.equals("double")
            || type.equals("Float") || type.equals("float")
            || type.equals("Short") || type.equals("short")
            || type.equals("Byte") || type.equals("byte")
            || type.equals("Boolean") || type.equals("boolean")
            || type.equals("BigDecimal") || type.equals("BigInteger")
            || type.equals("UUID")
            || type.equals("LocalDate") || type.equals("LocalDateTime") || type.equals("LocalTime")
            || type.equals("ZonedDateTime") || type.equals("OffsetDateTime") || type.equals("Instant")
            || type.equals("Date");
    }

    private static void addStandardImport(List<String> imports, String type, String originalPackage) {
        String imp = null;
        switch (type) {
            case "LocalDate":
                imp = "java.time.LocalDate";
                break;
            case "LocalDateTime":
                imp = "java.time.LocalDateTime";
                break;
            case "LocalTime":
                imp = "java.time.LocalTime";
                break;
            case "ZonedDateTime":
                imp = "java.time.ZonedDateTime";
                break;
            case "OffsetDateTime":
                imp = "java.time.OffsetDateTime";
                break;
            case "Instant":
                imp = "java.time.Instant";
                break;
            case "Date":
                imp = "java.util.Date";
                break;
            case "UUID":
                imp = "java.util.UUID";
                break;
            case "BigDecimal":
                imp = "java.math.BigDecimal";
                break;
            case "BigInteger":
                imp = "java.math.BigInteger";
                break;
            case "Set":
                imp = "java.util.Set";
                break;
            case "List":
                imp = "java.util.List";
                break;
            case "Map":
                imp = "java.util.Map";
                break;
            default:
                if (!isBasicType(type) && !originalPackage.isEmpty()) {
                    boolean exists = false;
                    for (String existing : imports) {
                        if (existing.endsWith("." + type)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        imp = originalPackage + "." + type;
                    }
                }
                break;
        }

        if (imp != null && !imports.contains(imp)) {
            imports.add(imp);
        }
    }

    private static List<String[]> parseRecordFields(String sourceRecord) {
        try {
            String relativePath = "src/main/java/" + sourceRecord.replace(".", "/") + ".java";
            Path recordPath = Paths.get(relativePath);
            if (!Files.exists(recordPath)) {
                System.out.println("Error: Source record file not found at " + relativePath);
                return null;
            }
            String content = new String(Files.readAllBytes(recordPath), StandardCharsets.UTF_8);
            int recordIdx = content.indexOf("record ");
            if (recordIdx == -1) return null;
            int nameStart = recordIdx + "record ".length();
            int parenStart = content.indexOf("(", nameStart);
            if (parenStart == -1) return null;
            int parenEnd = -1;
            int openCount = 0;
            for (int i = parenStart; i < content.length(); i++) {
                if (content.charAt(i) == '(') openCount++;
                else if (content.charAt(i) == ')') {
                    openCount--;
                    if (openCount == 0) { parenEnd = i; break; }
                }
            }
            if (parenEnd == -1) return null;
            String fieldsContent = content.substring(parenStart + 1, parenEnd).trim();
            List<String[]> parsedFields = new ArrayList<>();
            if (!fieldsContent.isEmpty()) {
                List<String> rawFieldsList = new ArrayList<>();
                StringBuilder currentField = new StringBuilder();
                int openParen = 0;
                for (int i = 0; i < fieldsContent.length(); i++) {
                    char c = fieldsContent.charAt(i);
                    if (c == '(') openParen++;
                    else if (c == ')') openParen--;
                    if (c == ',' && openParen == 0) {
                        rawFieldsList.add(currentField.toString());
                        currentField.setLength(0);
                    } else { currentField.append(c); }
                }
                if (currentField.length() > 0) rawFieldsList.add(currentField.toString());
                for (String rawField : rawFieldsList) {
                    rawField = rawField.trim().replaceAll("@\\w+(?:\\([^)]*\\))?\\s+", "");
                    String[] parts = rawField.split("\\s+");
                    if (parts.length >= 2) {
                        String type = parts[parts.length - 2];
                        String name = parts[parts.length - 1];
                        parsedFields.add(new String[]{type, name});
                    }
                }
            }
            return parsedFields;
        } catch (Exception e) { return null; }
    }

    private static void generateViewModel(String sourceRecord, List<String[]> parsedFields, boolean generateProperties) {
        try {
            String relativePath = "src/main/java/" + sourceRecord.replace(".", "/") + ".java";
            Path recordPath = Paths.get(relativePath);
            if (!Files.exists(recordPath)) return;
            String content = new String(Files.readAllBytes(recordPath), StandardCharsets.UTF_8);
            String originalPackage = "";
            java.util.regex.Matcher pkgMatcher = java.util.regex.Pattern.compile("package\\s+([^;]+);").matcher(content);
            if (pkgMatcher.find()) originalPackage = pkgMatcher.group(1).trim();
            int recordIdx = content.indexOf("record ");
            int nameStart = recordIdx + "record ".length();
            int parenStart = content.indexOf("(", nameStart);
            String recordName = content.substring(nameStart, parenStart).trim();
            if (recordName.contains("<")) recordName = recordName.substring(0, recordName.indexOf("<")).trim();
            String modelPackage = originalPackage.replace(".entity", ".model");
            String modelClassName = recordName + "Model";
            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(modelPackage).append(";\n\n");
            
            // Generate imports
            sb.append("import ").append(sourceRecord).append(";\n");
            sb.append("import io.jettra.flux.annotations.JettraViewModel;\n");
            sb.append("import io.jettra.flux.annotations.PropertiesInRecord;\n");
            sb.append("import io.jettra.flux.annotations.PropertiesLabel;\n");
            sb.append("import io.jettra.flux.annotations.ViewSelectOne;\n");
            sb.append("import io.jettra.flux.annotations.ViewSelectMany;\n");
            sb.append("import io.jettra.flux.annotations.TableColumnField;\n");
            sb.append("import io.jettra.rules.validations.NotNull;\n");
            
            List<String> existingImports = new ArrayList<>();
            java.util.regex.Matcher importMatcher = java.util.regex.Pattern.compile("import\\s+([^;]+);").matcher(content);
            while (importMatcher.find()) {
                String imp = importMatcher.group(1).trim();
                existingImports.add(imp);
                sb.append("import ").append(imp).append(";\n");
            }

            List<String> recordImports = extractRecordImports(sourceRecord, parsedFields);
            for (String imp : recordImports) {
                if (!imp.equals(sourceRecord) && !imp.startsWith("io.jettra.flux.") && !imp.startsWith("io.jettra.rules.")) {
                    boolean exists = false;
                    for (String existing : existingImports) {
                        if (existing.equals(imp)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        sb.append("import ").append(imp).append(";\n");
                        existingImports.add(imp);
                    }
                }
            }
            
            // Identify referenced entities and add imports for them
            for (String[] field : parsedFields) {
                String type = field[0];
                String entityType = type;
                if (type.startsWith("List<") || type.startsWith("Set<") || type.startsWith("Collection<")) {
                    entityType = type.substring(type.indexOf('<') + 1, type.indexOf('>'));
                }
                
                if (!isBasicType(entityType)) {
                    boolean alreadyImported = false;
                    for (String imp : existingImports) {
                        if (imp.endsWith("." + entityType)) {
                            alreadyImported = true;
                            break;
                        }
                    }
                    if (!alreadyImported) {
                        sb.append("import ").append(originalPackage).append(".").append(entityType).append(";\n");
                        existingImports.add(originalPackage + "." + entityType);
                    }
                }
            }
            sb.append("\n");

            sb.append("@JettraViewModel\n");
            sb.append("public class ").append(modelClassName).append(" {\n\n");

            for (String[] field : parsedFields) {
                String type = field[0];
                String name = field[1];
                
                sb.append("    @PropertiesInRecord\n");
                sb.append("    @PropertiesLabel(value = \"").append(recordName.toLowerCase()).append(".").append(name).append("\", label = \"").append(capitalize(name)).append("\")\n");
                
                if (isBasicType(type)) {
                    if (type.equals("String")) {
                        sb.append("    @NotNull\n");
                    }
                } else if (type.startsWith("List<") || type.startsWith("Set<") || type.startsWith("Collection<")) {
                    String genericType = type.substring(type.indexOf('<') + 1, type.indexOf('>'));
                    String servicePkg = originalPackage.replace(".entity", ".services");
                    String serviceFqn = servicePkg + "." + genericType + "Service";
                    sb.append("    @ViewSelectMany(label = \"name\", source = \"").append(serviceFqn).append("\", method = \"findAll\")\n");
                    sb.append("    @TableColumnField(field = \"name\")\n");
                } else {
                    String servicePkg = originalPackage.replace(".entity", ".services");
                    String serviceFqn = servicePkg + "." + type + "Service";
                    sb.append("    @ViewSelectOne(label = \"name\", source = \"").append(serviceFqn).append("\", method = \"findAll\")\n");
                }
                
                sb.append("    private ").append(type).append(" ").append(name).append(";\n\n");
            }

            sb.append("    public ").append(modelClassName).append("() {}\n\n");

            for (String[] field : parsedFields) {
                String type = field[0];
                String name = field[1];
                String capName = capitalize(name);

                sb.append("    public ").append(type).append(" get").append(capName).append("() {\n");
                sb.append("        return ").append(name).append(";\n");
                sb.append("    }\n\n");

                sb.append("    public void set").append(capName).append("(").append(type).append(" ").append(name).append(") {\n");
                sb.append("        this.").append(name).append(" = ").append(name).append(";\n");
                sb.append("    }\n\n");
            }
            
            sb.append("}\n");

            String outputRelativePath = "src/main/java/" + modelPackage.replace(".", "/") + "/" + modelClassName + ".java";
            Path outputPath = Paths.get(outputRelativePath);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            
            System.out.println("Generated ViewModel: " + outputRelativePath);

            if (generateProperties) {
                updatePropertiesFiles(recordName, parsedFields);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to generate ViewModel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void updatePropertiesFiles(String recordName, List<String[]> parsedFields) {
        try {
            Path resourcesDir = Paths.get("src/main/resources");
            if (!Files.exists(resourcesDir)) {
                System.out.println("Directory src/main/resources does not exist. Skipping properties generation.");
                return;
            }

            Files.list(resourcesDir)
                 .filter(p -> p.getFileName().toString().startsWith("messages") && p.getFileName().toString().endsWith(".properties"))
                 .forEach(p -> appendProperties(p, recordName, parsedFields));

        } catch (Exception e) {
            System.err.println("Failed to update properties files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void appendProperties(Path propFile, String recordName, List<String[]> parsedFields) {
        try {
            List<String> lines = Files.readAllLines(propFile, StandardCharsets.UTF_8);
            boolean modified = false;
            String prefix = recordName.toLowerCase() + ".";

            for (String[] field : parsedFields) {
                String name = field[1];
                String propKey = prefix + name;
                String propValue = capitalize(name);

                boolean exists = false;
                for (String line : lines) {
                    if (line.trim().startsWith(propKey + "=") || line.trim().startsWith(propKey + " =")) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    lines.add(propKey + " = " + propValue);
                    modified = true;
                }
            }

            if (modified) {
                Files.write(propFile, lines, StandardCharsets.UTF_8);
                System.out.println("Updated properties file: " + propFile.toString());
            }

        } catch (Exception e) {
            System.err.println("Failed to write to properties file " + propFile.toString() + ": " + e.getMessage());
        }
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private static void generateRestClient(String sourceRecord, List<String[]> parsedFields) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);

            String restClientPackage = originalPackage.replace(".entity", ".restclient");
            String clientClassName = recordName + "RestClient";

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(restClientPackage).append(";\n\n");
            
            sb.append("import ").append(sourceRecord).append(";\n");
            sb.append("import io.jettra.rest.annotations.DELETE;\n");
            sb.append("import io.jettra.rest.annotations.GET;\n");
            sb.append("import io.jettra.rest.annotations.POST;\n");
            sb.append("import io.jettra.rest.annotations.PUT;\n");
            sb.append("import io.jettra.rest.annotations.Path;\n");
            sb.append("import io.jettra.rest.annotations.PathParam;\n");
            sb.append("import io.jettra.rest.client.RestClient;\n");
            sb.append("import java.util.List;\n");

            List<String> recordImports = extractRecordImports(sourceRecord, parsedFields);
            for (String imp : recordImports) {
                if (!imp.startsWith("io.jettra.rest.") && !imp.equals("java.util.List") && !imp.equals(sourceRecord)) {
                    sb.append("import ").append(imp).append(";\n");
                }
            }
            sb.append("\n");

            String baseUriModule = "api";
            String[] pkgParts = originalPackage.split("\\.");
            if (pkgParts.length > 2) {
                baseUriModule = pkgParts[pkgParts.length - 2];
            }
            String pluralEndpoint = "/api/" + baseUriModule + "/" + recordName.toLowerCase() + "s";

            sb.append("@RestClient(baseUri = \"").append(pluralEndpoint).append("\")\n");
            sb.append("public interface ").append(clientClassName).append(" {\n\n");

            sb.append("    @GET\n");
            sb.append("    List<").append(recordName).append("> findAll();\n\n");

            sb.append("    @POST\n");
            sb.append("    void save(").append(recordName).append(" ").append(recordName.toLowerCase()).append(");\n\n");

            sb.append("    @PUT\n");
            sb.append("    void update(").append(recordName).append(" ").append(recordName.toLowerCase()).append(");\n\n");

            sb.append("    @DELETE\n");
            sb.append("    @Path(\"/{id}\")\n");
            sb.append("    void delete(@PathParam(\"id\") String id);\n");
            
            for (String[] field : parsedFields) {
                String type = field[0];
                String name = field[1];
                sb.append("\n    @GET\n");
                sb.append("    @Path(\"/").append(name.toLowerCase()).append("/{").append(name).append("}\")\n");
                sb.append("    List<").append(recordName).append("> findBy").append(capitalize(name)).append("(@PathParam(\"").append(name).append("\") ").append(type).append(" ").append(name).append(");\n");
            }
            
            sb.append("}\n");

            String outputRelativePath = "src/main/java/" + restClientPackage.replace(".", "/") + "/" + clientClassName + ".java";
            Path outputPath = Paths.get(outputRelativePath);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            
            System.out.println("Generated RestClient: " + outputRelativePath);

        } catch (Exception e) {
            System.err.println("Failed to generate RestClient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateService(String sourceRecord, List<String[]> parsedFields) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);

            String servicePackage = originalPackage.replace(".entity", ".services");
            String serviceClassName = recordName + "Service";
            
            String restClientPackage = originalPackage.replace(".entity", ".restclient");
            String clientClassName = recordName + "RestClient";

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(servicePackage).append(";\n\n");
            
            sb.append("import ").append(sourceRecord).append(";\n");
            sb.append("import ").append(restClientPackage).append(".").append(clientClassName).append(";\n");
            sb.append("import io.jettra.core.inject.annotation.Inject;\n");
            sb.append("import java.util.List;\n");

            List<String> recordImports = extractRecordImports(sourceRecord, parsedFields);
            for (String imp : recordImports) {
                if (!imp.equals(sourceRecord) && !imp.equals("java.util.List") && !imp.startsWith("io.jettra.core.")) {
                    sb.append("import ").append(imp).append(";\n");
                }
            }
            sb.append("\n");

            sb.append("public class ").append(serviceClassName).append(" {\n\n");

            sb.append("    @Inject\n");
            sb.append("    private ").append(clientClassName).append(" client;\n\n");

            sb.append("    public List<").append(recordName).append("> findAll() {\n");
            sb.append("        List<").append(recordName).append("> records = client.findAll();\n");
            sb.append("        if (records == null) return List.of();\n");
            sb.append("        return records;\n");
            sb.append("    }\n\n");

            sb.append("    public void save(").append(recordName).append(" record) {\n");
            sb.append("        client.save(record);\n");
            sb.append("    }\n\n");

            sb.append("    public void delete(String id) {\n");
            sb.append("        client.delete(id);\n");
            sb.append("    }\n");

            if (parsedFields != null) {
                for (String[] field : parsedFields) {
                    String type = field[0];
                    String name = field[1];
                    sb.append("\n    public List<").append(recordName).append("> findBy").append(capitalize(name)).append("(").append(type).append(" ").append(name).append(") {\n");
                    sb.append("        List<").append(recordName).append("> records = client.findBy").append(capitalize(name)).append("(").append(name).append(");\n");
                    sb.append("        if (records == null) return List.of();\n");
                    sb.append("        return records;\n");
                    sb.append("    }\n");
                }
            }

            sb.append("}\n");

            String outputRelativePath = "src/main/java/" + servicePackage.replace(".", "/") + "/" + serviceClassName + ".java";
            Path outputPath = Paths.get(outputRelativePath);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            
            System.out.println("Generated Service: " + outputRelativePath);

        } catch (Exception e) {
            System.err.println("Failed to generate Service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateConverter(String sourceRecord, List<String[]> parsedFields) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);

            String converterPackage = originalPackage.replace(".entity", ".converter");
            String converterClassName = recordName + "ModelConverter";
            String modelPackage = originalPackage.replace(".entity", ".model");
            String modelClassName = recordName + "Model";

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(converterPackage).append(";\n\n");
            
            sb.append("import ").append(sourceRecord).append(";\n");
            sb.append("import ").append(modelPackage).append(".").append(modelClassName).append(";\n");
            sb.append("import io.jettra.scoped.ApplicationScoped;\n");

            List<String> recordImports = extractRecordImports(sourceRecord, parsedFields);
            for (String imp : recordImports) {
                if (!imp.equals(sourceRecord) && !imp.startsWith("io.jettra.scoped.")) {
                    sb.append("import ").append(imp).append(";\n");
                }
            }
            sb.append("\n");

            sb.append("@ApplicationScoped\n");
            sb.append("public class ").append(converterClassName).append(" {\n");
            
            sb.append("  public ").append(modelClassName).append(" toModel(").append(recordName).append(" record) {\n");
            sb.append("    if (record == null) {\n      return null;\n    }\n");
            sb.append("    ").append(modelClassName).append(" model = new ").append(modelClassName).append("();\n");
            for (String[] field : parsedFields) {
                String name = field[1];
                sb.append("    model.set").append(capitalize(name)).append("(record.").append(name).append("());\n");
            }
            sb.append("    return model;\n");
            sb.append("  }\n\n");
            
            sb.append("  public ").append(recordName).append(" toRecord(").append(modelClassName).append(" model) {\n");
            sb.append("    if (model == null) {\n      return null;\n    }\n");
            sb.append("    return new ").append(recordName).append("(\n");
            for (int i = 0; i < parsedFields.size(); i++) {
                String name = parsedFields.get(i)[1];
                sb.append("          model.get").append(capitalize(name)).append("()");
                if (i < parsedFields.size() - 1) {
                    sb.append(",");
                }
                sb.append("\n");
            }
            sb.append("        );\n");
            sb.append("  }\n");
            sb.append("}\n");

            String outputRelativePath = "src/main/java/" + converterPackage.replace(".", "/") + "/" + converterClassName + ".java";
            Path outputPath = Paths.get(outputRelativePath);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            
            System.out.println("Generated Converter: " + outputRelativePath);

        } catch (Exception e) {
            System.err.println("Failed to generate Converter: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static void generatePage(String sourceRecord, List<String[]> parsedFields, boolean isCrud) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);

            String pagePackage = originalPackage.replace(".entity", ".pages");
            String pageClassName = recordName + (isCrud ? "CrudPage" : "Page");

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(pagePackage).append(";\n\n");
            
            sb.append("import ").append(sourceRecord).append(";\n");
            sb.append("import io.jettra.flux.widgets.*;\n");
            sb.append("import io.jettra.core.server.Page;\n");
            sb.append("import io.jettra.flux.core.Widget;\n");
            sb.append("import com.sun.net.httpserver.HttpExchange;\n");
            sb.append("import java.util.Map;\n\n");

            sb.append("@Page(path = \"/").append(recordName.toLowerCase()).append(isCrud ? "-crud" : "").append("\")\n");
            sb.append("public class ").append(pageClassName).append(" {\n\n");
            
            sb.append("    protected Widget buildCenter(HttpExchange exchange, Map<String, String> params, String currentTheme) {\n");
            
            if (isCrud) {
                sb.append("        return Column.of(Header.of(2, \"Gestión de ").append(recordName).append("\"),\n");
                sb.append("                Paragraph.of(\"CRUD autogenerado para ").append(recordName).append("\")\n");
                sb.append("        );\n");
            } else {
                sb.append("        return Column.of(Header.of(2, \"Página ").append(recordName).append("\"),\n");
                sb.append("                Paragraph.of(\"Página básica autogenerada para ").append(recordName).append("\")\n");
                sb.append("        );\n");
            }
            
            sb.append("    }\n");
            sb.append("}\n");

            String outputRelativePath = "src/main/java/" + pagePackage.replace(".", "/") + "/" + pageClassName + ".java";
            Path outputPath = Paths.get(outputRelativePath);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated Page: " + outputRelativePath);
        } catch (Exception e) {
            System.err.println("Failed to generate Page: " + e.getMessage());
        }
    }

    private static void generateTestRest(String sourceRecord, List<String[]> parsedFields) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);

            String testPackage = originalPackage.replace(".entity", ".restclient");
            String testClassName = recordName + "RestClientTest";

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(testPackage).append(";\n\n");
            sb.append("public class ").append(testClassName).append(" {\n");
            sb.append("    // TODO: Implement REST tests for ").append(recordName).append("\n");
            sb.append("}\n");

            String outputRelativePath = "src/test/java/" + testPackage.replace(".", "/") + "/" + testClassName + ".java";
            Path outputPath = Paths.get(outputRelativePath);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated Test Rest: " + outputRelativePath);
        } catch (Exception e) {
            System.err.println("Failed to generate Test Rest: " + e.getMessage());
        }
    }

    private static void generateTestService(String sourceRecord, List<String[]> parsedFields) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);

            String testPackage = originalPackage.replace(".entity", ".services");
            String testClassName = recordName + "ServiceTest";

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(testPackage).append(";\n\n");
            sb.append("public class ").append(testClassName).append(" {\n");
            sb.append("    // TODO: Implement Service tests for ").append(recordName).append("\n");
            sb.append("}\n");

            String outputRelativePath = "src/test/java/" + testPackage.replace(".", "/") + "/" + testClassName + ".java";
            Path outputPath = Paths.get(outputRelativePath);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated Test Service: " + outputRelativePath);
        } catch (Exception e) {
            System.err.println("Failed to generate Test Service: " + e.getMessage());
        }
    }

    private static void generateTestPage(String sourceRecord, List<String[]> parsedFields) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);

            String testPackage = originalPackage.replace(".entity", ".pages");
            String testClassName = recordName + "PageTest";

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(testPackage).append(";\n\n");
            sb.append("public class ").append(testClassName).append(" {\n");
            sb.append("    // TODO: Implement Page tests for ").append(recordName).append("\n");
            sb.append("}\n");

            String outputRelativePath = "src/test/java/" + testPackage.replace(".", "/") + "/" + testClassName + ".java";
            Path outputPath = Paths.get(outputRelativePath);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated Test Page: " + outputRelativePath);
        } catch (Exception e) {
            System.err.println("Failed to generate Test Page: " + e.getMessage());
        }
    }


    private static void generateRepository(String sourceRecord, List<String[]> parsedFields) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);
            String repoPackage = originalPackage.replace(".entity", ".repository");
            String repoName = recordName + "Repository";
            String repoImplName = recordName + "RepositoryImpl";

            String idType = "String";
            if (parsedFields != null && !parsedFields.isEmpty()) {
                String potentialIdType = parsedFields.get(0)[0];
                for (String[] field : parsedFields) {
                    if (field[1].toLowerCase().equals("id")) {
                        idType = field[0];
                        break;
                    }
                }
                if (idType.equals("String")) {
                    idType = potentialIdType;
                }
            }

            // Interface
            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(repoPackage).append(";\n\n");
            sb.append("import ").append(sourceRecord).append(";\n");
            sb.append("import java.util.List;\n");
            sb.append("import java.util.Optional;\n\n");
            sb.append("public interface ").append(repoName).append(" {\n");
            sb.append("    List<").append(recordName).append("> findAll();\n");
            sb.append("    List<").append(recordName).append("> findAll(int page, int size);\n");
            sb.append("    void save(").append(recordName).append(" record);\n");
            sb.append("    void delete(").append(idType).append(" id);\n");
            sb.append("    Optional<").append(recordName).append("> findById(").append(idType).append(" id);\n");
            
            if (parsedFields != null) {
                for (String[] field : parsedFields) {
                    String type = field[0];
                    String name = field[1];
                    sb.append("    List<").append(recordName).append("> findBy").append(capitalize(name)).append("(").append(type).append(" ").append(name).append(");\n");
                }
            }
            sb.append("}\n");

            Path outPath = Paths.get("src/main/java/" + repoPackage.replace(".", "/") + "/" + repoName + ".java");
            Files.createDirectories(outPath.getParent());
            Files.write(outPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated Repository Interface: " + outPath.toString());

            // Implementation
            StringBuilder sbi = new StringBuilder();
            sbi.append("package ").append(repoPackage).append(";\n\n");
            sbi.append("import ").append(sourceRecord).append(";\n");
            sbi.append("import io.jettra.scoped.ApplicationScoped;\n");
            sbi.append("import java.util.ArrayList;\n");
            sbi.append("import java.util.List;\n");
            sbi.append("import java.util.Optional;\n");
            sbi.append("import java.util.stream.Collectors;\n\n");
            sbi.append("@ApplicationScoped\n");
            sbi.append("public class ").append(repoImplName).append(" implements ").append(repoName).append(" {\n\n");
            sbi.append("    private static final List<").append(recordName).append("> db = new ArrayList<>();\n\n");
            sbi.append("    @Override\n");
            sbi.append("    public List<").append(recordName).append("> findAll() {\n");
            sbi.append("        return new ArrayList<>(db);\n");
            sbi.append("    }\n\n");
            sbi.append("    @Override\n");
            sbi.append("    public List<").append(recordName).append("> findAll(int page, int size) {\n");
            sbi.append("        int from = (page - 1) * size;\n");
            sbi.append("        if (from >= db.size()) return new ArrayList<>();\n");
            sbi.append("        int to = Math.min(from + size, db.size());\n");
            sbi.append("        return new ArrayList<>(db.subList(from, to));\n");
            sbi.append("    }\n\n");
            
            String idField = parsedFields != null && !parsedFields.isEmpty() ? parsedFields.get(0)[1] : "id";
            for (String[] field : parsedFields) {
                if (field[1].toLowerCase().equals("id")) {
                    idField = field[1];
                    break;
                }
            }

            sbi.append("    @Override\n");
            sbi.append("    public void save(").append(recordName).append(" record) {\n");
            sbi.append("        delete(record.").append(idField).append("());\n");
            sbi.append("        db.add(record);\n");
            sbi.append("    }\n\n");

            sbi.append("    @Override\n");
            sbi.append("    public void delete(").append(idType).append(" id) {\n");
            sbi.append("        db.removeIf(r -> r.").append(idField).append("().equals(id));\n");
            sbi.append("    }\n\n");

            sbi.append("    @Override\n");
            sbi.append("    public Optional<").append(recordName).append("> findById(").append(idType).append(" id) {\n");
            sbi.append("        return db.stream().filter(r -> r.").append(idField).append("().equals(id)).findFirst();\n");
            sbi.append("    }\n");

            if (parsedFields != null) {
                for (String[] field : parsedFields) {
                    String type = field[0];
                    String name = field[1];
                    sbi.append("\n    @Override\n");
                    sbi.append("    public List<").append(recordName).append("> findBy").append(capitalize(name)).append("(").append(type).append(" ").append(name).append(") {\n");
                    sbi.append("        return db.stream().filter(r -> r.").append(name).append("().equals(").append(name).append(")).collect(Collectors.toList());\n");
                    sbi.append("    }\n");
                }
            }
            sbi.append("}\n");

            Path outImplPath = Paths.get("src/main/java/" + repoPackage.replace(".", "/") + "/" + repoImplName + ".java");
            Files.write(outImplPath, sbi.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated Repository Implementation: " + outImplPath.toString());
            
        } catch (Exception e) {
            System.err.println("Failed to generate Repository: " + e.getMessage());
        }
    }

    private static void generateController(String sourceRecord, List<String[]> parsedFields) {
        try {
            int lastDot = sourceRecord.lastIndexOf('.');
            String originalPackage = sourceRecord.substring(0, lastDot);
            String recordName = sourceRecord.substring(lastDot + 1);
            
            String controllerPackage = originalPackage.replace(".entity", ".controller");
            String controllerName = recordName + "Controller";
            String repoPackage = originalPackage.replace(".entity", ".repository");
            String repoName = recordName + "Repository";
            
            String baseUriModule = "api";
            String[] pkgParts = originalPackage.split("\\.");
            if (pkgParts.length > 2) {
                baseUriModule = pkgParts[pkgParts.length - 2];
            }
            String endpointPath = "/plugin/" + baseUriModule + "/" + recordName.toLowerCase();

            String idType = "String";
            if (parsedFields != null && !parsedFields.isEmpty()) {
                String potentialIdType = parsedFields.get(0)[0];
                for (String[] field : parsedFields) {
                    if (field[1].toLowerCase().equals("id")) {
                        idType = field[0];
                        break;
                    }
                }
                if (idType.equals("String")) {
                    idType = potentialIdType;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(controllerPackage).append(";\n\n");
            
            sb.append("import ").append(sourceRecord).append(";\n");
            sb.append("import ").append(repoPackage).append(".").append(repoName).append(";\n");
            sb.append("import io.jettra.core.inject.annotation.Inject;\n");
            sb.append("import io.jettra.rest.annotations.Consumes;\n");
            sb.append("import io.jettra.rest.annotations.DELETE;\n");
            sb.append("import io.jettra.rest.annotations.GET;\n");
            sb.append("import io.jettra.rest.annotations.POST;\n");
            sb.append("import io.jettra.rest.annotations.PUT;\n");
            sb.append("import io.jettra.rest.annotations.Path;\n");
            sb.append("import io.jettra.rest.annotations.PathParam;\n");
            sb.append("import io.jettra.rest.annotations.QueryParam;\n");
            sb.append("import io.jettra.rest.annotations.Produces;\n");
            sb.append("import io.jettra.rest.annotations.Secured;\n");
            sb.append("import io.jettra.rest.annotations.accreditation.RolesAllowed;\n");
            sb.append("import io.jettra.rest.core.Response;\n");
            sb.append("import io.jettra.server.discoverer.Discovered;\n");
            sb.append("import io.jettra.server.openapi.annotations.OpenApi;\n");
            sb.append("import io.jettra.server.openapi.annotations.Operation;\n");
            sb.append("import jcf.systemRole;\n");
            sb.append("import java.util.List;\n\n");

            sb.append("@Secured\n");
            sb.append("@Path(\"").append(endpointPath).append("\")\n");
            sb.append("@RolesAllowed({systemRole.ADMIN})\n");
            sb.append("@Discovered\n");
            sb.append("@OpenApi(title = \"").append(recordName).append("\", version = \"v1.0\", description = \"API for ").append(recordName).append(" management\")\n");
            sb.append("public class ").append(controllerName).append(" {\n\n");
            
            sb.append("    @Inject\n");
            sb.append("    private ").append(repoName).append(" ").append(repoName.substring(0,1).toLowerCase()).append(repoName.substring(1)).append(";\n\n");

            sb.append("    @GET\n");
            sb.append("    @Path(\"/\")\n");
            sb.append("    @Produces(\"application/json\")\n");
            sb.append("    @Operation(summary = \"findAll\", description = \"Returns all records\")\n");
            sb.append("    public List<").append(recordName).append("> findAll(@QueryParam(\"page\") Integer page, @QueryParam(\"size\") Integer size) {\n");
            sb.append("        if (page != null && size != null) {\n");
            sb.append("            return ").append(repoName.substring(0,1).toLowerCase()).append(repoName.substring(1)).append(".findAll(page, size);\n");
            sb.append("        }\n");
            sb.append("        return ").append(repoName.substring(0,1).toLowerCase()).append(repoName.substring(1)).append(".findAll();\n");
            sb.append("    }\n\n");

            sb.append("    @POST\n");
            sb.append("    @Consumes(\"application/json\")\n");
            sb.append("    @Produces(\"application/json\")\n");
            sb.append("    @Operation(summary = \"save\", description = \"Saves a new ").append(recordName).append("\")\n");
            sb.append("    public Response save(").append(recordName).append(" record) {\n");
            sb.append("        ").append(repoName.substring(0,1).toLowerCase()).append(repoName.substring(1)).append(".save(record);\n");
            sb.append("        return Response.ok(\"{\\\"message\\\": \\\"Saved successfully\\\"}\").build();\n");
            sb.append("    }\n\n");

            sb.append("    @PUT\n");
            sb.append("    @Consumes(\"application/json\")\n");
            sb.append("    @Produces(\"application/json\")\n");
            sb.append("    @Operation(summary = \"update\", description = \"Updates an existing ").append(recordName).append("\")\n");
            sb.append("    public Response update(").append(recordName).append(" record) {\n");
            sb.append("        ").append(repoName.substring(0,1).toLowerCase()).append(repoName.substring(1)).append(".save(record);\n");
            sb.append("        return Response.ok(\"{\\\"message\\\": \\\"Updated successfully\\\"}\").build();\n");
            sb.append("    }\n\n");

            sb.append("    @DELETE\n");
            sb.append("    @Path(\"/{id}\")\n");
            sb.append("    @Produces(\"application/json\")\n");
            sb.append("    @Operation(summary = \"delete\", description = \"Deletes a ").append(recordName).append(" by id\")\n");
            sb.append("    public Response delete(@PathParam(\"id\") ").append(idType).append(" id) {\n");
            sb.append("        ").append(repoName.substring(0,1).toLowerCase()).append(repoName.substring(1)).append(".delete(id);\n");
            sb.append("        return Response.ok(\"{\\\"message\\\": \\\"Deleted successfully\\\"}\").build();\n");
            sb.append("    }\n");
            
            if (parsedFields != null) {
                for (String[] field : parsedFields) {
                    String type = field[0];
                    String name = field[1];
                    sb.append("\n    @GET\n");
                    sb.append("    @Path(\"/").append(name.toLowerCase()).append("/{").append(name).append("}\")\n");
                    sb.append("    @Produces(\"application/json\")\n");
                    sb.append("    @Operation(summary = \"findBy").append(capitalize(name)).append("\", description = \"Finds records by ").append(name).append("\")\n");
                    sb.append("    public List<").append(recordName).append("> findBy").append(capitalize(name)).append("(@PathParam(\"").append(name).append("\") ").append(type).append(" ").append(name).append(") {\n");
                    sb.append("        return ").append(repoName.substring(0,1).toLowerCase()).append(repoName.substring(1)).append(".findBy").append(capitalize(name)).append("(").append(name).append(");\n");
                    sb.append("    }\n");
                }
            }

            sb.append("}\n");
            
            Path outPath = Paths.get("src/main/java/" + controllerPackage.replace(".", "/") + "/" + controllerName + ".java");
            Files.createDirectories(outPath.getParent());
            Files.write(outPath, sb.toString().getBytes(StandardCharsets.UTF_8));
            System.out.println("Generated Controller: " + outPath.toString());
            
        } catch (Exception e) {
            System.err.println("Failed to generate Controller: " + e.getMessage());
        }
    }
}
