/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class JenaOutputUtils {
	
	private static final Log log = LogFactory.getLog(JenaOutputUtils.class.getName());
		
	public static void setNameSpacePrefixes(Model model, WebappDaoFactory wadf) {
		
		if (model == null) {
			log.warn("input model is null");
			return;
		}
		
		Map<String,String> prefixes = new HashMap<String,String>();
		List<Ontology> ontologies = wadf.getOntologyDao().getAllOntologies();
		Iterator<Ontology> iter = ontologies.iterator();
		String namespace = null;
		String prefix = null;
		
		prefixes.put("vitro", "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#");
		while (iter.hasNext()) {
			Ontology ontology = iter.next();
            			
			namespace = ontology.getURI(); // this method returns the namespace
            if (namespace == null || namespace.isEmpty()) {
            	log.warn("ontology with empty namespace found");
            	continue;
            }
            
            prefix = ontology.getPrefix();
            if (prefix == null || prefix.isEmpty()) {
            	log.debug("no prefix found for namespace: " + namespace);
            	continue;
            }
            
			prefixes.put(prefix,namespace);
		}
	    
		model.setNsPrefixes(prefixes);
		return;
	}	
}
