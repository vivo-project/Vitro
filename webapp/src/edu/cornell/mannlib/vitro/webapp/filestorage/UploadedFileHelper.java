/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileAlreadyExistsException;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.FileInfo;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;

/**
 * A helper object to handle the mundane details of dealing with uploaded files
 * in the model.
 */
public class UploadedFileHelper {
	private static final Log log = LogFactory.getLog(UploadedFileHelper.class);

	private final FileStorage fileStorage;
	private final WebappDaoFactory wadf;
	private final IndividualDao individualDao;
	private final DataPropertyStatementDao dataPropertyStatementDao;
	private final ObjectPropertyStatementDao objectPropertyStatementDao;
	private final ServletContext ctx;

	public UploadedFileHelper(FileStorage fileStorage, WebappDaoFactory wadf, ServletContext ctx) {
		this.fileStorage = fileStorage;
		this.wadf = wadf;
		this.individualDao = wadf.getIndividualDao();
		this.dataPropertyStatementDao = wadf.getDataPropertyStatementDao();
		this.objectPropertyStatementDao = wadf.getObjectPropertyStatementDao();
		this.ctx = ctx;
	}

	/**
	 * We have a filename, a mimetype, and some content. Create a file in the
	 * file storage system and in the model.
	 * 
	 * @return information about the newly created file.
	 */
	public FileInfo createFile(String filename, String mimeType,
			InputStream inputStream) throws FileAlreadyExistsException,
			IOException {
		if (filename == null) {
			throw new NullPointerException("filename may not be null.");
		}
		if (mimeType == null) {
			throw new NullPointerException("mimeType may not be null.");
		}
		if (inputStream == null) {
			throw new NullPointerException("inputStream may not be null.");
		}

		// Create the file individuals in the model
		Individual byteStream = createByteStreamIndividual(filename);
		String bytestreamUri = byteStream.getURI();
		String aliasUrl = byteStream.getDataValue(VitroVocabulary.FS_ALIAS_URL);

		Individual file = createFileIndividual(mimeType, filename, byteStream);
		String fileUri = file.getURI();

		// Store the file in the FileStorage system.
		fileStorage.createFile(bytestreamUri, filename, inputStream);

		// And wrap it all up in a tidy little package.
		FileInfo.Builder builder = new FileInfo.Builder();
		builder.setFilename(filename);
		builder.setMimeType(mimeType);
		builder.setUri(fileUri);
		builder.setBytestreamUri(bytestreamUri);
		builder.setBytestreamAliasUrl(aliasUrl);
		return builder.build();
	}

	/**
	 * Record this image file and thumbnail on this entity.
	 */
	public void setImagesOnEntity(String entityUri, FileInfo mainInfo,
			FileInfo thumbInfo) {
		if (entityUri == null) {
			throw new NullPointerException("entityUri may not be null.");
		}
		if (mainInfo == null) {
			throw new NullPointerException("mainInfo may not be null.");
		}
		if (thumbInfo == null) {
			throw new NullPointerException("thumbInfo may not be null.");
		}

		Individual entity = individualDao.getIndividualByURI(entityUri);
		if (entity == null) {
			throw new NullPointerException("No entity found for URI '"
					+ entityUri + "'.");
		}

		// Add the thumbnail file to the main image file.
		objectPropertyStatementDao
				.insertNewObjectPropertyStatement(new ObjectPropertyStatementImpl(
						mainInfo.getUri(), VitroVocabulary.FS_THUMBNAIL_IMAGE,
						thumbInfo.getUri()));

		// Add the main image file to the entity.
		entity.setMainImageUri(mainInfo.getUri());
		individualDao.updateIndividual(entity);

		log.debug("Set images on '" + entity.getURI() + "': main=" + mainInfo
				+ ", thumb=" + thumbInfo);
	}

	/**
	 * If this Individual has an image, remove it and the thumbnail. If the
	 * image file and/or the thumbnail file have no other references, delete
	 * them.
	 * 
	 * Note: after this operation, entity is stale.
	 */
	public void removeMainImage(Individual entity) {
		ImageInfo imageInfo = ImageInfo.instanceFromEntityUri(wadf, entity);
		if (imageInfo == null) {
			log.debug("No image to remove from '" + entity.getURI() + "'");
			return;
		}

		// Remove the main image from the entity.
		entity.setMainImageUri(null);
		individualDao.updateIndividual(entity);

		// Remove the thumbnail from the main image.
		ObjectPropertyStatement stmt = new ObjectPropertyStatementImpl(
				imageInfo.getMainImage().getUri(),
				VitroVocabulary.FS_THUMBNAIL_IMAGE, imageInfo.getThumbnail()
						.getUri());
		objectPropertyStatementDao.deleteObjectPropertyStatement(stmt);

		// If nobody else is using them, get rid of then.
		deleteIfNotReferenced(imageInfo.getMainImage());
		deleteIfNotReferenced(imageInfo.getThumbnail());
	}

	/**
	 * Create a bytestream individual in the model. The only property is the
	 * alias URL
	 */
	private Individual createByteStreamIndividual(String filename) {
		Individual byteStream = new IndividualImpl();
		byteStream.setVClassURI(VitroVocabulary.FS_BYTESTREAM_CLASS);

		String uri = null;
		try {
			uri = individualDao.insertNewIndividual(byteStream);
		} catch (InsertException e) {
			throw new IllegalStateException(
					"Failed to create the bytestream individual.", e);
		}

		dataPropertyStatementDao
				.insertNewDataPropertyStatement(new DataPropertyStatementImpl(
						uri, VitroVocabulary.FS_ALIAS_URL, FileServingHelper
								.getBytestreamAliasUrl(uri, filename, ctx)));

		return individualDao.getIndividualByURI(uri);
	}

	/**
	 * Create a file surrogate individual in the model. It has data properties
	 * for filename and mimeType. It also has a link to its bytestream
	 * Individual.
	 */
	private Individual createFileIndividual(String mimeType, String filename,
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
	 * If nobody is using this file any more, delete its bytestream from the
	 * file system, and delete both the surrogate and the bytestream from the
	 * model.
	 */
	private void deleteIfNotReferenced(FileInfo file) {
		if (!isFileReferenced(file.getUri())) {
			try {
				fileStorage.deleteFile(file.getBytestreamUri());
				individualDao.deleteIndividual(file.getBytestreamUri());
				individualDao.deleteIndividual(file.getUri());
			} catch (IOException e) {
				throw new IllegalStateException("Can't delete the file: '"
						+ file.getBytestreamUri(), e);
			}
		}
	}

	/**
	 * Are there any ObjectPropertyStatements in the model whose object is this
	 * file surrogate?
	 */
	private boolean isFileReferenced(String surrogateUri) {
		if (surrogateUri == null) {
			return false;
		}

		ObjectPropertyStatement opStmt = new ObjectPropertyStatementImpl(null,
				null, surrogateUri);
		List<ObjectPropertyStatement> stmts = objectPropertyStatementDao
				.getObjectPropertyStatements(opStmt);
		if (log.isDebugEnabled()) {
			log.debug(stmts.size() + " statements referencing '" + surrogateUri
					+ "'");
			for (ObjectPropertyStatement stmt : stmts) {
				log.debug("'" + stmt.getSubjectURI() + "' -- '"
						+ stmt.getPropertyURI() + "' -- '"
						+ stmt.getObjectURI() + "'");
			}
		}
		return !stmts.isEmpty();
	}

}
