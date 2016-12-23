/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.developer.loggers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Records and process the stack trace so a developer's logger can (1) test
 * against it, and (2) display it.
 * 
 * If the "enabled" flag is not set, no processing is done, and the stack trace
 * is empty. That way, there is essentially no overhead in creating a disabled
 * instance.
 */
public class StackTraceUtility {
	private static final Log log = LogFactory.getLog(StackTraceUtility.class);

	private final Class<?> lowestClassInStackTrace;
	private final List<StackTraceElement> stackTrace;
	private final String methodName;

	public StackTraceUtility(Class<?> lowestClassInStackTrace, boolean enabled) {
		this.lowestClassInStackTrace = lowestClassInStackTrace;

		this.stackTrace = enabled ? loadStackTrace() : Collections
				.<StackTraceElement> emptyList();

		this.methodName = (this.stackTrace.isEmpty()) ? "UNKNOWN"
				: this.stackTrace.get(0).getMethodName();

	}

	private List<StackTraceElement> loadStackTrace() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		List<StackTraceElement> list = new ArrayList<StackTraceElement>(
				Arrays.asList(stack));

		trimStackTraceAtBeginning(list);
		trimStackTraceAtEnd(list);
		removeJenaClassesFromStackTrace(list);

		log.debug("Stack array: " + Arrays.toString(stack));
		log.debug("Stack trace: " + list);
		return Collections.unmodifiableList(list);
	}

	private void trimStackTraceAtBeginning(List<StackTraceElement> list) {
		ListIterator<StackTraceElement> iter = list.listIterator();
		while (iter.hasNext()) {
			StackTraceElement ste = iter.next();
			if (ste.getClassName().equals(lowestClassInStackTrace.getName())) {
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
			if (ste.getClassName().startsWith("org.apache.jena.")) {
				iter.remove();
			}
		}
	}

	public boolean passesStackRestriction(String restriction) {
		if (StringUtils.isEmpty(restriction)) {
			return true;
		} else {
			try {
				return passesStackRestriction(Pattern.compile(restriction));
			} catch (Exception e) {
				log.warn("Failed when testing stack restriction: '"
						+ restriction + "'");
				return true;
			}
		}

	}

	public boolean passesStackRestriction(Pattern restriction) {
		if (restriction == null) {
			return true;
		}
		String q = assembleCallStackString();
		return restriction.matcher(q).find();
	}

	private String assembleCallStackString() {
		StringBuilder stack = new StringBuilder();
		for (StackTraceElement ste : stackTrace) {
			stack.append(ste.getClassName()).append(" ")
					.append(ste.getMethodName()).append(" ");
		}
		return stack.deleteCharAt(stack.length() - 1).toString();
	}

	public String format(boolean requested) {
		StringBuilder sb = new StringBuilder();
		if (requested) {
			for (StackTraceElement ste : stackTrace) {
				sb.append(String.format("   %s.%s(%s:%d) \n",
						ste.getClassName(), ste.getMethodName(),
						ste.getFileName(), ste.getLineNumber()));
			}
			sb.append("   ...\n");
		}
		return sb.toString();
	}

	public String getMethodName() {
		return this.methodName;
	}

}
