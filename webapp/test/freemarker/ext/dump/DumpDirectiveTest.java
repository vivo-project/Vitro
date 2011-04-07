/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
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
            Assert.fail(e.getMessage());
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
            Assert.assertEquals(expected, dumpData);           
        } catch (Exception e) {
            Assert.fail(e.getMessage());
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
            Assert.assertEquals(expected, dumpData);           
        } catch (Exception e) {
            Assert.fail(e.getMessage());
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
            Assert.assertEquals(expected, dumpData);           
        } catch (Exception e) {
            Assert.fail(e.getMessage());
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
            Assert.assertEquals(expected, dumpData);           
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } 
    }
     
   // RY test method and directive types with and without help methods
    
   @Test
   public void dumpMethod() {

       String varName = "profileUri";
       TemplateMethodModel value = new StubMethodModel();
       Map<String, Object> dataModel = new HashMap<String, Object>();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Method");
       //expected.put("value", value);

       try {           
           Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
           Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
           Assert.assertEquals(expected, dumpData);           
       } catch (Exception e) {
           Assert.fail(e.getMessage());
       } 
   }

   @Test
   public void dumpDirective() {

       String varName = "widget";
       TemplateDirectiveModel value = new StubDirectiveModel();
       Map<String, Object> dataModel = new HashMap<String, Object>();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Directive");
       //expected.put("value", value);

       try {           
           Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
           Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
           Assert.assertEquals(expected, dumpData);           
       } catch (Exception e) {
           Assert.fail(e.getMessage());
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
    
    private class StubMethodModel implements TemplateMethodModel {

        @Override
        public Object exec(List arg0) throws TemplateModelException {
            return null;
        }        
    }
    
    private class StubDirectiveModel implements TemplateDirectiveModel {

        @Override
        public void execute(Environment arg0, Map arg1, TemplateModel[] arg2,
                TemplateDirectiveBody arg3) throws TemplateException,
                IOException {
            
        }
        
    }
}
