/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors.utils;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

//Returns the appropriate n3 based on data getter

public interface ProcessDataGetterN3 {
	public List<String> retrieveN3Required(int counter);
    public List<String> retrieveN3Optional(int counter);
    public List<String >retrieveLiteralsOnForm(int counter);
    
     
    public List<String> retrieveUrisOnForm(int counter);
    public List<FieldVTwo> retrieveFields(int counter);
    public List<String> getLiteralVarNamesBase();
    public List<String> getUriVarNamesBase();
    public String getVarName(String base, int counter);
    public String getDataGetterVar(int counter);
    public List<String> getNewResources(int counter);

}
