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

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;

/**
 *Process edits from display model editing, so form should submit to this page which should
 *then process the parameters and then make the necessary changes to the model.
 */
public class MenuManagementEdit extends VitroHttpServlet {
   private static final String CMD_PARAM = "cmd";   
   private final static String REORDER_PARAM_VALUE = "Reorder";
   private final static String REDIRECT_URL = "/individual?uri=http%3A%2F%2Fvitro.mannlib.cornell.edu%2Fontologies%2Fdisplay%2F1.1%23DefaultMenu&switchToDisplayModel=true";
   private static Model removeStatements = null; 
   private static Model addStatements = null; 
    
    @Override
    protected void doPost(HttpServletRequest rawRequest, HttpServletResponse resp)
            throws ServletException, IOException {
    	
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
    		
    	}
    }


	public String getCommand(VitroRequest vreq) {
    	String command = vreq.getParameter(CMD_PARAM);
    	return command;
    }
    
    public boolean isReorder(String command) {
    	return command.equals(REORDER_PARAM_VALUE);
    }
    
    public boolean isHomePage(String uri) {
    	return uri.equals(DisplayVocabulary.DISPLAY_NS + "Home");
    }
    
   
    //Process command: in this case just reorder, but may be extended to include delete later
    public void processCommand(String command, VitroRequest vreq, HttpServletResponse resp) {

    	OntModel displayModel = getDisplayModel(vreq);
    	if(displayModel == null) {
    		//Throw some kind of exception
    		log.error("Display model not being retrieved correctly");
    	}
    	//if Add, then create new menu item and new page elements, and use the values above
    	String errorMessage = "";
    	if(isReorder(command)) {
    		errorMessage = processReorder(displayModel, vreq);
    	}
    	
    	//Edits to model occur here
    	displayModel.enterCriticalSection(Lock.WRITE);
    	try {
    		//Output statements to be removed to log
    		log.debug("Statement to be removed are ");
    		StringWriter r = new StringWriter();
			removeStatements.write(r, "N3");
    		log.debug(r.toString());
    		r.close();
    		//Output statements to be added to log
    		log.debug("Statements to be added are ");
    		StringWriter a = new StringWriter();
    		addStatements.write(a, "N3");
    		log.debug(a.toString());
    		a.close();
    		//Remove and add statements
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

	
   
	
	//This should be in write mode
    //TODO: find better way of doing this
    private OntModel getDisplayModel(VitroRequest vreq) {
    	if(vreq.getAttribute(vreq.SPECIAL_WRITE_MODEL) != null) {
    		return vreq.getJenaOntModel();
    	} else {
    		return vreq.getDisplayModel();
    	}
    }
    Log log = LogFactory.getLog(MenuManagementEdit.class);
}
