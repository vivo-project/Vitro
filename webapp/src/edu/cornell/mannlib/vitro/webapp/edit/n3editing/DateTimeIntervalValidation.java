/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;

public class DateTimeIntervalValidation implements N3Validator {
    private static Log log = LogFactory.getLog(DateTimeIntervalValidation.class);
    
    private String startFieldName;
    private String endFieldName;

    private String startValueName;
    private String endValueName;
    
    public DateTimeIntervalValidation(String startFieldName, String endFieldName){
        this.startFieldName = startFieldName;
        this.endFieldName = endFieldName;
        startValueName = startFieldName + ".value";
        endValueName = endFieldName + ".value";
    }
    
    public Map<String, String> validate(EditConfiguration editConfig,
            EditSubmission editSub) {
        Map<String, Literal> existingLiterals = editConfig.getLiteralsInScope();
        Literal existingStartYear = existingLiterals.get(startValueName);
        Literal existingEndYear = existingLiterals.get(endValueName);

        Map<String, Literal> literalsFromForm = editSub.getLiteralsFromForm();
        Literal formStartYear = literalsFromForm.get(startValueName);
        Literal formEndYear = literalsFromForm.get(endValueName);

        Map<String, String> errors = new HashMap<String, String>();

        if (formStartYear != null && formEndYear != null) {
            errors.putAll(checkDateLiterals(formStartYear, formEndYear));
        } else if (formStartYear != null && existingEndYear != null) {
            errors.putAll(checkDateLiterals(formStartYear, existingEndYear));
        } else if (existingStartYear != null && formEndYear != null) {
            errors.putAll(checkDateLiterals(existingStartYear, formEndYear));
        } else if (existingStartYear != null && existingEndYear != null) {
            errors.putAll(checkDateLiterals(existingStartYear, existingEndYear));
        }

        if (errors.size() != 0)
            return errors;
        else
            return null;
    }

    private Map<String, String> checkDateLiterals(Literal startLit, Literal endLit) {
        Map<String, String> errors = new HashMap<String, String>();        
        try{
             XSDDateTime startDate = (XSDDateTime)startLit.getValue();
             XSDDateTime endDate = (XSDDateTime)endLit.getValue();
             if( startDate != null && endDate!= null ){
                 Calendar startCal = startDate.asCalendar();
                 Calendar endCal = endDate.asCalendar();
                                  
                 if( endCal != null && ! endCal.after( startCal ) ){
                     errors.put(startFieldName, "Start year must be before end year");
                     errors.put(endFieldName, "End year must be after start year");
                 }
             }
        }catch(ClassCastException cce){
            errors.put(startFieldName, "could not format star or end date");
            errors.put(endFieldName, "could not format star or end date");
            log.debug("could not format dates " + cce);
        }
            
        return errors;
    }

}
