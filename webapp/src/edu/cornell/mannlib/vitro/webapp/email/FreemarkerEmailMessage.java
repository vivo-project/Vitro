/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.email;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper.TemplateProcessingException;
import freemarker.template.Configuration;

/**
 * A framework that makes it simpler to send email messages with a body built
 * from a Freemarker template.
 * 
 * In fact, the body can be plain text from a template, HTML from a template, or
 * both.
 */
public class FreemarkerEmailMessage {
	private static final Log log = LogFactory
			.getLog(FreemarkerEmailMessage.class);

	private static final String ATTRIBUTE_NAME = "freemarkerConfig";

	private final HttpServletRequest req;
	private final Session session;
	private final Configuration config;
	private final ServletContext ctx;

	private final List<Recipient> recipients = new ArrayList<Recipient>();
	private final InternetAddress replyToAddress;

	private InternetAddress fromAddress = null;
	private String subject = "";
	private String htmlTemplateName;
	private String textTemplateName;
	private Map<String, Object> bodyMap = Collections.emptyMap();

	/**
	 * Package access - should only be created by the factory.
	 */
	FreemarkerEmailMessage(HttpServletRequest req, Session session,
			InternetAddress replyToAddress) {
		this.req = req;
		this.session = session;
		this.replyToAddress = replyToAddress;

		this.ctx = req.getSession().getServletContext();

		Object o = req.getAttribute(ATTRIBUTE_NAME);
		if (!(o instanceof Configuration)) {
			String oClass = (o == null) ? "null" : o.getClass().getName();

			throw new IllegalStateException(
					"Request does not contain a Configuration at '"
							+ ATTRIBUTE_NAME + "': " + oClass);
		}
		this.config = (Configuration) o;
	}

	public void addRecipient(RecipientType type, String emailAddress) {
		if (type == null) {
			throw new NullPointerException("type may not be null.");
		}
		if (emailAddress == null) {
			log.warn("recipient type was '" + type
					+ "', but email address was null.");
			return;
		}

		try {
			recipients.add(new Recipient(type, emailAddress));
		} catch (AddressException e) {
			log.warn("invalid recipient address: " + type + ", '"
					+ emailAddress + "'");
			return;
		}
	}

	public void addRecipient(RecipientType type, String emailAddress,
			String personalName) {
		if (personalName == null) {
			addRecipient(type, emailAddress);
		}
		if (type == null) {
			throw new NullPointerException("type may not be null.");
		}
		if (emailAddress == null) {
			log.warn("recipient type was '" + type
					+ "', but email address was null.");
			return;
		}

		try {
			recipients.add(new Recipient(type, emailAddress, personalName));
		} catch (AddressException e) {
			log.warn("invalid recipient address: " + type + ", '"
					+ emailAddress + "', personal name '" + personalName + "'");
			return;
		} catch (UnsupportedEncodingException e) {
			log.warn("invalid recipient address: " + type + ", '"
					+ emailAddress + "', personal name '" + personalName + "'");
			return;
		}
	}

	public void setSubject(String subject) {
		this.subject = nonNull(subject, "");
	}

	public void setHtmlTemplate(String templateName) {
		this.htmlTemplateName = nonNull(templateName, "");
	}

	public void setTextTemplate(String templateName) {
		this.textTemplateName = nonNull(templateName, "");
	}

	public void setBodyMap(Map<String, Object> body) {
		if (body == null) {
			this.bodyMap = Collections.emptyMap();
		} else {
			this.bodyMap = Collections
					.unmodifiableMap(new HashMap<String, Object>(body));
		}
	}

	public void send() {
		String textBody = figureMessageBody(textTemplateName);
		String htmlBody = figureMessageBody(htmlTemplateName);

		try {
			MimeMessage msg = new MimeMessage(session);
			msg.setReplyTo(new Address[] { replyToAddress });

			if (fromAddress == null) {
				msg.addFrom(new Address[] { replyToAddress });
			} else {
				msg.addFrom(new Address[] { fromAddress });
			}

			for (Recipient recipient : recipients) {
				msg.addRecipient(recipient.type, recipient.address);
			}

			msg.setSubject(subject);

			if (textBody.isEmpty()) {
				if (htmlBody.isEmpty()) {
					log.error("Message has neither text body nor HTML body");
				} else {
					msg.setContent(htmlBody, "text/html");
				}
			} else {
				if (htmlBody.isEmpty()) {
					msg.setContent(textBody, "text/plain");
				} else {
					MimeMultipart content = new MimeMultipart("alternative");
					addBodyPart(content, textBody, "text/plain");
					addBodyPart(content, htmlBody, "text/html");
					msg.setContent(content);
				}
			}

			msg.setSentDate(new Date());

			Transport.send(msg);

		} catch (MessagingException e) {
			log.error("Failed to send message.", e);
		}
	}

	/**
	 * Process the template. If there is no template name or if there is a
	 * problem with the process, return an empty string.
	 */
	private String figureMessageBody(String templateName) {
		if (templateName.isEmpty()) {
			return "";
		}

		try {
			TemplateProcessingHelper helper = new TemplateProcessingHelper(
					config, req, ctx);
			return helper.processTemplate(templateName, bodyMap).toString();
		} catch (TemplateProcessingException e) {
			log.warn("Exception while processing email template '"
					+ templateName + "'", e);
			return "";
		}
	}

	private void addBodyPart(MimeMultipart content, String textBody, String type)
			throws MessagingException {
		MimeBodyPart bodyPart = new MimeBodyPart();
		bodyPart.setContent(textBody, type);
		content.addBodyPart(bodyPart);
	}

	private <T> T nonNull(T value, T defaultValue) {
		return (value == null) ? defaultValue : value;
	}

	private static class Recipient {
		final Message.RecipientType type;
		final InternetAddress address;

		public Recipient(RecipientType type, String address)
				throws AddressException {
			this.type = type;
			this.address = new InternetAddress(address);
		}

		public Recipient(RecipientType type, String address, String personalName)
				throws AddressException, UnsupportedEncodingException {
			this.type = type;
			this.address = new InternetAddress(address, personalName);
		}
	}

}
