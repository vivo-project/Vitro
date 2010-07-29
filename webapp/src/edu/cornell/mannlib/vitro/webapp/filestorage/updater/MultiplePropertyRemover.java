/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * If a resource has more than one image or more than one thumbnail, this
 * discards the extras.
 */
public class MultiplePropertyRemover extends FsuScanner {

	public MultiplePropertyRemover(FSUController controller) {
		super(controller);
	}

	/**
	 * By now, we have removed any non-literals or dead ends, so keep the first
	 * one and discard any extras.
	 */
	public void remove() {
		updateLog.section("Checking for resources with more "
				+ "than one main image, or more than one thumbnail.");

		removeExtraProperties(imageProperty, "main image");
		removeExtraProperties(thumbProperty, "thumbnail");
	}

	/**
	 * Check each resource that has this property.
	 */
	public void removeExtraProperties(Property prop, String label) {
		for (Resource resource : ModelWrapper.listResourcesWithProperty(model,
				prop)) {
			removeExtraProperties(resource, prop, label);
		}
	}

	/**
	 * If this resource has more than one of this property, delete the extras.
	 */
	private void removeExtraProperties(Resource resource, Property prop,
			String label) {
		List<Statement> stmts = getStatements(resource, prop);
		for (int i = 1; i < stmts.size(); i++) {
			Statement stmt = stmts.get(i);
			RDFNode node = stmt.getObject();
			if (node.isLiteral()) {
				String value = ((Literal) node).getString();
				updateLog.warn(resource, "removing extra " + label
						+ " property: '" + value + "'");
			} else {
				updateLog.warn(resource, "removing extra " + label
						+ " property: '" + node + "'");
			}
			ModelWrapper.removeStatement(model, stmt);
		}
	}
}
