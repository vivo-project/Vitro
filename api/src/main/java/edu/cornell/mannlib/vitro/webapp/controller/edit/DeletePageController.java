/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;


import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;

/**
 *Process deletions for page, deleting page, data getters and any associated menu items.
 */
public class DeletePageController extends VitroHttpServlet {
   
   private final static String REDIRECT_URL = "/pageList";
   private static Model removeStatements = null; 
    
    @Override
    protected void doPost(HttpServletRequest rawRequest, HttpServletResponse resp)
            throws ServletException, IOException {
    	if (!isAuthorizedToDisplayPage(rawRequest, resp, SimplePermission.MANAGE_MENUS.ACTION)) {
    		return;
    	}
    	removeStatements = ModelFactory.createDefaultModel();
    	VitroRequest vreq = new VitroRequest(rawRequest);
    	String pageUri = vreq.getParameter("pageURI");
     	if(pageUri != null) {
    		doDeletePage(pageUri, vreq, resp);
    	}
		resp.sendRedirect(rawRequest.getContextPath() + REDIRECT_URL);
    }

    protected void doGet(HttpServletRequest rawRequest, HttpServletResponse resp)  throws ServletException, IOException {
    	doPost(rawRequest, resp);
    }
    
    
    //Parameter retrieval is identical, but in one case an entirey new menu item needs to be created
    //along with a new page
    public void doDeletePage(String pageUri, VitroRequest vreq, HttpServletResponse resp) {
    	
    	OntModel displayModel = getDisplayModel(vreq);
    	if(displayModel == null) {
    		//Throw some kind of exception
    		log.error("Display model not being retrieved correctly");
    	}
    	
    	String errorMessage = "";
    		processDelete(pageUri, displayModel, vreq);
    	
    	//Edits to model occur here
    	displayModel.enterCriticalSection(Lock.WRITE);
    	try {
    		log.debug("Statement to be removed are ");
    		StringWriter r = new StringWriter();
			removeStatements.write(r, "N3");
    		log.debug(r.toString());
    		r.close();
    		displayModel.remove(removeStatements);
    	
    	} catch(Exception ex) {
    		log.error("An error occurred in processing command", ex);
    		errorMessage += "An error occurred and the operation could not be completed successfully.";
    	}finally {
    		displayModel.leaveCriticalSection();
    	}
    }
    
   
	
		

	private void processDelete(String pageUri, OntModel displayModel, VitroRequest vreq) {
		//get the page resource
		Resource pageResource = getExistingPage(pageUri, displayModel);
		//if the page is related to a menu item, get the menu item information 
    	Resource menuItemResource = getExistingMenuItem(pageResource, displayModel);
    	
		//What statements should be added and removed
   		removeStatements.add(getStatementsToRemove(displayModel, menuItemResource, pageResource));
   		//No statements to add
	}

	
	

	
	//What statements need to be removed
	private Model getStatementsToRemove(OntModel displayModel,
			Resource menuItemResource, Resource pageResource) {
		Model removeModel = ModelFactory.createDefaultModel();		
		removeModel.add(displayModel.listStatements(pageResource, null, (RDFNode) null));
		//if menu item exists for this page, get all statements related to menu item and page
		if(menuItemResource != null) {
			removeModel.add(displayModel.listStatements(menuItemResource, null, (RDFNode) null));
			//Also remove any statements where menu item resource is an object
			removeModel.add(displayModel.listStatements(null, null, menuItemResource));
		}
		//Get all data getter statements
		Model associatedDataGettersModel = getDataGettersStatements(pageResource, displayModel);
		removeModel.add(associatedDataGettersModel);
		return removeModel;
	}

	//Get all data getters associated with page
	private Model getDataGettersStatements(Resource pageResource, OntModel displayModel) {
		Model dataGettersModel = ModelFactory.createDefaultModel();		
		//To iterate through to get all data getters and then all their statements
		//PAge to data getter statements have already been added when all page statements were added
		StmtIterator dataGetterIt = displayModel.listStatements(
				pageResource, 
				ResourceFactory.createProperty(DisplayVocabulary.HAS_DATA_GETTER), 
				(RDFNode) null);
		while(dataGetterIt.hasNext()) {
			Statement dataGetterStmt = dataGetterIt.nextStatement();
			Resource dataGetterResource = dataGetterStmt.getResource();
			dataGettersModel.add(displayModel.listStatements(dataGetterResource, null, (RDFNode) null));
		}
		return dataGettersModel;
	}




	private Resource getExistingPage(String pageUri, OntModel displayModel) {
		return ResourceFactory.createResource(pageUri);
	}

	private Resource getExistingMenuItem(Resource pageResource,  OntModel displayModel) {
		Resource menuItemResource = null;
		StmtIterator menuItemIt = displayModel.listStatements(null, 
				DisplayVocabulary.TO_PAGE, 
				pageResource);
		while(menuItemIt.hasNext()) {
			Statement menuStmt = menuItemIt.nextStatement();
			menuItemResource = menuStmt.getSubject();
		}
		return menuItemResource;
	}

	//This code is called without model switching, so in this case
	//we just want the regular model
    private OntModel getDisplayModel(VitroRequest vreq) {
    	return vreq.getDisplayModel();    
    }
    
    Log log = LogFactory.getLog(MenuManagementEdit.class);
}
