/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit;

import java.util.Map;
import java.util.List;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;

public class MultiValueEditSubmissionTemplateModel {
    
    private final MultiValueEditSubmission editSub;

    public MultiValueEditSubmissionTemplateModel(MultiValueEditSubmission editSub){
        this.editSub = editSub;
    }
    
    public Map<String, List<Literal>> getLiteralsFromForm() {
        return editSub.getLiteralsFromForm();
    }

    /*
    public Map<String, List<String>> getValidationErrors() {
        return editSub.getValidationErrors();
    }*/

    public Map<String, List<String>> getUrisFromForm() {
        return editSub.getUrisFromForm();
    }

}
