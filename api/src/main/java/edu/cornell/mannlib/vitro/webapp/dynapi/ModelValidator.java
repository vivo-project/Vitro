package edu.cornell.mannlib.vitro.webapp.dynapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.*;
import org.topbraid.shacl.util.ModelPrinter;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationUtil;
import org.topbraid.shacl.vocabulary.SH;

public class ModelValidator {

    private final Log log = LogFactory.getLog(this.getClass());

    private final Model model;
    private final ValidationEngine engine;

    public ModelValidator(Model model){
        this.model = model;
        this.engine = ValidationUtil.createValidationEngine(model, model, true);
    }

    private Resource validationOverShaclRules(String uri) throws InterruptedException {
       Resource resource = engine.validateNode(model.getResource(uri).asNode());
       if (resource.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() != 0) {
           log.warn(ModelPrinter.get().print(resource.getModel()));
       }
       return resource;
    }


    public boolean isValid(String uri){
        Resource resource = null;
        try {
            resource = validationOverShaclRules(uri);
        } catch (InterruptedException e) {
            log.warn("Resource " + uri + " is not in accordance with defined SHACL validation rules.", e);
        }
        return resource != null && resource.getModel().listStatements(null, SH.resultSeverity, SH.Violation).toList().size() == 0;
    }

}
