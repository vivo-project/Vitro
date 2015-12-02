/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.email;

import java.io.IOException;
import java.io.StringWriter;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.directives.EmailDirective;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * A framework that makes it simpler to send email messages with a body built
 * from a Freemarker template.
 * 
 * The template must call the @email directive, which may provide the subject
 * line, the HTML content, and the plain text content. If these values are not
 * provided by the directive, they default to empty strings, or to values that
 * were set by the controller.
 * 
 * @see EmailDirective
 */
public class FreemarkerEmailMessage {
	private static final Log log = LogFactory
			.getLog(FreemarkerEmailMessage.class);

	private final VitroRequest vreq;
	private final Session mailSession;
	private final Configuration config;

	private final List<Recipient> recipients = new ArrayList<Recipient>();
	private final InternetAddress replyToAddress;

	private InternetAddress fromAddress = null;
	private String subject = "";
	private String templateName = "";
	private String htmlContent = "";
	private String textContent = "";
	private Map<String, Object> bodyMap = Collections.emptyMap();

	/**
	 * Package access - should only be created by the factory.
	 */
	FreemarkerEmailMessage(VitroRequest vreq, Configuration fConfig,
			Session mailSession, InternetAddress replyToAddress) {
		this.vreq = vreq;
		this.mailSession = mailSession;
		this.replyToAddress = replyToAddress;
		this.config = fConfig;
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
		} catch (UnsupportedEncodingException e) {
			log.warn("invalid recipient address: " + type + ", '"
					+ emailAddress + "', personal name '" + personalName + "'");
			return;
		}
	}

	public void setSubject(String subject) {
		this.subject = nonNull(subject, "");
	}

	public void setHtmlContent(String htmlContent) {
		this.htmlContent = nonNull(htmlContent, "");
	}

	public void setTextContent(String textContent) {
		this.textContent = nonNull(textContent, "");
	}

	public void setTemplate(String templateName) {
		this.templateName = nonNull(templateName, "");
	}

	public void setBodyMap(Map<String, Object> body) {
		if (body == null) {
			this.bodyMap = Collections.emptyMap();
		} else {
			this.bodyMap = new HashMap<String, Object>(body);
		}
	}

	public void processTemplate() {
		bodyMap.put("email", new EmailDirective(this));

		try {
			config.getTemplate(templateName).process(bodyMap,
					new StringWriter());
		} catch (TemplateException e) {
			log.error(e, e);
		} catch (IOException e) {
			log.error(e, e);
		}
	}

	public boolean send() {
		try {
			MimeMessage msg = new MimeMessage(mailSession);
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

			if (textContent.isEmpty()) {
				if (htmlContent.isEmpty()) {
					log.error("Message has neither text body nor HTML body");
				} else {
					msg.setContent(htmlContent, "text/html");
				}
			} else {
				if (htmlContent.isEmpty()) {
					msg.setContent(textContent, "text/plain");
				} else {
					MimeMultipart content = new MimeMultipart("alternative");
					addBodyPart(content, textContent, "text/plain");
					addBodyPart(content, htmlContent, "text/html");
					msg.setContent(content);
				}
			}

			msg.setSentDate(new Date());

			Transport.send(msg);
			return true;
		} catch (MessagingException e) {
			log.error("Failed to send message.", e);
			return false;
		}
	}

	private void addBodyPart(MimeMultipart content, String textBody, String type)
			throws MessagingException {
		MimeBodyPart bodyPart = new MimeBodyPart();
		bodyPart.setContent(textBody, type);
		content.addBodyPart(bodyPart);
	}

	public String getReplyToAddress() {
		return replyToAddress.getAddress();
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
				throws UnsupportedEncodingException {
			this.type = type;
			this.address = new InternetAddress(address, personalName);
		}
	}

}
