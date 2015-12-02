/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Capabilities;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphEventManager;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.TransactionHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphWithPerform;
import com.hp.hpl.jena.graph.impl.SimpleBulkUpdateHandler;
import com.hp.hpl.jena.graph.impl.SimpleEventManager;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.DeleteDeniedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.RDFServiceImpl;
import edu.cornell.mannlib.vitro.webapp.utils.logging.ToString;

public class ListeningGraph implements GraphWithPerform {
    
    private static final Log log = LogFactory.getLog(ListeningGraph.class);
    
    private RDFServiceImpl rdfServiceImpl;
    private String graphURI;
    
    private BulkUpdateHandler bulkUpdateHandler;
    private GraphEventManager eventManager;
    private PrefixMapping prefixMapping = new PrefixMappingImpl();
    
    public ListeningGraph(String graphURI, RDFServiceImpl rdfServiceImpl) {
        this.graphURI = graphURI;
        this.rdfServiceImpl = rdfServiceImpl;
    }
    
    @Override
    public void add(Triple triple) throws AddDeniedException {
        performAdd(triple);
    }

    @Override
    public void performAdd(Triple triple) throws AddDeniedException {
        if (log.isDebugEnabled()) {
            log.debug("adding " + triple + " to " + graphURI);
        }
        this.rdfServiceImpl.notifyListeners(triple, ModelChange.Operation.ADD, graphURI);
    }

    @Override
    public void delete(Triple triple) throws DeleteDeniedException {
        performDelete(triple);
    }
    
    @Override 
    public void performDelete(Triple triple) throws DeleteDeniedException {
        if (log.isDebugEnabled()) {
            log.debug("deleting " + triple + " from " + graphURI);
        }
        this.rdfServiceImpl.notifyListeners(triple, ModelChange.Operation.REMOVE, graphURI);
    }    
      
    @Override
    public void close() {
    	// Nothing to close.
    }

    @Override
    public boolean contains(Triple arg0) {
       return contains(arg0.getSubject(), arg0.getPredicate(), arg0.getObject());
    }

    @Override
    public boolean contains(Node subject, Node predicate, Node object) {
        return false;
    }

    @Override
    public boolean dependsOn(Graph arg0) {
        return false; // who knows?
    }

    @Override
    public ExtendedIterator<Triple> find(TripleMatch arg0) {
        Triple t = arg0.asTriple();
        return find(t.getSubject(), t.getPredicate(), t.getObject());
    }
  
    @Override
    public ExtendedIterator<Triple> find(Node subject, Node predicate, Node object) {
        List<Triple> triplist = new ArrayList<Triple>();
        return WrappedIterator.create(triplist.iterator());
    }
    
	@Override
	public void clear() {
		for (Triple t: find(null, null, null).toList()) {
			delete(t);
		}
	}

	@Override
	public void remove(Node subject, Node predicate, Node object) {
		for (Triple t: find(subject, predicate, object).toList()) {
			delete(t);
		}
	}

    @Override
    @Deprecated
    public BulkUpdateHandler getBulkUpdateHandler() {
        if (this.bulkUpdateHandler == null) {
            this.bulkUpdateHandler = new SimpleBulkUpdateHandler(this);
        }
        return this.bulkUpdateHandler;
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public GraphEventManager getEventManager() {
        if (eventManager == null) {
            eventManager = new SimpleEventManager(this);
        }
        return eventManager;
    }

    @Override
    public PrefixMapping getPrefixMapping() {
        return prefixMapping;
    }

    @Override
    public GraphStatisticsHandler getStatisticsHandler() {
        return null;
    }

    @Override
    public TransactionHandler getTransactionHandler() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return !contains(null, null, null);
    }

    @Override
    public boolean isIsomorphicWith(Graph arg0) {
        throw new UnsupportedOperationException("isIsomorphicWith() not supported " +
                "by SPARQL graphs");
    }

    @Override
    public int size() {
        int size = find(null, null, null).toList().size();
        return size;
    }
    
    private final static Capabilities capabilities = new Capabilities() {
        
        @Override
		public boolean addAllowed() {
            return false;
        }
        
        @Override
        public boolean addAllowed(boolean everyTriple) {
            return false;
        }
        
        @Override
        public boolean canBeEmpty() {
            return true;
        }
        
        @Override
        public boolean deleteAllowed() {
            return false;
        }
        
        @Override
        public boolean deleteAllowed(boolean everyTriple) {
            return false;
        }
        
        @Override
        public boolean findContractSafe() {
            return true;
        }
        
        @Override
        public boolean handlesLiteralTyping() {
            return true;
        }
        
        @Override
        public boolean iteratorRemoveAllowed() {
            return false;
        }
        
        @Override
        public boolean sizeAccurate() {
            return true;
        }
    };

	@Override
	public String toString() {
		return "ListeningGraph["+ToString.hashHex(this)
				+ ", " + rdfServiceImpl
				+ ", " + ToString.modelName(graphURI) + "]";
	}

}
