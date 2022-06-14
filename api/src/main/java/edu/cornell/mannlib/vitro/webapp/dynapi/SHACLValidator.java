package edu.cornell.mannlib.vitro.webapp.dynapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
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

public class SHACLValidator implements ModelValidator {

    private final Log log = LogFactory.getLog(this.getClass());

    private final Model data;
    private final Model scheme;
    private final ValidationEngine engine;

    public SHACLValidator(Model data, Model scheme){
        this.data = data;
        this.scheme = scheme;
        engine = ValidationUtil.createValidationEngine(data, scheme, true);
    }

    private void cleanEngine(){
        engine.getReport().removeProperties();
        engine.getReport().getModel().removeAll();
    }

    private Resource validateResourceOverSHACLRules(String uri) throws InterruptedException {
        cleanEngine();
        Resource report = engine.validateNode(data.getResource(uri).asNode());
        if (report.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
            log.warn(ModelPrinter.get().print(report.getModel()));
        }
        return report;
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
    public boolean isValidResource(String uri){
        Resource resource = null;
        try {
            resource = validateResourceOverSHACLRules(uri);
        } catch (InterruptedException e) {
            log.warn("Validation of the resource " + uri + " has been interrupted.", e);
        }
        return resource != null && resource.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 0;
    }

    @Override
    public boolean isValidFile(String path){
        Resource resource = null;
        try {
            resource = validateFileOverSHACLRules(path);
        } catch (IOException e) {
            log.warn("File " + path + " can't be read.", e);
        }
        return resource != null && resource.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 0;
    }

}
