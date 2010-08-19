/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

//import org.openrdf.repository.Repository;
//import org.openrdf.repository.RepositoryConnection;
//import org.openrdf.repository.RepositoryResult;
//import org.openrdf.repository.http.HTTPRepository;
//import org.openrdf.repository.sail.SailRepository;
//import org.openrdf.rio.RDFFormat;
//import org.openrdf.sail.memory.MemoryStore;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class JenaToExternalTripleStoreSynchronizer extends StatementListener implements ServletContextListener {

	private ExternalTripleStoreInterface extStore;
	
	public JenaToExternalTripleStoreSynchronizer(ExternalTripleStoreInterface extStore, Model additionsQueue, Model retractionsQueue) {
		this.extStore = extStore;
	}
	
	private Model additionsQueue = ModelFactory.createDefaultModel();
	private Model retractionsQueue = ModelFactory.createDefaultModel();
	
	// TODO: add a statement filter to filter out password props
	
	@Override
	public void addedStatement(Statement stmt) {
		if (!retractionsQueue.contains(stmt)) {
			additionsQueue.add(stmt);
		} else {
			retractionsQueue.remove(stmt);
		}
//		try {
			StmtIterator sit = retractionsQueue.listStatements();
			while (sit.hasNext()) {
				Statement s = sit.nextStatement();
				
			}
//		} catch (TripleStoreUnavailableException e) {
//			// log something?
//		} 
	}
	
	@Override
	public void removedStatement(Statement stmt) {
		processUpdate(stmt, REMOVE);
	}
	
	public void processUpdate(Statement stmt, boolean mode) {
//		Repository myRepository = new HTTPRepository(sesameServer, repositoryID);
//		myRepository.initialize();
//		RepositoryConnection myConn = myRepository.getConnection();
//		System.out.println(myConn.size()+" statements in remote Sesame");
//		
//		Repository tempRepository = new SailRepository(new MemoryStore());
//		tempRepository.initialize();
//		tempRepository.getConnection().add(retractionsFile, null, RDFFormat.N3);
//		System.out.println(tempRepository.getConnection().size()+" statements to retract");
//		RepositoryResult<Statement> retractStmts = tempRepository.getConnection().getStatements(null, null, null, false);
//		System.out.println("Retracting statements from repository...");
//		//myConn.remove(retractStmts);
//		while (retractStmts.hasNext()) {
//			Statement stmt = retractStmts.next();
//			myConn.remove(stmt.getSubject(), stmt.getPredicate(), null);
//		}
	}
	
	public static final boolean ADD = true;
	public static final boolean REMOVE = false;
	
	public static final int SESAME_SERVER_URI = 0;
	public static final int SESAME_REPOSITORY_NAME = 1;
	
	public List<String[]> getSesameRepositories() {
		List<String[]> sesameRepositoryList = new ArrayList<String[]>();
		String[] testRepository = new String[2];
		testRepository[SESAME_SERVER_URI] = "http://vivosesame.mannlib.cornell.edu:8080/openrdf-sesame";
		testRepository[SESAME_REPOSITORY_NAME] = "syncTest";
		sesameRepositoryList.add(testRepository);
		return sesameRepositoryList;
	}
	
	public void contextInitialized(ServletContextEvent sce) {
		
		OntModel jenaOntModel = (OntModel) sce.getServletContext().getAttribute(JenaBaseDao.JENA_ONT_MODEL_ATTRIBUTE_NAME);
		jenaOntModel.getBaseModel().register(this);
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		
	}
	
	
	
}
