/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletContext;

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

public class ExtendedLinkedDataUtils {
	
	private static final Log log = LogFactory.getLog(ExtendedLinkedDataUtils.class.getName());
	
	public static Model createModelFromQueries(ServletContext sc, String rootDir, OntModel sourceModel, String subject) {
		log.debug("Exploring queries in directory '" + rootDir + "'");
		
		Model model = ModelFactory.createDefaultModel(); 
		
		@SuppressWarnings("unchecked")
		Set<String> pathSet = sc.getResourcePaths(rootDir);
		
		if (pathSet == null) {
		  log.warn(rootDir + " not found.");
		  return model;
		}
		
		for ( String path : pathSet ) {
            File file = new File(sc.getRealPath(path));	
            if (file.isDirectory()) {           	
            	model.add(createModelFromQueries(sc, path, sourceModel, subject));
            } else if (file.isFile()) { 
    			if (!path.endsWith(".sparql")) {
    				log.warn("Ignoring file " + path + " because the file extension is not sparql.");
    				continue;
    			}
            	model.add(createModelFromQuery(file, sourceModel, subject));
				log.debug("model size is " + model.size() + " after query in '"
						+ path + "'");
            } else {
            	log.warn("path is neither a directory nor a file " + path);
            }
		} // end - for
				
		return model;
	}	
	
	public static Model createModelFromQuery(File sparqlFile, OntModel sourceModel, String subject) {
		
		Model model = ModelFactory.createDefaultModel(); 
						
		BufferedReader reader = null;
		
		try {
			try {
				reader = new BufferedReader(new FileReader(sparqlFile));
				StringBuffer fileContents = new StringBuffer();
				String ln;
			
				while ( (ln = reader.readLine()) != null) {
					fileContents.append(ln).append('\n');
				}		
						
				String query = fileContents.toString();
				String subjectString = "<" + subject + ">";
				query = query.replaceAll("PERSON_URI", subjectString);
				
				Query q = QueryFactory.create(query, Syntax.syntaxARQ);
				QueryExecution qe = QueryExecutionFactory.create(q, sourceModel);
				qe.execConstruct(model);
		   	} catch (Exception e) {
				log.error("Unable to process file " + sparqlFile.getAbsolutePath(), e);
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		} catch (IOException ioe) {
			// this is for the reader.close above
			log.warn("Exception while trying to close file: " + sparqlFile.getAbsolutePath(), ioe);
		}			
				
		return model;
	}	
}
