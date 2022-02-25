package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import org.apache.commons.logging.LogFactory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SPARQLQuery extends AbstractQueryOperation{

 	private static final Log log = LogFactory.getLog(SPARQLQuery.class);

	private String queryText;
	private ModelComponent modelComponent;

	@Override
	public void dereference() {
		//TODO
	}

	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#sparqlQueryText", minOccurs = 1, maxOccurs = 1)
	public void setQueryText(String queryText) {
		this.queryText = queryText;
	}
	
	@Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasQueryModel", minOccurs = 1, maxOccurs = 1)
	public void setQueryModel(ModelComponent model) {
		this.modelComponent = model;
	}
	
	@Override
	public OperationResult run(OperationData input) {
		if (!isInputValid(input)) {
			return new OperationResult(500);
		}
		int resultCode = 200;
		Model queryModel = ModelAccess.on(input.getContext()).getOntModel(modelComponent.getName());
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		for (String paramName : requiredParams.getNames()) {
			pss.setLiteral(paramName, input.get(paramName)[0],requiredParams.get(paramName).getRDFDataType());	
		}
		pss.setCommandText(queryText);
		queryModel.enterCriticalSection(Lock.READ);
		try {
			QueryExecution qexec = QueryExecutionFactory.create(pss.toString(), queryModel);
			try {
				ResultSet results = qexec.execSelect();
				int i = 1;
				List<String> vars = results.getResultVars();
				log.debug("Query vars: " + String.join(", ", vars));

				while (results.hasNext()) {
					QuerySolution solution = results.nextSolution();
					log.debug("Query solution " + i++);
					for (String var :vars) {
						if (solution.contains(var)) {
							log.debug(var + " : " + solution.get(var));
						}
					}
				}
				
			} catch(Exception e) {
				log.error(e.getLocalizedMessage());
				e.printStackTrace();
				resultCode = 500;
			}finally {
				qexec.close();
			}
		} catch(Exception e) {
			log.error(e.getLocalizedMessage());
			e.printStackTrace();
			resultCode = 500;
		} finally {
			queryModel.leaveCriticalSection();
		}
		return new OperationResult(resultCode);
	}

}
