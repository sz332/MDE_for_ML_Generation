package at.vres.master.mdml.tbcg;

import at.vres.master.mdml.decomposition.MLInformationHolder;
import at.vres.master.mdml.mapping.JSONInformationHolder;
import at.vres.master.mdml.mapping.MappingHandler;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import java.io.*;
import java.security.KeyException;
import java.util.*;

public class VelocityTemplateHandler {
    private static final String VELOCITY_TEMPLATE_PATH_KEY = "file.resource.loader.path";
    private final List<String> templateNames = new LinkedList<>();
    private VelocityEngine internalEngine;
    private final Map<String, VelocityEngine> externalEngines = new HashMap<>();
    private final Map<Context, String> contexts = new HashMap<>();

    public void initInternalEngine(String internalTemplateFolderPath) {
        VelocityEngine ve = new VelocityEngine();
        Properties p = new Properties();
        p.setProperty(VELOCITY_TEMPLATE_PATH_KEY, internalTemplateFolderPath);
        ve.init(p);
        internalEngine = ve;
    }

    public String createContextInternalAndMerge(MLInformationHolder data, String templateName, String encoding) {
        Writer writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        data.getParts().forEach(context::put);
        data.getProperties().forEach(context::put);
        data.getStereotypes().forEach((stKey, stVal) -> stVal.forEach(context::put));
        contexts.put(context, templateName);
        if(internalEngine != null) internalEngine.mergeTemplate(templateName, encoding, context, writer);
        else throw new NullPointerException("The internal engine has not been initialized!");
        return writer.toString();
    }

    public Writer createContextExternalAndMerge(Map<String, Object> data, String templateName, String encoding, String templateFolder, Writer writer) throws KeyException {
        VelocityContext context = new VelocityContext();
        data.forEach(context::put);
        contexts.put(context, templateName);
        if(externalEngines.containsKey(templateFolder)) externalEngines.get(templateFolder).mergeTemplate(templateName, encoding, context, writer);
        else throw new KeyException("No VelocityEngine initialized for the given template folder path!");
        return writer;
    }

    public void clearExternalEngines() {
        externalEngines.clear();
    }

    public boolean removeExternalEngine(String externalTemplateFolderPath) {
        return externalEngines.remove(externalTemplateFolderPath) != null;
    }

    public void initExternalEngine(String externalTemplateFolderPath) {
        VelocityEngine ve = new VelocityEngine();
        Properties p = new Properties();
        p.setProperty(VELOCITY_TEMPLATE_PATH_KEY, externalTemplateFolderPath);
        ve.init(p);
        externalEngines.put(externalTemplateFolderPath, ve);
    }

    public void initExternalEngines(List<String> externalTemplateFolderPaths) {
        externalTemplateFolderPaths.forEach(this::initExternalEngine);
    }

    private static Properties getDefaultProperties() {
        Properties p = new Properties();
        p.setProperty(VELOCITY_TEMPLATE_PATH_KEY, "C:\\Users\\rup\\IdeaProjects\\MasterModelDrivenML\\templates");
        return p;
    }

    public List<String> getTemplateNames() {
        return templateNames;
    }

    public void addTemplate(String templateName) {
        templateNames.add(templateName);
    }

    public void loadTemplates() {
        templateNames.forEach(temp -> {

        });
    }

    public static String generateFromExtractedInformation(Map<String, MLInformationHolder> map, String templateName) {
        VelocityEngine ve = new VelocityEngine();
        ve.init(getDefaultProperties());
        VelocityContext context = new VelocityContext();
        map.forEach((key, value) -> {
            value.getParts().forEach(context::put);
            value.getProperties().forEach(context::put);
            value.getStereotypes().forEach((stKey, stVal) -> stVal.forEach(context::put));
        });

        String result = "";
        try (StringWriter sw = new StringWriter()) {
            ve.mergeTemplate(templateName, "UTF-8", context, sw);
            RuntimeInstance ri = new RuntimeInstance();
            final String absPath = "C:\\Users\\rup\\IdeaProjects\\MasterModelDrivenML\\templates\\" + templateName;
            try {
                SimpleNode node = ri.parse(new FileReader(absPath), ve.getTemplate(templateName));
                TemplateVisitor tv = new TemplateVisitor();
                Object visit = tv.visit(node, null);
                System.out.println("visit = " + visit);
            } catch (ParseException | FileNotFoundException e) {
                e.printStackTrace();
            }
            result = sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String generateFromJSON(String jsonPath) {
        JSONInformationHolder jih = MappingHandler.readJSON(jsonPath);
        Properties properties = new Properties();
        properties.put(VELOCITY_TEMPLATE_PATH_KEY, jih.getTemplateFolder());
        return generateFromObject(jih.getTemplate(), jih.getParameters(), properties);
    }

    public static String generateFromObject(String templateName, Map<String, Object> parameters, Properties properties) {
        VelocityEngine ve = new VelocityEngine();
        ve.init(properties);
        VelocityContext context = new VelocityContext();
        parameters.forEach(context::put);
        String result = "";
        try (StringWriter sw = new StringWriter()) {
            ve.mergeTemplate(templateName, "UTF-8", context, sw);
            System.out.println("TEST: \n" + sw);
            result = sw.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void velTestRun(String templateName) {
        Properties p = new Properties();
        p.setProperty("file.resource.loader.path", "C:\\Users\\rup\\IdeaProjects\\MasterModelDrivenML\\templates");
        VelocityEngine ve = new VelocityEngine();
        ve.init(p);

        VelocityContext context = new VelocityContext();
        context.put("df", "df");
        context.put("y", "weather");
        context.put("train_size", 0.7);
        context.put("features", new String[]{"\"precipitation\", \"temp_max\", \"temp_min\", \"wind\""});

        try (StringWriter sw = new StringWriter()) {
            ve.mergeTemplate(templateName, "UTF-8", context, sw);
            System.out.println("TEST: \n" + sw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VelocityEngine getInternalEngine() {
        return internalEngine;
    }

    public Map<String, VelocityEngine> getExternalEngines() {
        return externalEngines;
    }

    public Map<Context, String> getContexts() {
        return contexts;
    }
}
