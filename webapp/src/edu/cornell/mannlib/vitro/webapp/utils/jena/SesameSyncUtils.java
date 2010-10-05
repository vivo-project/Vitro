/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import com.hp.hpl.jena.rdf.model.Model;

public class SesameSyncUtils {

	private static final Log log = LogFactory.getLog(SesameSyncUtils.class);
	
	public void writeModelToSesameContext
	  (Model jenaModel, String serverURI, String repositoryId, String contextId) 
	  throws RepositoryException, IOException, RDFParseException {
	    Repository myRepository = new HTTPRepository(serverURI, repositoryId);
	    myRepository.initialize();
	    RepositoryConnection myConn = myRepository.getConnection();    
	    
	    myConn.setAutoCommit(false);
	    try {
	    
		    Resource contextRes = (contextId != null) 
		    	? new URIImpl(contextId) : null ;
		    		
		    if (contextRes != null) {
		    	myConn.clear(contextRes);
		    } else {
		    	myConn.clear();
		    }
		    
		    PipedInputStream in = new PipedInputStream();
		    PipedOutputStream out = new PipedOutputStream(in);
		    try {
			    new Thread((new JenaOutputter(jenaModel, out))).start();
			    if (contextRes != null) {
			    	myConn.add(in,"http://example.org/base/", RDFFormat.NTRIPLES, contextRes);
			    } else {
			    	myConn.add(in,"http://example.org/base/", RDFFormat.NTRIPLES);
			    }
		    } finally {
		    	in.close();
		    }
		    
		    myConn.commit();
		    
	    } catch (Throwable e) {
	    	myConn.rollback();
            e.printStackTrace();
            log.error("Error writing to Sesame repository", e);
            throw new RuntimeException("Error writing to Sesame repository", e);
	    } finally {
	    	myConn.close();
	    } 
  
	}
	
	private class JenaOutputter implements Runnable {
		
		private Model model;
		private OutputStream out;
		
		public JenaOutputter(Model model, OutputStream out) {
			this.model = model;
			this.out = out;
		}
		
		public void run() {		
			try {
				model.write(out, "N-TRIPLE");
			} finally {
				try {
					out.flush();
					out.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		
	}
	
}
