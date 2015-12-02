/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;

/**
 * A minimal implementation of the OntologyDao.
 * 
 * I have only implemented the methods that I needed. Feel free to implement
 * others.
 */
public class OntologyDaoStub implements OntologyDao {
	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private final Map<String, Ontology> ontologies = new HashMap<String, Ontology>();
	
	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public String insertNewOntology(Ontology ontology) {
		ontologies.put(ontology.getURI(), ontology);
		return ontology.getURI();
	}

	@Override
	public List<Ontology> getAllOntologies() {
		return new ArrayList<Ontology>(ontologies.values());
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public Ontology getOntologyByURI(String ontologyURI) {
		throw new RuntimeException(
				"OntologyDaoStub.getOntologyByURI() not implemented.");
	}

	@Override
	public void updateOntology(Ontology ontology) {
		throw new RuntimeException(
				"OntologyDaoStub.updateOntology() not implemented.");
	}

	@Override
	public void deleteOntology(Ontology ontology) {
		throw new RuntimeException(
				"OntologyDaoStub.deleteOntology() not implemented.");
	}

}
