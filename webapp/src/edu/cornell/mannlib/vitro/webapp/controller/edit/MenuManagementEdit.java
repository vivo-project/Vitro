/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import com.hp.hpl.jena.ontology.OntModel;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.SelectDataGetterUtils;

/**
 *Process edits from display model editing, so form should submit to this page which should
 *then process the parameters and then make the necessary changes to the model.
 */
public class MenuManagementEdit extends VitroHttpServlet {
   private static final String CMD_PARAM = "cmd";
   private final static String EDIT_FORM = "testMenuManagement.ftl"; 
   private final static String EDIT_PARAM_VALUE = "Edit";
   private final static String DELETE_PARAM_VALUE = "Remove";
   private final static String ADD_PARAM_VALUE = "Add";
   private final static String REORDER_PARAM_VALUE = "Reorder";
   private final static String REDIRECT_URL = "/individual?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fontologies%2Fdisplay%2F1.1%23DefaultMenu&switchToDisplayModel=true";
   private static Model removeStatements = null; 
   private static Model addStatements = null; 
    
    @Override
    protected void doPost(HttpServletRequest rawRequest, HttpServletResponse resp)
            throws ServletException, IOException {
    	/*
           RequestDispatcher rd = request
                    .getRequestDispatcher("/edit/postEditCleanUp.jsp");
            rd.forward(request, resp);*/
    	removeStatements = ModelFactory.createDefaultModel();
    	addStatements = ModelFactory.createDefaultModel();
    	VitroRequest vreq = new VitroRequest(rawRequest);
    	
    	String command = getCommand(vreq);
    	if(command != null) {
    		processCommand(command, vreq, resp);
    	} else {
    		log.error("Command is null");
    	}
        //Need to redirect correctly
    	if(!isReorder(command)){
    		resp.sendRedirect(rawRequest.getContextPath() + REDIRECT_URL);
    	} else {
    		//Provide some JSON message back to reorder
    	}
    }


	public String getCommand(VitroRequest vreq) {
    	String command = vreq.getParameter(CMD_PARAM);
    	return command;
    }
    
    public boolean isEdit(String command) {
    	return command.equals(EDIT_PARAM_VALUE);
    }
    
    public boolean isAdd(String command) {
    	return command.equals(ADD_PARAM_VALUE);
    }
    
    public boolean isDelete(String command) {
    	return command.equals(DELETE_PARAM_VALUE);
    }
    
    public boolean isReorder(String command) {
    	return command.equals(REORDER_PARAM_VALUE);
    }
    
    public boolean isHomePage(String uri) {
    	return uri.equals(DisplayVocabulary.DISPLAY_NS + "Home");
    }
    
    //Parameter retrieval is identical, but in one case an entirey new menu item needs to be created
    //along with a new page
    public void processCommand(String command, VitroRequest vreq, HttpServletResponse resp) {
    	//Get parameters for menu item being edited
    	String menuItem = vreq.getParameter("menuItem");
    	OntModel displayModel = getDisplayModel(vreq);
    	if(displayModel == null) {
    		//Throw some kind of exception
    		log.error("Display model not being retrieved correctly");
    	}
    	//if Add, then create new menu item and new page elements, and use the values above
    	String errorMessage = "";
    	if(isAdd(command)){
    		processAdd(menuItem, displayModel, command, vreq);
    	}
    	//Otherwise use existing resource
    	else if(isEdit(command)) {
    		processEdit(menuItem, displayModel, command, vreq);
    		
    	} else if(isDelete(command)) {
    		processDelete(menuItem, displayModel, command, vreq);
    	} else if(isReorder(command)) {
    		errorMessage = processReorder(displayModel, vreq);
    	}
    	
    	//Edits to model occur here
    	displayModel.enterCriticalSection(Lock.WRITE);
    	try {
    		log.debug("Statement to be removed are ");
    		StringWriter r = new StringWriter();
			removeStatements.write(r, "N3");
    		log.debug(r.toString());
    		r.close();
    		log.debug("Statements to be added are ");
    		StringWriter a = new StringWriter();
    		addStatements.write(a, "N3");
    		log.debug(a.toString());
    		a.close();
     		displayModel.remove(removeStatements);
     		displayModel.add(addStatements);
    	
    	} catch(Exception ex) {
    		log.error("An error occurred in processing command", ex);
    		errorMessage += "An error occurred and the operation could not be completed successfully.";
    	}finally {
    		displayModel.leaveCriticalSection();
    	}
    	
    	//if reorder, need to send back an AJAX response
    	if(isReorder(command)){
    		sendReorderResponse(errorMessage, resp);
    	}
    	
    }
    
    private String processReorder(OntModel displayModel, VitroRequest vreq) {
    	//Assuming individual uris passed in the order of their new menu positions
    	String[]individuals = vreq.getParameterValues("individuals");
		String errorMessage = null;
		if(individuals.length > 0 ) {
			removeStatements = removePositionStatements(displayModel, individuals);
			addStatements = addPositionStatements(displayModel, individuals); 
		} else {
			errorMessage = "No individuals passed";
		}
		return errorMessage;
	}
    
    private void sendReorderResponse(String errorMessage, HttpServletResponse resp) {
    	try{
			JSONObject rObj = new JSONObject();
			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("application/json;charset=UTF-8");
      
			if( errorMessage != null && !errorMessage.isEmpty()){
				rObj.put("errorMessage", errorMessage);
				resp.setStatus(500 /*HttpURLConnection.HTTP_SERVER_ERROR*/);
			}else{
				rObj.put("errorMessage", "");
			}            
           Writer writer = resp.getWriter();
           writer.write(rObj.toString());
		} catch(Exception ex) {
			log.error("Error creating JSON object for response", ex);
		}
    }


	private Model removePositionStatements(OntModel displayModel,
			String[] individuals) {
		Model removePositionStatements = ModelFactory.createDefaultModel();
		
		for(String individual: individuals) {
			Resource individualResource = ResourceFactory.createResource(individual);
			
			removePositionStatements.add(displayModel.listStatements(
					individualResource, 
					DisplayVocabulary.MENU_POSITION, 
					(RDFNode) null));
			
		}
		
		return removePositionStatements;
	}

	private Model addPositionStatements(OntModel displayModel,
			String[] individuals) {
		Model addPositionStatements = ModelFactory.createDefaultModel();
		int index = 0;
		int len = individuals.length;
		for(index = 0; index < len; index++) {
			Resource individualResource = ResourceFactory.createResource(individuals[index]);
			int position = index + 1;
			addPositionStatements.add(addPositionStatements.createStatement(
					individualResource, 
					DisplayVocabulary.MENU_POSITION, 
					addPositionStatements.createTypedLiteral(position)));
			
		}
		return addPositionStatements;
	}

	

	private void processDelete(String menuItem, OntModel displayModel,
			String command, VitroRequest vreq) {
    	Resource menuItemResource = getExistingMenuItem(menuItem, displayModel);
		Resource pageResource = getExistingPage(menuItemResource, displayModel);
		//What statements should be added and removed
   		removeStatements.add(getStatementsToRemove(command, displayModel, menuItemResource, pageResource));
   		//No statements to add
	}


	private void processEdit(String menuItem, OntModel displayModel,
			String command, VitroRequest vreq) {
    	Resource menuItemResource = getExistingMenuItem(menuItem, displayModel);
		Resource pageResource = getExistingPage(menuItemResource, displayModel);
		//if home page process separately
		if(isHomePage(displayModel, pageResource)) {
			processHomePage(vreq, displayModel, menuItemResource, pageResource);
		} else {
			//What statements should be added and removed
	   		removeStatements.add(getStatementsToRemove(command, displayModel, menuItemResource, pageResource));
			addStatements.add(getStatementsToAdd(vreq, command, displayModel, menuItemResource, pageResource));
		}
	}

	
	//Home page expects only menu name to change
	//No other edits are currently supported
	private void processHomePage(VitroRequest vreq, OntModel displayModel, Resource menuItemResource, Resource pageResource) {
		//remove statements for existing linkText and title
		removeMenuName(displayModel, removeStatements, vreq, menuItemResource, pageResource);
		//add new statements for link text and title, setting equal to new menu name
		updateMenuName(addStatements, vreq, menuItemResource, pageResource);
	}


	private void processAdd(String menuItem, OntModel displayModel, String command, VitroRequest vreq) {
		String menuName = vreq.getParameter("menuName");
    	Resource menuItemResource = createNewMenuItem(menuName, displayModel);
		Resource pageResource = createNewPage(menuItemResource, displayModel);
		//no statements to remove, just to add
		addStatements.add(getStatementsToAdd(vreq, command, displayModel, menuItemResource, pageResource));
		addStatements.add(associateMenuItemToPage(menuItemResource, pageResource));

    }
    
    //Get last menu item positin
    private int getLastPosition(OntModel displayModel) {
    	StmtIterator positions = displayModel.listStatements(null, DisplayVocabulary.MENU_POSITION, (RDFNode) null);
    	int maxPosition = 1;
    	while(positions.hasNext()) {
    		Statement s = positions.nextStatement();
    		log.debug("Position statement is " + s);
    		int pos = s.getInt();
    		log.debug("position is " + pos);
    		if(pos > maxPosition) {
    			maxPosition = pos;
    		}
    	}
    	log.debug("Maximum menu item position is " + maxPosition);
    	return  maxPosition;
    }
    
    //Statements to add - should not be called for delete
    private Model getStatementsToAdd(VitroRequest vreq, String command, OntModel displayModel,
		Resource menuItemResource, Resource pageResource) {
    	Model addModel = ModelFactory.createDefaultModel();
		if(isAdd(command)) {
			generateStatementsForAdd(addModel, displayModel, menuItemResource, pageResource);
		} 
		generateStatementsForUpdates(vreq, addModel, displayModel, menuItemResource, pageResource);
		
		return addModel;
	}
    
    //These statements need to be added based on parameter values
    //This is a simple overwrite, no checking for existing values whatsoever
    private void generateStatementsForUpdates(VitroRequest vreq, Model addModel,
			OntModel displayModel, Resource menuItemResource,
			Resource pageResource) {
    	
    	updateMenuName(addModel, vreq, menuItemResource, pageResource);
    	updateUrl(addModel, vreq, pageResource);
    	updateTemplate(addModel, vreq, pageResource);
    	updateDataGetter(addModel, displayModel, vreq, pageResource);
	}

    
	private void updateDataGetter(Model addModel, OntModel displayModel, VitroRequest vreq,
			Resource pageResource) {
		//Selected class group
    	String classGroup = vreq.getParameter("selectClassGroup");
    	//Selected class
    	
    	
    	//For this, need to check whether All or not b/c this will identify the data getter type
    	//There should be a "specify data getter" method that specifices the data getter
    	Resource dataGetterResource = getDataGetter(vreq, addModel, displayModel, pageResource);
    	//TODO: if null, throw an exception or error
    	if(dataGetterResource != null) {
    		Resource classGroupResource = ResourceFactory.createResource(classGroup);
    		//Whatever the data getter might be assign class group
    		addModel.add(addModel.createStatement(
    				dataGetterResource, 
    				ResourceFactory.createProperty(DisplayVocabulary.FOR_CLASSGROUP), 
    				classGroupResource));
    		//Get the model corresponding to the data getter to be employed and return
    		Model dataGetterModel = SelectDataGetterUtils.createDataGetterModel(vreq, dataGetterResource);
    		addModel.add(dataGetterModel);
    	}
		
	}

	//For now returning the first "data getter" we have - this will be a more complex operation
	//if multiple data getters possible as then will have to determine which data getter required
	//Based on whether this is an add or edit operation, return the appropriate data getter resource
	//If add, then a new data getter has been created but not yet added to the display model
	//If edit, then data getter already exists
	private Resource getDataGetter(VitroRequest vreq, Model addModel, OntModel displayModel, Resource pageResource) {
		String command = vreq.getParameter(CMD_PARAM);
		return getDataGetter(command, addModel, displayModel, pageResource);
	}
	
	private Resource getDataGetter(String command, Model addModel, OntModel displayModel, Resource pageResource) {
		StmtIterator dataGetterIt = null;
		//if addition, we havent' committed the data getter changes yet so the resource will be different
		if(isAdd(command)) {
			dataGetterIt = addModel.listStatements(
					pageResource, 
					ResourceFactory.createProperty(DisplayVocabulary.HAS_DATA_GETTER), 
					(RDFNode) null);
		} else {
			dataGetterIt = displayModel.listStatements(
					pageResource, 
					ResourceFactory.createProperty(DisplayVocabulary.HAS_DATA_GETTER), 
					(RDFNode) null);
		}
		
		if(dataGetterIt != null && dataGetterIt.hasNext()) {
			return dataGetterIt.nextStatement().getResource();
		}
		return null;
	}
	
	private Resource getDataGetterFromDisplayModel(Resource pageResource, OntModel displayModel) {
		StmtIterator dataGetterIt = displayModel.listStatements(
				pageResource, 
				ResourceFactory.createProperty(DisplayVocabulary.HAS_DATA_GETTER), 
				(RDFNode) null);
		if(dataGetterIt != null && dataGetterIt.hasNext()) {
			return dataGetterIt.nextStatement().getResource();
		}
		return null;
	}

	private void updateTemplate(Model addModel, VitroRequest vreq,
			Resource pageResource) {
		String selectedTemplate = vreq.getParameter("selectedTemplate");
    	if(selectedTemplate.equals("custom")) {
    		String customTemplate = vreq.getParameter("customTemplate");
    		addModel.add(addModel.createStatement(pageResource, DisplayVocabulary.REQUIRES_BODY_TEMPLATE, customTemplate));

    	}
		
	}

	private void updateUrl(Model addModel, VitroRequest vreq,
			Resource pageResource) {
		// TODO Auto-generated method stub
		String prettyUrl = vreq.getParameter("prettyUrl");
		addModel.add(addModel.createStatement(pageResource, DisplayVocabulary.URL_MAPPING, prettyUrl));
	}

	private void updateMenuName(Model addModel, VitroRequest vreq,
			Resource menuItemResource, Resource pageResource) {
		String menuName = vreq.getParameter("menuName");
		addModel.add(addModel.createStatement(menuItemResource, DisplayVocabulary.LINK_TEXT, menuName));
		addModel.add(addModel.createStatement(
				pageResource, 
				ResourceFactory.createProperty(DisplayVocabulary.TITLE), 
				menuName));
		
	}

	private void removeMenuName(OntModel displayModel, Model removeModel, VitroRequest vreq,
			Resource menuItemResource, Resource pageResource) {
		String menuName = vreq.getParameter("menuName");
		removeModel.add(displayModel.listStatements(
				menuItemResource, 
				DisplayVocabulary.LINK_TEXT, 
				(RDFNode) null));
		removeModel.add(displayModel.listStatements(
				pageResource, 
				ResourceFactory.createProperty(DisplayVocabulary.TITLE), 
				(RDFNode) null));
		
	}
	
	private void generateStatementsForAdd(Model addModel, OntModel displayModel, Resource menuItemResource, Resource pageResource) {
    	//Need to generate the menu item and page in their entirety
		//Menu item
		addModel.add(addModel.createStatement(
				ResourceFactory.createResource(DisplayVocabulary.DEFAULT_MENU)
				,ResourceFactory.createProperty(DisplayVocabulary.HAS_ELEMENT),
				menuItemResource));
		addModel.add(addModel.createStatement(menuItemResource, RDF.type, DisplayVocabulary.NAVIGATION_ELEMENT));
		int newMenuPosition = getLastPosition(displayModel) + 1;
		log.debug("new menu position is " + newMenuPosition);
		addModel.add(addModel.createStatement(menuItemResource, 
				DisplayVocabulary.MENU_POSITION, 
				addModel.createTypedLiteral(newMenuPosition)));
		//page resource, type, title and url mapping, and what data getter associated
		addModel.add(addModel.createStatement(pageResource, 
				RDF.type, 
				ResourceFactory.createResource(DisplayVocabulary.PAGE_TYPE)));
		//Need to create a data getter
		Model dataGetterStatements = generateDataGetter(pageResource, displayModel);
		addModel.add(dataGetterStatements);
    }
    
    

	//Get statements for data getter
	private Model generateDataGetter(Resource pageResource, OntModel displayModel) {
    	Model dataGetterModel = ModelFactory.createDefaultModel();
    	String dataGetterUri = generateDataGetterUri(pageResource, displayModel);
    	Resource dataGetter = ResourceFactory.createResource(dataGetterUri);
    	dataGetterModel.add(dataGetterModel.createStatement(
    			pageResource, 
    			ResourceFactory.createProperty(DisplayVocabulary.HAS_DATA_GETTER), 
    			dataGetter));
    	
		return dataGetterModel;
	}

	private String generateDataGetterUri(Resource pageResource, OntModel displayModel) {
		String dataGetterUriBase = pageResource.getURI() + "-dataGetter";
		String dataGetterUri = dataGetterUriBase;
		int counter = 0;
		while(displayModel.getIndividual(dataGetterUriBase) != null) {
			dataGetterUri = dataGetterUriBase + counter;
			counter++;
		}
		return dataGetterUri;
	}

	//What statements need to be removed
	private Model getStatementsToRemove(String command, OntModel displayModel,
			Resource menuItemResource, Resource pageResource) {
		Model removeModel = ModelFactory.createDefaultModel();
		//if delete then remove all statements pertaining to any of the objects
		if(isEdit(command)) {
			//remove top level properties
			removeModel.add(displayModel.listStatements(
					pageResource, 
					ResourceFactory.createProperty(DisplayVocabulary.TITLE), 
					(RDFNode) null));
			removeModel.add(displayModel.listStatements(
					pageResource, 
					DisplayVocabulary.URL_MAPPING, 
					(RDFNode) null));
			removeModel.add(displayModel.listStatements(
					pageResource, 
					DisplayVocabulary.REQUIRES_BODY_TEMPLATE, 
					(RDFNode) null));
			//Also need to get rid of link text in addition to title
			removeModel.add(displayModel.listStatements(
					menuItemResource, 
					DisplayVocabulary.LINK_TEXT,
					(RDFNode) null));
		    //remove data getter properties - the link between page and data getter remains
			Resource dataGetter = getDataGetterFromDisplayModel(pageResource, displayModel);
			removeModel.add(displayModel.listStatements(dataGetter, null, (RDFNode) null));
		}
		
		if(isDelete(command)) {
			//Remove all statements from data getter, page and menu item
			Resource dataGetter = getDataGetterFromDisplayModel(pageResource, displayModel);
			removeModel.add(displayModel.listStatements(dataGetter, null, (RDFNode) null));
			removeModel.add(displayModel.listStatements(pageResource, null, (RDFNode) null));
			removeModel.add(displayModel.listStatements(menuItemResource, null, (RDFNode) null));
			//Also remove any statements where menu item resource is an object
			removeModel.add(displayModel.listStatements(null, null, menuItemResource));
		}
		return removeModel;
	}

	private Resource getExistingPage(Resource menuItem, OntModel displayModel) {
		StmtIterator pageIt = displayModel.listStatements(menuItem, DisplayVocabulary.TO_PAGE, (RDFNode) null);
		if(pageIt.hasNext()) {
			return pageIt.nextStatement().getResource();
		}
		return null;
	}

	private Resource getExistingMenuItem(String menuItem, OntModel displayModel) {
		return displayModel.getResource(menuItem);
	}

	//What should page uri be? for now menuItem + page
	private Resource createNewPage(Resource menuItem, OntModel displayModel) {
		return  ResourceFactory.createResource(menuItem.getURI() + "Page");
	}

	private Resource createNewMenuItem(String menuName, OntModel displayModel) {
		return ResourceFactory.createResource(generateNewMenuItemUri(menuName, displayModel));
	}
	
	//Create connection
	private Model associateMenuItemToPage(Resource menuItemResource, Resource pageResource) {
		Model m = ModelFactory.createDefaultModel();
		m.add(m.createStatement(menuItemResource, DisplayVocabulary.TO_PAGE, pageResource));
		return m;
	}
	
	//Add to model

	
	//TODO: Check if this is an appropriate mechanism for generating menu uri
	private String generateNewMenuItemUri (String menuName, OntModel displayModel) {
		String menuUriBase = DisplayVocabulary.DISPLAY_NS + menuName.replaceAll(" ", "") + "MenuItem";
		String menuUri = menuUriBase;
		int counter = 0;
		while(displayModel.getIndividual(menuUri) != null) {
			menuUri = menuUriBase + counter;
			counter++;
		}
		return menuUri;
	}

	//This should be in write mode
    //TODO: find better way of doing this
    private OntModel getDisplayModel(VitroRequest vreq) {
    	if(vreq.getAttribute(vreq.SPECIAL_WRITE_MODEL) != null) {
    		return vreq.getWriteModel();
    	} else {
    		return (OntModel) getServletContext().getAttribute("http://vitro.mannlib.cornell.edu/default/vitro-kb-displayMetadata");
    	}
    }
    
    //Is home page
    private boolean isHomePage(OntModel writeModel, Resource page) {
    	StmtIterator homePageIt = writeModel.listStatements(page, RDF.type, ResourceFactory.createResource(DisplayVocabulary.HOME_PAGE_TYPE));
    	return (homePageIt.hasNext());
    }

    Log log = LogFactory.getLog(MenuManagementEdit.class);
}
