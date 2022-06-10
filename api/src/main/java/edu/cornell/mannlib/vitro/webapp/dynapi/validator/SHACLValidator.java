package edu.cornell.mannlib.vitro.webapp.dynapi.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static edu.cornell.mannlib.vitro.webapp.utils.configuration.ConfigurationBeanLoader.isMatchingJavaUri;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

public class SHACLValidator implements ModelValidator {

    private final Log log = LogFactory.getLog(this.getClass());

    protected final Model data;
    protected final Model scheme;
    protected Map<String, Resource> map;

    public SHACLValidator(Model data, Model scheme){
        this.data = data;
        this.scheme = scheme;
    }

    private void clean(){
        map = new HashMap<String, Resource>();
    }

    private Resource validateResourceOverSHACLRules(String uri) throws InterruptedException {
        Model dataModel = JenaUtil.createMemoryModel();
        dataModel.add(data.getResource(uri).listProperties());
        Resource report = ValidationUtil.validateModel(dataModel, scheme, true);

        if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
        }
        return report;
    }

    private Resource validateResourceAndLinkedObjectsOverSHACLRules(String uri) throws InterruptedException {
        clean();
        collectHierarchy(data.getResource(uri));
        Model dataModel = JenaUtil.createMemoryModel();

        for(Resource resource:map.values()) {
            dataModel.add(resource.listProperties());
        }
        Resource report = ValidationUtil.validateModel(dataModel, scheme, true);

        if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
        }
        return report;

    }

    protected boolean shouldBeValidated(Resource resource){
        return !map.containsKey(resource.getURI());
    }

    private void collectHierarchy(Resource resource){
        if(shouldBeValidated(resource)){
            map.put(resource.getURI(), resource);
            Set<Statement> properties = resource.listProperties().toSet();
            for (Statement statement : properties) {
                if (statement.getObject().isResource()) {
                    collectHierarchy(statement.getObject().asResource());
                }
            }
        }
    }

    private Resource validateFileOverSHACLRules(String path) throws IOException {
        Model dataModel = JenaUtil.createMemoryModel();
        Path p = new File(path).toPath();

        StringReader stringReader =  new StringReader(new String(Files.readAllBytes(p)));

        dataModel.read(stringReader, null, "N3");

        Resource report = ValidationUtil.validateModel(dataModel.union(data), scheme, true);

        if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
        }
        return report;
    }

    @Override
    public boolean isValidResource(String uri, boolean deepCheck){
        Resource report = null;
        try {
            report = (deepCheck)?validateResourceAndLinkedObjectsOverSHACLRules(uri):validateResourceOverSHACLRules(uri);
        } catch (InterruptedException e) {
            log.warn("Validation of the resource " + uri + " has been interrupted.", e);
        }
        return report != null && report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 0;
    }

    @Override
    public boolean isValidFile(String path){
        Resource report = null;
        try {
            report = validateFileOverSHACLRules(path);
        } catch (IOException e) {
            log.warn("File " + path + " can't be read.", e);
        }
        return report != null && report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 0;
    }

}
