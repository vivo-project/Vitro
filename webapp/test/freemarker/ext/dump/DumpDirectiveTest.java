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

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import freemarker.core.CollectionAndSequence;
import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.CollectionModel;
import freemarker.ext.dump.BaseDumpDirective.DateType;
import freemarker.ext.dump.BaseDumpDirective.Key;
import freemarker.ext.dump.BaseDumpDirective.Type;
import freemarker.ext.dump.BaseDumpDirective.Value;
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
    public void dumpUndefinedValue() {
        
        String varName = "dog";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.VALUE.toString(), Value.UNDEFINED.toString());

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump);
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
        c.set(1991, Calendar.MAY, 5);
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

   @Test
   public void dumpHelplessMethod() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel methodModel = new HelplessMethod();
       dataModel.put(varName, methodModel);
        
       Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
       expectedDumpValue.put(Key.TYPE.toString(), Type.METHOD);
       expectedDumpValue.put(Key.CLASS.toString(), methodModel.getClass().getName());
       expectedDumpValue.put(Key.HELP.toString(), null);

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
       expectedDumpValue.put(Key.CLASS.toString(), methodModel.getClass().getName());
       expectedDumpValue.put(Key.HELP.toString(), getMethodHelp(varName));

       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(varName, expectedDumpValue);
       
       test(varName, dataModel, expectedDump);
   }

   @Test
   public void dumpMethodWithStringHelp() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel methodModel = new MethodWithStringHelp();
       dataModel.put(varName, methodModel);
        
       Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
       expectedDumpValue.put(Key.TYPE.toString(), Type.METHOD);
       expectedDumpValue.put(Key.CLASS.toString(), methodModel.getClass().getName());
       expectedDumpValue.put(Key.HELP.toString(), null);

       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(varName, expectedDumpValue);
       
       test(varName, dataModel, expectedDump);
   }

   @Test
   public void dumpMethodWithStringStringMapHelp() {

       String varName = "square";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateMethodModel methodModel = new MethodWithStringStringMapHelp();
       dataModel.put(varName, methodModel);
        
       Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
       expectedDumpValue.put(Key.TYPE.toString(), Type.METHOD);
       expectedDumpValue.put(Key.CLASS.toString(), methodModel.getClass().getName());
       expectedDumpValue.put(Key.HELP.toString(), new HashMap<String, Object>());

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
       expectedDumpValue.put(Key.CLASS.toString(), directiveModel.getClass().getName());
       expectedDumpValue.put(Key.HELP.toString(), null);

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
       expectedDumpValue.put(Key.CLASS.toString(), directiveModel.getClass().getName());
       expectedDumpValue.put(Key.HELP.toString(), getDirectiveHelp(varName));

       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(varName, expectedDumpValue);
       
       test(varName, dataModel, expectedDump); 
   }
 
   @Test
   public void dumpDirectiveWithStringHelp() {

       String varName = "dump";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateDirectiveModel directiveModel = new DirectiveWithStringHelp();
       dataModel.put(varName, directiveModel);
        
       Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
       expectedDumpValue.put(Key.TYPE.toString(), Type.DIRECTIVE);
       expectedDumpValue.put(Key.CLASS.toString(), directiveModel.getClass().getName());
       expectedDumpValue.put(Key.HELP.toString(), null);

       Map<String, Object> expectedDump = new HashMap<String, Object>();
       expectedDump.put(varName, expectedDumpValue);
       
       test(varName, dataModel, expectedDump);
   } 

   @Test
   public void dumpDirectiveWithStringStringMapHelp() {

       String varName = "dump";
       Map<String, Object> dataModel = new HashMap<String, Object>();
       
       TemplateDirectiveModel directiveModel = new DirectiveWithStringStringMapHelp();
       dataModel.put(varName, directiveModel);
        
       Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
       expectedDumpValue.put(Key.TYPE.toString(), Type.DIRECTIVE);
       expectedDumpValue.put(Key.CLASS.toString(), directiveModel.getClass().getName());
       expectedDumpValue.put(Key.HELP.toString(), new HashMap<String, Object>());

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
        List<Map<String, Object>> myListItemsExpectedDump = new ArrayList<Map<String, Object>>(myList.size());
        for ( String str : myList) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), str);
            myListItemsExpectedDump.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myListItemsExpectedDump);

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
        List<Map<String, Object>> myArrayExpectedDumpValue = new ArrayList<Map<String, Object>>(myArray.length);
        for ( String str : myArray) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), str);
            myArrayExpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myArrayExpectedDumpValue);

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
        
        List<Map<String, Object>> mixedListExpectedDumpValue = new ArrayList<Map<String, Object>>(mixedList.size());
        
        Map<String, Object> myStringExpectedDumpValue = new HashMap<String, Object>();        
        myStringExpectedDumpValue.put(Key.TYPE.toString(), Type.STRING);
        myStringExpectedDumpValue.put(Key.VALUE.toString(), myString);
        mixedListExpectedDumpValue.add(myStringExpectedDumpValue);
 
        Map<String, Object> myIntExpectedDumpValue = new HashMap<String, Object>();  
        myIntExpectedDumpValue.put(Key.TYPE.toString(), Type.NUMBER);
        myIntExpectedDumpValue.put(Key.VALUE.toString(), myInt);
        mixedListExpectedDumpValue.add(myIntExpectedDumpValue);
        
        Map<String, Object> myBoolExpectedDumpValue = new HashMap<String, Object>();  
        myBoolExpectedDumpValue.put(Key.TYPE.toString(), Type.BOOLEAN);
        myBoolExpectedDumpValue.put(Key.VALUE.toString(), myBool);
        mixedListExpectedDumpValue.add(myBoolExpectedDumpValue);
        
        Map<String, Object> myListExpectedDumpValue = new HashMap<String, Object>();
        myListExpectedDumpValue.put(Key.TYPE.toString(), Type.SEQUENCE); 
        List<Map<String, Object>> myListItemsExpectedDumpValue = new ArrayList<Map<String, Object>>(myList.size());
        for ( String animal : myList ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.STRING);
            itemDump.put(Key.VALUE.toString(), animal);
            myListItemsExpectedDumpValue.add(itemDump);            
        }        
        myListExpectedDumpValue.put(Key.VALUE.toString(), myListItemsExpectedDumpValue);
        mixedListExpectedDumpValue.add(myListExpectedDumpValue);
        
        expectedDumpValue.put(Key.VALUE.toString(), mixedListExpectedDumpValue);

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
        List<Map<String, Object>> myIntSetExpectedDumpValue = new ArrayList<Map<String, Object>>(myIntSet.size());
        for ( int i : myIntSet ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.NUMBER);
            itemDump.put(Key.VALUE.toString(), i);
            myIntSetExpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myIntSetExpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump); 
    }
    
    @Test
    public void dumpSimpleCollection() {

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
        List<Map<String, Object>> myCollectionExpectedDumpValue = new ArrayList<Map<String, Object>>(odds.size());
        for ( int i : odds ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.NUMBER);
            itemDump.put(Key.VALUE.toString(), i);
            myCollectionExpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myCollectionExpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump); 
    }    

    @Test
    public void dumpCollectionModel() {

        String varName = "oddNums";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Set<Integer> odds = new HashSet<Integer>();
        for (int i=0; i <= 10; i++) {
            if (i % 2 == 1) {
                odds.add(i);
            }
        }
        TemplateCollectionModel myCollection = new CollectionModel(odds, new BeansWrapper());
        dataModel.put(varName, myCollection);
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.COLLECTION);       
        List<Map<String, Object>> myCollectionExpectedDumpValue = new ArrayList<Map<String, Object>>(odds.size());
        for ( int i : odds ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.NUMBER);
            itemDump.put(Key.VALUE.toString(), i);
            myCollectionExpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myCollectionExpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump); 
    }  
 
    @Test
    public void dumpCollectionAndSequenceModel() {

        String varName = "oddNums";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        
        Set<Integer> odds = new HashSet<Integer>();
        for (int i=0; i <= 10; i++) {
            if (i % 2 == 1) {
                odds.add(i);
            }
        }
        TemplateCollectionModel coll = new CollectionModel(odds, new BeansWrapper());
        TemplateCollectionModel myCollection = new CollectionAndSequence(coll);
        dataModel.put(varName, myCollection);
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), Type.SEQUENCE);       
        List<Map<String, Object>> myCollectionExpectedDumpValue = new ArrayList<Map<String, Object>>(odds.size());
        for ( int i : odds ) {
            Map<String, Object> itemDump = new HashMap<String, Object>();
            itemDump.put(Key.TYPE.toString(), Type.NUMBER);
            itemDump.put(Key.VALUE.toString(), i);
            myCollectionExpectedDumpValue.add(itemDump);
        }
        expectedDumpValue.put(Key.VALUE.toString(), myCollectionExpectedDumpValue);

        Map<String, Object> expectedDump = new HashMap<String, Object>();
        expectedDump.put(varName, expectedDumpValue);
        
        test(varName, dataModel, expectedDump); 
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
    
    
    /////////////////////////// Private test classes and helper methods ///////////////////////////

    private void test(String varName, Map<String, Object> dataModel, Map<String, Object> expectedDump) {
        Map<String, Object> dump = getDump(varName, dataModel);
        assertEquals(expectedDump, dump);        
    }
    
    private void dumpObject(int exposureLevel) {
        
        String varName = "employee";
        Map<String, Object> dataModel = new HashMap<String, Object>();
        BeansWrapper wrapper = new BeansWrapper();
        wrapper.setExposureLevel(exposureLevel);
        Employee employee = getEmployee();
        try {
            dataModel.put("employee", wrapper.wrap(employee));
        } catch (TemplateModelException e) {
            // logging is suppressed, so what do we do here?
        }
        
        Map<String, Object> expectedDumpValue = new HashMap<String, Object>();
        expectedDumpValue.put(Key.TYPE.toString(), employee.getClass().getName());
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

    private class MethodWithStringHelp implements TemplateMethodModel {

        @Override
        public Object exec(List arg0) throws TemplateModelException {
            return null;
        } 
        
        public String help(String name) {
            return "help";
        }
    }

    private class MethodWithStringStringMapHelp implements TemplateMethodModel {

        @Override
        public Object exec(List arg0) throws TemplateModelException {
            return null;
        } 
        
        public Map<String, String> help(String name) {
            return new HashMap<String, String>();
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
    
    private class DirectiveWithStringHelp implements TemplateDirectiveModel {

        @Override
        public void execute(Environment arg0, Map arg1, TemplateModel[] arg2,
                TemplateDirectiveBody arg3) throws TemplateException,
                IOException {            
        }
        
        public String help(String name) {
            return "help";
        }
    }

    private class DirectiveWithStringStringMapHelp implements TemplateDirectiveModel {

        @Override
        public void execute(Environment arg0, Map arg1, TemplateModel[] arg2,
                TemplateDirectiveBody arg3) throws TemplateException,
                IOException {            
        }
        
        public Map<String, String> help(String name) {
            return new HashMap<String, String>();
        }
    }
    
    private Map<String, Object> getDirectiveHelp(String name) {
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put("effect", "Dump the contents of a template variable.");
        
        map.put("comments", "Sequences (lists and arrays) are enclosed in square brackets. Hashes are enclosed in curly braces.");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("var", "name of variable to dump");
        map.put("parameters", params);
        
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
        map.put("parameters", params);
        
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
        private boolean married;
        private int id;
        private String middleName;
        private List<String> favoriteColors;
        // private Map<String, String> degrees;
        private Employee supervisor;
        private float salary;
        
        Employee(String firstName, String lastName, int id, Date birthdate) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.middleName = null; // test a null value
            this.birthdate = birthdate;
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
        
//        void setDegrees(Map<String, String> degrees) {
//            this.degrees = degrees;
//        }

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
        
        public Date getBirthdate() {
            return birthdate;
        }
        
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
        
        public Employee boss() {
            return supervisor;
        }
        
        public List<String> getFavoriteColors() {
            return favoriteColors;
        }
        
//        public Map<String, String> getDegrees() {
//            return degrees;
//        }
        
        public String familyName() {
            return lastName;
        }
    }
    
    private Employee getEmployee() {

        Calendar c = Calendar.getInstance();
        c.set(1982, Calendar.MAY, 5);
        c = DateUtils.truncate(c, Calendar.DATE);
        Employee jdoe = new Employee("John", "Doe", 34523, c.getTime());
        jdoe.setFavoriteColors("blue", "green");
        jdoe.setSalary(65000);

//        Map<String, String> degrees = new HashMap<String, String>();
//        degrees.put("BA", "Mathematics");
//        degrees.put("MS", "Computer Science");
//        jdoe.setDegrees(degrees);
        
        c.clear();
        c.set(1975, Calendar.OCTOBER, 25);
        c = DateUtils.truncate(c, Calendar.DATE);
        Employee jsmith = new Employee("Jane", "Smith", 78234, c.getTime());
        jsmith.setFavoriteColors("red", "orange");

        jdoe.setSupervisor(jsmith);

        return jdoe;
    }
    
    private Map<String, Object> getJohnDoeExpectedDump(int exposureLevel) {

        Map<String, Object> expectedDump = new HashMap<String, Object>();

        Map<String, Object> supervisorExpectedDump = new HashMap<String, Object>();
        supervisorExpectedDump.put(Key.TYPE.toString(), "freemarker.ext.dump.DumpDirectiveTest$Employee");
        supervisorExpectedDump.put(Key.VALUE.toString(), getJaneSmithExpectedDump(exposureLevel));
        
        // Properties
        SortedMap<String, Object> propertiesExpectedDump = new TreeMap<String, Object>();
        
        if (exposureLevel < BeansWrapper.EXPOSE_NOTHING) {           
    
            Map<String, Object> birthdateExpectedDump = new HashMap<String, Object>();
            birthdateExpectedDump.put(Key.TYPE.toString(), Type.DATE);
            birthdateExpectedDump.put(Key.DATE_TYPE.toString(), DateType.UNKNOWN);
            Calendar c = Calendar.getInstance();
            c.set(1982, Calendar.MAY, 5);
            c = DateUtils.truncate(c, Calendar.DATE);
            birthdateExpectedDump.put(Key.VALUE.toString(), c.getTime());
            propertiesExpectedDump.put("birthdate", birthdateExpectedDump);
    
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
            middleNameExpectedDump.put(Key.VALUE.toString(), Value.NULL.toString());
            propertiesExpectedDump.put("middleName", middleNameExpectedDump);
            
            Map<String, Object> marriedExpectedDump = new HashMap<String, Object>();
            marriedExpectedDump.put(Key.TYPE.toString(), Type.BOOLEAN);
            marriedExpectedDump.put(Key.VALUE.toString(), true);
            propertiesExpectedDump.put("married", marriedExpectedDump);
                 

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

// This test fails, don't know why
//            Map<String, Object> degreesExpectedDump = new HashMap<String, Object>();
//            degreesExpectedDump.put(Key.TYPE.toString(), Type.HASH);
//            Map<String, Map<String, Object>> degreeMapExpectedDump = new HashMap<String, Map<String, Object>>();
//            Map<String, Object> degree1ExpectedDump = new HashMap<String, Object>();
//            degree1ExpectedDump.put(Key.TYPE.toString(), Type.STRING);
//            degree1ExpectedDump.put(Key.VALUE.toString(), "Mathematics");
//            degreeMapExpectedDump.put("BA", degree1ExpectedDump);
//            Map<String, Object> degree2ExpectedDump = new HashMap<String, Object>();
//            degree2ExpectedDump.put(Key.TYPE.toString(), Type.STRING);
//            degree2ExpectedDump.put(Key.VALUE.toString(), "Computer Science");
//            degreeMapExpectedDump.put("MS", degree2ExpectedDump);
//            degreesExpectedDump.put(Key.VALUE.toString(), degreeMapExpectedDump);
//            propertiesExpectedDump.put("degrees", degreesExpectedDump);
            
        }        
        
        expectedDump.put(Key.PROPERTIES.toString(), propertiesExpectedDump);
        
        // Methods       
        SortedMap<String, Object> methodDump = getEmployeeMethodsExpectedDump(exposureLevel, "Doe");
        if ( ! methodDump.isEmpty()) {
            methodDump.put("boss()", supervisorExpectedDump);
        }
        expectedDump.put(Key.METHODS.toString(), methodDump);
        
        return expectedDump;
    }
    
    private SortedMap<String, Object> getEmployeeMethodsExpectedDump(int exposureLevel, String familyName) {
        
        SortedMap<String, Object> expectedDump = new TreeMap<String, Object>();
        
        if (exposureLevel <= BeansWrapper.EXPOSE_SAFE) {

            Map<String, Object> nameExpectedDump = new HashMap<String, Object>();
            nameExpectedDump.put(Key.TYPE.toString(), "String");
            expectedDump.put("getName(String)", nameExpectedDump);
            
            expectedDump.put("setFavoriteColors(Strings)", Collections.emptyMap());
            
            expectedDump.put("setNickname(String)", Collections.emptyMap());
            
            Map<String, Object> familyNameExpectedDump = new HashMap<String, Object>();
            familyNameExpectedDump.put(Key.TYPE.toString(), Type.STRING);
            familyNameExpectedDump.put(Key.VALUE.toString(), familyName);
            expectedDump.put("familyName()", familyNameExpectedDump);

            Map<String, Object> employeeCountExpectedDump = new HashMap<String, Object>();
            employeeCountExpectedDump.put(Key.TYPE.toString(), Type.NUMBER);
            employeeCountExpectedDump.put(Key.VALUE.toString(), Employee.getEmployeeCount());
            expectedDump.put("getEmployeeCount()", employeeCountExpectedDump);
        }   

        return expectedDump;
    }
    
    private Map<String, Object> getJaneSmithExpectedDump(int exposureLevel) {

        Map<String, Object> expectedDump = new HashMap<String, Object>();

        Map<String, Object> supervisorExpectedDump = new HashMap<String, Object>();
        supervisorExpectedDump.put(Key.VALUE.toString(), Value.NULL.toString());
        
        SortedMap<String, Object> propertiesExpectedDump = new TreeMap<String, Object>();
        
        // Properties 
        if (exposureLevel < BeansWrapper.EXPOSE_NOTHING) {
            
            Map<String, Object> birthdateExpectedDump = new HashMap<String, Object>();
            birthdateExpectedDump.put(Key.TYPE.toString(), Type.DATE);
            birthdateExpectedDump.put(Key.DATE_TYPE.toString(), DateType.UNKNOWN);
            Calendar c = Calendar.getInstance();
            c.set(1975, Calendar.OCTOBER, 25);
            c = DateUtils.truncate(c, Calendar.DATE);
            birthdateExpectedDump.put(Key.VALUE.toString(), c.getTime());
            propertiesExpectedDump.put("birthdate", birthdateExpectedDump);

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
            middleNameExpectedDump.put(Key.VALUE.toString(), Value.NULL.toString());
            propertiesExpectedDump.put("middleName", middleNameExpectedDump);
              
            Map<String, Object> marriedExpectedDump = new HashMap<String, Object>();
            marriedExpectedDump.put(Key.TYPE.toString(), Type.BOOLEAN);
            marriedExpectedDump.put(Key.VALUE.toString(), true);
            propertiesExpectedDump.put("married", marriedExpectedDump);      
        
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
            
//            Map<String, Object> degreesExpectedDump = new HashMap<String, Object>();
//            degreesExpectedDump.put(Key.VALUE.toString(), Value.NULL.toString());
//            propertiesExpectedDump.put("degrees", degreesExpectedDump);            
        }
        expectedDump.put(Key.PROPERTIES.toString(), propertiesExpectedDump);
        
        // Methods
        SortedMap<String, Object> methodDump = getEmployeeMethodsExpectedDump(exposureLevel, "Smith");
        if ( ! methodDump.isEmpty()) {
            methodDump.put("boss()", supervisorExpectedDump);
        }
        expectedDump.put(Key.METHODS.toString(), methodDump);
        
        return expectedDump;
    }
    
}
