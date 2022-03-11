package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.converters.IOMessageConverterUtils;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.PrimitiveData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.Lock;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.StringData;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class SPARQLQuery extends Operation {

    private static final Log log = LogFactory.getLog(SPARQLQuery.class);

    private String queryText;
    private ModelComponent modelComponent;
    private Parameters requiredParams = new Parameters();
    private Parameters providedParams = new Parameters();

    @Override
    public void dereference() {
        // TODO
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#requiresParameter")
    public void addRequiredParameter(Parameter param) {
        requiredParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#providesParameter")
    public void addProvidedParameter(Parameter param) {
        providedParams.add(param);
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#sparqlQueryText", minOccurs = 1, maxOccurs = 1)
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#hasModel", minOccurs = 1, maxOccurs = 1)
    public void setQueryModel(ModelComponent model) {
        this.modelComponent = model;
    }

    @Override
    public Parameters getRequiredParams() {
        return requiredParams;
    }

    @Override
    public Parameters getProvidedParams() {
        return providedParams;
    }

    @Override
    public OperationResult run(OperationData inputOutput) {
        if (!isInputValid(inputOutput)) {
            return new OperationResult(500);
        }
        int resultCode = 200;
        Model queryModel = ModelAccess.on(inputOutput.getContext()).getOntModel(modelComponent.getName());
        ParameterizedSparqlString pss = new ParameterizedSparqlString();
        for (String paramName : requiredParams.getNames()) {
            pss.setLiteral(paramName, inputOutput.get(paramName), requiredParams.get(paramName).getRDFDataType());
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

                int j = 0;
                while (results.hasNext()) {
                    QuerySolution solution = results.nextSolution();
                    log.debug("Query solution " + i++);
                    for (String var : vars) {
                        if (solution.contains(var)) {
                            log.debug(var + " : " + solution.get(var));
                            String fieldName = computeProvidedFieldName(j + "." + var);
                            Parameter parameter = getProvidedParams().get(fieldName);
                            ParameterType type = (parameter != null) ? parameter.getType() : null;
                            PrimitiveData data = IOMessageConverterUtils.getPrimitiveDataFromString(solution.get(var).toString(), type);
                            if (data != null)
                                inputOutput.add(fieldName, data);
                        }
                    }
                    j++;
                }

            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                e.printStackTrace();
                resultCode = 500;
            } finally {
                qexec.close();
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            e.printStackTrace();
            resultCode = 500;
        } finally {
            queryModel.leaveCriticalSection();
        }
        if (!isOutputValid(inputOutput)) {
            return new OperationResult(500);
        }
        return new OperationResult(resultCode);
    }

}
