/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package stubs.edu.cornell.mannlib.vitro.webapp.startup;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * Keep track of how many messages come in.
 */
public class StartupStatusStub extends StartupStatus {
	private static final Log log = LogFactory.getLog(StartupStatusStub.class);

	// ----------------------------------------------------------------------
	// Stub infrastructure
	// ----------------------------------------------------------------------

	private int infoCount = 0;
	private int warningCount = 0;
	private int fatalCount = 0;

	public StartupStatusStub(ServletContext ctx) {
		ctx.setAttribute(ATTRIBUTE_NAME, this);
	}

	public int getInfoCount() {
		return infoCount;
	}

	public int getWarningCount() {
		return warningCount;
	}

	public int getFatalCount() {
		return fatalCount;
	}

	// ----------------------------------------------------------------------
	// Stub methods
	// ----------------------------------------------------------------------

	@Override
	public void info(ServletContextListener listener, String message) {
		log.debug("INFO: " + message);
		infoCount++;
	}

	@Override
	public void info(ServletContextListener listener, String message,
			Throwable cause) {
		log.debug("INFO: " + message + " " + cause);
		infoCount++;
	}

	@Override
	public void warning(ServletContextListener listener, String message) {
		log.debug("WARNING: " + message);
		warningCount++;
	}

	@Override
	public void warning(ServletContextListener listener, String message,
			Throwable cause) {
		log.debug("WARNING: " + message + " " + cause);
		warningCount++;
	}

	@Override
	public void fatal(ServletContextListener listener, String message) {
		log.debug("FATAL: " + message);
		fatalCount++;
	}

	@Override
	public void fatal(ServletContextListener listener, String message,
			Throwable cause) {
		log.debug("FATAL: " + message + " " + cause);
		fatalCount++;
	}

	// ----------------------------------------------------------------------
	// Un-implemented methods
	// ----------------------------------------------------------------------

	@Override
	public void listenerNotExecuted(ServletContextListener listener) {
		throw new RuntimeException(
				"StartupStatusStub.listenerNotExecuted() not implemented.");
	}

	@Override
	public void listenerExecuted(ServletContextListener listener) {
		throw new RuntimeException(
				"StartupStatusStub.listenerExecuted() not implemented.");
	}

	@Override
	public boolean allClear() {
		throw new RuntimeException(
				"StartupStatusStub.allClear() not implemented.");
	}

	@Override
	public boolean isStartupAborted() {
		throw new RuntimeException(
				"StartupStatusStub.isStartupAborted() not implemented.");
	}

	@Override
	public List<StatusItem> getStatusItems() {
		throw new RuntimeException(
				"StartupStatusStub.getStatusItems() not implemented.");
	}

	@Override
	public List<StatusItem> getErrorItems() {
		throw new RuntimeException(
				"StartupStatusStub.getErrorItems() not implemented.");
	}

	@Override
	public List<StatusItem> getWarningItems() {
		throw new RuntimeException(
				"StartupStatusStub.getWarningItems() not implemented.");
	}

	@Override
	public List<StatusItem> getItemsForListener(ServletContextListener listener) {
		throw new RuntimeException(
				"StartupStatusStub.getItemsForListener() not implemented.");
	}

}
