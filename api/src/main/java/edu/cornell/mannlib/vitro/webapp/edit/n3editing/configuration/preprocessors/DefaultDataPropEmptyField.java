/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditSubmissionUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller.ProcessRdfFormController.Utilities;

/**
 * Editors have gotten into the habit of clearing the text from the
 * textarea and saving it to invoke a delete.  see Issue VITRO-432   
 *
 */
public class DefaultDataPropEmptyField implements ModelChangePreprocessor{

    @Override
    public void preprocess(Model retractionsModel, Model additionsModel,
            HttpServletRequest request) {
        
        EditConfigurationVTwo configuration = EditConfigurationUtils.getEditConfiguration(request);
        
        HttpSession session = request.getSession();
        MultiValueEditSubmission submission = EditSubmissionUtils.getEditSubmissionFromSession(session,configuration);
        
        //if data property, then check for empty string condition
        //which means only one value and it is an empty string
        if( checkForEmptyString(submission, configuration, new VitroRequest(request)) ) {
            additionsModel.removeAll();
        }        
    }
    

    protected boolean checkForEmptyString(
            MultiValueEditSubmission submission,
            EditConfigurationVTwo configuration, 
            VitroRequest vreq) {
        
        if(EditConfigurationUtils.isDataProperty(configuration.getPredicateUri(), vreq)) {
            // Our editors have gotten into the habit of clearing the text from the
            // textarea and saving it to invoke a delete.  see Issue VITRO-432   
            if (configuration.getFields().size() == 1) {
                String onlyField = configuration.getFields().keySet().iterator().next();
                List<Literal> value = submission.getLiteralsFromForm().get(onlyField);
                if( value == null || value.size() == 0){
                    return true;
                }else {
                    if(value.size() == 1) {
                        if( "".equals(value.get(0).getLexicalForm())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;    
    }

}
