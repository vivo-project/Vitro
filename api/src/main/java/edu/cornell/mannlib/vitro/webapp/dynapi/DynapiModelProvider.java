package edu.cornell.mannlib.vitro.webapp.dynapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class DynapiModelProvider {

    private static DynapiModelProvider INSTANCE = new DynapiModelProvider();
    private static final Log log = LogFactory.getLog(DynapiModelProvider.class);
    private OntModel permanentModel;

    public static DynapiModelProvider getInstance() {
        return INSTANCE;
    }

    public void init(OntModel abox, OntModel tbox) {
        permanentModel = ModelFactory.createOntologyModel();
        permanentModel.addSubModel(abox);
        permanentModel.addSubModel(tbox);
        log.debug("abox size:" + abox.size());
        log.debug("tbox size:" + tbox.size());
        log.debug("OntModel size:" + permanentModel.size());
    }

    public void setModel(OntModel model) {
        this.permanentModel = model;
    }

    public Model getModel() {
        return permanentModel;
    }
}
