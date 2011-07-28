/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.ResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalNamespaceClassUtils {
    private static final Log log = LogFactory.getLog(LocalNamespaceClassUtils.class.getName());
    
    //Expects hash where key = namespace uri
    //return hash where key = class uri, and value = display Name + (prefix) of ontology
    public static HashMap<String, String> getLocalNamespacesClasses(VitroRequest vreq, HashMap<String, String> namespaces) {
       HashMap<String, String> localClasses = new HashMap<String, String>();
       List<VClass> allClasses = vreq.getWebappDaoFactory().getVClassDao().getAllVclasses();
       for(VClass v: allClasses) {
    	   String classNamespace = v.getNamespace();
    	   String classUri = v.getURI();
    	   
    	   if(namespaces.containsKey(classNamespace)){
    		   String namespaceLabel = namespaces.get(classNamespace);
    		   String prefix = namespaceLabel.substring(namespaceLabel.indexOf("(") + 1, namespaceLabel.indexOf(")"));
    		   localClasses.put(classUri, v.getName() + " ( " + prefix + ")");
    	   }
       }
       return localClasses;
    }
    
    private static HashMap<String, String> convertToHash(List<String> namespaces) {
    	HashMap<String, String> namespaceHash = new HashMap<String, String>();
    	for(String n: namespaces){
    		namespaceHash.put(n, "true");
    	}
    	return namespaceHash;
    }
    
    //Retrieve all VClasses and sort into local namespaces 
    //TODO: Check better mechanism utilizing sparql query
    //Can't depend on retrieval of classes b/c an ontology may not have any classes yet
    //Display name and URI, with URI being key
    public static HashMap<String, String> getLocalOntologyNamespaces(VitroRequest vreq) {
        HashMap<String, String> foundNamespaces = new HashMap<String, String>();
        String defaultNamespacePattern = getDefaultOntologyNamespace(vreq);
        
    	//Get all namespacs
    	//There's an APP for that!
        //defualt namespace pattern is null if the default namespace does not employ /individual
        if(defaultNamespacePattern != null) {
	    	 OntologyDao dao = vreq.getFullWebappDaoFactory().getOntologyDao();
	         List<Ontology> onts = dao.getAllOntologies();
	         for(Ontology on: onts) {
	        	String uri = on.getURI();
	        	if(uri.startsWith(defaultNamespacePattern)) {
	        		String name =  on.getName();
	        		String prefix = on.getPrefix();
	        		foundNamespaces.put(uri, name + " (" + prefix + ")");
	        	}
	         }
        }
    	
        return foundNamespaces;
    }
    
    public static String getDefaultOntologyNamespace(VitroRequest vreq) {
    	String defaultNamespace= vreq.getWebappDaoFactory().getDefaultNamespace();
    	//Assuming following linked data approach so expects /individual at end
    	int lastIndex = defaultNamespace.lastIndexOf("/individual");
    	//if namespace is correct
    	if(lastIndex != -1) {
    		defaultNamespace = defaultNamespace.substring(0, lastIndex) + "/ontology/";
    		return defaultNamespace;
    	} else {
    		log.error("Default namespace " + defaultNamespace + " should have /individual, returning null for default namespace");
    		return null;
    	}
    }
}
