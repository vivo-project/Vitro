/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.validators;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;

public class StartYearBeforeEndYear implements N3Validator {
	private String startFieldName;
	private String endFieldName;
	
	public StartYearBeforeEndYear(String startFieldName, String endFieldName){
		this.startFieldName = startFieldName;
		this.endFieldName = endFieldName;
	}
	public Map<String, String> validate(EditConfiguration editConfig,
			EditSubmission editSub) {
		Map<String, Literal> existingLiterals = editConfig.getLiteralsInScope();
		Literal existingStartYear = existingLiterals.get(startFieldName);
		Literal existingEndYear = existingLiterals.get(endFieldName);

		Map<String, Literal> literalsFromForm = editSub.getLiteralsFromForm();
		Literal formStartYear = literalsFromForm.get(startFieldName);
		Literal formEndYear = literalsFromForm.get(endFieldName);

		Map<String, String> errors = new HashMap<String, String>();

		if (formStartYear != null && formEndYear != null) {
			errors.putAll(checkDateLiterals(formStartYear, formEndYear));
		} else if (formStartYear != null && existingEndYear != null) {
			errors.putAll(checkDateLiterals(formStartYear, existingEndYear));
		} else if (existingStartYear != null && formEndYear != null) {
			errors.putAll(checkDateLiterals(existingStartYear, formEndYear));
		} else if (existingStartYear != null && existingEndYear != null) {
			errors
					.putAll(checkDateLiterals(existingStartYear,
							existingEndYear));
		}

		if (errors.size() != 0)
			return errors;
		else
			return null;
	}

	private Map<String, String> checkDateLiterals(Literal startLit,
			Literal endLit) {
		Map<String, String> errors = new HashMap<String, String>();
		try {
			int start = Integer.parseInt(startLit.getLexicalForm());
			int end = Integer.parseInt(endLit.getLexicalForm());
			if (end < start) {
				errors.put(startFieldName, "Start year must be before end year");
				errors.put(endFieldName, "End year must be after start year");
			}
		} catch (NumberFormatException nfe) {
		    errors.put(startFieldName, "could not format star or end date");
		    errors.put(endFieldName, "could not format star or end date");
		}
		return errors;
	}

}
