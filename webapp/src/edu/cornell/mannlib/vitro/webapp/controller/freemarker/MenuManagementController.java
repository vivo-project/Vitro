/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.ManageMenus;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.beans.VClassGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VClassGroupCache;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

/*
 * Custom controller for menu management.  This will be replaced later once N3 Editing
 * has been successfully refactored and integrated with menu management.
 */
public class MenuManagementController extends FreemarkerHttpServlet {
    private static final Log log = LogFactory.getLog(MenuManagementController.class);
    protected final static String SUBMIT_FORM = "/processEditDisplayModel"; 
    protected final static String CANCEL_FORM = "/individual?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fontologies%2Fdisplay%2F1.1%23DefaultMenu&switchToDisplayModel=true"; 
    protected final static String DELETE_FORM = "menuManagement-remove.ftl";
    protected final static String EDIT_FORM = "menuManagement.ftl"; 
    protected final static String CMD_PARAM = "cmd";
    protected final static String EDIT_PARAM_VALUE = "edit";
    protected final static String DELETE_PARAM_VALUE = "delete";
    protected final static String ADD_PARAM_VALUE = "add";
    //since forwarding from edit Request dispatch for now
    
    protected final static String ITEM_PARAM = "objectUri";
     
    public final static Actions REQUIRED_ACTIONS = new Actions(new ManageMenus());
    
    @Override
    protected Actions requiredActions(VitroRequest vreq) {
    	return REQUIRED_ACTIONS;
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
       //Parameters should include the menu item being edited/added/removed/reordered
    	Map<String, Object> data = new HashMap<String,Object>();
    	this.initializeData(data, vreq);

    	//if no menu item passed, return empty data
    	//TODO: Check if exception needs to be thrown   		
    	String cmd = getCommand(vreq); 
    	String template = EDIT_FORM;
    	if(cmd.equals(ADD_PARAM_VALUE)) {
    		processAddMenuItem(vreq, data);
    	} else if(cmd.equals(EDIT_PARAM_VALUE)) {
    		processEditMenuItem(vreq, data);
    	} else if(cmd.equals(DELETE_PARAM_VALUE)) {
    		processDeleteMenuItem(vreq, data);
    		template = DELETE_FORM;
    	} else {
    		//Throw some kind of error or do nothing
    	}
    	
    	return new TemplateResponseValues(template, data);
    	
    }
    
    //Certain parameters are always passed
    private void initializeData(Map<String, Object> data, VitroRequest vreq) {
    	//Form url submission
    	data.put("formUrls", vreq.getContextPath() + SUBMIT_FORM);
    	data.put("cancelUrl", vreq.getContextPath() + CANCEL_FORM);
    	data.put("internalClassUri", "");
    	
    }

    //Based on parameters, ascertain command
    private String getCommand(VitroRequest vreq) {
    	String command = vreq.getParameter(CMD_PARAM);
    	if(command == null || command.isEmpty()) {
    		//Check if objectUri null, if exists then edit otherewise add
    		String objectUri = vreq.getParameter("objectUri");
    		if(objectUri == null || objectUri.isEmpty()) {
    			command = ADD_PARAM_VALUE;
    		} else {
    			command = EDIT_PARAM_VALUE;
    		}
    	}
		return command;
	}

	private void processDeleteMenuItem(VitroRequest vreq , Map<String, Object> data) {
		String menuItem = getMenuItem(vreq);
    	data.put("menuItem", menuItem);
    	data.put("menuAction", "Remove");
    	//Generate empty values for fields
    	//TODO: Remove these if only portion of template utilized
   
    
    	data.put("prettyUrl", "");
    	data.put("associatedPage", "");
    	data.put("associatedPageURI", "");
    	data.put("classGroup", new ArrayList<String>());
    	//not a page already assigned a class group
    	data.put("isClassGroupPage", false);
    	data.put("includeAllClasses", false);
    	data.put("classGroups", this.getClassGroups());
    	data.put("selectedTemplateType", "default");
    	//
    	this.getMenuItemData(vreq, menuItem, data);
    	this.getPageData(vreq, data);    	
	}

	private void processAddMenuItem(VitroRequest vreq, Map<String, Object> data) {
    	data.put("menuAction", "Add");
    	//Generate empty values for fields
    	data.put("menuItem", "");
    	data.put("menuName", "");
    	data.put("prettyUrl", "");
    	data.put("associatedPage", "");
    	data.put("associatedPageURI", "");
    	data.put("classGroup", new ArrayList<String>());
    	//not a page already assigned a class group
    	data.put("isClassGroupPage", false);
    	data.put("includeAllClasses", false);
    	data.put("classGroups", this.getClassGroups());
    	data.put("selectedTemplateType", "default");
    	//defaults to regular class group page
     	//Check whether institutional internal class exists
		this.checkInstitutionalInternalClass(data);
	}

	private void processEditMenuItem(VitroRequest vreq, Map<String, Object> data) {
		if(!hasMenuItem(vreq)) {
    		return;
    	}
		//Get parameter for menu item
    	String menuItem = getMenuItem(vreq);
    	data.put("menuItem", menuItem);
    	data.put("menuAction", "Edit");
    	//Get All class groups
    	data.put("classGroups", this.getClassGroups());
     	//Check whether institutional internal class exists
		this.checkInstitutionalInternalClass(data);
    	//Get data for menu item and associated page
    	this.getMenuItemData(vreq, menuItem, data);
    	this.getPageData(vreq, data);    	
	}
    
    private String getMenuItem(VitroRequest vreq) {
    	return vreq.getParameter(ITEM_PARAM);
    }
    
    private boolean hasMenuItem(VitroRequest vreq) {
    	return (getMenuItem(vreq) != null && !getMenuItem(vreq).isEmpty());
    }
	

    /*
     * Sparql queries and data
     */
    
    
    
    private void getMenuItemData(VitroRequest vreq, String menuItem, Map<String, Object> data) {
    	OntModel writeModel = vreq.getWriteModel();
    	Individual item = writeModel.getIndividual(menuItem);
    	if(item != null) {
	    	StmtIterator it = item.listProperties(DisplayVocabulary.LINK_TEXT);
	    	
	    	if(it.hasNext()) {
	    		String linkText = it.nextStatement().getLiteral().getString();
	    		log.debug("Link text retrieved is " + linkText);
	    		//stored as menu name
	    		data.put("menuName", linkText);
	    	}
	    	StmtIterator pageIt = item.listProperties(DisplayVocabulary.TO_PAGE);
	    	if(pageIt.hasNext()) {
	    		Resource pageResource = pageIt.nextStatement().getResource();
	    		String pageUri = pageResource.getURI();
	    		log.debug("Page URI is " + pageUri);
	    		data.put("page", pageUri);
	    	}
    	}
    	
    	
    }
    
    //pretty-url, also type
    private void getPageData(VitroRequest vreq, Map<String, Object> data) {
    	String pageUri = (String) data.get("page");
    	OntModel writeModel = vreq.getWriteModel();
    	Individual page = writeModel.getIndividual(pageUri);
    	if(page != null) {
    		StmtIterator urlMappingIt = page.listProperties(DisplayVocabulary.URL_MAPPING);
	    	
	    	if(urlMappingIt.hasNext()) {
	    		String urlMapping = urlMappingIt.nextStatement().getLiteral().getString();
	    		log.debug("URL Mapping retrieved is " + urlMapping);
	    		data.put("prettyUrl", urlMapping);
	    	}
	    	//If home page, then specify?
	    	this.checkHomePage(writeModel, page, data);
	    	//Check if custom template required and if so save the info, 
	    	this.getCustomTemplate(writeModel, page, data);
	    	//retrieve information for page based on the data getter, with class group and individuals for classes getting different information
	    	//the browse page does not have a "data getter"
	    	this.getPageDataGetterInfo(writeModel, page, data);
	    	//This is an all statement iterator
	    	log.debug("Debug statements: all statements in model for debugger");
	    	StmtIterator debugIt = writeModel.listStatements(page, null, (RDFNode) null);
	    	while(debugIt.hasNext()) {
	    		log.debug("Statement: " + debugIt.nextStatement().toString());
	    	}
    	}
    }
    
    
    private void checkHomePage(OntModel writeModel, Individual page,
			Map<String, Object> data) {
		StmtIterator homePageIt = writeModel.listStatements(page, RDF.type, ResourceFactory.createResource(DisplayVocabulary.HOME_PAGE_TYPE));
		if (homePageIt.hasNext()) {
			data.put("isHomePage", true);
			data.put("isClassGroupPage", false);
        	//Home Page does not have a "group" associated with
        	data.put("associatedPage", "");
        	data.put("associatedPageURI", "");
        	data.put("classGroup", new ArrayList<String>());
        	data.put("includeAllClasses", false);
	    	
		}
	}
    
    //If custom template included, get that information
    private void getCustomTemplate(OntModel writeModel, Individual page,
			Map<String, Object> data) {
    	StmtIterator customTemplateIt = writeModel.listStatements(page, DisplayVocabulary.REQUIRES_BODY_TEMPLATE, (RDFNode) null);
		if (customTemplateIt.hasNext()) {
			String customTemplate = customTemplateIt.nextStatement().getLiteral().getString();
			data.put("selectedTemplateType", "custom");
			data.put("customTemplate", customTemplate);
		} else {
			data.put("selectedTemplateType", "default");
		}
    }

	//Get data getter related info
    //All items will have data getter except for Browse or Home page
    //Home can be edited but not removed
    private void getPageDataGetterInfo(OntModel writeModel, Resource page, Map<String, Object> data) {
    	
    	
    	//Alternative is to do this via sparql query
    	StmtIterator dataGetterIt = writeModel.listStatements(page, ResourceFactory.createProperty(DisplayVocabulary.HAS_DATA_GETTER), (RDFNode) null);
    	while(dataGetterIt.hasNext()) {
    		Statement dataGetterStmt = dataGetterIt.nextStatement();
    		Resource dataGetter = dataGetterStmt.getResource();
    		//Get types of data getter
    		StmtIterator dataGetterTypes = writeModel.listStatements(dataGetter, RDF.type, (RDFNode) null);
    		while(dataGetterTypes.hasNext()) {
    			String dataGetterType = dataGetterTypes.nextStatement().getResource().getURI();
    			if(dataGetterType.equals(DisplayVocabulary.CLASSGROUP_PAGE_TYPE)) {
    				this.retrieveClassGroupPage(writeModel, dataGetter, data);
    			} else if(dataGetterType.equals(DisplayVocabulary.CLASSINDIVIDUALS_INTERNAL_TYPE)) {
    				this.retrieveIndividualsForClassesPage(writeModel, dataGetter, data);
    			} else {
    				//Not sure what to do here
    			}
    		}
    	}
    
    }
  
    //Based on institutional internal page and not general individualsForClasses
    private void retrieveIndividualsForClassesPage(OntModel writeModel,
		Resource dataGetter, Map<String, Object> data) {
		data.put("isIndividualsForClassesPage", true);
		data.put("isClassGroupPage", false);
		data.put("includeAllClasses", false);
		//Get the classes and put them here
		this.getClassesForInternalDataGetter(writeModel, dataGetter, data);
		//Also save the class group for display
		this.getClassGroupForDataGetter(writeModel, dataGetter, data);
		this.checkIfPageInternal(writeModel, data);
		
	}

	private void checkIfPageInternal(OntModel writeModel,
			Map<String, Object> data) {
		//if internal class exists, and data getter indicates page is internal
		if(data.containsKey("internalClass") && data.containsKey("isInternal")) {
			data.put("pageInternalOnly", true);
			
		}
		
	}

	private void retrieveClassGroupPage(OntModel writeModel, Resource dataGetter,
			Map<String, Object> data) {
		//This is a class group page so 
		data.put("isClassGroupPage", true);
		data.put("includeAllClasses", true);
		
		//Get the class group
		this.getClassGroupForDataGetter(writeModel, dataGetter, data);
		
	}

	//Instead of returning vclasses, just returning class Uris as vclasses appear to need their own template
	//to show up correctly
	private void getClassesForInternalDataGetter(OntModel writeModel, Resource dataGetter,
			Map<String, Object> data) {
    	

		StmtIterator classesIt = writeModel.listStatements(dataGetter, 
				ResourceFactory.createProperty(DisplayVocabulary.GETINDIVIDUALS_FOR_CLASS), 
				(RDFNode) null);
		
    	//Just need the class uris
    	List<String> classUris = new ArrayList<String>();

		while(classesIt.hasNext()) {
			String classUri = classesIt.nextStatement().getResource().getURI();
    		classUris.add(classUri);
		}
		data.put("includeClasses", classUris);
		
		//This checks whether restrict classes returned and include institutional internal class
		//TODO: Create separate method to get restricted classes
		//Get restrict classes - specifically internal class 
		
		StmtIterator internalIt = writeModel.listStatements(dataGetter, 
				ResourceFactory.createProperty(DisplayVocabulary.RESTRICT_RESULTS_BY_INTERNAL), 
				(RDFNode) null);
		if(internalIt.hasNext()) {
			data.put("isInternal", internalIt.nextStatement().getLiteral().getString());
		}
		
	}
	
	
	//Check whether any classes exist with internal class restrictions
	private void checkInstitutionalInternalClass(Map<String, Object> data) {
		//TODO: replace with more generic ModelContext retrieval method
		OntModel mainModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
		StmtIterator internalIt = mainModel.listStatements(null, ResourceFactory.createProperty(VitroVocabulary.IS_INTERNAL_CLASSANNOT), (RDFNode) null);
		//List<String> internalClasses = new ArrayList<String>();
		if(internalIt.hasNext()) {			
			String internalClass = internalIt.nextStatement().getSubject().getURI();
			data.put("internalClass", internalClass);
			data.put("internalClassUri", internalClass);
		}
		
	}

    //Get the class page
	private void getClassGroupForDataGetter(OntModel writeModel, Resource dataGetter,
			Map<String, Object> data) {
		StmtIterator classGroupIt = writeModel.listStatements(dataGetter, 
				ResourceFactory.createProperty(DisplayVocabulary.FOR_CLASSGROUP), 
				(RDFNode) null);
		//Assuming just one class group per page/item
		if(classGroupIt.hasNext()) {
			String classGroup = classGroupIt.nextStatement().getResource().getURI();
			VClassGroup vclassGroup = getClassGroup(classGroup);
			data.put("classGroup", vclassGroup);
			data.put("associatedPage", vclassGroup.getPublicName());
			data.put("associatedPageURI", vclassGroup.getURI());
		}
		
	}

	//Get classes in class group, useful in case of edit
    private VClassGroup getClassGroup(String classGroupUri) {
    	VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(getServletContext());
    	VClassGroup group = vcgc.getGroup(classGroupUri);
    	return group;
    }
    
    //Get All VClass Groups
    private List<HashMap<String, String>> getClassGroups() {
    	//Wanted this to be 
    	VClassGroupCache vcgc = VClassGroupCache.getVClassGroupCache(getServletContext());
        List<VClassGroup> vcgList = vcgc.getGroups();
        //For now encoding as hashmap with label and URI as trying to retrieve class group
        //results in errors for some reason
        //TODO: Check how to do this properly
        List<HashMap<String, String>> classGroups = new ArrayList<HashMap<String, String>>();
        for(VClassGroup vcg: vcgList) {
        	HashMap<String, String> hs = new HashMap<String, String>();
        	hs.put("publicName", vcg.getPublicName());
        	hs.put("URI", vcg.getURI());
        	classGroups.add(hs);
        }
        return classGroups;
    }
    
    
    
    
}
