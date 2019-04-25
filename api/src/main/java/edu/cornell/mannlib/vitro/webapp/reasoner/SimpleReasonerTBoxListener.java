  /* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.reasoner;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.rdf.listeners.StatementListener;
import org.apache.jena.rdf.model.Statement;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;
import edu.cornell.mannlib.vitro.webapp.utils.threads.VitroBackgroundThread;

/**
 * Route notification of changes to TBox to the incremental ABox reasoner.
 * The incremental ABox reasoner handles only subClass and
 * equivalentClass class axioms. Reasoning dones as a result of TBox
 * changes is always done in a separate thread.
 */

public class SimpleReasonerTBoxListener extends StatementListener {

	private static final Log log = LogFactory.getLog(SimpleReasonerTBoxListener.class);

    private SimpleReasoner simpleReasoner;
    private Thread workerThread;
    private boolean stopRequested;
    private String name;

	private volatile boolean processingUpdates = false;
	private ConcurrentLinkedQueue<ModelUpdate> modelUpdates = null;

	public SimpleReasonerTBoxListener(SimpleReasoner simpleReasoner) {
		this.simpleReasoner = simpleReasoner;
		this.stopRequested = false;
		this.modelUpdates = new ConcurrentLinkedQueue<ModelUpdate>();
		this.processingUpdates = false;
	}

	public SimpleReasonerTBoxListener(SimpleReasoner simpleReasoner, String name) {
		this.simpleReasoner = simpleReasoner;
	    this.name = name;
		this.stopRequested = false;
		this.modelUpdates = new ConcurrentLinkedQueue<ModelUpdate>();
		this.processingUpdates = false;
	}

	@Override
	public void addedStatement(Statement statement) {

		ModelUpdate mu = new ModelUpdate(statement, ModelUpdate.Operation.ADD, ModelNames.TBOX_ASSERTIONS);
		processUpdate(mu);
	}

	@Override
	public void removedStatement(Statement statement) {

		ModelUpdate mu = new ModelUpdate(statement, ModelUpdate.Operation.RETRACT, ModelNames.TBOX_ASSERTIONS);
		processUpdate(mu);
	}

	private synchronized void processUpdate(ModelUpdate mu) {
		if (!processingUpdates && (modelUpdates.peek() != null)) {
			log.warn("TBoxProcessor thread was not running and work queue is not empty. size = " + modelUpdates.size() + " The work will be processed now.");
		}

		modelUpdates.add(mu);

		if (!processingUpdates) {
			processingUpdates = true;
			workerThread = new TBoxUpdateProcessor("TBoxUpdateProcessor (" + getName() + ")");
			workerThread.start();
		}
	}

   private synchronized ModelUpdate nextUpdate() {
	    ModelUpdate mu = modelUpdates.poll();
	    processingUpdates = (mu != null);
	    return mu;
   }

   public String getName() {
		return (name == null) ? "SimpleReasonerTBoxListener" : name;
   }

   public void setStopRequested() {
	    this.stopRequested = true;
   }

   private class TBoxUpdateProcessor extends VitroBackgroundThread {
        public TBoxUpdateProcessor(String name) {
        	super(name);
        }

        @Override
        public void run() {
            try {
	        	 log.debug("starting thread");
	        	 ModelUpdate mu = nextUpdate();
	        	 while (mu != null && !stopRequested) {
	    		    if (mu.getOperation() == ModelUpdate.Operation.ADD) {
	    				simpleReasoner.addedTBoxStatement(mu.getStatement());
	    		    } else if (mu.getOperation() == ModelUpdate.Operation.RETRACT) {
	    			    simpleReasoner.removedTBoxStatement(mu.getStatement());
	    		    } else {
	    			    log.error("unexpected operation value in ModelUpdate object: " + mu.getOperation());
	    		    }
	    		    mu = nextUpdate();
	        	 }
            }  finally {
        	     processingUpdates = false;
        	     log.debug("ending thread");
            }
        }
    }
}
