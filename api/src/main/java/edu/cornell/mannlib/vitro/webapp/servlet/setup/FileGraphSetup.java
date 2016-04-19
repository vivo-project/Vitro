/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.WhichService.CONTENT;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.dao.jena.RDFServiceDataset;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService.ModelSerializationFormat;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;

// This ContextListener must run after the JenaDataSourceSetup ContextListener

public class FileGraphSetup implements ServletContextListener {
	private static final Log log = LogFactory.getLog(FileGraphSetup.class);

	private static final String RDF = "rdf";
    private static final String ABOX = "abox";
    private static final String TBOX = "tbox";
    private static final String FILEGRAPH = "filegraph";

    public static final String FILEGRAPH_URI_ROOT = "http://vitro.mannlib.cornell.edu/filegraph/";
    
    /** Ignore hidden files when looking for filegraph RDF. */
	private static final DirectoryStream.Filter<Path> REJECT_HIDDEN_FILES = new DirectoryStream.Filter<Path>() {
		@Override
		public boolean accept(Path entry) throws IOException {
			return !Files.isHidden(entry);
		}
	};    

    @Override
	public void contextInitialized(ServletContextEvent sce) {

        boolean aboxChanged = false; // indicates whether any ABox file graph model has changed
        boolean tboxChanged = false; // indicates whether any TBox file graph model has changed

        ServletContext ctx = sce.getServletContext();
        
        try {
            OntDocumentManager.getInstance().setProcessImports(true);
            Dataset dataset = ModelAccess.on(ctx).getDataset(); 
			RDFService rdfService = ModelAccess.on(ctx).getRDFService(CONTENT);

            // ABox files
            Set<Path> paths = getFilegraphPaths(ctx, RDF, ABOX, FILEGRAPH);

            cleanupDB(dataset, pathsToURIs(paths, ABOX), ABOX);

            // Just update the ABox filegraphs in the DB; don't attach them to a base model.
            aboxChanged = readGraphs(paths, rdfService, ABOX, /* aboxBaseModel */ null);		

            // TBox files
            paths = getFilegraphPaths(ctx, RDF, TBOX, FILEGRAPH);

            cleanupDB(dataset, pathsToURIs(paths, TBOX),TBOX);

            OntModel tboxBaseModel = ModelAccess.on(ctx).getOntModel(ModelNames.TBOX_ASSERTIONS);
            tboxChanged = readGraphs(paths, rdfService, TBOX, tboxBaseModel);
        } catch (ClassCastException cce) {
            String errMsg = "Unable to cast servlet context attribute to the appropriate type " + cce.getLocalizedMessage();
            log.error(errMsg);
            throw new ClassCastException(errMsg);
        } catch (Throwable t) {
            log.error(t, t);
        } finally {
            OntDocumentManager.getInstance().setProcessImports(false);
        }

        if ( (aboxChanged || tboxChanged) && !isUpdateRequired(ctx)) {
            log.info("a full recompute of the Abox will be performed because" +
                    " the filegraph abox(s) and/or tbox(s) have changed or are being read for the first time." );
            SimpleReasonerSetup.setRecomputeRequired(ctx, SimpleReasonerSetup.RecomputeMode.BACKGROUND);
        }
    }

	private Set<Path> getFilegraphPaths(ServletContext ctx, String... strings) {
		StartupStatus ss = StartupStatus.getBean(ctx);

		String homeDirProperty = ApplicationUtils.instance().getHomeDirectory().getPath().toString();
		Path filegraphDir = Paths.get(homeDirProperty, strings);

		Set<Path> paths = new TreeSet<>();
		if (Files.isDirectory(filegraphDir)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(
					filegraphDir, REJECT_HIDDEN_FILES)) {
				for (Path p : stream) {
					paths.add(p);
				}
				ss.info(this, "Read " + paths.size() + " RDF files from '"
						+ filegraphDir + "'");
			} catch (IOException e) {
				ss.fatal(this, "Failed to read filegraph RDF from '"
						+ filegraphDir + "' directory.", e);
			}
		} else {
			ss.info(this, "Filegraph directory '" + filegraphDir
					+ "' doesn't exist.");
		}
		log.debug("Paths from '" + filegraphDir + "': " + paths);
		return paths;
	}

	/*
     * Reads the graphs stored as files in sub-directories of 
     *   1. updates the SDB store to reflect the current contents of the graph.
     *   2. adds the graph as an in-memory submodel of the base in-memory graph 
     *      
     * Note: no connection needs to be maintained between the in-memory copy of the
     * graph and the DB copy.
     */
    private boolean readGraphs(Set<Path> pathSet, RDFService rdfService, String type, OntModel baseModel) {

        int count = 0;

        boolean modelChanged = false;

        // For each file graph in the target directory update or add that graph to
        // the Jena SDB, and attach the graph as a submodel of the base model
        for ( Path p : pathSet ) {

            count++; // note this will count the empty files too
            try {
                FileInputStream fis = new FileInputStream( p.toFile() );
                try {
                    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
                    String fn = p.getFileName().toString().toLowerCase();
                    if ( fn.endsWith(".nt") ) {
                        model.read( fis, null, "N-TRIPLE" );
                    } else if ( fn.endsWith(".n3") || fn.endsWith(".ttl") ) {
                        model.read( fis, null, "N3" );
                    } else if ( fn.endsWith(".owl") || fn.endsWith(".rdf") || fn.endsWith(".xml") ) {
                        model.read( fis, null, "RDF/XML" );
                    } else if ( fn.endsWith(".md") ) {
                    	// Ignore markdown files - documentation.
                    } else {
                        log.warn("Ignoring " + type + " file graph " + p + " because the file extension is unrecognized.");
                    }

                    if ( !model.isEmpty() && baseModel != null ) {
                        baseModel.addSubModel(model);
                        log.debug("Attached file graph as " + type + " submodel " + p.getFileName());
                    } 

                    modelChanged = modelChanged | updateGraphInDB(rdfService, model, type, p);

                } catch (Exception ioe) {
                    log.error("Unable to process file graph " + p, ioe);
                    System.out.println("Unable to process file graph " + p);
                    ioe.printStackTrace();
                } finally {
                    fis.close();
                }
            } catch (FileNotFoundException fnfe) {
                log.warn(p + " not found. Unable to process file graph" + 
                        ((fnfe.getLocalizedMessage() != null) ? 
                                fnfe.getLocalizedMessage() : "") );
            } catch (IOException ioe) {
                // this is for the fis.close() above.
                log.warn("Exception while trying to close file graph file: " + p,ioe);
            }
        } // end - for
        
		log.info("Read " + count + " " + type + " file graph" + ((count == 1) ? "" : "s"));

        return modelChanged;
    }

    /*
     * If a graph with the given name doesn't exist in the DB then add it.
     *
     * Otherwise, if a graph with the given name is in the DB and is not isomorphic with
     * the graph that was read from the file system then replace the graph
     * in the DB with the one read from the file system.
     * 
     * Otherwise, if a graph with the given name is in the DB and is isomorphic with
     * the graph that was read from the files system, then do nothing. 
     */
    public boolean updateGraphInDB(RDFService rdfService, Model fileModel, String type, Path path) throws RDFServiceException {
        String graphURI = pathToURI(path,type);
        
        if (rdfService.isEquivalentGraph(graphURI, fileModel)) {
            return false;
        }

		Model dbModel = new RDFServiceDataset(rdfService).getNamedModel(graphURI);
		if (log.isDebugEnabled()) {
			log.debug(String.format(
					"%s %s dbModel size is %d, fileModel size is %d", type,
					path.getFileName(), dbModel.size(), fileModel.size()));
		}

		log.info("Updating " + path + " because graphs are not isomorphic");
		log.info("dbModel: " + dbModel.size() + " ; fileModel: " + fileModel.size());
		dbModel.removeAll();
		dbModel.add(fileModel);
		return true;
    }

    /*
     * Deletes any file graphs that are  no longer present in the file system
     * from the DB. 
     * 
     * @param uriSet (input)   - a set of graph URIs representing the file
     *                           graphs (of the given type) in the file
     *                           system.
     * @param type (input)     - abox or tbox.
     * @param kbStore (output) - the SDB store for the application                        
     */
    public void cleanupDB(Dataset dataset, Set<String> uriSet, String type) {

        Pattern graphURIPat = Pattern.compile("^" + FILEGRAPH_URI_ROOT + type);   

        Iterator<String> iter = dataset.listNames();	

        while (iter.hasNext()) {
            String graphURI = iter.next();
            Matcher matcher = graphURIPat.matcher(graphURI);

            if (matcher.find()) {
                if (!uriSet.contains(graphURI)) {
                    Model model = dataset.getNamedModel(graphURI);
                    model.removeAll(); // delete the graph from the DB
                    log.info("Removed " + type + " file graph " + graphURI + " from the DB store because the file no longer exists in the file system");
                }
            }            
        }

        return;
    }

	/*
	 * Takes a set of paths for file graphs and returns a set containing a graph
	 * uri for each path.
	 */
	private Set<String> pathsToURIs(Set<Path> paths, String type) {
		HashSet<String> uriSet = new HashSet<String>();
		for (Path path : paths) {
			uriSet.add(pathToURI(path, type));
		}
		log.debug("uriSet = " + uriSet);
		return uriSet;
	}
    
    /*
     * Takes a path for a file graph and returns the corresponding SDB URI
     * for the graph. The correspondence is by defined convention.
     */
    private String pathToURI(Path path, String type) {
    		return FILEGRAPH_URI_ROOT + type + "/" + path.getFileName(); 
    }
    
    @Override
	public void contextDestroyed( ServletContextEvent sce ) {
        // nothing to do
    }

    private static boolean isUpdateRequired(ServletContext ctx) {
        return (ctx.getAttribute(UpdateKnowledgeBase.KBM_REQURIED_AT_STARTUP) != null);
    }    
    
}
