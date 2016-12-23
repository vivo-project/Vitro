/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reasoner.plugin;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import edu.cornell.mannlib.vitro.webapp.reasoner.ReasonerPlugin;
import edu.cornell.mannlib.vitro.webapp.reasoner.SimpleReasoner;

/**
 * handles rules of the form
 * assertedProp1(?x, ?y) ^ assertedProp2(?y, ?z) -&gt; inferredProp(?x, ?z)
 * 
 */
public abstract class SimpleBridgingRule implements ReasonerPlugin {
	
    private static final Log log = LogFactory.getLog(SimpleBridgingRule.class);
    
	private Property assertedProp1;
	private Property assertedProp2;
	private Property inferredProp;
	
	private String   queryStr;
	private String   retractionTestString;
	
	private SimpleReasoner simpleReasoner;
	
	protected SimpleBridgingRule(String assertedProp1, String assertedProp2, String inferredProp) {
		this.assertedProp1 = ResourceFactory.createProperty(assertedProp1);
        this.assertedProp2 = ResourceFactory.createProperty(assertedProp2);
        this.inferredProp = ResourceFactory.createProperty(inferredProp);
        
        this.queryStr = "CONSTRUCT { \n" +
                        "  ?x <" + inferredProp + "> ?z \n" +
                        "} WHERE { \n" +
                        "  ?x <" + assertedProp1 + "> ?y . \n" +
                        "  ?y <" + assertedProp2 + "> ?z \n" +
                        "}";
        
        this.retractionTestString = 
                "  ASK { \n" +
                "  ?x <" + assertedProp1 + "> ?y . \n" +
                "  ?y <" + assertedProp2 + "> ?z \n" +                                 
                "  } ";
        
	}
	
	public boolean isConfigurationOnlyPlugin() {
	    return false;
	}
	
	public boolean isInterestedInAddedStatement(Statement stmt) {
		return isRelevantPredicate(stmt);
	}
	
	public boolean isInterestedInRemovedStatement(Statement stmt) {
		return isRelevantPredicate(stmt);
	}
	
	public void addedABoxStatement(Statement stmt, 
            Model aboxAssertionsModel, 
            Model aboxInferencesModel, 
            OntModel TBoxInferencesModel) {
		if (!isInterestedInAddedStatement(stmt) || ignore(stmt)) {
			return;
		}
        Model inf = constructInferences(this.queryStr, stmt, aboxAssertionsModel);
        StmtIterator sit = inf.listStatements();
        while(sit.hasNext()) {
        	Statement s = sit.nextStatement();
        	if (simpleReasoner != null) simpleReasoner.addInference(s,aboxInferencesModel);
        }     
	}
	
	private boolean ignore(Statement stmt) {
		return (
				(stmt.getSubject().isAnon() || stmt.getObject().isAnon())
			    // can't deal with blank nodes
		||
		        (!stmt.getObject().isResource()) 
			    // don't deal with literal values
	    );
	}

	private Query createQuery(String queryString, Statement stmt, Statement statement2) {
       String queryStr = new String(queryString);
        if (stmt.getPredicate().equals(assertedProp1)) {
            queryStr = queryStr.replace(
                    "?x", "<" + stmt.getSubject().getURI() + ">");
            queryStr = queryStr.replace(
                    "?y", "<" + ((Resource) stmt.getObject()).getURI() + ">");
        } else if (stmt.getPredicate().equals(assertedProp2)) {
            queryStr = queryStr.replace(
                    "?y", "<" + stmt.getSubject().getURI() + ">");
            queryStr = queryStr.replace(
                    "?z", "<" + ((Resource) stmt.getObject()).getURI() + ">");          
        } else {
            // should never be here
            return null;
        }
        if (statement2 != null) {
            queryStr = queryStr.replace(
                    "?x", "<" + stmt.getSubject().getURI() + ">");
            queryStr = queryStr.replace(
                    "?z", "<" + ((Resource) stmt.getObject()).getURI() + ">");  
        }
        
        if (log.isDebugEnabled()) {
            log.debug(queryStr);
        }
        Query query = null;
        try {
            query = QueryFactory.create(queryStr);
        } catch (QueryParseException e) {
            log.error("Unable to parse query for SimpleBridgingRule. \n" +
                      "This may mean that one of the following URIs is malformed: \n" +
                      stmt.getSubject() + "\n" + stmt.getObject() + "\n"
                     );
            log.error(e, e);
            throw(e);
        }
        return query;
	}
	
	private Model constructInferences(String queryString, Statement stmt, Model aboxAssertionsModel) {
	    Query query = createQuery(queryString, stmt, null);
		QueryExecution qe = QueryExecutionFactory.create(query, aboxAssertionsModel);
		try {
			return qe.execConstruct();
		} finally {
			qe.close();
		}
		
	}
	
    public void removedABoxStatement(Statement stmt, 
            Model aboxAssertionsModel, 
            Model aboxInferencesModel, 
            OntModel TBoxInferencesModel) {

		if (!isInterestedInRemovedStatement(stmt) || ignore(stmt)) {
			return;
		}
        
        // I initially tried constructing the statements to remove with a single
        // SPARQL CONSTRUCT statement, but that didn't seem to perform very well.
        // So this first retrieves a list of candidate ?x <inferredProp> ?z
        // statements, and then runs an ASK query to determine if there are still
        // statements ?x <assertedProp1> ?y and ?y <assertedProp2> ?z that entail
        // the statement in question.  If not, the statement is removed.
        
        // find-based candidate identification        
        Resource x = null;
        RDFNode z = null;
        if (stmt.getPredicate().equals(assertedProp1)) {
            x = stmt.getSubject();
        } else if (stmt.getPredicate().equals(assertedProp2)) {
            z = stmt.getObject();
        }
        Iterator<Statement> sit = aboxInferencesModel.listStatements(x, this.inferredProp, z).toList().iterator();
        
        while(sit.hasNext()) {
        	Statement s = sit.next();
        	Query ask = createQuery(this.retractionTestString, stmt, s);
        	QueryExecution qe = QueryExecutionFactory.create(ask, aboxAssertionsModel);
        	try {
            	if (!qe.execAsk()) {
                    if (log.isDebugEnabled()) {
                        log.debug("==> removing " + s);
                    }
                    if (simpleReasoner != null) simpleReasoner.removeInference(s,aboxInferencesModel);            	    
            	}
        	} finally {
        	    qe.close();
        	}
        }     
    }
	
    private boolean isRelevantPredicate(Statement stmt) {
		return (assertedProp1.equals(stmt.getPredicate())
				|| assertedProp2.equals(stmt.getPredicate()));
    }
    
    public void setSimpleReasoner(SimpleReasoner simpleReasoner) {
    	this.simpleReasoner = simpleReasoner;
    }

    public SimpleReasoner getSimpleReasoner() {
    	return this.simpleReasoner;
    }
}

