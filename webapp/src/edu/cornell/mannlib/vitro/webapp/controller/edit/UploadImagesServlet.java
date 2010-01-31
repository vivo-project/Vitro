/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit;

/**
 * @version 0.9 2004-01-29
 * @author Jon Corson-Rikert
 *
 * UPDATES:
 * 2005-07-22 jc55  added support for entering remote image URL when uploading thumbnail image
 * 2005-06-30 jc55  added support for home parameter
 */

/************** DOCUMENTATION *********************
 * This servlet uses 3 directory locations on the server, which for illustrative purposes
 *     we assume has the Tomcat application context at /usr/local/tomcat/webapps/vivo
 *     and the source code and build files at /usr/local/src/Vitro/dream
 *
 *  1) workspaceDir: a temp directory where the file is uploaded to and reports are stored
 *  2) websiteDir  : a website directory where the image is copied so that it appears on the website immediately after upload
 *  3) sourceDir   : the directory in the source tree where the images are stored so that the context can be recreated and/or moved
 *
 */

import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import javax.servlet.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.LoginFormBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

public class UploadImagesServlet extends VitroHttpServlet {
    private static final Log log = LogFactory.getLog(UploadImagesServlet.class.getName());
    private String sourceDirName;  // all uploaded images are copied to the source directory, not just the application context
    private String websiteDirName; // the application context
    private String webAppName;     // the name of the application context (e.g., vivo, usaep)

    private static String UPLOAD_SERVLET_PROPERTIES = "/upload.properties";
    private static String UPLOAD_SERVLET_PROPERTIES_WIN="\\upload.properties";

    /**
     * Notice that init() gets called the first time the servlet is requested,
     * at least under tomcat 5.5.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // something like: /usr/local/tomcat/webapps/vivo
        websiteDirName = getServletContext().getRealPath("");

        //get the last directory, which should be the webapp name.
        String[] dirs = new String[0];
        if (File.separator.equals("\\")) {
            dirs =  websiteDirName.split("\\\\");
        } else {
            dirs =  websiteDirName.split(File.separator);
        }
        webAppName = dirs[dirs.length-1];

        // something like: /usr/local/src/Vitro/dream/common/web
        try{
            sourceDirName = getSourceDirName();
        }catch(Exception ex){
            log.error("initialization Exception: "+ex.getMessage());
        }

        log.info("UploadImagesServlet initialized to copy uploaded images to source directory: " + sourceDirName);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
        doPost(request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doPost(HttpServletRequest req,
            HttpServletResponse response)
        throws ServletException, IOException
    {
    	
    	VitroRequest request = new VitroRequest(req);
    	
        boolean overwriteExistingImage=false;
        //BufferedReader in = null;
        PrintWriter out = null;

        File sourceDir;    // the working source directory for the website -- put the file here so if the site is refreshed the file won't be lost
        File contentDir;   // the actual website directory -- make a copy there so the user can see the uploaded file before the next ant deploy
        File tempDir;      // a temporary directory in the Tomcat context where files are uploaded before copying to sourceDir and websiteDir, and where reports are stored

        String paramStr = "<table align='center' width='75%'><tr><td align='left'>";
        String destinationStr=null;
        String imageTypeStr=null;
        String primaryContentTypeStr=null;
        ArrayList secondaryContentTypeList=null;

        String individualURI=null;

        String userName=null;
        HttpSession session = request.getSession();
        LoginFormBean fb = (LoginFormBean) session.getAttribute("loginHandler");
        String tempDirName=null;
        if ( fb != null ) {
            userName = fb.getLoginName();
            tempDirName = websiteDirName + "/" + "batch";
            tempDir = new File(tempDirName);
            if (!tempDir.exists() ) {
                tempDir.mkdir();
                paramStr += "<p>Created new temporary working upload directory: " + tempDir.toString() + "</p>";
            }
            tempDirName += "/" + userName;
            tempDir = new File(tempDirName);
            if (!tempDir.exists()) {
                tempDir.mkdir();
                paramStr += "<p>Created new temporary working upload directory for user: " + userName + ": " + tempDir.toString() + "</p>";
            }
        } else {
            request.setAttribute("processError","User name not decoded from login formbean");
            getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
            return;
        }

        // Use an advanced form of the constructor that specifies a character encoding
        // of the request (not of the file contents) and a file rename policy.
        MultipartRequest multi = new MultipartRequest(request,tempDirName,10*1024*1024,"ISO-8859-1",new DefaultFileRenamePolicy());

        String userStr="unknown"; // could get this from userName above
        String remoteLocStr=null;

        paramStr += "<p>PARAMS: <br><ul>";
        Enumeration params = multi.getParameterNames();
        while (params.hasMoreElements()) {
            String name = (String)params.nextElement();
            if ( name.equalsIgnoreCase("entityUri")) {
                individualURI = multi.getParameter(name);
                paramStr += "<li>Individual URI = " + individualURI + "</li>";
                request.setAttribute("entityUri", individualURI );
            } else if ( name.equalsIgnoreCase("mode")) {
                String modeStr = multi.getParameter(name);
                overwriteExistingImage = modeStr.equalsIgnoreCase("replace") ? true : false;
                paramStr += "<li>overwriting existing image = " + overwriteExistingImage + "</li>";
            } else if ( name.equalsIgnoreCase("submitter")) {
                userStr = multi.getParameter(name);
                //workDirName += "/" + userStr;
                paramStr += "<li>user " + userStr + " storing in server directory = " + websiteDirName + "</li>";
            } else if ( name.equalsIgnoreCase("submitMode")) {
                String submitStr = multi.getParameter(name);
                paramStr += "<li>submitted via button: " + submitStr + "</li>";
            } else if ( name.equalsIgnoreCase("destination")) {
                destinationStr = multi.getParameter(name);
                paramStr += "<li>destination directory: " + destinationStr + "</li>";
                request.setAttribute("destination", destinationStr );
            } else if ( name.equalsIgnoreCase("type")) {
                imageTypeStr = multi.getParameter(name);
                paramStr += "<li>imageType: " + imageTypeStr + "</li>";
                request.setAttribute("type", imageTypeStr );
            } else if ( name.equalsIgnoreCase("contentType")) {
                String contentTypeStr = multi.getParameter(name);
                paramStr += "<li>acceptable content types: " + contentTypeStr + "</li>";
                StringTokenizer acceptedTypeTokens = new StringTokenizer( contentTypeStr,"/");
                int partCount = acceptedTypeTokens.countTokens();
                if ( partCount > 0 ) {
                    secondaryContentTypeList = new ArrayList();
                    for (int i=0; i<partCount; i++ ) {
                        switch (i) {
                            case 0: primaryContentTypeStr = acceptedTypeTokens.nextToken(); break;
                            default: secondaryContentTypeList.add(acceptedTypeTokens.nextToken());break;
                        }
                    }
                }
            } else if ( name.equalsIgnoreCase("home")) {
                String portalIdStr=multi.getParameter(name);
                request.setAttribute("home",portalIdStr);
            } else if ( name.equalsIgnoreCase("remoteURL")) {
                remoteLocStr=multi.getParameter(name);
            } else {
                String value = multi.getParameter(name);
                paramStr += "<li>unexpected parameter [" + name + "] =" + value + "</li>";
            }
        }

        try {
        	
        	sourceDir = null;
        	try {
	            sourceDir = new File(sourceDirName);
	            if (!sourceDir.exists()) {
	                sourceDir.mkdir();
	                paramStr += "<li>Created new modifications directory in source area from which app is deployed: " + sourceDir.toString() + "</li>";
	            }
	            sourceDir = new File(sourceDirName + "/images");
	            if (!sourceDir.exists()) {
	                sourceDir.mkdir();
	                paramStr += "<li>Created new image directory: " + sourceDir.toString() + "</li>";
	            }
	            StringTokenizer uploadTokens = new StringTokenizer( destinationStr,"/");
	            int uploadDepthCount = uploadTokens.countTokens();
	            if ( uploadDepthCount > 0 ) {
	                for (int i=0; i<uploadDepthCount; i++ ) {
	                    String nextDirStr = uploadTokens.nextToken();
	                    sourceDir = new File( sourceDir.getAbsolutePath() + "/" + nextDirStr);
	                    if ( !sourceDir.exists() ) {
	                        sourceDir.mkdir();
	                        paramStr += "<li>Created new source directory: " + sourceDir.toString() + "</li>";
	                    }
	                }
	            }
        	} catch (Exception e) {
        		log.warn("Unable to use source directory to back up uploaded image", e);
        	}

            String contentDirName = websiteDirName;
            // check if top level output directory exists
            contentDir = new File(contentDirName);
            if (!contentDir.exists()) {
                contentDir.mkdir();
                paramStr += "<li>Created new web site directory: " + contentDir.toString() + "</li>";
            }
            contentDirName += "/" + "images";
            contentDir = new File( contentDirName );
            if (!contentDir.exists()) {
                contentDir.mkdir();
                paramStr += "<p>Created new website content directory " + contentDir.toString() + "</p>";
            }
            StringTokenizer outputTokens = new StringTokenizer( destinationStr,"/");
            int outputDepthCount = outputTokens.countTokens();
            if ( outputDepthCount > 0 ) {
                for (int i=0; i<outputDepthCount; i++ ) {
                    String nextDirStr = outputTokens.nextToken();
                    contentDir = new File( contentDir.getAbsolutePath() + "/" + nextDirStr);
                    if (!contentDir.exists()) {
                        contentDir.mkdir();
                        paramStr += "<li>Created new content directory: " + contentDir.toString() + "</li>";
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Exception when creating directories ", ex);
            request.setAttribute("processError","Upload failed: unable to create directories for uploads.  See error log for details.");
            getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
            return;
        }

        paramStr += "</ul></p></td></tr></table>";
        request.setAttribute("prevparams", paramStr );

        String originalFileName = null;
        String infileFullPathName = null;
        String filesystemName = null;

        Enumeration files = multi.getFileNames();
        if (files.hasMoreElements()) {
            String thisInputName = (String)files.nextElement();
            filesystemName = multi.getFilesystemName(thisInputName); // file name after any renaming, e.g., if file by that name already exists
            originalFileName = multi.getOriginalFileName(thisInputName); // before renaming policy applied
            if (thisInputName == null || thisInputName.equals("")) {
                log.error("No input file provided for upload");
                request.setAttribute("processError","Error: no input file provided for upload");
                getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
                return;
            }

            String typeStr = multi.getContentType(thisInputName);
            if ( typeStr==null || typeStr.equals("")) {
                log.error("Error: if input file provided, it has no readable file type");
                request.setAttribute("processError","Error: if an input file was provided, it has no readable file type");
                getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
                return;
            }

            StringTokenizer contentTypeTokens = new StringTokenizer( typeStr,"/");
            int typeTokenCount = contentTypeTokens.countTokens();
            if ( typeTokenCount == 2 ) {
                for (int i=0; i<typeTokenCount; i++ ) {
                    String partStr = contentTypeTokens.nextToken();
                    switch (i) {
                        case 0: if (!partStr.equalsIgnoreCase(primaryContentTypeStr)) {
                                    log.error("Error: file uploaded (" + originalFileName + ") is not of primary content type '" + primaryContentTypeStr + "'");
                                    request.setAttribute("processError","Error: file uploaded (" + originalFileName + ") is not of primary content type '" + primaryContentTypeStr + "'");
                                    getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
                                    return;
                                }
                                break;
                        case 1: if (secondaryContentTypeList.size() > 0) {
                                    Iterator typeIter = secondaryContentTypeList.iterator();
                                    boolean typeMatch=false;
                                    String typeConcat=primaryContentTypeStr;
                                    int count=0;
                                    while ( typeIter.hasNext() ) {
                                        String whichType = (String)typeIter.next();
                                        if ( count == 0 ) {
                                            typeConcat="/" + whichType;
                                        } else {
                                            typeConcat+=" or " + primaryContentTypeStr + "/" + whichType;
                                        }
                                        ++count;
                                        if (whichType.equals("*") || whichType.equalsIgnoreCase(partStr)) {
                                            typeMatch=true;
                                        }
                                    }
                                    if (!typeMatch) {
                                        log.error("Error: file uploaded (" + originalFileName + ") has content type " + typeStr + " that does not match '" + primaryContentTypeStr + typeConcat + "'");
                                        request.setAttribute("processError","Error: file uploaded (" + originalFileName + ") has content type " + typeStr + " that does not match " + primaryContentTypeStr + typeConcat );
                                        getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
                                        return;
                                    }
                                } // else any secondary type accepted
                                break;
                    }
                }
            } else {
                log.error("Error: file uploaded (" + originalFileName + ") has unrecognized content type '" + typeStr + "'");
                request.setAttribute("processError","Error: file uploaded (" + originalFileName + ") has unrecognized content type '" + typeStr + "'");
                getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
                return;
            }

            File f = multi.getFile(thisInputName); // see Core Java v1 pp 769 onward on File managment
            if (f != null) {
                 infileFullPathName = f.toString();
            }
        }

        request.setAttribute("input",originalFileName); // but can't specify value parameter for form inputs of type file

        int posDot = originalFileName.lastIndexOf('.'); //filesystemName.lastIndexOf('.') to increment versions;
        try {
            // BufferedReader handles the input file like a TEXT file (you can read lines from it)
            // BufferedInputStream handles the input file like a BINARY file (if you want to read a line from it you must read
            //   it character by character until finding the line separator)
            // They are meant to do DIFFERENT things -- use the class that is more suitable for your task
            // For uploading text and parsing it use: BufferedReader in = new BufferedReader(new FileReader(infileFullPathName));
            out= new PrintWriter( new FileWriter( tempDir + "/" + originalFileName.substring(0,posDot) + ".html"));
            request.setAttribute("outputLink","<a target='_new' href='batch/" + userName + "/" + originalFileName.substring(0,posDot) + ".html'>"+originalFileName.substring(0,posDot)+".html</a>");

        } catch ( IOException ex ) {
            request.setAttribute("processError","error creating report file " + tempDir + "/" + originalFileName.substring(0,posDot) + ".html: " + ex.getMessage());
            getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
            return;
        }

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Upload Report</title>");
        out.println("<script language='Javascript'>");
        out.println("function destroy( windowRef ) {");
        out.println("  if (windowRef && !windowRef.closed) {");
        out.println("     windowRef.close();");
        out.println("  }");
        out.println("}");
        out.println("</script>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Image Upload Report</h2>");

        out.println( loadImage(request,originalFileName,overwriteExistingImage,destinationStr,imageTypeStr,individualURI,remoteLocStr));

        String contextName = request.getContextPath(); // e.g., /vivo
        log.info("context name from getContextPath(): " + contextName);

        out.println("<p><a href='" +  contextName + File.separator + "images" + File.separator + destinationStr + File.separator + originalFileName + "'>" + originalFileName + "</a></p>");
        out.println("<p><img width='100' src='" + contextName + File.separator + "images" + File.separator + destinationStr + File.separator + originalFileName + "' alt='" + originalFileName + "'/></p>");
        out.println("<p><img src='" + contextName + File.separator + "images" + File.separator + destinationStr + File.separator + originalFileName + "' alt='" + originalFileName + "'/></p>");

        // Actually open the input file for copying
        BufferedInputStream input = new BufferedInputStream(new FileInputStream(infileFullPathName));


        // Create the copies of the uploaded file
        FileOutputStream sourceFile = null;     // The copy of the uploaded file for the working directory
        FileOutputStream contentFile = null;  // the copy put directly on the web site so the user can see it before next ant deploy is done
        if (sourceDir != null) {
	        try {
	            sourceFile = new FileOutputStream(sourceDir + File.separator + originalFileName); // fileSystemName to increment versions); // apparently don't need File.separator
	        } catch (FileNotFoundException fnf ) {
	            request.setAttribute("processError","Warning: could not create image backup file (" + sourceDir + File.separator + originalFileName);
	        }
        } else {
        	String msg = "<p>Warning: unable to make a backup copy of uploaded image.</p>";
        	Object processErrorAttribute = request.getAttribute("processError");
        	if ( (processErrorAttribute != null) && (processErrorAttribute instanceof String) ) {
        		request.setAttribute("processError",((String)processErrorAttribute)+msg);
        	} else {
        		request.setAttribute("processError",msg);
        	}
        }
        try {
            contentFile  = new FileOutputStream(contentDir + File.separator + originalFileName); // fileSystemName to increment versions);
        } catch (FileNotFoundException fnf) {
            out.println("Error: the image file cannot be created<br/>");
            out.println(fnf.getMessage() + "<br/>");
            request.setAttribute("processError","Error: could not create image file (" + contentDir + File.separator + originalFileName);
            getServletConfig().getServletContext().getRequestDispatcher("/uploadimages.jsp").forward( request, response );
            return;
        }


         /* Read from the Input Stream and write into the File OutputStream */
        int length = 1000;
        byte[] byteArray = new byte[1000];
        try {
            length = input.read( byteArray );
            while (length != -1) {
                if (sourceFile != null)
                    sourceFile.write( byteArray, 0, length);
                contentFile.write( byteArray, 0, length);
                length = input.read( byteArray );
            }
        } catch (IOException ioe) {
            out.println("The input file does not seem to be readable (IO Error): </br>");
            out.println( ioe.getMessage() + "<br/>");
        } catch (Exception e) {
            out.println("<br> The Following Exception occured in the servlet:</br>");
            out.println("<br>" + e.toString() + " </br>");
        }

        if (sourceFile != null)
            sourceFile.close();
        contentFile.close();

        try { // now delete input file from temp directory
            out.println("<p>opening " + infileFullPathName + " for deletion</p>");
            File tempFile = new File( infileFullPathName );
            if ( tempFile.exists()) {
                try {
                    boolean gone = tempFile.delete();
                    if ( gone ) {
                        out.println("<p>deleted file " + infileFullPathName + " since has been copied to web site</p>");
                    } else {
                        out.println("<p>could not delete file " + infileFullPathName + "</p>");
                    }
                } catch ( Exception ex ) {
                    out.println("<p>Exception: " + ex.getMessage() + "<br>");
                    ex.printStackTrace ();
                    out.println("</p>");
                }
            } else {
                out.println("<p>Error -- file " + infileFullPathName + " does not exist</p>");
            }
        } catch (Exception ex) {
            out.println("<p>Exception: " + ex.getMessage() + "<br/>");
            ex.printStackTrace ();
            out.println("</p>");
        }

        out.println("<form name='closeForm'><input type='submit' value='close window' onclick='destroy(window)'></form>");
        out.println("</body></html>");
        out.flush();
        out.close();

        getServletContext().getRequestDispatcher( "/uploadimages.jsp" ).forward( request, response );
    }


    public String loadImage(HttpServletRequest request,String fileName,boolean overwriteExisting,String destination,String imageType,String individualURI,String optionalRemoteLocStr)
    {
        String messageStr="<p>";

        // first check to verify that individual exists .
        Individual individual = getWebappDaoFactory().getIndividualDao().getIndividualByURI(individualURI);
        String recordName = null, previousImageStr=null;
        try {
            if (individual != null) {
                recordName = individual.getName();
                previousImageStr = individual.getImageThumb();
                messageStr += "<p>Uploading file for individual: " + recordName + "</p>";
            } else {
                log.error("Error: no individual found with URI " + individualURI);
                request.setAttribute("processError","Error: no individual found with URI " + individualURI);
                messageStr += "Error: no individual found with URI " + individualURI + "</p>";
                return messageStr;
            }
        } catch ( Exception ex ) {
            log.error("Error: exception on checking individual URI " + individualURI + ": " + ex.getMessage());
            request.setAttribute("processError","Error: exception on checking individual URI " + individualURI + ": " + ex.getMessage());
            return messageStr;
        }

        boolean individualUpdated=false;
        boolean noExistingImage=(previousImageStr==null || previousImageStr.equals(""))? true : false;
        if (noExistingImage || overwriteExisting) {
            if (imageType.equalsIgnoreCase("thumb")) {
                if (optionalRemoteLocStr!=null && !optionalRemoteLocStr.equals("") && !optionalRemoteLocStr.equals("http://")) {
                    individual.setImageFile(optionalRemoteLocStr);
                    individual.setImageThumb(destination+"/"+fileName);
                } else {
                    individual.setImageThumb(destination+"/"+fileName);
                }
            } else {
                individual.setImageFile(destination+"/"+fileName);
            }
            try {
                getWebappDaoFactory().getIndividualDao().updateIndividual(individual);
                individualUpdated=true;
            } catch ( Exception ex ) {
                log.error("Error: Exception on getWebappDaoFactory().getIndividualDao().updateIndividual(" + individualURI +"); message: " + ex.getMessage());
                request.setAttribute("processError","Error: Exception on getWebappDaoFactory().getIndividualDao().updateIndividual(" + individualURI +"); message: " + ex.getMessage());
                return messageStr;
            }
        } else if (!noExistingImage) {
            if (imageType.equalsIgnoreCase("thumb")) {
                messageStr += "<p>This individual already has a thumbnail image associated with it: " + previousImageStr + "</p>";
            } else {
                messageStr += "<p>This individual already has an optional large-size image associated with it: " + previousImageStr + "</p>";
            }
        }

        messageStr += "<table width='70%' border='1' cellspacing='1' cellpadding='1'>";
        messageStr += "<tr><th>individual id</th><th>image</th></tr>";
        messageStr += "<tr align='center'>";
        messageStr += "<td>" + individualURI + "</td>";
        messageStr += "<td>" + ((fileName == null || fileName.equals("")) ? "<font color='red'>missing image file name</font>" : fileName )   + "</td>";
        messageStr += "</tr>";
        messageStr += "</table>";

        try {
            if (individualUpdated) {
                request.setAttribute("processError","updated individual <a href=\"entity?uri=" + java.net.URLEncoder.encode(individualURI,"UTF-8") + "\">"+recordName+"</a>");
            } else {
                request.setAttribute("processError","individual <a href=\"entity?uri=" + java.net.URLEncoder.encode(individualURI,"UTF-8") + "\">"+recordName+"</a> already has an image: please confirm if you wish to replace it");
            }
        } catch (UnsupportedEncodingException ex) {
            request.setAttribute("processError","Could not create link to individual "+recordName+" (URI: "+individualURI+")");
        }

        return messageStr;
    }

    /**
     * attempts to get the property file from UPLOAD_SERVLET_PROPERTIES
     * and returns the property UploadImagesServlet.sourceDirName.
     * @return returns the property UploadImagesServlet.sourceDirName
     * @throws IOException
     */
    private  String getSourceDirName() throws IOException{
        Properties props = new Properties();
        InputStream raw = this.getClass().getResourceAsStream( UPLOAD_SERVLET_PROPERTIES );
        if (raw == null) {
            raw = this.getClass().getResourceAsStream( UPLOAD_SERVLET_PROPERTIES_WIN );
            if (raw == null)
                throw new IOException("UploadImagesServlet.getSourceDirName()" +
                        " Failed to find resource: " + UPLOAD_SERVLET_PROPERTIES );
        }
        try{
            props.load( raw );
        } catch (Exception ex){
            throw new IOException("unable to load upload.properties file: " + ex.getMessage());
        } finally {
            raw.close();
        }
        String dirName = props.getProperty("UploadImagesServlet.sourceDirName");
        if( dirName == null ) {
            log.error("getSourceDirName(): property sourceDirName not defined in upload.properties");
        } else {
            File dir = new File(dirName);
            if(!dir.exists()) {
                log.warn("getSourceDirName(): " +
                    "The specified upload directory "+ dirName + " does not exist. " +
                    "Not saving upload images to source dir.");
            }
            if(!dir.canWrite()) {
                log.warn("getSourceDirName(): " +
                    "The specified upload directory "+ dirName + " is not writable." +
                    " Not saving upload images to source dir.");
            }
        }
        return dirName;
    }

    private static String stripDoubleQuotes( String termStr ) {
        if (termStr==null || termStr.equals("")) {
            return termStr;
        }
        int whichChar = 32; //double quote
        int characterPosition= -1;
        while ( ( characterPosition = termStr.indexOf( whichChar, characterPosition+1 ) ) >= 0 ) {
            termStr = termStr.substring( characterPosition+1 );
            ++characterPosition;
        }
        return termStr;
    }


    private static String escapeSingleQuotes( String termStr ) {
        if (termStr==null || termStr.equals("")) {
            return termStr;
        }
        int whichChar = 39; //single quote
        int characterPosition= -1;
        while ( ( characterPosition = termStr.indexOf( whichChar, characterPosition+1 ) ) >= 0 ) {
            if ( characterPosition == 0 ) // just drop it
                termStr = termStr.substring( characterPosition+1 );
            else if ( termStr.charAt(characterPosition-1) != 92 ) // already escaped
                termStr = termStr.substring(0,characterPosition) + "\\" + termStr.substring(characterPosition);
            ++characterPosition;
        }
        return termStr;
    }


    private static String stripLeadingAndTrailingSpaces( String termStr ) {
        int characterPosition= -1;

        while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
            termStr = termStr.substring(characterPosition+1);
        }
        while ( termStr.indexOf(32) >= (termStr.length()-1) ) {
            termStr = termStr.substring(0,termStr.length()-1);
        }
        return termStr;
    }
}



