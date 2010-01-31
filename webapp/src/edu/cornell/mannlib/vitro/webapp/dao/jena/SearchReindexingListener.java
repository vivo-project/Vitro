/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualCreationEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualDeletionEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualEditEvent;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.search.indexing.IndexBuilder;


public class SearchReindexingListener implements ModelChangedListener {
	
	private static final Log log = LogFactory.getLog(SearchReindexingListener.class.getName());	
	
	private OntModel ontModel;
	private ServletContext servletContext;
	
	protected DateFormat xsdDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private boolean dirty = false;
	private boolean indexing = false;

	public SearchReindexingListener(OntModel ontModel, ServletContext sc) {
		this.ontModel = ontModel;
		this.servletContext = sc;
	}	
	
	private class Reindexer implements Runnable {
		public void run() {
			while(dirty) {
				dirty = false;
				IndexBuilder builder = (IndexBuilder) servletContext.getAttribute(IndexBuilder.class.getName());
				indexing = true;
				try {
		        builder.run();
				} finally {
					indexing = false;
				}
			}
		}
	}
	
	public void addedStatement(Statement arg0) {

	}

	
	public void addedStatements(Statement[] arg0) {
		// TODO Auto-generated method stub

	}

	
	public void addedStatements(List arg0) {
		// TODO Auto-generated method stub

	}

	
	public void addedStatements(StmtIterator arg0) {
		// TODO Auto-generated method stub

	}

	
	public void addedStatements(Model arg0) {
		// TODO Auto-generated method stub

	}

	
	public void notifyEvent(Model arg0, Object arg1) {
		if ((arg1 instanceof IndividualCreationEvent) || (arg1 instanceof IndividualUpdateEvent)) {
			IndividualEditEvent ee = (IndividualEditEvent) arg1;
			if (!ee.getBegin()) {
				dirty=true;
				if (!indexing) {
					new Thread(new Reindexer()).start();
				}
			}
		} else if (arg1 instanceof IndividualDeletionEvent) {
			IndividualEditEvent ee = (IndividualEditEvent) arg1;
	        IndexBuilder builder = (IndexBuilder) servletContext.getAttribute(IndexBuilder.class.getName());
	        if (builder != null) {
	        	builder.entityDeleted(ee.getIndividualURI());
	        } else {
	        	log.warn("Unable to remove individual from search index: no attribute " + IndexBuilder.class.getName() + " in servlet context");
	        }
		}
	}

	
	public void removedStatement(Statement arg0) {
		// TODO Auto-generated method stub

	}

	
	public void removedStatements(Statement[] arg0) {
		// TODO Auto-generated method stub

	}

	
	public void removedStatements(List arg0) {
		// TODO Auto-generated method stub

	}

	
	public void removedStatements(StmtIterator arg0) {
		// TODO Auto-generated method stub

	}

	
	public void removedStatements(Model arg0) {
		// TODO Auto-generated method stub

	}

}
