/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.email;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

/**
 * A factory that creates Freemarker-based email messages.
 * 
 * Client code should call isConfigured(), to be sure that the required email
 * properties have been provided. If isConfigured() returns false, the client
 * code should respond accordingly.
 * 
 * On the other hand, if the configuration properties are provided, but are
 * syntactically invalid, an exception is thrown and startup is aborted.
 */
public class FreemarkerEmailFactory {
	private static final Log log = LogFactory
			.getLog(FreemarkerEmailFactory.class);

	public static final String SMTP_HOST_PROPERTY = "email.smtpHost";
	public static final String REPLY_TO_PROPERTY = "email.replyTo";

	private static final String ATTRIBUTE_NAME = FreemarkerEmailFactory.class
			.getName();

	// ----------------------------------------------------------------------
	// static methods
	// ----------------------------------------------------------------------

	public static FreemarkerEmailMessage createNewMessage(VitroRequest vreq) {
		if (!isConfigured(vreq)) {
			throw new IllegalStateException("Email factory is not configured.");
		}

		FreemarkerEmailFactory factory = getFactory(vreq);
		return new FreemarkerEmailMessage(vreq, factory.getEmailSession(),
				factory.getReplyToAddress());
	}

	public static boolean isConfigured(HttpServletRequest req) {
		FreemarkerEmailFactory factory = getFactory(req);
		return (factory != null) && (factory.isConfigured());
	}

	/**
	 * Client code that does not use the FreemarkerEmailFactory can still use
	 * it's Email Session.
	 */
	public static Session getEmailSession(HttpServletRequest req) {
		if (!isConfigured(req)) {
			throw new IllegalStateException("Email factory is not configured.");
		}
		return getFactory(req).getEmailSession();
	}

	private static FreemarkerEmailFactory getFactory(HttpServletRequest req) {
		ServletContext ctx = req.getSession().getServletContext();
		return (FreemarkerEmailFactory) ctx.getAttribute(ATTRIBUTE_NAME);
	}

	// ----------------------------------------------------------------------
	// The factory
	// ----------------------------------------------------------------------

	private final String smtpHost;
	private final InternetAddress replyToAddress;
	private final Session emailSession;

	public FreemarkerEmailFactory(ServletContext ctx) {
		this.smtpHost = getSmtpHostFromConfig(ctx);
		this.replyToAddress = getReplyToAddressFromConfig(ctx);
		this.emailSession = createEmailSession(this.smtpHost);
	}

	boolean isConfigured() {
		return (!smtpHost.isEmpty()) && (replyToAddress != null);
	}

	InternetAddress getReplyToAddress() {
		return replyToAddress;
	}

	Session getEmailSession() {
		return emailSession;
	}

	private String getSmtpHostFromConfig(ServletContext ctx) {
		ConfigurationProperties config = ConfigurationProperties.getBean(ctx);
		String hostName = config.getProperty(SMTP_HOST_PROPERTY, "");
		if (hostName.isEmpty()) {
			log.info("Configuration property for '" + SMTP_HOST_PROPERTY
					+ "' is empty: email is disabled.");
		}
		return hostName;
	}

	private InternetAddress getReplyToAddressFromConfig(ServletContext ctx) {
		ConfigurationProperties config = ConfigurationProperties.getBean(ctx);
		String rawAddress = config.getProperty(REPLY_TO_PROPERTY, "");
		if (rawAddress.isEmpty()) {
			log.info("Configuration property for '" + REPLY_TO_PROPERTY
					+ "' is empty: email is disabled.");
			return null;
		}

		try {
			InternetAddress[] addresses = InternetAddress.parse(rawAddress,
					false);
			if (addresses.length == 0) {
				throw new IllegalStateException(
						"No Reply-To address configured in '"
								+ REPLY_TO_PROPERTY + "'");
			} else if (addresses.length > 1) {
				throw new IllegalStateException(
						"More than one Reply-To address configured in '"
								+ REPLY_TO_PROPERTY + "'");
			} else {
				return addresses[0];
			}
		} catch (AddressException e) {
			throw new IllegalStateException(
					"Error while parsing Reply-To address configured in '"
							+ REPLY_TO_PROPERTY + "'", e);
		}
	}

	private Session createEmailSession(String hostName) {
		Properties props = new Properties(System.getProperties());
		props.put("mail.smtp.host", hostName);
		return Session.getDefaultInstance(props, null);
	}

	// ----------------------------------------------------------------------
	// Setup class
	// ----------------------------------------------------------------------

	public static class Setup implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);

			try {
				FreemarkerEmailFactory factory = new FreemarkerEmailFactory(ctx);
				ctx.setAttribute(ATTRIBUTE_NAME, factory);

				if (factory.isConfigured()) {
					ss.info(this, "The system is configured to "
							+ "send mail to users.");
				} else {
					ss.info(this, "Configuration parameters are missing: "
							+ "the system will not send mail to users.");
				}
			} catch (Exception e) {
				ss.warning(this,
						"Failed to initialize FreemarkerEmailFactory. "
								+ "The system will not be able to send email "
								+ "to users.", e);
			}
		}

		@Override
		public void contextDestroyed(ServletContextEvent sce) {
			sce.getServletContext().removeAttribute(ATTRIBUTE_NAME);
		}
	}

}
