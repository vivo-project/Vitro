/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.hp.hpl.jena.rdf.model.Model;

public class NamespacePrefixMapSetup implements ServletContextListener {
	
	public void contextInitialized(ServletContextEvent sce) {
	    
	    if (AbortStartup.isStartupAborted(sce.getServletContext())) {
            return;
        }
	    
		HashMap<String,String> prefixToNamespace = new HashMap<String,String>();
		HashMap<String,String> namespaceToPrefix = new HashMap<String,String>();
		Model model = (Model) sce.getServletContext().getAttribute("jenaOntModel");
		long start = System.currentTimeMillis();
		int count = 0;
		String prefixPrefix = "p";
		for (String ns : (List<String>) model.listNameSpaces().toList() ) {
			System.out.println(prefixPrefix+count+" => "+ns);
			prefixToNamespace.put(prefixPrefix+count,ns);
			namespaceToPrefix.put(ns,prefixPrefix+count);
			count++;
		}
		sce.getServletContext().setAttribute("prefixToNamespaceMap",prefixToNamespace);
		sce.getServletContext().setAttribute("namespaceToPrefixMap",namespaceToPrefix);
		System.out.println( (System.currentTimeMillis()-start)+" ms to map namespaces");
	}

	public void contextDestroyed(ServletContextEvent sce) {
		//
	}
	
}
