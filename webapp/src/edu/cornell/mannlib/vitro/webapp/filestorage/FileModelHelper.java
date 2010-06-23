/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

/**
 * <p>
 * A collection of methods to help manipulate the model, with regard to uploaded
 * files.
 * </p>
 * <p>
 * Some of the public methods are static, since the Individual that is passed as
 * a parameter holds all necessary references for the operation. Other methods
 * require an instance, which is initialized with a {@link WebappDaoFactory}.
 * </p>
 */
public class FileModelHelper {
	private static final Log log = LogFactory.getLog(FileModelHelper.class);

	// ----------------------------------------------------------------------
	// Static methods -- the Individual holds all necessary references.
	// ----------------------------------------------------------------------

	/**
	 * Is this a FileByteStream individual?
	 */
	public static boolean isFileBytestream(Individual entity) {
		for (VClass vClass : entity.getVClasses()) {
			if (VitroVocabulary.FS_BYTESTREAM_CLASS.equals(vClass.getURI())) {
				log.debug("Entity '" + entity.getURI() + "' is a bytestream");
				return true;
			}
		}
		log.debug("Entity '" + entity.getURI() + "' is not a bytestream");
		return false;
	}

	/**
	 * Locate the file surrogate for the main image of this entity.
	 * 
	 * @return the surrogate, or <code>null</code> if there is no such image, or
	 *         if the entity itself is <code>null</code>.
	 */
	public static Individual getMainImage(Individual entity) {
		if (entity == null) {
			return null;
		}

		Individual mainFile = entity
				.getRelatedIndividual(VitroVocabulary.IND_MAIN_IMAGE);

		if (mainFile == null) {
			log.debug("Entity '" + entity.getURI()
					+ "' had no associated main image.");
			return null;
		} else {
			log.debug("Entity '" + entity.getURI()
					+ "' had associated main image: '" + mainFile.getURI()
					+ "'");
			return mainFile;
		}
	}

	/**
	 * Locate the file surrogate for the thumbnail of this file.
	 * 
	 * @return the surrogate, or <code>null</code> if there is no thumbnail, or
	 *         if the file itself is <code>null</code>.
	 */
	public static Individual getThumbnailForImage(Individual fileSurrogate) {
		if (fileSurrogate == null) {
			return null;
		}

		Individual thumbFile = fileSurrogate
				.getRelatedIndividual(VitroVocabulary.FS_THUMBNAIL_IMAGE);

		if (thumbFile == null) {
			log.warn("Main image file '" + fileSurrogate.getURI()
					+ "' had no associated thumbnail.");
			return null;
		} else {
			log.debug("Main image file '" + fileSurrogate.getURI()
					+ "' had associated thumbnail: '" + thumbFile.getURI()
					+ "'");
			return thumbFile;
		}
	}

	/**
	 * Locate the bytestream object for this file.
	 * 
	 * @return the bytestream object, or <code>null</code> if there is no
	 *         bytestream, or if the file itself is <code>null</code>.
	 */
	public static Individual getBytestreamForFile(Individual fileSurrogate) {
		if (fileSurrogate == null) {
			return null;
		}

		Individual byteStream = fileSurrogate
				.getRelatedIndividual(VitroVocabulary.FS_DOWNLOAD_LOCATION);

		if (byteStream == null) {
			log.error("File surrogate '" + fileSurrogate.getURI()
					+ "' had no associated bytestream.");
			return null;
		} else {
			log.debug("File surroage'" + fileSurrogate.getURI()
					+ "' had associated bytestream: '" + byteStream.getURI()
					+ "'");
			return byteStream;
		}
	}

	/**
	 * Find the filename for this file.
	 * 
	 * @return the filename, or <code>null</code> if the file itself is
	 *         <code>null</code>.
	 */
	public static String getFilename(Individual fileSurrogate) {
		if (fileSurrogate == null) {
			return null;
		}

		String filename = fileSurrogate
				.getDataValue(VitroVocabulary.FS_FILENAME);

		if (filename == null) {
			log.error("File had no filename: '" + fileSurrogate.getURI() + "'");
		} else {
			log.debug("Filename for '" + fileSurrogate.getURI() + "' was '"
					+ filename + "'");
		}
		return filename;
	}

	/**
	 * Find the MIME type for this file.
	 * 
	 * @return the MIME type, or <code>null</code> if the file itself is
	 *         <code>null</code>.
	 */
	public static String getMimeType(Individual fileSurrogate) {
		if (fileSurrogate == null) {
			return null;
		}

		String mimeType = fileSurrogate
				.getDataValue(VitroVocabulary.FS_MIME_TYPE);

		if (mimeType == null) {
			log.error("File had no mimeType: '" + fileSurrogate.getURI() + "'");
		} else {
			log.debug("mimeType for '" + fileSurrogate.getURI() + "' was '"
					+ mimeType + "'");
		}
		return mimeType;
	}

	/**
	 * Return the URI for this individual, or <code>null</code> if the
	 * individual is <code>null</code>.
	 */
	private static String getUri(Individual entity) {
		if (entity == null) {
			return null;
		} else {
			return entity.getURI();
		}
	}

	/**
	 * Locate the URI of the bytestream of the main image for this entity.
	 * 
	 * @return the URI, or <code>null</code> if there is no such bytestream, or
	 *         if the entity itself is <code>null</code>.
	 */
	public static String getMainImageBytestreamUri(Individual entity) {
		Individual mainFile = getMainImage(entity);
		Individual byteStream = getBytestreamForFile(mainFile);
		return getUri(byteStream);
	}

	/**
	 * Find the filename of the main image for this entity.
	 * 
	 * @return the filename, or <code>null</code> if there is no such image.
	 */
	public static String getMainImageFilename(Individual entity) {
		Individual mainFile = getMainImage(entity);
		return getFilename(mainFile);
	}

	/**
	 * Locate the individual that represents the bytestream of the thumbnail of
	 * the main image for this entity.
	 * 
	 * @return the URI, or <code>null</code> if there is no such thumbnail
	 *         image, or if the entity itself is <code>null</code>.
	 */
	public static String getThumbnailBytestreamUri(Individual entity) {
		Individual mainFile = getMainImage(entity);
		Individual thumbFile = getThumbnailForImage(mainFile);
		Individual byteStream = getBytestreamForFile(thumbFile);
		return getUri(byteStream);
	}

	/**
	 * Find the filename of the thumbnail of the main image.
	 * 
	 * @return the filename, or <code>null</code> if there is no such thumbnail
	 *         image, or if the entity itself is <code>null</code>.
	 */
	public static String getThumbnailFilename(Individual entity) {
		Individual mainFile = getMainImage(entity);
		Individual thumbFile = getThumbnailForImage(mainFile);
		return getFilename(thumbFile);
	}

	// ----------------------------------------------------------------------
	// Instance methods -- need access to a WebappDaoFactory
	// ----------------------------------------------------------------------

	private final IndividualDao individualDao;
	private final ObjectPropertyStatementDao objectPropertyStatementDao;
	private final DataPropertyStatementDao dataPropertyStatementDao;

	public FileModelHelper(WebappDaoFactory webappDaoFactory) {
		this.individualDao = webappDaoFactory.getIndividualDao();
		this.objectPropertyStatementDao = webappDaoFactory
				.getObjectPropertyStatementDao();
		this.dataPropertyStatementDao = webappDaoFactory
				.getDataPropertyStatementDao();
	}

	/**
	 * Some of these methods require an Individual as an argument.
	 */
	public Individual getIndividualByUri(String uri) {
		return individualDao.getIndividualByURI(uri);
	}

	/**
	 * If this URI represents a ByteStream object, we need to find it's
	 * surrogate object in order to find the mime type.
	 * 
	 * @return the mime type, or <code>null</code> if we couldn't find the mime
	 *         type, or if the bytestream object itself is null.
	 */
	public String getMimeTypeForBytestream(String bytestreamUri) {
		if (bytestreamUri == null) {
			return null;
		}

		ObjectPropertyStatement opStmt = new ObjectPropertyStatementImpl(null,
				VitroVocabulary.FS_DOWNLOAD_LOCATION, bytestreamUri);
		List<ObjectPropertyStatement> stmts = objectPropertyStatementDao
				.getObjectPropertyStatements(opStmt);
		if (stmts.size() > 1) {
			String uris = "";
			for (ObjectPropertyStatement stmt : stmts) {
				uris += "'" + stmt.getSubjectURI() + "' ";
			}
			log.warn("Found " + stmts.size() + " Individuals that claim '"
					+ bytestreamUri + "' as its bytestream:" + uris);
		}
		if (stmts.isEmpty()) {
			log.warn("No individual claims '" + "' as its bytestream.");
			return null;
		}
		Individual surrogate = individualDao.getIndividualByURI(stmts.get(0)
				.getSubjectURI());

		return getMimeType(surrogate);
	}

	/**
	 * Create a file surrogate individual in the model.
	 */
	public Individual createFileIndividual(String mimeType, String filename,
			Individual byteStream) {
		Individual file = new IndividualImpl();
		file.setVClassURI(VitroVocabulary.FS_FILE_CLASS);

		String uri = null;
		try {
			uri = individualDao.insertNewIndividual(file);
		} catch (InsertException e) {
			throw new IllegalStateException(
					"Failed to create the file individual.", e);
		}

		dataPropertyStatementDao
				.insertNewDataPropertyStatement(new DataPropertyStatementImpl(
						uri, VitroVocabulary.FS_FILENAME, filename));
		dataPropertyStatementDao
				.insertNewDataPropertyStatement(new DataPropertyStatementImpl(
						uri, VitroVocabulary.FS_MIME_TYPE, mimeType));
		objectPropertyStatementDao
				.insertNewObjectPropertyStatement(new ObjectPropertyStatementImpl(
						uri, VitroVocabulary.FS_DOWNLOAD_LOCATION, byteStream
								.getURI()));

		return individualDao.getIndividualByURI(uri);
	}

	/**
	 * Create a bytestream individual in the model.
	 */
	public Individual createByteStreamIndividual() {
		Individual byteStream = new IndividualImpl();
		byteStream.setVClassURI(VitroVocabulary.FS_BYTESTREAM_CLASS);

		String uri = null;
		try {
			uri = individualDao.insertNewIndividual(byteStream);
		} catch (InsertException e) {
			throw new IllegalStateException(
					"Failed to create the bytestream individual.", e);
		}

		return individualDao.getIndividualByURI(uri);
	}

	/**
	 * Store this file surrogate as the main image on this entity.
	 */
	public void setAsMainImageOnEntity(Individual person,
			Individual imageSurrogate) {
		person.setMainImageUri(imageSurrogate.getURI());
		individualDao.updateIndividual(person);
		log.debug("Set main image '" + getUri(imageSurrogate) + "' on '"
				+ person.getURI() + "'");
	}

	/**
	 * Remove the current main image from this entity.
	 * 
	 * @return the file surrogate, or <code>null</code> if there was none.
	 */
	public Individual removeMainImage(Individual person) {
		Individual mainImage = getMainImage(person);
		person.setMainImageUri(null);
		individualDao.updateIndividual(person);
		log.debug("Removed main image '" + getUri(mainImage) + "' from '"
				+ person.getURI() + "'");
		return mainImage;
	}

	/**
	 * Store this file surrogate as the thumnail on this entity.
	 */
	public void setThumbnailOnIndividual(Individual entity,
			Individual thumbnailSurrogate) {
		String mainImageUri = entity.getMainImageUri();
		objectPropertyStatementDao
				.insertNewObjectPropertyStatement(new ObjectPropertyStatementImpl(
						mainImageUri, VitroVocabulary.FS_THUMBNAIL_IMAGE,
						thumbnailSurrogate.getURI()));
	}

	/**
	 * Are there any ObjectPropertyStatements in the model whose object is this
	 * file surrogate?
	 */
	public boolean isFileReferenced(Individual surrogate) {
		ObjectPropertyStatement opStmt = new ObjectPropertyStatementImpl(null,
				null, surrogate.getURI());
		List<ObjectPropertyStatement> stmts = objectPropertyStatementDao
				.getObjectPropertyStatements(opStmt);
		if (log.isDebugEnabled()) {
			log.debug(stmts.size() + " statements referencing '"
					+ surrogate.getURI() + "'");
			for (ObjectPropertyStatement stmt : stmts) {
				log.debug("'" + stmt.getSubjectURI() + "' -- '"
						+ stmt.getPropertyURI() + "' -- '"
						+ stmt.getObjectURI() + "'");
			}
		}
		return !stmts.isEmpty();
	}

	/**
	 * This file is being deleted; remove both the surrogate and its bytestream
	 * from the model.
	 */
	public void removeFileFromModel(Individual surrogate) {
		Individual bytestream = getBytestreamForFile(surrogate);
		individualDao.deleteIndividual(bytestream);
		individualDao.deleteIndividual(surrogate);
	}

}
