/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Literal;

public class PersonHasPositionValidator implements N3Validator {
	 
	public Map<String,String> validate(EditConfiguration editConfig, EditSubmission editSub){
//		Map<String,String> existingUris = editConfig.getUrisInScope();
//		Map<String,Literal> existingLiterals = editConfig.getLiteralsInScope();					 
		Map<String,String> urisFromForm = editSub.getUrisFromForm();
		Map<String,Literal> literalsFromForm = editSub.getLiteralsFromForm();
		
		Literal newOrgName = literalsFromForm.get("newOrgName");
		if( newOrgName.getLexicalForm() != null && "".equals(newOrgName.getLexicalForm()) )
			newOrgName = null;
		String newOrgType = urisFromForm.get("newOrgType");
		if( "".equals(newOrgType ) )
			newOrgType = null;
		String organizationUri = urisFromForm.get("organizationUri");
		if( "".equals(organizationUri))
			organizationUri = null;
		
		System.out.println("newOrgName " + newOrgName);
		System.out.println("newOrgType " + newOrgType);
		System.out.println("organizationUri " + organizationUri);
		
		Map<String,String> errors = new HashMap<String,String>();		
		if( organizationUri != null && (newOrgName != null || newOrgType != null)  ){
			errors.put("newOrgName", "Must choose from an existing orginization or create a new one, not both.");	
			errors.put("organizationUri", "Must choose from an existing orginizations or create a new one, not both.");
		}else if( organizationUri == null && newOrgName != null && newOrgType == null) {
			errors.put("newOrgType", "Must select a type for the new organization");			
		}else if( organizationUri == null && newOrgName == null && newOrgType != null) {
			errors.put("newOrgName", "Must select a name for the new organization");			
		}
		
		if( errors.size() != 0 )
			return errors;
		else 
			return null;
   }
}