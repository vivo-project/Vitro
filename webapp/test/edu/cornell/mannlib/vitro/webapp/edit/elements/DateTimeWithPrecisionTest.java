/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.elements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.Field;


public class DateTimeWithPrecisionTest {

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
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+".month", new String[]{"12"});
        queryParameters.put(FIELDNAME+".day", new String[]{"01"});
        queryParameters.put(FIELDNAME+".hour", new String[]{"12"});
        queryParameters.put(FIELDNAME+".minute", new String[]{"00"});
        queryParameters.put(FIELDNAME+".second", new String[]{"00"});                        
        EditConfiguration editConfig=null;
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", editConfig, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = null;        
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);
        
        Assert.assertNotNull(precisionURI);
        //Assert.assertEquals(dtwp.PRECISIONS[6], precisionURI);        
        Assert.assertEquals(VitroVocabulary.Precision.SECOND.uri(), precisionURI);
    }
    
    @Test
    public void precisionMinutesValidationTest() throws Exception{
        String FIELDNAME = "testfield";       
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+".month", new String[]{"12"});
        queryParameters.put(FIELDNAME+".day", new String[]{"01"});
        queryParameters.put(FIELDNAME+".hour", new String[]{"12"});
        queryParameters.put(FIELDNAME+".minute", new String[]{"00"});
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
    public void precisionHourssValidationTest() throws Exception{
        String FIELDNAME = "testfield";        
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+".month", new String[]{"12"});
        queryParameters.put(FIELDNAME+".day", new String[]{"01"});
        queryParameters.put(FIELDNAME+".hour", new String[]{"12"});
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
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+".month", new String[]{"12"});
        queryParameters.put(FIELDNAME+".day", new String[]{"01"});
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
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+".month", new String[]{"12"});
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
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
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
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
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
        
        DateTime result = new DateTime( date.getLexicalForm() );
        DateTime expected = new DateTime(1999,1,1,0,0,0,0);
        Assert.assertEquals(expected, result);
        
        Object obj = date.getValue();
        Assert.assertNotNull(obj);
        Assert.assertEquals(XSDDateTime.class, obj.getClass());        
    }

    
    @Test
    public void day30Test()  throws Exception{        
        String FIELDNAME = "testfield";        
        Field field = new Field();
        field.setName(FIELDNAME);
        DateTimeWithPrecision dtwp = new DateTimeWithPrecision(field);
        
        
        /* Check if it works with day number under 29 */
        Map<String,String[]> queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+".month", new String[]{"12"});
        queryParameters.put(FIELDNAME+".day", new String[]{"28"});
                
        Map<String,String> validationMsgs = dtwp.getValidationMessages("testfield", (EditConfiguration)null, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
        
        String precisionURI = dtwp.getSubmittedPrecision( queryParameters);        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);               
        
        /* Check for days greater than 28 */
        queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+".month", new String[]{"12"});
        queryParameters.put(FIELDNAME+".day", new String[]{"30"});
                
        validationMsgs = dtwp.getValidationMessages("testfield", (EditConfiguration)null, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
                       
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);
        
        /* Check for leap year */
        queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+".year", new String[]{"2000" });
        queryParameters.put(FIELDNAME+".month", new String[]{"2"});
        queryParameters.put(FIELDNAME+".day", new String[]{"29"});
                
        validationMsgs = dtwp.getValidationMessages("testfield", (EditConfiguration)null, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() == 0 );
                       
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);
        
        /* check for non leap year */
        queryParameters = new HashMap<String, String[]>();                        
        queryParameters.put(FIELDNAME+".year", new String[]{"1999" });
        queryParameters.put(FIELDNAME+".month", new String[]{"2"});
        queryParameters.put(FIELDNAME+".day", new String[]{"29"});
                
        validationMsgs = dtwp.getValidationMessages("testfield", (EditConfiguration)null, queryParameters);        
        Assert.assertNotNull(validationMsgs);
        Assert.assertTrue(validationMsgs.size() > 0 );
                       
        precisionURI = dtwp.getSubmittedPrecision( queryParameters);        
        Assert.assertNotNull(precisionURI);
        Assert.assertEquals(VitroVocabulary.Precision.DAY.uri(), precisionURI);
    }
}
