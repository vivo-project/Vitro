package edu.cornell.mannlib.vitro.webapp.visualization.coauthorship;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.hp.hpl.jena.query.DataSource;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationController;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.constants.QueryConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.utils.GenericQueryHandler;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.GenericQueryMap;

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

	public void generateVisualization(VisualizationController visualizationController, DataSource dataSource) {

		String resultFormatParam = "RS_TEXT";
        String rdfResultFormatParam = "RDF/XML-ABBREV";

        String egoURIParam = vitroRequest.getParameter(VisualizationFrameworkConstants.INDIVIDUAL_URI_URL_HANDLE);

        String renderMode = vitroRequest.getParameter(VisualizationFrameworkConstants.RENDER_MODE_URL_HANDLE);
        
        String visMode = vitroRequest.getParameter(VisualizationFrameworkConstants.VIS_MODE_URL_HANDLE);

        String visContainer = vitroRequest.getParameter(VisualizationFrameworkConstants.VIS_CONTAINER_URL_HANDLE);
        
        
        
        /*
		 * If the data being requested is about a standalone image, which is used when we want
		 * to render an image & other info for a co-author OR ego for that matter.
		 * */
		if (VisualizationFrameworkConstants.DATA_RENDER_MODE_URL_VALUE.equalsIgnoreCase(renderMode) 
				&& VisualizationFrameworkConstants.IMAGE_VIS_MODE_URL_VALUE.equalsIgnoreCase(visMode) ) {
			
			
			String filterRule = "?predicate = vitro:imageThumb";
			GenericQueryHandler imageQueryHandler = new GenericQueryHandler(egoURIParam, 
																			filterRule, 
																			resultFormatParam, 
																			rdfResultFormatParam, 
																			dataSource, 
																			log);
			
			try {
				
				GenericQueryMap imagePropertyToValues = imageQueryHandler.getJavaValueObjects();
				
				String imagePath = "";
				/*
				 * If there is no imageThumb property we want to give the link to "No Image" snap. 
				 * */
				if (imagePropertyToValues.size() > 0) {
					
					String vitroSparqlNamespace = QueryConstants.PREFIX_TO_NAMESPACE.get("vitro"); 
					String imageThumbProperty = vitroSparqlNamespace + "imageThumb";
					
					Set<String> personImageThumbPaths = imagePropertyToValues.get(imageThumbProperty);
					
					/*
					 * Although we know that there can be only one imagePath we are restricted by Java's
					 * expression power.
					 * */
					for (String providedImagePath : personImageThumbPaths) {
						imagePath = "/images/" + providedImagePath;
					}
					
					String imageServerPath = visualizationController.getServletContext().getRealPath(imagePath);
					
					File imageFile = new File(imageServerPath) ;
					
					if (imageFile == null) {
						
						Portal portal = vitroRequest.getPortal();
						String themeDir = portal != null ? portal.getThemeDir() : Portal.DEFAULT_THEME_DIR_FROM_CONTEXT;
						
						System.out.println("bfore cxtpth " + themeDir);
						
						themeDir = vitroRequest.getContextPath() + '/' + themeDir;
						
						System.out.println("bfore cxtpth " + themeDir);
						
						imagePath = themeDir + "site_icons/visualization/coauthorship/no_image.png";
						
						System.out.println(imagePath);
						
					}
					
					
				} else {
					
					Portal portal = vitroRequest.getPortal();
					String themeDir = portal != null ? portal.getThemeDir() : Portal.DEFAULT_THEME_DIR_FROM_CONTEXT;
					
					System.out.println("bfore cxtpth " + themeDir);
					
					themeDir = vitroRequest.getContextPath() + '/' + themeDir;
					
					System.out.println("bfore cxtpth " + themeDir);
					
					imagePath = themeDir + "site_icons/visualization/coauthorship/no_image.png";
					
					System.out.println(imagePath);
					
					
					
				}
				
				
			} catch (MalformedQueryParametersException e) {
				try {
					handleMalformedParameters(e.getMessage());
				} catch (ServletException e1) {
					log.error(e1.getStackTrace());
				} catch (IOException e1) {
					log.error(e1.getStackTrace());
				}
				return;
			}
			
		} 
        

        QueryHandler queryManager =
        	new QueryHandler(egoURIParam,
						     resultFormatParam,
						     rdfResultFormatParam,
						     dataSource,
						     
						     log);

		try {
			
			VisVOContainer authorNodesAndEdges = queryManager.getVisualizationJavaValueObjects();
			
	    	/*
	    	 * In order to avoid unneeded computations we have pushed this "if" condition up.
	    	 * This case arises when the render mode is data. In that case we dont want to generate 
	    	 * HTML code to render sparkline, tables etc. Ideally I would want to avoid this flow.
	    	 * It is ugly! 
	    	 * */
	    	if (VisualizationFrameworkConstants.DATA_RENDER_MODE_URL_VALUE.equalsIgnoreCase(renderMode)) { 
			
	    			/*
	    			 * When just the graphML file is required - based on which actual visualization will 
	    			 * be rendered.
	    			 * */
	    			prepareVisualizationQueryDataResponse(authorNodesAndEdges);
					return;
	    		
	    		
			}
	    	
	    	/*
	    	 * Computations required to generate HTML for the sparklines & related context.
	    	 * */
	    	
	    	/*
	    	 * This is required because when deciding the range of years over which the vis
	    	 * was rendered we dont want to be influenced by the "DEFAULT_PUBLICATION_YEAR".
	    	 * */
//	    	publishedYearsForCollege.remove(VOConstants.DEFAULT_PUBLICATION_YEAR);

	    	/*
	    	VisualizationCodeGenerator visualizationCodeGenerator = 
	    		new VisualizationCodeGenerator(yearToPublicationCount, log);
	    	
			String visContentCode = visualizationCodeGenerator
										.getMainVisualizationCode(authorDocuments,
															  	  publishedYears,
															  	  visMode,
															  	  visContainer);

			String visContextCode = visualizationCodeGenerator
										.getVisualizationContextCode(vitroRequest.getRequestURI(), 
																	 collegeURIParam,
																	 visMode);
																	 */

	    	/*
	    	 * This is side-effecting because the response of this method is just to redirect to
	    	 * a page with visualization on it.
	    	 * */
			
			RequestDispatcher requestDispatcher = null;

	    	prepareVisualizationQueryStandaloneResponse(egoURIParam, request, response, vitroRequest);

//		    	requestDispatcher = request.getRequestDispatcher(Controllers.BASIC_JSP);
		    	requestDispatcher = request.getRequestDispatcher("/templates/page/blankPage.jsp");

	    	try {
	            requestDispatcher.forward(request, response);
	        } catch (Exception e) {
	            log.error("EntityEditController could not forward to view.");
	            log.error(e.getMessage());
	            log.error(e.getStackTrace());
	        }

		} catch (MalformedQueryParametersException e) {
			try {
				handleMalformedParameters(e.getMessage());
			} catch (ServletException e1) {
				log.error(e1.getStackTrace());
			} catch (IOException e1) {
				log.error(e1.getStackTrace());
			}
			return;
		}

	}

	private void prepareVisualizationQueryDataResponse(VisVOContainer authorNodesAndEdges) {

		response.setContentType("text/xml");
		
		try {
		
		PrintWriter responseWriter = response.getWriter();
		
		/*
		 * We are side-effecting responseWriter since we are directly manipulating the response 
		 * object of the servlet.
		 * */
		
		CoAuthorshipGraphMLWriter coAuthorShipGraphMLWriter = new CoAuthorshipGraphMLWriter(authorNodesAndEdges);
		
		responseWriter.append(coAuthorShipGraphMLWriter.getCoAuthorshipGraphMLContent());
		
		responseWriter.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void prepareVisualizationQueryImageResponse(String imageURL) {

		response.setContentType("text/plain");
		
		try {
		
		PrintWriter responseWriter = response.getWriter();
		
		responseWriter.append(imageURL);
		
		responseWriter.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void prepareVisualizationQueryStandaloneResponse(String egoURIParam, 
															 HttpServletRequest request,
															 HttpServletResponse response, 
															 VitroRequest vreq) {

        Portal portal = vreq.getPortal();

//        request.setAttribute("visContentCode", visContentCode);
//        request.setAttribute("visContextCode", visContextCode);

        request.setAttribute("egoURIParam", egoURIParam);
        
        request.setAttribute("bodyJsp", "/templates/visualization/co_authorship.jsp");
        request.setAttribute("portalBean", portal);
//        request.setAttribute("title", "Individual Publication Count Visualization");
//        request.setAttribute("scripts", "/templates/visualization/visualization_scripts.jsp");

	}

	private void handleMalformedParameters(String errorMessage)
			throws ServletException, IOException {

		Portal portal = vitroRequest.getPortal();

		request.setAttribute("error", errorMessage);

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(Controllers.BASIC_JSP);
		request.setAttribute("bodyJsp", "/templates/visualization/visualization_error.jsp");
		request.setAttribute("portalBean", portal);
		request.setAttribute("title", "Visualization Query Error - Individual Publication Count");

		try {
			requestDispatcher.forward(request, response);
		} catch (Exception e) {
			log.error("EntityEditController could not forward to view.");
			log.error(e.getMessage());
			log.error(e.getStackTrace());
		}
	}

}
