/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class DumpAllDirectiveTest {

    private Template template;

    @Before
    public void setUp() {
        Configuration config = new Configuration();
        String templateStr = "";
        try {
            template = new Template("template", new StringReader(templateStr), config);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        // Turn off log messages to console
        Logger.getLogger(BaseDumpDirective.class).setLevel(Level.OFF);
    }

    @Test 
    public void dumpDataModel() {
        
    }
    
    
    /////////////////////////// Private stub classes and helper methods ///////////////////////////

    

}
