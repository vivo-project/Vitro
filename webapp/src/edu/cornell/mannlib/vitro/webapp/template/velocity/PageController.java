package edu.cornell.mannlib.vitro.webapp.template.velocity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class PageController extends VitroHttpServlet {

	private static final Log log = LogFactory.getLog(PageController.class.getName());
	
	protected VitroRequest vreq;
	protected PrintWriter out;
	protected Portal portal;
	protected VelocityContext context;
	protected ServletContext servletContext;
	
    public void doGet( HttpServletRequest request, HttpServletResponse response )
		throws IOException, ServletException {
    	try {
	        super.doGet(request,response);
	        
	        out = response.getWriter();
	        vreq = new VitroRequest(request);
	        portal = vreq.getPortal();
	        
	        servletContext = getServletContext();
	        String templatePath = servletContext.getRealPath("/templates/velocity");
	        String templateName = "page.vm";
	        
	        Properties p = new Properties();
	        p.setProperty("file.resource.loader.path", templatePath);
	        Velocity.init(p);
	        
	        // RY This might need to be an instance variable, as in StringTemplate version
	        context = new VelocityContext();           
	        context.put("title", getTitle());
	        //context.put("aboutText", portal.getAboutText());
	        //context.put("acknowledgeText", portal.getAcknowledgeText());
	
	        Template template = null;
	        try {
	        	template = Velocity.getTemplate(templateName);
	
	        }
	        catch (ResourceNotFoundException e) {
	        	System.out.println("Can't find template " + templateName);
	        }
	        catch (ParseErrorException e) {
	        	System.out.println("Problem parsing template " + templateName);
	        }           	
	        catch (MethodInvocationException e) {
	        	System.out.println("Method invocation exception in template " + templateName);
	        }
	
	        StringWriter sw = new StringWriter();
	        if (template != null) {
	        	template.merge(context, sw);
	        	out.print(sw);
	        }
	        
	    } catch (Throwable e) {
	        log.error("AboutControllerVelocity could not forward to view.");
	        log.error(e.getMessage());
	        log.error(e.getStackTrace());
	    }
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
		doGet(request, response);
	}
	
    protected String getTitle() { 
    	return null; 
    }
	
}

