/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

public class ChildVClassesOptions implements FieldOptions {

    private static final String LEFT_BLANK = "";
    private String vclassUri;

    private String defaultOptionLabel;
    
    static Log log = LogFactory.getLog(ChildVClassesOptions.class);
    
    public ChildVClassesOptions(String vclassUri) {
        super();        
        this.vclassUri = vclassUri;        
    }
    
    public ChildVClassesOptions setDefaultOptionLabel(String label){
        this.defaultOptionLabel = label;
        return this;
    }

    @Override
    public Map<String, String> getOptions(            
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) throws Exception{
        // now create an empty HashMap to populate and return
        HashMap <String,String> optionsMap = new LinkedHashMap<String,String>();
        
        // for debugging, keep a count of the number of options populated
        int optionsCount=0;

        
        if (vclassUri==null || vclassUri.equals("")){
            throw new Exception("no vclassUri found for field \""+fieldName+"\" in SelectListGenerator.getOptions() when OptionsType CHILD_VCLASSES specified");
        } else {
            
            // first test to see whether there's a default "leave blank" value specified with the literal options            
            if (defaultOptionLabel!=null) {
                optionsMap.put(LEFT_BLANK, defaultOptionLabel);
            }
            
            // now populate the options                            
            VClassDao vclassDao = wDaoFact.getVClassDao();
            List<String> subClassList = vclassDao.getAllSubClassURIs(vclassUri);
            
            if( subClassList == null || subClassList.size()==0 ) { 
                log.debug("No subclasses of " + vclassUri + " found in the model so only default value from field's literalOptions will be used" );
            } else {                
                for( String subClassUri : subClassList ) {
                    VClass subClass = vclassDao.getVClassByURI(subClassUri);
                    if( subClass != null && !OWL.Nothing.getURI().equals(subClassUri)) {                        
                        optionsMap.put(subClassUri,subClass.getName().trim());                        
                        ++optionsCount;
                    }
                }
            }
        }
        
        log.debug("added "+optionsCount+" options for field \""+fieldName+"\"");
        return optionsMap;
    }

    public String getClassUri(){
        return vclassUri;    
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return null;
    }
}
