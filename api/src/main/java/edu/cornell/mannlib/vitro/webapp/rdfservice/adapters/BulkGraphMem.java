package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;

public class BulkGraphMem extends GraphMem {
	
	public void addWithoutNotify(Triple t) {
		checkOpen();
		performAdd(t);
	}

	public void deleteWithoutNotify(Triple t) {
		checkOpen();
		performDelete(t);
	}
}
