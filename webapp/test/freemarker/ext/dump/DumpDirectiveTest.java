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
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.STRING);
        expectedDumpValue.put(Key.VALUE.toString(), value);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }
    
    @Test
    public void dumpBoolean() {

        String varName = "isLoggedIn";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        boolean value = true;
        dataModel.put(varName, value);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.BOOLEAN);
        expectedDumpValue.put(Key.VALUE.toString(), value);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);     
    }
    
    @Test
    public void dumpNumber() {
        
        String varName = "tabCount";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        int value = 7;
        dataModel.put(varName, value);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.NUMBER);
        expectedDumpValue.put(Key.VALUE.toString(), value);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);         
    }
    
    @Test
    public void dumpSimpleDate() {

        String varName = "now";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Date now = new Date();
        dataModel.put(varName, now);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.DATE);
        expectedDumpValue.put(Key.DATE_TYPE.toString(), DateType.UNKNOWN);
        expectedDumpValue.put(Key.VALUE.toString(), now);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }

    @Test
    public void dumpCalendarDate() {

        String varName = "myDate";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Calendar c = Calendar.getInstance();
        c.set(91, Calendar.MAY, 5);
        Date myDate = c.getTime();
        dataModel.put("myDate", myDate);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.DATE);
        expectedDumpValue.put(Key.DATE_TYPE.toString(), DateType.UNKNOWN);
        expectedDumpValue.put(Key.VALUE.toString(), myDate);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }
    @Test
    public void dumpDateTime() {
        
        String varName = "timestamp";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Timestamp ts = new Timestamp(1302297332043L);
        dataModel.put(varName, ts);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.DATE);
        expectedDumpValue.put(Key.DATE_TYPE.toString(), DateType.DATETIME);
        expectedDumpValue.put(Key.VALUE.toString(), ts);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }

    @Test
    public void dumpSqlDate() {
        
        String varName = "date";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        java.sql.Date date = new java.sql.Date(1302297332043L);
        dataModel.put(varName, date);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.DATE);
        expectedDumpValue.put(Key.DATE_TYPE.toString(), DateType.DATE);
        expectedDumpValue.put(Key.VALUE.toString(), date);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }
    
    @Test
    public void dumpTime() {
        
        String varName = "time";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Time time = new Time(1302297332043L);
        dataModel.put(varName, time);
         
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.DATE);
        expectedDumpValue.put(Key.DATE_TYPE.toString(), DateType.TIME);
        expectedDumpValue.put(Key.VALUE.toString(), time);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
    }
    
   // RY test method and directive types with and without help methods
    
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
   
    @Test
    public void dumpStringList() {
        
        String varName = "fruit";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        List<String> myList = new ArrayList<String>();
        myList.add("apples");
        myList.add("bananas");
        myList.add("oranges");
        dataModel.put(varName, myList);
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.SEQUENCE);
        List<Map<String, Object>> myListexpectedDumpValue = new ArrayList<Map<String, Object>>(myList.size());
        for ( String str : myList) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), str);
            myListexpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myListexpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);       
    }
    
    @Test
    public void dumpStringArray() {
        
        String varName = "fruit";        
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        String[] myArray = { "apples", "bananas", "oranges" };
        dataModel.put(varName, myArray);
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.SEQUENCE);
        List<Map<String, Object>> myArrayexpectedDumpValue = new ArrayList<Map<String, Object>>(myArray.length);
        for ( String str : myArray) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), str);
            myArrayexpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myArrayexpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
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
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.SEQUENCE);
        
        List<Map<String, Object>> mixedListexpectedDumpValue = new ArrayList<Map<String, Object>>(mixedList.size());
        
        Map<String, Object> myStringexpectedDumpValue = new HashMap<String, Object>();        
        myStringexpectedDumpValue.put(Key.TYPE.toString(), Type.STRING);
        myStringexpectedDumpValue.put(Key.VALUE.toString(), myString);
        mixedListexpectedDumpValue.add(myStringexpectedDumpValue);
 
        Map<String, Object> myIntexpectedDumpValue = new HashMap<String, Object>();  
        myIntexpectedDumpValue.put(Key.TYPE.toString(), Type.NUMBER);
        myIntexpectedDumpValue.put(Key.VALUE.toString(), myInt);
        mixedListexpectedDumpValue.add(myIntexpectedDumpValue);
        
        Map<String, Object> myBoolexpectedDumpValue = new HashMap<String, Object>();  
        myBoolexpectedDumpValue.put(Key.TYPE.toString(), Type.BOOLEAN);
        myBoolexpectedDumpValue.put(Key.VALUE.toString(), myBool);
        mixedListexpectedDumpValue.add(myBoolexpectedDumpValue);
        
        Map<String, Object> myListexpectedDumpValue = new HashMap<String, Object>();
        myListexpectedDumpValue.put(Key.TYPE.toString(), Type.SEQUENCE); 
        List<Map<String, Object>> myListItemsexpectedDumpValue = new ArrayList<Map<String, Object>>(myList.size());
        for ( String animal : myList ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), animal);
            myListItemsexpectedDumpValue.add(itemDump);            
        }        
        myListexpectedDumpValue.put(Key.VALUE.toString(), myListItemsexpectedDumpValue);
        mixedListexpectedDumpValue.add(myListexpectedDumpValue);
        
        expectedDumpValue.put(Key.VALUE.toString(), mixedListexpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
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
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.SEQUENCE);        
        List<Map<String, Object>> myIntSetexpectedDumpValue = new ArrayList<Map<String, Object>>(myIntSet.size());
        for ( int i : myIntSet ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.NUMBER);
            itemDump.put(Key.VALUE.toString(), i);
            myIntSetexpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myIntSetexpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
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
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.COLLECTION);       
        List<Map<String, Object>> myCollectionexpectedDumpValue = new ArrayList<Map<String, Object>>(odds.size());
        for ( int i : odds ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.NUMBER);
            itemDump.put(Key.VALUE.toString(), i);
            myCollectionexpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myCollectionexpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
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
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.HASH_EX);
        SortedMap<String, Object> myMapExpectedDump = new TreeMap<String, Object>();

        for ( String key : myMap.keySet() ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), myMap.get(key));
            myMapExpectedDump.put(key, itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), (myMapExpectedDump));

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump); 

        // Test the sorting of the map
        List<String> expectedKeys = new ArrayList<String>(myMapExpectedDump.keySet());
        @SuppressWarnings("unchecked")
        Map<String, Object> actualDumpValue = (Map<String, Object>) dump.get(varName);
        @SuppressWarnings("unchecked")
        SortedMap<String, Object> myMapActualDump = (SortedMap<String, Object>) actualDumpValue.get(Key.VALUE.toString());
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
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.HASH_EX);
        SortedMap<String, Object> mixedMapExpectedDump = new TreeMap<String, Object>();
        
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
        
        expectedDumpValue.put(Key.VALUE.toString(), mixedMapExpectedDump);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump); 

        // Test the sorting of the outer map        
        List<String> expectedDumpValueKeys = new ArrayList<String>(mixedMapExpectedDump.keySet());
        @SuppressWarnings("unchecked")
        Map<String, Object> actualDumpValue = (Map<String, Object>) dump.get(varName);
        @SuppressWarnings("unchecked")
        SortedMap<String, Object> mixedMapActualDump = (SortedMap<String, Object>) actualDumpValue.get(Key.VALUE.toString());
        List<String> actualDumpValueKeys = new ArrayList<String>(mixedMapActualDump.keySet());
        assertEquals(expectedDumpValueKeys, actualDumpValueKeys);
        
        // Test the sorting of the inner map
        List<String> myMapItemsExpectedDumpKeys = new ArrayList<String>(myMapItemsExpectedDump.keySet());
        @SuppressWarnings("unchecked")
        Map<String, Object> myMapActualDump = (Map<String, Object>) mixedMapActualDump.get("myMap");
        @SuppressWarnings("unchecked")
        SortedMap<String, Object> myMapItemsActualDump = (SortedMap<String, Object>) myMapActualDump.get(Key.VALUE.toString());
        List<String> myMapItemsActualDumpKeys = new ArrayList<String>(myMapItemsActualDump.keySet());
        assertEquals(myMapItemsExpectedDumpKeys, myMapItemsActualDumpKeys);
        
    }    

    @Test
    public void dumpObjectWithExposeNothingWrapper() {
        dumpObject(BeansWrapper.EXPOSE_NOTHING);       
    }
 
    @Test
    public void dumpObjectWithExposePropertiesOnlyWrapper() {
        dumpObject(BeansWrapper.EXPOSE_PROPERTIES_ONLY);  
    }
    
    @Test
    public void dumpObjectWithExposeSafeWrapper() {
        dumpObject(BeansWrapper.EXPOSE_SAFE);            
    }
    
    @Test
    public void dumpObjectWithExposeAllWrapper() {
        dumpObject(BeansWrapper.EXPOSE_ALL);    
    }
    
    
    /////////////////////////// Private stub classes and helper methods ///////////////////////////

    private void test(String varName, Map<String, Object> dataModel, Map<String, Object> expectedDump) {
        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump);        
    }
    
    private void dumpObject(int exposureLevel) {
        
        String varName = "employee";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(exposureLevel);
        try {
            dataModel.put("employee", wrapper.wrap(getEmployee()));
        } catch (TemplateModelException e) {
            // logging is suppressed, so what do we do here?
        }
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), "freemarker.ext.dump.DumpDirectiveTest$Employee");
        expectedDumpValue.put(Key.VALUE.toString(), getJohnDoeExpectedDump(exposureLevel));

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        testObjectDump(varName, dataModel, expectedDump);         
    }

    private void testObjectDump(String varName, Map<String, Object> dataModel, Map<String, Object> expectedDump) {
        
        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump);         
        
        // Test the sorting of the properties
        @SuppressWarnings("unchecked")
        Map<String, Object> expectedVarDump = (Map<String, Object>) expectedDump.get(varName);
        @SuppressWarnings("unchecked")
        Map<String, Object> expectedValueDump = (Map<String, Object>) expectedVarDump.get(Key.VALUE.toString());
        @SuppressWarnings("unchecked")
        SortedMap<String, Object> expectedPropertyDump = (SortedMap<String, Object>) expectedValueDump.get(Key.PROPERTIES.toString());
        List<String> expectedPropertyDumpKeys = new ArrayList<String>(expectedPropertyDump.keySet());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> actualVarDump = (Map<String, Object>) dump.get(varName);
        @SuppressWarnings("unchecked")
        Map<String, Object> actualValueDump = (Map<String, Object>) actualVarDump.get(Key.VALUE.toString());
        @SuppressWarnings("unchecked")
        SortedMap<String, Object> actualPropertyDump = (SortedMap<String, Object>) actualValueDump.get(Key.PROPERTIES.toString());
        List<String> actualPropertyDumpKeys = new ArrayList<String>(actualPropertyDump.keySet());
        
        assertEquals(expectedPropertyDumpKeys, actualPropertyDumpKeys);  
   
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
    
    public static class Employee {
        
        private static int count = 0;
        
        private String firstName;
        private String lastName;
        private String nickname;
        //private Date birthdate;
        private boolean married;
        private int id;
        private String middleName;
        private List<String> favoriteColors;
        private Employee supervisor;
        private float salary;
        
        Employee(String firstName, String lastName, int id) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.middleName = null; // test a null value
            //this.birthdate = birthdate;
            this.married = true;
            this.id = id;
            this.nickname = "";
            this.favoriteColors = new ArrayList<String>();
            count++;
        }

        protected void setSupervisor(Employee supervisor) {
            this.supervisor = supervisor;
        }

        void setSalary(float salary) {
            this.salary = salary;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
        
        public void setFavoriteColors(String...colors) {
            for (String color : colors) {
                favoriteColors.add(color);
            }
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
        
        public String getMiddleName() {
            return middleName;
        }

        public String getNickname() {
            return nickname;
        }
        
//        public Date getBirthdate() {
//            return birthdate;
//        }
        
        public int getId() {
            return id;
        }      
        
        public boolean isMarried() {
            return married;
        }
 
        @Deprecated
        public int getFormerId() {
            return id % 10000;
        }

        public Employee getSupervisor() {
            return supervisor;
        }
        
        public List<String> getFavoriteColors() {
            return favoriteColors;
        }
    }
    
    private Employee getEmployee() {

        Employee jdoe = new Employee("John", "Doe", 34523);
        jdoe.setFavoriteColors("blue", "green");
        jdoe.setSalary(65000);
        
        Employee jsmith = new Employee("Jane", "Smith", 78234);
        jsmith.setFavoriteColors("red", "orange");
        
        jdoe.setSupervisor(jsmith);

        return jdoe;
    }
    
    private Map<String, Object> getJohnDoeExpectedDump(int exposureLevel) {

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        
        // Properties
        SortedMap<String, Object> propertiesExpectedDump = new TreeMap<String, Object>();
        
        if (exposureLevel != BeansWrapper.EXPOSE_NOTHING) {           
    
//          Map<String, Object> birthdateExpectedDump = new HashMap<String, Object>();
//          birthdateExpectedDump.put(Key.TYPE.toString(), Type.DATE);
//          birthdateExpectedDump.put(Key.DATE_TYPE.toString(), DateType.UNKNOWN);
//          Calendar c = Calendar.getInstance();
//          c.set(75, Calendar.MAY, 5);
//          birthdateExpectedDump.put(Key.VALUE.toString(), c.getTime());
//          propertiesExpectedDump.put("birthdate", birthdateExpectedDump);
    
            Map<String, Object> fullNameExpectedDump = new HashMap<String, Object>();
            fullNameExpectedDump.put(Key.TYPE.toString(), Type.STRING);
            fullNameExpectedDump.put(Key.VALUE.toString(), "John Doe");
            propertiesExpectedDump.put("fullName", fullNameExpectedDump);
    
            Map<String, Object> idExpectedDump = new HashMap<String, Object>();
            idExpectedDump.put(Key.TYPE.toString(), Type.NUMBER);
            idExpectedDump.put(Key.VALUE.toString(), 34523);
            propertiesExpectedDump.put("id", idExpectedDump);
    
            Map<String, Object> nicknameExpectedDump = new HashMap<String, Object>();
            nicknameExpectedDump.put(Key.TYPE.toString(), Type.STRING);
            nicknameExpectedDump.put(Key.VALUE.toString(), "");
            propertiesExpectedDump.put("nickname", nicknameExpectedDump);
    
            Map<String, Object> middleNameExpectedDump = new HashMap<String, Object>();
            middleNameExpectedDump.put(Key.VALUE.toString(), "null");
            propertiesExpectedDump.put("middleName", middleNameExpectedDump);
            
            Map<String, Object> marriedExpectedDump = new HashMap<String, Object>();
            marriedExpectedDump.put(Key.TYPE.toString(), Type.BOOLEAN);
            marriedExpectedDump.put(Key.VALUE.toString(), true);
            propertiesExpectedDump.put("married", marriedExpectedDump);
    
            Map<String, Object> supervisorExpectedDump = new HashMap<String, Object>();
            supervisorExpectedDump.put(Key.TYPE.toString(), "freemarker.ext.dump.DumpDirectiveTest$Employee");

            supervisorExpectedDump.put(Key.VALUE.toString(), getJaneSmithExpectedDump(exposureLevel));               
            propertiesExpectedDump.put("supervisor", supervisorExpectedDump);    
            
            Map<String, Object> favoriteColorsExpectedDump = new HashMap<String, Object>(); 
            favoriteColorsExpectedDump.put(Key.TYPE.toString(), Type.SEQUENCE);
            List<Map<String, Object>> favoriteColorListExpectedDump = new ArrayList<Map<String, Object>>();
            Map<String, Object> color1ExpectedDump = new HashMap<String, Object>();
            color1ExpectedDump.put(Key.TYPE.toString(), Type.STRING);
            color1ExpectedDump.put(Key.VALUE.toString(), "blue");
            favoriteColorListExpectedDump.add(color1ExpectedDump);
            Map<String, Object> color2ExpectedDump = new HashMap<String, Object>();
            color2ExpectedDump.put(Key.TYPE.toString(), Type.STRING); 
            color2ExpectedDump.put(Key.VALUE.toString(), "green"); 
            favoriteColorListExpectedDump.add(color2ExpectedDump);
            favoriteColorsExpectedDump.put(Key.VALUE.toString(), favoriteColorListExpectedDump);        
            propertiesExpectedDump.put("favoriteColors", favoriteColorsExpectedDump);
        }        
        
        expectedDump.put(Key.PROPERTIES.toString(), propertiesExpectedDump);
        
        // Methods       
        expectedDump.put(Key.METHODS.toString(), getEmployeeMethodsExpectedDump(exposureLevel)); 
        
        return expectedDump;
    }
    
    private List<String> getEmployeeMethodsExpectedDump(int exposureLevel) {
        
        List<String> expectedDump = new ArrayList<String>();
        if (exposureLevel == BeansWrapper.EXPOSE_SAFE || exposureLevel == BeansWrapper.EXPOSE_ALL) {
            expectedDump.add("getEmployeeCount");
            expectedDump.add("getName(String)");
            expectedDump.add("setFavoriteColors(Strings)");
            expectedDump.add("setNickname(String)");
        }   
        Collections.sort(expectedDump);
        return expectedDump;
    }
    
    private Map<String, Object> getJaneSmithExpectedDump(int exposureLevel) {

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        
        SortedMap<String, Object> propertiesExpectedDump = new TreeMap<String, Object>();
        
        // Properties 
        if (exposureLevel != BeansWrapper.EXPOSE_NOTHING) {
            
//          Map<String, Object> birthdateExpectedDump = new HashMap<String, Object>();
//          birthdateExpectedDump.put(Key.TYPE.toString(), Type.DATE);
//          birthdateExpectedDump.put(Key.DATE_TYPE.toString(), DateType.UNKNOWN);
//          Calendar c = Calendar.getInstance();
//          c.set(75, Calendar.MAY, 5);
//          birthdateExpectedDump.put(Key.VALUE.toString(), c.getTime());
//          propertiesExpectedDump.put("birthdate", birthdateExpectedDump);

            Map<String, Object> fullNameExpectedDump = new HashMap<String, Object>();
            fullNameExpectedDump.put(Key.TYPE.toString(), Type.STRING);
            fullNameExpectedDump.put(Key.VALUE.toString(), "Jane Smith");
            propertiesExpectedDump.put("fullName", fullNameExpectedDump);
        
            Map<String, Object> idExpectedDump = new HashMap<String, Object>();
            idExpectedDump.put(Key.TYPE.toString(), Type.NUMBER);
            idExpectedDump.put(Key.VALUE.toString(), 78234);
            propertiesExpectedDump.put("id", idExpectedDump);
        
            Map<String, Object> nicknameExpectedDump = new HashMap<String, Object>();
            nicknameExpectedDump.put(Key.TYPE.toString(), Type.STRING);
            nicknameExpectedDump.put(Key.VALUE.toString(), "");
            propertiesExpectedDump.put("nickname", nicknameExpectedDump);
        
            Map<String, Object> middleNameExpectedDump = new HashMap<String, Object>();
            middleNameExpectedDump.put(Key.VALUE.toString(), "null");
            propertiesExpectedDump.put("middleName", middleNameExpectedDump);
              
            Map<String, Object> marriedExpectedDump = new HashMap<String, Object>();
            marriedExpectedDump.put(Key.TYPE.toString(), Type.BOOLEAN);
            marriedExpectedDump.put(Key.VALUE.toString(), true);
            propertiesExpectedDump.put("married", marriedExpectedDump);      
        
            Map<String, Object> supervisorExpectedDump = new HashMap<String, Object>();
            supervisorExpectedDump.put(Key.VALUE.toString(), "null");
            propertiesExpectedDump.put("supervisor", supervisorExpectedDump);             
    
            Map<String, Object> favoriteColorsExpectedDump = new HashMap<String, Object>(); 
            favoriteColorsExpectedDump.put(Key.TYPE.toString(), Type.SEQUENCE);
            List<Map<String, Object>> favoriteColorListExpectedDump = new ArrayList<Map<String, Object>>();
            Map<String, Object> color1ExpectedDump = new HashMap<String, Object>();
            color1ExpectedDump.put(Key.TYPE.toString(), Type.STRING);
            color1ExpectedDump.put(Key.VALUE.toString(), "red"); 
            favoriteColorListExpectedDump.add(color1ExpectedDump);
            Map<String, Object> color2ExpectedDump = new HashMap<String, Object>();
            color2ExpectedDump.put(Key.TYPE.toString(), Type.STRING); 
            color2ExpectedDump.put(Key.VALUE.toString(), "orange"); 
            favoriteColorListExpectedDump.add(color2ExpectedDump);
            favoriteColorsExpectedDump.put(Key.VALUE.toString(), favoriteColorListExpectedDump);        
            propertiesExpectedDump.put("favoriteColors", favoriteColorsExpectedDump);
        }
        expectedDump.put(Key.PROPERTIES.toString(), propertiesExpectedDump);
        
        // Methods
        expectedDump.put(Key.METHODS.toString(), getEmployeeMethodsExpectedDump(exposureLevel)); 
        
        return expectedDump;
    }
    
}
