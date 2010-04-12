/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;


import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;

public class ContactMailServlet extends VitroHttpServlet {
	private static final Logger LOG = Logger.getLogger(ContactMailServlet.class);
	
    public static HttpServletRequest request;
    public static HttpServletRequest response;
    private static String smtpHost = null;
    private static final Log log = LogFactory.getLog(ContactMailServlet.class.getName());

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
			LOG.debug("Found Vitro.smtpHost value of " + host);
		} else {
			LOG.debug("No Vitro.smtpHost specified");
		}
		return (host != null && host.length() > 0) ? host : null;
	}
    
    public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException {
        VitroRequest vreq = new VitroRequest(request);
        Portal portal = vreq.getPortal();

        String   confirmpage    = "/thankyou.jsp";
        String   errpage        = "/contact_err.jsp";
        String status = null; // holds the error status
        
        if (smtpHost==null || smtpHost.equals("")){
            status = "This application has not yet been configured to send mail " +
            		"-- smtp host has not been identified in the Configuration Properties file.";
            response.sendRedirect( "test?bodyJsp=" + errpage + "&ERR=" + status + "&home=" + portal.getPortalId() );
            return;
        }

        String SPAM_MESSAGE = "Your message was flagged as spam.";

        boolean probablySpam = false;
        String spamReason = "";

        String originalReferer = (String) request.getSession().getAttribute("commentsFormReferer");
        request.getSession().removeAttribute("commentsFormReferer");
        if (originalReferer == null) {
            originalReferer = "none";
            // (the following does not support cookie-less browsing:)
            // probablySpam = true;
            // status = SPAM_MESSAGE;
        } else {
            String referer = request.getHeader("Referer");
            if (referer == null || 
            		(referer.indexOf("comments") <0 
            		  && referer.indexOf("correction") <0) ) {
                probablySpam=true;
                status = SPAM_MESSAGE ;
                spamReason = "The form was not submitted from the Contact Us or Corrections page.";
            }
        }

        String formType = vreq.getParameter("DeliveryType");
        String[] deliverToArray = null;
        int recipientCount = 0;
        String deliveryfrom = null;

        if ("comment".equals(formType)) {
            if (portal.getContactMail() == null || portal.getContactMail().trim().length()==0) {
                log.error("No contact mail address defined in current portal "+portal.getPortalId());
                throw new Error(
                        "To establish the Contact Us mail capability the system administrators must  "
                        + "specify an email address in the current portal.");
            } else {
                deliverToArray = portal.getContactMail().split(",");
            }
            deliveryfrom   = "Message from the "+portal.getAppName()+" Contact Form (ARMANN-nospam)";
        } else if ("correction".equals(formType)) {
            if (portal.getCorrectionMail() == null || portal.getCorrectionMail().trim().length()==0) {
                log.error("Expecting one or more correction email addresses to be specified in current portal "+portal.getPortalId()+"; will attempt to use contact mail address");
                if (portal.getContactMail() == null || portal.getContactMail().trim().length()==0) {
                    log.error("No contact mail address or correction mail address defined in current portal "+portal.getPortalId());
                } else {
                    deliverToArray = portal.getContactMail().split(",");
                }
            } else {
                deliverToArray = portal.getCorrectionMail().split(",");
            }
            deliveryfrom   = "Message from the "+portal.getAppName()+" Correction Form (ARMANN-nospam)";
        } else {
            deliverToArray = portal.getContactMail().split(",");
            status = SPAM_MESSAGE ;
            spamReason = "The form specifies no delivery type.";
        }
        recipientCount=(deliverToArray == null) ? 0 : deliverToArray.length;
        if (recipientCount == 0) {
            log.error("recipientCount is 0 when DeliveryType specified as \""+formType+"\"");
            throw new Error(
                    "To establish the Contact Us mail capability the system administrators must  "
                    + "specify at least one email address in the current portal.");
        }

        // obtain passed in form data with a simple trim on the values
        String   webusername    = vreq.getParameter("webusername");// Null.trim(); will give you an exception
        String   webuseremail   = vreq.getParameter("webuseremail");//.trim();
        String   comments       = vreq.getParameter("s34gfd88p9x1");

        if( webusername == null || "".equals(webusername) ){
            probablySpam=true;
            status = SPAM_MESSAGE;
            spamReason = "A proper webusername field was not found in the form submitted.";
            webusername="";
        } else
            webusername=webusername.trim();

        if( webuseremail == null || "".equals(webuseremail) ){
            probablySpam=true;
            status = SPAM_MESSAGE;
            spamReason = "A proper webuser email field was not found in the form submitted.";
            webuseremail="";
        } else
            webuseremail=webuseremail.trim();

        if (comments==null || comments.equals("")) { // to avoid error messages in log due to null comments String
            probablySpam=true;
            status = SPAM_MESSAGE;
            spamReason = "The proper comments field was not found in the form submitted.";
        } else {
            comments=comments.trim();
        }


        /* *************************** The following chunk code is for blocking specific types of spam messages.  It should be removed from a more generalized codebase. */

        /* if this blog markup is found, treat comment as blog spam */
        if (!probablySpam
            && (comments.indexOf("[/url]") > -1
            || comments.indexOf("[/URL]") > -1
            || comments.indexOf("[url=") > -1
            || comments.indexOf("[URL=") > -1)) {
            probablySpam = true;
            status = SPAM_MESSAGE;
            spamReason = "The message contained blog link markup.";
        }

        /* if message is absurdly short, treat as blog spam */
        if (!probablySpam && comments.length()<15) {
            probablySpam=true;
            status = SPAM_MESSAGE;
            spamReason="The message was too short.";
        }

        /* if contact form was requested directly, and message contains 'phentermine', treat as spam */
        if (!probablySpam && originalReferer.equals("none") && (comments.indexOf("phentermine")>-1 || comments.indexOf("Phentermine")>-1)) {
            probablySpam=true;
            status = SPAM_MESSAGE;
            spamReason = "The comments form was requested directly, and the message contains the word 'Phentermine.'";
        }

        /* ************************** end spam filtering code */

        StringBuffer msgBuf = new StringBuffer(); // contains the intro copy for the body of the email message
        String lineSeparator = System.getProperty("line.separator"); // \r\n on windows, \n on unix
        // from MyLibrary
        msgBuf.setLength(0);
        msgBuf.append("Content-Type: text/html; charset='us-ascii'" + lineSeparator);
        msgBuf.append("<html>" + lineSeparator );
        msgBuf.append("<head>" + lineSeparator );
        msgBuf.append("<style>a {text-decoration: none}</style>" + lineSeparator );
        msgBuf.append("<title>" + deliveryfrom + "</title>" + lineSeparator );
        msgBuf.append("</head>" + lineSeparator );
        msgBuf.append("<body>" + lineSeparator );
        msgBuf.append("<h4>" + deliveryfrom + "</h4>" + lineSeparator );
        msgBuf.append("<h4>From: "+webusername +" (" + webuseremail + ")"+" at IP address "+request.getRemoteAddr()+"</h4>"+lineSeparator);

        if (!(originalReferer == null || originalReferer.equals("none"))){
            //The spam filter that is being used by the listsrv is rejecting <a href="...
            //so try with out the markup, if that sill doesn't work,
            //uncomment the following line to strip the http://
            //msgBuf.append("<p><i>likely viewing page " + stripProtocol(originalReferer) );
            msgBuf.append("<p><i>likely viewing page " + originalReferer );
        }

        msgBuf.append(lineSeparator + "</i></p><h3>Comments:</h3>" + lineSeparator );
        if (comments==null || comments.equals("")) {
            msgBuf.append("<p>BLANK MESSAGE</p>");
        } else {
            msgBuf.append("<p>"+comments+"</p>");
        }
        msgBuf.append("</body>" + lineSeparator );
        msgBuf.append("</html>" + lineSeparator );

        String msgText = msgBuf.toString();
        // debugging
        PrintWriter outFile = new PrintWriter (new FileWriter(request.getSession().getServletContext().getRealPath("/WEB-INF/LatestMessage.html"),true)); //autoflush

        Calendar cal = Calendar.getInstance();

        outFile.println("<hr/>");
        outFile.println();
        outFile.println("<p>"+cal.getTime()+"</p>");
        outFile.println();
        if (probablySpam) {
            outFile.println("<p>REJECTED - SPAM</p>");
            outFile.println("<p>"+spamReason+"</p>");
            outFile.println();
        }
        outFile.print( msgText );
        outFile.println();
        outFile.println();
        outFile.flush();
        // outFile.close();

        // Set the smtp host
        Properties props = System.getProperties();
        props.put("mail.smtp.host", smtpHost);
        Session s = Session.getDefaultInstance(props,null); // was Session.getInstance(props,null);
        //s.setDebug(true);
        try {
            // Construct the message
            MimeMessage msg = new MimeMessage( s );
            //System.out.println("trying to send message from servlet");

            // Set the from address
            msg.setFrom( new InternetAddress( webuseremail ));

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

            //System.out.println("sending from servlet");

        if (!probablySpam)
            Transport.send( msg ); // try to send the message via smtp - catch error exceptions


        } catch (AddressException e) {
            status = "Please supply a valid email address.";
            outFile.println( status );
            outFile.println( e.getMessage() );
        } catch (SendFailedException e) {
            status = "The system was unable to deliver your mail.  Please try again later.  [SEND FAILED]";
            outFile.println( status );
            outFile.println( e.getMessage() );
        } catch (MessagingException e) {
            status = "The system was unable to deliver your mail.  Please try again later.  [MESSAGING]";
            outFile.println( status );
            outFile.println( e.getMessage() );
            e.printStackTrace();
        }

        outFile.flush();
        outFile.close();

        // Redirect to the appropriate confirmation page
        if (status == null && !probablySpam) {
            // message was sent successfully
            response.sendRedirect( "test?bodyJsp=" + confirmpage + "&home=" + portal.getPortalId() );
        } else {
            // exception occurred
            response.sendRedirect( "test?bodyJsp=" + errpage + "&ERR=" + status + "&home=" + portal.getPortalId() );
        }

    }

    public void doPost( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException
    {
        doGet( request, response );
    }

    /** Intended to mangle url so it can get through spam filtering
     *    http://host/dir/servlet?param=value ->  host: dir/servlet?param=value */
    public String stripProtocol( String in ){
        if( in == null )
            return "";
        else
            return in.replaceAll("http://", "host: " );
    }
}
