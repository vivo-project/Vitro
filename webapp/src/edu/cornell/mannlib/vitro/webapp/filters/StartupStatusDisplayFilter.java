/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * No matter what URL is requested, check to see whether the StartupStatus
 * contains errors or warnings. If it does, hijack the request to show the
 * StartupStatus display page.
 * 
 * This only happens once. This filter does nothing after displaying the startup
 * status page one time.
 */
public class StartupStatusDisplayFilter implements Filter {
	private static final String TEMPLATE_PATH = "/templates/freemarker/body/admin/startupStatus-displayRaw.ftl";

	private ServletContext ctx;
	private boolean statusAlreadyDisplayed;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		ctx = filterConfig.getServletContext();
		statusAlreadyDisplayed = false;
	}

	@Override
	public void destroy() {
		// nothing to do.
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		StartupStatus ss = StartupStatus.getBean(ctx);
		if (ss.allClear() || statusAlreadyDisplayed) {
			chain.doFilter(req, resp);
			return;
		}

		displayStartupStatus(req, resp);
		statusAlreadyDisplayed = true;
	}

	private void displayStartupStatus(ServletRequest req, ServletResponse resp)
			throws IOException, ServletException {
		Configuration cfg = new Configuration();
		cfg.setTemplateLoader(new WebappTemplateLoader(ctx));
		Template tpl = cfg.getTemplate(TEMPLATE_PATH);

		Map<String, Object> bodyMap = new HashMap<String, Object>();
		bodyMap.put("status", StartupStatus.getBean(ctx));

		try {
			PrintWriter out = resp.getWriter();
			tpl.process(bodyMap, out);
			out.flush();
		} catch (TemplateException e) {
			throw new ServletException("Problem with Freemarker Template", e);
		}
	}
}
