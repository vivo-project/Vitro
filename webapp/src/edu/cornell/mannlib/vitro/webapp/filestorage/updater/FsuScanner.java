/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

/**
 * Base class for the tools that scan the model. Holds some useful fields and
 * some utility methods.
 */
public abstract class FsuScanner {
	protected final Model model;
	protected final FSULog updateLog;

	protected final Property imageProperty;
	protected final Property thumbProperty;

	public FsuScanner(FSUController controller) {
		this.model = controller.getModel();
		this.updateLog = controller.getUpdateLog();

		this.imageProperty = model.createProperty(FileStorageUpdater.IMAGEFILE);
		this.thumbProperty = model
				.createProperty(FileStorageUpdater.IMAGETHUMB);
	}

	/**
	 * Read all of the specified properties on a resource, and return a
	 * {@link List} of the {@link String} values.
	 */
	protected List<String> getValues(Resource resource, Property property) {
		List<String> list = new ArrayList<String>();
		StmtIterator stmts = resource.listProperties(property);
		try {
			while (stmts.hasNext()) {
				Statement stmt = stmts.next();
				RDFNode object = stmt.getObject();
				if (object.isLiteral()) {
					list.add(((Literal) object).getString());
				} else {
					updateLog.error(resource,
							"property value was not a literal: "
									+ "property is '" + property.getURI()
									+ "', value is '" + object + "'");
				}
			}
		} finally {
			stmts.close();
		}
		return list;
	}

	/**
	 * Read all of the specified properties on a resource, and return a
	 * {@link List} of the {@link Statement}s.
	 */
	protected List<Statement> getStatements(Resource resource, Property property) {
		List<Statement> list = new ArrayList<Statement>();
		
		resource.getModel().enterCriticalSection(Lock.READ);
		StmtIterator stmts = resource.listProperties(property);
		try {
			while (stmts.hasNext()) {
				list.add(stmts.next());
			}
		} finally {
			stmts.close();
			resource.getModel().leaveCriticalSection();
		}
		return list;
	}

	/**
	 * Find the filename within a path so we can add this prefix to it, while
	 * retaining the path.
	 */
	protected String addFilenamePrefix(String prefix, String path) {
		int slashHere = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		if (slashHere == -1) {
			return prefix + path;
		} else {
			String dirs = path.substring(0, slashHere + 1);
			String filename = path.substring(slashHere + 1);
			return dirs + prefix + filename;
		}
	}

	/**
	 * We are about to create a file - if a file of this name already exists,
	 * increment the name until we have no collision.
	 * 
	 * @return the original file, or the file with the incremented name.
	 */
	protected File checkNameConflicts(final File file) {
		if (!file.exists()) {
			// No conflict.
			return file;
		}

		File parent = file.getParentFile();
		String filename = file.getName();
		for (int i = 0; i < 100; i++) {
			File newFile = new File(parent, i + filename);
			if (!newFile.exists()) {
				updateLog.log("File '" + file + "' already exists, using '"
						+ newFile + "' to avoid conflict.");
				return newFile;
			}
		}

		updateLog.error("File '" + file
				+ "' already exists. Unable to avoid conflict.");
		return file;
	}
}
