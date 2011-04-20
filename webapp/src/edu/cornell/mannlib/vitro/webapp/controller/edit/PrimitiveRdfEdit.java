/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.RequiresAuthorizationFor;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseBasicAjaxControllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.ajax.VitroAjaxController;
import edu.cornell.mannlib.vitro.webapp.dao.jena.DependentResourceDeleteJena;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditN3Utils;

@RequiresAuthorizationFor(UseBasicAjaxControllers.class)
public class PrimitiveRdfEdit extends VitroAjaxController {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doRequest(VitroRequest vreq,
            HttpServletResponse response) throws ServletException, IOException {
        
        //Test error case
        /*
        if (1==1) {
            doError(response, "Test error", 500);
            return;
        } */
        
        /* Predefined values for RdfFormat are "RDF/XML", 
         * "N-TRIPLE", "TURTLE" (or "TTL") and "N3". null represents 
         * the default language, "RDF/XML". "RDF/XML-ABBREV" is a synonym for "RDF/XML" */
        String format = vreq.getParameter("RdfFormat");      
        if( format == null )            
            format = "N3";      
        if ( ! ("N-TRIPLE".equals(format) || "TURTLE".equals(format) || "TTL".equals(format)
                || "N3".equals(format)|| "RDF/XML-ABBREV".equals(format) || "RDF/XML".equals(format) )){
            doError(response,"RdfFormat was not recognized.",500);
            return;
        }
        
        //parse RDF 
        Set<Model> additions= null;
        try {
            additions = parseRdfParam(vreq.getParameterValues("additions"),format);
        } catch (Exception e) {
            doError(response,"Error reading RDF, set log level to debug for this class to get error messages in the server logs.",HttpStatus.SC_BAD_REQUEST);
            return;
        }
                        
        Set<Model> retractions = null;
        try {
            retractions = parseRdfParam(vreq.getParameterValues("retractions"),format);
        } catch (Exception e) {
            doError(response,"Error reading RDF, set log level to debug for this class to get error messages in the server logs.",HttpStatus.SC_BAD_REQUEST);
            return;
        }

        String editorUri = EditN3Utils.getEditorUri(vreq);           
        try {
            processChanges( additions, retractions, getWriteModel(vreq),getQueryModel(vreq), editorUri);
        } catch (Exception e) {
            doError(response,e.getMessage(),HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }           
        
    }
    
    protected void processChanges(Set<Model> additions, Set<Model> retractions, OntModel writeModel, OntModel queryModel, String editorURI ) throws Exception{
        Model a = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
        for(Model m : additions)
            a.add(m);
        
        Model r = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
        for(Model m : retractions)
            r.add(m);
        
        processChanges(a,r,writeModel,queryModel,editorURI);
               
    }
    
    protected void processChanges(Model additions, Model retractions, OntModel writeModel, OntModel queryModel, String editorURI ) throws Exception{        
        /*
         * Do a diff on the additions and retractions and then only add the delta to the jenaOntModel.
         */            
        Model assertionsAdded = additions.difference( retractions );    
        Model assertionsRetracted = retractions.difference( additions );
            
        Model depResRetractions = 
            DependentResourceDeleteJena
              .getDependentResourceDeleteForChange(assertionsAdded, assertionsRetracted, queryModel);
        assertionsRetracted.add( depResRetractions );                     
         
        Lock lock = null;
        try{
            lock =  writeModel.getLock();
            lock.enterCriticalSection(Lock.WRITE);
            writeModel.getBaseModel().notifyEvent(new EditEvent(editorURI,true));   
            writeModel.add( assertionsAdded );
            writeModel.remove( assertionsRetracted );
        }catch(Throwable t){
            throw new Exception("Error while modifying model \n" + t.getMessage());     
        }finally{
            writeModel.getBaseModel().notifyEvent(new EditEvent(editorURI,false));
            lock.leaveCriticalSection();
        }        
    }
    


    /**
     * Convert the values from a parameters into RDF models.
     * @param parameters - the result of request.getParameters(String)
     * @param format - a valid format string for Jena's Model.read()
     * @return 
     * @throws Exception
     */
    protected Set<Model> parseRdfParam(String[] parameters, String format) throws Exception{
        Set<Model> models = new HashSet<Model>();               
        for( String param : parameters){
            try{
                StringReader reader = new StringReader(param);
                Model model = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();          
                model.read(reader, null, format);
                models.add(model);
            }catch(Error ex){
                log.error("Error reading RDF as " + format + " in " + param);
                throw new Exception("Error reading RDF, set log level to debug for this class to get error messages in the sever logs.");
            }
        }
        return models;
    }
    
    protected OntModel getWriteModel(HttpServletRequest request){
        HttpSession session = request.getSession(false);
        if( session == null || session.getAttribute("jenaOntModel") == null )
            return (OntModel)getServletContext().getAttribute("jenaOntModel");
        else
            return (OntModel)session.getAttribute("jenaOntModel");
    }
    
    protected OntModel getQueryModel(HttpServletRequest request){
        return getWriteModel(request);
    }
    
    Log log = LogFactory.getLog(PrimitiveRdfEdit.class.getName());


    static public boolean checkLoginStatus(HttpServletRequest request){
    	return LoginStatusBean.getBean(request).isLoggedIn();
    }


}
