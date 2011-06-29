package edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.EditConfigurationGenerator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;
import edu.cornell.mannlib.vitro.webapp.web.MiscWebUtils;

public class EditDataPropStmtRequestDispatchController extends FreemarkerHttpServlet {
	
	public static Log log = LogFactory.getLog(EditDataPropStmtRequestDispatchController.class);
	
	final String DEFAULT_DATA_FORM = "edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultDataPropertyFormGenerator";
	final String DEFAULT_ERROR_FORM = "error.jsp";
	
	@Override
	protected ResponseValues processRequest(VitroRequest vreq) {   
		
		HttpSession session = vreq.getSession();
	    if( EditConfiguration.getEditKey( vreq ) == null ){
	        vreq.setAttribute("editKey",EditConfiguration.newEditKey(session));
	    }else{
	        vreq.setAttribute("editKey", EditConfiguration.getEditKey( vreq ));
	    }
	
	    //set title to Edit to maintain functionality from 1.1.1 and avoid updates to Selenium tests
	    vreq.setAttribute("title","Edit");
	    
	    String subjectUri   = vreq.getParameter("subjectUri");
	    String predicateUri = vreq.getParameter("predicateUri");
	    String formParam    = vreq.getParameter("editForm");
	    String command      = vreq.getParameter("cmd");
	    
	    
	    if( subjectUri == null || subjectUri.trim().length() == 0 ) {
	        log.error("required subjectUri parameter missing");
	        return doHelp(vreq, "subjectUri was empty, it is required by EditDataPropStmtRequestDispatchController");
	    }
	    if( predicateUri == null || predicateUri.trim().length() == 0) {
	        log.error("required subjectUri parameter missing");
	        return doHelp(vreq, "predicateUri was empty, it is required by EditDataPropStmtRequestDispatchController");
	    }
	    
	    // Since we have the URIs let's put the individual, data property, and optional data property statement in the request
	    vreq.setAttribute("subjectUri", subjectUri);
	    vreq.setAttribute("subjectUriJson", MiscWebUtils.escape(subjectUri));
	    vreq.setAttribute("predicateUri", predicateUri);
	    vreq.setAttribute("predicateUriJson", MiscWebUtils.escape(predicateUri));

	    WebappDaoFactory wdf = vreq.getWebappDaoFactory();

	    Individual subject = wdf.getIndividualDao().getIndividualByURI(subjectUri);
	    if( subject == null ) {
	        log.error("Could not find subject Individual '"+subjectUri+"' in model");
	        return doHelp(vreq, "EditDataPropStmtRequestDispatchController: Could not find subject Individual in model: '" + subjectUri + "'");
	    }
	    vreq.setAttribute("subject", subject);

	    DataProperty dataproperty = wdf.getDataPropertyDao().getDataPropertyByURI( predicateUri );
	    if( dataproperty == null) {
	        // No dataproperty will be returned for rdfs:label, but we shouldn't throw an error.
	        // This is controlled by the Jena layer, so we can't change the behavior.
	        if (! predicateUri.equals(VitroVocabulary.LABEL)) {
	            log.error("Could not find data property '"+predicateUri+"' in model");
	            return doHelp(vreq, "EditDataPropStmtRequestDispatchController: Could not find DataProperty in model: " + predicateUri);
	        }
	    }
	    else {
	        vreq.setAttribute("predicate", dataproperty);
	    }
	    
        // Keep track of what form we are using so it can be returned to after a failed validation 
        // I'd like to get this from the request but sometimes that doesn't work well, internal forwards etc.
         //TODO: this needs to be the same as the mapping in web.xml 
	    vreq.setAttribute("formUrl", "/edit/editRequest?" + vreq.getQueryString());
	    
	    String datapropKeyStr = vreq.getParameter("datapropKey");
	    int dataHash = 0;
	    if( datapropKeyStr != null ){
	        try {
	            dataHash = Integer.parseInt(datapropKeyStr);
	            vreq.setAttribute("datahash", dataHash);
	            log.debug("Found a datapropKey in parameters and parsed it to int: " + dataHash);
	         } catch (NumberFormatException ex) {
	            return doHelp(vreq, "Cannot decode incoming datapropKey value "+datapropKeyStr+" as an integer hash in EditDataPropStmtRequestDispatchController");
	        }
	    }

	    DataPropertyStatement dps = null;
	    if( dataHash != 0) {
	        Model model = (Model)session.getServletContext().getAttribute("jenaOntModel");
	        dps = RdfLiteralHash.getPropertyStmtByHash(subject, predicateUri, dataHash, model);
	                              
	        if (dps==null) {
	            log.error("No match to existing data property \""+predicateUri+"\" statement for subject \""+subjectUri+"\" via key "+datapropKeyStr);
	            //TODO: Needs to forward to dataPropMissingStatement.jsp
	            return null;
	        }                     
	        vreq.setAttribute("dataprop", dps );
	    }

	    if( log.isDebugEnabled() ){
	        if (dataproperty != null) {
	            log.debug("predicate for DataProperty from request is " + dataproperty.getURI() + " with rangeDatatypeUri of '" + dataproperty.getRangeDatatypeURI() + "'");
	        }
	        if( dps == null )
	            log.debug("no existing DataPropertyStatement statement was found, making a new statemet");
	        else{
	            log.debug("Found an existing DataPropertyStatement");
	            String msg = "existing datapropstmt: ";
	            msg += " subject uri: <"+dps.getIndividualURI() + ">\n";
	            msg += " prop uri: <"+dps.getDatapropURI() + ">\n";
	            msg += " prop data: \"" + dps.getData() + "\"\n";
	            msg += " datatype: <" + dps.getDatatypeURI() + ">\n";
	            msg += " hash of this stmt: " + RdfLiteralHash.makeRdfLiteralHash(dps);
	            log.debug(msg);
	        }
	    }
	    
	    
//	    vreq.setAttribute("preForm", "/edit/formPrefix.jsp");
//	    vreq.setAttribute("postForm", "/edit/formSuffix.jsp");
	    
	    if( "delete".equals(command) ){ 
	    	//TODO: Needs to forward to dataPropStmtDelete.jsp
	    	return null;
    }
	    
	    String form = null;
	    if (formParam != null) {
	        form = formParam;
	    }   
	    else if (predicateUri.equals(VitroVocabulary.LABEL)) {  // dataproperty is null here
	        form = "rdfsLabelForm.jsp"; 
	    }
	    else {
	        form = dataproperty.getCustomEntryForm();
	        if (form != null && form.length()>0) {
	            log.warn("have a custom form for this data property: "+form);
	            vreq.setAttribute("hasCustomForm","true");
	        } else {
	            form = DEFAULT_DATA_FORM;
	        }
	    }
	    vreq.setAttribute("form", form);

	    if( session.getAttribute("requestedFromEntity") == null ) {
	        session.setAttribute("requestedFromEntity", subjectUri );
	    }    
	    
        /****  make the edit configuration ***/
        EditConfiguration editConfig = makeEditConfiguration( form, vreq, session);
        
        //what template?
        String template = editConfig.getTemplate();
        
        //what goes in the map for templates?
        Map<String,Object> templateData = new HashMap<String,Object>();
        templateData.put("editConfiguration", editConfig);
        
        return new TemplateResponseValues(editConfig.getTemplate(), templateData);
	    
	}
	
	
    private EditConfiguration makeEditConfiguration(
            String editConfGeneratorName, VitroRequest vreq, HttpSession session) {
    	
    	EditConfigurationGenerator editConfigurationGenerator = null;
    	
        Object object = null;
        try {
            Class classDefinition = Class.forName(editConfGeneratorName);
            object = classDefinition.newInstance();
            editConfigurationGenerator = (EditConfigurationGenerator) object;
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }    	
        
        if(editConfigurationGenerator == null){
        	log.error("could not find editConfigurationGenerator " + editConfGeneratorName);
        	return null;
        } else {
            return editConfigurationGenerator.getEditConfiguration(vreq, session);
        }
        
    }
	
    private ResponseValues doHelp(VitroRequest vreq, String message){
        //output some sort of help message for the developers.
        
        return null;
    }
}
