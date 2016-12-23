/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.utils.developer.DeveloperSettings;
import edu.cornell.mannlib.vitro.webapp.utils.developer.Key;
import edu.cornell.mannlib.vitro.webapp.utils.developer.loggers.StackTraceUtility;

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

	private static boolean isEnabled() {
		return log.isInfoEnabled()
				&& DeveloperSettings.getInstance().getBoolean(
						Key.LOGGING_RDF_ENABLE);
	}

	private final Object[] args;
	private final StackTraceUtility stackTrace;

	private boolean traceRequested;
	private Pattern queryStringRestriction;
	private Pattern callStackRestriction;

	private long startTime;

	public RDFServiceLogger(Object... args) {
		this.args = args;
		this.stackTrace = new StackTraceUtility(LoggingRDFService.class,
				isEnabled());

		try {
			getProperties();
			if (isEnabled()) {
				if (passesQueryRestriction()
						&& stackTrace
								.passesStackRestriction(callStackRestriction)) {
					this.startTime = System.currentTimeMillis();
				}
			}
		} catch (Exception e) {
			log.error("Failed to create instance", e);
		}
	}

	private void getProperties() {
		DeveloperSettings settings = DeveloperSettings.getInstance();
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

	@Override
	public void close() {
		try {
			if (startTime != 0L) {
				long endTime = System.currentTimeMillis();

				float elapsedSeconds = (endTime - startTime) / 1000.0F;
				String cleanArgs = Arrays.deepToString(args).replaceAll(
						"[\\n\\r\\t]+", " ");
				String formattedTrace = stackTrace.format(traceRequested);

				log.info(String.format("%8.3f %s %s %s", elapsedSeconds,
						stackTrace.getMethodName(), cleanArgs, formattedTrace));
			}
		} catch (Exception e) {
			log.error("Failed to write log record", e);
		}
	}

}
