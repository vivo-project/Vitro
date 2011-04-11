/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package freemarker.ext.dump;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.SimpleCollection;
import freemarker.template.Template;
import freemarker.template.TemplateCollectionModel;
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
        // Turn off log messages to console
        Logger.getLogger(BaseDumpDirective.class).setLevel(Level.OFF);
    }

    @Test
    public void dumpString() {
        
        String varName = "dog";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        String value = "Rover";
        dataModel.put(varName, value);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "String");
        expected.put("value", value);

        test(varName, dataModel, expected);
    }
    
    @Test
    public void dumpBoolean() {

        String varName = "isLoggedIn";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        boolean value = true;
        dataModel.put(varName, value);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Boolean");
        expected.put("value", value);

        test(varName, dataModel, expected);     
    }
    
    @Test
    public void dumpNumber() {
        
        String varName = "tabCount";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        int value = 7;
        dataModel.put(varName, value);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Number");
        expected.put("value", value);

        test(varName, dataModel, expected);         
    }
    
    @Test
    public void dumpSimpleDate() {

        String varName = "now";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Date now = new Date();
        dataModel.put(varName, now);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Date");
        expected.put("dateType", "Unknown");
        expected.put("value", now);

        test(varName, dataModel, expected);
    }

    @Test
    public void dumpDateTime() {
        
        String varName = "timestamp";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Timestamp ts = new Timestamp(1302297332043L);
        dataModel.put(varName, ts);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Date");
        expected.put("dateType", "DateTime");
        expected.put("value", ts);

        test(varName, dataModel, expected);
    }

    @Test
    public void dumpSqlDate() {
        
        String varName = "date";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        java.sql.Date date = new java.sql.Date(1302297332043L);
        dataModel.put(varName, date);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Date");
        expected.put("dateType", "Date");
        expected.put("value", date);

        test(varName, dataModel, expected);
    }
    
    @Test
    public void dumpTime() {
        
        String varName = "time";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Time time = new Time(1302297332043L);
        dataModel.put(varName, time);
         
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Date");
        expected.put("dateType", "Time");
        expected.put("value", time);

        test(varName, dataModel, expected);
    }
    
   // RY test method and directive types with and without help methods
    
   @Test
   public void dumpHelplessMethod() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel value = new HelplessMethod();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Method");
       expected.put("help", null);

       test(varName, dataModel, expected); 
   }

   @Test
   public void dumpHelpfulMethod() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel value = new HelpfulMethod();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Method");
       expected.put("help", getMethodHelp(varName));

       test(varName, dataModel, expected);
   }

   @Test
   public void dumpMethodWithBadHelp() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel value = new MethodWithBadHelp();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Method");
       expected.put("help", null);

       test(varName, dataModel, expected);
   }
   
   @Test
   public void dumpHelplessDirective() {

       String varName = "dump";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateDirectiveModel value = new HelplessDirective();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Directive");
       expected.put("help", null);

       test(varName, dataModel, expected);
   }

   @Test
   public void dumpHelpfulDirective() {

       String varName = "dump";   
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateDirectiveModel value = new HelpfulDirective();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Directive");
       expected.put("help", getDirectiveHelp(varName));

       test(varName, dataModel, expected); 
   }
 
   @Test
   public void dumpDirectiveWithBadHelp() {

       String varName = "dump";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateDirectiveModel value = new DirectiveWithBadHelp();
       dataModel.put(varName, value);
        
       Map<String, Object> expected = new HashMap<String, Object>();
       expected.put("name", varName);
       expected.put("type", "Directive");
       expected.put("help", null);

       test(varName, dataModel, expected);
   } 
   
    @Test
    public void dumpStringList() {
        
        String varName = "fruit";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        List<String> list = new ArrayList<String>();
        list.add("apples");
        list.add("bananas");
        list.add("oranges");
        dataModel.put(varName, list);
        
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Sequence");
        List<Map<String, Object>> dump = new ArrayList<Map<String, Object>>();
        for ( String str : list) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("type", "String");
            map.put("value", str);
            dump.add(map);
        }
        expected.put("value", dump);

        test(varName, dataModel, expected);       
    }
    
    @Test
    public void dumpStringArray() {
        
        String varName = "fruit";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        String[] list = { "apples", "bananas", "oranges" };
        dataModel.put(varName, list);
        
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Sequence");
        List<Map<String, Object>> dump = new ArrayList<Map<String, Object>>();
        for ( String str : list) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("type", "String");
            map.put("value", str);
            dump.add(map);
        }
        expected.put("value", dump);

        test(varName, dataModel, expected);       
    }    
    
    @Test
    public void dumpMixedList() {
        
        String varName = "stuff";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        List<Object> list = new ArrayList<Object>();
        list.add("apples");
        list.add(4);
        list.add(false);
        List<String> animals = new ArrayList<String>();
        animals.add("dog");
        animals.add("cat");
        animals.add("elephant");
        Collections.sort(animals);
        list.add(animals);
        dataModel.put(varName, list);
        
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Sequence");
        
        List<Map<String, Object>> dump = new ArrayList<Map<String, Object>>();
        
        Map<String, Object> stringMap = new HashMap<String, Object>();        
        stringMap.put("type", "String");
        stringMap.put("value", "apples");
        dump.add(stringMap);
 
        Map<String, Object> numberMap = new HashMap<String, Object>();  
        numberMap.put("type", "Number");
        numberMap.put("value", 4);
        dump.add(numberMap);
        
        Map<String, Object> booleanMap = new HashMap<String, Object>();  
        booleanMap.put("type", "Boolean");
        booleanMap.put("value", false);
        dump.add(booleanMap);
        
        Map<String, Object> sequenceMap = new HashMap<String, Object>();
        sequenceMap.put("type", "Sequence"); 
        List<Map<String, Object>> animalDump = new ArrayList<Map<String, Object>>();
        for ( String animal : animals ) {
            Map<String, Object> animalMap = new HashMap<String, Object>();
            animalMap.put("type", "String");
            animalMap.put("value", animal);
            animalDump.add(animalMap);            
        }        
        sequenceMap.put("value", animalDump);
        dump.add(sequenceMap);
        
        expected.put("value", dump);

        test(varName, dataModel, expected);       
    }    

    @Test
    public void dumpNumberSet() {

        String varName = "oddNums";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Set<Integer> odds = new HashSet<Integer>();
        for (int i=0; i <= 10; i++) {
            if (i % 2 == 1) {
                odds.add(i);
            }
        }
        dataModel.put(varName, odds);
        
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Sequence");
        List<Map<String, Object>> dump = new ArrayList<Map<String, Object>>();
        for ( int i : odds ) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("type", "Number");
            map.put("value", i);
            dump.add(map);
        }
        expected.put("value", dump);

        test(varName, dataModel, expected); 
    }
    
    @Test
    public void dumpNumberCollection() {

        String varName = "oddNums";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Set<Integer> odds = new HashSet<Integer>();
        for (int i=0; i <= 10; i++) {
            if (i % 2 == 1) {
                odds.add(i);
            }
        }
        TemplateCollectionModel collection = new SimpleCollection(odds);
        dataModel.put(varName, collection);
        
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "Collection");
        List<Map<String, Object>> dump = new ArrayList<Map<String, Object>>();
        for ( int i : odds ) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("type", "Number");
            map.put("value", i);
            dump.add(map);
        }
        expected.put("value", dump);

        test(varName, dataModel, expected); 
    }    
    
    @Test
    public void dumpHash() {
        
    }

    // RY Do these with different BeansWrappers
    @Test
    public void dumpStringToStringMap() {

        String varName = "capitals";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Map<String, String> capitals = new HashMap<String, String>();
        capitals.put("Albany", "New York");
        capitals.put("St. Paul", "Minnesota");
        capitals.put("Austin", "Texas");
        capitals.put("Sacramento", "California");
        capitals.put("Richmond", "Virginia");
        dataModel.put(varName, capitals);
        
        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("name", varName);
        expected.put("type", "HashEx");
        Map<String, Object> dump = new HashMap<String, Object>();
        for ( String key : capitals.keySet() ) {
            Map<String, String> capitalDump = new HashMap<String, String>();
            capitalDump.put("type", "String");
            capitalDump.put("value", capitals.get(key));
            dump.put(key, capitalDump);
        }
        expected.put("value", dump);

        test(varName, dataModel, expected);  
    }
    
    /////////////////////////// Private stub classes and helper methods ///////////////////////////
    
    private void test(String varName, Map<String, Object> dataModel, Map<String, Object> expected) {
        try {           
            Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
            Map<String, Object> dumpData = new DumpDirective().getTemplateVariableData(varName, env);
            assertEquals(expected, dumpData); 
        } catch (Exception e) {
            fail(e.getMessage());
        }        
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
