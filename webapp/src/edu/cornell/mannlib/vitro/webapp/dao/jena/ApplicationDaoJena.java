/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class ApplicationDaoJena extends JenaBaseDao implements ApplicationDao {

	Integer portalCount = null;
	List<String> externallyLinkedNamespaces = null;

	
    public ApplicationDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
	   
	
	public boolean isFlag1Active() {
		boolean somePortalIsFiltering=false;		
		if (portalCount == null) {
			boolean active = false;
			for (Portal p : getWebappDaoFactory().getPortalDao().getAllPortals()) {
				if (p.isFlag1Filtering()) {
					somePortalIsFiltering = true;
				}
			}
		}		
		return somePortalIsFiltering && getWebappDaoFactory().getPortalDao().getAllPortals().size() > 1;		
	}

	
	public boolean isFlag2Active() {
		return (getFlag2ValueMap().isEmpty()) ? false : true;
	}


    public List<String> getExternallyLinkedNamespaces() {
        if (externallyLinkedNamespaces == null) {            
            externallyLinkedNamespaces = new ArrayList<String>();
            OntModel ontModel = getOntModelSelector().getDisplayModel();
            Property linkedNamespaceProp = ontModel.getProperty(VitroVocabulary.DISPLAY + "linkedNamespace");
            NodeIterator nodes = ontModel.listObjectsOfProperty(linkedNamespaceProp);
            while (nodes.hasNext()) {
                RDFNode node = nodes.next();
                if (node.isLiteral()) {
                    String namespace = ((Literal)node).getLexicalForm();
                    // org.openrdf.model.impl.URIImpl.URIImpl.getNamespace() returns a 
                    // namespace with a final slash, so this makes matching easier.
                    // It also accords with the way the default namespace is defined.
                    if (!namespace.endsWith("/")) {
                        namespace = namespace + "/";
                    }
                    externallyLinkedNamespaces.add(namespace);
                }
            }
        }
        return externallyLinkedNamespaces;
    }
    
}
