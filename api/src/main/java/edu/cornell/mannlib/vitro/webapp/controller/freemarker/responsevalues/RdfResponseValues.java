/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.web.ContentType;

public class RdfResponseValues extends BaseResponseValues {
    private final Model model;
    
    public RdfResponseValues(ContentType contentType, Model model) {
        super(contentType);
        this.model = model;
    }

    public RdfResponseValues(ContentType contentType, Model model, int statusCode) {
        super(contentType, statusCode);
        this.model = model;
    }
    
    @Override
    public Model getModel() {
       return model;
    }
}
