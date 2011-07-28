/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Generator;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditN3Utils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;
import edu.cornell.mannlib.vitro.webapp.filestorage.uploadrequest.FileUploadServletRequest;
import edu.cornell.mannlib.vitro.webapp.utils.MailUtil;

/**
 * Process a N3 form submission with a multipart data encoding. This follows a
 * similar sort of processing as a N3 form update where each submitted file is
 * processed using the configuration information from a Field.
 * 
 * For each file uploaded the assertions of the Field associated with the file
 * will be processed. These additional values will be defined:
 * 
 * ?fileURI = the URI of the newly uploaded file. ?fileName = original name of
 * the uploaded file. ?fileSaveLocation = location on file system the file was
 * saved to. ?fileSavedName = name that was used to save the file, this may be
 * different than the fileName if there is an existing file with a conflicting
 * name. ?fileContentType = MIME type reported by client browser. ?fileSize =
 * file size reported by ServletFileUpload parser.
 * 
 * How to do optional n3: For a Field, each N3 assertion string is optional. N3
 * Strings that are incomplete after the substitution process will ignored as
 * optional.
 */
public class N3MultiPartUpload extends VitroHttpServlet {
    private static final int DEFAULT_MAX_SIZE = 1024 * 1024 * 1024;

    private static final String DEFAULT_FILE_URI_PREFIX = "http://vivo.library.cornell.edu/ns/0.1#file";

    private static final String DEFAULT_BASE_DIR = "/usr/local/vitrofiles";

    private static String fileUriPrefix = DEFAULT_FILE_URI_PREFIX;

    private static String baseDirectoryForFiles = DEFAULT_BASE_DIR;

    private static int maxFileSize = DEFAULT_MAX_SIZE;

    private static PostUpload postUpload = null;

    /**
     * Check to see if there is a postFileUpload object configured in the file
     * /WEB-INF/postFileUpload.jsp"
     */
    @Override
    public void init() throws ServletException {
		ConfigurationProperties configProperties = ConfigurationProperties
				.getBean(getServletContext());

		fileUriPrefix = configProperties.getProperty("n3.defaultUriPrefix",
				DEFAULT_FILE_URI_PREFIX);
		baseDirectoryForFiles = configProperties.getProperty(
				"n3.baseDirectoryForFiles", DEFAULT_BASE_DIR);

		String postUploadProcess = configProperties.getProperty("n3.postUploadProcess");
		System.out.println("Attempting to load postUploadProcess "
				+ postUploadProcess);
		postUpload = getPostUpload(postUploadProcess);

		String maxSize = configProperties.getProperty("n3.maxSize",	Long.toString(DEFAULT_MAX_SIZE));
		log.debug("Max size is " + maxSize);
		try {
			maxFileSize = Integer.parseInt(maxSize);
		} catch (NumberFormatException nfe) {
			log.error(nfe);
			maxFileSize = DEFAULT_MAX_SIZE;
		}
    }

    @Override
    protected void doPost(HttpServletRequest rawRequest, HttpServletResponse resp)
            throws ServletException, IOException {
        log.debug("N3MultiPartProcess 0.01");
        
		FileUploadServletRequest request = FileUploadServletRequest.parseRequest(rawRequest, maxFileSize);
   		if (request.hasFileUploadException()) {
            // TODO: forward to error message
            throw new ServletException("Size limit exceeded: " + request.getFileUploadException().getLocalizedMessage());
       	}
        if (!request.isMultipart()) {
            // TODO: forward to error message
            throw new ServletException("Must POST a multipart encoded request");
        }

        log.debug("multipart content detected: " + request.isMultipart());

        ServletContext application = getServletContext();
        HttpSession session = request.getSession();

        List<String> errorMessages = new ArrayList<String>();
        OntModel jenaOntModel = getJenaOntModel(request, application);

		EditConfiguration editConfig = EditConfiguration.getConfigFromSession(
				session, request);
		if (editConfig == null) {
			RequestDispatcher rd = request
			.getRequestDispatcher("/edit/messages/noEditConfigFound.jsp");
			rd.forward(request, resp);
			return;
		}
		
        EditSubmission submission = 
            new EditSubmission(request.getParameterMap(), editConfig, request.getFiles());
        EditN3Generator n3Subber = editConfig.getN3Generator();

        // check for form validation errors
        Map<String, String> errors = submission.getValidationErrors();
        EditSubmission.putEditSubmissionInSession(session, submission);

        if (errors != null && !errors.isEmpty()) {
            String form = editConfig.getFormUrl();
            request.setAttribute("formUrl", form);
            RequestDispatcher rd = request.getRequestDispatcher(form);
            rd.forward(request, resp);
            return;
        }

        boolean requestIsAnUpdate = editConfig.getObject() != null
                && editConfig.getObject().trim().length() > 0;
        log.debug(requestIsAnUpdate ? "request is an update for a file object"
                : "request is for a new file object");
        /** *************************************************** */
        String uploadFileName = "";
        if (requestIsAnUpdate) {
          log.error("Editing an existing file resource is not supported by N3MultiPartUpload.java ");
          request.setAttribute("errors", "Editing an existing file resource is not supported.");
          RequestDispatcher rd = request
                  .getRequestDispatcher("/edit/fileUploadError.jsp");
          rd.forward(request, resp);
          return;            
        } else { // do new file upload
            Map<String, List<Model>> requiredFieldAssertions = new HashMap<String, List<Model>>();

            boolean saveFileToDiskSuccess = false;
            for (String fileItemKey : request.getFiles().keySet()) {
                Field field = editConfig.getField(fileItemKey);
                if (field.getOptionsType() != Field.OptionsType.FILE) {
                    log.debug("Field "
                            + fileItemKey
                            + " is not of Field.OptionsType.FILE, "
                            + "assertion graphs for non-file Fields don't get processed by N3MultiPartProcess");
                    continue;
                }

                /* build the models from the field assertions for each file */
                for (FileItem fileItem : request.getFiles().get(fileItemKey)) {
                    try {
                        requiredFieldAssertions.putAll(buildModelsForFileItem(
                                fileItem, editConfig, submission, field
                                        .getAssertions(), jenaOntModel,
                                n3Subber, baseDirectoryForFiles));
                        saveFileToDiskSuccess = true;
                        if (log.isDebugEnabled()) {
                            log.debug("build assertions for field "
                                    + fileItemKey + " and file "
                                    + fileItem.getName() + "\n"
                                    + requiredFieldAssertions.get(fileItemKey));
                            
                        }
                        //DEBUG
                        System.out.println("build assertions for field "
                                + fileItemKey + " and file "
                                + fileItem.getName() + "\n"
                                + requiredFieldAssertions.get(fileItemKey));
                        //Save upload file name for use in email confirmation
                        uploadFileName = fileItem.getName();
                    } catch (Exception e) {
                        long t = System.currentTimeMillis();
                        log.error("uplaod ticket " + t + " " + e.getMessage(), e);
                        errors.put(fileItem.getFieldName(),e.getMessage());                                                
                        saveFileToDiskSuccess = false;
                        break;
                    }
                }
            }
            if ( ! saveFileToDiskSuccess) {
                if (errors != null && !errors.isEmpty()) {
                    String form = editConfig.getFormUrl();
                    request.setAttribute("formUrl", form);
                    RequestDispatcher rd = request.getRequestDispatcher(form);
                    rd.forward(request, resp);
                    return;
                }
            }else{                
                OntModel assertionModel = ModelFactory
                        .createOntologyModel(OntModelSpec.OWL_MEM);
                for (List<Model> modelList : requiredFieldAssertions.values()) {
                    for (Model model : modelList) {
                        assertionModel.add(model);
                    }
                }
                
                if( log.isDebugEnabled()){
                    StmtIterator it = assertionModel.listStatements();
                    while(it.hasNext()){
                        log.debug( it.nextStatement().toString() );
                    }
                    
                }
                
                //DEBUG to see what statements are being added
                StmtIterator it = assertionModel.listStatements();
                while(it.hasNext()){
                    System.out.println("NEXT Statement:" +  it.nextStatement().toString() );
                }

                /* ****** do PostUpload if there is one ******* */
                boolean postUploadSuccess = false;
                if (postUpload != null) {
                    log.debug("found post upload processing task ");
                    for (String fileURI : requiredFieldAssertions.keySet()) {
                        try {
                            Model pModel = postUpload.postUpload(
                                    assertionModel, jenaOntModel, application,
                                    fileURI);
                            if (pModel != null) {
                                postUploadSuccess = true;
                                assertionModel.add(pModel);
                            }
                        } catch (Exception re) {
                            long t = System.currentTimeMillis();
                            log.error("upload ticket " + t + " "
                                    + re.getMessage());
                            errorMessages
                                    .add("There was an error uploading your file, "
                                            + "upload ticket "
                                            + t
                                            + " "                                            
                                            + re.getMessage());
                            postUploadSuccess = false;
                            break;
                        }
                    }
                }
                if (postUploadSuccess) {
                    /* **** Save the models for all the files **** */
                    String editorUri = EditN3Utils.getEditorUri(request);
                    Lock lock = null;
                    try {
                        lock = jenaOntModel.getLock();
                        lock.enterCriticalSection(Lock.WRITE);
                        jenaOntModel.getBaseModel().notifyEvent(
                                new EditEvent(editorUri, true));
                        jenaOntModel.add(assertionModel);

                    } catch (Throwable t) {
                        errorMessages
                                .add("error adding edit change n3required model to in memory model \n"
                                        + t.getMessage());
                    } finally {
                        jenaOntModel.getBaseModel().notifyEvent(
                                new EditEvent(editorUri, false));
                        lock.leaveCriticalSection();
                    }
                }
            }
        }// end of do new file upload

        if (errorMessages.size() > 0) {
            request.setAttribute("errors", errorMessages);
            RequestDispatcher rd = request
                    .getRequestDispatcher("/edit/fileUploadError.jsp");
            rd.forward(request, resp);
        } else {
        	//This is before the redirect occurs from postEditCleanUp
        	//Send out email confirmation here
        	try {
        		sendUserEmail(request, session, uploadFileName);
        	} catch(Exception ex) {
        		System.out.println("Problem with retrieving and/or sending email");
        	}
        	
            RequestDispatcher rd = request
                    .getRequestDispatcher("/edit/postEditCleanUp.jsp");
            rd.forward(request, resp);
        }
    }

    private Map<String, List<Model>> buildModelsForFileItem(FileItem fileItem,
            EditConfiguration editConfig, EditSubmission submission,
            List<String> assertions, OntModel jenaOntModel,
            EditN3Generator n3Subber, String dataDir) throws Exception {
        Map<String, List<Model>> requiredFieldAssertions = new HashMap<String, List<Model>>();
        List<Model> modelList = new ArrayList<Model>(assertions.size());

        /* **** Save file and add file properties ***** */
        assertions = saveFileAndSubInFileInfo(fileItem, dataDir, n3Subber,
                assertions);
        if (log.isDebugEnabled())
            log.debug("subsititued in literals from file save: " + assertions);

        /* **** Make a URI for the file that was just saved **** */
        String newFileUri = makeNewUri(editConfig.getNewResources().get(
                "fileURI"), jenaOntModel);
        assertions = EditN3Generator.subInUris("fileURI", newFileUri, assertions);

        /* **** URIs and Literals on Form/Parameters ** */
        assertions = EditN3Generator.subInUris(submission.getUrisFromForm(),
                assertions);
        assertions = n3Subber.subInLiterals(submission.getLiteralsFromForm(),
                assertions);
        if (log.isDebugEnabled())
            log.debug("subsititued in literals from form: " + assertions);

        /* **** URIs and Literals in Scope ************ */
        assertions = EditN3Generator
                .subInUris(editConfig.getUrisInScope(), assertions);
        assertions = n3Subber.subInLiterals(editConfig.getLiteralsInScope(),
                assertions);
        if (log.isDebugEnabled())
            log.debug("subsititued in URIs and Literals from scope:"
                    + assertions);

        /* **** build models from N3 assertion strings */
        for (String n3 : assertions) {
            try {
                Model model = ModelFactory.createDefaultModel();
                StringReader reader = new StringReader(n3);
                model.read(reader, "", "N3");
                modelList.add(model);
            } catch (Throwable t) {
                log.debug("error processing N3 assertion string "
                        + "for field " + fileItem.getFieldName() + " for file "
                        + fileItem.getName() + "\n" + t.getMessage() + '\n'
                        + "n3: \n" + n3);
            }
        }
        requiredFieldAssertions.put(newFileUri, modelList);
        return requiredFieldAssertions;
    }

    private List<String> saveFileAndSubInFileInfo(FileItem fileItem,
            String dataDir, EditN3Generator n3generator, List<String> assertions)
            throws Exception {
        String originalName = fileItem.getName();
        String name = originalName.replaceAll("[,+\\\\/$%^&*#@!<>'\"~;]", "_");
        name = name.replace("..", "_");
        name = name.trim().toLowerCase();

        String saveLocation = dataDir + File.separator + name;
        String savedName = name;
        int next = 0;
        boolean foundUnusedName = false;
        while (!foundUnusedName) {
            File test = new File(saveLocation);
            if (test.exists()) {
                next++;
                savedName = name + '(' + next + ')';
                saveLocation = dataDir + File.separator + savedName;
            } else {
                foundUnusedName = true;
            }
        }

        File uploadedFile = new File(saveLocation);
        try {
            fileItem.write(uploadedFile);
        } catch (Exception ex) {
            log.error("Unable to save POSTed file. " + ex.getMessage());
            throw new Exception("Unable to save file to the disk. "
                    + ex.getMessage());
        }

        if( fileItem.getSize() < 1){
            throw new Exception("No file was uploaded or file was empty.");
        }else{        
            Map<String, Literal> fileValues = new HashMap<String, Literal>();
            fileValues.put("fileName", ResourceFactory
                    .createTypedLiteral(originalName));
            fileValues.put("fileSaveLocation", ResourceFactory
                    .createTypedLiteral(saveLocation));
            fileValues.put("fileSavedName", ResourceFactory
                    .createTypedLiteral(savedName));
            fileValues.put("fileContentType", ResourceFactory
                    .createTypedLiteral(fileItem.getContentType()));
            fileValues.put("fileSize", ResourceFactory.createTypedLiteral(fileItem
                    .getSize()));        
            assertions = n3generator.subInLiterals(fileValues, assertions);                        
        }
        return assertions;
    }

    public static String makeNewUri(String prefix, Model model) {
        if (prefix == null || prefix.length() == 0)
            prefix = fileUriPrefix;

        String uri = prefix + Math.abs(random.nextInt());
        Resource r = ResourceFactory.createResource(uri);
        while (model.containsResource(r)) {
            uri = prefix + Math.abs(random.nextInt());
            r = ResourceFactory.createResource(uri);
        }
        return uri;
    }

    private OntModel getJenaOntModel(HttpServletRequest request,
            ServletContext application) {
        Object sessionOntModel = request.getSession().getAttribute(
                "jenaOntModel");
        OntModel jenaOntModel = (sessionOntModel != null && sessionOntModel instanceof OntModel) ? (OntModel) sessionOntModel
                : (OntModel) application.getAttribute("jenaOntModel");
        return jenaOntModel;
    }

    private PostUpload getPostUpload(String postUploadProcess) {
        PostUpload newPu = null;
        // this is hard coded for now but needs to be configured some how.
        String name = "edu.cornell.mannlib.datastar.DataStarPostUpload";

        try {
            Class<?> newClass = Class.forName(name);
            Object obj = newClass.newInstance();
            if (obj instanceof PostUpload) {
                newPu = (PostUpload) obj;
            }
        } catch (Throwable ex) {
            log
                    .error("could not build PostUpload object for Class "
                            + name, ex);
        }
        if (newPu != null) {
            log.info("using postUploadProcess " + name);
        }
        return newPu;
    }

    public static interface PostUpload {
        public String checkStatus(OntModel model, ServletContext context)
                throws Exception;
  
        public Model postUpload(OntModel model, OntModel fullModel,
                ServletContext context, String fileURI) throws Exception;
    }

    public Map<String, List<String>> fieldsToAssertionMap(
            Map<String, Field> fields) {
        Map<String, List<String>> out = new HashMap<String, List<String>>();
        for (String fieldName : fields.keySet()) {
            Field field = fields.get(fieldName);

            List<String> copyOfN3 = new ArrayList<String>();
            for (String str : field.getAssertions()) {
                copyOfN3.add(str);
            }
            out.put(fieldName, copyOfN3);
        }
        return out;
    }

    public Map<String, List<String>> fieldsToRetractionMap(
            Map<String, Field> fields) {
        Map<String, List<String>> out = new HashMap<String, List<String>>();
        for (String fieldName : fields.keySet()) {
            Field field = fields.get(fieldName);

            List<String> copyOfN3 = new ArrayList<String>();
            if( field.getRetractions() != null ){
                for (String str : field.getRetractions()) {
                    copyOfN3.add(str);
                }
            }
            out.put(fieldName, copyOfN3);
        }
        return out;
    }
    
    public void sendUserEmail(HttpServletRequest request, HttpSession session, String uploadFileName) {
    	UserAccount userAccount = LoginStatusBean.getCurrentUser(request);
    	if (userAccount == null) {
    		return;
    	}
    	
        try{
	        System.out.println("User URI is " + userAccount.getUri());
	        String email = userAccount.getEmailAddress();
	        String deliveryFrom = "hjk54@cornell.edu";//TO DO: replace with email address to be used
	        //Now send message
	        MailUtil mu = new MailUtil(request);
	        List<String> deliverToArray = new ArrayList<String>();
	        deliverToArray.add(email);
	        
	        //Compose body of message
	        StringBuffer msgBuf = new StringBuffer();
	        String lineSeparator = System.getProperty("line.separator"); 
	        msgBuf.setLength(0);
	        msgBuf.append("<html>" + lineSeparator );
	        msgBuf.append("<head>" + lineSeparator );
	        msgBuf.append("<style>a {text-decoration: none}</style>" + lineSeparator );
	        msgBuf.append("<title>" + deliveryFrom + "</title>" + lineSeparator );
	        msgBuf.append("</head>" + lineSeparator );
	        msgBuf.append("<body>" + lineSeparator );
	        String messageBody = "<h4>File has been uploaded to datastar";
	        //Include file name if it exists and is not empty
	        if(uploadFileName != null && uploadFileName != "") {
	        	messageBody += ": " + uploadFileName;
	        }
	        messageBody += "</h4>";
	        msgBuf.append(messageBody + lineSeparator + "</body></html>");
	        String messageText = msgBuf.toString();
	        
	        //Send message
	        mu.sendMessage(messageText, "Datastar File Upload: Success", deliveryFrom, email, deliverToArray);
        } catch(Exception ex) {
        	System.out.println("Error " + ex.getMessage());
        }
	}

//    private boolean logAddRetract(String msg, Map<String, List<String>> add,
//            Map<String, List<String>> retract) {
//        log.debug(msg);
//        if (add != null)
//            log.debug("assertions: " + add.toString());
//        if (retract != null)
//            log.debug("retractions: " + retract.toString());
//        return true;
//    }

    static Random random = new Random();

    Log log = LogFactory.getLog(N3MultiPartUpload.class);
}
