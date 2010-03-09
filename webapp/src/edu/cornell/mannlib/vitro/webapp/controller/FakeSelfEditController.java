/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.util.Enumeration;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;
//import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.FakeSelfEditingIdentifierFactory;

public class FakeSelfEditController extends VitroHttpServlet {
	
	private static final Log log = LogFactory.getLog(FakeSelfEditController.class.getName());
	
    public void doGet( HttpServletRequest request, HttpServletResponse response )
    	throws IOException, ServletException {
        
//        if (!checkLoginStatus(request, response)) {
//            return;
//        }
        String redirectUrl = null;
        
        try {
            super.doGet(request,response);
            VitroRequest vreq = new VitroRequest(request);
            HttpSession session = request.getSession(true);
            
            Object obj = vreq.getSession().getAttribute("loginHandler");        
            LoginFormBean loginHandler = null;
            if( obj != null && obj instanceof LoginFormBean )
                loginHandler = ((LoginFormBean)obj);
            if( loginHandler == null ||
                ! "authenticated".equalsIgnoreCase(loginHandler.getLoginStatus()) ||
                 Integer.parseInt(loginHandler.getLoginRole()) <= LoginFormBean.CURATOR ){       
      
                session.setAttribute("postLoginRequest",
                        vreq.getRequestURI()); //+( vreq.getQueryString()!=null?('?' + vreq.getQueryString()):"" ));
                redirectUrl=request.getContextPath() + Controllers.LOGIN + "?login=block";
                response.sendRedirect(redirectUrl);
                return;
            }
            
            String netid = null;
            String msg = null;

            // Form to use netid submitted 
            if(  vreq.getParameter("force") != null ){        
                VitroRequestPrep.forceToSelfEditing(request);
                netid = request.getParameter("netid");
                msg = "You are using the netid '" + netid + "' to test self-editing."
;                FakeSelfEditingIdentifierFactory.clearFakeIdInSession( session );
                FakeSelfEditingIdentifierFactory.putFakeIdInSession( netid , session );
                //redirectUrl = request.getContextPath() + "/admin/fakeselfedit" + "?netid=" + netid;        
            }
            else {
                // Form to stop using netid submitted
                if ( request.getParameter("stopfaking") != null){
                    VitroRequestPrep.forceOutOfSelfEditing(request);
                    FakeSelfEditingIdentifierFactory.clearFakeIdInSession( session );
                }    
                netid = (String)session.getAttribute(FakeSelfEditingIdentifierFactory.FAKE_SELF_EDIT_NETID);
                msg = "You have not configured a netid to test self-editing.";
                if( netid != null ) {
                    msg = "You are testing self-editing as '" + netid + "'.";
                    //redirectUrl = request.getContextPath() + "/admin/fakeselfedit" + "?netid=" + netid;
                }
                else {
                    netid = "";
                }
            }

            if (redirectUrl != null) {
                response.sendRedirect(redirectUrl);
                return;                
            }
            
            request.setAttribute("msg", msg);
            request.setAttribute("netid", netid); 
            
            request.setAttribute("title", "Self-Edit Test");
            request.setAttribute("bodyJsp", "/admin/fakeselfedit.jsp");
            
            RequestDispatcher rd =
                request.getRequestDispatcher(Controllers.BASIC_JSP);
            rd.forward(request, response);

        } catch (Throwable e) {
            log.error("FakeSelfEditController could not forward to view.");
            log.error(e.getMessage());
            log.error(e.getStackTrace());
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    	doGet(request, response);
    }


}
