/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * All image properties should have literal values. Burn any that don't.
 */
public class NonLiteralPropertyRemover extends FsuScanner {

	public NonLiteralPropertyRemover(FSUController controller) {
		super(controller);
	}

	/**
	 * Remove any image properties whose objects are not {@link Literal}s.
	 */
	public void remove() {
		updateLog.section("Checking for image properties whose objects "
				+ "are not literals.");

		removeNonLiterals(imageProperty, "image file");
		removeNonLiterals(thumbProperty, "thumbnail");
	}

	/**
	 * Check all resources for bogus values on this property.
	 */
	private void removeNonLiterals(Property prop, String label) {
		ResIterator resources = model.listResourcesWithProperty(prop);
		try {
			while (resources.hasNext()) {
				Resource resource = resources.next();
				removeNonLiterals(resource, prop, label);
			}
		} finally {
			resources.close();
		}
	}

	/**
	 * Check this resource for bogus values onthis property.
	 */
	private void removeNonLiterals(Resource resource, Property prop,
			String label) {
		List<RDFNode> bogusValues = new ArrayList<RDFNode>();
		StmtIterator stmts = resource.listProperties(prop);
		try {
			while (stmts.hasNext()) {
				Statement stmt = stmts.next();
				RDFNode object = stmt.getObject();
				if (!object.isLiteral()) {
					bogusValues.add(object);
				}
			}
		} finally {
			stmts.close();
		}

		for (RDFNode bogusValue : bogusValues) {
			updateLog.warn(resource, "discarding " + label
					+ " property with non-literal as object: '" + bogusValue
					+ "'");
			model.createStatement(resource, prop, bogusValue).remove();
		}
	}

}
