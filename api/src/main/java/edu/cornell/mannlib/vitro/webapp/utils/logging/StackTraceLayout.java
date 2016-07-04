/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.logging;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This is a nasty Layout that prints a full stack trace for every logging
 * event.
 * 
 * Here is an example of how to use this to show full stack traces every time we
 * get a log message from org.apache.jena.riot.
 * 
 * <pre>
 * log4j.appender.RiotAppender=org.apache.log4j.RollingFileAppender 
 * log4j.appender.RiotAppender.File= $${catalina.home}/logs/${webapp.name}.riot.log
 * log4j.appender.RiotAppender.MaxFileSize=10MB 
 * log4j.appender.RiotAppender.MaxBackupIndex=10 
 * log4j.appender.RiotAppender.layout=edu.cornell.mannlib.vitro.webapp.utils.logging.StackTraceLayout 
 * log4j.logger.org.apache.jena.riot=INFO, RiotAppender
 * </pre>
 */
public class StackTraceLayout extends Layout {

	public StackTraceLayout() {
	}

	@Override
	public void activateOptions() {
	}

	/**
	 * Print the level, the message, and the full stack trace.
	 */
	@Override
	public String format(LoggingEvent event) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(event.getLevel().toString()).append(" - ")
				.append(event.getRenderedMessage()).append(LINE_SEP);
		for (StackTraceElement traceElement : Thread.currentThread()
				.getStackTrace()) {
			buffer.append("    ").append(traceElement.toString())
					.append(LINE_SEP);
		}
		return buffer.toString();
	}

	/**
	 * The StackTraceLayout does not handle the throwable contained within
	 * LoggingEvents. Thus, it returns {@code true}.
	 */
	@Override
	public boolean ignoresThrowable() {
		return true;
	}

}
