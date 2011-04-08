/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class DumpDirectiveTest {
    
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
    }

    @Test
    public void dumpString() {
        
        String varName = "dog";
        String value = "Rover";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        dataModel.put(varName, value);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "String");
        expected.put("value", value);

        try {           
            Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
            Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
            assertEquals(expected, dumpData);           
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void dumpBoolean() {

        String varName = "hasSiteAdminAccess";
        boolean value = true;
        Map<String, Object> dataModel = new HashMap<String, Object>();
        dataModel.put(varName, value);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Boolean");
        expected.put("value", value);

        try {           
            Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
            Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
            assertEquals(expected, dumpData);           
        } catch (Exception e) {
            fail(e.getMessage());
        }       
    }
    
    @Test
    public void dumpNumber() {
        
        String varName = "tabCount";
        int value = 7;
        Map<String, Object> dataModel = new HashMap<String, Object>();
        dataModel.put(varName, value);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Number");
        expected.put("value", value);

        try {           
            Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
            Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
            assertEquals(expected, dumpData);           
        } catch (Exception e) {
            fail(e.getMessage());
        }           
    }
    
    @Test
    public void dumpDate() {

        String varName = "tabCount";
        int value = 7;
        Map<String, Object> dataModel = new HashMap<String, Object>();
        dataModel.put(varName, value);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Number");
        expected.put("value", value);

        try {           
            Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
            Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
            assertEquals(expected, dumpData);           
        } catch (Exception e) {
            fail(e.getMessage());
        } 
    }
     
   // RY test method and directive types with and without help methods
    
   @Test
   public void dumpHelplessMethod() {

       String varName = "square";
       TemplateMethodModel value = new HelplessMethod();
       Map<String, Object> dataModel = new HashMap<String, Object>();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Method");
       expected.put("help", null);

       try {           
           Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
           Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
           assertEquals(expected, dumpData);           
       } catch (Exception e) {
           fail(e.getMessage());
       } 
   }

   @Test
   public void dumpHelpfulMethod() {

       String varName = "square";
       TemplateMethodModel value = new HelpfulMethod();
       Map<String, Object> dataModel = new HashMap<String, Object>();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Method");
       expected.put("help", getMethodHelp(varName));

       try {           
           Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
           Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
           assertEquals(expected, dumpData);           
       } catch (Exception e) {
           fail(e.getMessage());
       } 
   }

   @Test
   public void dumpMethodWithBadHelp() {

       String varName = "square";
       TemplateMethodModel value = new MethodWithBadHelp();
       Map<String, Object> dataModel = new HashMap<String, Object>();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Method");
       expected.put("help", null);

       try {           
           Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
           Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
           assertEquals(expected, dumpData);           
       } catch (Exception e) {
           fail(e.getMessage());
       } 
   }
   @Test
   public void dumpHelplessDirective() {

       String varName = "dump";
       TemplateDirectiveModel value = new HelplessDirective();
       Map<String, Object> dataModel = new HashMap<String, Object>();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Directive");
       expected.put("help", null);

       try {           
           Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
           Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
           assertEquals(expected, dumpData);           
       } catch (Exception e) {
           fail(e.getMessage());
       } 
   }

   @Test
   public void dumpHelpfulDirective() {

       String varName = "dump";
       TemplateDirectiveModel value = new HelpfulDirective();
       Map<String, Object> dataModel = new HashMap<String, Object>();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Directive");
       expected.put("help", getDirectiveHelp(varName));

       try {           
           Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
           Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
           assertEquals(expected, dumpData);           
       } catch (Exception e) {
           fail(e.getMessage());
       } 
   }
 
   @Test
   public void dumpDirectiveWithBadHelp() {

       String varName = "dump";
       TemplateDirectiveModel value = new DirectiveWithBadHelp();
       Map<String, Object> dataModel = new HashMap<String, Object>();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Directive");
       expected.put("help", null);

       try {           
           Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
           Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
           assertEquals(expected, dumpData); 
       } catch (Exception e) {
           fail(e.getMessage());
       } 
   } 
    // RY Do these with different BeansWrappers
    @Test
    public void dumpSequence() {
        
    }
    
    @Test
    public void dumpHash() {
        
    }
    
    @Test
    public void dumpHashEx() {
        
    }
    
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
        
        map.put("name", name);
        
        map.put("returns", "The square of the argument");

        List<String>params = new ArrayList<String>();
        params.add("Integer to square");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add(name + "(4)");
        map.put("examples", examples);
        
        return map;     
    }
    

}
