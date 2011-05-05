/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.dao.ApplicationDao;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class ApplicationDaoJena extends JenaBaseDao implements ApplicationDao {

    private static final Property LINKED_NAMESPACE_PROP = 
            ResourceFactory.createProperty(
                    VitroVocabulary.DISPLAY + "linkedNamespace");
    
    private static final Property ONTOLOGY_PREFIX_PROP = 
        ResourceFactory.createProperty(VitroVocabulary.vitroURI + "ontologyPrefixAnnot");
    
	Integer portalCount = null;
	
	List<String> externallyLinkedNamespaces = null;
    ModelChangedListener externalNamespaceChangeListener = null;
    
    Set<String> rdfaNamespaces = null;
    ModelChangedListener ontologyChangeListener = null;
	
    public ApplicationDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
        
        externalNamespaceChangeListener = new ExternalNamespaceChangeListener();
        getOntModelSelector().getDisplayModel().register(externalNamespaceChangeListener);
        
        ontologyChangeListener = new OntologyChangeListener();
        getOntModelSelector().getDisplayModel().register(ontologyChangeListener);
    }
    
    public void close() {
        if (externalNamespaceChangeListener != null) {
            getOntModelSelector().getDisplayModel().unregister(externalNamespaceChangeListener);
        }
        if (ontologyChangeListener != null) {
            getOntModelSelector().getDisplayModel().unregister(ontologyChangeListener);
        }        
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

	private static final boolean CLEAR_CACHE = true;
	
	@Override
	public synchronized List<String> getExternallyLinkedNamespaces() {
	    return getExternallyLinkedNamespaces(!CLEAR_CACHE);
	}

    public synchronized List<String> getExternallyLinkedNamespaces(boolean clearCache) {
        if (clearCache || externallyLinkedNamespaces == null) {            
            externallyLinkedNamespaces = new ArrayList<String>();
            OntModel ontModel = getOntModelSelector().getDisplayModel();
            NodeIterator nodes = ontModel.listObjectsOfProperty(LINKED_NAMESPACE_PROP);
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

    @Override
    public boolean isExternallyLinkedNamespace(String namespace) {
        List<String> namespaces = getExternallyLinkedNamespaces();
        return namespaces.contains(namespace);
    }
    
    private class ExternalNamespaceChangeListener extends StatementListener {
        
        @Override
        public void addedStatement(Statement stmt) {
            process(stmt);
        }
        
        @Override
        public void removedStatement(Statement stmt) {
            process(stmt);
        }
        
        //We could also listen for end-of-edit events,
        //but there should be so few of these statements that
        //it won't be very expensive to run this method multiple
        //times when the model is updated.
        
        private void process(Statement stmt) {
            if (stmt.getPredicate().equals(LINKED_NAMESPACE_PROP)) {
                getExternallyLinkedNamespaces(CLEAR_CACHE);
            }
        }
        
    }

    @Override
    public synchronized Set<String> getRdfaNamespaces() {
        return getRdfaNamespaces(!CLEAR_CACHE);
    }

    @Override
    public synchronized Set<String> getRdfaNamespaces(boolean clearCache) {
        
        if (clearCache || rdfaNamespaces == null) {            
            rdfaNamespaces = new HashSet<String>();
            OntologyDao ontDao = getWebappDaoFactory().getOntologyDao();
            List<Ontology> ontologies = ontDao.getAllOntologies();
            if (ontologies != null) {
                for (Ontology o : ontologies) {
                    String uri = o.getURI();
                    if (StringUtils.isBlank(uri)) {
                        continue;
                    }
                    String prefix = o.getPrefix();
                    if (prefix == null) {
                        if (VitroVocabulary.VITRO_PUBLIC.equals(uri)) {
                            prefix = "vpub";
                        }                 
                    }
                    if ( ! StringUtils.isBlank(prefix) ) {
                        rdfaNamespaces.add("xmlns:" + prefix + "=\"" + uri + "\"");
                    }
                }
            }
            // Removed by ontDao.getAllOntologies()
            rdfaNamespaces.add("xmlns:vitro=\"" + VitroVocabulary.vitroURI + "\"");
        }
        
        return rdfaNamespaces;
    }
        
    private class OntologyChangeListener extends StatementListener {
        
        @Override
        public void addedStatement(Statement stmt) {
            process(stmt);
        }
        
        @Override
        public void removedStatement(Statement stmt) {
            process(stmt);
        }
        
        //We could also listen for end-of-edit events,
        //but there should be so few of these statements that
        //it won't be very expensive to run this method multiple
        //times when the model is updated.
        
        private void process(Statement stmt) {
            if (stmt.getPredicate().equals(ONTOLOGY_PREFIX_PROP)) {
                getExternallyLinkedNamespaces(CLEAR_CACHE);
            }
        }        
    }
    
}
