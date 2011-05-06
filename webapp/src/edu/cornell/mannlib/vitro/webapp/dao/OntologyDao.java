/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;

public interface OntologyDao {

    public abstract List<Ontology> getAllOntologies();

    public abstract Ontology getOntologyByURI(String ontologyURI);

    String insertNewOntology(Ontology ontology);

    void updateOntology(Ontology ontology);

    void deleteOntology(Ontology ontology);
    
    public Map<String, String> getOntNsToPrefixMap();

}