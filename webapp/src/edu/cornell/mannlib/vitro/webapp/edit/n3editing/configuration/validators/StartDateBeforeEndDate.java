/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;

public class StartDateBeforeEndDate implements N3Validator {
	private String startFieldName;
	private String endFieldName;
	
	public StartDateBeforeEndDate(String startFieldName, String endFieldName){
		this.startFieldName = startFieldName;
		this.endFieldName = endFieldName;
	}
	public Map<String, String> validate(EditConfiguration editConfig,
			EditSubmission editSub) {
		Map<String, Literal> existingLiterals = editConfig.getLiteralsInScope();
		Literal existingStartDate = existingLiterals.get(startFieldName);
		Literal existingEndDate = existingLiterals.get(endFieldName);

		Map<String, Literal> literalsFromForm = editSub.getLiteralsFromForm();
		Literal formStartDate = literalsFromForm.get(startFieldName);
		Literal formEndDate = literalsFromForm.get(endFieldName);

		Map<String, String> errors = new HashMap<String, String>();

		if (formStartDate != null && formEndDate != null) {
			errors.putAll(checkDateLiterals(formStartDate, formEndDate));
		} else if (formStartDate != null && existingEndDate != null) {
			errors.putAll(checkDateLiterals(formStartDate, existingEndDate));
		} else if (existingStartDate != null && formEndDate != null) {
			errors.putAll(checkDateLiterals(existingStartDate, formEndDate));
		} else if (existingStartDate != null && existingEndDate != null) {
			errors
					.putAll(checkDateLiterals(existingStartDate,
					        existingEndDate));
		}

		if (errors.size() != 0)
			return errors;
		else
			return null;
	}

	private Map<String, String> checkDateLiterals(Literal startLit,
			Literal endLit) {
		Map<String, String> errors = new HashMap<String, String>();
        Calendar startDate = getDateFromLiteral(startLit);
        Calendar endDate = getDateFromLiteral(endLit);
		try {
		    if (startDate.compareTo(endDate) > 0) {
                errors.put(startFieldName, "Start date cannot follow end date");
                errors.put(endFieldName, "End date cannot precede start date");		        
		    }
		} catch (NullPointerException npe){ 
		    log.error("Cannot compare date to null.");
		    
		} catch (IllegalArgumentException iae) {
		    log.error("IllegalArgumentException");
		}
		return errors;
	}
	
	private Calendar getDateFromLiteral(Literal dateLit) {
	    
	    String[] date = dateLit.getLexicalForm().split("-");
	    int year = Integer.parseInt(date[0]);
	    int day = date.length < 3 ? 1 : Integer.parseInt(date[2]);
	    int month = date.length < 2 ? 0 : Integer.parseInt(date[1]);
	    Calendar c = Calendar.getInstance();
	    c.set(year, month, day);
	    return c;
	}

    private Log log = LogFactory.getLog(StartDateBeforeEndDate.class);
}
