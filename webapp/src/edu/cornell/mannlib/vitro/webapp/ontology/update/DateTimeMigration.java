/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.ontology.update;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
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
	private ChangeLogger logger;  
	private ChangeRecord record;
	
	private static final String dateTimeURI = "http://vivoweb.org/ontology/core#dateTime";
	private static final String dateTimePrecisionURI = "http://vivoweb.org/ontology/core#dateTimePrecision";
	private static final String hasTimeIntervalURI = "http://vivoweb.org/ontology/core#hasTimeInterval";
	private static final String dateTimeIntervalURI = "http://vivoweb.org/ontology/core#dateTimeInterval";
	private static final String startURI = "http://vivoweb.org/ontology/core#start";
	private static final String endURI = "http://vivoweb.org/ontology/core#end";
	
	private static final String yPrecisionURI = "http://vivoweb.org/ontology/core#yearPrecision";
	private static final String ymPrecisionURI = "http://vivoweb.org/ontology/core#yearMonthPrecision";
	private static final String ymdPrecisionURI = "http://vivoweb.org/ontology/core#yearMonthDayPrecision";
	private static final String ymdtPrecisionURI = "http://vivoweb.org/ontology/core#yearMonthDayTimePrecision";
	
	private DatatypeProperty dateTimeProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createDatatypeProperty(dateTimeURI);
	private ObjectProperty hasTimeIntervalProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(hasTimeIntervalURI);
	private ObjectProperty dateTimeIntervalProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(dateTimeIntervalURI);
	private ObjectProperty dateTimePrecisionProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(dateTimePrecisionURI);
	private ObjectProperty startProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(startURI);
	private ObjectProperty endProp = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)).createObjectProperty(endURI);
	
	/**
	 * Constructor 
	 *  
	 * @param   aboxModel    - the knowledge base to be updated
	 * @param   logger       - for writing to the change log
	 *                         and the error log.
	 * @param   record       - for writing to the additions model 
	 *                         and the retractions model.                  
	 */
	public DateTimeMigration(OntModel aboxModel,ChangeLogger logger, ChangeRecord record) {
		
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
	       
		    int datelessCount = 0;
		    while (iter.hasNext()) {

			   Statement stmt1 = iter.next();
			   
			   if (!stmt1.getObject().isResource()) {
				   logger.log("WARN: the object of this statement is expected to be a resource: " + ABoxUpdater.stmtString(stmt1));
				   continue;
			   }

			   Statement stmt2 = aboxModel.getProperty(stmt1.getObject().asResource(), dateTimeIntervalProp);

			   if (stmt2 == null) {
				   datelessCount++;
				   additions.add(stmt1.getSubject(), dateTimeIntervalProp, stmt1.getObject());
				   //additions.add(stmt1.getObject().asResource(), dateTimeIntervalForProp, stmt1.getSubject());
				   continue;
			   }
			   
			   if (!stmt2.getObject().isResource()) {
				   logger.log("WARN: the object of this statement is expected to be a resource: " + ABoxUpdater.stmtString(stmt2));
				   continue;
			   }
			   
			   retractions.add(stmt2);
			   //retractions.add(stmt2.getObject().asResource(), dateTimeIntervalForProp, stmt2.getSubject());
			   additions.add(stmt1.getSubject(), dateTimeIntervalProp, stmt1.getObject());
			   //additions.add(stmt1.getObject().asResource(), dateTimeIntervalForProp, stmt1.getSubject());
			   
			   StmtIterator iter2 = aboxModel.listStatements(stmt2.getObject().asResource(), (Property) null, (RDFNode) null);
			   
			   while (iter2.hasNext()) {
				   Statement stmt3 = iter2.next();
				   retractions.add(stmt3);
				   
				   if ( (stmt3.getPredicate().equals(startProp)) || (stmt3.getPredicate().equals(endProp)) ) {
					   additions.add(stmt1.getObject().asResource(), stmt3.getPredicate(), stmt3.getObject());
				   }
			   }  
		    }
		   
		    aboxModel.remove(retractions);
		    record.recordRetractions(retractions);
		    aboxModel.add(additions);
		    record.recordAdditions(additions);
		    
			if (datelessCount > 0) {	
			    logger.log("INFO: Found " + datelessCount + " Academic Term and/or Year individual" + ((datelessCount > 1) ? "s" : "") +
			    		" that " + ((datelessCount > 1) ? "don't have " : "doesn't have ") + "an associated date." + 
			    		 " Such an individual will be displayed as an incomplete Date/Time" +
			    	     " interval on any Course page that refers to it.");
			}
		    
			if (additions.size() > 0) {	
			   long count = additions.size() / 2;	
			   logger.log("Updated " + count + " Academic interval" + ((count > 1) ? "s" : "") + " to the new date/time format");
			}
		} finally {
			aboxModel.leaveCriticalSection();
		}	
	}
		
	/**
	 * 
	 *  
	 */
	public void updateLiterals() throws IOException {

		DateFormat yearFormat = new SimpleDateFormat("yyyy");
		DateFormat yearMonthFormat = new SimpleDateFormat("yyyy-MM");
		DateFormat yearMonthDayFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat yearMonthDayFormat2 = new SimpleDateFormat("dd-MMM-yy");
	 	
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
		               newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), getDateTimeLiteral(date) );
		    	   } catch (ParseException pe) {
		    		   logger.log("Parse Exception for year literal: " + stmt.getObject().asLiteral().getLexicalForm() + 
		    				   ". The following statement has been removed from the knowledge base " + ABoxUpdater.stmtString(stmt));
		    	   }		    	   		    	   
		       } else  if (ymPrecisionURI.equals(precision)) {
		    	   try {
		               date = yearMonthFormat.parse(stmt.getObject().asLiteral().getLexicalForm());
		               newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), getDateTimeLiteral(date) );
		    	   } catch (ParseException pe) {
		    		   logger.log("Parse Exception for yearMonth literal: " + stmt.getObject().asLiteral().getLexicalForm() + 
		    				   ". The following statement has been removed from the knowledge base " + ABoxUpdater.stmtString(stmt));
		    	   }
		       } else  if (ymdPrecisionURI.equals(precision)) {
		    	   try {
		               date = yearMonthDayFormat.parse(stmt.getObject().asLiteral().getLexicalForm());
		               newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), getDateTimeLiteral(date) );
		    	   } catch (ParseException pe) {		   
		    		   try {
		                   date = yearMonthDayFormat2.parse(stmt.getObject().asLiteral().getLexicalForm());
		                   newStmt = ResourceFactory.createStatement(stmt.getSubject(), stmt.getPredicate(), getDateTimeLiteral(date) );  		   
		    		   } catch (ParseException pe2) {
			    		   logger.log("Parse Exception for yearMonthDay literal: " + stmt.getObject().asLiteral().getLexicalForm() + 
			    				   ". The following statement has been removed from the knowledge base " + ABoxUpdater.stmtString(stmt));		    			   
		    		   }
		    	   }	   
		       } else  if (ymdtPrecisionURI.equals(precision)) {
		    	   logger.log("WARNING: unhandled precision found for individual " + stmt.getSubject().getURI() + ": " + precision +
		    			      ". The following statement has been removed from the knowledge base " + ABoxUpdater.stmtString(stmt));
		       } else {
		    	   logger.log("WARNING: unrecognized precision found for individual " + stmt.getSubject().getURI() + ": " + precision +
		    			      ". The following statement has been removed from the knowledge base " + ABoxUpdater.stmtString(stmt));
		       }

			   retractions.add(stmt);
			     
		       if (newStmt != null ) {
			     additions.add(newStmt);
		       }		   
		    }
		   
		    aboxModel.remove(retractions);
		    record.recordRetractions(retractions);
		    aboxModel.add(additions);
		    record.recordAdditions(additions);
		    
			if (additions.size() > 0) {
					logger.log("Updated " + additions.size() + " date/time literal" + 
							((additions.size() > 1) ? "s" : "") + " to the xsd:dateTime representation");
			}		   
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

	    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    cal.setTime(date);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND,0);
	    XSDDateTime dt = new XSDDateTime(cal);
	    String dateString = dt.toString().substring(0, dt.toString().length()-1);
	    return ResourceFactory.createTypedLiteral(dateString, XSDDatatype.XSDdateTime);
    }
}