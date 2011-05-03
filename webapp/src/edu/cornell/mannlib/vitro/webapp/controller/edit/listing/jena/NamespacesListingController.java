/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing.jena;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseMiscellaneousAdminPages;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class NamespacesListingController extends BaseEditController {

    public void doGet(HttpServletRequest request, HttpServletResponse response) {    	
        if (!isAuthorizedToDisplayPage(request, response, new Actions(new UseMiscellaneousAdminPages()))) {
        	return;
        }

        VitroRequest vrequest = new VitroRequest(request);
               
        OntModel ontModel = (OntModel) getServletContext().getAttribute("jenaOntModel");
               
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
        request.setAttribute("bodyJsp", Controllers.HORIZONTAL_JSP);
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        try {
            rd.forward(request,response);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    
}
