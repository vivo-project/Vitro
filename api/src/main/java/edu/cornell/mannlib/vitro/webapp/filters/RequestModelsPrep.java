/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.filters;

import java.io.IOException;
import java.text.Collator;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * This used to set up a lot of request-based objects. Now, most of them are
 * obtained through ModelAccess, which does not require setup here. However, it
 * does require teardown.
 *
 * This is done in a filter, so it applies to both Servlets and JSPs.
 */
@Configuration
@Order(8)
public class RequestModelsPrep implements Filter {

	@Override
	public void init(FilterConfig fc) throws ServletException {
		// Nothing to do
	}

	@Bean(value = "RequestModelsPrep")
	public FilterRegistrationBean<RequestModelsPrep> loggingFilter(){
		FilterRegistrationBean<RequestModelsPrep> registrationBean
				= new FilterRegistrationBean<>();

		registrationBean.setFilter(new RequestModelsPrep());
		registrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);

		return registrationBean;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		// If we're not authorized for this request, skip the chain and
		// redirect.
		if (!ModelSwitcher.authorizedForSpecialModel(req)) {
			VitroHttpServlet.redirectUnauthorizedRequest(req, resp);
			return;
		}

		try {
			setCollator(new VitroRequest(req));
			filterChain.doFilter(req, resp);
		} finally {
			if (ModelAccess.isPresent(req)) {
				ModelAccess.on(req).close();
			}
		}
	}

	//TODO Does this method do anything?
	private void setCollator(VitroRequest vreq) {
		@SuppressWarnings("unchecked")
		Enumeration<Locale> locales = vreq.getLocales();
		while (locales.hasMoreElements()) {
			Locale locale = locales.nextElement();
			Collator collator = Collator.getInstance(locale);
			if (collator != null) {
				vreq.setCollator(collator);
				return;
			}
		}
		vreq.setCollator(Collator.getInstance());
	}

	@Override
	public void destroy() {
		// Nothing to destroy
	}

}
