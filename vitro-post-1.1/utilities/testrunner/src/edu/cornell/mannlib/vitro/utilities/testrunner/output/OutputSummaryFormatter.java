/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.utilities.testrunner.output;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import edu.cornell.mannlib.vitro.utilities.testrunner.FileHelper;
import edu.cornell.mannlib.vitro.utilities.testrunner.IgnoredTests.IgnoredTestInfo;
import edu.cornell.mannlib.vitro.utilities.testrunner.LogStats;
import edu.cornell.mannlib.vitro.utilities.testrunner.SeleniumRunnerParameters;
import edu.cornell.mannlib.vitro.utilities.testrunner.Status;
import edu.cornell.mannlib.vitro.utilities.testrunner.datamodel.DataModel;
import edu.cornell.mannlib.vitro.utilities.testrunner.datamodel.SuiteData;
import edu.cornell.mannlib.vitro.utilities.testrunner.datamodel.SuiteData.TestData;
import edu.cornell.mannlib.vitro.utilities.testrunner.output.OutputDataListener.ProcessOutput;

/**
 * Creates the summary HTML file.
 */
public class OutputSummaryFormatter {
	public static final String SUMMARY_HTML_FILENAME = "summary.html";
	public static final String SUMMARY_CSS_FILENAME = "summary.css";
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private final SeleniumRunnerParameters parms;

	private LogStats logStats;
	private DataModel dataModel;

	public OutputSummaryFormatter(SeleniumRunnerParameters parms) {
		this.parms = parms;
	}

	/**
	 * Create a summary HTML file from the info contained in this log file and
	 * these suite outputs.
	 */
	public void format(LogStats logStats, DataModel dataModel) {
		this.logStats = logStats;
		this.dataModel = dataModel;

		PrintWriter writer = null;
		try {
			copyCssFile();

			File outputFile = new File(parms.getOutputDirectory(),
					SUMMARY_HTML_FILENAME);
			writer = new PrintWriter(outputFile);

			writeHeader(writer);
			writeStatsSection(writer);
			writeErrorMessagesSection(writer);
			writeCondensedTable(writer);
			writeIgnoreSection(writer);
			writeSuiteErrorMessagesSection(writer);
			writeFooter(writer);
		} catch (IOException e) {
			// There is no appeal for any problems here. Just report them.
			e.printStackTrace();
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * Copy the CSS file into the output directory.
	 */
	private void copyCssFile() {
		InputStream cssStream = this.getClass().getResourceAsStream(
				SUMMARY_CSS_FILENAME);
		if (cssStream == null) {
			System.out.println("Couldn't find the CSS file: '"
					+ SUMMARY_CSS_FILENAME + "'");
		} else {
			File cssTarget = new File(parms.getOutputDirectory(),
					SUMMARY_CSS_FILENAME);
			try {
				FileHelper.copy(cssStream, cssTarget);
				cssStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeHeader(PrintWriter w) {
		Status runStatus = dataModel.getRunStatus();
		String statusString = (runStatus == Status.PENDING) ? "IN PROGRESS"
				: runStatus.toString();
		String startString = formatDateTime(dataModel.getStartTime());

		w.println("<html>");
		w.println("<head>");
		w.println("  <title>Summary of Acceptance Tests " + startString
				+ "</title>");
		w.println("  <link rel=\"stylesheet\" type=\"text/css\" "
				+ "href=\"summary.css\">");
		w.println("</head>");
		w.println("<body>");
		w.println();
		w.println("  <div class=\"heading\">");
		w.println("    Acceptance test results: " + startString);
		w.println("    <div class=\"" + runStatus.getHtmlClass()
				+ " one-word\">" + statusString + "</div>");
		w.println("  </div>");
	}

	private void writeStatsSection(PrintWriter w) {
		String passClass = dataModel.isAnyPasses() ? Status.OK.getHtmlClass()
				: "";
		String failClass = dataModel.isAnyFailures() ? Status.ERROR
				.getHtmlClass() : "";
		String ignoreClass = dataModel.isAnyIgnores() ? Status.IGNORED
				.getHtmlClass() : "";

		String start = formatDateTime(dataModel.getStartTime());
		String end = formatDateTime(dataModel.getEndTime());
		String elapsed = formatElapsedTime(dataModel.getElapsedTime());

		w.println("  <div class=\"section\">Summary</div>");
		w.println();
		w.println("  <table class=\"summary\" cellspacing=\"0\">");
		w.println("    <tr>");
		w.println("      <td>");
		w.println("        <table cellspacing=\"0\">");
		w.println("  	      <tr><td>Start time:</td><td>" + start
				+ "</td></tr>");
		w.println("  	      <tr><td>End time:</td><td>" + end + "</td></tr>");
		w.println("  	      <tr><td>Elapsed time</td><td>" + elapsed
				+ "</td></tr>");
		w.println("  	    </table>");
		w.println("      </td>");
		w.println("      <td>");
		w.println("        <table class=\"tallys\" cellspacing=\"0\">");
		w.println("          <tr><th>&nbsp;</th><th>Suites</th><th>Tests</th>");
		w.println("          <tr class=\"" + passClass
				+ "\"><td>Passed</td><td>" + dataModel.getPassingSuiteCount()
				+ "</td><td>" + dataModel.getPassingTestCount() + "</td>");
		w.println("          <tr class=\"" + failClass
				+ "\"><td>Failed</td><td>" + dataModel.getFailingSuiteCount()
				+ "</td><td>" + dataModel.getFailingTestCount() + "</td>");
		w.println("          <tr class=\"" + ignoreClass
				+ "\"><td>Ignored</td><td>" + dataModel.getIgnoredSuiteCount()
				+ "</td><td>" + dataModel.getIgnoredTestCount() + "</td>");
		if (dataModel.isAnyPending()) {
			w.println("          <tr class=\"" + Status.PENDING.getHtmlClass()
					+ "\"><td>Pending</td><td>"
					+ dataModel.getPendingSuiteCount() + "</td><td>"
					+ dataModel.getPendingTestCount() + "</td>");
		}
		w.println("          <tr><td class=\"total\">Total</td><td>"
				+ dataModel.getTotalSuiteCount() + "</td><td>"
				+ dataModel.getTotalTestCount() + "</td>");
		w.println("  	    </table>");
		w.println("      </td>");
		w.println("    </tr>");
		w.println("  </table>");
		w.println();
	}

	private void writeErrorMessagesSection(PrintWriter w) {
		String errorClass = Status.ERROR.getHtmlClass();

		w.println("  <div class=section>Errors and warnings</div>");
		w.println();
		w.println("  <table cellspacing=\"0\">");

		if ((!logStats.hasErrors()) && (!logStats.hasWarnings())) {
			w.println("      <tr><td colspan=\"2\">No errors or warnings</td></tr>");
		} else {
			for (String e : logStats.getErrors()) {
				w.println("      <tr class=\"" + errorClass
						+ "\"><td>ERROR</td><td>" + e + "</td></tr>");
			}
		}
		w.println("    </table>");
		w.println();
	}

	private void writeCondensedTable(PrintWriter w) {
		w.println("  <div class=section>Condensed List</div>");
		w.println();
		w.println("  <table class=\"condensed\" cellspacing=\"0\">");
		for (SuiteData s : dataModel.getAllSuites()) {
			String sReason = "";
			if (s.getStatus() == Status.IGNORED) {
				sReason = dataModel.getReasonForIgnoring(s.getName(), "*");
			} else if (s.getFailureMessages() != null) {
				sReason = s.getFailureMessages().getErrout();
			} else if (s.getStatus() == Status.PENDING) {
				sReason = Status.PENDING.toString();
			}

			w.println("    <tr>");
			w.println("      <td class=\"" + s.getStatus().getHtmlClass()
					+ "\">");
			w.println("        <div class=\"suite\">" + outputLink(s)
					+ "</div>");
			if (!sReason.isEmpty()) {
				// The entire class is either failed or pending or ignored.
				w.println("        <div class=\"reason\">" + sReason + "</div>");
			} else {
				// Show the individual tests.
				for (TestData t : s.getTestMap().values()) {
					String tClass = t.getStatus().getHtmlClass();
					String tReason = dataModel.getReasonForIgnoring(
							s.getName(), t.getTestName());

					w.println("        <div class=\"test " + tClass + "\">");
					w.println("          " + outputLink(t));
					if (!tReason.isEmpty()) {
						w.println("          <div class=\"tReason\">" + tReason
								+ "</div>");
					}
					w.println("        </div>");
				}
			}
			w.println("      </td>");
			w.println("    </tr>");
		}
		w.println("  </table>");
		w.println();
	}

	private void writeSuiteErrorMessagesSection(PrintWriter w) {
		Map<String, SuiteData> failedSuiteMap = dataModel
				.getSuitesWithFailureMessages();
		if (failedSuiteMap.isEmpty()) {
			return;
		}

		w.println("  <div class=section>All tests</div>");
		w.println();
		for (SuiteData s : failedSuiteMap.values()) {
			ProcessOutput output = s.getFailureMessages();

			w.println("  <a name=\"" + SuiteData.failureMessageAnchor(s)
					+ "\">");
			w.println("  <table cellspacing=\"0\">");
			w.println("    <tr><th>Standard Output</th></tr>\n");
			w.println("    <tr><td><pre>" + output.getStdout()
					+ "</pre></td></tr>\n");
			w.println("  </table>");
			w.println("<br/>&nbsp;<br/>");

			w.println("  <table cellspacing=\"0\">");
			w.println("    <tr><th>Error Output</th></tr>\n");
			w.println("    <tr><td><pre>" + output.getErrout()
					+ "</pre></td></tr>\n");
			w.println("  </table>");
			w.println("<br/>&nbsp;<br/>");
			w.println();
		}
	}

	private void writeIgnoreSection(PrintWriter w) {
		String warnClass = Status.IGNORED.getHtmlClass();
		Collection<IgnoredTestInfo> ignoredTests = dataModel
				.getIgnoredTestInfo();

		w.println("  <div class=section>Ignored</div>");
		w.println();
		w.println("  <table class=\"ignored\" cellspacing=\"0\">");
		w.println("    <tr><th>Suite name</th><th>Test name</th>"
				+ "<th>Reason for ignoring</th></tr>\n");
		if (ignoredTests.isEmpty()) {
			w.println("    <tr><td colspan=\"3\">No tests ignored.</td>"
					+ "</tr>");
		} else {
			for (IgnoredTestInfo info : ignoredTests) {
				String suiteName = info.suiteName;
				String testName = info.testName;
				String reason = dataModel.getReasonForIgnoring(suiteName,
						testName);

				w.println("    <tr class=\"" + warnClass + "\">");
				w.println("      <td>" + suiteName + "</td>");
				w.println("      <td>" + testName + "</td>");
				w.println("      <td>" + reason + "</td>");
				w.println("    </tr>");
			}
		}
		w.println("  </table>");
		w.println();
	}

	private void writeFooter(PrintWriter w) {
		w.println("  <div class=section>Log</div>");
		w.println("  <pre class=\"log\">");

		Reader reader = null;
		try {
			reader = new FileReader(parms.getLogFile());
			char[] buffer = new char[4096];
			int howMany;
			while (-1 != (howMany = reader.read(buffer))) {
				w.write(buffer, 0, howMany);
			}
		} catch (IOException e) {
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

		w.println("  </pre>");
		w.println("</body>");
		w.println("</html>");
	}

	private String formatElapsedTime(long elapsed) {
		if (elapsed == 0) {
			return "---";
		}

		long elapsedSeconds = elapsed / 1000L;
		long seconds = elapsedSeconds % 60L;
		long elapsedMinutes = elapsedSeconds / 60L;
		long minutes = elapsedMinutes % 60L;
		long hours = elapsedMinutes / 60L;

		String elapsedTime = "";
		if (hours > 0) {
			elapsedTime += hours + "h ";
		}
		if (minutes > 0 || hours > 0) {
			elapsedTime += minutes + "m ";
		}
		elapsedTime += seconds + "s";

		return elapsedTime;
	}

	private String formatDateTime(long dateTime) {
		if (dateTime == 0) {
			return "---";
		}

		return dateFormat.format(new Date(dateTime));
	}

	private String outputLink(SuiteData s) {
		if (s.getOutputLink() == null) {
			return s.getName();
		} else {
			return "<a href=\"" + s.getOutputLink() + "\">" + s.getName()
					+ "</a>";
		}
	}

	private String outputLink(TestData t) {
		if (t.getOutputLink() == null) {
			return t.getTestName();
		} else {
			return "<a href=\"" + t.getOutputLink() + "\">" + t.getTestName()
					+ "</a>";
		}
	}
}
