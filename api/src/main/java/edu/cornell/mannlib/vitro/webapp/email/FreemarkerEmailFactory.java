/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.email;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Scanner;

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
import edu.cornell.mannlib.vitro.webapp.freemarker.config.FreemarkerConfiguration;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import freemarker.template.Configuration;

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
		Configuration fConfig = FreemarkerConfiguration.getConfig(vreq);
		return new FreemarkerEmailMessage(vreq, fConfig,
				factory.getEmailSession(), factory.getReplyToAddress());
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

	public static boolean isConfigured(HttpServletRequest req) {
		return (getFactory(req) != null);
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
		new SmtpHostTester().test(this.smtpHost);

		this.replyToAddress = getReplyToAddressFromConfig(ctx);
		this.emailSession = createEmailSession(smtpHost);
	}

	String getSmtpHost() {
		return smtpHost;
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
			throw new NotConfiguredException(SMTP_HOST_PROPERTY);
		}
		return hostName;
	}

	private InternetAddress getReplyToAddressFromConfig(ServletContext ctx) {
		ConfigurationProperties config = ConfigurationProperties.getBean(ctx);
		String rawAddress = config.getProperty(REPLY_TO_PROPERTY, "");
		if (rawAddress.isEmpty()) {
			throw new NotConfiguredException(REPLY_TO_PROPERTY);
		}

		try {
			InternetAddress[] addresses = InternetAddress.parse(rawAddress,
					false);
			if (addresses.length == 0) {
				throw new BadPropertyValueException("No Reply-To address",
						REPLY_TO_PROPERTY);
			} else if (addresses.length > 1) {
				throw new BadPropertyValueException(
						"More than one Reply-To address", REPLY_TO_PROPERTY);
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
	// Helper classes
	// ----------------------------------------------------------------------

	public static class NotConfiguredException extends RuntimeException {
		public NotConfiguredException(String property) {
			super("Configuration property for '" + property
					+ "' is empty - Email functions are disabled.");
		}
	}

	public static class BadPropertyValueException extends RuntimeException {
		public BadPropertyValueException(String problem, String property) {
			super(problem + " configured in '" + property
					+ "' - Email functions are disabled.");
		}
	}

	public static class InvalidSmtpHost extends RuntimeException {
		public InvalidSmtpHost(String smtpHost, String reason) {
			super("Invalid SMTP host: '" + smtpHost + "': " + reason
					+ " - Email functions are disabled.");
		}
	}

	/**
	 * Checks to see whether the SMTP host will talk to us.
	 */
	public static class SmtpHostTester {
		private static final int SMTP_PORT = 25;
		private static final int SMTP_SUCCESS_CODE = 220;

		/**
		 * Try to open a connection to the SMTP host and conduct an "empty"
		 * conversation using SMTP.
		 * 
		 * @throws InvalidSmtpHost
		 *             If anything goes wrong.
		 */
		public void test(String smtpHost) throws InvalidSmtpHost {
			Socket socket = null;
			PrintStream out = null;
			Scanner in = null;
			try {
				InetAddress hostAddr = InetAddress.getByName(smtpHost);
				socket = new Socket(hostAddr, SMTP_PORT);

				out = new PrintStream(socket.getOutputStream());
				in = new Scanner(new InputStreamReader(socket.getInputStream()));

				int smtpCode = in.nextInt();
				if (smtpCode != SMTP_SUCCESS_CODE) {
					throw new InvalidSmtpHost(smtpHost,
							"host will not converse: "
									+ "SMTP initialization code is " + smtpCode);
				}

				out.println("QUIT");
			} catch (UnknownHostException e) {
				throw new InvalidSmtpHost(smtpHost,
						"host name is not recognized");
			} catch (ConnectException e) {
				throw new InvalidSmtpHost(smtpHost,
						"refused connection on port " + SMTP_PORT);
			} catch (IOException e) {
				throw new RuntimeException("unrecognized problem: ", e);
			} finally {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
				if ((socket != null) && (!socket.isClosed())) {
					try {
						socket.close();
					} catch (IOException e) {
						log.error("failed to close socket", e);
					}
				}
			}
		}
	}

	/**
	 * Tries to create a FreemarkerEmailFactory bean and store it in the servlet
	 * context.
	 */
	public static class Setup implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext ctx = sce.getServletContext();
			StartupStatus ss = StartupStatus.getBean(ctx);

			try {
				FreemarkerEmailFactory factory = new FreemarkerEmailFactory(ctx);
				ctx.setAttribute(ATTRIBUTE_NAME, factory);
				ss.info(this,
						"The system will send email from '"
								+ factory.getReplyToAddress() + "' through '"
								+ factory.getSmtpHost() + "'.");
			} catch (NotConfiguredException e) {
				ss.info(this, e.getMessage());
			} catch (BadPropertyValueException e) {
				ss.warning(this, e.getMessage());
			} catch (InvalidSmtpHost e) {
				ss.warning(this, e.getMessage());
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
