/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings.Keys;

/**
 * Writes the log message for the LoggingRDFService.
 * 
 * If not enabled, or if the logging level is insufficient, this does nothing.
 * 
 * If enabled, it checks for restrictions. If there is a restriction pattern
 * (regular expression), the a log message will only be printed if one of the
 * fully-qualified class names in the stack trace matches that pattern.
 * 
 * If everything passes muster, the constructor will record the time that the
 * instance was created.
 * 
 * When close() is called, if a start time was recorded, then a log record is
 * produced. This contains the elapsed time, the name of the method, and any
 * arguments passed to the constructor. It may also include a stack trace, if
 * requested.
 * 
 * The stack trace is abbreviated. It will reach into this class, and will not
 * extend past the first reference to the ApplicationFilterChain. It also omits
 * any Jena classes. Perhaps it should be abbreviated further?
 */
public class RDFServiceLogger implements AutoCloseable {
	private static final Log log = LogFactory.getLog(RDFServiceLogger.class);

	private final ServletContext ctx;
	private final Object[] args;

	private boolean isEnabled;
	private boolean traceRequested;
	private Pattern restriction;

	private String methodName;
	private List<StackTraceElement> trace = Collections.emptyList();

	private long startTime;

	public RDFServiceLogger(ServletContext ctx, Object... args) {
		this.ctx = ctx;
		this.args = args;

		getProperties();

		if (isEnabled && log.isInfoEnabled()) {
			loadStackTrace();
			if (passesRestrictions()) {
				this.startTime = System.currentTimeMillis();
			}
		}
	}

	private void getProperties() {
		DeveloperSettings settings = DeveloperSettings.getBean(ctx);
		isEnabled = settings.getBoolean(Keys.LOGGING_RDF_ENABLE);
		traceRequested = settings.getBoolean(Keys.LOGGING_RDF_STACK_TRACE);

		String restrictionString = settings
				.getString(Keys.LOGGING_RDF_RESTRICTION);
		if (StringUtils.isBlank(restrictionString)) {
			restriction = null;
		} else {
			try {
				restriction = Pattern.compile(restrictionString);
			} catch (Exception e) {
				log.error("Failed to compile the pattern for "
						+ Keys.LOGGING_RDF_RESTRICTION.key() + " = "
						+ restriction + " " + e);
				isEnabled = false;
			}
		}
	}

	private void loadStackTrace() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		List<StackTraceElement> list = new ArrayList<StackTraceElement>(
				Arrays.asList(stack));

		trimStackTraceAtBeginning(list);
		trimStackTraceAtEnd(list);
		removeJenaClassesFromStackTrace(list);

		if (list.isEmpty()) {
			this.methodName = "UNKNOWN";
		} else {
			this.methodName = list.get(0).getMethodName();
		}

		this.trace = list;
		log.debug("Stack array: " + Arrays.toString(stack));
		log.debug("Stack trace: " + this.trace);
	}

	private void trimStackTraceAtBeginning(List<StackTraceElement> list) {
		ListIterator<StackTraceElement> iter = list.listIterator();
		while (iter.hasNext()) {
			StackTraceElement ste = iter.next();
			if (ste.getClassName().equals(LoggingRDFService.class.getName())) {
				break;
			} else {
				iter.remove();
			}
		}
	}

	private void trimStackTraceAtEnd(List<StackTraceElement> list) {
		ListIterator<StackTraceElement> iter = list.listIterator();
		boolean trimming = false;
		while (iter.hasNext()) {
			StackTraceElement ste = iter.next();
			if (trimming) {
				iter.remove();
			} else if (ste.getClassName().contains("ApplicationFilterChain")) {
				trimming = true;
			}
		}
	}

	private void removeJenaClassesFromStackTrace(List<StackTraceElement> list) {
		ListIterator<StackTraceElement> iter = list.listIterator();
		while (iter.hasNext()) {
			StackTraceElement ste = iter.next();
			if (ste.getClassName().startsWith("com.hp.hpl.jena.")) {
				iter.remove();
			}
		}
	}

	private boolean passesRestrictions() {
		if (restriction == null) {
			return true;
		}
		for (StackTraceElement ste : trace) {
			if (restriction.matcher(ste.getClassName()).find()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void close() {
		if (startTime != 0L) {
			long endTime = System.currentTimeMillis();

			float elapsedSeconds = (endTime - startTime) / 1000.0F;
			String cleanArgs = Arrays.deepToString(args).replaceAll(
					"[\\n\\r\\t]+", " ");
			String formattedTrace = formatStackTrace();

			log.info(String.format("%8.3f %s %s %s", elapsedSeconds,
					methodName, cleanArgs, formattedTrace));
		}
	}

	private String formatStackTrace() {
		StringBuilder sb = new StringBuilder();

		if (traceRequested) {
			for (StackTraceElement ste : trace) {
				sb.append(String.format("\n   line %d4, %s",
						ste.getLineNumber(), ste.getClassName()));
			}
			sb.append("\n   ...");
		}

		return sb.toString();
	}

}
