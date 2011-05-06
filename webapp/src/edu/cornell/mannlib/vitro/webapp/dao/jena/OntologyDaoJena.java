/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.ProfileException;
import com.hp.hpl.jena.rdf.listeners.StatementListener;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class OntologyDaoJena extends JenaBaseDao implements OntologyDao {

    private static final Property ONTOLOGY_PREFIX_PROP = 
        ResourceFactory.createProperty(VitroVocabulary.vitroURI + "ontologyPrefixAnnot");

    Map<String, String> ontNamespaceToPrefix = null;
    ModelChangedListener ontologyChangeListener = null;
    
    public OntologyDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
        
        ontologyChangeListener = new OntologyChangeListener();
        getOntModelSelector().getTBoxModel().register(ontologyChangeListener);
    }
    
    public void close() {
        if (ontologyChangeListener != null) {
            getOntModelSelector().getTBoxModel().unregister(ontologyChangeListener);
        }        
    }
    
    // TODO: add model-per-ontology support
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getTBoxModel();
    }
    
    public static synchronized String adjustOntologyURI(String ontologyURI) { 
    	if ( (ontologyURI.length()>1) && (ontologyURI.charAt(ontologyURI.length()-1)=='#') ) { 
    		return ontologyURI.substring(0,ontologyURI.length()-1);
    	} else {
    		return ontologyURI;
    	}
    }
    
    private com.hp.hpl.jena.ontology.Ontology getOntology(String ontologyURI, OntModel ontModel) {
    	try {
    		ontModel.enterCriticalSection(Lock.READ);
    		return ontModel.getOntology(adjustOntologyURI(ontologyURI));
    	} finally {
    		ontModel.leaveCriticalSection();
    	}
    }

    public void deleteOntology(Ontology ontology) {
    	deleteOntology(ontology,getOntModel());
    }

    public void deleteOntology(Ontology ontology, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        try {
            com.hp.hpl.jena.ontology.Ontology o = getOntology(ontology.getURI(),ontModel);
            if (o == null) {
                o = ontModel.getOntology(adjustOntologyURI(ontology.getURI()));
            }
            if (o != null) {
                o.remove();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
    }

    public List<Ontology> getAllOntologies() {
        List<Ontology> ontologies = new ArrayList<Ontology>();
        getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator ontIt = getOntModel().listOntologies();
            try {
                while (ontIt.hasNext()) {
                    OntResource ontRes = (OntResource) ontIt.next();
                    if (!(NONUSER_NAMESPACES.contains(ontRes.getURI()) || NONUSER_NAMESPACES.contains(ontRes.getURI()+"#")))
                        ontologies.add(ontologyFromOntologyResource(ontRes));
                }
            } finally {
                ontIt.close();
            }
        } catch (ProfileException e) {
        	// The current profile does not support listing ontology objects, so we will return an empty list
        } finally {
            getOntModel().leaveCriticalSection();
        }
        Collections.sort(ontologies);
        return (ontologies.size()>0) ? ontologies : null;
    }
    
    public Ontology getOntologyByURI(String ontologyURI) {
    	Ontology o = null;
    	try {
    		o = ontologyFromOntologyResource(getOntology(ontologyURI,getOntModel()));	
    	} catch (Exception e) {}
    	if (o == null) {
	        try {
	        	o = ontologyFromOntologyResource(getOntology(adjustOntologyURI(ontologyURI),getOntModel()));
	        } catch (Exception e) {}
    	}
        return o;
    }

    public String insertNewOntology(Ontology ontology) {
    	return insertNewOntology(ontology,getOntModel());
    }

    public String insertNewOntology(Ontology ontology, OntModel ontModel) {
        if (ontology != null && ontology.getURI() != null && ontology.getURI().length()>0) {
            ontModel.enterCriticalSection(Lock.WRITE);
            try {
                com.hp.hpl.jena.ontology.Ontology o = ontModel.createOntology(adjustOntologyURI(ontology.getURI()));
                if (ontology.getName() != null && ontology.getName().length()>0) {
                    o.setLabel(ontology.getName(), PREFERRED_LANGUAGES[0]);
                }
                if (ontology.getPrefix() != null && ontology.getPrefix().length()>0) {
                    addPropertyStringValue(o,ONTOLOGY_PREFIX_ANNOT,ontology.getPrefix(),ontModel);
                }
                return o.getURI();
            } finally {
                ontModel.leaveCriticalSection();
            }
        } else {
            return null;
        }
    }

    public void updateOntology(Ontology ontology) {
    	updateOntology(ontology,getOntModel());
    }
    
    public void updateOntology(Ontology ontology, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        if (ontology != null && ontology.getURI() != null && ontology.getURI().length()>0) {
            try {
                com.hp.hpl.jena.ontology.Ontology o = getOntology(ontology.getURI(),ontModel);
                if (o == null) {
                    log.error("OntologyDaoJena.updateOntology() could not find ontology "+ontology.getURI()+" in Jena model");
                } else {
                    if (ontology.getName() != null && ontology.getName().length()>0) {
                        o.setLabel(ontology.getName(), PREFERRED_LANGUAGES[0]);
                    }
                    if (ontology.getPrefix() != null && ontology.getPrefix().length()>0) {
                        updatePropertyStringValue(o,ONTOLOGY_PREFIX_ANNOT,ontology.getPrefix(),ontModel);
                    }
                }
            } finally {
                ontModel.leaveCriticalSection();
            }
        }
    }

    private Ontology ontologyFromOntologyResource(OntResource ontRes) {
        if (ontRes == null)
            return null;
        Ontology ontology = new Ontology();
        ontology.setName(getLabelOrId(ontRes));
        ontology.setURI(ontRes.getURI());
        ontology.setPrefix(getPropertyStringValue(ontRes,ONTOLOGY_PREFIX_ANNOT));
        
        // we need this for the time being because other things are expecting getAllOntologies() to return objects with trailing fragment separators
        // TODO: improve this so '#' is only appended if the last character is not an XML name character
        if (!(ontology.getURI().substring(ontology.getURI().length()-1,ontology.getURI().length()).equals("#") ||
                ontology.getURI().substring(ontology.getURI().length()-1,ontology.getURI().length()).equals("/")
            )) {
            ontology.setURI(ontology.getURI()+"#");
        }
        
        return ontology;
    }

    private static final boolean CLEAR_CACHE = true;
    
    @Override
    public synchronized Map<String, String> getOntNsToPrefixMap() {
        return getOntNsToPrefixMap(!CLEAR_CACHE);
    }

    private synchronized Map<String, String> getOntNsToPrefixMap(boolean clearCache) {
        
        if (clearCache || ontNamespaceToPrefix == null) {            
            ontNamespaceToPrefix = new HashMap<String, String>();
            List<Ontology> ontologies = getAllOntologies();
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
                        } else {
                            prefix = "";
                        }
                    }

                    ontNamespaceToPrefix.put(uri, prefix);
                }
            }
            // Removed by getAllOntologies()
            ontNamespaceToPrefix.put(VitroVocabulary.vitroURI, "vitro");
        }
        
        return ontNamespaceToPrefix;
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
        
        // We could also listen for end-of-edit events,
        // but there should be so few of these statements that
        // it won't be very expensive to run this method multiple
        // times when the model is updated.
        
        private void process(Statement stmt) {
            if (stmt.getPredicate().equals(ONTOLOGY_PREFIX_PROP)) {
                getOntNsToPrefixMap(CLEAR_CACHE);
            }
        }        
    }
    

}
