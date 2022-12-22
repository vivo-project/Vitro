package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.io.StringWriter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

public class DynapiModelProvider {

	private static DynapiModelProvider INSTANCE = new DynapiModelProvider();
	private static final Log log = LogFactory.getLog(DynapiModelProvider.class);

	private OntModel abox;
	private OntModel tbox;
	private OntModel permanentModel;
	private List<Rule> rules;
	private Reasoner reasoner;

	public static DynapiModelProvider getInstance() {
		return INSTANCE;
	}

	public void init(OntModel abox, OntModel tbox) {
		this.abox = abox;
		this.tbox = tbox;
		initReasoner();
	}

    private void initReasoner() {
        rules = Rule.parseRules("[rdfs9:  (?x rdfs:subClassOf ?y), (?a rdf:type ?x) -> (?a rdf:type ?y)]");
		reasoner = new GenericRuleReasoner(rules);
    }

	public void setModel(OntModel model) {
		this.permanentModel = model;
	}

	public Model getModel() {
		if (permanentModel != null) {
		    if (reasoner == null) {
		        initReasoner();
		    }
		    InfModel inferencingModel = ModelFactory.createInfModel(reasoner, permanentModel);
			return inferencingModel;
		}
		return constructModel();
	}

	private Model constructModel() {
		Model union = ModelFactory.createUnion(abox, tbox);
		Model memModel = ModelFactory.createDefaultModel();
		memModel.add(union);
		InfModel unionWithInferencing = ModelFactory.createInfModel(reasoner, memModel);
		if (log.isDebugEnabled()) {
			log.debug("ABox size " + abox.size());
			log.debug("TBox size " + tbox.size());
			log.debug("Union size " + union.size());
			log.debug("memModel size " + memModel.size());
			log.debug("unionWithInferencing size " + unionWithInferencing.size());
			Model inf = unionWithInferencing.difference(memModel);
			StringWriter writer = new StringWriter();
			inf.write(writer, "N3");
			log.debug(writer.toString());
		}
		return unionWithInferencing;
	}
}
