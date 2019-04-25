/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.jena;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.stream.StreamSource;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ModelMaker;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

@WebServlet(name = "JenaXMLFileUpload", urlPatterns = {"/jenaXmlFileUpload/*"} )
public class JenaXMLFileUpload  extends JenaIngestController  {
	Log log = LogFactory.getLog(JenaXMLFileUpload.class);
	private String baseDirectoryForFiles;
	private int maxFileSize = 1024 * 1024 * 500;

	private XsltExecutable xsltExec;
	private Processor processor;

	public void init() throws ServletException {
		super.init();
		File baseDir = new File( getServletContext().getRealPath("/xmlFileUpload"));

		if( baseDir.exists() && baseDir.isDirectory() ){
			System.out.println("JenaXMLFileUpload, found upload directory of " + baseDir.getAbsolutePath());
		}else{
			System.out.println("Attemping to setup JenaXMLFileUpload with temp directory of " + baseDir.getAbsolutePath());
			baseDir.mkdir();
			if( baseDir.exists() && baseDir.isDirectory() ){
				System.out.println("Created directory " + baseDir.getAbsolutePath());
			}else{
				System.out.println("Could not create directory " + baseDir.getAbsolutePath());
			}
		}
		baseDirectoryForFiles = baseDir.getAbsolutePath();

		File xslt = new File(getServletContext().getRealPath("/xslt/xml2rdf.xsl"));
		System.out.println("JenaXMLFileUpload, attempting to load xslt " + xslt.getAbsolutePath());
		processor = new Processor(false);
		XsltCompiler compiler  = processor.newXsltCompiler();
		try {
			xsltExec = compiler.compile(new StreamSource( xslt ));
			System.out.println("JenaXMLFileUpload, loaded " + xslt.getAbsolutePath());
		} catch (SaxonApiException e) {
			System.out.println("could not compile xslt/xml2rdf.xsl" );
			System.out.println(e.getMessage());
		}
	}

	@Override
	public long maximumMultipartFileSize() {
		return maxFileSize;
	}

	@Override
	public boolean stashFileSizeException() {
		return true;
	}

	/**
	 * Each file will be converted to RDF/XML and loaded to the target model.
	 * If any of the files fail, no data will be loaded.
	 *
	 * parameters:
	 * targetModel - model to save to
	 * defaultNamespace - namespace to use for elements in xml that lack a namespace
	 *
	 */
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse resp)
	throws ServletException, IOException {
		VitroRequest vreq = new VitroRequest(request);
		if (vreq.hasFileSizeException()) {
			throw new ServletException("Size limit exceeded: "
					+ vreq.getFileSizeException().getLocalizedMessage());
		}
        if (vreq.isMultipart()) {
        	log.debug("multipart content detected");
        } else {
            // TODO: forward to error message
            throw new ServletException("Must POST a multipart encoded request");
        }

		if (!isAuthorizedToDisplayPage(request, resp,
				SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION)) {
        	return;
        }

        ModelMaker modelMaker = getModelMaker(vreq);
        String targetModel = request.getParameter("targetModel");
		if (targetModel == null) {
			throw new ServletException("targetModel not specified.");
		}

        Model m = modelMaker.getModel(targetModel);
        if( m == null )
        	throw new ServletException("targetModel '" + targetModel + "' was not found.");
        request.setAttribute("targetModel", targetModel);

        List<File> filesToLoad = saveFiles( vreq.getFiles() );
        List<File> rdfxmlToLoad = convertFiles( filesToLoad);
        List<Model> modelsToLoad = loadRdfXml( rdfxmlToLoad );

        try{
        	m.enterCriticalSection(Lock.WRITE);
			for(Model model : modelsToLoad ){
				m.add(model);
			}
        } finally {
        	m.leaveCriticalSection();
        }

        long count = countOfStatements(modelsToLoad);
        request.setAttribute("statementCount", count);

		request.setAttribute("title","Uploaded files and converted to RDF");

		request.setAttribute("fileItems",vreq.getFiles());

        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+vreq.getAppBean().getThemeDir()+"css/edit.css\"/>");

        try {
			JSPPageHandler.renderBasicPage(request, resp, "/jenaIngest/xmlFileUploadSuccess.jsp");
        } catch (Exception e) {
            System.out.println(this.getClass().getName()+" could not forward to view.");
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (!isAuthorizedToDisplayPage(request, response,
				SimplePermission.USE_ADVANCED_DATA_TOOLS_PAGES.ACTION)) {
        	return;
        }

		VitroRequest vreq = new VitroRequest(request);

		//make a form for uploading a file
		request.setAttribute("title","Upload file and convert to RDF");

		request.setAttribute("modelNames", getModelMaker(vreq).listModels().toList());
		request.setAttribute("models", null);

        request.setAttribute("css", "<link rel=\"stylesheet\" type=\"text/css\" href=\""+vreq.getAppBean().getThemeDir()+"css/edit.css\"/>");

        try {
			JSPPageHandler.renderBasicPage(request, response, "/jenaIngest/xmlFileUpload.jsp");
        } catch (Exception e) {
            System.out.println(this.getClass().getName()+" could not forward to view.");
            System.out.println(e.getMessage());
			e.printStackTrace(System.out);
        }

	}

	private List<Model> loadRdfXml(List<File> rdfxmlToLoad) throws ServletException {
		List<Model> models = new ArrayList<Model>(rdfxmlToLoad.size());
		for( File file: rdfxmlToLoad){
			Model tempModel = ModelFactory.createDefaultModel();
			try {
				tempModel.read(new FileInputStream(file), null);
				models.add(tempModel);
			} catch (FileNotFoundException e) {
				throw new ServletException("Could not find file " + file.getAbsolutePath() + " when loading RDF/XML.");
			}
		}
		return models;
	}

	private List<File> convertFiles(List<File> filesToLoad) throws ServletException  {
		List<File> rdfxmlFiles = new ArrayList<File>(filesToLoad.size());
		for( File file: filesToLoad){
			try {
				//look for an example of this in S9APIExamples.java from saxon he 9
				XsltTransformer t = xsltExec.load();
				//this is how to set parameters:
				//t.setParameter(new QName("someparametername"), new XdmAtomicValue(10));
				Serializer out = new Processor(false).newSerializer();
				out.setOutputProperty(Serializer.Property.METHOD, "xml");
				out.setOutputProperty(Serializer.Property.INDENT, "yes");
				File outFile = new File(file.getAbsolutePath() + ".rdfxml");
				rdfxmlFiles.add(outFile);
				out.setOutputFile(outFile);
				t.setSource(new StreamSource(file));
				t.setDestination(out);
				t.transform();
			} catch (SaxonApiException e) {
				log.error("could not convert " + file.getAbsolutePath() + " to RDF/XML: " + e.getMessage());
				throw new ServletException("could not convert " + file.getAbsolutePath() + " to RDF/XML: " + e.getMessage());
			}
		}
		return rdfxmlFiles;
	}

	/**
	 * Save files to baseDirectoryForFiles and return a list of File objects.
	 * @param fileStreams File streams to process
	 * @throws ServletException
	 */
	private List<File> saveFiles( Map<String, List<FileItem>> fileStreams ) throws ServletException{
	    // save files to disk
        List<File> filesToLoad = new ArrayList<File>();
        for(String fileItemKey : fileStreams.keySet()){
        	for( FileItem fileItem : fileStreams.get(fileItemKey)){
        		String originalName = fileItem.getName();
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
        		try {
        			fileItem.write(uploadedFile);
        		} catch (Exception ex) {
        			log.error("Unable to save POSTed file. " + ex.getMessage());
        			throw new ServletException("Unable to save file to the disk. "
        					+ ex.getMessage());
        		}

        		if( fileItem.getSize() < 1){
        			throw new ServletException("No file was uploaded or file was empty.");
        		}else{
        			filesToLoad.add( uploadedFile );
        		}
        	}
        }
        return filesToLoad;
	}

	private long countOfStatements( List<Model> models){

		long count =0;
		for( Model m : models){
			StmtIterator it = m.listStatements();
			while( it.hasNext()){
				it.next();
				count++;
			}
			it.close();
		}
		return count;

	}

}
