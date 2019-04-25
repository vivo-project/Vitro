/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.reasoner.plugin;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.reasoner.ReasonerPlugin;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;

/**
 * handles rules of the form
 * assertedProp(?x, ?y) ^ type(?x) -&gt; inferredProp(?x, ?y)
 *
 * @author bjl23
 *
 */
public abstract class SimplePropertyAndTypeRule implements ReasonerPlugin {

	private Property ASSERTED_PROP;
	private Resource TYPE;
	private Property INFERRED_PROP;
	private SimpleReasoner simpleReasoner;

	protected SimplePropertyAndTypeRule(String assertedProp, String type, String inferredProp) {
		TYPE = ResourceFactory.createResource(type);
        ASSERTED_PROP = ResourceFactory.createProperty(assertedProp);
        INFERRED_PROP = ResourceFactory.createProperty(inferredProp);
	}

	public boolean isConfigurationOnlyPlugin() {
        return false;
    }

	public boolean isInterestedInAddedStatement(Statement stmt) {
		return (RDF.type.equals(stmt.getPredicate()) || isRelevantPredicate(stmt));
	}

	public boolean isInterestedInRemovedStatement(Statement stmt) {
		return (RDF.type.equals(stmt.getPredicate()) || isRelevantPredicate(stmt));
	}

	public void addedABoxStatement(Statement stmt,
            Model aboxAssertionsModel,
            Model aboxInferencesModel,
            OntModel TBoxInferencesModel) {
		boolean relevantType = isRelevantType(stmt, TBoxInferencesModel);
		boolean relevantPredicate = isRelevantPredicate(stmt);

		if (relevantType) {
			StmtIterator stmtIt = aboxAssertionsModel.listStatements(
					stmt.getSubject(), ASSERTED_PROP, (RDFNode)null);
			while (stmtIt.hasNext()) {
				Statement s = stmtIt.nextStatement();
				tryToInfer(stmt.getSubject(),
						   INFERRED_PROP,
						   s.getObject(),
						   aboxAssertionsModel,
						   aboxInferencesModel);
			}
		} else if (relevantPredicate) {
			if(aboxAssertionsModel.contains(
					stmt.getSubject(), RDF.type, TYPE)
			  || aboxInferencesModel.contains(
					  stmt.getSubject(), RDF.type, TYPE)) {
				tryToInfer(stmt.getSubject(),
						   INFERRED_PROP,
						   stmt.getObject(),
						   aboxAssertionsModel,
						   aboxInferencesModel);
			}
		}
	}

	private void tryToInfer(Resource subject,
			                Property predicate,
			                RDFNode object,
			                Model aboxAssertionsModel,
			                Model aboxInferencesModel) {
		// this should be part of a superclass or some class that provides
		// reasoning framework functions
		Statement s = ResourceFactory.createStatement(subject, predicate, object);
		if (simpleReasoner != null) {
			simpleReasoner.addInference(s,aboxInferencesModel);
		}
	}

    public void removedABoxStatement(Statement stmt,
            Model aboxAssertionsModel,
            Model aboxInferencesModel,
            OntModel TBoxInferencesModel) {

    	if (isRelevantPredicate(stmt)) {
//    		if (aboxAssertionsModel.contains(
//    				stmt.getSubject(), RDF.type, BIBO_DOCUMENT)
//    			        || aboxInferencesModel.contains(
//    						stmt.getSubject(), RDF.type, BIBO_DOCUMENT)) {
    		    if (simpleReasoner != null) {
    		       simpleReasoner.removeInference(ResourceFactory.createStatement(stmt.getSubject(), INFERRED_PROP, stmt.getObject()), aboxInferencesModel);
    		    }
//    		}
    	} else if (isRelevantType(stmt, TBoxInferencesModel)) {
    		if(!aboxInferencesModel.contains(
    				stmt.getSubject(), RDF.type, TYPE)) {
    			StmtIterator groundIt = aboxAssertionsModel.listStatements(
    					stmt.getSubject(), ASSERTED_PROP, (RDFNode) null);
    			while (groundIt.hasNext()) {
    				Statement groundStmt = groundIt.nextStatement();
        		    simpleReasoner.removeInference(ResourceFactory.createStatement(groundStmt.getSubject(), INFERRED_PROP, groundStmt.getObject()), aboxInferencesModel);
    			}
    		}
    	}
    }

    private boolean isRelevantType(Statement stmt, Model TBoxInferencesModel) {
		return (RDF.type.equals(stmt.getPredicate())
				&& (TYPE.equals(stmt.getObject())
						|| TBoxInferencesModel.contains(
								(Resource) stmt.getObject(), RDFS.subClassOf, TYPE)));
    }

    private boolean isRelevantPredicate(Statement stmt) {
		return (ASSERTED_PROP.equals(stmt.getPredicate()));
    }

	public void setSimpleReasoner(SimpleReasoner simpleReasoner) {
		this.simpleReasoner = simpleReasoner;
	}

	public SimpleReasoner getSimpleReasoner() {
		return this.simpleReasoner;
	}
}
