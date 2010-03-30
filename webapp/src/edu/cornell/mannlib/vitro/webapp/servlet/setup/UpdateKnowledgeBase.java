/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.ontology.update.OntologyUpdater;

/**
 * Invokes process to test whether the knowledge base needs any updating
 * to align with ontology changes.
 * @author bjl23
 *
 */
public class UpdateKnowledgeBase implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		
		OntModelSelector oms = new SimpleOntModelSelector(
				(OntModel) sce.getServletContext().getAttribute(
						JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME));
		
		try {
			(new OntologyUpdater(sce.getServletContext(), oms)).update(); 
		} catch (IOException ioe) {
			throw new RuntimeException("IOException updating knowledge base " +
					"for ontology changes", ioe);
		}
		
	}	
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do	
	}

}
