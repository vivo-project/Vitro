package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;

@WebServlet(name = "RESTEndpoint", urlPatterns = { "/api/rest/*" })
public class RESTEndpoint extends VitroHttpServlet  {

	private static final long serialVersionUID = 1L;
 	private static final Log log = LogFactory.getLog(RESTEndpoint.class);
	private ResourcePool resourcePool = ResourcePool.getInstance();
 	
	
	@Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (request.getMethod().equalsIgnoreCase("PATCH")){
       doPatch(request, response);
    } else {
        super.service(request, response);
    }
	}
	
	@Override
	public void doPost( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	@Override
	public void doGet( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	@Override
	public void doDelete( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	@Override
	public void doPut( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	public void doPatch( HttpServletRequest request, HttpServletResponse response ) {
		process(request, response);
	}
	
	private void process(HttpServletRequest request, HttpServletResponse response) {
		String requestURL = request.getRequestURI();
		//String resourceName = requestURL.substring(requestURL.lastIndexOf("/") + 1 );
		//Resource resource = resourcePool.getByName(resourceName);
		log.debug(request.getMethod());
	}
	
}
