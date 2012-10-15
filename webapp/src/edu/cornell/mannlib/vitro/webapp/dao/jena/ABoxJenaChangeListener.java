/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.rdf.model.ModelChangedListener;

import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;

public class ABoxJenaChangeListener extends JenaChangeListener {

    public ABoxJenaChangeListener(ModelChangedListener listener) {
        super(listener);
        ignoredGraphs.add(JenaDataSourceSetupBase.JENA_INF_MODEL);
        ignoredGraphs.add(JenaDataSourceSetupBase.JENA_TBOX_ASSERTIONS_MODEL);
        ignoredGraphs.add(JenaDataSourceSetupBase.JENA_TBOX_INF_MODEL);
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
                        JenaDataSourceSetupBase.JENA_DB_MODEL.equals(graphURI) 
                                || (!ignoredGraphs.contains(graphURI) 
                                        && !graphURI.contains("filegraph") 
                                                && !graphURI.contains("tbox")));
    }
    
}
