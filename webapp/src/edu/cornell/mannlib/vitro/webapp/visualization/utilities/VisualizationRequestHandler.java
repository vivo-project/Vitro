package edu.cornell.mannlib.vitro.webapp.visualization.utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.hp.hpl.jena.query.DataSource;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationController;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationFrameworkConstants;

public class VisualizationRequestHandler {
	
	private VitroRequest vitroRequest;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private Log log;


	
	public VisualizationRequestHandler(VitroRequest vitroRequest,
			HttpServletRequest request, HttpServletResponse response, Log log) {

		this.vitroRequest = vitroRequest;
		this.request = request;
		this.response = response;
		this.log = log;

	}

	public void generateVisualization(DataSource dataSource) {

        String individualURIParam = vitroRequest.getParameter(VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE);

        String visMode = vitroRequest.getParameter(VisualizationFrameworkConstants.VIS_MODE_URL_HANDLE);
        
        String profileVisMode = "PROFILE_URL";
        
        String coAuthorVisMode = "COAUTHORSHIP_URL";

        String preparedURL = "";

        try {
        
	    	/*
	    	 * By default we will be generating profile url else some specific url like coAuthorShip vis 
	    	 * url for that individual.
	    	 * */
			if (coAuthorVisMode.equalsIgnoreCase(visMode)) {
				
				preparedURL += request.getContextPath()
								+ "/admin/visQuery"
								+ "?" 
								+ VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE 
								+ "=" + URLEncoder.encode(individualURIParam, 
						 				 VisualizationController.URL_ENCODING_SCHEME).toString()
						 	    + "&"
			 				    + VisualizationFrameworkConstants.VIS_TYPE_URL_HANDLE 
								+ "=" + URLEncoder.encode("coauthorship", 
						 				 VisualizationController.URL_ENCODING_SCHEME).toString()
			 				    + "&"
			 				    + VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE
								+ "=" + URLEncoder.encode(VisualizationFrameworkConstants.STANDALONE_RENDER_MODE_URL_VALUE, 
						 				 VisualizationController.URL_ENCODING_SCHEME).toString();
				

				prepareVisualizationQueryResponse(preparedURL);
				return;

			} else {
				
				preparedURL += request.getContextPath()
								+ "/individual"
								+ "?" 
								+ VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE 
								+ "=" + URLEncoder.encode(individualURIParam, 
										 VisualizationController.URL_ENCODING_SCHEME).toString();
				
				prepareVisualizationQueryResponse(preparedURL);
				return;
	
			}
			
        } catch (UnsupportedEncodingException e) {
			log.error(e.getLocalizedMessage());
		}

	}

	private void prepareVisualizationQueryResponse(String preparedURL) {

		response.setContentType("text/plain");
		
		try {
		
		PrintWriter responseWriter = response.getWriter();
		
		responseWriter.append(preparedURL);
		
		responseWriter.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
