/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaSpecialModelMaker;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;

public class RDFUploadController extends BaseEditController {
	
    private static int maxFileSizeInBytes = 1024 * 1024 * 2000; //2000mb 
    private static FileItem fileStream=null; 
    private static final String INGEST_MENU_JSP = "/jenaIngest/ingestMenu.jsp";
    private static final String LOAD_RDF_DATA_JSP = "/jenaIngest/loadRDFData.jsp";
	
	public void doPost(HttpServletRequest rawRequest,
			HttpServletResponse response) throws ServletException, IOException {
		FileUploadServletRequest req = FileUploadServletRequest.parseRequest(rawRequest,
				maxFileSizeInBytes);
		if (req.hasFileUploadException()) {
			forwardToFileUploadError(req.getFileUploadException().getLocalizedMessage(), req, response);
			return;
		}

		Map<String, List<FileItem>> fileStreams = req.getFiles();
	    
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
		
		String modelName = req.getParameter("modelName");
		if(modelName!=null){
			loadRDF(req,request,response);
			return;
		}	
		
		Portal currentPortal = request.getPortal();		
	    			   
		boolean remove = "remove".equals(request.getParameter("mode"));
		String verb = remove?"Removed":"Added";
		
		String languageStr = request.getParameter("language");
		
		boolean makeClassgroups = (request.getParameter("makeClassgroups") != null);
		
		int[] portalArray = null;
		String individualCheckIn = request.getParameter("checkIndividualsIntoPortal"); 
		if (individualCheckIn != null) {
		    if (individualCheckIn.equals("current")) {
		        portalArray = new int[1];
		        portalArray[0] = currentPortal.getPortalId();
		    } else if (individualCheckIn.equals("all")) {
		        try {
		            Collection<Portal> portalCollection = request.getFullWebappDaoFactory().getPortalDao().getAllPortals();
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
		String RDFUrlStr =  request.getParameter("rdfUrl");
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
		        stmtCount = operateOnModel(request.getFullWebappDaoFactory(), memModel,tempModel,remove,makeClassgroups,portalArray,loginBean.getUserURI());
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

	public void loadRDF(FileUploadServletRequest req,
			VitroRequest request,HttpServletResponse response) throws ServletException, IOException {
		Map<String, List<FileItem>> fileStreams = req.getFiles();
		String filePath = fileStreams.get("filePath").get(0).getName();
		fileStream = fileStreams.get("filePath").get(0);
		String modelName = req.getParameter("modelName");
		String docLoc = req.getParameter("docLoc");
		String languageStr = request.getParameter("language");
		ModelMaker maker = getVitroJenaModelMaker(request);
		
		if (docLoc!=null && modelName != null) {
			doLoadRDFData(modelName,docLoc,filePath,languageStr,maker);
			request.setAttribute("title","Ingest Menu");
			request.setAttribute("bodyJsp",INGEST_MENU_JSP);
		} else {
			request.setAttribute("title","Load RDF Data");
			request.setAttribute("bodyJsp",LOAD_RDF_DATA_JSP);
		}
		Portal portal = request.getPortal();
		RequestDispatcher rd = request.getRequestDispatcher(Controllers.BASIC_JSP);      
        request.setAttribute("portalBean",portal);
        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+portal.getThemeDir()+"css/edit.css\"/>");

        try {
            rd.forward(request, response);
        } catch (Exception e) {
            System.out.println(this.getClass().getName()+" could not forward to view.");
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace());
        }
		
	}
	
    private long operateOnModel(WebappDaoFactory webappDaoFactory, OntModel mainModel, Model changesModel, boolean remove, boolean makeClassgroups, int[] portal,  String userURI) {
        mainModel.enterCriticalSection(Lock.WRITE);
        try {
            mainModel.getBaseModel().notifyEvent(new EditEvent(userURI,true));
            try {                
                if (makeClassgroups) {
                    Model classgroupModel = 
                        JenaModelUtils.makeClassGroupsFromRootClasses(webappDaoFactory, changesModel, changesModel);
                    mainModel.add(classgroupModel);
                }                
                if (!remove && portal != null && portal.length>0) {
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
    
    private void doLoadRDFData(String modelName, String docLoc, String filePath, String language, ModelMaker modelMaker) {
		Model m = modelMaker.getModel(modelName);
		m.enterCriticalSection(Lock.WRITE);
		try {
			if ( (docLoc != null) && (docLoc.length()>0) ) {
				m.read(docLoc, language);
			} else if ( (filePath != null) && (filePath.length()>0) ) {
				File file = new File(filePath);
				File[] files;
				if (file.isDirectory()) {
					files = file.listFiles();
				} else {
					files = new File[1];
					files[0] = file;
				}
				for (int i=0; i<files.length; i++) {
					File currentFile = files[i];
					log.info("Reading file "+currentFile.getName());
					
					try {
					
						m.read(fileStream.getInputStream(), null, language);
						fileStream.delete();
					} catch (IOException ioe) {
						throw new RuntimeException(ioe);
					}
				}
			}
		} finally { 
			m.leaveCriticalSection();
		}
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
     
     private ModelMaker getVitroJenaModelMaker(HttpServletRequest request) {
  		ModelMaker myVjmm = (ModelMaker) request.getSession().getAttribute("vitroJenaModelMaker");
  		myVjmm = (myVjmm == null) ? (ModelMaker) getServletContext().getAttribute("vitroJenaModelMaker") : myVjmm;
  		return new VitroJenaSpecialModelMaker(myVjmm, request);
  	}
     
	private static final Log log = LogFactory.getLog(RDFUploadController.class.getName());
}
