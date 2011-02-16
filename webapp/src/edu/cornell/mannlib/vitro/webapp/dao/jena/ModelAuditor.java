/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelChangedListener;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.EditEvent;

public class ModelAuditor implements ModelChangedListener {

    protected static final Log log = LogFactory.getLog(ModelAuditor.class.getName());

    private Resource editEvent;
    private OntModel auditModel;
    private OntModel auditedModel;

    private Resource STATEMENT_EVENT = ResourceFactory.createResource(VitroVocabulary.STATEMENT_EVENT);
    private Resource STATEMENT_ADDITION_EVENT = ResourceFactory.createResource(VitroVocabulary.STATEMENT_ADDITION_EVENT);
    private Resource STATEMENT_REMOVAL_EVENT = ResourceFactory.createResource(VitroVocabulary.STATEMENT_REMOVAL_EVENT);

    private Property STATEMENT_EVENT_STATEMENT = ResourceFactory.createProperty(VitroVocabulary.STATEMENT_EVENT_STATEMENT);
    private Property STATEMENT_EVENT_DATETIME = ResourceFactory.createProperty(VitroVocabulary.STATEMENT_EVENT_DATETIME);

    private Resource LOGIN_EVENT = ResourceFactory.createResource(VitroVocabulary.LOGIN_EVENT);
    private Property LOGIN_DATETIME = ResourceFactory.createProperty(VitroVocabulary.LOGIN_DATETIME);
    private Property LOGIN_AGENT  = ResourceFactory.createProperty(VitroVocabulary.LOGIN_AGENT);
    
    private Property PART_OF_EDIT_EVENT = ResourceFactory.createProperty(VitroVocabulary.PART_OF_EDIT_EVENT);

    private Resource EDIT_EVENT = ResourceFactory.createResource(VitroVocabulary.EDIT_EVENT);
    private Property EDIT_EVENT_AGENT = ResourceFactory.createProperty(VitroVocabulary.EDIT_EVENT_AGENT);
    private Property EDIT_EVENT_DATETIME = ResourceFactory.createProperty(VitroVocabulary.EDIT_EVENT_DATETIME);
	
	private DateFormat xsdDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	public ModelAuditor(OntModel auditModel, OntModel auditedModel) {
		this.auditModel = auditModel;
		this.auditedModel = auditedModel;
	}
	
	private Resource createStatementEvent(Statement stmt) {
		Resource statementEvent = auditModel.createIndividual(STATEMENT_EVENT);
		auditModel.add(statementEvent,STATEMENT_EVENT_DATETIME, xsdDateTimeFormat.format(Calendar.getInstance().getTime()), XSDDatatype.XSDdateTime);
		Resource reifiedStmt = auditModel.createReifiedStatement(stmt);
		auditModel.add(statementEvent,PART_OF_EDIT_EVENT, reifiedStmt);
		statementEvent.addProperty(STATEMENT_EVENT_STATEMENT,reifiedStmt);
		if (editEvent != null) {
			auditModel.add(statementEvent,PART_OF_EDIT_EVENT, editEvent);
		}
		return statementEvent;
	}
	
	private void doAddedStatement(Statement stmt) {
		Resource statementEvent = createStatementEvent(stmt);
		auditModel.add(statementEvent,RDF.type, STATEMENT_ADDITION_EVENT);
		//statementEvent.addProperty(RDF.type, STATEMENT_ADDITION_EVENT);
	}
	
	private void doRemovedStatement(Statement stmt) {
		Resource statementEvent = createStatementEvent(stmt);
		auditModel.add(statementEvent,RDF.type,STATEMENT_REMOVAL_EVENT);
        //statementEvent.addProperty(RDF.type, STATEMENT_REMOVAL_EVENT);
    }

    public void addedStatement(Statement arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            doAddedStatement(arg0);
        } finally {
            auditModel.leaveCriticalSection();
        }
    }

    public void addedStatements(Statement[] arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            for (int i=0; i<arg0.length; i++) {
                doAddedStatement(arg0[i]);
            }
        } finally {
            auditModel.leaveCriticalSection();
        }
    }

    public void addedStatements(List arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            Iterator sIt = arg0.iterator();
            while (sIt.hasNext()) {
                Statement stmt = (Statement) sIt.next();
                doAddedStatement(stmt);
            }
        } finally {
            auditModel.leaveCriticalSection();
        }
    }

    public void addedStatements(StmtIterator arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            while (arg0.hasNext()) {
                doAddedStatement((Statement)arg0.next());
            }
        } finally {
            auditModel.leaveCriticalSection();
        }
    }
    
    public void addedStatements(Model arg0) {
    	auditModel.enterCriticalSection(Lock.WRITE);
    	try {
            ClosableIterator stmtIt = arg0.listStatements();
            try {
                while (stmtIt.hasNext()) {
                    Statement stmt = (Statement) stmtIt.next();
                    doAddedStatement(stmt);
                }
            } finally {
                stmtIt.close();
            }
    	} finally {
    		auditModel.leaveCriticalSection();
    	}
    }

	public void notifyEvent(Model model, Object arg1) { 
		try {
			if (arg1 instanceof EditEvent) {
			EditEvent evt = (EditEvent) arg1;
				if (evt.getBegin()) {
					editEvent = auditModel.createIndividual(EDIT_EVENT);
					auditModel.add(editEvent,ResourceFactory.createProperty(VitroVocabulary.EDIT_EVENT_DATETIME),xsdDateTimeFormat.format(Calendar.getInstance().getTime()), XSDDatatype.XSDdateTime);
					//editEvent.addProperty(ResourceFactory.createProperty(VitroVocabulary.EDIT_EVENT_DATETIME),xsdDateTimeFormat.format(Calendar.getInstance().getTime()), XSDDatatype.XSDdateTime);
					for (Iterator<String> i = evt.getPropertyMap().keySet().iterator(); i.hasNext(); ) {
						String propURI = i.next();
						List<RDFNode> nodeList = evt.getPropertyMap().get(propURI);
						Property prop = ResourceFactory.createProperty(propURI);
						for (Iterator<RDFNode> j = nodeList.iterator(); j.hasNext(); ) {
							RDFNode node = j.next();
							
							
							if (node.isLiteral()) {
								auditModel.add(editEvent,prop,(Literal)node);
								//editEvent.addProperty(prop, (Literal)node);
							} else if (node.isResource()) {
								auditModel.add(editEvent,prop,(Resource)node);
								//editEvent.addProperty(prop, (Resource)node);
							}
							
						}
					}			
				} else {
					this.editEvent = null;
				}
			} else if( arg1 instanceof LoginEvent ){ 
			   LoginEvent login = (LoginEvent)arg1;
			   Resource loginEvent = auditModel.createIndividual(LOGIN_EVENT);	  
			   auditModel.add(loginEvent,
			           LOGIN_DATETIME, 
			           xsdDateTimeFormat.format(Calendar.getInstance().getTime()), 
			           XSDDatatype.XSDdateTime);
			   auditModel.add(loginEvent, 
			           LOGIN_AGENT, 
			           ResourceFactory.createResource(login.getLoginUri())); 
               
			}
		} catch (Exception e) {
			log.error(e, e);
		}
 	}

    public void removedStatement(Statement arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            doRemovedStatement(arg0);
        } finally {
            auditModel.leaveCriticalSection();
        }
    }

    public void removedStatements(Statement[] arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            for (int i=0; i<arg0.length; i++) {
                doRemovedStatement(arg0[i]);
            }
        } finally {
            auditModel.leaveCriticalSection();
        }
    }

    public void removedStatements(List arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            Iterator sIt = arg0.iterator();
            while (sIt.hasNext()) {
                Statement stmt = (Statement) sIt.next();
                doRemovedStatement(stmt);
            }
        } finally {
            auditModel.leaveCriticalSection();
        }
    }

    public void removedStatements(StmtIterator arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            while (arg0.hasNext()) {
                doRemovedStatement((Statement)arg0.next());
            }
        } finally {
            auditModel.leaveCriticalSection();
        }
    }

    public void removedStatements(Model arg0) {
        auditModel.enterCriticalSection(Lock.WRITE);
        try {
            ClosableIterator stmtIt = arg0.listStatements();
            try {
                while (stmtIt.hasNext()) {
                    Statement stmt = (Statement) stmtIt.next();
                    doRemovedStatement(stmt);
                }
            } finally {
                stmtIt.close();
            }
        } finally {
            auditModel.leaveCriticalSection();
        }
    }

}
