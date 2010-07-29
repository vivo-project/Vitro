/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.tidy.Tidy;

public class MakeTidy {
	private static final Log log = LogFactory.getLog(MakeTidy.class);
	private static PrintWriter outFile = new PrintWriter(new LoggingWriter(log));

	public String process(String value) {
		Tidy tidy = new Tidy(); // obtain a new Tidy instance

	    // set desired config options using tidy setters: see http://jtidy.sourceforge.net/apidocs/index.html
	    tidy.setAsciiChars(true);                // convert quotes and dashes to nearest ASCII character
	    tidy.setDropEmptyParas(true);            // discard empty p elements
        tidy.setDropFontTags(true);              // discard presentation tags
	    tidy.setDropProprietaryAttributes(true); // discard proprietary attributes
        tidy.setForceOutput(true);               // output document even if errors were found
	    tidy.setLogicalEmphasis(true);           // replace i by em and b by strong
	    tidy.setMakeBare(true);                  // remove Microsoft cruft
	    tidy.setMakeClean(true);                 // remove presentational clutter
        tidy.setPrintBodyOnly(true);             // output BODY content only
        tidy.setShowWarnings(true);              // show warnings
        tidy.setTidyMark(true);                  // add meta element indicating tidied doc
        tidy.setTrimEmptyElements(true);         // trim empty elements
	    tidy.setWord2000(true);                  // draconian cleaning for Word 2000
        tidy.setXHTML(true);                     // output extensible HTML
	    
		tidy.setErrout(outFile);
		tidy.setShowErrors(Integer.MAX_VALUE);
		outFile.println("\nInput:\n" + value + "\n");

		StringWriter sw = new StringWriter();
		/* Node rootNode = */tidy.parse(new StringReader(value), sw);
		String outputStr = sw.toString();
		log.debug("\nTidied Output:\n" + outputStr + "\n");
		return outputStr;
	}

	/**
	 * A {@link Writer} that sends its output to a log file, at INFO level.
	 */
	private static class LoggingWriter extends Writer {
		private final Log logger;
		private String buffer;

		LoggingWriter(Log logger) {
			this.logger = logger;
			this.buffer = "";
		}

		/**
		 * Append the new stuff to the buffer, and write any complete lines to
		 * the log.
		 */
		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			buffer += new String(cbuf, off, len);
			dumpLines();
		}

		/**
		 * If the buffer isn't empty, clean it out by completing the line and
		 * dumping it to the log.
		 */
		@Override
		public void close() throws IOException {
			if (buffer.length() > 0) {
				buffer += "\n";
				dumpLines();
			}
		}

		/**
		 * We don't want to log a partial line, so {@link #flush()} does
		 * nothing.
		 */
		@Override
		public void flush() throws IOException {
		}

		/**
		 * If there are any complete lines in the buffer, write them to the log
		 * and remove them from the buffer.
		 */
		private void dumpLines() {
			while (true) {
				int lineEnd = buffer.indexOf("\n");
				if (lineEnd == -1) {
					return;
				} else {
					logger.info(buffer.substring(0, lineEnd).trim());
					buffer = buffer.substring(lineEnd + 1);
				}
			}
		}
	}
}
