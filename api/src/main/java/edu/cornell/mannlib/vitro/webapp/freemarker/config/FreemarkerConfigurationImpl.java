/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.freemarker.config;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import freemarker.core.ParseException;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.TemplateNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleScalar;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

/**
 * Extend the Freemarker Configuration class to include some information that is
 * particular to the current request.
 * 
 * A reference to the current request is not always available to the Freemarker
 * Configuration, so we take advantage of the fact that each request runs in a
 * separate thread, and store a reference to that request in a ThreadLocal
 * object.
 * 
 * Then, we override any methods that should return that request-based
 * information instead of (or in addition to) the common info.
 * 
 * Only the getters are overridden, not the setters. So if you call
 * setAllSharedVariables(), for example, it will have no effect on the
 * request-based information.
 * 
 * Notice that the reference to the current request is actually stored through a
 * WeakReference. This is because the ThreadLocal will not be cleared when the
 * webapp is stopped, so none of the references from that ThreadLocal are
 * eligible for garbage collection. If any of those references is an instance of
 * a class that is loaded by the webapp, then the webapp ClassLoader is not
 * eligible for garbage collection. This would be a huge memory leak.
 * 
 * Thanks to the WeakReference, the request is eligible for garbage collection
 * if nothing else refers to it. In theory, this means that the WeakReference
 * could return a null, but if the garbage collector has taken the request, then
 * who is invoking this object?
 */
public class FreemarkerConfigurationImpl extends Configuration {
	private static final Log log = LogFactory
			.getLog(FreemarkerConfigurationImpl.class);

	private static final String ATTRIBUTE_NAME = RequestBasedInformation.class
			.getName();

	private final ThreadLocal<WeakReference<HttpServletRequest>> reqRef = new ThreadLocal<>();

	void setRequestInfo(HttpServletRequest req) {
		reqRef.set(new WeakReference<>(req));
		req.setAttribute(ATTRIBUTE_NAME, new RequestBasedInformation(req, this));
	}

	private RequestBasedInformation getRequestInfo() {
		HttpServletRequest req = reqRef.get().get();
		return (RequestBasedInformation) req.getAttribute(ATTRIBUTE_NAME);
	}

	@Override
	public Object getCustomAttribute(String name) {
		Map<String, Object> attribs = getRequestInfo().getCustomAttributes();
		if (attribs.containsKey(name)) {
			return attribs.get(name);
		} else {
			return super.getCustomAttribute(name);
		}
	}

	@Override
	public String[] getCustomAttributeNames() {
		Set<String> rbiNames = getRequestInfo().getCustomAttributes().keySet();
		return joinNames(rbiNames, super.getCustomAttributeNames());
	}

	@Override
	public TemplateModel getSharedVariable(String name) {
		Map<String, TemplateModel> vars = getRequestInfo().getSharedVariables();
		if (vars.containsKey(name)) {
			return vars.get(name);
		} else {
			return super.getSharedVariable(name);
		}
	}

	@Override
	public Set<String> getSharedVariableNames() {
		Set<String> rbiNames = getRequestInfo().getSharedVariables().keySet();

		@SuppressWarnings("unchecked")
		Set<String> superNames = super.getSharedVariableNames();

		Set<String> allNames = new HashSet<>(superNames);
		allNames.addAll(rbiNames);
		return allNames;
	}

	@Override
	public Locale getLocale() {
		return getRequestInfo().getReq().getLocale();
	}

	private String[] joinNames(Set<String> nameSet, String[] nameArray) {
		Set<String> allNames = new HashSet<>(nameSet);
		for (String n : nameArray) {
			allNames.add(n);
		}
		return (String[]) allNames.toArray();
	}

	// ----------------------------------------------------------------------
	// Apply DataGetters to templates when loading.
	//
	// TODO Clean this up VIVO-249
	// ----------------------------------------------------------------------

	/**
	 * Override getTemplate(), so we can apply DataGetters to all included
	 * templates.
	 * 
	 * This won't work for top-level Templates, since the Environment hasn't
	 * been created yet. When TemplateProcessingHelper creates the Environment,
	 * it must call retrieveAndRunDataGetters() for the top-level Template.
	 */
	@Override
	public Template getTemplate(String name, Locale locale, Object customLookupCondition, String encoding, boolean parseAsFTL, boolean ignoreMissing) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException {
		Template template = super.getTemplate(name, locale, customLookupCondition, encoding, parseAsFTL, ignoreMissing);

		if (template == null) {
			log.debug("Template '" + name + "' not found for locale '" + locale
					+ "'.");
			return template;
		}

		Environment env = getEnvironment();
		if (env == null) {
			log.debug("Not fetching data getters for template '"
					+ template.getName() + "'. No environment.");
			return template;
		}

		retrieveAndRunDataGetters(env, template.getName());
		return template;
	}

	/**
	 * Find the DataGetters for this template, and apply them to the Freemarker
	 * environment.
	 */
	public static void retrieveAndRunDataGetters(Environment env,
			String templateName) {
		HttpServletRequest req = (HttpServletRequest) env
				.getCustomAttribute("request");
		VitroRequest vreq = new VitroRequest(req);

		if (dataGettersAlreadyApplied(env, templateName)) {
			log.debug("DataGetters for '" + templateName
					+ "' have already been applied");
			return;
		}

		try {
			List<DataGetter> dgList = DataGetterUtils
					.getDataGettersForTemplate(vreq, vreq.getDisplayModel(),
							templateName);
			log.debug("Retrieved " + dgList.size()
					+ " data getters for template '" + templateName + "'");

			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = (Map<String, Object>) DeepUnwrap
					.permissiveUnwrap(env.getDataModel());
			for (DataGetter dg : dgList) {
				applyDataGetter(dg, env, dataMap);
			}
		} catch (Exception e) {
			log.warn(e, e);
		}
	}

	/**
	 * Have the DataGetters for this template already been applied to this
	 * environment? If not, record that they are being applied now.
	 */
	@SuppressWarnings("unchecked")
	private static boolean dataGettersAlreadyApplied(Environment env,
			String templateName) {
		Set<String> names;
		Object o = env.getCustomAttribute("dataGettersApplied");
		if (o instanceof Set) {
			names = (Set<String>) o;
		} else {
			names = new HashSet<String>();
		}

		boolean added = names.add(templateName);
		if (added) {
			env.setCustomAttribute("dataGettersApplied", names);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Get the data from a DataGetter, and store it in global variables in the
	 * Freemarker environment.
	 */
	private static void applyDataGetter(DataGetter dg, Environment env,
			Map<String, Object> dataMap) throws TemplateModelException {
		Map<String, Object> moreData = dg.getData(dataMap);
		ObjectWrapper wrapper = env.getObjectWrapper();
		if (moreData != null) {
			for (String key : moreData.keySet()) {
				Object value = moreData.get(key);
				env.setGlobalVariable(key, wrapper.wrap(value));
				log.debug("Stored in environment: '" + key + "' = '" + value
						+ "'");
			}
		}
	}

	// ----------------------------------------------------------------------
	// Helper class
	// ----------------------------------------------------------------------

	/**
	 * Holds the request-based information. Currently, it's shared variables, a
	 * custom attribute, and the locale. In the future, it could be more.
	 */
	private static class RequestBasedInformation {
		private final HttpServletRequest req;
		private final Configuration c;
		private final Map<String, Object> customAttributes = new HashMap<>();
		private final Map<String, TemplateModel> sharedVariables = new HashMap<>();

		public RequestBasedInformation(HttpServletRequest req, Configuration c) {
			this.req = req;
			this.c = c;

			setSharedVariables(req);
			setCustomAttributes(req);
		}

		public HttpServletRequest getReq() {
			return req;
		}

		public Map<String, Object> getCustomAttributes() {
			return customAttributes;
		}

		public Map<String, TemplateModel> getSharedVariables() {
			return sharedVariables;
		}

		private void setSharedVariables(HttpServletRequest req) {
			ServletContext ctx = req.getSession().getServletContext();
			VitroRequest vreq = new VitroRequest(req);
			ApplicationBean appBean = vreq.getAppBean();
			String siteName = appBean.getApplicationName();
			String tagLine = appBean.getShortHand();
			String themeDir = appBean.getThemeDir().replaceAll("/$", "");
			String currentTheme = themeDir
					.substring(themeDir.lastIndexOf('/') + 1);
			Map<String, String> siteUrls = getSiteUrls(ctx, themeDir);

			sharedVariables.put("siteName", wrap(siteName));
			sharedVariables.put("themeDir", wrap(themeDir));
			sharedVariables.put("currentTheme", wrap(currentTheme));
			sharedVariables.put("siteTagline", wrap(tagLine));
			sharedVariables.put("urls", wrap(siteUrls));
		}

		private Map<String, String> getSiteUrls(ServletContext ctx,
				String themeDir) {
			Map<String, String> urls = new HashMap<String, String>();

			// Templates use this to construct urls.
			urls.put("base", ctx.getContextPath());
			urls.put("home", UrlBuilder.getHomeUrl());
			urls.put("about", UrlBuilder.getUrl(Route.ABOUT));
			urls.put("search", UrlBuilder.getUrl(Route.SEARCH));
			urls.put("termsOfUse", UrlBuilder.getUrl(Route.TERMS_OF_USE));
			urls.put("login", UrlBuilder.getLoginUrl());
			urls.put("logout", UrlBuilder.getLogoutUrl());
			urls.put("siteAdmin", UrlBuilder.getUrl(Route.SITE_ADMIN));

			urls.put("themeImages", UrlBuilder.getUrl(themeDir + "/images"));
			urls.put("images", UrlBuilder.getUrl("/images"));
			urls.put("theme", UrlBuilder.getUrl(themeDir));
			urls.put("index", UrlBuilder.getUrl("/browse"));
			urls.put("developerAjax", UrlBuilder.getUrl("/admin/developerAjax"));

			return urls;
		}

		private TemplateModel wrap(Object o) {
			try {
				return c.getObjectWrapper().wrap(o);
			} catch (TemplateModelException e) {
				log.error("Failed to wrap this "
						+ "for the Freemarker configuration: " + o, e);
				return new SimpleScalar(String.valueOf(o));
			}
		}

		private void setCustomAttributes(HttpServletRequest req) {
			customAttributes.put("request", req);
		}

	}
}
