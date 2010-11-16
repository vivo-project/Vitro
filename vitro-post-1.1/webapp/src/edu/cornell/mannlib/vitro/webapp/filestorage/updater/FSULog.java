/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filestorage.updater;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * Writes the log file for the {@link FileStorageUpdater}. Be sure to call
 * {@link #close()} when finished.
 */
public class FSULog {
	private final SimpleDateFormat timeStamper = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	private final File logFile;
	private final PrintWriter writer;
	private boolean open;

	FSULog(File logDirectory, String prefix) throws IOException {
		this.logFile = generateTimestampedFilename(logDirectory, prefix);
		this.writer = new PrintWriter(this.logFile);
		open = true;
	}

	/**
	 * Create a filename for the log file that contains a timestamp, so if we
	 * run the process more than once, we will see multiple files.
	 */
	private File generateTimestampedFilename(File upgradeDirectory,
			String prefix) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-sss");
		String filename = prefix + "." + sdf.format(new Date()) + ".txt";
		return new File(upgradeDirectory, filename);
	}

	/**
	 * Where are we writing the output?
	 */
	String getFilename() {
		return this.logFile.getAbsolutePath();
	}

	/**
	 * Write this message.
	 */
	void log(String message) {
		writer.println(timeStamper.format(new Date()) + " INFO  " + message);
	}

	/**
	 * Write this message about this resource.
	 */
	void log(Resource resource, String message) {
		log(showResource(resource) + message);
	}

	/**
	 * Write this warning message.
	 */
	public void warn(String message) {
		writer.println(timeStamper.format(new Date()) + " WARN  " + message);
	}

	/**
	 * Write this warning message about this resource.
	 */
	public void warn(Resource resource, String message) {
		warn(showResource(resource) + message);
	}

	/**
	 * Write this error message.
	 */
	void error(String message) {
		writer.println(timeStamper.format(new Date()) + " ERROR " + message);
	}

	/**
	 * Write this exception as an error message..
	 */
	void error(Exception e) {
		error(e.toString());
		e.printStackTrace(writer);
	}

	/**
	 * Write an error message with this exception.
	 */
	public void error(String message, Exception e) {
		error(message);
		e.printStackTrace(writer);
	}

	/**
	 * Write an error message about this resource and with this exception.
	 */
	public void error(Resource resource, String message) {
		error(showResource(resource) + message);
	}

	/**
	 * Write an error message about this resource and with this exception.
	 */
	public void error(Resource resource, String message, Exception e) {
		error(showResource(resource) + message, e);
	}

	/**
	 * Write a section heading.
	 */
	public void section(String message) {
		log(">>>>>>>>>> ");
		log(">>>>>>>>>> " + message);
		log(">>>>>>>>>> ");
	}

	/**
	 * Close the writer, if not already closed.
	 */
	public void close() {
		if (open) {
			writer.close();
			open = false;
		}
	}

	/**
	 * Format the resource label and URI for output in a message.
	 */
	private String showResource(Resource resource) {
		return "On resource '" + getLabel(resource) + "' (" + getUri(resource)
				+ "), ";
	}

	/**
	 * Find the URI for this resource, if there is one.
	 */
	private String getUri(Resource resource) {
		if (resource != null) {
			String uri = resource.getURI();
			if (uri != null) {
				return uri;
			}
		}
		return "no URI";
	}

	/**
	 * Find the label for this resource, if there is one.
	 */
	private String getLabel(Resource resource) {
		if (resource != null) {
			Model model = resource.getModel();
			if (model != null) {
				Property prop = model.createProperty(VitroVocabulary.LABEL);
				Statement stmt = resource.getProperty(prop);
				if (stmt != null) {
					RDFNode node = stmt.getObject();
					if (node.isLiteral()) {
						return ((Literal) node).getString();
					}
				}
			}
		}
		return "no label";
	}

}
