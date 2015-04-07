/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

public class URIUtils {
	public static boolean hasExistingURI(String uriStr, OntModel ontModel) {
		ontModel.enterCriticalSection(Lock.READ);
		try {
			if (anyStatements(ontModel, createResource(uriStr), null, null)) {
				return true;
			}
			if (anyStatements(ontModel, null, createProperty(uriStr), null)) {
				return true;
			}
			if (anyStatements(ontModel, null, null, createResource(uriStr))) {
				return true;
			}
			return false;
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	public static boolean hasEditableEntity(String uriStr, OntModel ontModel) {
		ontModel.enterCriticalSection(Lock.READ);
		try {
			Resource res = createResource(uriStr);
			if (anyStatements(ontModel, res, RDF.type, OWL.Thing)) {
				return true;
			}
			if (anyStatements(ontModel, res, RDF.type, OWL.Class)) {
				return true;
			}
			if (anyStatements(ontModel, res, RDF.type, OWL.DatatypeProperty)) {
				return true;
			}
			if (anyStatements(ontModel, res, RDF.type, OWL.ObjectProperty)) {
				return true;
			}
			return false;
		} finally {
			ontModel.leaveCriticalSection();
		}
	}

	private static boolean anyStatements(OntModel m, Resource s, Property p,
			RDFNode o) {
		StmtIterator stmts = m.listStatements(s, p, o);
		try {
			return stmts.hasNext();
		} finally {
			stmts.close();
		}
	}

}
