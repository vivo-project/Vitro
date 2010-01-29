package edu.cornell.mannlib.vitro.webapp.edit.validator.impl;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vedit.validator.ValidationObject;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;

public class URIValidator {

	private WebappDaoFactory wadf = null;
	
	public URIValidator(WebappDaoFactory wadf) {
		this.wadf = wadf;
	}
	
	public ValidationObject validate (Object obj) throws IllegalArgumentException {
		ValidationObject vo = new ValidationObject();
		
		if (obj != null && obj instanceof String) {
			String errMsg = wadf.checkURI((String)obj);
			if (errMsg != null) {
				vo.setValid(false);
				vo.setMessage(errMsg);
			} else {
				vo.setValid(true);
			}
		} else {
			vo.setValid(false);
			vo.setMessage("Please enter a URI");
		}
		
		vo.setValidatedObject(obj);
		
		return vo;
	}
	
}
