/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.ProfileException;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;

public class OntologyDaoJena extends JenaBaseDao implements OntologyDao {

    public OntologyDaoJena(WebappDaoFactoryJena wadf) {
        super(wadf);
    }
    
    // TODO: add model-per-ontology support
    @Override
    protected OntModel getOntModel() {
    	return getOntModelSelector().getTBoxModel();
    }
    
    public static synchronized String adjustOntologyURI(String ontologyURI) { 
    	String uri = ontologyURI.trim();
    	int length = uri.length();
		if ( (length>1) && (uri.charAt(length-1)=='#') ) { 
    		return uri.substring(0,length-1);
    	} else {
    		return uri;
    	}
    }
    
    private org.apache.jena.ontology.Ontology getOntology(String ontologyURI,
                                                          OntModel ontModel) {
    	
    	// Something non-ideal happens here.  There are places in the code that
    	// call getOntology() but don't pass the URI of the ontology resource 
    	// itself.  Instead, they pass the namespace that would appear
    	// in a PREFIX declaration.  For example, we might have an ontology with
    	// the namespace http://example.org/ontology# .
    	// A class in this namespace might have the URI 
    	// http://example.org/ontology#SomeClass .  The ontology resource 
    	// itself, however, may have the URI http://example.org/ontology 
    	// (no final hash mark).  To support assumptions in the code,
    	// this method calls adjustOntologyURI to remove a trailing hash
    	// mark if an ontology resource is not found at the specified URI.
    	try {
    		ontModel.enterCriticalSection(Lock.READ);
    		org.apache.jena.ontology.Ontology o = ontModel.getOntology(
    				ontologyURI);
    		if (o != null) {
    			return o;
    		} else {
    			return ontModel.getOntology(
        				adjustOntologyURI(ontologyURI));
    		}
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
            org.apache.jena.ontology.Ontology o = getOntology(ontology.getURI(),ontModel);
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
    	if (ontology == null) {
    		return null;
    	} 
    	try {
    		String ontologyURI = adjustAndValidateOntologyURI(ontology.getURI());
            ontModel.enterCriticalSection(Lock.WRITE);
            try {
                org.apache.jena.ontology.Ontology o = ontModel.createOntology(ontologyURI);
                if (ontology.getName() != null && ontology.getName().length()>0) {
                    o.setLabel(ontology.getName(), getDefaultLanguage());
                }
                if (ontology.getPrefix() != null && ontology.getPrefix().length()>0) {
                    addPropertyStringValue(o,ONTOLOGY_PREFIX_ANNOT,ontology.getPrefix(),ontModel);
                }
                return o.getURI();
            } finally {
                ontModel.leaveCriticalSection();
            }
    	} catch (URISyntaxException e) {
    		log.warn("Failed to insert new ontology: " + ontology, e);
    		throw new RuntimeException(e);
        }
    }

	private String adjustAndValidateOntologyURI(String uri) throws URISyntaxException {
		if (uri == null || uri.isEmpty()) {
			throw new URISyntaxException(uri, "URI is empty");
		}
		String adjusted = adjustOntologyURI(uri);
		return new URI(adjusted).toString();
	}

	public void updateOntology(Ontology ontology) {
    	updateOntology(ontology,getOntModel());
    }
    
    public void updateOntology(Ontology ontology, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        if (ontology != null && ontology.getURI() != null && ontology.getURI().length()>0) {
            try {
                org.apache.jena.ontology.Ontology o = getOntology(ontology.getURI(),ontModel);
                if (o == null) {
                    log.error("OntologyDaoJena.updateOntology() could not find ontology "+ontology.getURI()+" in Jena model");
                } else {
                    updateRDFSLabel(o, ontology.getName());
                    updatePropertyStringValue(o, ONTOLOGY_PREFIX_ANNOT, 
                            ontology.getPrefix(), ontModel);
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


}
