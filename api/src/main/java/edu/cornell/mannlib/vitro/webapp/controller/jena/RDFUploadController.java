/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.ABOX_ASSERTIONS;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames.TBOX_ASSERTIONS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.JenaModelUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.OntModelSelector;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceGraph;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.BulkUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeSet;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceUtils;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;

public class RDFUploadController extends JenaIngestController {
    
    private static int maxFileSizeInBytes = 1024 * 1024 * 2000; //2000mb
    private static FileItem fileStream = null; 
    private static final String LOAD_RDF_DATA_JSP="/jenaIngest/loadRDFData.jsp";
    
	@Override
	public long maximumMultipartFileSize() {
		return maxFileSizeInBytes;
	}

	@Override
	public boolean stashFileSizeException() {
		return true;
	}
	
    @Override
	public void doPost(HttpServletRequest req,
            HttpServletResponse response) throws ServletException, IOException {
		if (!isAuthorizedToDisplayPage(req, response,
				SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION)) {
            return;
        }

		VitroRequest request = new VitroRequest(req);        
        if (request.hasFileSizeException()) {
            forwardToFileUploadError(
                    request.getFileSizeException().getLocalizedMessage(), 
                            req, response);
            return;
        }

        Map<String, List<FileItem>> fileStreams = request.getFiles();
        
        LoginStatusBean loginBean = LoginStatusBean.getBean(request);
        
        try {
            String modelName = req.getParameter("modelName");
            if(modelName!=null){
                loadRDF(request,response);
                return;
            }    
        } catch (Exception e) {
            log.error(e,e);
            throw new RuntimeException(e);
        }
                       
        boolean remove = "remove".equals(request.getParameter("mode"));
        String verb = remove?"Removed":"Added";
        
        String languageStr = request.getParameter("language");
        
        boolean makeClassgroups = ("true".equals(request.getParameter(
                "makeClassgroups")));
        
        // add directly to the ABox model without reading first into 
        // a temporary in-memory model
        boolean directRead = ("directAddABox".equals(request.getParameter(
                "mode")));
          
        String uploadDesc ="";        
                
        OntModel uploadModel = (directRead) 
            ? getABoxModel(getServletContext())
            : ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
            
        /* ********************* GET RDF by URL ********************** */
        String RDFUrlStr =  request.getParameter("rdfUrl");
        if (RDFUrlStr != null && RDFUrlStr.length() > 0) {
            try {
                uploadModel.enterCriticalSection(Lock.WRITE);
                try {
                    uploadModel.read(RDFUrlStr, languageStr); 
                    // languageStr may be null and default would be RDF/XML
                } finally {
                    uploadModel.leaveCriticalSection();
                }
                uploadDesc = verb + " RDF from " + RDFUrlStr;                
            } catch (JenaException ex){
                    forwardToFileUploadError("Could not parse file to " + 
                           languageStr + ": " + ex.getMessage(), req, response);
                    return;
            }catch (Exception e) {                               
                forwardToFileUploadError("Could not load from URL: " + 
                        e.getMessage(), req, response);                  
                return;
            }            
        } else {
            /* **************** upload RDF from POST ********************* */
            if( fileStreams.get("rdfStream") != null 
                    && fileStreams.get("rdfStream").size() > 0 ) {
                FileItem rdfStream = fileStreams.get("rdfStream").get(0);
                try {
                    if (directRead) {
                        addUsingRDFService(rdfStream.getInputStream(), languageStr,
                                request.getRDFService());
                    } else {
                        uploadModel.enterCriticalSection(Lock.WRITE);
                        try {
                            uploadModel.read(
                                    rdfStream.getInputStream(), null, languageStr);
                        } finally {
                            uploadModel.leaveCriticalSection();
                        }
                    }
                    uploadDesc = verb + " RDF from file " + rdfStream.getName();                
                } catch (IOException e) {
                    forwardToFileUploadError("Could not read file: " + 
                            e.getLocalizedMessage(), req, response);
                    return;
                }catch (JenaException ex){
                    forwardToFileUploadError("Could not parse file to " + 
                            languageStr + ": " + ex.getMessage(), 
                                    req, response);
                    return;
                }catch (Exception e) {                               
                    forwardToFileUploadError("Could not load from file: " + 
                            e.getMessage(), req, response);                  
                    return;
                }finally{            
                    rdfStream.delete();
                }
            }
        }
        
        /* ********** Do the model changes *********** */
        if( !directRead && uploadModel != null ){
            
            uploadModel.loadImports();
            
            long tboxstmtCount = 0L;
            long aboxstmtCount = 0L;

            JenaModelUtils xutil = new JenaModelUtils();
            
            OntModel tboxModel = getTBoxModel();
            OntModel aboxModel = getABoxModel(
                    getServletContext());
            OntModel tboxChangeModel = null;
            Model aboxChangeModel = null;
            OntModelSelector ontModelSelector = ModelAccess.on(getServletContext()).getOntModelSelector();
            
            if (tboxModel != null) {
                boolean AGGRESSIVE = true;
                tboxChangeModel = xutil.extractTBox(uploadModel, AGGRESSIVE);
                // aggressively seek all statements that are part of the TBox  
                tboxstmtCount = operateOnModel(request.getUnfilteredWebappDaoFactory(),
                        tboxModel, tboxChangeModel, ontModelSelector,
                                remove, makeClassgroups, loginBean.getUserURI());
            }
            if (aboxModel != null) {
                aboxChangeModel = uploadModel.remove(tboxChangeModel);
                aboxstmtCount = aboxChangeModel.size();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                aboxChangeModel.write(os, "N3");
                ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
                if(!remove) {
                    readIntoModel(in, "N3", request.getRDFService(), 
                            ModelNames.ABOX_ASSERTIONS);
                } else {
                    removeFromModel(in, "N3", request.getRDFService(), 
                            ModelNames.ABOX_ASSERTIONS);
                }
//                operateOnModel(request.getUnfilteredWebappDaoFactory(),
//                        aboxModel, aboxChangeModel, ontModelSelector, 
//                                remove, makeClassgroups, loginBean.getUserURI());
            }
            request.setAttribute("uploadDesc", uploadDesc + ". " + verb + " " + 
                    (tboxstmtCount + aboxstmtCount) + "  statements.");
        } else {
            request.setAttribute("uploadDesc", "RDF upload successful.");
        }

        request.setAttribute("title","Ingest RDF Data");

        try {
            JSPPageHandler.renderBasicPage(request, response, "/templates/edit/specific/upload_rdf_result.jsp");
        } catch (Exception e) {
            log.error("Could not forward to view: " + e.getLocalizedMessage());            
        }
    }

    private static final boolean BEGIN = true;
    private static final boolean END = !BEGIN;
    
    private ChangeSet makeChangeSet(RDFService rdfService) {
        ChangeSet cs = rdfService.manufactureChangeSet();
        cs.addPreChangeEvent(new BulkUpdateEvent(null, BEGIN));
        cs.addPostChangeEvent(new BulkUpdateEvent(null, END));
        return cs;
    }
    
    private void addUsingRDFService(InputStream in, String languageStr, 
            RDFService rdfService) {
        ChangeSet changeSet = makeChangeSet(rdfService);
        RDFService.ModelSerializationFormat format = 
                ("RDF/XML".equals(languageStr) 
                        || "RDF/XML-ABBREV".equals(languageStr))
                                ? RDFService.ModelSerializationFormat.RDFXML
                                : RDFService.ModelSerializationFormat.N3;
        changeSet.addAddition(in, format, 
                ABOX_ASSERTIONS);
        try {
            rdfService.changeSetUpdate(changeSet);
        } catch (RDFServiceException rdfse) {
            log.error(rdfse);
            throw new RuntimeException(rdfse);
        }
    }
    
    public void loadRDF(VitroRequest request, HttpServletResponse response) 
                                throws ServletException {
        Map<String, List<FileItem>> fileStreams = request.getFiles();
        String filePath = fileStreams.get("filePath").get(0).getName();
        fileStream = fileStreams.get("filePath").get(0);
        String modelName = request.getParameter("modelName");
        String docLoc = request.getParameter("docLoc");
        String languageStr = request.getParameter("language");
        ModelMaker maker = getModelMaker(request);

        String bodyJsp;
        if (modelName == null) {
            request.setAttribute("title","Load RDF Data");
            bodyJsp = LOAD_RDF_DATA_JSP;
        } else {          
            RDFService rdfService = getRDFService(request, maker, modelName);
            try {
                doLoadRDFData(modelName, docLoc, filePath, languageStr, rdfService);
            } finally {
                rdfService.close();
            }
            WhichService modelType = getModelType(request);
            bodyJsp = showModelList(request, maker, modelType);
        } 
        
        try {
            JSPPageHandler.renderBasicPage(request, response, bodyJsp);
        } catch (Exception e) {
            String errMsg = " could not forward to view.";
            log.error(errMsg, e);
            throw new ServletException(errMsg, e);
        }
        
    }
    
    private RDFService getRDFService(VitroRequest vreq, ModelMaker maker, String modelName) {
        if (isUsingMainStoreForIngest(vreq)) {
            log.debug("Using main RDFService");
			return ModelAccess.on(getServletContext()).getRDFService();
        } else {
            log.debug("Making RDFService for single model from ModelMaker");
            Model m = maker.getModel(modelName);
            return new RDFServiceModel(m);   
        }
    }
    
    private long operateOnModel(WebappDaoFactory webappDaoFactory, 
                                OntModel mainModel, 
                                Model changesModel, 
                                OntModelSelector ontModelSelector,
                                boolean remove, 
                                boolean makeClassgroups,  
                                String userURI) {
            
        EditEvent startEvent = null, endEvent = null;
        
        if (remove) {
            startEvent = new BulkUpdateEvent(userURI, true);
            endEvent = new BulkUpdateEvent(userURI, false);
        } else {
            startEvent = new EditEvent(userURI, true);
            endEvent = new EditEvent(userURI, false);
        }
         
        Model[] classgroupModel = null;
        
        if (makeClassgroups) {
            classgroupModel = JenaModelUtils.makeClassGroupsFromRootClasses(
                        webappDaoFactory, changesModel);
            OntModel appMetadataModel = ontModelSelector
                    .getApplicationMetadataModel(); 
            appMetadataModel.enterCriticalSection(Lock.WRITE);
            try {
                appMetadataModel.add(classgroupModel[0]);
            } finally {
                appMetadataModel.leaveCriticalSection();
            }
        }
            
        mainModel.enterCriticalSection(Lock.WRITE);
        try {
            
            mainModel.getBaseModel().notifyEvent(startEvent);
            try {                                                 
                if (remove) {
                    RDFService rdfService = new RDFServiceModel(mainModel);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    changesModel.write(out, "N-TRIPLE");
                    ChangeSet cs = makeChangeSet(rdfService);
                    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                    cs.addRemoval(in, RDFService.ModelSerializationFormat.NTRIPLE, null);
                    try {
                        rdfService.changeSetUpdate(cs);
                    } catch (RDFServiceException e) {
                        throw new RuntimeException(e);
                    }
                    //mainModel.remove(changesModel);
                } else {
                    mainModel.add(changesModel);
                    if (classgroupModel != null) {
                        mainModel.add(classgroupModel[1]);
                    }
                } 
            } finally {
                mainModel.getBaseModel().notifyEvent(endEvent);
            }
        } finally {
            mainModel.leaveCriticalSection();
        }
        return changesModel.size();        
    }
    
    private void doLoadRDFData(String modelName, 
                               String docLoc, 
                               String filePath, 
                               String language, 
                               RDFService rdfService) {
        try {
            if ( (docLoc != null) && (docLoc.length()>0) ) {
                URL docLocURL = new URL(docLoc);
                InputStream in = docLocURL.openStream();
                readIntoModel(in, language, rdfService, modelName);
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
                    log.debug("Reading file " + currentFile.getName());
                    try {
                        readIntoModel(fileStream.getInputStream(), language, 
                                rdfService, modelName);
                        fileStream.delete();
                    } catch (IOException ioe) {
                        String errMsg = "Error loading RDF from " + 
                                currentFile.getName();
                        log.error(errMsg, ioe);
                        throw new RuntimeException(errMsg, ioe);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void readIntoModel(InputStream in, String language, 
            RDFService rdfService, String modelName) {
        ChangeSet cs = makeChangeSet(rdfService);
        cs.addAddition(in, RDFServiceUtils.getSerializationFormatFromJenaString(
                        language), modelName);
        try {
            rdfService.changeSetUpdate(cs);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void removeFromModel(InputStream in, String language, 
            RDFService rdfService, String modelName) {
        ChangeSet cs = makeChangeSet(rdfService);
        cs.addRemoval(in, RDFServiceUtils.getSerializationFormatFromJenaString(
                        language), modelName);
        try {
            rdfService.changeSetUpdate(cs);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }
    
     private void forwardToFileUploadError( String errrorMsg , 
                                            HttpServletRequest req, 
                                            HttpServletResponse response) 
                                                    throws ServletException{
         VitroRequest vreq = new VitroRequest(req);
         req.setAttribute("title","RDF Upload Error ");
         req.setAttribute("errors", errrorMsg);
         
         req.setAttribute("css",
                 "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + 
                 vreq.getAppBean().getThemeDir() + "css/edit.css\"/>");
         try {
             JSPPageHandler.renderBasicPage(req, response, "/jsp/fileUploadError.jsp");
         } catch (IOException e1) {
             log.error(e1);
             throw new ServletException(e1);
         }            
         return;
     }
     
     private OntModel getABoxModel(ServletContext ctx) {   
         RDFService rdfService = ModelAccess.on(ctx).getRDFService();
         Model abox = RDFServiceGraph.createRDFServiceModel(
                 new RDFServiceGraph(rdfService, ABOX_ASSERTIONS));
         return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, abox);
     }    

     private OntModel getTBoxModel() { 
    	 return ModelAccess.on(getServletContext()).getOntModel(TBOX_ASSERTIONS);
     }    
     
    private static final Log log = LogFactory.getLog(
            RDFUploadController.class.getName());
}
