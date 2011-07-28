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

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class InitialJenaModelUtils {

	private static final String INIT_DATA_DIRECTORY = "/WEB-INF/init-data";
	
	private static final Log log = LogFactory.getLog(InitialJenaModelUtils.class.getName());
	
	public static Model loadInitialModel(ServletContext ctx, String defaultNamespace) {
	
		OntModel initialModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		
		try {
			JenaDataSourceSetupBase.readOntologyFilesInPathSet(INIT_DATA_DIRECTORY, ctx, initialModel);
		} catch (Throwable t) {
			log.warn("Unable to read initial model data from " + INIT_DATA_DIRECTORY, t);
		}
	
		if (initialModel.size() == 0) {
			return initialModel;
		}
		
		//find and rename portal object
		//currently, only a single portal is permitted in the initialization data
		Resource portalResource = null;
		ClosableIterator<Resource> portalResIt = initialModel.listSubjectsWithProperty(RDF.type, initialModel.getResource(VitroVocabulary.PORTAL));
		try {
			if (portalResIt.hasNext()) {
				Resource portalRes = portalResIt.next();
				if (portalRes.isAnon()) {
					portalResource = portalRes;
				}
			}
		} finally {
			portalResIt.close();
		}
		if (portalResource != null) {
			ResourceUtils.renameResource(portalResource, defaultNamespace + "portal1");
		}
		
		return initialModel;
		
	}
	
	public static Model basicInterfaceData(String defaultNamespace) {
		OntModel essentialInterfaceData = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Resource portalClass = essentialInterfaceData.getResource(VitroVocabulary.PORTAL);
        Property themeDirProperty = essentialInterfaceData.getProperty(VitroVocabulary.PORTAL_THEMEDIR);
        Individual portal1 = essentialInterfaceData.createIndividual(defaultNamespace+"portal1",portalClass);
        String defaultThemeStr = ApplicationBean.DEFAULT_THEME_DIR_FROM_CONTEXT;
        if (defaultThemeStr == null) {
        	throw new RuntimeException("No default theme has been set; unable to create default portal.");      	
        }
        portal1.setPropertyValue(themeDirProperty,ResourceFactory.createPlainLiteral(defaultThemeStr));
		portal1.setLabel("Vitro", null);	
		return essentialInterfaceData;		
	}
	
	public static Model basicClassgroup(String defaultNamespace) {
		OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		Individual thingsClassGroup = m.createIndividual(
				defaultNamespace+"vitroClassGroupThings", 
				m.createResource(VitroVocabulary.CLASSGROUP));
		thingsClassGroup.addLabel("Things", null);
		thingsClassGroup.addProperty(
				m.getProperty(VitroVocabulary.DISPLAY_RANK_ANNOT),
				"50", XSDDatatype.XSDint);
		m.add(OWL.Thing, 
				m.getProperty(VitroVocabulary.IN_CLASSGROUP), thingsClassGroup);
		return m;
	}
				
}
