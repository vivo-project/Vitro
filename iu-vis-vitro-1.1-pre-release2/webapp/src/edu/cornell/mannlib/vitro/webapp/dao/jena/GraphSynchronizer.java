/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEvents;
import com.hp.hpl.jena.graph.GraphListener;
import com.hp.hpl.jena.graph.Triple;

import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;

public class GraphSynchronizer implements GraphListener {

	private static final Log log = LogFactory.getLog(GraphSynchronizer.class.getName());
	
	private Graph g;
	
	public GraphSynchronizer (Graph synchronizee) {
		g = synchronizee;
	}
	
	public void notifyAddArray(Graph arg0, Triple[] arg1) {
		g.getBulkUpdateHandler().add(arg1);
	}

	
	public void notifyAddGraph(Graph arg0, Graph arg1) {
		g.getBulkUpdateHandler().add(arg1);

	}

	
	public void notifyAddIterator(Graph arg0, Iterator arg1) {
		g.getBulkUpdateHandler().add(arg1);
	}

	
	public void notifyAddList(Graph arg0, List arg1) {
		g.getBulkUpdateHandler().add(arg1);
	}

	public void notifyAddTriple(Graph arg0, Triple arg1) {
		g.add(arg1);
	}

	
	public void notifyDeleteArray(Graph arg0, Triple[] arg1) {
		g.getBulkUpdateHandler().delete(arg1);
	}

	
	public void notifyDeleteGraph(Graph arg0, Graph arg1) {
		g.getBulkUpdateHandler().delete(arg1);
	}

	public void notifyDeleteIterator(Graph arg0, Iterator arg1) {
		g.getBulkUpdateHandler().delete(arg1);
	}

	public void notifyDeleteList(Graph arg0, List arg1) {
		g.getBulkUpdateHandler().delete(arg1);
	}
	
	public void notifyDeleteTriple(Graph arg0, Triple arg1) {
		g.delete(arg1);
		log.trace("Delete triple");
		if (arg1.getObject().isLiteral()) {
			log.trace(arg1.getObject().getLiteralLexicalForm());
		} else if (arg1.getObject().isVariable()) {
			log.trace("Triple object is variable");
		} else if (arg1.getObject().isURI()) {
			log.trace(arg1.getObject().getURI());
		} else if (arg1.getObject().isBlank()) {
			log.trace("Triple object is blank");
		} else if (arg1.getObject().isConcrete()) {
			log.trace("Triple object is concrete");
		} 
		log.trace(arg1.getObject().toString());
	}
	
	public void notifyEvent(Graph arg0, Object arg1) {
		// ontModel.removeAll(s,p,o) doesn't trigger a notifyDeleteTriple() !?!
		// So, I'm doing this silly thing to make sure we see the deletion.
		// What else is lurking around the corner?
		if (arg1 instanceof GraphEvents) {
			GraphEvents ge = ((GraphEvents) arg1);
			Object content = ge.getContent();
			if (ge.getTitle().equals("remove") && content instanceof Triple) {
				notifyDeleteTriple( arg0, (Triple) content );
			}
		}
	}

}
