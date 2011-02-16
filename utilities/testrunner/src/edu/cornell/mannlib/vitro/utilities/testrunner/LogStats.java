/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract any summary information from the log file.
 */
public class LogStats {
	public static LogStats EMPTY_LOG_STATS = new LogStats();

	private static final Pattern SUITE_NAME_PATTERN = Pattern
			.compile("Running suite (.*)");
	private static final Pattern ERROR_PATTERN = Pattern
			.compile("ERROR\\s+(.*)");
	private static final Pattern WARNING_PATTERN = Pattern
			.compile("WARN\\s+(.*)");

	/**
	 * Factory method.
	 */
	public static LogStats parse(File logFile) {
		return new LogStats(logFile);
	}

	private final List<String> suiteNames = new ArrayList<String>();
	private final List<String> errors = new ArrayList<String>();
	private final List<String> warnings = new ArrayList<String>();

	private LogStats() {
		// Nothing to initialize for empty instance.
	}

	private LogStats(File logFile) {

		BufferedReader reader = null;
		String line;
		try {
			reader = new BufferedReader(new FileReader(logFile));
			while (null != (line = reader.readLine())) {
				Matcher m;
				m = SUITE_NAME_PATTERN.matcher(line);
				if (m.matches()) {
					suiteNames.add(m.group(1));
				} else {
					m = ERROR_PATTERN.matcher(line);
					if (m.matches()) {
						errors.add(m.group(1));
					} else {
						m = WARNING_PATTERN.matcher(line);
						if (m.matches()) {
							warnings.add(m.group(1));
						}
					}
				}
			}

		} catch (IOException e) {
			// Can't give up - I need to create as much output as I can.
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public Collection<String> getErrors() {
		return Collections.unmodifiableCollection(errors);
	}

	public boolean hasWarnings() {
		return !warnings.isEmpty();
	}

	public Collection<String> getWarnings() {
		return Collections.unmodifiableCollection(warnings);
	}

}
