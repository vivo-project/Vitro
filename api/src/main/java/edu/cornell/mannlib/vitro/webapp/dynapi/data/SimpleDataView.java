package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.operations.SparqlSelectQuery;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

public class SimpleDataView {

    public static List<String> getNames(Parameters params) {
        List<String> result = new LinkedList<>();
        for (String name : params.getNames()) {
            Parameter param = params.get(name);
            if (!param.isJsonContainer() && !JsonView.isJsonNode(param)) {
                result.add(name);
            }
        }
        return result;
    }

    public static String getStringRepresentation(String name, DataStore store) {
        final Data data = store.getData(name);
        return data.getObject().toString();
    }

    public static String getStringRepresentation(Data data) {
        return data.getObject().toString();
    }

    public static void addFromSolution(DataStore dataStore, List<String> vars, QuerySolution solution,
            Parameters outputParams) throws ConversionException {
        List<String> simpleData = getNames(outputParams);
        for (String var : vars) {
            SparqlSelectQuery.log.debug(var + " : " + solution.get(var));
            if (simpleData.contains(var) && solution.contains(var)) {
                RDFNode solVar = solution.get(var);
                Parameter param = outputParams.get(var);
                Data data = new Data(param);
                // TODO: new data should be created based on it's RDF type and parameter type
                if (param.getType().isRdfType()) {
                    data.setRawString(solVar.toString());
                } else {
                    if (solVar.isLiteral()) {
                        Literal literal = (Literal) solVar;
                        data.setRawString(literal.getLexicalForm());
                    } else {
                        data.setRawString(solVar.toString());
                    }
                }

                data.earlyInitialization();
                dataStore.addData(var, data);
                simpleData.remove(var);
            }
        }
    }

    public static List<String> getPlainStringList(Parameters params, DataStore dataStore) {
        List<String> uris = new LinkedList<String>();
        for (String paramName : params.getNames()) {
            Data data = dataStore.getData(paramName);
            if (data.getParam().isString()) {
                String uri = data.getSerializedValue();
                uris.add(uri);
            }
        }
        return uris;
    }

}
