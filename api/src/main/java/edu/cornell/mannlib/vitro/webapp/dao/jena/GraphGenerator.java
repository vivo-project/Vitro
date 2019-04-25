/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.jena.graph.Graph;

public interface GraphGenerator {

	public Graph generateGraph();

	public boolean isGraphClosed();

}
