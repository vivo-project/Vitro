/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;

/**  
* Performs knowledge base updates to the abox to align with the new
* 1.2 Date/Time representation.
*   
*/ 
public class DateTimeMigration {

	private OntModel aboxModel;
	private OntologyChangeLogger logger;  
	private OntologyChangeRecord record;
	
	private static final String dateTimeURI = "http://vivoweb.org/ontology/core#dateTime";
	private static final String dateTimePrecisionURI = "http://vivoweb.org/ontology/core#dateTimePrecision";
	private static final String hasTimeIntervalURI = "http://vivoweb.org/ontology/core#hasTimeInterval";
	private static final String dateTimeIntervalURI = "http://vivoweb.org/ontology/core#dateTimeInterval";
	private static final String dateTimeIntervalForURI = "http://vivoweb.org/ontology/core#dateTimeIntervalFor";
	
	private static final String yPrecisionURI = "http://vivoweb.org/ontology/core#yearPrecision";
	private static final String ymPrecisionURI = "http://vivoweb.org/ontology/core#yearMonthPrecision";
	private static final String ymdPrecisionURI = "http://vivoweb.org/ontology/core#yearMonthDayPrecision";
	private static final String ymdtPrecisionURI = "http://vivoweb.org/ontology/core#yearMonthDayTimePrecision";
	
	private DatatypeProperty dateTimeProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createDatatypeProperty(dateTimeURI);
	private ObjectProperty hasTimeIntervalProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(hasTimeIntervalURI);
	private ObjectProperty dateTimeIntervalProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(dateTimeIntervalURI);
	private ObjectProperty dateTimeIntervalForProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(dateTimeIntervalForURI);
	private ObjectProperty dateTimePrecisionProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(dateTimePrecisionURI);
	
	
	/**
	 * Constructor 
	 *  
	 * @param   aboxModel    - the knowledge base to be updated
	 * @param   logger       - for writing to the change log
	 *                         and the error log.
	 * @param   record       - for writing to the additions model 
	 *                         and the retractions model.                  
	 */
	public DateTimeMigration(OntModel aboxModel,OntologyChangeLogger logger, OntologyChangeRecord record) {
		
		this.aboxModel = aboxModel;
		this.logger = logger;
		this.record = record;
	}
	
	/**
	 * 
	 * Update the abox to align with changes in the Date/Time class
	 * and property definitions in the transition from version 1.1 to 1.2.
	 *  
	 */
	public void updateABox() throws IOException {
		
		updateAcademicIntervals();
		updateLiterals();
	}
	
	/**
	 * 
	 *  
	 */
	public void updateAcademicIntervals() throws IOException {

		aboxModel.enterCriticalSection(Lock.WRITE);
		
		try {			
	        Model additions = ModelFactory.createDefaultModel();
	        Model retractions = ModelFactory.createDefaultModel();

		    StmtIterator iter = aboxModel.listStatements((Resource) null, hasTimeIntervalProp, (RDFNode) null);
	       
		    while (iter.hasNext()) {

			   Statement stmt = iter.next();

			   Statement stmt2 = aboxModel.getProperty(stmt.getObject().asResource(), dateTimeIntervalProp);

			   if (stmt2 != null) {
				  retractions.add(stmt2);
				  retractions.add(stmt2.getObject().asResource(), dateTimeIntervalForProp, stmt2.getSubject());
				  additions.add(stmt.getSubject(), dateTimeIntervalProp, stmt2.getObject());
				  additions.add(stmt2.getObject().asResource(), dateTimeIntervalProp, stmt.getSubject());
			   }
				   
		    }
		   
		    aboxModel.remove(retractions);
		    record.recordRetractions(retractions);
		    aboxModel.add(additions);
		    record.recordAdditions(additions);
		   
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}
	/**
	 * 
	 *  
	 */
	public void updateLiterals() throws IOException {

		// note: not handling timezones - they are not expected to be in the 1.1.1 data
		DateFormat yearFormat = new SimpleDateFormat("yyyy");
		DateFormat yearMonthFormat = new SimpleDateFormat("yyyy-mm");
		DateFormat yearMonthDayFormat = new SimpleDateFormat("yyyy-mm-dd");
	 	
		aboxModel.enterCriticalSection(Lock.WRITE);
		
		try {			
	        Model additions = ModelFactory.createDefaultModel();
	        Model retractions = ModelFactory.createDefaultModel();

		    StmtIterator iter = aboxModel.listStatements((Resource) null, dateTimeProp, (RDFNode) null);
	       
		    while (iter.hasNext()) {
		       Statement newStmt = null;
		       Date date = null;
			   Statement stmt = iter.next();
			   String precision = getPrecision(stmt);

		       if (precision == null) {
		    	   logger.log("WARNING: no precision found for individual " + stmt.getSubject().getURI() );
		       } else  if (yPrecisionURI.equals(precision)) {
		    	   try {
		               date = yearFormat.parse(stmt.getObject().asLiteral().getLexicalForm());
		    	   } catch (ParseException pe) {
		    		   logger.log("Parse Exception for year literal: " + stmt.getObject().asLiteral().getLexicalForm() + ". skipping statement.");
		    	   }
		    	   
		    	   newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), getDateTimeLiteral(date) );
		    	   
		       } else  if (ymPrecisionURI.equals(precision)) {
		    	   try {
		               date = yearMonthFormat.parse(stmt.getObject().asLiteral().getLexicalForm());
		    	   } catch (ParseException pe) {
		    		   logger.log("Parse Exception for year literal: " + stmt.getObject().asLiteral().getLexicalForm() + ". skipping statement.");
		    	   }
		    	   
		    	   newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), getDateTimeLiteral(date) );
		       } else  if (ymdPrecisionURI.equals(precision)) {
		    	   try {
		               date = yearMonthDayFormat.parse(stmt.getObject().asLiteral().getLexicalForm());
		    	   } catch (ParseException pe) {
		    		   logger.log("Parse Exception for year literal: " + stmt.getObject().asLiteral().getLexicalForm() + ". skipping statement.");
		    	   }
		    	   
		    	   newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), getDateTimeLiteral(date) );
	   
		       } else  if (ymdtPrecisionURI.equals(precision)) {
		    	   logger.log("WARNING: unhandled precision found for individual " + stmt.getSubject().getURI() + ": " + precision );
		       } else {
		    	   logger.log("WARNING: unrecognized precision found for individual " + stmt.getSubject().getURI() + ": " + precision );
		       }
			   
		       if (newStmt != null ) {
			     additions.add(newStmt);
			     retractions.add(stmt);
		       }		   
		    }
		   
		    aboxModel.remove(retractions);
		    record.recordRetractions(retractions);
		    aboxModel.add(additions);
		    record.recordAdditions(additions);
		   
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}
	
	public String getPrecision(Statement stmt) {
		
		String precision = null;
		
		aboxModel.enterCriticalSection(Lock.WRITE);
		
		try {
			
	       
		   StmtIterator iter = aboxModel.listStatements(stmt.getSubject(), dateTimePrecisionProp, (RDFNode) null);
	       
		   while (iter.hasNext()) {
			   Statement statement = iter.next();
			   precision = ((Resource)statement.getObject()).getURI();
		   }
		   
		   return precision;
		   
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}	

     
    public static Literal getDateTimeLiteral(Date date) {

      // Note this loses time zone info, don't know how get parser to extract that
      //Calendar cal  = Calendar.getInstance( TimeZone.getTimeZone("GMT") );
    	
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      XSDDateTime dt = new XSDDateTime(cal);
      return ResourceFactory.createTypedLiteral(dt);
        
    }
}