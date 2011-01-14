/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.jena;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dao.jena.VitroJenaModelMaker;

public class JenaIngestWorkflowProcessor {
    
    private static final Log log = LogFactory.getLog(JenaIngestWorkflowProcessor.class.getName());

	private Individual workflowInd;
	private ModelMaker vitroJenaModelMaker;
	private Map<String,Literal> varMap;
	private List<ActionHandler> actionHandlerList;
	private JenaIngestUtils utils;
	
	public JenaIngestWorkflowProcessor(Individual workflowInd, ModelMaker vitroJenaModelMaker) {
		this.varMap = new HashMap<String,Literal>();
		this.workflowInd = workflowInd;
		this.vitroJenaModelMaker = vitroJenaModelMaker;
		actionHandlerList = new LinkedList<ActionHandler>();
		actionHandlerList.add(new ClearModelAction());
		actionHandlerList.add(new AddModelsAction());
		actionHandlerList.add(new SubtractModelsAction());
		actionHandlerList.add(new ExecuteSparqlConstructAction());
		actionHandlerList.add(new SplitPropertyValuesAction());
		actionHandlerList.add(new ProcessPropertyValueStringsAction());
		actionHandlerList.add(new SmushResourcesAction());
		actionHandlerList.add(new NameBlankNodesAction());
		this.utils = new JenaIngestUtils();
	}
	
	public void run() {
		run(null);
	}
	
	/**
	 * Runs the workflow
	 */
	public void run(Individual startingWorkflowStep) {
		for (Individual step : getWorkflowSteps(startingWorkflowStep)) {
			Individual action = getAction(step);
			log.debug("Executing workflow action "+action.getURI());
			for (ActionHandler handler : actionHandlerList) {
				ActionResult result = handler.handleAction(action);
				if (result != null) {
					break;
				}
			}
		}
	}
	
	/*
	 * returns the Action related to the supplied WorkflowStep
	 */
	private Individual getAction(Individual stepInd) {
		log.debug("Workflow step: "+stepInd.getURI());
		RDFNode actionNode = stepInd.getPropertyValue(WorkflowOntology.action);
		if (actionNode != null && actionNode.canAs(Individual.class)) {
			return (Individual) actionNode.as(Individual.class);
		}
		return null;
	}
	
	public List<Individual> getWorkflowSteps(Individual startingWorkflowStep) {
		List<Individual> workflowSteps = new LinkedList<Individual>();
		Individual currentInd = (startingWorkflowStep == null) ? getWorkflowStep(workflowInd.getPropertyValue(WorkflowOntology.firstStep)) : startingWorkflowStep;
		while (currentInd != null) {
			workflowSteps.add(currentInd);
			currentInd = getWorkflowStep(currentInd.getPropertyValue(WorkflowOntology.nextStep));
		}
		return workflowSteps;
	}
	
	private Individual getWorkflowStep(RDFNode stepNode) {
		if (stepNode == null) {
			return null;
		}
		if ( (stepNode != null) && (stepNode.canAs(Individual.class)) ) {
			Individual nextStepInd = (Individual) stepNode.as(Individual.class);
			if (instanceOf(nextStepInd,WorkflowOntology.WorkflowStep)) {
				return nextStepInd;
			} 
		}
		return null;
	}
	
	private boolean instanceOf(Individual ind, Resource type) {
		for ( Resource typeRes : (List<Resource>) ind.listRDFTypes(false).toList() ) {
			if (!typeRes.isAnon() && typeRes.getURI().equals(type.getURI())) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * gets the appropriate Jena Literal for a Value individual in the model,
	 * depending on whether the Value is a Variable or a Literal
	 * At some point 
	 */
	private Literal getValue(RDFNode valueIndNode) {
		Individual valueInd = (Individual) valueIndNode.as(Individual.class);
		if (instanceOf(valueInd,WorkflowOntology.Literal)) {
			RDFNode valueNode = valueInd.getPropertyValue(WorkflowOntology.literalValue);
			if ( (valueNode != null) && (valueNode.isLiteral()) ) {
				return (Literal) valueNode.as(Literal.class);
			}
		} else if (instanceOf(valueInd,WorkflowOntology.Variable)){
			RDFNode variableNameNode = valueInd.getPropertyValue(WorkflowOntology.variableName);
			if ( (variableNameNode != null) && (variableNameNode.isLiteral())) {
				return varMap.get( ((Literal)variableNameNode.as(Literal.class)).getLexicalForm() );
			}
		}
		return null;
	}
	
	/*
	 * returns the model represented by the given Node, which is expected to be an Individual of type Model
	 */
	private Model getModel(RDFNode modelNode) {
	    if (modelNode == null) {
	        return null;
	    }
		Individual modelInd = (Individual) modelNode.as(Individual.class);
		String modelNameStr = ((Literal)modelInd.getPropertyValue(WorkflowOntology.modelName).as(Literal.class)).getLexicalForm();
		// false = strict mode off, i.e., 
		// if a model already exists of the given name, return it.  Otherwise, create a new one.
		return vitroJenaModelMaker.createModel(modelNameStr,false);
	}
	
	private interface ActionResult {}
	private class ActionResultImpl implements ActionResult {}
	
	private interface ActionHandler {
		public ActionResult handleAction(Individual actionInd);
	}
	
	// ALL THE DIFFERENT ACTION HANDLERS
	
	private class ClearModelAction implements ActionHandler { 
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.ClearModelAction)) {
				Model sourceModel = getModel(actionInd.getPropertyValue(WorkflowOntology.sourceModel)); 
				sourceModel.enterCriticalSection(Lock.WRITE);
				try{
					// this method is used so that any listeners can see each statement removed
					sourceModel.removeAll((Resource)null,(Property)null,(RDFNode)null);
				} finally {
					sourceModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			} else {
				return null;
			}		
		}
	}
	
	private class AddModelsAction implements ActionHandler { 
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.AddModelAction)) {
				Model sourceModel = getModel(actionInd.getPropertyValue(WorkflowOntology.sourceModel)); 
				Model modelToAdd = getModel(actionInd.getPropertyValue(WorkflowOntology.modelToAdd));
				Model destinationModel = getModel(actionInd.getPropertyValue(WorkflowOntology.destinationModel)); 
				Boolean applyChangesDirectlyToSource = false;
				RDFNode valueNode = actionInd.getPropertyValue(WorkflowOntology.applyChangesDirectlyToSource);
				if ((valueNode != null) && (valueNode.isLiteral())) {
				    applyChangesDirectlyToSource = ((Literal)valueNode.as(Literal.class)).getBoolean();
				}

				sourceModel.enterCriticalSection(Lock.WRITE);
				try {
					modelToAdd.enterCriticalSection(Lock.READ);
					try {
					    if (applyChangesDirectlyToSource) {
					        // TODO: are all listeners notified this way?
					        sourceModel.add(modelToAdd);
					    } else {
    						destinationModel.enterCriticalSection(Lock.WRITE);
    						try{
    							destinationModel.add(modelToAdd);
    						} finally {
    							destinationModel.leaveCriticalSection();
    						}
					    }
					} finally {
						modelToAdd.leaveCriticalSection();
					}
				} finally {
					sourceModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			} else {
				return null;
			}		
		}
	}
	
	private class SubtractModelsAction implements ActionHandler { 
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.SubtractModelAction)) {
				Model sourceModel = getModel(actionInd.getPropertyValue(WorkflowOntology.sourceModel)); 
				Model modelToSubtract = getModel(actionInd.getPropertyValue(WorkflowOntology.modelToSubtract));
				Model destinationModel = getModel(actionInd.getPropertyValue(WorkflowOntology.destinationModel)); 
				Boolean applyChangesDirectlyToSource = false;
				RDFNode valueNode = actionInd.getPropertyValue(WorkflowOntology.applyChangesDirectlyToSource);
				if ((valueNode != null) && (valueNode.isLiteral())) {
				    applyChangesDirectlyToSource = ((Literal)valueNode.as(Literal.class)).getBoolean();
				}
				sourceModel.enterCriticalSection(Lock.WRITE);
				try {
					modelToSubtract.enterCriticalSection(Lock.READ);
					try {
    					if (applyChangesDirectlyToSource) {
                            // TODO: are all listeners notified this way?
    					    sourceModel.remove(modelToSubtract);
    					} else {
    						destinationModel.enterCriticalSection(Lock.WRITE);
    						try{
    							destinationModel.add(sourceModel.difference(modelToSubtract));
    						} finally {
    							destinationModel.leaveCriticalSection();
    						}
    					}
					} finally {
						modelToSubtract.leaveCriticalSection();
					}
				} finally {
					sourceModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			} else {
				return null;
			}		
		}
	}
	
	private class ExecuteSparqlConstructAction implements ActionHandler {
		
		private static final String QUERY_STR_PROPERTY = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7/sparql#queryStr";
			
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.SPARQLCONSTRUCTAction)) {
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : (List<RDFNode>) actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					log.debug("SPARQL: adding submodel ");
					sourceModel.addSubModel(getModel(node));
				}
				if (actionInd.getPropertyValue(WorkflowOntology.destinationModel) == null) {
				    log.debug("Error: destination model for SPARQL Construct action not specified for this action");
				    return null;
				}
				Model destinationModel = getModel(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				Model tempModel = ModelFactory.createDefaultModel();
				OntResource sparqlQuery = (OntResource) actionInd.getPropertyValue(WorkflowOntology.sparqlQuery);
				String queryStr = ((Literal)sparqlQuery.getPropertyValue(ResourceFactory.createProperty(QUERY_STR_PROPERTY))).getLexicalForm();
				log.debug("SPARQL query: \n" + queryStr);
				Query query = QueryFactory.create(queryStr,Syntax.syntaxARQ);
		        QueryExecution qexec = QueryExecutionFactory.create(query,sourceModel);
		        qexec.execConstruct(tempModel);
		        destinationModel.add(tempModel);
				return new ActionResultImpl();
			}
			return null;
		}
	}
	
	private class SmushResourcesAction implements ActionHandler {
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.SmushResourcesAction)) {
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : (List<RDFNode>) actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					sourceModel.addSubModel(getModel(node));
				}
				Model destinationModel = getModel(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				String smushPropertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.smushOnProperty)).getLexicalForm();
				destinationModel.enterCriticalSection(Lock.WRITE);
				try {
					destinationModel.add(utils.smushResources(sourceModel, ResourceFactory.createProperty(smushPropertyURI)));
				} finally {
					destinationModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			}
			return null;
		}
	}
	
	private class NameBlankNodesAction implements ActionHandler {
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.NameBlankNodesAction)) {
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : (List<RDFNode>) actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					sourceModel.addSubModel(getModel(node));
				}
				Model destinationModel = getModel(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				String uriPrefix = getValue(actionInd.getPropertyValue(WorkflowOntology.uriPrefix)).getLexicalForm();
				destinationModel.add(utils.renameBNodes(sourceModel, uriPrefix));
				return new ActionResultImpl();
			}
			return null;
		}
	}
	
	private class SplitPropertyValuesAction implements ActionHandler { 
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.SplitPropertyValuesAction)) {
				// We use an OntModel here because this API supports submodels
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : (List<RDFNode>) actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					sourceModel.addSubModel(getModel(node));
				}
				Model destinationModel = getModel(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				String propertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.originalProperty)).getLexicalForm();
				String newPropertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.newProperty)).getLexicalForm();
				String splitRegex = getValue(actionInd.getPropertyValue(WorkflowOntology.splitRegex)).getLexicalForm();
				boolean trim = true;
				try {
					trim = getValue(actionInd.getPropertyValue(WorkflowOntology.trim)).getBoolean();
				} catch (Exception e) {}
				destinationModel.enterCriticalSection(Lock.WRITE);
				try {
					destinationModel.add(utils.splitPropertyValues(sourceModel, propertyURI, splitRegex, newPropertyURI, trim));
				} finally {
					destinationModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			} else {
				return null;
			}		
		}
	}	
	
	private class ProcessPropertyValueStringsAction implements ActionHandler { 
		public ActionResult handleAction(Individual actionInd) {
			if (instanceOf(actionInd,WorkflowOntology.ProcessPropertyValueStringsAction)) {
				// We use an OntModel here because this API supports submodels
				OntModel sourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				for (RDFNode node : (List<RDFNode>) actionInd.listPropertyValues(WorkflowOntology.sourceModel).toList()) {
					sourceModel.addSubModel(getModel(node));
				}
				Model destinationModel = null;
				try {
					destinationModel = getModel(actionInd.getPropertyValue(WorkflowOntology.destinationModel));
				} catch (Exception e) {}
				Model additionsModel = null;
				try {
					additionsModel = getModel(actionInd.getPropertyValue(WorkflowOntology.additionsModel));
				} catch (Exception e) {}
				Model retractionsModel = null;
				try {
					retractionsModel = getModel(actionInd.getPropertyValue(WorkflowOntology.retractionsModel));
				} catch (Exception e) {}
				String processorClass = getValue(actionInd.getPropertyValue(WorkflowOntology.processorClass)).getLexicalForm();
				String processorMethod = getValue(actionInd.getPropertyValue(WorkflowOntology.processorMethod)).getLexicalForm();
				String propertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.originalProperty)).getLexicalForm();
				String newPropertyURI = getValue(actionInd.getPropertyValue(WorkflowOntology.newProperty)).getLexicalForm();
				destinationModel.enterCriticalSection(Lock.WRITE);
				try {
				    if (log.isDebugEnabled()) {
				        log.debug("calling processPropertyValueStrings ...");
				    }
					utils.processPropertyValueStrings(sourceModel, destinationModel, additionsModel, retractionsModel, processorClass, processorMethod, propertyURI, newPropertyURI);
				} finally {
					destinationModel.leaveCriticalSection();
				}
				return new ActionResultImpl();
			} else {
				return null;
			}		
		}
	}	
	
	
}
