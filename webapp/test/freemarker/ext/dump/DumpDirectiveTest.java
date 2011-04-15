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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.dump.BaseDumpDirective.DateType;
import freemarker.ext.dump.BaseDumpDirective.Key;
import freemarker.ext.dump.BaseDumpDirective.Type;
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

/**
 * Unit tests of dump directive. The tests follow the same basic pattern:
 * 1. Create the data model
 * 2. Create the expected dump data structure
 * 3. Create the actual dump data structure by running the data model through a processing environment
 * 4. Compare expected and actual dump data structures
 * 
 * @author rjy7
 *
 */
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
         
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.STRING);
        expectedDump.put(Key.VALUE.toString(), value);

        test(varName, dataModel, expectedDump);
    }
    
    @Test
    public void dumpBoolean() {

        String varName = "isLoggedIn";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        boolean value = true;
        dataModel.put(varName, value);
         
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.BOOLEAN);
        expectedDump.put(Key.VALUE.toString(), value);

        test(varName, dataModel, expectedDump);     
    }
    
    @Test
    public void dumpNumber() {
        
        String varName = "tabCount";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        int value = 7;
        dataModel.put(varName, value);
         
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.NUMBER);
        expectedDump.put(Key.VALUE.toString(), value);

        test(varName, dataModel, expectedDump);         
    }
    
    @Test
    public void dumpSimpleDate() {

        String varName = "now";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Date now = new Date();
        dataModel.put(varName, now);
         
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.DATE);
        expectedDump.put(Key.DATE_TYPE.toString(), DateType.UNKNOWN);
        expectedDump.put(Key.VALUE.toString(), now);

        test(varName, dataModel, expectedDump);
    }

    @Test
    public void dumpDateTime() {
        
        String varName = "timestamp";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Timestamp ts = new Timestamp(1302297332043L);
        dataModel.put(varName, ts);
         
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.DATE);
        expectedDump.put(Key.DATE_TYPE.toString(), DateType.DATETIME);
        expectedDump.put(Key.VALUE.toString(), ts);

        test(varName, dataModel, expectedDump);
    }

    @Test
    public void dumpSqlDate() {
        
        String varName = "date";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        java.sql.Date date = new java.sql.Date(1302297332043L);
        dataModel.put(varName, date);
         
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.DATE);
        expectedDump.put(Key.DATE_TYPE.toString(), DateType.DATE);
        expectedDump.put(Key.VALUE.toString(), date);

        test(varName, dataModel, expectedDump);
    }
    
    @Test
    public void dumpTime() {
        
        String varName = "time";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Time time = new Time(1302297332043L);
        dataModel.put(varName, time);
         
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.DATE);
        expectedDump.put(Key.DATE_TYPE.toString(), DateType.TIME);
        expectedDump.put(Key.VALUE.toString(), time);

        test(varName, dataModel, expectedDump);
    }
    
   // RY test method and directive types with and without help methods
    
   @Test
   public void dumpHelplessMethod() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel methodModel = new HelplessMethod();
       dataModel.put(varName, methodModel);
        
       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(Key.NAME.toString(), varName);
       expectedDump.put(Key.TYPE.toString(), Type.METHOD);
       expectedDump.put("help", null);

       test(varName, dataModel, expectedDump); 
   }

   @Test
   public void dumpHelpfulMethod() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel methodModel = new HelpfulMethod();
       dataModel.put(varName, methodModel);
        
       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(Key.NAME.toString(), varName);
       expectedDump.put(Key.TYPE.toString(), Type.METHOD);
       expectedDump.put("help", getMethodHelp(varName));

       test(varName, dataModel, expectedDump);
   }

   @Test
   public void dumpMethodWithBadHelp() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel methodModel = new MethodWithBadHelp();
       dataModel.put(varName, methodModel);
        
       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(Key.NAME.toString(), varName);
       expectedDump.put(Key.TYPE.toString(), Type.METHOD);
       expectedDump.put("help", null);

       test(varName, dataModel, expectedDump);
   }
   
   @Test
   public void dumpHelplessDirective() {

       String varName = "dump";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateDirectiveModel directiveModel = new HelplessDirective();
       dataModel.put(varName, directiveModel);
        
       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(Key.NAME.toString(), varName);
       expectedDump.put(Key.TYPE.toString(), Type.DIRECTIVE);
       expectedDump.put("help", null);

       test(varName, dataModel, expectedDump);
   }

   @Test
   public void dumpHelpfulDirective() {

       String varName = "dump";   
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateDirectiveModel directiveModel = new HelpfulDirective();
       dataModel.put(varName, directiveModel);
        
       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(Key.NAME.toString(), varName);
       expectedDump.put(Key.TYPE.toString(), Type.DIRECTIVE);
       expectedDump.put("help", getDirectiveHelp(varName));

       test(varName, dataModel, expectedDump); 
   }
 
   @Test
   public void dumpDirectiveWithBadHelp() {

       String varName = "dump";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateDirectiveModel directiveModel = new DirectiveWithBadHelp();
       dataModel.put(varName, directiveModel);
        
       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(Key.NAME.toString(), varName);
       expectedDump.put(Key.TYPE.toString(), Type.DIRECTIVE);
       expectedDump.put("help", null);

       test(varName, dataModel, expectedDump);
   } 
   
    @Test
    public void dumpStringList() {
        
        String varName = "fruit";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        List<String> myList = new ArrayList<String>();
        myList.add("apples");
        myList.add("bananas");
        myList.add("oranges");
        dataModel.put(varName, myList);
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.SEQUENCE);
        List<Map<String, Object>> myListExpectedDump = new ArrayList<Map<String, Object>>(myList.size());
        for ( String str : myList) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), str);
            myListExpectedDump.add(itemDump);
        }
        expectedDump.put(Key.VALUE.toString(), myListExpectedDump);

        test(varName, dataModel, expectedDump);       
    }
    
    @Test
    public void dumpStringArray() {
        
        String varName = "fruit";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        String[] myArray = { "apples", "bananas", "oranges" };
        dataModel.put(varName, myArray);
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.SEQUENCE);
        List<Map<String, Object>> myArrayExpectedDump = new ArrayList<Map<String, Object>>(myArray.length);
        for ( String str : myArray) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), str);
            myArrayExpectedDump.add(itemDump);
        }
        expectedDump.put(Key.VALUE.toString(), myArrayExpectedDump);

        test(varName, dataModel, expectedDump);       
    }    
    
    @Test
    public void dumpMixedList() {
        
        String varName = "stuff";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        List<Object> mixedList = new ArrayList<Object>();
        
        String myString = "apples";
        mixedList.add(myString);
        
        int myInt = 4;
        mixedList.add(myInt);
        
        boolean myBool = true;
        mixedList.add(myBool);
        
        List<String> myList = new ArrayList<String>();
        myList.add("dog");
        myList.add("cat");
        myList.add("elephant");
        mixedList.add(myList);
        
        dataModel.put(varName, mixedList);
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.SEQUENCE);
        
        List<Map<String, Object>> mixedListExpectedDump = new ArrayList<Map<String, Object>>(mixedList.size());
        
        Map<String, Object> myStringExpectedDump = new HashMap<String, Object>();        
        myStringExpectedDump.put(Key.TYPE.toString(), Type.STRING);
        myStringExpectedDump.put(Key.VALUE.toString(), myString);
        mixedListExpectedDump.add(myStringExpectedDump);
 
        Map<String, Object> myIntExpectedDump = new HashMap<String, Object>();  
        myIntExpectedDump.put(Key.TYPE.toString(), Type.NUMBER);
        myIntExpectedDump.put(Key.VALUE.toString(), myInt);
        mixedListExpectedDump.add(myIntExpectedDump);
        
        Map<String, Object> myBoolExpectedDump = new HashMap<String, Object>();  
        myBoolExpectedDump.put(Key.TYPE.toString(), Type.BOOLEAN);
        myBoolExpectedDump.put(Key.VALUE.toString(), myBool);
        mixedListExpectedDump.add(myBoolExpectedDump);
        
        Map<String, Object> myListExpectedDump = new HashMap<String, Object>();
        myListExpectedDump.put(Key.TYPE.toString(), Type.SEQUENCE); 
        List<Map<String, Object>> myListItemsExpectedDump = new ArrayList<Map<String, Object>>(myList.size());
        for ( String animal : myList ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), animal);
            myListItemsExpectedDump.add(itemDump);            
        }        
        myListExpectedDump.put(Key.VALUE.toString(), myListItemsExpectedDump);
        mixedListExpectedDump.add(myListExpectedDump);
        
        expectedDump.put(Key.VALUE.toString(), mixedListExpectedDump);

        test(varName, dataModel, expectedDump);       
    }    

    @Test
    public void dumpNumberSet() {

        String varName = "oddNums";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Set<Integer> myIntSet = new HashSet<Integer>();
        for (int i=0; i <= 10; i++) {
            if (i % 2 == 1) {
                myIntSet.add(i);
            }
        }
        dataModel.put(varName, myIntSet);
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.SEQUENCE);        
        List<Map<String, Object>> myIntSetExpectedDump = new ArrayList<Map<String, Object>>(myIntSet.size());
        for ( int i : myIntSet ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.NUMBER);
            itemDump.put(Key.VALUE.toString(), i);
            myIntSetExpectedDump.add(itemDump);
        }
        expectedDump.put(Key.VALUE.toString(), myIntSetExpectedDump);

        test(varName, dataModel, expectedDump); 
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
        TemplateCollectionModel myCollection = new SimpleCollection(odds);
        dataModel.put(varName, myCollection);
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.COLLECTION);       
        List<Map<String, Object>> myCollectionExpectedDump = new ArrayList<Map<String, Object>>(odds.size());
        for ( int i : odds ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.NUMBER);
            itemDump.put(Key.VALUE.toString(), i);
            myCollectionExpectedDump.add(itemDump);
        }
        expectedDump.put(Key.VALUE.toString(), myCollectionExpectedDump);

        test(varName, dataModel, expectedDump); 
    }    
    
    @Test
    public void dumpHash() {
        
    }

    @Test
    public void dumpStringToStringMap() {

        String varName = "capitals";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("Albany", "New York");
        myMap.put("St. Paul", "Minnesota");
        myMap.put("Austin", "Texas");
        myMap.put("Sacramento", "California");
        myMap.put("Richmond", "Virginia");
        dataModel.put(varName, myMap);
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.HASH_EX);
        SortedMap<String, Object> myMapExpectedDump = new TreeMap<String, Object>();

        for ( String key : myMap.keySet() ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), myMap.get(key));
            myMapExpectedDump.put(key, itemDump);
        }
        expectedDump.put(Key.VALUE.toString(), (myMapExpectedDump));

        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump); 

        // Test the sorting of the map
        List<String> expectedKeys = new ArrayList<String>(myMapExpectedDump.keySet());
        @SuppressWarnings("unchecked")
        SortedMap<String, Object> myMapActualDump = (SortedMap<String, Object>) dump.get(Key.VALUE.toString());
        List<String> actualKeys = new ArrayList<String>(myMapActualDump.keySet());
        assertEquals(expectedKeys, actualKeys);               
    }
    
    @Test
    public void dumpStringToObjectMap() {

        String varName = "stuff";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Map<String, Object> mixedMap = new HashMap<String, Object>();
        
        String myString = "apples";
        mixedMap.put("myString", myString);
        
        boolean myBool = true;
        mixedMap.put("myBoolean", myBool);
        
        int myInt = 4;
        mixedMap.put("myNumber", myInt);
        
        Date myDate = new Date();
        mixedMap.put("myDate", myDate);
        
        List<String> myList = new ArrayList<String>();
        myList.add("apples");
        myList.add("bananas");
        myList.add("oranges");
        mixedMap.put("myList", myList);
        
        Map<String, String> myMap = new HashMap<String, String>();
        myMap.put("Great Expectations", "Charles Dickens");
        myMap.put("Pride and Prejudice", "Jane Austen");
        myMap.put("Middlemarch", "George Eliot");
        myMap.put("Jude the Obscure", "Thomas Hardy");
        mixedMap.put("myMap", myMap);
        
        dataModel.put(varName, mixedMap);
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), Type.HASH_EX);
        Map<String, Object> mixedMapExpectedDump = new HashMap<String, Object>(mixedMap.size());
        
        Map<String, Object> myStringExpectedDump = new HashMap<String, Object>();
        myStringExpectedDump.put(Key.TYPE.toString(), Type.STRING);
        myStringExpectedDump.put(Key.VALUE.toString(), myString);
        mixedMapExpectedDump.put("myString", myStringExpectedDump);
        
        Map<String, Object> myBooleanExpectedDump = new HashMap<String, Object>();
        myBooleanExpectedDump.put(Key.TYPE.toString(), Type.BOOLEAN);
        myBooleanExpectedDump.put(Key.VALUE.toString(), myBool);
        mixedMapExpectedDump.put("myBoolean", myBooleanExpectedDump);
 
        Map<String, Object> myIntExpectedDump = new HashMap<String, Object>();
        myIntExpectedDump.put(Key.TYPE.toString(), Type.NUMBER);
        myIntExpectedDump.put(Key.VALUE.toString(), myInt);
        mixedMapExpectedDump.put("myNumber", myIntExpectedDump);
        
        Map<String, Object> myDateExpectedDump = new HashMap<String, Object>();
        myDateExpectedDump.put(Key.TYPE.toString(), Type.DATE);
        myDateExpectedDump.put(Key.DATE_TYPE.toString(), DateType.UNKNOWN);
        myDateExpectedDump.put(Key.VALUE.toString(), myDate);
        mixedMapExpectedDump.put("myDate", myDateExpectedDump);
        
        Map<String, Object> myListExpectedDump = new HashMap<String, Object>();
        myListExpectedDump.put(Key.TYPE.toString(), Type.SEQUENCE); 
        List<Map<String, Object>> myListItemsExpectedDump = new ArrayList<Map<String, Object>>(myList.size());
        for ( String item : myList ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), item);
            myListItemsExpectedDump.add(itemDump);            
        }        
        myListExpectedDump.put(Key.VALUE.toString(), myListItemsExpectedDump);
        mixedMapExpectedDump.put("myList", myListExpectedDump);
        
        Map<String, Object> myMapExpectedDump = new HashMap<String, Object>();
        myMapExpectedDump.put(Key.TYPE.toString(), Type.HASH_EX);
        SortedMap<String, Object> myMapItemsExpectedDump = new TreeMap<String, Object>();
        for ( String key : myMap.keySet() ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), myMap.get(key));
            myMapItemsExpectedDump.put(key, itemDump);
        }
        myMapExpectedDump.put(Key.VALUE.toString(), myMapItemsExpectedDump);
        mixedMapExpectedDump.put("myMap", myMapExpectedDump);
        
        expectedDump.put(Key.VALUE.toString(), mixedMapExpectedDump);
   
        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump); 

        // Test the sorting of the myMap dump
        List<String> expectedKeys = new ArrayList<String>(myMapItemsExpectedDump.keySet());
        @SuppressWarnings("unchecked")
        Map<String, Object> mixedMapActualDump = (Map<String, Object>) dump.get(Key.VALUE.toString());
        @SuppressWarnings("unchecked")
        Map<String, Object> myMapActualDump = (Map<String, Object>) mixedMapActualDump.get("myMap");        
        @SuppressWarnings("unchecked")
        Map<String, Object> myMapItemsActualDump = (SortedMap<String, Object>) myMapActualDump.get(Key.VALUE.toString());
        List<String> actualKeys = new ArrayList<String>(myMapItemsActualDump.keySet());
        assertEquals(expectedKeys, actualKeys);   
    }    

    @Test
    public void dumpObjectWithExposeNothingWrapper() {
        
        String varName = "employee";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_NOTHING);
        try {
            dataModel.put("employee", wrapper.wrap(getEmployee()));
        } catch (TemplateModelException e) {
            // ??
        }
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), "freemarker.ext.dump.DumpDirectiveTest$Employee");
        expectedDump.put(Key.VALUE.toString(), new HashMap<String, Object>());
        test(varName, dataModel, expectedDump);         
    }
 
    @Test
    public void dumpObjectWithExposePropertiesOnlyWrapper() {
        
        String varName = "employee";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_PROPERTIES_ONLY);
        try {
            dataModel.put("employee", wrapper.wrap(getEmployee()));
        } catch (TemplateModelException e) {
            // ??
        }
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), "freemarker.ext.dump.DumpDirectiveTest$Employee");
        
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("fullName", "John Doe");
        properties.put("nickname", "");
        properties.put("id", 34523);     
        properties.put("supervisor", "");
        properties.put("supervisees", "");
        Calendar c = Calendar.getInstance();
        c.set(75, Calendar.MAY, 5);
        properties.put("birthdate", c.getTime());

        expectedDump.put(Key.VALUE.toString(), properties);
        
        //test(varName, dataModel, expectedDump);         
    }
    
    @Test
    public void dumpObjectWithExposeSafeWrapper() {
        
        String varName = "employee";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_NOTHING);
        try {
            dataModel.put("employee", wrapper.wrap(getEmployee()));
        } catch (TemplateModelException e) {
            // ??
        }
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), "freemarker.ext.dump.DumpDirectiveTest$Employee");
        expectedDump.put(Key.VALUE.toString(), new HashMap<String, Object>());
        
        test(varName, dataModel, expectedDump);         
    }
    
    @Test
    public void dumpObjectWithExposeAllWrapper() {
        
        String varName = "employee";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(BeansWrapper.EXPOSE_NOTHING);
        try {
            dataModel.put("employee", wrapper.wrap(getEmployee()));
        } catch (TemplateModelException e) {
            // ??
        }
        
        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(Key.NAME.toString(), varName);
        expectedDump.put(Key.TYPE.toString(), "freemarker.ext.dump.DumpDirectiveTest$Employee");
        expectedDump.put(Key.VALUE.toString(), new HashMap<String, Object>());
        test(varName, dataModel, expectedDump);         
    }
    
    
    /////////////////////////// Private stub classes and helper methods ///////////////////////////
    
    private void test(String varName, Map<String, Object> dataModel, Map<String, Object> expectedDump) {
        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump);        
    }
    
    private Map<String, Object> getDump(String varName, Map<String, Object> dataModel) {
        try {
            Environment env = template.createProcessingEnvironment(dataModel, new StringWriter());
            return new DumpDirective().getTemplateVariableData(varName, env);     
        } catch (Exception e) {
            fail(e.getMessage());
            return null;
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
        
        map.put(Key.NAME.toString(), name);
        
        map.put("returns", "The square of the argument");

        List<String>params = new ArrayList<String>();
        params.add("Integer to square");
        map.put("params", params);
        
        List<String> examples = new ArrayList<String>();
        examples.add(name + "(4)");
        map.put("examples", examples);
        
        return map;     
    }
    
    public static class Employee {
        
        private static int count = 0;
        
        private String firstName;
        private String lastName;
        private String nickname;
        private Date birthdate;
        private int id;
        private Employee supervisor;
        private List<Employee> supervisees;
        private float salary;
        
        Employee(String firstName, String lastName, Date birthdate, int id) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthdate = birthdate;
            this.id = id;
            this.nickname = "";
            count++;
        }

        void setSupervisor(Employee supervisor) {
            this.supervisor = supervisor;
        }

        void setSupervisees(List<Employee> supervisees) {
            this.supervisees = supervisees;
        }

        void setSalary(float salary) {
            this.salary = salary;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        // Not available to templates
        float getSalary() {
            return salary;
        }
        
        public static int getEmployeeCount() {
            return count;
        }
        
        /*  Public accessor methods for templates */
        
        public String getFullName() {
            return firstName + " " + lastName;
        }
        
        public String getName(String which) {
            return which == "first" ? firstName : lastName;
        }

        public String getNickname() {
            return nickname;
        }
        
        public Date getBirthdate() {
            return birthdate;
        }
        
        public int getId() {
            return id;
        }      
 
        @Deprecated
        public int getFormerId() {
            return id % 10000;
        }

        public Employee getSupervisor() {
            return supervisor;
        }
        
        public List<Employee> getSupervisees() {
            return supervisees;
        }
    }
    
    private Employee getEmployee() {
        Calendar c = Calendar.getInstance();
        c.set(75, Calendar.MAY, 5);
        Employee jdoe = new Employee("John", "Doe", c.getTime(), 34523);
        
        c.set(65, Calendar.AUGUST, 10);
        Employee jsmith = new Employee("Jane", "Smith", c.getTime(), 78234);
        
        c.set(80, Calendar.JUNE, 20);
        Employee mjones = new Employee("Michael", "Jones", c.getTime(), 65432);
        
        c.set(81, Calendar.NOVEMBER, 30);
        Employee mturner = new Employee("Mary", "Turner", c.getTime(), 89531);
        
        List<Employee> supervisees = new ArrayList<Employee>();
        supervisees.add(mjones);
        supervisees.add(mturner);
        jdoe.setSupervisor(jsmith);
        jdoe.setSupervisees(supervisees);
        jdoe.setSalary(65000);
        
        return jdoe;
    }
}
