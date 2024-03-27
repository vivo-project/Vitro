/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation.JacksonJsonContainer;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

public class RdfView {

    private static final String XML_LANG = "xml:lang";
    private static final String DATATYPE = "datatype";
    private static final String LITERAL = "literal";
    private static final String VALUE = "value";
    private static final String TYPE = "type";
    private static final String URI = "uri";
    private static ObjectMapper mapper = new ObjectMapper();

    public static Map<String, List<Literal>> getLiteralsMap(DataStore dataStore, Parameters params) {
        Map<String, List<Literal>> result = new HashMap<>();
        for (String name : params.getNames()) {
            Parameter param = params.get(name);
            if (param.getType().isLiteral()) {
                Data data = dataStore.getData(name);
                Literal literal = ResourceFactory.createTypedLiteral(data.getObject().toString(), param.getType()
                        .getRdfType().getRDFDataType());
                List<Literal> list = Collections.singletonList(literal);
                result.put(name, list);
            } else if (JsonContainerView.isJsonArray(param) && param.getType().getValuesType().isLiteral()) {
                JacksonJsonContainer array = (JacksonJsonContainer) dataStore.getData(name).getObject();
                RDFDatatype rdfDataType = param.getType().getValuesType().getRdfType().getRDFDataType();
                List<String> list = array.getDataAsStringList();
                List<Literal> literalList = new LinkedList<>();
                for (String literalString : list) {
                    Literal literal = ResourceFactory.createTypedLiteral(literalString, rdfDataType);
                    literalList.add(literal);
                }
                result.put(name, literalList);
            }
        }
        return result;
    }

    public static Map<String, List<String>> getUrisMap(DataStore dataStore, Parameters params) {
        Map<String, List<String>> result = new HashMap<>();
        for (String name : params.getNames()) {
            Parameter param = params.get(name);
            if (param.getType().isUri()) {
                Data data = dataStore.getData(name);
                List<String> list = Collections.singletonList(data.getObject().toString());
                result.put(name, list);
            } else if (JsonContainerView.isJsonArray(param) && param.getType().getValuesType().isUri()) {
                JacksonJsonContainer array = (JacksonJsonContainer) dataStore.getData(name).getObject();
                List<String> list = array.getDataAsStringList();
                result.put(name, list);
            }
        }
        return result;
    }

    public static List<String> getLiteralNames(Parameters params) {
        List<String> result = new LinkedList<>();
        for (String name : params.getNames()) {
            if (params.get(name).getType().isLiteral()) {
                result.add(name);
            }
        }
        return result;
    }

    public static List<String> getUriNames(Parameters params) {
        List<String> result = new LinkedList<>();
        for (String name : params.getNames()) {
            if (params.get(name).getType().isUri()) {
                result.add(name);
            }
        }
        return result;
    }

    public static List<String> getUris(DataStore dataStore, Parameters params) {
        List<String> uris = new LinkedList<String>();
        List<String> uriParamNames = RdfView.getUriNames(params);
        for (String paramName : uriParamNames) {
            Data data = dataStore.getData(paramName);
            String uri = SimpleDataView.getStringRepresentation(data);
            uris.add(uri);
        }
        return uris;
    }

    public static boolean isRdfNode(Data data) {
        return data.getParam().getType().isRdfType();
    }

    public static JsonNode getAsJsonNode(Data data) {
        ObjectNode object = mapper.createObjectNode();
        RDFNode node = (RDFNode) data.getObject();
        if (node.isURIResource()) {
            object.put(TYPE, URI);
            object.put(VALUE, node.asResource().getURI());
        } else if (node.isLiteral()) {
            object.put(TYPE, LITERAL);
            Literal literal = node.asLiteral();
            object.put(VALUE, literal.getLexicalForm());
            object.put(DATATYPE, literal.getDatatypeURI());
            String lang = literal.getLanguage();
            if (!StringUtils.isBlank(lang)) {
                object.put(XML_LANG, lang);
            }
        }
        return object;
    }

}
