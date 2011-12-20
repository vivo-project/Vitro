/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.MenuManagementDataUtils;

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

import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.PageDataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.DataGetterUtils;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.SelectDataGetterUtils;

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
    	data.put("formUrls", vreq.getContextPath() + SUBMIT_FORM);
    	data.put("cancelUrl", vreq.getContextPath() + CANCEL_FORM);
     	MenuManagementDataUtils.includeRequiredSystemData(getServletContext(), data);
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
    	data.put("title", "Remove Menu Item");
    	//Generate empty values for fields
    	data.put("prettyUrl", "");
    	data.put("associatedPage", "");
    	data.put("associatedPageURI", "");
    	data.put("classGroup", new ArrayList<String>());
    	//not a page already assigned a class group
    	data.put("isClassGroupPage", false);
    	data.put("includeAllClasses", false);
    	data.put("classGroups", DataGetterUtils.getClassGroups(getServletContext()));
    	data.put("selectedTemplateType", "default");
    	//
    	this.getMenuItemData(vreq, menuItem, data);
    	this.getPageData(vreq, data);    	
	}

	private void processAddMenuItem(VitroRequest vreq, Map<String, Object> data) {
    	data.put("title", "Add Menu Item");
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
    	data.put("classGroups", DataGetterUtils.getClassGroups(getServletContext()));
    	data.put("selectedTemplateType", "default");
    	//defaults to regular class group page
	}

	private void processEditMenuItem(VitroRequest vreq, Map<String, Object> data) {
		if(!hasMenuItem(vreq)) {
    		return;
    	}
		data.put("title", "Edit Menu Item");
		//Get parameter for menu item
    	String menuItem = getMenuItem(vreq);
    	data.put("menuItem", menuItem);
    	data.put("menuAction", "Edit");
    	//Get All class groups
    	data.put("classGroups", DataGetterUtils.getClassGroups(getServletContext()));
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
	    	this.getPageDataGetterInfo(vreq, writeModel, page, data);
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
    private void getPageDataGetterInfo(VitroRequest vreq, OntModel writeModel, Resource page, Map<String, Object> data) {
    	//Alternative is to do this via sparql query
    	StmtIterator dataGetterIt = writeModel.listStatements(page, ResourceFactory.createProperty(DisplayVocabulary.HAS_DATA_GETTER), (RDFNode) null);
    	while(dataGetterIt.hasNext()) {
    		Statement dataGetterStmt = dataGetterIt.nextStatement();
    		Resource dataGetter = dataGetterStmt.getResource();
    		//Get types of data getter
    		StmtIterator dataGetterTypes = writeModel.listStatements(dataGetter, RDF.type, (RDFNode) null);
    		while(dataGetterTypes.hasNext()) {
    			String dataGetterType = dataGetterTypes.nextStatement().getResource().getURI();
    			this.retrieveData(vreq, page, dataGetterType, data);
    		}
    	}
    
    }
  
    private void retrieveData(VitroRequest vreq, Resource page, String dataGetterType,  Map<String, Object> templateData) {
    	//Data Getter type is now a class name
    	String className = DataGetterUtils.getClassNameFromUri(dataGetterType);
    	try{
    		String pageURI = page.getURI();
    		PageDataGetter pg = (PageDataGetter) Class.forName(className).newInstance();
    		Map<String, Object> pageInfo = DataGetterUtils.getMapForPage( vreq, pageURI );

    		Map<String, Object> pageData = DataGetterUtils.getAdditionalData(pageURI, dataGetterType, pageInfo, vreq, pg, getServletContext());
    		SelectDataGetterUtils.processAndRetrieveData(vreq, getServletContext(), pageData, className, templateData);
    	} catch(Exception ex) {
    		log.error("Exception occurred in instantiation page data getter for " + className, ex);
    	}
    	
		
	}

    

    
    
    
    
}
