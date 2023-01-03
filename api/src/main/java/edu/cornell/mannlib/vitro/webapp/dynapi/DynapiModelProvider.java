package edu.cornell.mannlib.vitro.webapp.dynapi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class DynapiModelProvider {

	private static DynapiModelProvider INSTANCE = new DynapiModelProvider();
	private static final Log log = LogFactory.getLog(DynapiModelProvider.class);
	private OntModel abox;
	private OntModel tbox;
	private OntModel permanentModel;
    
	public static DynapiModelProvider getInstance() {
		return INSTANCE;
	}

	public void init(OntModel abox, OntModel tbox) {
		this.abox = abox;
		this.tbox = tbox;
	}

	public void setModel(OntModel model) {
		this.permanentModel = model;
	}

	public Model getModel() {
		return constructModelWithSparql();
	}
	
    private Model constructModelWithSparql() {
        Model memModel;
        if (permanentModel != null) {
            memModel = permanentModel;
        } else {
            Model union = ModelFactory.createUnion(abox, tbox);
            memModel = ModelFactory.createDefaultModel();
            memModel.add(union);
        }
        return memModel;
    }
	
}
