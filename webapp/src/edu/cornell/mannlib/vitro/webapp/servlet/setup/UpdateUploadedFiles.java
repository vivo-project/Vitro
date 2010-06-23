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
import edu.cornell.mannlib.vitro.webapp.filestorage.updater.FileStorageUpdater;

/**
 * TODO
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
					.getAttribute("webappDaoFactory");
			if (wadf == null) {
				throw new IllegalStateException("Webapp DAO Factory is null");
			}

			OntModel jenaOntModel = (OntModel) ctx
					.getAttribute(JenaBaseDao.JENA_ONT_MODEL_ATTRIBUTE_NAME);
			if (jenaOntModel == null) {
				throw new IllegalStateException("Ontology model is null");
			}

			FileStorage fileStorage = (FileStorage) ctx
					.getAttribute(FileStorageSetup.ATTRIBUTE_NAME);
			if (fileStorage == null) {
				throw new IllegalStateException("File storage system is null");
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

			
			FileStorageUpdater fsu = new FileStorageUpdater(wadf, jenaOntModel,
					fileStorage, uploadDirectory);
			fsu.update();
		} catch (Exception e) {
			log.error("Unknown problem", e);
			throw new RuntimeException(e);
		}
	}
}
