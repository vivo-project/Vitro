/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;

/**
 * Writes the log message for the LoggingRDFService.
 * 
 * If not enabled, or if the logging level is insufficient, this does nothing.
 * 
 * If enabled, it checks for restrictions. If there is a restriction on the call
 * stack (regular expression), then a log message will only be printed if the
 * pattern is found in the concatenated call stack (fully-qualified class names
 * and method names). If there is a restriction on the query string (regular
 * expression) then a log message will only be printed if the pattern is found
 * in the query string.
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

	private final Object[] args;

	private boolean isEnabled;
	private boolean traceRequested;
	private Pattern queryStringRestriction;
	private Pattern callStackRestriction;

	private String methodName;
	private List<StackTraceElement> trace = Collections.emptyList();

	private long startTime;

	public RDFServiceLogger(Object... args) {
		this.args = args;

		try {
			getProperties();
			if (isEnabled && log.isInfoEnabled()) {
				loadStackTrace();
				if (passesQueryRestriction() && passesStackRestriction()) {
					this.startTime = System.currentTimeMillis();
				}
			}
		} catch (Exception e) {
			log.error("Failed to create instance", e);
		}
	}

	private void getProperties() {
		DeveloperSettings settings = DeveloperSettings.getInstance();
		isEnabled = settings.getBoolean(Key.LOGGING_RDF_ENABLE);
		traceRequested = settings.getBoolean(Key.LOGGING_RDF_STACK_TRACE);
		queryStringRestriction = patternFromSettings(settings,
				Key.LOGGING_RDF_QUERY_RESTRICTION);
		callStackRestriction = patternFromSettings(settings,
				Key.LOGGING_RDF_STACK_RESTRICTION);
	}

	private Pattern patternFromSettings(DeveloperSettings settings, Key key) {
		String patternString = settings.getString(key);
		if (StringUtils.isBlank(patternString)) {
			return null;
		}
		try {
			return Pattern.compile(patternString);
		} catch (Exception e) {
			log.error("Failed to compile the pattern for " + key + " = "
					+ patternString + " " + e);
			return Pattern.compile("^_____NEVER MATCH_____$");
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

	private boolean passesQueryRestriction() {
		if (queryStringRestriction == null) {
			return true;
		}
		String q = assembleQueryString();
		return queryStringRestriction.matcher(q).find();
	}

	private String assembleQueryString() {
		List<String> stringArgs = new ArrayList<>();
		for (Object arg : args) {
			if (arg instanceof String) {
				stringArgs.add((String) arg);
			}
		}
		return StringUtils.join(stringArgs, " ");
	}

	private boolean passesStackRestriction() {
		if (callStackRestriction == null) {
			return true;
		}
		String q = assembleCallStackString();
		return callStackRestriction.matcher(q).find();
	}

	private String assembleCallStackString() {
		StringBuilder stack = new StringBuilder();
		for (StackTraceElement ste : trace) {
			stack.append(ste.getClassName()).append(" ")
					.append(ste.getMethodName()).append(" ");
		}
		return stack.deleteCharAt(stack.length() - 1).toString();
	}

	@Override
	public void close() {
		try {
			if (startTime != 0L) {
				long endTime = System.currentTimeMillis();

				float elapsedSeconds = (endTime - startTime) / 1000.0F;
				String cleanArgs = Arrays.deepToString(args).replaceAll(
						"[\\n\\r\\t]+", " ");
				String formattedTrace = formatStackTrace();

				log.info(String.format("%8.3f %s %s %s", elapsedSeconds,
						methodName, cleanArgs, formattedTrace));
			}
		} catch (Exception e) {
			log.error("Failed to write log record", e);
		}
	}

	private String formatStackTrace() {
		StringBuilder sb = new StringBuilder();

		if (traceRequested) {
			for (StackTraceElement ste : trace) {
				sb.append(String.format("\n   line %4d, %s",
						ste.getLineNumber(), ste.getClassName()));
			}
			sb.append("\n   ...");
		}

		return sb.toString();
	}

}
