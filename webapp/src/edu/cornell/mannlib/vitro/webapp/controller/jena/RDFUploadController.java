/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

public class RDFUploadController extends BaseEditController {
	
    private static int maxFileSizeInBytes = 1024 * 1024 * 100; //100mb 
	
	public void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException {
        boolean isMultipart = ServletFileUpload.isMultipartContent(req);        
        if (!isMultipart) {
            // TODO: forward to error message
            throw new ServletException("Must POST a multipart encoded request");
        }
        
        /* The post parameters seem to get consumed by the parsing so we have to make a copy. */
        Map<String, List<String>> queryParameters = new HashMap<String, List<String>>();
        Map<String, List<FileItem>> fileStreams = new HashMap<String, List<FileItem>>();

        Iterator<FileItem> iter;
        try {
            iter = getFileItemIterator(req);
        } catch (FileUploadException e) {             
            forwardToFileUploadError(e.getLocalizedMessage(), req, response);
            return;
        }

        // get files or parameter values
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            String name = item.getFieldName();
            if (item.isFormField()) {
                if (queryParameters.containsKey(name)) {                    
                    try {
                        String value = item.getString("UTF-8");
                        queryParameters.get(name).add(value);
                    } catch (UnsupportedEncodingException e) {
                        forwardToFileUploadError(e.getLocalizedMessage(), req, response);
                        return;
                    }                    
                } else {
                    List<String> valueList = new ArrayList<String>(1);                    
                    try {
                        String value = item.getString("UTF-8");
                        valueList.add(value);
                    } catch (UnsupportedEncodingException e) {
                        forwardToFileUploadError(e.getLocalizedMessage(), req, response);
                        return;
                    }                    
                    queryParameters.put(name, valueList);
                }
            } else {
                if (fileStreams.containsKey(name)) {
                    fileStreams.get(name).add(item);
                } else {
                    List<FileItem> itemList = new ArrayList<FileItem>();
                    itemList.add(item);
                    fileStreams.put(name, itemList);
                }
            }
        }
	    
		VitroRequest request = new VitroRequest(req);		
		if (!checkLoginStatus(request,response) ){
		    try {
                response.sendRedirect(getDefaultLandingPage(request));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		    return;
		}		
		
		LoginFormBean loginBean = (LoginFormBean) request.getSession().getAttribute("loginHandler");
		
		try {
			super.doGet(request,response);
		} catch (Exception e) {
		    forwardToFileUploadError(e.getLocalizedMessage(), req, response);
			return;
		}
				
		Portal currentPortal = request.getPortal();		
	    			   
		boolean remove = isRemoveRequest(queryParameters);
		String verb = remove?"Removed":"Added";
		
		String languageStr = getLanguage(queryParameters);
		
		boolean makeClassgroups = (queryParameters.get("makeClassgroups") != null) ? true : false;
		
		int[] portalArray = null;
		String individualCheckIn = getCheckIn(queryParameters); 
		if (individualCheckIn != null) {
		    if (individualCheckIn.equals("current")) {
		        portalArray = new int[1];
		        portalArray[0] = currentPortal.getPortalId();
		    } else if (individualCheckIn.equals("all")) {
		        try {
		            Collection<Portal> portalCollection = getWebappDaoFactory().getPortalDao().getAllPortals();
		            portalArray = new int[portalCollection.size()];
		            int index = 0;
		            for (Iterator<Portal> pit = portalCollection.iterator(); pit.hasNext(); ) { 
		                Portal p = pit.next();
		                portalArray[index] = p.getPortalId();
		                index++;
		            }
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		    }
		}
          
		String uploadDesc ="";		
		
		Model tempModel = null;
		
		/* ********************* GET RDF by URL ********************** */
		String RDFUrlStr =  getRdfUrl(queryParameters);
		if (RDFUrlStr != null && RDFUrlStr.length() > 0) {
			tempModel = ModelFactory.createDefaultModel();
			try {
				tempModel.read(RDFUrlStr, languageStr); // languageStr may be null and default would be RDF/XML
				uploadDesc = verb + " RDF from " + RDFUrlStr;				
			} catch (JenaException ex){
	                forwardToFileUploadError("Could not parse file to " + languageStr + ": " + ex.getMessage(), req, response);
	                return;
			}catch (Exception e) {                               
                forwardToFileUploadError("Could not load from URL: " + e.getMessage(), req, response);                  
                return;
			}			
		} else {
		    /* **************** upload RDF from POST ********************* */
		    if( fileStreams.get("rdfStream") != null && fileStreams.get("rdfStream").size() > 0 ){
		        FileItem rdfStream = fileStreams.get("rdfStream").get(0);
		        tempModel = ModelFactory.createDefaultModel();
		        try {
		            tempModel.read( rdfStream.getInputStream(), null, languageStr);
		            uploadDesc = verb + " RDF from file " + rdfStream.getName();                
		        } catch (IOException e) {
		            forwardToFileUploadError("Could not read file: " + e.getLocalizedMessage(), req, response);
		            return;
		        }catch (JenaException ex){
		            forwardToFileUploadError("Could not parse file to " + languageStr + ": " + ex.getMessage(), req, response);
		            return;
		        }catch (Exception e) {                               
	                forwardToFileUploadError("Could not load from file: " + e.getMessage(), req, response);                  
	                return;
	            }finally{		    
		            rdfStream.delete();
		        }
		    }
		}
		/* ********** Do the model changes *********** */
		long stmtCount = 0L;
		if( tempModel != null ){
		    OntModel memModel=null;
		    try {
		        memModel = (OntModel) request.getSession().getAttribute("baseOntModel");
		    } catch (Exception e) {}
		    if (memModel==null) {
		        memModel = (OntModel) getServletContext().getAttribute("baseOntModel");
		    }
		    if (memModel != null) {
		        stmtCount = operateOnModel(memModel,tempModel,remove,makeClassgroups,portalArray,loginBean.getUserURI());
		    }					
		}
			
		request.setAttribute("uploadDesc", uploadDesc + ". " + verb + " " + stmtCount + "  statements.");
	    
        RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);
        request.setAttribute("bodyJsp","/templates/edit/specific/upload_rdf_result.jsp");
        request.setAttribute("portalBean",currentPortal);
        request.setAttribute("title","Ingest RDF Data");
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+currentPortal.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            log.error("Could not forward to view: " + e.getLocalizedMessage());            
        }
    }

    
    private String getCheckIn(Map<String, List<String>> queryParameters) {
        //request.getParameter("checkIndividualsIntoPortal");
        
        List<String> checkins = queryParameters.get("checkIndividualsIntoPortal");
        if( checkins != null && checkins.size() > 0 )
            return checkins.get(0);
        else
            return null;
    }


    private boolean isRemoveRequest(Map<String, List<String>> queryParameters) {
        List<String> modes = queryParameters.get("mode");
        if( modes != null && modes.size() > 0 && "remove".equals(modes.get(0)))
            return true;
        else 
            return false;
    }

    private String getLanguage(Map<String, List<String>> queryParameters) {
        List<String> langs = queryParameters.get("language");
        if( langs != null && langs.size() > 0 )
            return langs.get(0);
        else
            return null;
    }

    private String getRdfUrl(Map<String, List<String>> queryParameters){
        List<String> items = queryParameters.get("rdfUrl");
        if( items != null && items.size() > 0)
            return items.get(0);
        else
            return null;        
    }
    
    private long operateOnModel(OntModel mainModel, Model changesModel, boolean remove, boolean makeClassgroups, int[] portal,  String userURI) {
        mainModel.enterCriticalSection(Lock.WRITE);
        try {
            mainModel.getBaseModel().notifyEvent(new EditEvent(userURI,true));
            try {                
                if (makeClassgroups) {
                    Model classgroupModel = 
                        JenaModelUtils.makeClassGroupsFromRootClasses(getWebappDaoFactory(), changesModel, changesModel);
                    mainModel.add(classgroupModel);
                }                
                if (portal != null && portal.length>0) {
                    for (int i=0; i<portal.length; i++) {
                        JenaModelUtils.checkAllIndividualsInModelIntoPortal(changesModel, changesModel, portal[i]);
                    }
                }                 
                if (remove) {
                    mainModel.remove(changesModel);
                } else {
                    mainModel.add(changesModel);
                } 
            } finally {
                mainModel.getBaseModel().notifyEvent(new EditEvent(userURI,false));
            }
        } finally {
            mainModel.leaveCriticalSection();
        }
        return changesModel.size();        
    }
    
    
     private void forwardToFileUploadError( String errrorMsg , HttpServletRequest req, HttpServletResponse response) throws ServletException{
         req.setAttribute("errors", errrorMsg);
         RequestDispatcher rd = req.getRequestDispatcher("/edit/fileUploadError.jsp");            
         try {
             rd.forward(req, response);
         } catch (IOException e1) {
             throw new ServletException(e1);
         }            
         return;
     }
     
     
	@SuppressWarnings("unchecked")
    private Iterator<FileItem> getFileItemIterator(HttpServletRequest request)
	throws FileUploadException {	    	    	   	    
	    // Create a factory for disk-based file items
	    File tempDir = (File)getServletContext().getAttribute("javax.servlet.context.tempdir");
	    DiskFileItemFactory factory = new DiskFileItemFactory();
	    factory.setSizeThreshold(maxFileSizeInBytes);
	    factory.setRepository(tempDir);

	    // Create a new file upload handler
	    ServletFileUpload upload = new ServletFileUpload(factory);
	    upload.setSizeMax(maxFileSizeInBytes);
	    return upload.parseRequest(request).iterator();
    }

	private static final Log log = LogFactory.getLog(RDFUploadController.class.getName());
}
