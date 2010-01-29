package edu.cornell.mannlib.vitro.webapp.utils.jena;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;

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
		
		//rename tabs
		List<AnonId> tabIds = new ArrayList<AnonId>();
		Iterator<Resource> tabResIt = initialModel.listSubjectsWithProperty(RDF.type, initialModel.getResource(VitroVocabulary.TAB));
		while (tabResIt.hasNext()) {
			Resource tabRes = tabResIt.next();
			if (tabRes.isAnon()) {
				tabIds.add(tabRes.getId());
			}
		}
		int tabIdInt = 0;
		for (AnonId tabId : tabIds) {
			tabIdInt++;
			ResourceUtils.renameResource(initialModel.createResource(tabId), defaultNamespace + "tab" + tabIdInt);
		}
		
		return initialModel;
		
	}
	
	public static Model basicPortalAndRootTab(String defaultNamespace) {

		OntModel essentialInterfaceData = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Resource portalClass = essentialInterfaceData.getResource(VitroVocabulary.PORTAL);
        Property themeDirProperty = essentialInterfaceData.getProperty(VitroVocabulary.PORTAL_THEMEDIR);
        Property flag1FilteringProperty = essentialInterfaceData.getProperty(VitroVocabulary.PORTAL_FLAG1FILTERING);
        Resource tabClass = essentialInterfaceData.getResource(VitroVocabulary.TAB);
        Resource primaryTabClass = essentialInterfaceData.getResource(VitroVocabulary.TAB_PRIMARYTAB);
        Property rootTabProperty = essentialInterfaceData.getProperty(VitroVocabulary.PORTAL_ROOTTAB);
        Property tabInPortalProperty = essentialInterfaceData.getProperty(VitroVocabulary.TAB_PORTAL);

        Individual portal1 = essentialInterfaceData.createIndividual(defaultNamespace+"portal1",portalClass);
        String defaultThemeStr = Portal.DEFAULT_THEME_DIR_FROM_CONTEXT;
        if (defaultThemeStr == null) {
        	throw new RuntimeException("No default theme has been set; unable to create default portal.");      	
        }
        portal1.setPropertyValue(themeDirProperty,ResourceFactory.createPlainLiteral(defaultThemeStr));
		portal1.setPropertyValue(flag1FilteringProperty, essentialInterfaceData.createTypedLiteral(true));
		portal1.setLabel("New Vitro Portal", null);
		Individual rootTab = essentialInterfaceData.createIndividual(defaultNamespace+"tab1",tabClass);
		rootTab.setLabel("Home", null);
		rootTab.addProperty(RDF.type, primaryTabClass);
		rootTab.addProperty(tabInPortalProperty, portal1);
		portal1.addProperty(rootTabProperty, rootTab);
		
		return essentialInterfaceData;
		
	}
	
}
