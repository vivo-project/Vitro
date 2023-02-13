/* $This file is distributed under the terms of the license in LICENSE$ */

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * A framework that makes it simpler to send email messages with a body built
 * from a Freemarker template.
 *
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

	public void setBodyMap(Map<String, Object> body) {
		if (body == null) {
			this.bodyMap = Collections.emptyMap();
		} else {
			this.bodyMap = new HashMap<String, Object>(body);
		}
	}

	public void processTemplate() {
		try {
			addDefaultBodyMapValues();
			StringWriter writer = new StringWriter();
			new Template(null, getInlineVariable("subject"), config).process(bodyMap, writer);
			subject = writer.toString();
			writer.getBuffer().setLength(0);
			new Template(null, getEmailTemplate(), config).process(bodyMap, writer);
			htmlContent = writer.toString();
			writer.getBuffer().setLength(0);
			new Template(null, getInlineVariable("textMessage"), config).process(bodyMap, writer);
			textContent = writer.toString();
		} catch (TemplateException | IOException e) {
			log.error(e, e);
		}
    }

	private void addDefaultBodyMapValues() {
		if (!bodyMap.containsKey("subject")) {
			if (StringUtils.isBlank(subject)) {
				bodyMap.put("subject", "No subject defined");	
			}
			bodyMap.put("subject", subject);	
		}
		if (!bodyMap.containsKey("textMessage")) {
			bodyMap.put("textMessage", "No text message defined");
		}
		if (!bodyMap.containsKey("htmlMessage")) {
			bodyMap.put("htmlMessage", "No html message defined");
		}
	}

	private String getInlineVariable(String name) {
		return "<@" + name + "?interpret />";
	}
	
	private String getEmailTemplate() {
		return "<html>\n"
		+ "    <head>\n"
		+ "        <title><@subject?interpret /></title>\n"
		+ "    </head>\n"
		+ "    <body>\n"
		+ "		<@htmlMessage?interpret />\n"
		+ "    </body>\n"
		+ "</html>";
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
					msg.setContent(htmlContent, "text/html; charset=UTF-8");
				}
			} else {
				if (htmlContent.isEmpty()) {
					msg.setContent(textContent, "text/plain; charset=UTF-8");
				} else {
					MimeMultipart content = new MimeMultipart("alternative");
					addBodyPart(content, textContent, "text/plain; charset=UTF-8");
					addBodyPart(content, htmlContent, "text/html; charset=UTF-8");
					msg.setContent(content);
				}
			}

			msg.setSentDate(new Date());
			sendMessage(msg);
			return true;
		} catch (MessagingException e) {
			log.error("Failed to send message.", e);
			return false;
		}
	}

	private void sendMessage(MimeMessage msg) {
		Thread thread = new Thread() {
			public void run() {
				try {
					Transport.send(msg);
				} catch (MessagingException e) {
					log.error(e, e);
				}
			}
		  };
		  thread.start();
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
