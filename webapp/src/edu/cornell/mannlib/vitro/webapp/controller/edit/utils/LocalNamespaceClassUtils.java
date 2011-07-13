/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import edu.cornell.mannlib.vedit.beans.Option;
import edu.cornell.mannlib.vitro.webapp.beans.ResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalNamespaceClassUtils {
    private static final Log log = LogFactory.getLog(LocalNamespaceClassUtils.class.getName());
    
    public static List<VClass> getLocalNamespacesClasses(VitroRequest vreq, List<String> namespace) {
    	HashMap<String, String> namespaceHash = convertToHash(namespace);
       List<VClass> localClasses = new ArrayList<VClass>();
       List<VClass> allClasses = vreq.getWebappDaoFactory().getVClassDao().getAllVclasses();
       for(VClass v: allClasses) {
    	   String classNamespace = v.getNamespace();
    	   String classUri = v.getURI();
    	   System.out.println("uri is " + classUri + " and namespace is " + classNamespace);
    	   if(namespaceHash.containsKey(classNamespace)){
    		   localClasses.add(v);
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
    public static List<String> getLocalOntologyNamespaces(VitroRequest vreq) {
        HashMap<String, String> foundNamespaces = new HashMap<String, String>();
        String defaultNamespacePattern = getDefaultOntologyNamespace(vreq);
    	List<String> localNamespaces = new ArrayList<String>();
        List<VClass> allClasses = vreq.getWebappDaoFactory().getVClassDao().getAllVclasses();
        for(VClass v: allClasses) {
        	String namespace = v.getNamespace();
        	if(namespace.startsWith(defaultNamespacePattern) && !foundNamespaces.containsKey(namespace)) {
        		foundNamespaces.put(namespace, "true");
        	}
        }
        localNamespaces.addAll(foundNamespaces.keySet());
        return localNamespaces;
    }
    
    public static String getDefaultOntologyNamespace(VitroRequest vreq) {
    	String defaultNamespace= vreq.getWebappDaoFactory().getDefaultNamespace();
    	defaultNamespace = defaultNamespace.substring(0, defaultNamespace.lastIndexOf("/")) + "ontology/";
    	return defaultNamespace;
    }
}
