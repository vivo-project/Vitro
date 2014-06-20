/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.rdf.model.ModelChangedListener;

import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelNames;

public class ABoxJenaChangeListener extends JenaChangeListener {

    public ABoxJenaChangeListener(ModelChangedListener listener) {
        super(listener);
        ignoredGraphs.add(ModelNames.ABOX_INFERENCES);
        ignoredGraphs.add(ModelNames.TBOX_ASSERTIONS);
        ignoredGraphs.add(ModelNames.TBOX_INFERENCES);
    }
    
    @Override
    public void addedStatement(String serializedTriple, String graphURI) {
        if (isABoxGraph(graphURI)) {
            super.addedStatement(serializedTriple, graphURI);
        }
    }

    @Override
    public void removedStatement(String serializedTriple, String graphURI) {
        if (isABoxGraph(graphURI)) {
            super.removedStatement(serializedTriple, graphURI);
        }
    }
    
    private boolean isABoxGraph(String graphURI) {
        return (graphURI == null || 
                        ModelNames.ABOX_ASSERTIONS.equals(graphURI) 
                                || (!ignoredGraphs.contains(graphURI) 
                                        && !graphURI.contains("filegraph") 
                                                && !graphURI.contains("tbox")));
    }
    
}
