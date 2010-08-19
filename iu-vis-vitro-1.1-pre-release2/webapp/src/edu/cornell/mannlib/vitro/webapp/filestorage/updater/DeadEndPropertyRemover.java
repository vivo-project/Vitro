/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * Removes any image properties (main or thumbnail) that point to files that
 * don't actually exist.
 */
public class DeadEndPropertyRemover extends FsuScanner {
	protected final File imageDirectory;

	public DeadEndPropertyRemover(FSUController controller) {
		super(controller);
		this.imageDirectory = controller.getImageDirectory();
	}

	/**
	 * Remove dead end properties for both main images and thumbnails.
	 */
	public void remove() {
		updateLog.section("Removing image properties whose "
				+ "referenced files do not exist.");

		removeDeadEndProperties(imageProperty, "main image");
		removeDeadEndProperties(thumbProperty, "thumbnail");
	}

	/**
	 * Check all of the individuals that possess this property.
	 */
	private void removeDeadEndProperties(Property prop, String label) {
		ResIterator resources = model.listResourcesWithProperty(prop);
		try {
			while (resources.hasNext()) {
				Resource resource = resources.next();
				removeDeadEndPropertiesFromResource(resource, prop, label);
			}
		} finally {
			resources.close();
		}
	}

	/**
	 * Check these statments on this resource. If any of them does not point to
	 * an existing file, remove the statement.
	 */
	private void removeDeadEndPropertiesFromResource(Resource resource,
			Property prop, String label) {
		for (Statement stmt : getStatements(resource, prop)) {
			RDFNode node = stmt.getObject();
			if (node.isLiteral()) {
				String filename = ((Literal)node).getString();
				File file = new File(imageDirectory, filename);
				if (!file.exists()) {
					updateLog.warn(resource, "removing link to " + label + " '"
							+ filename + "': file does not exist at '"
							+ file.getAbsolutePath() + "'.");
					model.remove(stmt);
				}
			}
		}
	}
	
}
