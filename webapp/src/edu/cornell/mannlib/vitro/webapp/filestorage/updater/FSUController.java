/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.filestorage.FileModelHelper;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;

/**
 * An object that can initialize one of the {@link FsuScanner}s.
 */
public interface FSUController {

	/** The Jena model. */
	Model getModel();

	/** The update log. */
	FSULog getUpdateLog();

	/** The directory where the old-style images were stored. */
	File getImageDirectory();

	/** The file storage system. */
	FileStorage getFileStorage();

	/** A file model helper with access to the DAO layer. */
	FileModelHelper getFileModelHelper();

	/** Where to store the files that were translated. */
	File getTranslatedDirectory();

	/** Where to store the files that weren't in use anyway. */
	File getUnreferencedDirectory();

}
