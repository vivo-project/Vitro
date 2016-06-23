/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.Comparator;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

/**
 * Represents an object that can return a list of options
 * for an HTML select list.
 * 
 * @author bdc34 
 *        
 */
public interface FieldOptions {
    
    /**
     * Any object that are needed to get the options should
     * be passed in the constructor of the implementation.
     * 
     * @return return a map of value-&gt;label for the options.
     * Should never return null.
     * 
     * @throws Exception 
     */
    public Map<String,String> getOptions(
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) throws Exception;
    
    /*
     * Certain field options may have custom sorting requirements. If no sorting requirements exist,
     * then the method will return null.
     */
    
    public Comparator<String[]> getCustomComparator();
}

/*
 * lic enum OptionsType {
LITERALS,x

HARDCODED_LITERALS,
STRINGS_VIA_DATATYPE_PROPERTY, 

INDIVIDUALS_VIA_OBJECT_PROPERTY, x

INDIVIDUALS_VIA_VCLASS, 

CHILD_VCLASSES, x

CHILD_VCLASSES_WITH_PARENT,
VCLASSGROUP,
FILE, 
UNDEFINED, x
DATETIME, 
DATE,
TIME
*/