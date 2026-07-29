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

        String command = argList.get(0);
        String sourceRecord = null;
        boolean isModel = false;
        boolean isProperties = false;

        for (int i = 0; i < argList.size(); i++) {
            String arg = argList.get(i);
            String nextArg = (i + 1 < argList.size()) ? argList.get(i + 1) : null;

            if ("-source-record".equalsIgnoreCase(arg) || "-from-record".equalsIgnoreCase(arg) || "source-record".equalsIgnoreCase(arg) || "from-record".equalsIgnoreCase(arg)) {
                if (nextArg != null) sourceRecord = nextArg;
            } else if ("-model".equalsIgnoreCase(arg) || "model".equalsIgnoreCase(arg)) {
                isModel = true;
            } else if ("-properties".equalsIgnoreCase(arg) || "properties".equalsIgnoreCase(arg)) {
                isProperties = true;
            }
        }

        switch (command.toLowerCase()) {
            case "-create-code":
            case "create-code":
                if (sourceRecord != null && isModel) {
                    generateViewModel(sourceRecord, isProperties);
                } else {
                    System.out.println("Missing arguments. Usage: ./mvn-flux -create-code -source-record <Paquete.Record> -model [-properties]");
                }
                break;
            case "help":
            case "-help":
            case "--help":
            case "-h":
                showHelp();
                break;
            default:
                System.out.println("Unknown command: " + command);
                System.out.println();
                showHelp();
        }
    }

    private static void showHelp() {
        System.out.println("JettraFlux CLI - Menú de Ayuda");
        System.out.println("Uso: ./mvn-flux <comando> [parámetros/opciones]");
        System.out.println("====================================================================================================");
        System.out.println("Comandos disponibles:\n");
        System.out.println("  -create-code           Genera código fuente automáticamente.");
        System.out.println("                   Sintaxis: ./mvn-flux -create-code -source-record <Paquete.Record> -model [-properties]");
        System.out.println("                     Ejemplo: ./mvn-flux -create-code -source-record com.example.entity.Person -model -properties\n");
    }

    private static void generateViewModel(String sourceRecord, boolean generateProperties) {
        try {
            String relativePath = "src/main/java/" + sourceRecord.replace(".", "/") + ".java";
            Path recordPath = Paths.get(relativePath);
            if (!Files.exists(recordPath)) {
                System.out.println("Error: Source record file not found at " + relativePath);
                return;
            }

            String content = new String(Files.readAllBytes(recordPath), StandardCharsets.UTF_8);

            // Extract package
            String originalPackage = "";
            java.util.regex.Matcher pkgMatcher = java.util.regex.Pattern.compile("package\\s+([^;]+);").matcher(content);
            if (pkgMatcher.find()) {
                originalPackage = pkgMatcher.group(1).trim();
            }

            // Extract record name and fields robustly
            int recordIdx = content.indexOf("record ");
            if (recordIdx == -1) {
                System.out.println("Error: Could not find 'record' keyword in " + relativePath);
                return;
            }
            int nameStart = recordIdx + "record ".length();
            int parenStart = content.indexOf("(", nameStart);
            if (parenStart == -1) {
                System.out.println("Error: Could not find '(' after record name.");
                return;
            }
            String recordName = content.substring(nameStart, parenStart).trim();
            if (recordName.contains("<")) {
                recordName = recordName.substring(0, recordName.indexOf("<")).trim();
            }

            int parenEnd = -1;
            int openCount = 0;
            for (int i = parenStart; i < content.length(); i++) {
                if (content.charAt(i) == '(') openCount++;
                else if (content.charAt(i) == ')') {
                    openCount--;
                    if (openCount == 0) {
                        parenEnd = i;
                        break;
                    }
                }
            }
            if (parenEnd == -1) {
                System.out.println("Error: Could not find matching ')' for record.");
                return;
            }
            String fieldsContent = content.substring(parenStart + 1, parenEnd).trim();

            String modelPackage = originalPackage.replace(".entity", ".model");
            String modelClassName = recordName + "Model";

            StringBuilder sb = new StringBuilder();
            sb.append("package ").append(modelPackage).append(";\n\n");
            
            // Generate imports
            sb.append("import ").append(sourceRecord).append(";\n");
            sb.append("import io.jettra.flux.annotations.JettraViewModel;\n");
            sb.append("import io.jettra.core.flux.FluxModelToRecordConversor;\n");
            sb.append("import io.jettra.flux.annotations.PropertiesInRecord;\n");
            sb.append("import io.jettra.flux.annotations.PropertiesLabel;\n");
            sb.append("import io.jettra.flux.annotations.ViewSelectOne;\n");
            sb.append("import io.jettra.flux.annotations.ViewSelectMany;\n");
            sb.append("import io.jettra.flux.annotations.TableColumnField;\n");
            sb.append("import io.jettra.rules.validations.NotNull;\n");
            
            java.util.regex.Matcher importMatcher = java.util.regex.Pattern.compile("import\\s+([^;]+);").matcher(content);
            while (importMatcher.find()) {
                sb.append("import ").append(importMatcher.group(1).trim()).append(";\n");
            }
            sb.append("\n");

            sb.append("@JettraViewModel\n");
            sb.append("@FluxModelToRecordConversor(goal = ").append(recordName).append(".class)\n");
            sb.append("public class ").append(modelClassName).append(" {\n\n");

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
                    } else {
                        currentField.append(c);
                    }
                }
                if (currentField.length() > 0) {
                    rawFieldsList.add(currentField.toString());
                }

                for (String rawField : rawFieldsList) {
                    rawField = rawField.trim();
                    rawField = rawField.replaceAll("@\\w+(?:\\([^)]*\\))?\\s+", "");
                    String[] parts = rawField.split("\\s+");
                    if (parts.length >= 2) {
                        String type = parts[parts.length - 2];
                        String name = parts[parts.length - 1];
                        parsedFields.add(new String[]{type, name});
                    }
                }
            }

            for (String[] field : parsedFields) {
                String type = field[0];
                String name = field[1];
                
                sb.append("    @PropertiesInRecord\n");
                sb.append("    @PropertiesLabel(value = \"").append(recordName.toLowerCase()).append(".").append(name).append("\", label = \"").append(capitalize(name)).append("\")\n");
                
                boolean isBasic = type.equals("String") || type.equals("Integer") || type.equals("Boolean") || type.equals("UUID") || type.equals("Long") || type.equals("Double") || type.equals("int") || type.equals("boolean") || type.equals("long") || type.equals("double");
                
                if (isBasic) {
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
}
