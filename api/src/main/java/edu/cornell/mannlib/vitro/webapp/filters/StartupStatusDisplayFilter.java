/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

/**
 * No matter what URL is requested, check to see whether the StartupStatus
 * contains errors or warnings. If it does, hijack the request to show the
 * StartupStatus display page.
 *
 * If the status only contains warnings, this only happens once. Subsequent
 * requests will display normally. However, if the status contains a fatal
 * error, this filter will hijack every request, and will not let you proceed.
 */
@Component(value="StartupStatusDisplayFilter")
@Order(1)
public class StartupStatusDisplayFilter implements Filter {
	Logger logger = LoggerFactory.getLogger(StartupStatusDisplayFilter.class);
	private static final String TEMPLATE_PATH = "templates/freemarker/body/admin/startupStatus-displayRaw.ftl";

	private ServletContext ctx;
	private StartupStatus ss;
	private boolean statusAlreadyDisplayed;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		ctx = filterConfig.getServletContext();
		ss = StartupStatus.getBean(ctx);
		statusAlreadyDisplayed = false;
	}

	@Override
	public void destroy() {
		// nothing to do.
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {

		if (ss.allClear() || (!isFatal() && statusAlreadyDisplayed)) {
			chain.doFilter(req, resp);
			return;
		}

		displayStartupStatus(req, resp);
		statusAlreadyDisplayed = true;
	}

	private void displayStartupStatus(ServletRequest req, ServletResponse resp)
			throws IOException, ServletException {
		HttpServletResponse hresp = (HttpServletResponse) resp;
		HttpServletRequest hreq = (HttpServletRequest) req;

		try {
			Map<String, Object> bodyMap = new HashMap<String, Object>();
			bodyMap.put("status", ss);
			bodyMap.put("showLink", !isFatal());
			bodyMap.put("contextPath", getContextPath());
			bodyMap.put("applicationName", getApplicationName());

			String url = "";

			String path = hreq.getRequestURI();
			if (path != null) {
				url = path;
			}

			String query = hreq.getQueryString();
			if (!StringUtils.isEmpty(query)) {
				url = url + "?" + query;
			}

			bodyMap.put("url", url);

			hresp.setContentType("text/html;charset=UTF-8");
			hresp.setStatus(SC_INTERNAL_SERVER_ERROR);
			Template tpl = loadFreemarkerTemplate();
			tpl.process(bodyMap, hresp.getWriter());
		} catch (TemplateException e) {
			throw new ServletException("Problem with Freemarker Template", e);
		}
	}

	private String getContextPath() {
		String cp = ctx.getContextPath();
		if ((cp == null) || cp.isEmpty()) {
			return "The application";
		} else {
			return cp;
		}
	}

	private Object getApplicationName() {
		String name = "";
		try {
			WebappDaoFactory wadf = ModelAccess.on(ctx).getWebappDaoFactory();
			ApplicationBean app = wadf.getApplicationDao().getApplicationBean();
			name = app.getApplicationName();
		} catch (Exception e) {
			// deal with problems below
		}

		if ((name != null) && (!name.isEmpty())) {
			return name;
		} else {
			return getContextPath();
		}
	}

	private Template loadFreemarkerTemplate() throws IOException {
		Configuration cfg = new Configuration();
		try{
			File file = ResourceUtils.getFile("classpath:"+TEMPLATE_PATH);
			BufferedReader br = new BufferedReader(new FileReader(file));
			String[] templatePath = TEMPLATE_PATH.split("/");
			String templateName = templatePath[templatePath.length-1];
			return new Template(templateName,br,cfg);
		}catch(FileNotFoundException e){
			logger.error("Unable to locate freemarker template '"+TEMPLATE_PATH+"' within resource folder.");
			throw new IOException("Unable to locate freemarker template '"+TEMPLATE_PATH+"' within resource folder.");
		}catch(IOException e){
			logger.error("Error while creating Template object from '"+TEMPLATE_PATH+"' template");
			throw e;
		}
	}

	private boolean isFatal() {
		return !ss.getErrorItems().isEmpty();
	}
}
