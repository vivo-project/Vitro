/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import freemarker.template.Configuration;

public class ContactMailController extends FreeMarkerHttpServlet {
	private static final Log log = LogFactory
			.getLog(ContactMailController.class);
    private static final long serialVersionUID = 1L;
	
    private final static String SPAM_MESSAGE        = "Your message was flagged as spam.";
    private final static String EMAIL_BACKUP_FILE_PATH = "/WEB-INF/LatestMessage.html";
    
    private final static String WEB_USERNAME_PARAM  = "webusername";
    private final static String WEB_USEREMAIL_PARAM = "webuseremail";
    private final static String COMMENTS_PARAM      = "s34gfd88p9x1";
	
    private static String smtpHost = null;

    public void init(ServletConfig servletConfig) throws javax.servlet.ServletException {
        super.init(servletConfig);
        smtpHost = getSmtpHostFromProperties();
    }
    
    public static boolean isSmtpHostConfigured() {
        if( smtpHost==null || smtpHost.equals("")) {
            return false;
        }
        return true;
    }

	public static String getSmtpHostFromProperties() {
		String host = ConfigurationProperties.getProperty("Vitro.smtpHost");
		if (host != null && !host.equals("")) {
			log.debug("Found Vitro.smtpHost value of " + host);
		} else {
			log.debug("No Vitro.smtpHost specified");
		}
		return (host != null && host.length() > 0) ? host : null;
	}
	
    protected String getTitle(String siteName) {
        return siteName + " Feedback Form";
    }
    
    protected String getBody(VitroRequest vreq, Map<String, Object> body, Configuration config) {
    	
        Portal portal = vreq.getPortal();
        String bodyTemplate = null;
        
        String statusMsg = null; // holds the error status
        
        if (!isSmtpHostConfigured()) {
            body.put("errorMessage", 
                    "This application has not yet been configured to send mail. " +
                    "An smtp host has not been specified in the configuration properties file.");
            bodyTemplate = "contactForm-error.ftl";
        }
        
        else {

            String   webusername    = vreq.getParameter(WEB_USERNAME_PARAM);
            String   webuseremail   = vreq.getParameter(WEB_USEREMAIL_PARAM);
            String   comments       = vreq.getParameter(COMMENTS_PARAM);
            
            String validationMessage = validateInput(webusername, webuseremail,
            		comments); 
            
            if (validationMessage != null) {
                // rjy7 We should reload the form, not go to the error page!
                body.put("errorMessage", 
                        "Invalid submission");
            	bodyTemplate = "contactForm-error.ftl";
            }
            
            else {
                webusername = webusername.trim();
                webuseremail = webuseremail.trim();
                comments = comments.trim();
                
                String spamReason = null;
                
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
                
                if (spamReason == null) {
        	        spamReason = checkForSpam(comments);
        	        if (spamReason != null) {
        	        	statusMsg = SPAM_MESSAGE;
        	        }
                }
        
                String formType = vreq.getParameter("DeliveryType");
                String[] deliverToArray = null;
                int recipientCount = 0;
                String deliveryfrom = null;
        
                if ("contact".equals(formType)) {
                    if (portal.getContactMail() == null || portal.getContactMail().trim().length()==0) {
                    	log.error("No contact mail address defined in current portal "+portal.getPortalId());
                        throw new Error(
                                "To establish the Contact Us mail capability the system administrators must  "
                                + "specify an email address in the current portal.");
                    } else {
                        deliverToArray = portal.getContactMail().split(",");
                    }
                    deliveryfrom   = "Message from the "+portal.getAppName()+" Contact Form";
                } else {
                    deliverToArray = portal.getContactMail().split(",");
                    statusMsg = SPAM_MESSAGE ;
                    spamReason = "The form specifies no delivery type.";
                }
                recipientCount=(deliverToArray == null) ? 0 : deliverToArray.length;
                if (recipientCount == 0) {
                	log.error("recipientCount is 0 when DeliveryType specified as \""+formType+"\"");
                    throw new Error(
                            "To establish the Contact Us mail capability the system administrators must  "
                            + "specify at least one email address in the current portal.");
                }
        
                String msgText = composeEmail(webusername, webuseremail, comments, 
                		deliveryfrom, originalReferer, vreq.getRemoteAddr(), config);
                
                // Write the email to a backup file
                try {
                    FileWriter fw = new FileWriter(getServletContext().getRealPath(EMAIL_BACKUP_FILE_PATH),true);
                    PrintWriter outFile = new PrintWriter(fw); 
                    writeBackupCopy(outFile, msgText, spamReason, config);
       
                    // Set the smtp host
                    Properties props = System.getProperties();
                    props.put("mail.smtp.host", smtpHost);
                    Session s = Session.getDefaultInstance(props,null); // was Session.getInstance(props,null);
                    //s.setDebug(true);
                    try {
                    	
                    	if (spamReason == null) {
            	        	sendMessage(s, webuseremail, webusername, deliverToArray, deliveryfrom, 
            	        			recipientCount, msgText);
                    	}
            
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
            
                    outFile.flush();
                    outFile.close();
                }
                catch (IOException e){
                	log.error("Can't open file to write email backup");                   
                }         
                
                // Message was sent successfully
                if (statusMsg == null && spamReason == null) {                  
                    bodyTemplate = "contactForm-confirmation.ftl";
                } else {
                    body.put("errorMessage", statusMsg);
                    bodyTemplate = "contactForm-error.ftl";
                }   
            }
        }
        
        return mergeBodyToTemplate(bodyTemplate, body, config);

    }
    
    /** Intended to mangle url so it can get through spam filtering
     *    http://host/dir/servlet?param=value ->  host: dir/servlet?param=value */
    public String stripProtocol( String in ){
        if( in == null )
            return "";
        else
            return in.replaceAll("http://", "host: " );
    }
    
    private String composeEmail(String webusername, String webuseremail,
    							String comments, String deliveryfrom,
    							String originalReferer, String ipAddr, Configuration config) {
 
        Map<String, Object> email = new HashMap<String, Object>();
        String template = "contactForm-email.ftl";
        
        email.put("subject", deliveryfrom);
        email.put("name", webusername);
        email.put("emailAddress", webuseremail);
        email.put("comments", comments);
        email.put("ip", ipAddr);
        if ( !(originalReferer == null || originalReferer.equals("none")) ) {
            email.put("referrer", UrlBuilder.urlDecode(originalReferer));
        }
    	
        return mergeBodyToTemplate(template, email, config);
    }
    
    private void writeBackupCopy(PrintWriter outFile, String msgText, 
    		String spamReason, Configuration config) {

        Map<String, Object> backup = new HashMap<String, Object>();
        String template = "contactForm-backup.ftl";
        
    	Calendar cal = Calendar.getInstance();
    	backup.put("datetime", cal.getTime().toString());

        if (spamReason != null) {
            backup.put("spamReason", spamReason);
        }
        
        backup.put("msgText", msgText);

        String backupText = mergeBodyToTemplate(template, backup, config);
        outFile.print(backupText);
        outFile.flush();
        //outFile.close(); 
    }
    
    private void sendMessage(Session s, String webuseremail, String webusername,
    		String[] deliverToArray, String deliveryfrom, int recipientCount,
    		String msgText) 
    		throws AddressException, SendFailedException, MessagingException {
        // Construct the message
        MimeMessage msg = new MimeMessage( s );
        //System.out.println("trying to send message from servlet");

        // Set the from address
        try {
            msg.setFrom( new InternetAddress( webuseremail, webusername ));
        } catch (UnsupportedEncodingException e) {
        	log.error("Can't set message sender with personal name " + webusername + 
        			" due to UnsupportedEncodingException");
            msg.setFrom( new InternetAddress( webuseremail ) );
        }

        // Set the recipient address
        
        if (recipientCount>0){
            InternetAddress[] address=new InternetAddress[recipientCount];
            for (int i=0; i<recipientCount; i++){
                address[i] = new InternetAddress(deliverToArray[i]);
            }
            msg.setRecipients( Message.RecipientType.TO, address );
        }

        // Set the subject and text
        msg.setSubject( deliveryfrom );

        // add the multipart to the message
        msg.setContent(msgText,"text/html");

        // set the Date: header
        msg.setSentDate( new Date() );

        Transport.send( msg ); // try to send the message via smtp - catch error exceptions

    }
    
    private String validateInput(String webusername, String webuseremail,
    							 String comments) {
    	
        if( webusername == null || "".equals(webusername.trim()) ){
            return "A proper webusername field was not found in the form submitted.";
        } 

        if( webuseremail == null || "".equals(webuseremail.trim()) ){
            return "A proper webuser email field was not found in the form submitted.";
        } 

        if (comments==null || "".equals(comments.trim())) { 
            return "The proper comments field was not found in the form submitted.";
        } 
        
        return null;
    }
    
    /**
     * @param request
     * @return null if message not judged to be spam, otherwise a String
     * containing the reason the message was flagged as spam.
     */
    private String checkForSpam(String comments) {
    	
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
}
