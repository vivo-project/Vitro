/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.startup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

/**
 * TODO
 */
public class StartupStatus {
	private static final String ATTRIBUTE_NAME = "STARTUP_STATUS";

	public static StartupStatus getBean(ServletContext ctx) {
		StartupStatus ss;

		Object o = ctx.getAttribute(ATTRIBUTE_NAME);
		if (o instanceof StartupStatus) {
			ss = (StartupStatus) o;
		} else {
			ss = new StartupStatus();
			ctx.setAttribute(ATTRIBUTE_NAME, ss);
		}

		return ss;
	}

	private List<StatusItem> itemList = new ArrayList<StatusItem>();

	public void info(ServletContextListener listener, String message) {
		addItem(StatusItem.Level.INFO, listener, message, null);
	}

	public void info(ServletContextListener listener, String message,
			Throwable cause) {
		addItem(StatusItem.Level.INFO, listener, message, cause);
	}

	public void warning(ServletContextListener listener, String message) {
		addItem(StatusItem.Level.WARNING, listener, message, null);
	}

	public void warning(ServletContextListener listener, String message,
			Throwable cause) {
		addItem(StatusItem.Level.WARNING, listener, message, cause);
	}

	public void fatal(ServletContextListener listener, String message) {
		addItem(StatusItem.Level.FATAL, listener, message, null);
	}

	public void fatal(ServletContextListener listener, String message,
			Throwable cause) {
		addItem(StatusItem.Level.FATAL, listener, message, cause);
	}

	public void listenerNotExecuted(ServletContextListener listener) {
		addItem(StatusItem.Level.NOT_EXECUTED, listener, "Not executed", null);
	}

	public boolean isStartupAborted() {
		for (StatusItem item : itemList) {
			if (item.level == StatusItem.Level.FATAL) {
				return true;
			}
		}
		return false;
	}

	public List<StatusItem> getStatusItems() {
		return Collections.unmodifiableList(itemList);
	}

	private void addItem(StatusItem.Level level, ServletContextListener source,
			String message, Throwable cause) {
		itemList.add(new StatusItem(level, source, message, cause));
	}

	public static class StatusItem {
		public enum Level {
			INFO, WARNING, FATAL, NOT_EXECUTED
		}

		final Level level;
		final String sourceName;
		final String message;
		final Throwable cause;

		public StatusItem(Level level, ServletContextListener source,
				String message, Throwable cause) {
			this.level = level;
			this.sourceName = source.getClass().getName();
			this.message = message;
			this.cause = cause;
		}

	}

}
