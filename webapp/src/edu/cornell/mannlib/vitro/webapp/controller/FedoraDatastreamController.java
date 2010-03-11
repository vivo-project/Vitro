/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.hp.hpl.jena.datatypes.BaseDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.ibm.icu.util.Calendar;

import edu.cornell.mannlib.vitro.webapp.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import fedora.client.FedoraClient;
import fedora.common.Constants;
import fedora.server.management.FedoraAPIM;
import fedora.server.types.gen.Datastream;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vedit.beans.LoginFormBean;


/**
 * Handles a request to change a datastream in a fedora repository.
 * Some of this code is copied from N3MultiPartUpload.java
 * 
 * @author bdc34
 *
 */
public class FedoraDatastreamController extends VitroHttpServlet implements Constants{
    private static String FEDORA_PROPERTIES = "/WEB-INF/fedora.properties";
    private static String DEFAULT_DSID = "DS1";
    
    private String fedoraUrl = null;
    private String adminUser = null;
    private String adminPassword = null;
    private String pidNamespace = null;    
    private String configurationStatus = "<p>Fedora configuration not yet loaded</p>";
    private boolean configured = false;
    private boolean connected = false;
    
    private static final int DEFAULT_MAX_SIZE = 1024 * 1024 * 50;//Shoudl this be changed to 1 GB to be consistent
    private static final String DEFAULT_FILE_URI_PREFIX = "http://vivo.library.cornell.edu/ns/0.1#individual";
    private static final String DEFAULT_BASE_DIR = "/usr/local/vitrofiles";
    private static String fileUriPrefix = DEFAULT_FILE_URI_PREFIX;
    private static String baseDirectoryForFiles = DEFAULT_BASE_DIR;
    private static int maxFileSize = DEFAULT_MAX_SIZE;
    
    protected String contentTypeProperty = VitroVocabulary.CONTENT_TYPE;
	protected String fileSizeProperty = VitroVocabulary.FILE_SIZE;
    protected String fileNameProperty = VitroVocabulary.FILE_NAME;
    protected String fileLocationProperty = VitroVocabulary.FILE_LOCATION;
    protected String fileLabelProperty = RDFS.label.getURI();
	protected String checksumNodeProperty = "";//Object property linking file to check sum node object
	protected String checksumNodeDateTimeProperty = "";
	protected String checksumNodeValueProperty = "";
    protected String checksumDataProperty = ""; //is there a vitro equivalent?
    
    protected String deleteNs = "";
    protected String individualPrefix = "";
    protected String fedoraNs = VitroVocabulary.VITRO_FEDORA;
    
    
    /**
     * The get will present a form to the user. 
     */
    public void doGet( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        try {
            super.doGet(req, res);
            log.debug("In doGet");
            
            VitroRequest vreq = new VitroRequest(req);
            OntModel sessionOntModel = (OntModel)vreq.getSession().getAttribute("jenaOntModel");
            
            synchronized (FedoraDatastreamController.class) {
                if( fedoraUrl == null ){
                    setup( sessionOntModel, getServletContext() );
                    if( fedoraUrl == null ) 
                        throw new FdcException("Connection to the file repository is " +
                        		"not setup correctly.  Could not read fedora.properties file");
                }else{
                    if( !canConnectToFedoraServer() ){
                        fedoraUrl = null;
                        throw new FdcException("Could not connect to Fedora.");
                    }                   
                }
            }     
            FedoraClient fedora;
            try { fedora = new FedoraClient(fedoraUrl,adminUser,adminPassword); } 
            catch (MalformedURLException e) {
                throw new FdcException("Malformed URL for fedora Repository location: " + fedoraUrl); 
            }
            
            FedoraAPIM apim;
            try { apim = fedora.getAPIM(); } catch (Exception e) {
                throw new FdcException("could not create fedora APIM:" + e.getMessage());
            }

            //check if logged in
                        
            //get URI for file individual
            if( req.getParameter("uri") == null || "".equals(req.getParameter("uri")))                                        
                throw new FdcException("No file uri specified in request");
            
            boolean isDelete =  (req.getParameter("delete") != null && "true".equals(req.getParameter("delete")));
           
            String fileUri = req.getParameter("uri");
            //check if file individual has a fedora:PID for a data stream                        
            IndividualDao iwDao = vreq.getWebappDaoFactory().getIndividualDao();
            Individual entity = iwDao.getIndividualByURI(fileUri);
            
            if( entity == null )
                throw new FdcException( "No entity found in system for file uri " + fileUri);
            //System.out.println("Entity == null:" + (entity == null));                
            //get the fedora PID
            //System.out.println("entity data property " + entity.getDataPropertyMap().get(VitroVocabulary.FEDORA_PID));
            if( entity.getDataPropertyMap().get(VitroVocabulary.FEDORA_PID ) == null )
                throw new FdcException( "No fedora:pid found in system for file uri " + fileUri);            
            List<DataPropertyStatement> stmts = entity.getDataPropertyMap().get(VitroVocabulary.FEDORA_PID).getDataPropertyStatements();
            if( stmts == null || stmts.size() == 0)
                throw new FdcException( "No fedora:pid found in system for file uri " + fileUri);
            String pid = null;
            for(DataPropertyStatement stmt : stmts){
                if( stmt.getData() != null && stmt.getData().length() > 0){
                    pid = stmt.getData();
                    break;
                }
            }
            //System.out.println("pid is " + pid + " and comparison is " + (pid == null));
            if( pid == null )
                throw new FdcException( "No fedora:pid found in system for file uri " + fileUri);
            req.setAttribute("pid", pid);
            req.setAttribute("fileUri", fileUri);
          //get current file name to use on form
            req.setAttribute("fileName", entity.getName());     
            
            if(isDelete)
            {
            	//Execute a 'deletion', i.e. unlink dataset and file, without removing file
            	//Also save deletion as a deleteEvent entity which can later be queried
            	
            	 String datasetUri = null;
                 //Get dataset uri by getting the fromDataSet property
            	 edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty  fromDataSet = entity.getObjectPropertyMap().get(fedoraNs + "fromDataSet");
            	 if(fromDataSet != null)
            	 {
	                 List<ObjectPropertyStatement> fromDsStmts = fromDataSet.getObjectPropertyStatements();
	                 if(fromDsStmts.size() > 0) {
	                 	datasetUri = fromDsStmts.get(0).getObjectURI();
	                 	//System.out.println("object uri should be " + datasetUri);
	                 } else {
	                 	//System.out.println("No matching dataset uri could be found");
	                 } 
                 } else {
                	 //System.out.println("From dataset is null");
                 }
            	 
                 req.setAttribute("dataseturi", datasetUri);
            	 boolean success = deleteFile(req, entity, iwDao, sessionOntModel);
            	 req.setAttribute("deletesuccess", (success)?"success":"error");
            	 req.setAttribute("bodyJsp", "/edit/fileDeleteConfirm.jsp");
            	 RequestDispatcher rd = req.getRequestDispatcher(Controllers.BASIC_JSP);
 	             rd.forward(req, res);
            }
            else{
	            //check if the data stream exists in the fedora repository
	            Datastream ds = apim.getDatastream(pid,DEFAULT_DSID,null);
	            if( ds == null )
	                throw new FdcException("There was no datastream in the " +
	                		"repository for " + pid + " " + DEFAULT_DSID);            
	            req.setAttribute("dsid", DEFAULT_DSID);
	            
	                   
	            
	            //forward to form     
	            req.setAttribute("bodyJsp","/fileupload/datastreamModification.jsp");
	            RequestDispatcher rd = req.getRequestDispatcher(Controllers.BASIC_JSP);
	            rd.forward(req, res);
            }
        }catch(FdcException ex){
            req.setAttribute("errors", ex.getMessage());
            RequestDispatcher rd = req.getRequestDispatcher("/edit/fileUploadError.jsp");
            rd.forward(req, res);
            return;
        }
    }
                
    public void doPost(HttpServletRequest req, HttpServletResponse res)
    throws ServletException,IOException {
        try{          
            /* the post parameters seem to get consumed by the parsing so we have to
             * make a copy. */             
            boolean isMultipart = ServletFileUpload.isMultipartContent(req);
            if (!isMultipart) 
                throw new FdcException("Must POST a multipart encoded request");            
            
            Iterator<FileItem> iter;
            try { iter = getFileItemIterator(req);
            } catch (FileUploadException e) {
                e.printStackTrace();
                throw new FdcException("There was an error processing the " +
                		"parameters of your request.");
            }
            
            // get files or parameter values
            Map<String, List<String>> queryParameters =new HashMap<String, List<String>>();
            Map<String, List<FileItem>> fileStreams = new HashMap<String, List<FileItem>>();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();
                String name = item.getFieldName();
                if (item.isFormField()) {
                    if (queryParameters.containsKey(name)) {
                        String value = item.getString("UTF-8");
                        queryParameters.get(name).add(value);
                    } else {
                        List<String> valueList = new ArrayList<String>(1);
                        String value = item.getString("UTF-8");
                        valueList.add(value);
                        queryParameters.put(name, valueList);
                    }
                } else {
                    if (fileStreams.containsKey(name)) {
                        fileStreams.get(name).add(item);
                        log.debug("File in multipart content request:  field "
                                + name + " with file name " + item.getName()
                                + " detected.");
                    } else {
                        List<FileItem> itemList = new ArrayList<FileItem>();
                        itemList.add(item);
                        fileStreams.put(name, itemList);
                    }
                }
            }            

            //check if fedora is on line
            OntModel sessionOntModel = (OntModel)req.getSession().getAttribute("jenaOntModel");
            synchronized (FedoraDatastreamController.class) {
                if( fedoraUrl == null ){
                    setup( sessionOntModel, getServletContext() );
                    if( fedoraUrl == null ) 
                        throw new FdcException("Connection to the file repository is " +
                                "not setup correctly.  Could not read fedora.properties file");
                }else{
                    if( !canConnectToFedoraServer() ){
                        fedoraUrl = null;
                        throw new FdcException("Could not connect to Fedora.");
                    }                   
                }
            }     
            FedoraClient fedora;
            try { fedora = new FedoraClient(fedoraUrl,adminUser,adminPassword); } 
            catch (MalformedURLException e) {
                throw new FdcException("Malformed URL for fedora Repository location: " + fedoraUrl); 
            }            
            FedoraAPIM apim;
            try { apim = fedora.getAPIM(); } catch (Exception e) {
                throw new FdcException("could not create fedora APIM:" + e.getMessage());
            }
            
            //get the parameters from the request
            String pId=null;
            if( queryParameters.containsKey("pid") && queryParameters.get("pid") != null &&
                    queryParameters.get("pid").size() > 0 ){
                pId = queryParameters.get("pid").get(0);
            }
            String dsId=null;
            if( queryParameters.containsKey("dsid") && queryParameters.get("dsid") != null &&
                    queryParameters.get("dsid").size() > 0 ){
                dsId = queryParameters.get("dsid").get(0);
            }
            String fileUri=null;
            if( queryParameters.containsKey("fileUri") && queryParameters.get("fileUri") != null &&
                    queryParameters.get("fileUri").size() > 0 ){
                fileUri = queryParameters.get("fileUri").get(0);
            }
            boolean useNewName=false;
            if( "true".equals(queryParameters.get("useNewName"))){System.out.println("Use new name parameter is true");
                useNewName = true;
            }
            if( pId == null || pId.length() == 0 ) 
                throw new FdcException("Your form submission did not contain " +
                		"enough information to complete your request.(Missing pid parameter)");
            if( dsId == null || dsId.length() == 0 ) 
                throw new FdcException("Your form submission did not contain " +
                        "enough information to complete your request.(Missing dsid parameter)");
            if( fileUri == null || fileUri.length() == 0 ) 
                throw new FdcException("Your form submission did not contain " +
                        "enough information to complete your request.(Missing fileUri parameter)");
            
            FileItem fileRes = null;
            if( fileStreams.containsKey("fileRes") && fileStreams.get("fileRes").size()>0)
                fileRes = fileStreams.get("fileRes").get(0);
            if( fileRes == null ) 
                throw new FdcException("Your form submission did not contain " +
                "enough information to complete your request.(Missing fileRes)");
            
            //check if file individual has a fedora:PID for a data stream
            VitroRequest vreq = new VitroRequest(req);
            IndividualDao iwDao = vreq.getWebappDaoFactory().getIndividualDao();
            Individual fileEntity = iwDao.getIndividualByURI(fileUri);
            
            //check if logged in
            //TODO: check if logged in
                               
            //check if user is allowed to edit datastream
            //TODO:check if can edit datastream
            
            //check if digital object and data stream exist in fedora
            Datastream ds = apim.getDatastream(pId,dsId,null);
            if( ds == null )
                throw new FdcException("There was no datastream in the " +
                        "repository for " + pId + " " + DEFAULT_DSID);
                        
            //upload to temp holding area
            String originalName = fileRes.getName();
            String name = originalName.replaceAll("[,+\\\\/$%^&*#@!<>'\"~;]", "_");
            name = name.replace("..", "_");
            name = name.trim().toLowerCase();

            String saveLocation = baseDirectoryForFiles + File.separator + name;
            String savedName = name;
            int next = 0;
            boolean foundUnusedName = false;
            while (!foundUnusedName) {
                File test = new File(saveLocation);
                if (test.exists()) {
                    next++;
                    savedName = name + '(' + next + ')';
                    saveLocation = baseDirectoryForFiles + File.separator + savedName;
                } else {
                    foundUnusedName = true;
                }
            }

            File uploadedFile = new File(saveLocation);
            //System.out.println("Uploaded file path " + uploadedFile.getPath() + " - get file?" + uploadedFile.getName());
            String uploadedFileLocation = uploadedFile.getAbsolutePath();
            
            try {
                fileRes.write(uploadedFile);
            } catch (Exception ex) {
                log.error("Unable to save POSTed file. " + ex.getMessage());
                throw new FdcException("Unable to save file to the disk. "
                        + ex.getMessage());
            }
            
            //upload to temp area on fedora
            File file = new File(saveLocation);
            String uploadFileUri = fedora.uploadFile( file );                               
           // System.out.println("Fedora upload temp = upload file uri is " + uploadFileUri);
            String md5 = md5hashForFile( file );
            md5 = md5.toLowerCase();
            
            //make change to data stream on fedora
            apim.modifyDatastreamByReference(pId, dsId, 
                    null, null, 
                    fileRes.getContentType(), null, 
                    uploadFileUri, 
                    "MD5", null, 
                    null, false);
            
            String checksum =
                apim.compareDatastreamChecksum(pId,dsId,null);

            //update properties like checksum, file size, and content type
            
            WebappDaoFactory wdf = vreq.getWebappDaoFactory();
            DataPropertyStatement dps = null;
            DataProperty contentType = wdf.getDataPropertyDao().getDataPropertyByURI(this.contentTypeProperty);
            if(contentType != null)
            {
            	System.out.println("Setting content type to " + fileRes.getContentType());
            	wdf.getDataPropertyStatementDao().deleteDataPropertyStatementsForIndividualByDataProperty(fileEntity, contentType);            
	            dps = new DataPropertyStatementImpl();
	            dps.setIndividualURI(fileEntity.getURI());
	            dps.setDatapropURI(VitroVocabulary.CONTENT_TYPE);
	            dps.setData(fileRes.getContentType());
	            wdf.getDataPropertyStatementDao().insertNewDataPropertyStatement(dps);
            }

            DataProperty fileSize = wdf.getDataPropertyDao().getDataPropertyByURI(this.fileSizeProperty);
            if(fileSize != null)
            {
	            wdf.getDataPropertyStatementDao().deleteDataPropertyStatementsForIndividualByDataProperty(fileEntity, fileSize);            
	            dps = new DataPropertyStatementImpl();
	            dps.setIndividualURI(fileEntity.getURI());
	            dps.setDatapropURI(VitroVocabulary.FILE_SIZE);
	            dps.setData(Long.toString(fileRes.getSize()));
	            wdf.getDataPropertyStatementDao().insertNewDataPropertyStatement(dps);
	            //System.out.println("Updated file size with " + fileRes.getSize());
            }
            
            DataProperty checksumDp = wdf.getDataPropertyDao().getDataPropertyByURI(this.checksumDataProperty);
            if(checksumDp != null)
            {
            	//System.out.println("Checksum data property is also not null");
	            wdf.getDataPropertyStatementDao().deleteDataPropertyStatementsForIndividualByDataProperty(fileEntity, checksumDp);            
	            dps = new DataPropertyStatementImpl();
	            dps.setIndividualURI(fileEntity.getURI());
	            dps.setDatapropURI(checksumDp.getURI());
	            dps.setData(checksum);
	            wdf.getDataPropertyStatementDao().insertNewDataPropertyStatement(dps);
            }
       
            //I'm leaving if statement out for now as the above properties are obviously being replaced as well
            //if( "true".equals(useNewName)){
            	//Do we need to encapuslate in this if OR is this path always for replacing a file
            	//TODO: Put in check to see if file name has changed and only execute these statements if file name has changed
	            DataProperty fileNameProperty = wdf.getDataPropertyDao().getDataPropertyByURI(this.fileNameProperty);
	            if(fileNameProperty != null) {
	            	wdf.getDataPropertyStatementDao().deleteDataPropertyStatementsForIndividualByDataProperty(fileEntity, fileNameProperty);            
	            	dps = new DataPropertyStatementImpl();
		            dps.setIndividualURI(fileEntity.getURI());
		            dps.setDatapropURI(fileNameProperty.getURI());
		            dps.setData(originalName); //This follows the pattern of the original file upload - the name returned from the uploaded file object
		            wdf.getDataPropertyStatementDao().insertNewDataPropertyStatement(dps);
	            	//System.out.println("File name property is not null = " + fileNameProperty.getURI() + " updating to " + originalName);
	            } else {
	            	//System.out.println("file name property is null");
	            }
	            
	            //This doesn't seem to be settable as a data property - how else could we set this?
	            /*
	            DataProperty fileLocationProperty = wdf.getDataPropertyDao().getDataPropertyByURI(this.fileLocationProperty);
	            if(fileLocationProperty != null) {
	            	wdf.getDataPropertyStatementDao().deleteDataPropertyStatementsForIndividualByDataProperty(fileEntity, fileLocationProperty);            
	            	dps = new DataPropertyStatementImpl();
		            dps.setIndividualURI(fileEntity.getURI());
		            dps.setDatapropURI(fileLocationProperty.getURI());
		            dps.setData(saveLocation); //This follows the pattern of the original file upload - the name returned from the uploaded file object
		            wdf.getDataPropertyStatementDao().insertNewDataPropertyStatement(dps);
	            } else {
	            	System.out.println("File location property is null");
	            }
	            */
	            
	            //Need to also update the check sum node - how would we do that
	            //Find checksum node related to this particular file uri, then go ahead and update two specific fields
	            //ObjectProperty checksumNode = wdf.getObjectPropertyDao().getObjectPropertyByURI(this.checksumNodeProperty);
	            //if(checksumNode != null) {
	            //	System.out.println("Check sum node is not equal to null");
	            //	fileEntity.
	            //}
	            List<ObjectPropertyStatement >csNodeStatements = fileEntity.getObjectPropertyMap().get(this.checksumNodeProperty).getObjectPropertyStatements();
	            if(csNodeStatements.size() == 0) {
	            	System.out.println("No object property statements correspond to this property");
	            } else {
	            	ObjectPropertyStatement cnodeStatement = csNodeStatements.get(0);
	            	String cnodeUri = cnodeStatement.getObjectURI();
	            	//System.out.println("Checksum node uri is " + cnodeUri);
	            	
	            	Individual checksumNodeObject = iwDao.getIndividualByURI(cnodeUri);
	            	
	            	DataProperty checksumDateTime = wdf.getDataPropertyDao().getDataPropertyByURI(this.checksumNodeDateTimeProperty);
	            	if(checksumDateTime != null) {
	            		 String newDatetime = sessionOntModel.createTypedLiteral(new DateTime()).getString();
	            		//Review how to update date time
	            		wdf.getDataPropertyStatementDao().deleteDataPropertyStatementsForIndividualByDataProperty(checksumNodeObject, checksumDateTime);            
		            	dps = new DataPropertyStatementImpl();
			            dps.setIndividualURI(checksumNodeObject.getURI());
			            dps.setDatapropURI(checksumDateTime.getURI());
			            dps.setData(newDatetime); 
			            wdf.getDataPropertyStatementDao().insertNewDataPropertyStatement(dps);
	            		
	            	}
	            	DataProperty checksumNodeValue = wdf.getDataPropertyDao().getDataPropertyByURI(this.checksumDataProperty);
	            	if(checksumNodeValue != null) {
	            		wdf.getDataPropertyStatementDao().deleteDataPropertyStatementsForIndividualByDataProperty(checksumNodeObject, checksumNodeValue);            
		            	dps = new DataPropertyStatementImpl();
			            dps.setIndividualURI(checksumNodeObject.getURI());
			            dps.setDatapropURI(checksumNodeValue.getURI());
			            dps.setData(checksum); //Same as fileName above - change if needed
			            wdf.getDataPropertyStatementDao().insertNewDataPropertyStatement(dps);
	            	}
	            	
	            }
	            
	            //Assumes original entity name is equal to the location - as occurs with regular file upload
	            String originalEntityName = fileEntity.getName();
	            if(originalEntityName != originalName) {
	            	//System.out.println("Setting file entity to name of uploaded file");
	            	fileEntity.setName(originalName);
	            } else {
	            	//System.out.println("Conditional for file entity name and uploaded name is saying same");
	            }
	           iwDao.updateIndividual(fileEntity);
            //}
            
            req.setAttribute("fileUri", fileUri);
            req.setAttribute("originalFileName", fileEntity.getName());
            req.setAttribute("checksum", checksum);
            if( "true".equals(useNewName)){
                req.setAttribute("useNewName", "true");
                req.setAttribute("newFileName", originalName);
            }else{
                req.setAttribute("newFileName", fileEntity.getName());
            }
            
            //forward to form     
            req.setAttribute("bodyJsp","/fileupload/datastreamModificationSuccess.jsp");            
            RequestDispatcher rd = req.getRequestDispatcher(Controllers.BASIC_JSP);
            rd.forward(req, res);
        }catch(FdcException ex){
            req.setAttribute("errors", ex.getMessage());
            RequestDispatcher rd = req.getRequestDispatcher("/edit/fileUploadError.jsp");
            rd.forward(req, res);
            return;
        }
    }
    
    //Delete method
    public boolean deleteFile(HttpServletRequest req, Individual entity, IndividualDao iwDao, OntModel sessionOntModel) {
    	boolean success = false;
    	String fileUri = entity.getURI();
    	//Create uri based on milliseconds etc.?
    	Calendar c = Calendar.getInstance();
    	long timeMs = c.getTimeInMillis();
    	//Cuirrent date
	    SimpleDateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String formattedDeleteDate = dateTime.format(c.getTime());
    	String deleteEventName = "deleteEvent" + timeMs;
    	//System.out.println("Delete event name is " +deleteEventName + " - delete time is " + formattedDeleteDate);
    	
    	//Get current user
    	HttpSession session = req.getSession(true);
    	LoginFormBean loginBean = (LoginFormBean) session.getAttribute("loginHandler");
        String userURI = loginBean.getUserURI();
        //System.out.println("Current logged in user uri is " + userURI); 
       
        //Update model
    	sessionOntModel.enterCriticalSection(true);
    	
    	try {
    		
    		//Dataset Uri
    		String datasetUri = (String) req.getAttribute("dataseturi");
    		//System.out.println("Dataset uri is " + datasetUri);
    		//Remove the actual relationships: dsr:hasFile and fedora:fromDataSet
    		ObjectProperty hasFileProperty = sessionOntModel.getObjectProperty(fedoraNs + "hasFile");
    		
    		ObjectProperty fromDatasetProperty = sessionOntModel.getObjectProperty(fedoraNs + "fromDataSet");
    		if(hasFileProperty != null) {
    			//System.out.println("Has file property does exist");
    			sessionOntModel.remove(sessionOntModel.createStatement(sessionOntModel.getResource(datasetUri), hasFileProperty, sessionOntModel.getResource(fileUri)));
    		} else{
    			//System.out.println("Has file property does not exist");
    		}
    		
    		if(fromDatasetProperty != null) {
    			//System.out.println("From dataset property exists ");
    			sessionOntModel.remove(sessionOntModel.createStatement(sessionOntModel.getResource(fileUri), fromDatasetProperty, sessionOntModel.getResource(datasetUri)));
    		} else{
    			//System.out.println("From dataset property does not exist");
    		}
    		
    		 
    		//Create delete event entity and update with the correct information
    		//Type of Event
    		Resource deleteEventType = sessionOntModel.createResource(deleteNs + "DeleteEvent");
    		//Individual event
	    	Resource eventIndividual = sessionOntModel.createResource(individualPrefix + deleteEventName);
	    	//Event is of type DeleteEvent
	    	Statement rType = sessionOntModel.createStatement(eventIndividual, com.hp.hpl.jena.vocabulary.RDF.type, deleteEventType);
	    	sessionOntModel.add(rType);
	    	//Add properties to individual - deleteDateTime, deletedBy, forDataSet, forFile
	    	DatatypeProperty dateTimeProp = sessionOntModel.createDatatypeProperty(deleteNs + "deleteDateTime");
	    	dateTimeProp.setRange(XSD.dateTime);
	    	
	    	ObjectProperty deletedByProp = sessionOntModel.createObjectProperty(deleteNs + "deletedBy");
	    	ObjectProperty forDatasetProp = sessionOntModel.createObjectProperty(deleteNs + "forDataset");
	    	ObjectProperty forFileProp = sessionOntModel.createObjectProperty(deleteNs + "forFile");
	    	//Need to make sure date time property is set to correct xsd:DateTime
	    	//XSDDateTime now = new XSDDateTime(c);
	    	//XSDDateTime now = new XSDDateTime(java.util.Calendar.getInstance());
	    	eventIndividual.addProperty(dateTimeProp, sessionOntModel.createTypedLiteral(formattedDeleteDate, XSDDatatype.XSDdateTime));
	    	//eventIndividual.addProperty(dateTimeProp, sessionOntModel.createTypedLiteral(now, XSDDatatype.XSDdateTime));
	    	eventIndividual.addProperty(deletedByProp, sessionOntModel.getResource(userURI));
	    	if(datasetUri != null){
	    		//System.out.println("Dataset uri is " + datasetUri);
	    		eventIndividual.addProperty(forDatasetProp, sessionOntModel.getResource(datasetUri));
	    	}
	    	eventIndividual.addProperty(forFileProp, sessionOntModel.getResource(fileUri));
	    	success = true;
	    	
    	} finally {
    		sessionOntModel.leaveCriticalSection();
    	}
    	return success;
    }
    
	public void init() throws ServletException {
		super.init();

		fileUriPrefix = ConfigurationProperties.getProperty(
				"n3.defaultUriPrefix", DEFAULT_FILE_URI_PREFIX);
		baseDirectoryForFiles = ConfigurationProperties.getProperty(
				"n3.baseDirectoryForFiles", DEFAULT_BASE_DIR);

		String maxSize = ConfigurationProperties.getProperty("n3.maxSize", Long
				.toString(DEFAULT_MAX_SIZE));
		try {
			maxFileSize = Integer.parseInt(maxSize);
		} catch (NumberFormatException nfe) {
			log.error(nfe);
			maxFileSize = DEFAULT_MAX_SIZE;
		}
	}
   
    private void setup(OntModel model, ServletContext context) {
        this.configurationStatus = "";
        StringBuffer status = new StringBuffer("");
        
        if( connected && configured )
            return;
        
        Properties props = new Properties();
        String path = context.getRealPath(FEDORA_PROPERTIES);
        try{            
            InputStream in = new FileInputStream(new File( path ));
            if( in == null ){                
                log.error("No fedora.properties file found,"+ 
                        "it should be located at " + path);
                status.append("<h1>Fedora configuration failed.</h1>\n");
                status.append("<p>No fedora.properties file found,"+ 
                        "it should be located at " + path + "</p>\n");
                configured = false;
            } else {                
                props.load( in );
                fedoraUrl = props.getProperty("fedoraUrl");
                adminUser = props.getProperty("adminUser");
                adminPassword = props.getProperty("adminPassword");
                pidNamespace = props.getProperty("pidNamespace");
                
                if( fedoraUrl == null || adminUser == null || adminPassword == null ){
                    if( fedoraUrl == null ){
                        log.error("'fedoraUrl' not found in properties file");        
                        status.append("<p>'fedoraUrl' not found in properties file.</p>\n");
                    }
                    if( adminUser == null ) {
                        log.error("'adminUser' was not found in properties file, the " +
                              "user name of the fedora admin is needed to access the " +
                                "fedora API-M services.");                    
                        status.append("<p>'adminUser' was not found in properties file, the " +
                                "user name of the fedora admin is needed to access the " +
                                  "fedora API-M services.</p>\n");
                    }
                    if( adminPassword == null ){
                        log.error("'adminPassword' was not found in properties file, the " +
                        "admin password is needed to access the fedora API-M services.");
                        status.append("<p>'adminPassword' was not found in properties file, the " +
                        "admin password is needed to access the fedora API-M services.</p>\n");
                    }
                    if( pidNamespace == null ){
                        log.error("'pidNamespace' was not found in properties file, the " +
                        "PID namespace indicates which namespace to use when creating " +
                        "new fedor digital objects.");
                        status.append("<p>'pidNamespace' was not found in properties file, the " +
                                "PID namespace indicates which namespace to use when creating " +
                                "new fedor digital objects.</p>\n");
                    } 
                    fedoraUrl = null; adminUser = null; adminPassword = null;
                    configured = false;
                }  else {
                    configured = true;
                }
            }
        }catch(Exception ex){            
            status.append("<p>Fedora configuration failed.</p>\n");
            status.append("<p>Exception while loading" + path + "</p>\n");
            status.append("<p>" + ex.getMessage() + "</p>\n"); 
            log.error("could not load fedora properties", ex);
            fedoraUrl = null; adminUser = null; adminPassword = null;
            configured = false;
            return;
        }
         
        
        status.append(RELOAD_MSG); 
        this.configurationStatus += status.toString();
//       else{
//            status.append("<h2>Fedora configuration file ").append(path).append(" was loaded</h2>");
//            status.append("<p>fedoraUrl: ").append(fedoraUrl).append("</p>\n");
//            checkFedoraServer();
//        }
    }
    
    private boolean canConnectToFedoraServer( ){
        try{
            FedoraClient fc = new FedoraClient(fedoraUrl,adminUser, adminPassword);
            String fedoraVersion = fc.getServerVersion();
            if( fedoraVersion != null && fedoraVersion.length() > 0 ){
                configurationStatus += "<p>Fedora server is live and is running " +
                        "fedora version " + fedoraVersion + "</p>\n";
                connected = true;
                return true;
            } else {
                configurationStatus += "<p>Unable to reach fedora server</p>\n";
                connected = false;
                return false;
            }
        }catch (Exception e) {
            configurationStatus += "<p>There was an error while checking the " +
                    "fedora server version</p>\n<p>"+ e.getMessage() + "</p>\n";
            connected = false;
            return false;            
        }                            
    }
    
    public boolean isConfigured(){ return configured; }
    public boolean isConnected(){ return connected; }
    
    private class FdcException extends Exception {
        public FdcException(String message) {
            super(message);           
        }
    };
    
    private Iterator<FileItem> getFileItemIterator(HttpServletRequest request)
    throws FileUploadException {
        //Create a factory for disk-based file items
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(maxFileSize);
        factory.setRepository(new File(baseDirectoryForFiles));

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setSizeMax(maxFileSize);
        return upload.parseRequest(request).iterator();
    }
    
    private static final String RELOAD_MSG = 
        "<p>The fedora configuartion file will be reloaded if " +
        "you edit the properties file and check the status.</p>\n";

    public static String md5hashForFile(File file){  
        try {
            InputStream fin = new FileInputStream(file);
            java.security.MessageDigest md5er =
                MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024];
            int read;
            do {
                read = fin.read(buffer);
                if (read > 0)
                    md5er.update(buffer, 0, read);
            } while (read != -1);
            fin.close();
            byte[] digest = md5er.digest();
            if (digest == null)
                return null;
            String strDigest = "0x";
            for (int i = 0; i < digest.length; i++) {
                strDigest += Integer.toString((digest[i] & 0xff) 
                        + 0x100, 16).substring(1);
            }
            return strDigest;
        } catch (Exception e) {
            return null;
        }        
    }
    
    private static final Log log = LogFactory.getLog(FedoraDatastreamController.class.getName());
}
