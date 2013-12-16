/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;

public class ConstantFieldOptions implements FieldOptions {

    List<List<String>> options;       
    
    public ConstantFieldOptions(String ... optionPairs) throws Exception {
        super();                
        
        if (optionPairs==null) 
            throw new Exception("Must specify option pairs in ConstantFieldOptions constructor");
        
        if( optionPairs.length % 2 != 0)
            throw new Exception("options must be in pairs of (value,lable)");
        
        options = new ArrayList<List<String>>( optionPairs.length / 2 );        
        for(int i=0; i< optionPairs.length ; i=i+2){
            List<String> pair = new ArrayList<String>(2);                        
            pair.add(optionPairs[i]);
            pair.add(optionPairs[i+1]);                        
            options.add( pair );
        }                
    }


    public ConstantFieldOptions(String fieldName2,
            List<List<String>> optionPairs) throws Exception {
                        
        for(List<String> literalPair: optionPairs ){
            if( literalPair == null)
                throw new Exception("no items in optionPairs may be null.");
            if( literalPair.size() == 0 )
                throw new Exception("no items in optionPairs  may be empty lists.");
            if( literalPair.size() > 2)
                throw new Exception("no items in optionPairs  may be lists longer than 2 items.");                        
        }
                
        options = optionPairs;
    }


    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig, 
            String fieldName, 
            WebappDaoFactory wDaoFact) throws Exception {
        // originally not auto-sorted but sorted now, and empty values not removed or replaced
        HashMap <String,String> optionsMap = new LinkedHashMap<String,String>();
        
        for(Object obj: ((Iterable)options)){
            List<String> literalPair = (List)obj;
            String value=(String)literalPair.get(0);
            if( value != null){  // allow empty string as a value
                String label=(String)literalPair.get(1);
                if (label!=null) { 
                    optionsMap.put(value,label);
                } else {
                    optionsMap.put(value, value);
                }                
            }
        }
        
        return optionsMap;
    }
    
    public Comparator<String[]> getCustomComparator() {
    	return null;
    }

}
