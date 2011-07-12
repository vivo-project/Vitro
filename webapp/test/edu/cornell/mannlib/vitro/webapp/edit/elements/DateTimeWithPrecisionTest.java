/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.Field;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;


@RunWith(value= Parameterized.class)
public class DateTimeWithPrecisionTest {

    TimeZone orginalTimeZone;
    TimeZone testZone;
    
    public DateTimeWithPrecisionTest(TimeZone tz){
        orginalTimeZone = TimeZone.getDefault();
        testZone = tz;
    }

    @Parameters
    public static Collection<Object[]> data(){
        String allZones[] = TimeZone.getAvailableIDs();
        ArrayList<Object[]> data = new ArrayList<Object[]>( allZones.length );
        for( String zoneId : allZones ){
            Object v[] = new Object[1];
            v[0] = TimeZone.getTimeZone(zoneId);
            try{
                DateTimeZone dtz = DateTimeZone.forID(zoneId);
                if( dtz != null ){
                    data.add(v);    
                }
            }catch(IllegalArgumentException ex){
                //cannot convert to joda datetimezone.
            }            
        }
        return data;
    }

    @Before
    public void beforeTest(){
        TimeZone.setDefault( testZone );
    }
    
    @After
    public void after(){
        if( orginalTimeZone != null){
            TimeZone.setDefault(orginalTimeZone);
        }
    }
    
    @Test 
    public void fieldNameTemplateVariableTest() throws Exception{
        String FIELDNAME = "testfield";       
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        EditSubmission editSub = null;
        EditConfiguration editConfig = new EditConfiguration();
        editConfig.setUrisInScope(Collections.EMPTY_MAP);
        editConfig.setLiteralsInScope(Collections.EMPTY_MAP);
        
        Map templateVars = dtwp.getMapForTemplate(editConfig, editSub);
        Assert.assertNotNull(templateVars);
        
        Assert.assertTrue( templateVars.containsKey("fieldName") );
        Assert.assertEquals(templateVars.get("fieldName"), "testfield");
    }
    
    @Test
    public void precisionSecondsValidationTest() throws Exception{
        String FIELDNAME = "testfield";       
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"12"});
        queryParameters.put(FIELDNAME+"-day", new String[]{"01"});
        queryParameters.put(FIELDNAME+"-hour", new String[]{"12"});
        queryParameters.put(FIELDNAME+"-minute", new String[]{"00"});
        queryParameters.put(FIELDNAME+"-second", new String[]{"00"});                        
        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = null;        
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);
        
        Assert.assertNotNull(precisionURI);        
        Assert.assertEquals(VitroVocabulary.Precision.SECOND.uri(), precisionURI);
    }
    
    @Test
    public void precisionMinutesValidationTest() throws Exception{
        String FIELDNAME = "testfield";       
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"12"});
        queryParameters.put(FIELDNAME+"-day", new String[]{"01"});
        queryParameters.put(FIELDNAME+"-hour", new String[]{"12"});
        queryParameters.put(FIELDNAME+"-minute", new String[]{"00"});
        //no seconds
        
        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = null;        
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);
        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.MINUTE.uri(), precisionURI);        
    }
    
    @Test
    public void precisionHoursValidationTest() throws Exception{
        String FIELDNAME = "testfield";        
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"12"});
        queryParameters.put(FIELDNAME+"-day", new String[]{"01"});
        queryParameters.put(FIELDNAME+"-hour", new String[]{"12"});
        //no minutes
        //no seconds
        
        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = null;
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);
        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.HOUR.uri(), precisionURI);               
    }
    
    @Test
    public void precisionDaysValidationTest()  throws Exception{        
        String FIELDNAME = "testfield";        
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"12"});
        queryParameters.put(FIELDNAME+"-day", new String[]{"01"});
        //no hours
        //no minutes
        //no seconds
        
        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = null;        
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);
        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);               
    }
    
    @Test
    public void precisionMonthsValidationTest()throws Exception{
        String FIELDNAME = "testfield";        
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"12"});
        //no days
        //no hours
        //no minutes
        //no seconds 
        
        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = null;
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);
        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.MONTH.uri(), precisionURI);       
    }
    
    @Test
    public void precisionYearValidationTest() throws Exception{
        String FIELDNAME = "testfield";       
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        //no months
        //no days
        //no hours
        //no minutes
        //no seconds

        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = null;
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);
        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.YEAR.uri(), precisionURI);        
    }
    
    @Test
    public void precisionNoValueTest() throws Exception{
        String FIELDNAME = "testfield";       
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();
        //field is not filled out at all
        //no year
        //no months
        //no days
        //no hours
        //no minutes
        //no seconds

        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = null;
        
        precisionURI = dtwp.getSubmittedPrecision( queryParameters );
        
        Assert.assertNotNull(precisionURI);        
        Assert.assertEquals(dtwp.BLANK_SENTINEL, precisionURI);    
        
        Literal date = dtwp.getDateTime( queryParameters);        
        Assert.assertNull(date);                  
    }
    
    @Test
    public void getDateLiteralTest(){
        String FIELDNAME = "testfield";       
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        //no months
        //no days
        //no hours
        //no minutes
        //no seconds

        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
                
        Literal date = dtwp.getDateTime( queryParameters);        
        Assert.assertNotNull(date);        
        Assert.assertEquals( XSDDatatype.XSDdateTime.getURI() ,date.getDatatypeURI() );
        
        Object obj = date.getValue();
        Assert.assertNotNull(obj);
        Assert.assertEquals(XSDDateTime.class, obj.getClass());
        
        DateTime result = new DateTime( date.getLexicalForm());
        DateTime expected = new DateTime(1999,1,1,0,0,0,0 );
        Assert.assertEquals(expected.toInstant() , result.toInstant());
        
        Assert.assertEquals("1999-01-01T00:00:00" , date.getLexicalForm() );                        
    }

    
    @Test
    public void day30Test()  throws Exception{        
        String FIELDNAME = "testfield";        
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        
        /* Check if it works with day number under 29 */
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"12"});
        queryParameters.put(FIELDNAME+"-day", new String[]{"28"});
                
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", (EditConfiguration)null, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = dtwp.getSubmittedPrecision( queryParameters);        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);               
        
        /* Check for days greater than 28 */
        queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"12"});
        queryParameters.put(FIELDNAME+"-day", new String[]{"30"});
                
        validationMsgs = dtwp.getValidationMessages("testfield", (EditConfiguration)null, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
                       
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);
        
        /* Check for leap year */
        queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"2000" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"2"});
        queryParameters.put(FIELDNAME+"-day", new String[]{"29"});
                
        validationMsgs = dtwp.getValidationMessages("testfield", (EditConfiguration)null, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
                       
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);
        
        /* check for non leap year */
        queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+"-year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+"-month", new String[]{"2"});
        queryParameters.put(FIELDNAME+"-day", new String[]{"29"});
                
        validationMsgs = dtwp.getValidationMessages("testfield", (EditConfiguration)null, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() > 0 );
                       
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);
    }
        

    @Test
    public void basicGetMapForTemplateTest()  throws Exception{          
        String FIELDNAME = "testfield";        
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        EditConfiguration config = new EditConfiguration();
        EditSubmission sub = null;
        
        Map<String,String> urisInScope = new HashMap<String,String>();
        urisInScope.put(dtwp.getPrecisionVariableName(),
                VitroVocabulary.Precision.MINUTE.uri());
        config.setUrisInScope(urisInScope);
                
        Map<String,Literal> literalsInScope = new HashMap<String,Literal>();
        literalsInScope.put(dtwp.getValueVariableName(),
                            ResourceFactory.createTypedLiteral("1999-02-15T10:00",XSDDatatype.XSDdateTime) );
        config.setLiteralsInScope(literalsInScope);        
        
        Map<String,Object> map = dtwp.getMapForTemplate(config,sub);
        Assert.assertEquals("year wrong", "1999", map.get("year"));
        Assert.assertEquals("month wrong", "2", map.get("month"));
        Assert.assertEquals("day wrong", "15", map.get("day"));
        Assert.assertEquals("hour wrong", "10", map.get("hour"));
        Assert.assertEquals("minute wrong", "0", map.get("minute"));  
        Assert.assertEquals("second wrong", "", map.get("second"));
        
        Assert.assertEquals("precision wrong", VitroVocabulary.Precision.MINUTE.uri(), map.get("existingPrecision"));
        
        Assert.assertEquals("fieldname wrong", FIELDNAME, map.get("fieldName"));                    
    }
    
}
