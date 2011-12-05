/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.web.templatemodels;


import org.junit.Test;

public class BaseTemplateModelTest {

    private static String value;
    
    @Test 
    public void testCleanURIofNull(){
        
        BaseTemplateModel btm = new BaseTemplateModel(){};
        //should not throw NPE
        value = btm.cleanURIForDisplay( null );
        
        //should not throw NPE
        value = btm.cleanTextForDisplay( null );                
    }

}
