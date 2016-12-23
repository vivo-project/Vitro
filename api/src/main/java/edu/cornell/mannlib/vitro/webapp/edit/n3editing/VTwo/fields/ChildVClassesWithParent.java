/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

public class ChildVClassesWithParent implements FieldOptions {

    private static final String LEFT_BLANK = "";
    String fieldName;
    String classUri;
    String defaultOptionLabel = null;
    
    public ChildVClassesWithParent(String classUri) throws Exception {
        super();
        
        if (classUri==null || classUri.equals(""))
            throw new Exception ("vclassUri not set");
        
        this.classUri = classUri;
    }
   
    public ChildVClassesWithParent setDefaultOption(String label){
        this.defaultOptionLabel = label;
        return this;
    }
    
    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) throws Exception {
        
        HashMap <String,String> optionsMap = new LinkedHashMap<String,String>();      
        // first test to see whether there's a default "leave blank" value specified with the literal options        
        if ( ! StringUtils.isEmpty( defaultOptionLabel ) ){
            optionsMap.put(LEFT_BLANK, defaultOptionLabel);        
        } 
        
        optionsMap.put(classUri, "Other");       

        VClassDao vclassDao = wDaoFact.getVClassDao();
        List<String> subClassList = vclassDao.getAllSubClassURIs(classUri);
        if (subClassList != null && subClassList.size() > 0) {
            for (String subClassUri : subClassList) {
                VClass subClass = vclassDao.getVClassByURI(subClassUri);
                if (subClass != null && !OWL.Nothing.getURI().equals(subClassUri)) {
                    optionsMap.put(subClassUri, subClass.getName().trim());         
                }
            }
        }
       return optionsMap;
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return null;
    }

}
