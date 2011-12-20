/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.edit;

import java.util.Map;
import java.util.List;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;

public class MultiValueEditSubmissionTemplateModel {
    
    private final MultiValueEditSubmission editSub;

    public MultiValueEditSubmissionTemplateModel(MultiValueEditSubmission editSub){
        this.editSub = editSub;
    }
    
    public Map<String, List<String>> getLiteralsFromForm() {
    	if(editSub == null)
    		return null;
    	//Transforms from string to list of literals TO string to list of strings
        return EditConfigurationUtils.transformLiteralMap(editSub.getLiteralsFromForm());
    }

   
    public Map<String, String> getValidationErrors() {
    	if(editSub ==  null)
    		return null;
        return editSub.getValidationErrors();
    }

    public Map<String, List<String>> getUrisFromForm() {
    	if(editSub ==  null)
    		return null;
        return editSub.getUrisFromForm();
    }
    
    public boolean getSubmissionExists() {
    	return (this.editSub != null);
    }

}
