/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroModelSource;
import edu.cornell.mannlib.vitro.webapp.servlet.setup.JenaDataSourceSetupBase;


public class IdModelSelector implements ModelSelector{

    private final String name;
    
    public IdModelSelector(String name){
        if( name == null )
            throw new IllegalArgumentException("Name of model must not be null.");
        
        this.name = name;
    }
    
    @Override
    public Model getModel(HttpServletRequest request, ServletContext context) {
        VitroModelSource mSource = JenaDataSourceSetupBase.getVitroModelSource(context);
        Model m = mSource.getModel( name );
        return m;
    }

}
