/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.testing;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createLangLiteral;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createPlainLiteral;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createStatement;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createTypedLiteral;

import java.util.SortedSet;
import java.util.TreeSet;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Just some helper methods for Test classes that work with models.
 */
public class ModelUtilitiesTestHelper {
	public static Model model(Statement... stmts) {
		return ModelFactory.createDefaultModel().add(stmts);
	}

	public static Statement typeStatement(String subjectUri, String classUri) {
		return createStatement(createResource(subjectUri), RDF.type,
				createResource(classUri));
	}

	public static Statement objectProperty(String subjectUri,
			String propertyUri, String objectUri) {
		return createStatement(createResource(subjectUri),
				createProperty(propertyUri), createResource(objectUri));
	}

	public static Statement dataProperty(String subjectUri, String propertyUri,
			String objectValue) {
		return createStatement(createResource(subjectUri),
				createProperty(propertyUri), createPlainLiteral(objectValue));
	}

	public static Statement dataProperty(String subjectUri, String propertyUri,
			Object objectValue, XSDDatatype dataType) {
		return createStatement(createResource(subjectUri),
				createProperty(propertyUri),
				createTypedLiteral(String.valueOf(objectValue), dataType));
	}

	public static Statement dataProperty(String subjectUri, String propertyUri,
			String objectValue, String language) {
		return createStatement(createResource(subjectUri),
				createProperty(propertyUri),
				createLangLiteral(objectValue, language));
	}

	public static SortedSet<String> modelToStrings(Model m) {
		SortedSet<String> set = new TreeSet<>();
		for (Statement stmt : m.listStatements().toList()) {
			set.add(stmt.toString());
		}
		return set;
	}

}
