package edu.cornell.mannlib.vitro.webapp.dao;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;

import java.util.List;

public interface OntologyDao {

    public abstract List<Ontology> getAllOntologies();

    public abstract Ontology getOntologyByURI(String ontologyURI);

    String insertNewOntology(Ontology ontology);

    void updateOntology(Ontology ontology);

    void deleteOntology(Ontology ontology);

}