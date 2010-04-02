/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.SimpleOntModelSelector;
import edu.cornell.mannlib.vitro.webapp.ontology.update.OntologyUpdateSettings;
import edu.cornell.mannlib.vitro.webapp.ontology.update.OntologyUpdater;

/**
 * Invokes process to test whether the knowledge base needs any updating
 * to align with ontology changes.
 * @author bjl23
 *
 */
public class UpdateKnowledgeBase implements ServletContextListener {

	private final String DATA_DIR = "/WEB-INF/ontologies/update/";
	private final String LOG_DIR = "logs/";
	private final String REMOVED_DATA_DIR = "removedData/";
	private final String ASK_QUERY_FILE = DATA_DIR + "ask.sparql";
	private final String SUCCESS_ASSERTIONS_FILE = DATA_DIR + "success.n3";
	private final String SUCCESS_RDF_FORMAT = "N3";
	private final String DIFF_FILE = DATA_DIR + "diff.tab.txt";
	private final String LOG_FILE = DATA_DIR + LOG_DIR + 
									"knowledgeBaseUpdate.log";
	private final String ERROR_LOG_FILE = DATA_DIR + LOG_DIR +
									"knowledgeBaseUpdate.error.log";
	private final String REMOVED_DATA_FILE = DATA_DIR + REMOVED_DATA_DIR +
									"removedData.rdf";
	private final String SPARQL_CONSTRUCTS_DIR = DATA_DIR + "sparqlConstructs/";
	
	public void contextInitialized(ServletContextEvent sce) {
		
		/*
		
		ServletContext ctx = sce.getServletContext();
		
		OntModelSelector oms = new SimpleOntModelSelector(
				(OntModel) sce.getServletContext().getAttribute(
						JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME));
		
		OntologyUpdateSettings settings = new OntologyUpdateSettings();
		settings.setAskQueryFile(ctx.getRealPath(ASK_QUERY_FILE));
		settings.setDataDir(ctx.getRealPath(DATA_DIR));
		settings.setSparqlConstructsDir(ctx.getRealPath(SPARQL_CONSTRUCTS_DIR));
		settings.setDiffFile(ctx.getRealPath(DIFF_FILE));
		settings.setSuccessAssertionsFile(
				ctx.getRealPath(SUCCESS_ASSERTIONS_FILE));
		settings.setSuccessRDFFormat(ctx.getRealPath(SUCCESS_RDF_FORMAT));
		settings.setLogFile(ctx.getRealPath(LOG_FILE));
		settings.setErrorLogFile(ctx.getRealPath(ERROR_LOG_FILE));
		settings.setRemovedDataFile(ctx.getRealPath(REMOVED_DATA_FILE));
		WebappDaoFactory wadf = (WebappDaoFactory) ctx.getAttribute("webappDaoFactory");
		settings.setDefaultNamespace(wadf.getDefaultNamespace());
		
		try {
			(new OntologyUpdater(settings)).update(); 
		} catch (IOException ioe) {
			String errMsg = "IOException updating knowledge base " +
				"for ontology changes: ";
			// Tomcat doesn't always seem to print exceptions thrown from
			// context listeners
			System.out.println(errMsg);
			ioe.printStackTrace();
			throw new RuntimeException(errMsg, ioe);
		}	
		
		*/
		
	}	
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing to do	
	}

}
