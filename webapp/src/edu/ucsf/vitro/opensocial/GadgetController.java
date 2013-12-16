package edu.ucsf.vitro.opensocial;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ExceptionResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;

public class GadgetController extends FreemarkerHttpServlet {
	
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(GadgetController.class);
	
    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
    	if ("/clearcache".equalsIgnoreCase(vreq.getPathInfo())) {
    		OpenSocialManager.clearCache();
    		return new RedirectResponseValues("/");
    	}
    	else if ("/sandbox".equalsIgnoreCase(vreq.getPathInfo())) {
    		boolean sandbox = "True".equalsIgnoreCase(ConfigurationProperties.getBean(vreq.getSession()
    				.getServletContext()).getProperty("OpenSocial.sandbox"));
    		if (!sandbox) {
    			return new ExceptionResponseValues( new Exception("Sandbox not available"));
    		}
    		return processGadgetSandbox(vreq);
    	}
    	else {
    		return processGadgetDetails(vreq);
    	}
    }    	

    protected ResponseValues processGadgetDetails(VitroRequest vreq) {
    	try {
	        Map<String, Object> body = new HashMap<String, Object>();

            body.put("title", "Gadget Details");            
	        // VIVO OpenSocial Extension by UCSF
	        try {
		        OpenSocialManager openSocialManager = new OpenSocialManager(vreq, "gadgetDetails");
		        body.put(OpenSocialManager.TAG_NAME, openSocialManager);
		        if (openSocialManager.isVisible()) {
		        	body.put("bodyOnload", "my.init();");
		        }
	        } catch (IOException e) {
	            log.error("IOException in doTemplate()", e);
	        } catch (SQLException e) {
	            log.error("SQLException in doTemplate()", e);
	        }	               
	        
	        return new TemplateResponseValues("gadgetDetails.ftl", body);
        
	    } catch (Throwable e) {
	        log.error(e, e);
	        return new ExceptionResponseValues(e);
	    }
    }
    
    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
    	return "Gadget Details";
    }    

    protected ResponseValues processGadgetSandbox(VitroRequest vreq) {
    	if ("POST".equalsIgnoreCase(vreq.getMethod())) {
    		vreq.getSession().setAttribute(OpenSocialManager.OPENSOCIAL_GADGETS, vreq.getParameter("gadgetURLS"));
			vreq.getSession().setAttribute(OpenSocialManager.OPENSOCIAL_DEBUG, vreq.getParameter("debug") != null);
			vreq.getSession().setAttribute(OpenSocialManager.OPENSOCIAL_NOCACHE, vreq.getParameter("useCache") == null);
			return new RedirectResponseValues("/");
    	}
    	
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title", "Gadget Sandbox");            
        
        try {
	        OpenSocialManager openSocialManager = new OpenSocialManager(vreq, "gadgetSandbox");
	        String gadgetURLS = "";
	        for (GadgetSpec gadget : openSocialManager.getAllDBGadgets(false).values())
	        {
	            gadgetURLS += gadget.getGadgetURL() + System.getProperty("line.separator");
	        }
	        body.put("gadgetURLS", gadgetURLS);
	        body.put(OpenSocialManager.TAG_NAME, openSocialManager);
        } catch (IOException e) {
            log.error("IOException in doTemplate()", e);
        } catch (SQLException e) {
            log.error("SQLException in doTemplate()", e);
        }	               
	        
        
        return new TemplateResponseValues("gadgetLogin.ftl", body);
    }

}
