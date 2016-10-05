/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing.jena;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

public class NamespacesListingController extends BaseEditController {

    public void doGet(HttpServletRequest request, HttpServletResponse response) {    	
		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.USE_MISCELLANEOUS_ADMIN_PAGES.ACTION)) {
        	return;
        }

        VitroRequest vrequest = new VitroRequest(request);
               
		OntModel ontModel = ModelAccess.on(getServletContext()).getOntModel();
               
        ArrayList results = new ArrayList();
        request.setAttribute("results",results);
        results.add("XX");
        results.add("namespace");
        results.add("prefix");
        
        Property namespaceURIProp = ontModel.getProperty(VitroVocabulary.NAMESPACE_NAMESPACEURI);
        
        ontModel.enterCriticalSection(Lock.READ);
        try {
        	ClosableIterator closeIt = ontModel.listIndividuals(ontModel.getResource(VitroVocabulary.NAMESPACE));
        	try {
        		for (Iterator namespaceIt=closeIt; namespaceIt.hasNext();) {
        			Individual namespaceInd = (Individual) namespaceIt.next();
        			
        			String namespaceURI = "";
        			try {
        				namespaceURI = ((Literal)namespaceInd.getPropertyValue(namespaceURIProp)).getLexicalForm(); 
        			} catch (Exception e) { /* ignore it for now */ }
        			results.add("XX");
        			results.add(namespaceURI);
        			RDFNode prefixMapping = namespaceInd.getPropertyValue(ontModel.getProperty(VitroVocabulary.NAMESPACE_HASPREFIXMAPPING));
        			boolean prefixFound = false;
        			if ( (prefixMapping != null) && (prefixMapping.canAs(Individual.class)) ) {
        				Individual prefixMappingInd = (Individual) prefixMapping.as(Individual.class);
        				RDFNode prefixNode = prefixMappingInd.getPropertyValue(ontModel.getProperty(VitroVocabulary.NAMESPACE_PREFIX));
        				if ( (prefixNode != null) && prefixNode.isLiteral() ) {
        					prefixFound = true;
        					try {
        						results.add("<a href=\"editForm?controller=NamespacePrefix&amp;prefix="+((Literal)prefixNode).getLexicalForm()+"&amp;namespace="+URLEncoder.encode(namespaceURI,"UTF-8")+"\">"+((Literal)prefixNode).getLexicalForm()+"</a>");
        					} catch (Exception e) {
        						//
        					}
        				}
        			}
        			if (!prefixFound) {
        				try {
        					results.add("<a href=\"editForm?controller=NamespacePrefix&amp;namespace="+URLEncoder.encode(namespaceURI,"UTF-8")+"\">add prefix</a>");
        				} catch (Exception e) {
        					//
        				}
        			}
        		}
        	} finally {
        		closeIt.close();
        	}
        } finally {
           	ontModel.leaveCriticalSection();
        }

        request.setAttribute("columncount",new Integer(3));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Recognized Namespaces");
        try {
			JSPPageHandler.renderBasicPage(request, response, Controllers.HORIZONTAL_JSP);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    
}
