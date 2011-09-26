/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.config;

import static edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.DUMMY_BEAN;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.RevisionInfoBean.LevelRevisionInfo;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * <pre>
 * Read the revision information, and store it in the servlet context.
 * 
 * - The revision information is in a file in the classpath. 
 * - The name of the file is in RESOURCE_PATH, below.
 * - The first line is the build date, with a format as in DATE_FORMAT, below.
 * - Each additional non-blank line holds revision info for one application level:
 *    - level info is from inner (vitro) to outer (top-level product).
 *    - level info appears as product name, release name and revision level, 
 *        delimited by " ~ ". 
 *    - additional white space before and after info values is ignored.
 * 
 * Example file:
 *    2010-11-14 23:58:00
 *    vitroCore ~ Release 1.1 ~ 6604
 *    nihvivo ~ Release 1.1 ~ 1116
 * </pre>
 */
public class RevisionInfoSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(RevisionInfoSetup.class);

	private static final Pattern LEVEL_INFO_PATTERN = Pattern
			.compile("(.+) ~ (.+) ~ (.+)");

	static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	static final String RESOURCE_PATH = "/WEB-INF/resources/revisionInfo.txt";

	/**
	 * On startup, read the revision info from the resource file in the
	 * classpath.
	 * 
	 * If we can't find the file, or can't parse it, store an empty bean.
	 * 
	 * Don't allow any Exceptions to percolate up past this point.
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext context = sce.getServletContext();
		RevisionInfoBean bean;
		try {
			List<String> lines = readRevisionInfo(context);
			bean = parseRevisionInformation(lines);
		} catch (Exception e) {
			StartupStatus.getBean(context).warning(this,
					"Failed to load the revision info", e);
			bean = DUMMY_BEAN;
		}

		RevisionInfoBean.setBean(sce.getServletContext(), bean);
	}

	private List<String> readRevisionInfo(ServletContext context)
			throws IOException {
		BufferedReader reader = null;
		try {
			reader = openRevisionInfoReader(context);
			return readSignificantLines(reader);
		} finally {
			closeReader(reader);
		}
	}

	private BufferedReader openRevisionInfoReader(ServletContext context)
			throws FileNotFoundException {
		InputStream stream = context.getResourceAsStream(RESOURCE_PATH);
		if (stream == null) {
			throw new FileNotFoundException(
					"Can't find a resource in the webapp at '" + RESOURCE_PATH
							+ "'.");
		} else {
			return new BufferedReader(new InputStreamReader(stream));
		}
	}

	private List<String> readSignificantLines(BufferedReader reader)
			throws IOException {
		List<String> lines = new ArrayList<String>();

		String line = null;
		while (null != (line = reader.readLine())) {
			line = line.trim();
			if ((!line.isEmpty()) && (!line.startsWith("#"))) {
				lines.add(line);
			}
		}

		return lines;
	}

	private void closeReader(Reader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private RevisionInfoBean parseRevisionInformation(List<String> lines)
			throws ParseException {
		checkValidNumberOfLines(lines);
		String dateLine = lines.get(0);
		List<String> levelLines = lines.subList(1, lines.size());

		Date buildDate = parseDateLine(dateLine);
		List<LevelRevisionInfo> levelInfos = parseLevelLines(levelLines);
		return new RevisionInfoBean(buildDate, levelInfos);
	}

	private void checkValidNumberOfLines(List<String> lines)
			throws ParseException {
		if (lines.isEmpty()) {
			throw new ParseException(
					"The revision info resource file contains no data.", 0);
		}
	}

	private Date parseDateLine(String dateLine) throws ParseException {
		return new SimpleDateFormat(DATE_FORMAT).parse(dateLine);
	}

	private List<LevelRevisionInfo> parseLevelLines(List<String> levelLines)
			throws ParseException {
		List<LevelRevisionInfo> infos = new ArrayList<LevelRevisionInfo>();
		for (String line : levelLines) {
			Matcher m = LEVEL_INFO_PATTERN.matcher(line);
			if (m.matches()) {
				String name = m.group(1).trim();
				String release = m.group(2).trim();
				String revision = m.group(3).trim();
				infos.add(new LevelRevisionInfo(name, release, revision));
			} else {
				throw new ParseException(
						"Failed to parse the revision info in '" + line + "'",
						0);
			}
		}
		return infos;
	}

	/** On shutdown, clean up. */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		RevisionInfoBean.removeBean(sce.getServletContext());
	}

}
