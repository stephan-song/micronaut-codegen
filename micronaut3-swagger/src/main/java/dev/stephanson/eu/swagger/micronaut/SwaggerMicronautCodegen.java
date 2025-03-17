package dev.stephanson.eu.swagger.micronaut;

import com.samskivert.mustache.Mustache;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.languages.AbstractJavaCodegen;
import io.swagger.codegen.languages.features.BeanValidationFeatures;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SwaggerMicronautCodegen extends AbstractJavaCodegen implements BeanValidationFeatures {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerMicronautCodegen.class);
    private static final String TITLE = "title";
    private static final String CONFIG_PACKAGE = "configPackage";
    private static final String BASE_PACKAGE = "basePackage";
    private static final String ASYNC = "async";
    private static final String USE_TAGS = "useTags";
    private static final String INTERFACE_ONLY = "interfaceOnly";
    private static final String IMPLICIT_HEADERS = "implicitHeaders";
    private static final String WARN_BODY_PRIMITIVES = "warnBodyPrimitives";

    protected String title = "swagger-petstore";
    protected String configPackage = "io.swagger.configuration";
    protected String basePackage = "io.swagger";
    protected boolean interfaceOnly = false;
    protected boolean useTags = false;
    protected boolean useBeanValidation = true;
    protected boolean warnBodyPrimitives = false;

    public SwaggerMicronautCodegen() {
        super();
        outputFolder = "generated-code/micronaut";
        apiTestTemplateFiles.clear();
        embeddedTemplateDir = templateDir = "micronaut-swagger";
        apiPackage = "io.swagger.api";
        modelPackage = "io.swagger.model";
        invokerPackage = "io.swagger.api";
        artifactId = "swagger-micronaut";

        supportedLibraries.put(DEFAULT_LIBRARY, "Micronaut server application.");
        setLibrary(DEFAULT_LIBRARY);

        additionalProperties.putAll(Map.of(
            CONFIG_PACKAGE, configPackage,
            BASE_PACKAGE, basePackage,
            "jackson", "true"
        ));

        cliOptions.addAll(List.of(
            CliOption.newString(TITLE, "Server title"),
            CliOption.newString(CONFIG_PACKAGE, "Configuration package for code"),
            CliOption.newString(BASE_PACKAGE, "Base/invoker package for code"),
            CliOption.newBoolean(INTERFACE_ONLY, "Generate only API interface, no implementation"),
            CliOption.newBoolean(ASYNC, "Use async features"),
            CliOption.newBoolean(USE_TAGS, "Use tags for creating interface and controller class names"),
            CliOption.newBoolean(USE_BEANVALIDATION, "Use bean validation annotations"),
            CliOption.newBoolean(WARN_BODY_PRIMITIVES, "Warn on using primitives in the body"),
            CliOption.newString(CodegenConstants.LIBRARY, "Library template to use").addEnum(DEFAULT_LIBRARY, "Micronaut server").defaultValue(DEFAULT_LIBRARY)
        ));
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    @Override
    public String getName() {
        return "micronaut3-swagger";
    }

    @Override
    public String getHelp() {
        return "Generate Micronaut <=3.9.X server application using swagger definitions.";
    }

    @Override
    public void setUseBeanValidation(boolean useBeanValidation) {
        this.useBeanValidation = useBeanValidation;
    }

    @Override
    public void preprocessSwagger(Swagger swagger) {
        super.preprocessSwagger(swagger);
        swagger.setBasePath("/".equals(swagger.getBasePath()) ? "" : swagger.getBasePath());

        if (!additionalProperties.containsKey(TITLE)) {
            Optional
                .ofNullable(swagger.getInfo().getTitle())
                .map(this::sanitizeName)
                .map(t -> camelize(t, true))
                .ifPresent(title -> {
                    this.setTitle(title);
                    additionalProperties.put(TITLE, title);
                });
        }

        additionalProperties.putIfAbsent("serverPort", Optional.ofNullable(swagger.getHost())
            .map(it -> Pattern.compile(":(\\d+)$").matcher(it))
            .filter(Matcher::find)
            .map(it -> it.group(1))
            .orElse("8080"));

        if (swagger.getPaths() == null || swagger.getPaths().isEmpty()) return;

        for (var pathName: swagger.getPaths().keySet()) {
            var path = swagger.getPath(pathName);
            if (Objects.isNull(path)) continue;
            for (var operation: path.getOperations()) {
                var tags = new ArrayList<Map<String, String>>();
                for (var tag: operation.getTags()) {
                    var storedTag = new HashMap<String, String>();
                    storedTag.put("tag", tag);
                    storedTag.put("hasMore", "true");
                    tags.add(storedTag);
                }
                if (!tags.isEmpty()) {
                    tags.getLast().remove("hasMore");
                }

                if (!operation.getTags().isEmpty()) {
                    operation.setTags(List.of(operation.getTags().getFirst()));
                }
                operation.setVendorExtension("x-tags", tags);
            }
        }
    }

    @Override
    public void processOpts() {
        super.processOpts();
        additionalProperties.putAll(Map.of(
            "dateLibrary", "java8",
            "hideGenerationTimestamp", "true",
            "lambdaEscapeDoubleQuote", lamdaEscapeDoubleQuote(),
            "lambdaRemoveLineBreak", lambdaNoLineBreak(),
            "lambdaUpperCamelCase", lambdaUpperCamelCase()
        ));

        this.setBasePackage(super.getInvokerPackage());
        if (additionalProperties.containsKey(TITLE)) {
            this.setTitle((String) additionalProperties.get(TITLE));
        }

        if (additionalProperties.containsKey(CONFIG_PACKAGE)) {
            this.setConfigPackage((String) additionalProperties.get(CONFIG_PACKAGE));
        }

        if (additionalProperties.containsKey(INTERFACE_ONLY)) {
            this.setInterfaceOnly(Boolean.parseBoolean(additionalProperties.get(INTERFACE_ONLY).toString()));
        }

        if (additionalProperties.containsKey(USE_TAGS)) {
            this.setUseTags(Boolean.parseBoolean(additionalProperties.get(USE_TAGS).toString()));
        }

        if (additionalProperties.containsKey(USE_BEANVALIDATION)) {
            this.setUseBeanValidation(convertPropertyToBoolean(USE_BEANVALIDATION));
        }

        if (additionalProperties.containsKey(WARN_BODY_PRIMITIVES)) {
            this.warnBodyPrimitives = convertPropertyToBoolean(WARN_BODY_PRIMITIVES);
        }

        writePropertyBack(USE_BEANVALIDATION, useBeanValidation);

        modelDocTemplateFiles.remove("model_doc.mustache");
        apiDocTemplateFiles.remove("api_doc.mustache");

        typeMapping.put("file", "AttachedFile");
        importMapping.put("AttachedFile", "io.micronaut.http.server.types.files.AttachedFile");
    }


    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
        co.bodyParams.stream()
            .filter(ignored -> warnBodyPrimitives)
            .filter(p -> p.isPrimitiveType || p.isUuid)
            .forEach(p -> {
                LOGGER.warn(String.format("warning! Value type %s used as body parameter %s at %s.", p.dataType, p.baseName, co.operationId));
                LOGGER.warn("warning! This may lead to issues with some frameworks.");
                LOGGER.warn("warning! To disable this warning, set " + WARN_BODY_PRIMITIVES + " to false in additionalProperties.");
            });
        if ((library.equals(DEFAULT_LIBRARY)) && !useTags) {
            String basePath = resourcePath;
            if (basePath.startsWith("/")) {
                basePath = basePath.substring(1);
            }
            int pos = basePath.indexOf("/");
            if (pos > 0) {
                basePath = basePath.substring(0, pos);
            }

            if (basePath.isEmpty()) {
                basePath = "default";
            } else {
                co.subresourceOperation = !co.path.isEmpty();
            }
            List<CodegenOperation> opList = operations.computeIfAbsent(basePath, k -> new ArrayList<>());
            opList.add(co);
            co.baseName = basePath;
        } else {
            super.addOperationToGroup(tag, resourcePath, operation, co, operations);
        }
    }

    @Override
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        final var operations = objs.get("operations");
        if (!(operations instanceof Map)) return objs;

        final var ops = (List<CodegenOperation>) ((Map<?, ?>) operations).get("operation");
        ops.stream().flatMap(it -> it.responses.stream()).filter(Objects::nonNull).forEach(response -> {
            if ("0".equals(response.code)) response.code = "200";
            doDataTypeAssignment(response.dataType, new DataTypeAssigner() {
                @Override
                public void setReturnType(String returnType) {
                    response.dataType = returnType;
                }

                @Override
                public void setReturnContainer(String returnContainer) {
                    response.containerType = returnContainer;
                }
            });
        });

        ops.forEach(it -> doDataTypeAssignment(it.returnType, new DataTypeAssigner() {
            @Override
            public void setReturnType(String returnType) {
                it.returnType = returnType;
            }

            @Override
            public void setReturnContainer(String returnContainer) {
                it.returnContainer = returnContainer;
            }
        }));

        return objs;
    }

    private interface DataTypeAssigner {
        void setReturnType(String returnType);

        void setReturnContainer(String returnContainer);
    }

    /**
     * @param returnType       The return type that needs to be converted
     * @param dataTypeAssigner An object that will assign the data to the respective fields in the model.
     */
    private void doDataTypeAssignment(String returnType, DataTypeAssigner dataTypeAssigner) {
        if (returnType == null) {
            dataTypeAssigner.setReturnType("void");
        } else if (returnType.startsWith("List")) {
            int end = returnType.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(returnType.substring("List<".length(), end).trim());
                dataTypeAssigner.setReturnContainer("List");
            }
        } else if (returnType.startsWith("Map")) {
            int end = returnType.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(returnType.substring("Map<".length(), end).split(",")[1].trim());
                dataTypeAssigner.setReturnContainer("Map");
            }
        } else if (returnType.startsWith("Set")) {
            int end = returnType.lastIndexOf(">");
            if (end > 0) {
                dataTypeAssigner.setReturnType(returnType.substring("Set<".length(), end).trim());
                dataTypeAssigner.setReturnContainer("Set");
            }
        }
    }

    @Override
    public String toApiName(String name) {
        return name.isEmpty() ? "Default" : camelize(sanitizeName(name)) + "Api";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setConfigPackage(String configPackage) {
        this.configPackage = configPackage;
    }

    public void setBasePackage(String configPackage) {
        this.basePackage = configPackage;
    }

    public void setInterfaceOnly(boolean interfaceOnly) {
        this.interfaceOnly = interfaceOnly;
    }

    public void setUseTags(boolean useTags) {
        this.useTags = useTags;
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);

        if ("null".equals(property.example)) {
            property.example = null;
        }

        //Add imports for Jackson
        if (!Boolean.TRUE.equals(model.isEnum)) {
            model.imports.add("JsonProperty");

            if (Boolean.TRUE.equals(model.hasEnums)) {
                model.imports.add("JsonValue");
            }
        } else { // enum class
            //Needed imports for Jackson's JsonCreator
            if (additionalProperties.containsKey("jackson")) {
                model.imports.add("JsonCreator");
            }
        }
    }

    @Override
    public Map<String, Object> postProcessModelsEnum(Map<String, Object> objs) {
        objs = super.postProcessModelsEnum(objs);

        //Add imports for Jackson
        List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
        List<Object> models = (List<Object>) objs.get("models");
        for (Object _mo : models) {
            Map<String, Object> mo = (Map<String, Object>) _mo;
            CodegenModel cm = (CodegenModel) mo.get("model");
            // for enum model
            if (Boolean.TRUE.equals(cm.isEnum) && cm.allowableValues != null) {
                cm.imports.add(importMapping.get("JsonValue"));
                Map<String, String> item = new HashMap<String, String>();
                item.put("import", importMapping.get("JsonValue"));
                imports.add(item);
            }
        }

        return objs;
    }

    private Mustache.Lambda lamdaEscapeDoubleQuote() {
        return (frag, out) -> out.write(frag.execute().replaceAll("\"", Matcher.quoteReplacement("\\\"")));
    }

    private Mustache.Lambda lambdaNoLineBreak() {
        return (frag, out) -> out.write(frag.execute().replaceAll("[\\r\\n]", ""));
    }

    private Mustache.Lambda lambdaUpperCamelCase() {
        return (fragment, writer) -> writer.write(camelize((fragment.execute().toLowerCase())));
    }
}
