/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

public class URIUtils {
	
	private static final Log log = LogFactory.getLog(URIUtils.class.getName());
		
	 public static boolean hasExistingURI(String uriStr, OntModel ontModel) {
	    	boolean existingURI = false;
			ontModel.enterCriticalSection(Lock.READ);
			try {
				Resource newURIAsRes = ResourceFactory.createResource(uriStr);
				Property newURIAsProp = ResourceFactory.createProperty(uriStr);
				StmtIterator closeIt = ontModel.listStatements(
						newURIAsRes, null, (RDFNode)null);
				if (closeIt.hasNext()) {
					existingURI = true;
					
				}
				//if not in the subject position, check in object position
				if (!existingURI) {
					closeIt = ontModel.listStatements(null, null, newURIAsRes);
					if (closeIt.hasNext()) {
						existingURI= true;
					}
				}
				//Check for property
				if (!existingURI) {
					closeIt = ontModel.listStatements(
							null, newURIAsProp, (RDFNode)null);
					if (closeIt.hasNext()) {
						existingURI = true;
					}
				}
			} finally {
				ontModel.leaveCriticalSection();
			}
			
			return existingURI;	
	    }
}
