/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.api.distribute;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

/**
 * A generalize mocking data distributor (although it could have legitimate
 * uses).
 * 
 * Provide a path to the file, relative to the Vitro home directory. Provide the
 * content type.
 */
public class FileDistributor extends DataDistributorBase {
	private static final Log log = LogFactory.getLog(FileDistributor.class);

	/** The path to the data file, relative to the Vitro home directory. */
	private String datapath;

	/** The content type to attach to the file. */
	private String contentType;

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#path", minOccurs = 1, maxOccurs = 1)
	public void setPath(String path) {
		datapath = path;
	}

	@Property(uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#contentType", minOccurs = 1, maxOccurs = 1)
	public void setContentType(String cType) {
		contentType = cType;
	}

	@Override
	public String getContentType() throws DataDistributorException {
		return contentType;
	}

	@Override
	public void writeOutput(OutputStream output)
			throws DataDistributorException {
		Path home = ApplicationUtils.instance().getHomeDirectory().getPath();
		Path datafile = home.resolve(datapath);
		log.debug("data file is at: " + datapath);
		try (InputStream input = Files.newInputStream(datafile)) {
			IOUtils.copy(input, output);
		} catch (IOException e) {
			throw new ActionFailedException(e);
		}
	}

	@Override
	public void close() throws DataDistributorException {
		// Nothing to close.
	}

}
