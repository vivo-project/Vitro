/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;

public class EditSubmissionTemplateModel {
    private EditSubmission editSub;
    
    public Map<String, Literal> getLiteralsFromForm() {
        return editSub.getLiteralsFromForm();
    }

    public Map<String, String> getValidationErrors() {
        return editSub.getValidationErrors();
    }

    public Map<String, String> getUrisFromForm() {
        return editSub.getUrisFromForm();
    }

    public EditSubmissionTemplateModel(EditSubmission editSub){
        this.editSub = editSub;
    }


    
    
}
