/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;

public class MailUsersServlet extends VitroHttpServlet {
	private static final Log log = LogFactory.getLog(MailUsersServlet.class);
	
    public static HttpServletRequest request;
    public static HttpServletRequest response;

    @Override
	public void doGet( HttpServletRequest request, HttpServletResponse response )
        throws ServletException, IOException {
        VitroRequest vreq = new VitroRequest(request);

        String   confirmpage    = "/confirmUserMail.jsp";
        String   errpage        = "/contact_err.jsp";
        String status = null; // holds the error status
        
        if (!FreemarkerEmailFactory.isConfigured(vreq)) {
			status = "This application has not yet been configured to send mail. "
					+ "Email properties must be specified in the configuration properties file.";
            response.sendRedirect( "test?bodyJsp=" + errpage + "&ERR=" + status );
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
            //Review how spam works?
            /*if (referer.indexOf("comments")<0 && referer.indexOf("correction")<0) {
                probablySpam=true;
                status = SPAM_MESSAGE ;
                spamReason = "The form was not submitted from the Contact Us or Corrections page.";
            }*/
        }

        String formType = vreq.getParameter("DeliveryType");
        List<String> deliverToArray = null;
        int recipientCount = 0;
        String deliveryfrom = null;
        
        // get Individuals that the User mayEditAs
        deliverToArray = getEmailsForAllUserAccounts(vreq);
        
        //Removed all form type stuff b/c recipients pre-configured
        recipientCount=(deliverToArray == null) ? 0 : deliverToArray.size();
        
        if (recipientCount == 0) {
            //log.error("recipientCount is 0 when DeliveryType specified as \""+formType+"\"");
            throw new Error(
                    "To establish the Contact Us mail capability the system administrators must  "
                    + "specify at least one email address in the current portal.");
        }

        // obtain passed in form data with a simple trim on the values
        String   webusername    = vreq.getParameter("webusername");// Null.trim(); will give you an exception
        String   webuseremail   = vreq.getParameter("webuseremail");//.trim();
        String   comments       = vreq.getParameter("s34gfd88p9x1"); //what does this string signify?
        //webusername = "hjk54";
        //webuseremail = "hjk54@cornell.edu";
        //comments = "following are comments";
        
       webusername=webusername.trim();
       deliveryfrom = webuseremail; 
       comments=comments.trim();
        //Removed spam filtering code

        StringBuffer msgBuf = new StringBuffer(); // contains the intro copy for the body of the email message
        String lineSeparator = System.getProperty("line.separator"); // \r\n on windows, \n on unix
        // from MyLibrary
        msgBuf.setLength(0);
        //msgBuf.append("Content-Type: text/html; charset='us-ascii'" + lineSeparator);
        msgBuf.append("<html>" + lineSeparator );
        msgBuf.append("<head>" + lineSeparator );
        msgBuf.append("<style>a {text-decoration: none}</style>" + lineSeparator );
        msgBuf.append("<title>" + deliveryfrom + "</title>" + lineSeparator );
        msgBuf.append("</head>" + lineSeparator );
        msgBuf.append("<body>" + lineSeparator );
        msgBuf.append("<h4>" + deliveryfrom + "</h4>" + lineSeparator );
        msgBuf.append("<h4>From: "+webusername +" (" + webuseremail + ")"+" at IP address "+request.getRemoteAddr()+"</h4>"+lineSeparator);

        //Don't need any 'likely viewing page' portion to be emailed out to the others

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
        //PrintWriter outFile = new PrintWriter (new FileWriter(request.getSession().getServletContext().getRealPath("/WEB-INF/LatestMessage.html"),true)); //autoflush

        Calendar cal = Calendar.getInstance();

       /* outFile.println("<hr/>");
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
		*/

        Session s = FreemarkerEmailFactory.getEmailSession(vreq);
        //s.setDebug(true);
        try {
            // Construct the message
            MimeMessage msg = new MimeMessage( s );
            log.debug("trying to send message from servlet");

            // Set the from address
            msg.setFrom( new InternetAddress( webuseremail ));

            // Set the recipient address
            
            if (recipientCount>0){
                InternetAddress[] address=new InternetAddress[recipientCount];
                for (int i=0; i<recipientCount; i++){
                    address[i] = new InternetAddress(deliverToArray.get(i));
                }
                msg.setRecipients( Message.RecipientType.TO, address );
            }

            // Set the subject and text
            msg.setSubject( deliveryfrom );

            // add the multipart to the message
            msg.setContent(msgText,"text/html");

            // set the Date: header
            msg.setSentDate( new Date() );

            log.debug("sending from servlet");

        //if (!probablySpam)
            Transport.send( msg ); // try to send the message via smtp - catch error exceptions


        } catch (AddressException e) {
            status = "Please supply a valid email address.";
            log.debug("Error - status is " + status);
        } catch (SendFailedException e) {
            status = "The system was unable to deliver your mail.  Please try again later.  [SEND FAILED]";
            log.error("Error - status is " + status);
        } catch (MessagingException e) {
            status = "The system was unable to deliver your mail.  Please try again later.  [MESSAGING]";
            log.error("Error - status is " + status, e);
        }

        //outFile.flush();
        //outFile.close();

        // Redirect to the appropriate confirmation page
        if (status == null && !probablySpam) {
            // message was sent successfully
            response.sendRedirect( "test?bodyJsp=" + confirmpage );
        } else {
            // exception occurred
            response.sendRedirect( "test?bodyJsp=" + errpage + "&ERR=" + status );
        }

    }
    
	private List<String> getEmailsForAllUserAccounts(VitroRequest vreq) {
		UserAccountsDao uaDao = vreq.getFullWebappDaoFactory()
				.getUserAccountsDao();
		
		List<String> emails = new ArrayList<String>();
		for (UserAccount user : uaDao.getAllUserAccounts()) {
			emails.add(user.getEmailAddress());
		}

		return emails;
	}

    @Override
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
