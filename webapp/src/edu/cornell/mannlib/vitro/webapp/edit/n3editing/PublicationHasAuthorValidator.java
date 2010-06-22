package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

public class PublicationHasAuthorValidator implements N3Validator {

    private static String MISSING_AUTHOR_ERROR = "Must specify a new or existing author.";
    private static String MISSING_FIRST_NAME_ERROR = "Must specify the author's first name.";
    private static String MISSING_LAST_NAME_ERROR = "Must specify the author's last name.";
    
    @Override
    public Map<String, String> validate(EditConfiguration editConfig,
            EditSubmission editSub) {
        Map<String,String> urisFromForm = editSub.getUrisFromForm();
        Map<String,Literal> literalsFromForm = editSub.getLiteralsFromForm();
        
        Literal firstName = literalsFromForm.get("firstName");
        if( firstName.getLexicalForm() != null && "".equals(firstName.getLexicalForm()) )
            firstName = null;

        Literal lastName = literalsFromForm.get("lastName");
        if( lastName.getLexicalForm() != null && "".equals(lastName.getLexicalForm()) )
            lastName = null;
        
        String personUri = urisFromForm.get("personUri");
        if ("".equals(personUri)) {
            personUri = null;
        }
        
        Map<String,String> errors = new HashMap<String,String>();   
        
        if (personUri == null && lastName == null && firstName == null) {
            errors.put("lastName", MISSING_AUTHOR_ERROR);
        } else if (lastName != null && firstName == null) {
            errors.put("firstName", MISSING_FIRST_NAME_ERROR);
        } else if (lastName == null && firstName != null) {
            errors.put("lastName", MISSING_LAST_NAME_ERROR);
        }
        
        return errors.size() != 0 ? errors : null;
    }

}
