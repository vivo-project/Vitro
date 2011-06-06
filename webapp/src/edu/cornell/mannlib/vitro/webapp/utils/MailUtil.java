/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;

public class MailUtil {
	private static final Log log = LogFactory.getLog(MailUtil.class);
	private final HttpServletRequest req;
	
		public MailUtil(HttpServletRequest req) {
			this.req = req;
		}
        
        public void sendMessage(String messageText, String subject, String from, String to, List<String> deliverToArray) throws IOException{
        	
            Session s = FreemarkerEmailFactory.getEmailSession(req);
            
            try{
            	
            	int recipientCount = (deliverToArray == null) ? 0 : deliverToArray.size();
                if (recipientCount == 0) {
                    log.error(
                            "To establish the Contact Us mail capability the system administrators must  "
                            + "specify at least one email address in the current portal.");
                }
               
                MimeMessage msg = new MimeMessage( s );
                // Set the from address
                msg.setFrom( new InternetAddress( from ));

	            // Set the recipient address
	            
	            if (recipientCount>0){
	                InternetAddress[] address=new InternetAddress[recipientCount];
	                for (int i=0; i<recipientCount; i++){
	                    address[i] = new InternetAddress(deliverToArray.get(i));
	                }
	                msg.setRecipients( Message.RecipientType.TO, address );
	            }

	            // Set the subject and text
	            msg.setSubject( subject );

	            // add the multipart to the message
	            msg.setContent(messageText,"text/html");

	            // set the Date: header
	            msg.setSentDate( new Date() );
	            Transport.send( msg ); // try to send the message via smtp - catch error exceptions
            } catch(Exception ex) {
            	log.error("Exception sending message :"  + ex.getMessage(), ex);
            }
        }
        
}
