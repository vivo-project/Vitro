/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Some static methods that might help with logging for debug purposes.
 */
public class LogUtils {
	// ----------------------------------------------------------------------
	// Public Static methods
	// ----------------------------------------------------------------------

	public static String deepFormatForLog(Log log, String level, Object o) {
		if (!isLevelEnabled(log, level)) {
			return "";
		}
		return new LogUtils(log).deepFormat(o);
	}

	public static String formatRequestProperties(Log log, String level,
			HttpServletRequest req) {
		if (!isLevelEnabled(log, level)) {
			return "";
		}
		return new LogUtils(log).requestProperties(req);
	}

	// ----------------------------------------------------------------------
	// The instance
	// ----------------------------------------------------------------------

	private String requestProperties(HttpServletRequest req) {
		@SuppressWarnings("unchecked")
		Map<String, String[]> map = req.getParameterMap();

		String s = req.getRequestURL().append('\n').toString();
		for (String name : new TreeSet<String>(map.keySet())) {
			s += "   " + name + " = " + Arrays.toString(map.get(name)) + '\n';
		}
		return s.trim();
	}

	private static boolean isLevelEnabled(Log log, String level) {
		if ("fatal".equalsIgnoreCase(level)) {
			return log.isFatalEnabled();
		} else if ("error".equalsIgnoreCase(level)) {
			return log.isErrorEnabled();
		} else if ("warn".equalsIgnoreCase(level)) {
			return log.isWarnEnabled();
		} else if ("info".equalsIgnoreCase(level)) {
			return log.isInfoEnabled();
		} else if ("debug".equalsIgnoreCase(level)) {
			return log.isDebugEnabled();
		} else {
			return log.isTraceEnabled();
		}
	}

	private final Log log;
	private final List<Object> dontFormatAgain = new ArrayList<Object>();

	private LogUtils(Log log) {
		this.log = log;
	}

	public String deepFormat(Object o) {
		if (o == null) {
			return "null";
		}
		if (o instanceof String) {
			return "\"" + o + "\"";
		}
		if (dontFormatAgain.contains(o)) {
			return "...";
		}

		dontFormatAgain.add(o);

		if (o instanceof Collection<?>) {
			return formatCollection((Collection<?>) o);
		}
		if (o instanceof Map<?, ?>) {
			return formatMap((Map<?, ?>) (o));
		}
		if (o.getClass().isArray()) {
			return formatArray(o);
		}
		return formatObject(o);
	}

	private String formatClass(Object o) {
		if (o == null) {
			return "";
		}
		return o.getClass().getName();
	}

	private String formatCollection(Collection<?> collection) {
		StringBuilder result = new StringBuilder(formatClass(collection));
		result.append(": ");

		result.append('{');
		for (Iterator<?> it = collection.iterator(); it.hasNext();) {
			result.append(deepFormat(it.next()));
			if (it.hasNext()) {
				result.append(", ");
			}
		}
		result.append('}');

		return result.toString();
	}

	private String formatMap(Map<?, ?> map) {
		StringBuilder result = new StringBuilder(formatClass(map));
		result.append(": ");

		result.append('{');
		for (Iterator<?> it = map.keySet().iterator(); it.hasNext();) {
			Object key = it.next();
			result.append(deepFormat(key));
			result.append('=');
			result.append(deepFormat(map.get(key)));
			if (it.hasNext()) {
				result.append(", ");
			}
		}
		result.append('}');

		return result.toString();
	}

	private String formatArray(Object o) {
		return formatClass(o) + ": " + Arrays.deepToString((Object[]) o);
	}

	private String formatObject(Object o) {
		String className = o.getClass().getName();
		String valueString = String.valueOf(o);
		if (valueString.contains(className)) {
			return valueString;
		} else {
			return formatClass(o) + ": " + valueString;
		}
	}

}
