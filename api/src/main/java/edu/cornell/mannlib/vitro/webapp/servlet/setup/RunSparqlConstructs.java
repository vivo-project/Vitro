/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.FULL_ASSERTIONS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

public class RunSparqlConstructs implements ServletContextListener {

	private static final Log log = LogFactory.getLog(RunSparqlConstructs.class.getName());
	
	private static final String DEFAULT_DEFAULT_NAMESPACE = "http://vivo.library.cornell.edu/ns/0.1#individual";
	private static final String LOCAL_NAME_PREPEND = "individual";
	
	private static final String SPARQL_DIR = "/WEB-INF/sparqlConstructs";
	private static Random random = new Random(System.currentTimeMillis());
	
	public void contextInitialized(ServletContextEvent sce) {
		try {
			ServletContext ctx = sce.getServletContext();
			WebappDaoFactory wadf = ModelAccess.on(ctx).getWebappDaoFactory();

			String namespace = (wadf != null && wadf.getDefaultNamespace() != null) 
				? wadf.getDefaultNamespace() : DEFAULT_DEFAULT_NAMESPACE;
			
		    OntModel baseOntModel = ModelAccess.on(ctx).getOntModel(FULL_ASSERTIONS);
			Model anonModel = ModelFactory.createDefaultModel();
			Model namedModel = ModelFactory.createDefaultModel();
			
			Set<String> resourcePaths = ctx.getResourcePaths(SPARQL_DIR);
			for (String path : resourcePaths) {
				log.debug("Attempting to execute SPARQL at " + path);
				File file = new File(ctx.getRealPath(path));			
				try {
					BufferedReader reader = new BufferedReader(new FileReader(file));
					StringBuffer fileContents = new StringBuffer();
					String ln;
					try {
						while ( (ln = reader.readLine()) != null) {
							fileContents.append(ln).append('\n');
						}
						try {
							Query q = QueryFactory.create(fileContents.toString(), Syntax.syntaxARQ);
							QueryExecution qe = QueryExecutionFactory.create(q, baseOntModel);
							qe.execConstruct(anonModel);
						} catch (Exception e) {
							String queryErrMsg = "Unable to execute query at " + path + " :";
							log.error(queryErrMsg);
							System.out.println(queryErrMsg);
							e.printStackTrace();
						}
					} catch (IOException ioe) {
						log.error("IO Exception reading " + path + " :");
						ioe.printStackTrace();
					}
				} catch (FileNotFoundException fnfe) {
					log.info(path + " not found. Skipping.");
				}	
			}
			
			namedModel.add(anonModel);
			
			for ( Iterator<Resource> i = anonModel.listSubjects(); i.hasNext(); ) {
				Resource s = i.next();
				if (s.isAnon()) {
					int randomInt = -1;
					while ( randomInt < 0 || baseOntModel.getIndividual(namespace + LOCAL_NAME_PREPEND + randomInt) != null ) {
						randomInt = random.nextInt(Integer.MAX_VALUE);
					}
					Resource t = namedModel.createResource(s.getId());
					ResourceUtils.renameResource(t, namespace + LOCAL_NAME_PREPEND + randomInt);
				}
			}
			
			baseOntModel.addSubModel(namedModel);
			String msg = "Attaching " + namedModel.size() + " statements as a result of SPARQL CONSTRUCTS";
			log.info(msg);
			System.out.println(msg);

		} catch (Throwable t) {
			System.out.println("Throwable in listener " + this.getClass().getName());
			t.printStackTrace();
			log.error(t);
		}
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		// nothing
	}

}
