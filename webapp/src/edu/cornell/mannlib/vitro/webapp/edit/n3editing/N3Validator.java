package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.Map;

public interface N3Validator {
	public Map<String,String> validate(EditConfiguration editConfig, EditSubmission editSub);	
}
