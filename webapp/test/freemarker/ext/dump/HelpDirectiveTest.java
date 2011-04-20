/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import freemarker.core.Environment;
import freemarker.ext.dump.BaseDumpDirective.Key;
import freemarker.ext.dump.BaseDumpDirective.Type;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class HelpDirectiveTest {

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
    public void dumpHelplessMethod() {

        String varName = "square";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        TemplateMethodModel methodModel = new HelplessMethod();
        dataModel.put(varName, methodModel);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.METHOD);
        expectedDumpValue.put("help", null);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump); 
    }

    @Test
    public void dumpHelpfulMethod() {

        String varName = "square";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        TemplateMethodModel methodModel = new HelpfulMethod();
        dataModel.put(varName, methodModel);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.METHOD);
        expectedDumpValue.put("help", getMethodHelp(varName));

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }

    @Test
    public void dumpMethodWithBadHelp() {

        String varName = "square";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        TemplateMethodModel methodModel = new MethodWithBadHelp();
        dataModel.put(varName, methodModel);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.METHOD);
        expectedDumpValue.put("help", null);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }
    
    @Test
    public void dumpHelplessDirective() {

        String varName = "dump";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        TemplateDirectiveModel directiveModel = new HelplessDirective();
        dataModel.put(varName, directiveModel);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.DIRECTIVE);
        expectedDumpValue.put("help", null);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }
    
    @Test
    public void dumpHelpfulDirective() {

        String varName = "dump";   
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        TemplateDirectiveModel directiveModel = new HelpfulDirective();
        dataModel.put(varName, directiveModel);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.DIRECTIVE);
        expectedDumpValue.put("help", getDirectiveHelp(varName));

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump); 
    }
  
    @Test
    public void dumpDirectiveWithBadHelp() {

        String varName = "dump";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        TemplateDirectiveModel directiveModel = new DirectiveWithBadHelp();
        dataModel.put(varName, directiveModel);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.DIRECTIVE);
        expectedDumpValue.put("help", null);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    } 
    
    /////////////////////////// Private test classes and helper methods ///////////////////////////

    private class HelplessMethod implements TemplateMethodModel {

        @Override
        public Object exec(List arg0) throws TemplateModelException {
            return null;
        }        
    }

    private class HelpfulMethod implements TemplateMethodModel {

        @Override
        public Object exec(List arg0) throws TemplateModelException {
            return null;
        } 
        
        public Map<String, Object> help(String name) {
            return getMethodHelp(name);
        }
    }

    private class MethodWithBadHelp implements TemplateMethodModel {

        @Override
        public Object exec(List arg0) throws TemplateModelException {
            return null;
        } 
        
        public Map<String, Object> help() {
            return new HashMap<String, Object>();
        }
    }
    
    private class HelplessDirective implements TemplateDirectiveModel {

        @Override
        public void execute(Environment arg0, Map arg1, TemplateModel[] arg2,
                TemplateDirectiveBody arg3) throws TemplateException,
                IOException {            
        }
    }

    private class HelpfulDirective implements TemplateDirectiveModel {

        @Override
        public void execute(Environment arg0, Map arg1, TemplateModel[] arg2,
                TemplateDirectiveBody arg3) throws TemplateException,
                IOException {            
        }
        
        public Map<String, Object> help(String name) {
            return getDirectiveHelp(name);
        }
    }
    
    private class DirectiveWithBadHelp implements TemplateDirectiveModel {

        @Override
        public void execute(Environment arg0, Map arg1, TemplateModel[] arg2,
                TemplateDirectiveBody arg3) throws TemplateException,
                IOException {            
        }
        
        public String help(String name) {
            return "help";
        }
    }
    
    private Map<String, Object> getDirectiveHelp(String name) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put("effect", "Dump the contents of a template variable.");
        
        map.put("comments", "Sequences (lists and arrays) are enclosed in square brackets. Hashes are enclosed in curly braces.");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("var", "name of variable to dump");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add("<@" + name + " var=\"urls\" />");
        map.put("examples", examples);
        
        return map;        
    }
 
    private Map<String, Object> getMethodHelp(String name) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put("returns", "The square of the argument");

        List<String>params = new ArrayList<String>();
        params.add("Integer to square");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add(name + "(4)");
        map.put("examples", examples);
        
        return map;     
    }
    
    private Map<String, Object> getDump(String varName, Map<String, Object> dataModel) {
        try {
            Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
            return new DumpDirective().getTemplateVariableDump(varName, env);     
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
        }             
    }  

    private void test(String varName, Map<String, Object> dataModel, Map<String, Object> expectedDump) {
        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump);        
    }
}
