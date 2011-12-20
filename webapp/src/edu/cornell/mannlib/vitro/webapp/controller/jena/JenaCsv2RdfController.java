/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.Actions;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.usepages.UseAdvancedDataToolsPages;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSpecialModelMaker;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;
import edu.cornell.mannlib.vitro.webapp.utils.Csv2Rdf;
import edu.cornell.mannlib.vitro.webapp.utils.jena.JenaIngestUtils;


public class JenaCsv2RdfController extends JenaIngestController {
	Log log = LogFactory.getLog( JenaCsv2RdfController.class );
    
    private static final String CSV2RDF_JSP = "/jenaIngest/csv2rdf.jsp";
	private static final String CSV2RDF_SELECT_URI_JSP = "/jenaIngest/csv2rdfSelectUri.jsp";
	private static int maxFileSizeInBytes = 1024 * 1024 * 2000; //2000mb 
	
	@Override
	public void doPost(HttpServletRequest rawRequest,
			HttpServletResponse response) throws ServletException, IOException {
        if (!isAuthorizedToDisplayPage(rawRequest, response, new Actions(new UseAdvancedDataToolsPages()))) {
        	return;
        }

		FileUploadServletRequest req = FileUploadServletRequest.parseRequest(rawRequest,
				maxFileSizeInBytes);
		if (req.hasFileUploadException()) {
			forwardToFileUploadError(req.getFileUploadException().getLocalizedMessage(), req, response);
			return;
		}

		VitroRequest request = new VitroRequest(req);		
		Map<String, List<FileItem>> fileStreams = req.getFiles();
		FileItem fileStream = fileStreams.get("filePath").get(0);
		String filePath = fileStreams.get("filePath").get(0).getName();
		
		String actionStr = request.getParameter("action");
		actionStr = (actionStr != null) ? actionStr : "";
		
		if ("csv2rdf".equals(actionStr)) {
			String csvUrl = request.getParameter("csvUrl");
			if (!csvUrl.isEmpty() || !filePath.isEmpty()) {
				String destinationModelNameStr = request.getParameter(
						"destinationModelName");
				Model csv2rdfResult = null;
				try{
    				csv2rdfResult = doExecuteCsv2Rdf(
    						request, fileStream, filePath);
				}catch(Exception ex){
				    forwardToFileUploadError(ex.getMessage(),req,response);
				    return;
				}
				ModelMaker maker = getVitroJenaModelMaker(request);
				Boolean csv2rdf = true;
				JenaIngestUtils utils = new JenaIngestUtils();
				List<Model> resultList = new ArrayList<Model>();
				resultList.add(csv2rdfResult);
				Map<String,LinkedList<String>> propertyMap = 
					    utils.generatePropertyMap(resultList, maker);
				request.setAttribute("propertyMap",propertyMap);
				request.setAttribute("csv2rdf", csv2rdf);
				request.setAttribute("destinationModelName", destinationModelNameStr);
				request.setAttribute("title","URI Select");
				request.setAttribute("bodyJsp", CSV2RDF_SELECT_URI_JSP);
			} else {
				request.setAttribute("title","Convert CSV to RDF");
				request.setAttribute("bodyJsp",CSV2RDF_JSP);
			}
		}
		

		RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);      
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+request.getAppBean().getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }		
		
    }
	
//	 private void forwardToFileUploadError( String errrorMsg , HttpServletRequest req, HttpServletResponse response) throws ServletException{
//         req.setAttribute("errors", errrorMsg);
//         RequestDispatcher rd = req.getRequestDispatcher("/jsp/fileUploadError.jsp");            
//         try {
//             rd.forward(req, response);
//         } catch (IOException e1) {
//             throw new ServletException(e1);
//         }            
//         return;
//     }

    private void forwardToFileUploadError(String errrorMsg,
            HttpServletRequest req, HttpServletResponse response)
            throws ServletException {
        VitroRequest vreq = new VitroRequest(req);
        req.setAttribute("title", "CSV to RDF Error ");
        req.setAttribute("bodyJsp", "/jsp/fileUploadError.jsp");
        req.setAttribute("errors", errrorMsg);

        RequestDispatcher rd = req.getRequestDispatcher(Controllers.BASIC_JSP);
        req.setAttribute("css",
                "<link rel=\"stylesheet\" type=\"text/css\" href=\""
                        + vreq.getAppBean().getThemeDir() + "css/edit.css\"/>");
        try {
            rd.forward(req, response);
        } catch (IOException e1) {
            log.error(e1);
            throw new ServletException(e1);
        }
        return;
    }

	 public Model doExecuteCsv2Rdf(VitroRequest vreq, FileItem fileStream, String filePath) throws Exception {
			char[] quoteChars = {'"'};
			String namespace = "";
			String tboxNamespace = vreq.getParameter("tboxNamespace");
			String typeName = vreq.getParameter("typeName");
			String csvUrl = vreq.getParameter("csvUrl");
			Model destination = null;
			String destinationModelNameStr = vreq.getParameter("destinationModelName");
			if (destinationModelNameStr != null && destinationModelNameStr.length()>0) {
				destination = getModel(destinationModelNameStr, vreq);
			}
			Model tboxDestination = null;
			String tboxDestinationModelNameStr = vreq.getParameter("tboxDestinationModelName");
			if (tboxDestinationModelNameStr != null && tboxDestinationModelNameStr.length()>0) {
				tboxDestination = getModel(tboxDestinationModelNameStr, vreq);
			}
			
			char separatorChar = ',';
			if ("tab".equalsIgnoreCase(vreq.getParameter("separatorChar"))) {
				separatorChar = '\t';
			}
			
			Csv2Rdf c2r = new Csv2Rdf(separatorChar, quoteChars,namespace,tboxNamespace,typeName);
			
			InputStream is = null;
			
			try {
				if(!csvUrl.isEmpty())
					is = new URL(csvUrl).openStream();
				else if(!filePath.isEmpty())
					is = fileStream.getInputStream();
					
			} catch (IOException e) {
				throw new Exception("Unable to access URL " + csvUrl);
			}
			
			Model[] models = null;
			
			try {
				 models = c2r.convertToRdf(
						 is, vreq.getWebappDaoFactory(), destination);
			} catch (IOException e) {
				throw new Exception(
						"Unable to convert " + csvUrl + " to RDF");
			}
			
			// TODO: rework this
			vreq.getSession().setAttribute("csv2rdfResult", models[0]);
			if (tboxDestination != null) {
				tboxDestination.add(models[1]);
			}	
			
			return models[0];
		}

}
