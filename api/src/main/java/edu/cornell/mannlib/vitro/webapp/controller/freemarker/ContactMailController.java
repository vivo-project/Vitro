/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.TemplateProcessingHelper.TemplateProcessingException;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;

public class ContactMailController extends FreemarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(ContactMailController.class);
    private static final long serialVersionUID = 1L;
	
    private final static String SPAM_MESSAGE        = "Your message was flagged as spam.";
    
    private final static String WEB_USERNAME_PARAM  = "webusername";
    private final static String WEB_USEREMAIL_PARAM = "webuseremail";
    private final static String COMMENTS_PARAM      = "s34gfd88p9x1";
	
    private final static String TEMPLATE_CONFIRMATION = "contactForm-confirmation.ftl";
    private final static String TEMPLATE_EMAIL = "contactForm-email.ftl";
    private final static String TEMPLATE_BACKUP = "contactForm-backup.ftl";
    private final static String TEMPLATE_ERROR = "contactForm-error.ftl";
    private final static String TEMPLATE_FORM = "contactForm-form.ftl";
    
	private static final String EMAIL_JOURNAL_FILE_DIR = "emailJournal";
	private static final String EMAIL_JOURNAL_FILE_NAME = "contactFormEmails.html";
    
	@Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return siteName + " Feedback Form";
    }
    

    @Override
	protected ResponseValues processRequest(VitroRequest vreq) {
    	if (!FreemarkerEmailFactory.isConfigured(vreq)) {
			return errorNoSmtpServer();
		}
		
		String[] recipients = figureRecipients(vreq);
		if (recipients.length == 0) {
			return errorNoRecipients();
		}

		String webusername = nonNullAndTrim(vreq, WEB_USERNAME_PARAM);
		String webuseremail = nonNullAndTrim(vreq, WEB_USEREMAIL_PARAM);
		String comments = nonNullAndTrim(vreq, COMMENTS_PARAM);
	    String formType = nonNullAndTrim(vreq, "DeliveryType");
	    String captchaInput = nonNullAndTrim(vreq, "defaultReal");
	    String captchaDisplay = nonNullAndTrim(vreq, "defaultRealHash");

	    String errorMsg = validateInput(webusername, webuseremail, comments, captchaInput, captchaDisplay);

		if ( errorMsg != null) {
			return errorParametersNotValid(errorMsg, webusername, webuseremail, comments);
		}
		
		String spamReason = checkForSpam(comments, formType);
		if (spamReason != null) {
			return errorSpam();
		}

		return processValidRequest(vreq, webusername, webuseremail, recipients, comments);
	}

	private String[] figureRecipients(VitroRequest vreq) {
		String contactMailAddresses = vreq.getAppBean().getContactMail().trim();
		if ((contactMailAddresses == null) || contactMailAddresses.isEmpty()) {
			return new String[0];
		}
		
		return contactMailAddresses.split(",");
	}

	private ResponseValues processValidRequest(VitroRequest vreq,
			String webusername, String webuseremail, String[] recipients,
			String comments) throws Error {
		String statusMsg = null; // holds the error status

		ApplicationBean appBean = vreq.getAppBean();
		String deliveryfrom = "Message from the " + appBean.getApplicationName() + " Contact Form";

	    String originalReferer = getOriginalRefererFromSession(vreq);

	    String msgText = composeEmail(webusername, webuseremail, comments, 
	    		deliveryfrom, originalReferer, vreq.getRemoteAddr(), vreq);
	    
	    try {
	    	// Write the message to the journal file
	    	FileWriter fw = new FileWriter(locateTheJournalFile(), true);
	        PrintWriter outFile = new PrintWriter(fw); 
	        writeBackupCopy(outFile, msgText, vreq);
  
	        try {
	        	// Send the message
	        	Session s = FreemarkerEmailFactory.getEmailSession(vreq);
	        	sendMessage(s, webuseremail, webusername, recipients, deliveryfrom, msgText);
	        } catch (AddressException e) {
	            statusMsg = "Please supply a valid email address.";
	            outFile.println( statusMsg );
	            outFile.println( e.getMessage() );
	        } catch (SendFailedException e) {
	            statusMsg = "The system was unable to deliver your mail.  Please try again later.  [SEND FAILED]";
	            outFile.println( statusMsg );
	            outFile.println( e.getMessage() );
	        } catch (MessagingException e) {
	            statusMsg = "The system was unable to deliver your mail.  Please try again later.  [MESSAGING]";
	            outFile.println( statusMsg );
	            outFile.println( e.getMessage() );
	            e.printStackTrace();
	        }

	        outFile.close();
	    }
	    catch (IOException e){
	    	log.error("Can't open file to write email backup");                   
	    }         
	    
	    if (statusMsg == null) {                  
	    	// Message was sent successfully
	    	return new TemplateResponseValues(TEMPLATE_CONFIRMATION);
	    } else {
	    	Map<String, Object> body = new HashMap<String, Object>();
	        body.put("errorMessage", statusMsg);
			return new TemplateResponseValues(TEMPLATE_ERROR, body);
	    }   
	}

	/**
	 * The journal file belongs in a sub-directory of the Vitro home directory.
	 * If the sub-directory doesn't exist, create it.
	 */
	private File locateTheJournalFile() {
		File homeDir = ApplicationUtils.instance().getHomeDirectory().getPath().toFile();
		File journalDir = new File(homeDir, EMAIL_JOURNAL_FILE_DIR);
		if (!journalDir.exists()) {
			boolean created = journalDir.mkdir();
			if (!created) {
				throw new IllegalStateException(
						"Unable to create email journal directory at '"
								+ journalDir + "'");
			}
		}

		File journalFile = new File(journalDir, EMAIL_JOURNAL_FILE_NAME);
		return journalFile;
	}


	private String getOriginalRefererFromSession(VitroRequest vreq) {
		String originalReferer = (String) vreq.getSession().getAttribute("contactFormReferer");        		
	    if (originalReferer != null) {
	    	vreq.getSession().removeAttribute("contactFormReferer");
	    	/* does not support legitimate clients that don't send the Referer header
	    	  String referer = request.getHeader("Referer");
	          if (referer == null || 
	          		(referer.indexOf("comments") <0 
	          		  && referer.indexOf("correction") <0) ) {    
	              spamReason = "The form was not submitted from the " +
	              			   "Contact Us or Corrections page.";
	              statusMsg = SPAM_MESSAGE;
	          }
	        */
	    } else {
	        originalReferer = "none";
	    }
		return originalReferer;
	}

    /** Intended to mangle url so it can get through spam filtering
     *    http://host/dir/servlet?param=value -&gt;  host: dir/servlet?param=value */
    public String stripProtocol( String in ){
        if( in == null )
            return "";
        else
            return in.replaceAll("http://", "host: " );
    }
    
    private String composeEmail(String webusername, String webuseremail,
    							String comments, String deliveryfrom,
    							String originalReferer, String ipAddr,
    							HttpServletRequest request) {
 
        Map<String, Object> email = new HashMap<String, Object>();
        String template = TEMPLATE_EMAIL; 
        
        email.put("subject", deliveryfrom);
        email.put("name", webusername);
        email.put("emailAddress", webuseremail);
        email.put("comments", comments);
        email.put("ip", ipAddr);
        if ( !(originalReferer == null || originalReferer.equals("none")) ) {
            email.put("referrer", UrlBuilder.urlDecode(originalReferer));
        }
    	
        try {
            return processTemplateToString(template, email, request);
        } catch (TemplateProcessingException e) {
            log.error("Error processing email text through template: " + e.getMessage(), e);
            return null;            
        }
    }
    
    private void writeBackupCopy(PrintWriter outFile, String msgText, 
    		HttpServletRequest request) {

        Map<String, Object> backup = new HashMap<String, Object>();
        String template = TEMPLATE_BACKUP; 
        
    	Calendar cal = Calendar.getInstance();
    	backup.put("datetime", cal.getTime().toString());
        backup.put("msgText", msgText);
        
        try {
            String backupText = processTemplateToString(template, backup, request);
            outFile.print(backupText);
            outFile.flush();
            //outFile.close(); 
        } catch (TemplateProcessingException e) {
            log.error("Error processing backup text throug template: " + e.getMessage(), e);
        }
    }
    
    private void sendMessage(Session s, String webuseremail, String webusername,
    		String[] recipients, String deliveryfrom, String msgText) 
    		throws AddressException, SendFailedException, MessagingException {
        // Construct the message
        MimeMessage msg = new MimeMessage( s );
        //System.out.println("trying to send message from servlet");

        // Set the reply address
        try {
            msg.setReplyTo( new Address[] { new InternetAddress( webuseremail, webusername ) } );
        } catch (UnsupportedEncodingException e) {
        	log.error("Can't set message reply with personal name " + webusername +
        			" due to UnsupportedEncodingException");
//            msg.setFrom( new InternetAddress( webuseremail ) );
        }

        // Set the recipient address
        InternetAddress[] address=new InternetAddress[recipients.length];
        for (int i=0; i<recipients.length; i++){
            address[i] = new InternetAddress(recipients[i]);
        }
        msg.setRecipients( Message.RecipientType.TO, address );

		// Set the from address
		if (address != null && address.length > 0) {
			msg.setFrom(address[0]);
		} else {
            msg.setFrom( new InternetAddress( webuseremail ) );
		}

        // Set the subject and text
        msg.setSubject( deliveryfrom );

        // add the multipart to the message
        msg.setContent(msgText,"text/html; charset=UTF-8");

        // set the Date: header
        msg.setSentDate( new Date() );

        Transport.send( msg ); // try to send the message via smtp - catch error exceptions

    }
    
	private String nonNullAndTrim(HttpServletRequest req, String key) {
		String value = req.getParameter(key);
		return (value == null) ? "" : value.trim();
	}

    private String validateInput(String webusername, String webuseremail,
    							 String comments, String captchaInput, String captchaDisplay) {
    	
        if( webusername.isEmpty() ){
            return "Please enter a value in the Full name field.";
        } 

        if( webuseremail.isEmpty() ){
            return "Please enter a valid email address.";
        } 

        if (comments.isEmpty()) { 
            return "Please enter your comments or questions in the space provided.";
        } 
        
        if (captchaInput.isEmpty()) { 
            return "Please enter the contents of the gray box in the security field provided.";
        } 
        
		if ( !captchaHash(captchaInput).equals(captchaDisplay) ) {
			return "The value you entered in the security field did not match the letters displayed in the gray box.";
		}

        return null;
    }
    
    /**
     * @return null if message not judged to be spam, otherwise a String
     * containing the reason the message was flagged as spam.
     */
    private String checkForSpam(String comments, String formType) {
    	/* If the form doesn't specify a delivery type, treat as spam. */
	    if (!"contact".equals(formType)) {
	        return "The form specifies no delivery type.";
	    }

        /* if this blog markup is found, treat comment as blog spam */
        if (
            (comments.indexOf("[/url]") > -1
            || comments.indexOf("[/URL]") > -1
            || comments.indexOf("[url=") > -1
            || comments.indexOf("[URL=") > -1)) {
            return "The message contained blog link markup.";
        }

        /* if message is absurdly short, treat as blog spam */
        if (comments.length()<15) {
            return "The message was too short.";
        }
        
        return null;
        
    }
    
	private String captchaHash(String value) {
		int hash = 5381;
		value = value.toUpperCase();
		for(int i = 0; i < value.length(); i++) {
			hash = ((hash << 5) + hash) + value.charAt(i);
		}
		return String.valueOf(hash);
	}

	private ResponseValues errorNoSmtpServer() {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("errorMessage", 
                "This application has not yet been configured to send mail. " +
                "Email properties must be specified in the configuration properties file.");
		return new TemplateResponseValues(TEMPLATE_ERROR, body);
	}
    
	private ResponseValues errorNoRecipients() {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("errorMessage", "To establish the Contact Us mail capability "
				+ "the system administrators must specify "
				+ "at least one email address.");
		return new TemplateResponseValues(TEMPLATE_ERROR, body);
	}
	
	private ResponseValues errorParametersNotValid(String errorMsg, String webusername, String webuseremail, String comments) {
        Map<String, Object> body = new HashMap<String, Object>();
		body.put("errorMessage", errorMsg);
		body.put("formAction", "submitFeedback");
		body.put("webusername", webusername);
		body.put("webuseremail", webuseremail);
		body.put("comments", comments);
		return new TemplateResponseValues(TEMPLATE_FORM, body);
	}
	
	private ResponseValues errorSpam() {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("errorMessage", SPAM_MESSAGE);
		return new TemplateResponseValues(TEMPLATE_ERROR, body);
	}

}
