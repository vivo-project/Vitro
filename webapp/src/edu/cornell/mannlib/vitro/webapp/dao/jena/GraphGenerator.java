/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.graph.Graph;

public interface GraphGenerator {

	public Graph generateGraph();
	
	public boolean isGraphClosed();
	
}
