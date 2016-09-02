/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.reasoner.ReasonerPlugin;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;

/**
 * Disables sameAs in associated SimpleReasoner.
 */
public class DisableSameAs implements ReasonerPlugin {
	
    private static final Log log = LogFactory.getLog(DisableSameAs.class);
    	
	private SimpleReasoner simpleReasoner;

    public void setSimpleReasoner(SimpleReasoner simpleReasoner) {
    	this.simpleReasoner = simpleReasoner;
        this.simpleReasoner.setSameAsEnabled( false );
        log.info("owl:sameAs disabled in SimpleReasoner.");
    }	
	
    public boolean isConfigurationOnlyPlugin() {
        return true;
    }
    
    public SimpleReasoner getSimpleReasoner() {
    	return this.simpleReasoner;
    }

	public boolean isInterestedInAddedStatement(Statement stmt) {
		return false;
	}
	
	public boolean isInterestedInRemovedStatement(Statement stmt) {
		return false;
	}
	
	public void addedABoxStatement(Statement stmt, 
            Model aboxAssertionsModel, 
            Model aboxInferencesModel, 
            OntModel TBoxInferencesModel) {
        return;
	}
	
	
    public void removedABoxStatement(Statement stmt, 
            Model aboxAssertionsModel, 
            Model aboxInferencesModel, 
            OntModel TBoxInferencesModel) {
        return;
    }
	
}

