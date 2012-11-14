/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.ibm.icu.text.SimpleDateFormat;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * Show the list of living background threads (instances of
 * VitroBackgroundThread), and their status.
 */
public class ShowBackgroundThreadsController extends FreemarkerHttpServlet {

	private static final String TEMPLATE_NAME = "admin-showThreads.ftl";
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	
	@Override
	protected ResponseValues processRequest(VitroRequest vreq) throws Exception {

		SortedMap<String, ThreadInfo> threadMap = new TreeMap<String, ThreadInfo>();

		for (VitroBackgroundThread thread : VitroBackgroundThread
				.getLivingThreads()) {
			ThreadInfo threadInfo = getThreadInfo(thread);
			threadMap.put(threadInfo.getName(), threadInfo);
		}

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put("threads", new ArrayList<ThreadInfo>(threadMap.values()));

		return new TemplateResponseValues(TEMPLATE_NAME, bodyMap);

	}

	private ThreadInfo getThreadInfo(VitroBackgroundThread thread) {
		try {
			String name = thread.getName();
			String workLevel = String.valueOf(thread.getWorkLevel().getLevel());
			String since = formatDate(thread.getWorkLevel().getSince());
			String flags = String.valueOf(thread.getWorkLevel().getFlags());
			return new ThreadInfo(name, workLevel, since, flags);
		} catch (Exception e) {
			return new ThreadInfo("UNKNOWN THREAD", "UNKNOWN", "UNKNOWN",
					e.toString());
		}
	}

	private String formatDate(Date since) {
		return new SimpleDateFormat(DATE_FORMAT).format(since);
	}


	public static class ThreadInfo {
		private final String name;
		private final String workLevel;
		private final String since;
		private final String flags;

		public ThreadInfo(String name, String workLevel, String since,
				String flags) {
			this.name = name;
			this.workLevel = workLevel;
			this.since = since;
			this.flags = flags;
		}

		public String getName() {
			return name;
		}

		public String getWorkLevel() {
			return workLevel;
		}

		public String getSince() {
			return since;
		}

		public String getFlags() {
			return flags;
		}

	}
}
