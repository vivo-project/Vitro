/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class NamespacePrefixOperationController extends BaseEditController {

    private static final Log log = LogFactory.getLog(IndividualTypeOperationController.class.getName());

    public void doPost(HttpServletRequest req, HttpServletResponse response) {
        if (!isAuthorizedToDisplayPage(req, response, new Actions(new UseMiscellaneousAdminPages()))) {
        	return;
        }

    	VitroRequest request = new VitroRequest(req);
    	String defaultLandingPage = getDefaultLandingPage(request);

        HashMap epoHash = null;
        EditProcessObject epo = null;
        try {
            epoHash = (HashMap) request.getSession().getAttribute("epoHash");
            epo = (EditProcessObject) epoHash.get(request.getParameter("_epoKey"));
        } catch (NullPointerException e) {
            //session or edit process expired
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException f) {
                log.error(f, f);
                throw new RuntimeException(f);
            }
            return;
        }

        if (epo == null) {
            log.error("null epo");
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                log.error(e, e);
                throw new RuntimeException(e);
            }
            return;
        }
        
        if (request.getParameter("_cancel") == null) {
        	
        	OntModel ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
        	String namespaceStr = request.getParameter("namespace");
        	String prefixStr = request.getParameter("prefix");

        	if ( (namespaceStr != null) && (prefixStr != null) ) {
        		
        		Property namespaceURIProp = ontModel.getProperty(VitroVocabulary.NAMESPACE_NAMESPACEURI);
                
                ontModel.enterCriticalSection(Lock.WRITE);
                try {
                		
                			Individual namespaceInd = null;
                	
                			StmtIterator stmtIt = ontModel.listStatements((Resource)null,(Property)namespaceURIProp,ontModel.createLiteral(namespaceStr));
                			if (stmtIt.hasNext()) {
                				Statement stmt = stmtIt.nextStatement();
                				Resource namespaceRes = stmt.getSubject();
                				if (namespaceRes.canAs(Individual.class)) {
                					namespaceInd = (Individual) namespaceRes.as(Individual.class);
                				}
                			}
                			
                			if (namespaceInd == null) {
                				namespaceInd = ontModel.createIndividual(ontModel.getResource(VitroVocabulary.NAMESPACE));
                				namespaceInd.addProperty(namespaceURIProp,namespaceStr);
                			}
                			
                			HashSet<Individual> mappingSet = new HashSet<Individual>();
                			
                			StmtIterator mappingStatementIt = namespaceInd.listProperties(ontModel.getProperty(VitroVocabulary.NAMESPACE_HASPREFIXMAPPING));
                			while (mappingStatementIt.hasNext()) {
                				Statement stmt = mappingStatementIt.nextStatement();
                				if (stmt.getObject().canAs(Individual.class)) {
                					mappingSet.add( (Individual) stmt.getObject().as(Individual.class) );
                				}
                			}
                			
                			for (Individual oldMapping : mappingSet) {
                				oldMapping.remove();	
                			}
                			
                			if (request.getParameter("_delete")==null) {
                				Individual newMappingInd = ontModel.createIndividual(ontModel.getResource(VitroVocabulary.NAMESPACE_PREFIX_MAPPING));
                				newMappingInd.addProperty(ontModel.getProperty(VitroVocabulary.NAMESPACE_PREFIX),prefixStr);
                				namespaceInd.addProperty(ontModel.getProperty(VitroVocabulary.NAMESPACE_HASPREFIXMAPPING),newMappingInd);
                			} 
                			
                } finally {
                   	ontModel.leaveCriticalSection();
                }
        	}
        }

        //if no page forwarder was set, just go back to referring page:
        //the referer stuff all will be changed so as not to rely on the HTTP header
        
        String referer = epo.getReferer();
        if (referer == null) {
            try {
                response.sendRedirect(defaultLandingPage);
            } catch (IOException e) {
                log.error(e, e);
                throw new RuntimeException(e);
            }
        } else {
            try {
                response.sendRedirect(referer);
            } catch (IOException e) {
                log.error(e, e);
                throw new RuntimeException(e);
            }
        }

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        // don't use get; state changes
    }
	
}
