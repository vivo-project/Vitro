/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelSynchronizer;

/**
 * This is the beginning of a more sane and flexible model management system,
 * especially necessary for DataStaR.
 * Don't use it yet; it's going to change.
 * (That part is still insane, I know.)
 * @author bjl23
 */
public class AssembleModelsSetup implements ServletContextListener {

	private static final Log log = LogFactory.getLog(AssembleModelsSetup.class);
	
	private List<Model> assembledModels = new LinkedList<Model>();
	
	private String ASSEMBLERS_DIR_PATH = "/WEB-INF/assemblers/";
	private Resource ASSEMBLER_OBJECT = ResourceFactory.createProperty("http://jena.hpl.hp.com/2005/11/Assembler#Object");
	private String SYNTAX = "N3";
	
	public void contextInitialized(ServletContextEvent sce) {
	    
		OntModel jenaOntModel = null;
		try {
			jenaOntModel = (OntModel) sce.getServletContext().getAttribute("baseOntModel");
		} catch (Exception e) {
			log.error("No baseOntModel found to which to attach assembled models");
			return;
		}
		// read assemblers out of assemblers directory
		Set pathSet = sce.getServletContext().getResourcePaths(ASSEMBLERS_DIR_PATH);
		for (String path : (Set<String>)pathSet) {
			InputStream assemblerInputStream = sce.getServletContext().getResourceAsStream(path);
			Model assemblerModel = ModelFactory.createDefaultModel();
			try {
				assemblerModel.read(assemblerInputStream, null, SYNTAX);
				ExtendedIterator assemblerIt = assemblerModel.listResourcesWithProperty(RDF.type,ASSEMBLER_OBJECT);
				while (assemblerIt.hasNext()) {
					Resource assemblerObj = (Resource) assemblerIt.next();
					Model assembledModel = Assembler.general.openModel(assemblerObj);
					/* special stuff here */
					Model memModel = ModelFactory.createDefaultModel();
					memModel.add(assembledModel);
					memModel.register(new ModelSynchronizer(assembledModel));
					/* end special stuff */
					if (assembledModel != null) {
						jenaOntModel.addSubModel(memModel);
					}
				}
				if (assemblerIt != null) {
					assemblerIt.close();
				}
			} catch (Exception e) {
				log.error("Unable to use assembler at "+path);
			}
		}
		System.out.println("ContextListener AssembleModelsSetup done");
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		for (Model model : assembledModels) {
			if (model != null) {
				model.close();
			}
		}
	}

}
