/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.model;

import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.filestorage.FileServingHelper;

/**
 * An immutable packet of information about an uploaded file, with a builder
 * class to permit incremental construction.
 */
public class FileInfo {
	private static final Log log = LogFactory.getLog(FileInfo.class);

	// ----------------------------------------------------------------------
	// static Factory methods.
	// ----------------------------------------------------------------------

	/**
	 * If this request URL represents a BytestreamAliasURL, find the bytestream
	 * URI, find the surrogate, and get the info. Otherwise, return null.
	 */
	public static FileInfo instanceFromAliasUrl(
			WebappDaoFactory webappDaoFactory, String path, ServletContext ctx) {
		String bytestreamUri = FileServingHelper.getBytestreamUri(path, ctx);
		if (bytestreamUri == null) {
			return null;
		}
		return instanceFromBytestreamUri(webappDaoFactory, bytestreamUri);
	}

	/**
	 * If this URI represents a file bytestream, find its surrogate and get its
	 * info. Otherwise, return null.
	 */
	public static FileInfo instanceFromBytestreamUri(
			WebappDaoFactory webappDaoFactory, String bytestreamUri) {
		IndividualDao individualDao = webappDaoFactory.getIndividualDao();
		Individual entity = individualDao.getIndividualByURI(bytestreamUri);
		if (!isFileBytestream(entity)) {
			return null;
		}

		ObjectPropertyStatementDao objectPropertyStatementDao = webappDaoFactory
				.getObjectPropertyStatementDao();
		ObjectPropertyStatement opStmt = new ObjectPropertyStatementImpl(null,
				VitroVocabulary.FS_DOWNLOAD_LOCATION, entity.getURI());
		List<ObjectPropertyStatement> stmts = objectPropertyStatementDao
				.getObjectPropertyStatements(opStmt);

		if (stmts.size() > 1) {
			String uris = "";
			for (ObjectPropertyStatement stmt : stmts) {
				uris += "'" + stmt.getSubjectURI() + "' ";
			}
			log.warn("Found " + stmts.size() + " Individuals that claim '"
					+ entity.getURI() + "' as its bytestream:" + uris);
		}
		if (stmts.isEmpty()) {
			log.warn("No individual claims '" + entity.getURI()
					+ "' as its bytestream.");
			return null;
		}

		String surrogateUri = stmts.get(0).getSubjectURI();
		return instanceFromSurrogateUri(webappDaoFactory, surrogateUri);
	}

	/**
	 * If this URI represents a file surrogate, get its info. Otherwise, return
	 * null.
	 */
	public static FileInfo instanceFromSurrogateUri(
			WebappDaoFactory webappDaoFactory, String uri) {
		IndividualDao individualDao = webappDaoFactory.getIndividualDao();
		Individual surrogate = individualDao.getIndividualByURI(uri);
		if (!isFileSurrogate(surrogate)) {
			return null;
		}

		String filename = surrogate.getDataValue(VitroVocabulary.FS_FILENAME);
		if (filename == null) {
			log.error("File had no filename: '" + uri + "'");
		} else {
			log.debug("Filename for '" + uri + "' was '" + filename + "'");
		}

		String mimeType = surrogate.getDataValue(VitroVocabulary.FS_MIME_TYPE);
		if (mimeType == null) {
			log.error("File had no mimeType: '" + uri + "'");
		} else {
			log.debug("mimeType for '" + uri + "' was '" + mimeType + "'");
		}

		Individual byteStream = surrogate
				.getRelatedIndividual(VitroVocabulary.FS_DOWNLOAD_LOCATION);
		if (byteStream == null) {
			log.error("File surrogate '" + uri
					+ "' had no associated bytestream.");
		}

		String bytestreamUri = findBytestreamUri(byteStream, uri);
		String bytestreamAliasUrl = findBytestreamAliasUrl(byteStream, uri);

		return new FileInfo.Builder().setUri(uri).setFilename(filename)
				.setMimeType(mimeType).setBytestreamUri(bytestreamUri)
				.setBytestreamAliasUrl(bytestreamAliasUrl).build();
	}

	/**
	 * Is this a FileByteStream individual?
	 */
	private static boolean isFileBytestream(Individual entity) {
		if (entity == null) {
			return false;
		}
		if (entity.isVClass(VitroVocabulary.FS_BYTESTREAM_CLASS)) {
			log.debug("Entity '" + entity.getURI() + "' is a bytestream");
			return true;
		}
		log.debug("Entity '" + entity.getURI() + "' is not a bytestream");
		return false;
	}

	/**
	 * Is this a File individual?
	 */
	private static boolean isFileSurrogate(Individual entity) {
		if (entity == null) {
			return false;
		}
		if (entity.isVClass(VitroVocabulary.FS_FILE_CLASS)) {
			log.debug("Entity '" + entity.getURI() + "' is a file surrogate");
			return true;
		}
		log.debug("Entity '" + entity.getURI() + "' is not a file surrogate");
		return false;
	}

	/**
	 * Get the URI of the bytestream, or null if there is none.
	 */
	private static String findBytestreamUri(Individual byteStream,
			String surrogateUri) {
		if (byteStream == null) {
			return null;
		}

		String bytestreamUri = byteStream.getURI();
		log.debug("File surrogate'" + surrogateUri
				+ "' had associated bytestream: '" + byteStream.getURI() + "'");
		return bytestreamUri;
	}

	/**
	 * Get the alias URL from the bytestream, or null if there is none.
	 */
	private static String findBytestreamAliasUrl(Individual byteStream,
			String surrogateUri) {
		if (byteStream == null) {
			return null;
		}

		String aliasUrl = byteStream.getDataValue(VitroVocabulary.FS_ALIAS_URL);
		if (aliasUrl == null) {
			log.error("File had no aliasUrl: '" + surrogateUri + "'");
		} else {
			log.debug("aliasUrl for '" + surrogateUri + "' was '" + aliasUrl
					+ "'");
		}
		return aliasUrl;
	}

	// ----------------------------------------------------------------------
	// The instance variables and methods.
	// ----------------------------------------------------------------------

	private final String uri;
	private final String filename;
	private final String mimeType;
	private final String bytestreamUri;
	private final String bytestreamAliasUrl;

	private FileInfo(Builder builder) {
		this.uri = builder.uri;
		this.filename = builder.filename;
		this.mimeType = builder.mimeType;
		this.bytestreamUri = builder.bytestreamUri;
		this.bytestreamAliasUrl = builder.bytestreamAliasUrl;
	}

	public String getUri() {
		return uri;
	}

	public String getFilename() {
		return filename;
	}

	public String getMimeType() {
		return mimeType;
	}

	public String getBytestreamUri() {
		return bytestreamUri;
	}

	public String getBytestreamAliasUrl() {
		return bytestreamAliasUrl;
	}

	@Override
	public String toString() {
		return "FileInfo[uri=" + uri + ", filename=" + filename + ", mimeType="
				+ mimeType + ", bytestreamUri=" + bytestreamUri + ", aliasUrl="
				+ bytestreamAliasUrl + "]";
	}

	// ----------------------------------------------------------------------
	// The builder.
	// ----------------------------------------------------------------------

	/**
	 * A builder class allows us to supply the values one at a time, and then
	 * freeze them into an immutable object.
	 */
	public static class Builder {
		private String uri;
		private String filename;
		private String mimeType;
		private String bytestreamUri;
		private String bytestreamAliasUrl;

		public Builder setUri(String uri) {
			this.uri = uri;
			return this;
		}

		public Builder setFilename(String filename) {
			this.filename = filename;
			return this;
		}

		public Builder setMimeType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}

		public Builder setBytestreamUri(String bytestreamUri) {
			this.bytestreamUri = bytestreamUri;
			return this;
		}

		public Builder setBytestreamAliasUrl(String bytestreamAliasUrl) {
			this.bytestreamAliasUrl = bytestreamAliasUrl;
			return this;
		}

		public FileInfo build() {
			return new FileInfo(this);
		}
	}

}
