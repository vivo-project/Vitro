/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaBaseDao;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorage;
import edu.cornell.mannlib.vitro.webapp.filestorage.backend.FileStorageSetup;
import edu.cornell.mannlib.vitro.webapp.filestorage.updater.FileStorageAliasAdder;
import edu.cornell.mannlib.vitro.webapp.filestorage.updater.FileStorageUpdater;

/**
 * Check that the conditions are met for updating uploaded files. If everything
 * is in place, call the two updaters.
 * 
 * The first updater converts from old-style (pre 1.0) to new-style (post 1.0)
 * file storage.
 * 
 * The second updater insures that all bytestreams store their own alias URLs
 * (post 1.1.1).
 */
public class UpdateUploadedFiles implements ServletContextListener {
	private static final Log log = LogFactory.getLog(UpdateUploadedFiles.class);

	/**
	 * Nothing to do on teardown.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		return;
	}

	/**
	 * Check that the ontology model, the old upload directory, and the file
	 * storage system are all valid. Then do the update.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			ServletContext ctx = sce.getServletContext();

			WebappDaoFactory wadf = (WebappDaoFactory) ctx
					.getAttribute("assertionsWebappDaoFactory");
			if (wadf == null) {
				throw new IllegalStateException("Webapp DAO Factory is null. "
						+ "The ServletContext does not contain an attribute "
						+ "for '" + "assertionsWebappDaoFactory" + "'. "
						+ "Does the log contain a previous exception from "
						+ "JenaDataSourceSetup? Is it possible that web.xml "
						+ "is not set up to run JenaDataSourceSetup before "
						+ "UpdateUploadedFiles?");
			}

			OntModel jenaOntModel = (OntModel) ctx
					.getAttribute(JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME);
			if (jenaOntModel == null) {
				throw new IllegalStateException("Ontology model is null. "
						+ "The ServletContext does not contain an attribute "
						+ "for '"
						+ JenaBaseDao.ASSERTIONS_ONT_MODEL_ATTRIBUTE_NAME
						+ "'. "
						+ "Does the log contain a previous exception from "
						+ "JenaDataSourceSetup? Is it possible that web.xml "
						+ "is not set up to run JenaDataSourceSetup before "
						+ "UpdateUploadedFiles?");
			}

			FileStorage fileStorage = (FileStorage) ctx
					.getAttribute(FileStorageSetup.ATTRIBUTE_NAME);
			if (fileStorage == null) {
				throw new IllegalStateException("File storage system is null. "
						+ "The ServletContext does not contain an attribute "
						+ "for '" + FileStorageSetup.ATTRIBUTE_NAME + "'. "
						+ "Does the log contain a previous exception from "
						+ "FileStorageSetup? Is it possible that web.xml is "
						+ "not set up to run FileStorageSetup before "
						+ "UpdateUploadedFiles?");
			}

			String uploadDirectoryName = ConfigurationProperties
					.getProperty(FileStorageSetup.PROPERTY_FILE_STORAGE_BASE_DIR);
			if (uploadDirectoryName == null) {
				throw new IllegalStateException("Upload directory name is null");
			}
			File uploadDirectory = new File(uploadDirectoryName);
			if (!uploadDirectory.exists()) {
				throw new IllegalStateException("Upload directory '"
						+ uploadDirectory.getAbsolutePath()
						+ "' does not exist.");
			}

			String vivoDefaultNamespace = ConfigurationProperties
					.getProperty(FileStorageSetup.PROPERTY_DEFAULT_NAMESPACE);
			if (vivoDefaultNamespace == null) {
				throw new IllegalStateException("Default namespace is null.");
			}

			String webappImagePath = ctx.getRealPath("images");
			File webappImageDirectory = (webappImagePath == null) ? null
					: new File(webappImagePath);

			/*
			 * Update from old-style storage to new-style storage.
			 */
			FileStorageUpdater fsu = new FileStorageUpdater(wadf, jenaOntModel,
					fileStorage, uploadDirectory, webappImageDirectory);
			fsu.update();

			/*
			 * Insure that every FileByteStream object has an alias URL.
			 */
			FileStorageAliasAdder fsaa = new FileStorageAliasAdder(
					jenaOntModel, uploadDirectory, vivoDefaultNamespace);
			fsaa.update();
		} catch (Exception e) {
			log.fatal("Unknown problem", e);
		}
	}
}
