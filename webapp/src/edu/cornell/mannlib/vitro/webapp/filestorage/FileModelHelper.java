/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage;

import java.util.List;

import org.apache.log4j.Logger;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Static methods to help manipulate the model, with regard to uploaded files.
 */
public class FileModelHelper {
	private static final Logger log = Logger.getLogger(FileModelHelper.class);

	/**
	 * Locate the individual that represents the bytestream of the main image.
	 * 
	 * @return the URI, or <code>null</code> if there is no such image.
	 */
	public static String getMainImageUri(Individual entity) {
		Individual mainFile = getRelatedIndividual(entity,
				VitroVocabulary.IND_MAIN_IMAGE);
		if (mainFile == null) {
			return null;
		}

		Individual byteStream = getRelatedIndividual(mainFile,
				VitroVocabulary.FS_DOWNLOAD_LOCATION);
		if (byteStream == null) {
			log.error("File individual had no associated bytestream: '"
					+ mainFile.getURI() + "'");
			return null;
		}

		return byteStream.getURI();
	}

	/**
	 * Find the filename of the main image.
	 * 
	 * @return the filename, or <code>null</code> if there is no such image.
	 */
	public static String getMainImageFilename(Individual entity) {
		Individual mainFile = getRelatedIndividual(entity,
				VitroVocabulary.IND_MAIN_IMAGE);
		if (mainFile == null) {
			return null;
		}

		String filename = getDataValue(mainFile, VitroVocabulary.FS_FILENAME);
		if (filename == null) {
			log.error("File individual had no filename: '"
					+ mainFile.getURI() + "'");
			return null;
		}

		return filename;
	}

	/**
	 * Locate the individual that represents the bytestream of the thumbnail of
	 * the main image.
	 * @return the URI, or <code>null</code> if there is no such thumbnail image.
	 */
	public static String getThumbnailUri(Individual entity) {
		Individual mainFile = getRelatedIndividual(entity,
				VitroVocabulary.IND_MAIN_IMAGE);
		if (mainFile == null) {
			return null;
		}

		Individual thumbFile = getRelatedIndividual(mainFile,
				VitroVocabulary.FS_THUMBNAIL_IMAGE);
		if (thumbFile == null) {
			log.warn("Main image file had no associated thumbnail: '"
					+ mainFile.getURI() + "'");
			return null;
		}

		Individual byteStream = getRelatedIndividual(thumbFile,
				VitroVocabulary.FS_DOWNLOAD_LOCATION);
		if (byteStream == null) {
			log.error("File individual had no associated bytestream: '"
					+ thumbFile.getURI() + "'");
			return null;
		}

		return byteStream.getURI();
	}

	/**
	 * Find the filename of the thumbnail of the main image.
	 * 
	 * @return the filename, or <code>null</code> if there is no such thumbnail image.
	 */
	public static String getThumbnailFilename(Individual entity) {
		Individual mainFile = getRelatedIndividual(entity,
				VitroVocabulary.IND_MAIN_IMAGE);
		if (mainFile == null) {
			return null;
		}

		Individual thumbFile = getRelatedIndividual(mainFile,
				VitroVocabulary.FS_THUMBNAIL_IMAGE);
		if (thumbFile == null) {
			log.warn("Main image file had no associated thumbnail: '"
					+ mainFile.getURI() + "'");
			return null;
		}

		String filename = getDataValue(thumbFile, VitroVocabulary.FS_FILENAME);
		if (filename == null) {
			log.error("File individual had no filename: '"
					+ thumbFile.getURI() + "'");
			return null;
		}

		return filename;
	}

	/**
	 * Inspect the object properties on this individual and find the object of
	 * the specified property.
	 * 
	 * @return the object of the first such property, or <code>null</code>.
	 */
	private static Individual getRelatedIndividual(Individual individual,
			String propertyUri) {
		List<ObjectPropertyStatement> opStmts = individual
				.getObjectPropertyStatements();
		for (ObjectPropertyStatement opStmt : opStmts) {
			if (opStmt.getPropertyURI().equals(propertyUri)) {
				return opStmt.getObject();
			}
		}
		return null;
	}

	/**
	 * Inspect the data properties on this individual and find the value of
	 * the specified property.
	 * 
	 * @return the value of the first such property, or <code>null</code>.
	 */
	private static String getDataValue(Individual individual, String propertyUri) {
		List<DataPropertyStatement> dpStmts = individual
				.getDataPropertyStatements();
		for (DataPropertyStatement dpStmt : dpStmts) {
			if (dpStmt.getDatapropURI().equals(propertyUri)) {
				return dpStmt.getString();
			}
		}
		return null;
	}

	/** No need for instances because all of the methods are static. */
	private FileModelHelper() {
		// nothing to instantiate.
	}

}
