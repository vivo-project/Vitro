package edu.cornell.mannlib.vitro.webapp.controller.edit;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */
import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.sql.*;
import java.util.*;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;

import edu.cornell.mannlib.vitro.webapp.beans.User;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.UserDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.LogoutEvent;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

public class Logout extends HttpServlet /*implements SingleThreadModel*/ {
	
	private static final Log log = LogFactory.getLog(Logout.class.getName());

    public void doPost( HttpServletRequest request, HttpServletResponse response ) {
        try {
        	
        	VitroRequest vreq = new VitroRequest(request);
        	HttpSession session = vreq.getSession();
        	if (session != null) {
	        	UserDao userDao = ((WebappDaoFactory)session.getServletContext().getAttribute("webappDaoFactory")).getUserDao();
	            LoginFormBean f = (LoginFormBean) session.getAttribute( "loginHandler" );
	            if (f != null) {
		            User user = userDao.getUserByUsername(f.getLoginName());
		            if (user == null) {
		            	log.error("Unable to retrieve user " + f.getLoginName() + " from model");
		            } else {
		            	Authenticate.sendLoginNotifyEvent( new LogoutEvent( user.getURI() ), getServletContext(), session );
		            }
	            }
	            session.invalidate();
        	}
            response.sendRedirect("./");
            
            /*
            LoginFormBean f = (LoginFormBean) session.getAttribute( "loginHandler" );
            //reset the login bean properties
            f.setLoginStatus( "logged out" );
            f.setLoginRole( "1" );
            // f.setLoginName( "" ); leave so users can see who they last logged in as
            f.setLoginPassword( "" );
            f.setErrorMsg( "loginPassword", "" ); // remove any error messages
            f.setErrorMsg( "loginUsername", "" ); // remove any error messages
            f.setEmailAddress( "reset" );
            f.setSessionId( "" );
            
            // VitroRequestPrep.forceOutOfCuratorEditing(request);
            //CuratorEditingUriFactory.clearFakeIdInSession( session );    
			*/
            
        } catch (Exception ex) {
            log.error( ex.getMessage() );
            ex.printStackTrace();
        }
    }
    
    public void doGet( HttpServletRequest request, HttpServletResponse response ) {
    	doPost(request, response);
    }
}

