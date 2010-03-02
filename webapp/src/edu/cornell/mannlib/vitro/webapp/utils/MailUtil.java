/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.List;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import fedora.client.FedoraClient;

public class MailUtil {
	 	private String smtpHost = null;
        public MailUtil(){
        	smtpHost = getSmtpHostFromProperties();
        }
        
        public void sendMessage(String messageText, String subject, String from, String to, List<String> deliverToArray) throws IOException{
        	Properties props = System.getProperties();
            props.put("mail.smtp.host", smtpHost);
            Session s = Session.getDefaultInstance(props,null);
            
            try{
            	
            	int recipientCount = (deliverToArray == null) ? 0 : deliverToArray.size();
                if (recipientCount == 0) {
                    //log.error("recipientCount is 0 when DeliveryType specified as \""+formType+"\"");
                    throw new Error(
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
            	System.out.println("Exception sending message :"  + ex.getMessage());
            }
        }
        
        public boolean isSmtpHostConfigured() {
            if( smtpHost==null || smtpHost.equals("")) {
                return false;
            }
            return true;
        }

    	private String getSmtpHostFromProperties() {
    		String host = ConfigurationProperties.getProperty("Vitro.smtpHost");
    		if (host != null && !host.equals("")) {
    			//System.out.println("Found Vitro.smtpHost value is " + host);
    			//LOG.info("Found Vitro.smtpHost value of " + host);
    		} else {
    			System.out.println("No Vitro.smtpHost specified");
    		}
    		return (host != null && host.length() > 0) ? host : null;
    	}
}
