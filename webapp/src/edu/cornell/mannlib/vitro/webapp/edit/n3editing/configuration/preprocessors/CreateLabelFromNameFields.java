/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;

public class CreateLabelFromNameFields extends BaseEditSubmissionPreprocessor {

    private static final Log log = LogFactory.getLog(CreateLabelFromNameFields.class.getName());
    
    public CreateLabelFromNameFields(EditConfiguration editConfig) {
        super(editConfig);
    }

    // Create label by concatenating first name, middle name, and last name fields as
    // "<last name>, <first name> <middle name>". First name and last name are required;
    // middle name is optional. 
    // rjy7 Using all hard-coded field names for now. If we want to control these, pass in
    // a map of field names when creating the preprocessor object.
    public void preprocess(EditSubmission editSubmission) {
        Map<String, Literal> literalsFromForm = editSubmission.getLiteralsFromForm();
        try {
            // Create the label string
            
            // Assuming last name and first name fields will be on the form
            String lastName = literalsFromForm.get("lastName").getLexicalForm();
            String firstName = literalsFromForm.get("firstName").getLexicalForm();
           
            // The form may or may not have a middle name field
            String middleName = "";
            Literal middleNameLiteral = literalsFromForm.get("middleName");
            if (middleNameLiteral != null) {
                middleName = middleNameLiteral.getLexicalForm();
            }
            
            String label = lastName + ", " + firstName;
            if (!StringUtils.isEmpty(middleName)) {
                label += " " + middleName;
            }

            // Add the label to the form literals
            Field labelField = editConfiguration.getField("label");
            String rangeDatatypeUri = labelField.getRangeDatatypeUri();
            if (StringUtils.isEmpty(rangeDatatypeUri)) {
                rangeDatatypeUri = XSD.xstring.toString();
            }
            String rangeLang = labelField.getRangeLang();
            // RY Had to change createLiteral method to protected - check w/Brian
            Literal labelLiteral = editSubmission.createLiteral(label, rangeDatatypeUri, rangeLang);
            literalsFromForm.put("label", labelLiteral);
            editSubmission.setLiteralsFromForm(literalsFromForm);

        } catch (Exception e) {
            log.error("Error retrieving name values from edit submission.");
        }
        
    }
}
