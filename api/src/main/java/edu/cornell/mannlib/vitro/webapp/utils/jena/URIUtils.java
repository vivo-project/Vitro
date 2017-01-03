/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

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
