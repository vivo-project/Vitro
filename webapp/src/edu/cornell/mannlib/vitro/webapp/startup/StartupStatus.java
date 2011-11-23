/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.startup;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Accumulates a list of messages from the StartupManager, and from the context
 * listeners that the run during startup.
 * 
 * This is thread-safe, with immutable items in the list and synchronized access
 * to the list.
 */
public class StartupStatus {
	private static final Log log = LogFactory.getLog(StartupStatus.class);

	private static final String ATTRIBUTE_NAME = "STARTUP_STATUS";

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

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

	// ----------------------------------------------------------------------
	// methods to set status - note that these write to the log also.
	// ----------------------------------------------------------------------

	private SynchronizedStatusItemList itemList = new SynchronizedStatusItemList();

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

	/** Say that a previous fatal error prevented this listener from running. */
	public void listenerNotExecuted(ServletContextListener listener) {
		addItem(StatusItem.Level.NOT_EXECUTED,
				listener,
				"Not executed - startup was aborted by a previous fatal error.",
				null);
	}

	/** Create a simple item for this listener if no other exists. */
	public void listenerExecuted(ServletContextListener listener) {
		List<StatusItem> itemsForThisListener = getItemsForListener(listener);
		if (itemsForThisListener.isEmpty()) {
			addItem(StatusItem.Level.INFO, listener, "Ran successfully.", null);
		}
	}

	private void addItem(StatusItem.Level level, ServletContextListener source,
			String message, Throwable cause) {
		StatusItem item = new StatusItem(level, source, message, cause);
		itemList.add(item);

		String logMessage = "From " + item.getShortSourceName() + ": "
				+ item.getMessage();
		if (item.getLevel() == StatusItem.Level.FATAL) {
			if (cause == null) {
				log.fatal(logMessage);
			} else {
				log.fatal(logMessage, cause);
			}
		} else if (item.getLevel() == StatusItem.Level.WARNING) {
			if (cause == null) {
				log.warn(logMessage);
			} else {
				log.warn(logMessage, cause);
			}
		} else {
			if (cause == null) {
				log.info(logMessage);
			} else {
				log.info(logMessage, cause);
			}
		}
	}

	// ----------------------------------------------------------------------
	// methods to query status
	// ----------------------------------------------------------------------

	public boolean allClear() {
		return getErrorItems().isEmpty() && getWarningItems().isEmpty();
	}

	public boolean isStartupAborted() {
		return !getErrorItems().isEmpty();
	}

	public List<StatusItem> getStatusItems() {
		return itemList.filterItems(StatusItemFilter.ALL_ITEMS_FILTER);
	}

	public List<StatusItem> getErrorItems() {
		return itemList.filterItems(StatusItemFilter.ERROR_ITEMS_FILTER);
	}

	public List<StatusItem> getWarningItems() {
		return itemList.filterItems(StatusItemFilter.WARNING_ITEMS_FILTER);
	}

	public List<StatusItem> getItemsForListener(ServletContextListener listener) {
		return itemList.filterItems(StatusItemFilter.listenerFilter(listener));
	}

	// ----------------------------------------------------------------------
	// helper classes
	// ----------------------------------------------------------------------

	/**
	 * An immutable item that can't throw an exception during construction and
	 * will always contain suitable, non-null values.
	 */
	public static class StatusItem {
		public enum Level {
			INFO, WARNING, FATAL, NOT_EXECUTED
		}

		private final Level level;
		private final String sourceName;
		private final String shortSourceName;
		private final String message;
		private final String cause;

		private boolean unexpectedArguments;

		public StatusItem(Level level, ServletContextListener source,
				String message, Throwable cause) {
			this.level = figureLevel(level);
			this.sourceName = figureSourceName(source);
			this.shortSourceName = figureShortSourceName(source);
			this.message = message;
			this.cause = figureCauseString(cause);

			if (unexpectedArguments) {
				log.error("Unexpected arguments to "
						+ StatusItem.class.getName() + ": level=" + level
						+ ", source=" + source + ", message=" + message
						+ ", cause=" + cause);
			}
		}

		/** Level should never be null: we have a problem. */
		private Level figureLevel(Level newLevel) {
			if (newLevel == null) {
				unexpectedArguments = true;
				return Level.FATAL;
			} else {
				return newLevel;
			}
		}

		private String figureSourceName(ServletContextListener source) {
			if (source == null) {
				unexpectedArguments = true;
				return "UNKNOWN SOURCE";
			} else {
				return source.getClass().getName();
			}
		}

		/**
		 * Don't just use getSimpleName(): on an inner class we'd like to see
		 * the parent also.
		 */
		private String figureShortSourceName(ServletContextListener source) {
			if (source == null) {
				unexpectedArguments = true;
				return "UNKNOWN_SOURCE";
			} else {
				String sourceClassName = source.getClass().getName();
				int lastPeriodHere = sourceClassName.lastIndexOf('.');
				return sourceClassName.substring(lastPeriodHere + 1);
			}
		}

		/** Cause may be null - that's not unexpected. */
		private String figureCauseString(Throwable newCause) {
			if (newCause == null) {
				return "";
			} else {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				newCause.printStackTrace(pw);
				return sw.toString();
			}
		}

		public Level getLevel() {
			return level;
		}

		public String getSourceName() {
			return sourceName;
		}

		public String getShortSourceName() {
			return shortSourceName;
		}

		public String getMessage() {
			return message;
		}

		public String getCause() {
			return cause;
		}

	}

	/**
	 * A filter class and some basic instances.
	 */
	private static abstract class StatusItemFilter {
		public abstract boolean accept(StatusItem item);

		public static final StatusItemFilter ALL_ITEMS_FILTER = new StatusItemFilter() {
			@Override
			public boolean accept(StatusItem item) {
				return true;
			}
		};

		public static final StatusItemFilter ERROR_ITEMS_FILTER = new StatusItemFilter() {
			@Override
			public boolean accept(StatusItem item) {
				return item.level == StatusItem.Level.FATAL;
			}
		};

		public static final StatusItemFilter WARNING_ITEMS_FILTER = new StatusItemFilter() {
			@Override
			public boolean accept(StatusItem item) {
				return item.level == StatusItem.Level.WARNING;
			}
		};

		public static StatusItemFilter listenerFilter(
				ServletContextListener listener) {
			final String listenerName = listener.getClass().getName();

			return new StatusItemFilter() {
				@Override
				public boolean accept(StatusItem item) {
					return item.getSourceName().equals(listenerName);
				}
			};

		}
	}

	/**
	 * A list, with synchronized methods for adding to it, and for getting a
	 * filtered subset of it.
	 */
	private class SynchronizedStatusItemList {
		private final List<StatusItem> list = new ArrayList<StatusItem>();

		public void add(StatusItem item) {
			synchronized (list) {
				list.add(item);
			}
		}

		public List<StatusItem> filterItems(StatusItemFilter filter) {
			List<StatusItem> filteredList = new ArrayList<StatusItem>();
			synchronized (list) {
				for (StatusItem item : list) {
					if (filter.accept(item)) {
						filteredList.add(item);
					}
				}
			}
			return filteredList;
		}
	}
}
