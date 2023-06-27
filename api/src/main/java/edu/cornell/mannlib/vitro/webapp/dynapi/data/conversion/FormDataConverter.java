package edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import edu.cornell.mannlib.vitro.webapp.dynapi.RESTEndpoint;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.DataStore;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;

public class FormDataConverter {

    private static final Log log = LogFactory.getLog(FormDataConverter.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public static void convert(HttpServletRequest request, Procedure procedure, DataStore dataStore)
            throws ConversionException {
        Parameters required = procedure.getInputParams();
        Map<String, String[]> received = request.getParameterMap();
        convertRequiredParams(dataStore, required, received);
        convertOptionalParams(dataStore, procedure.getOptionalParams(), received);
    }

    private static void convertOptionalParams(DataStore dataStore, Parameters optional,
            Map<String, String[]> received) {
        for (String name : optional.getNames()) {
            String[] values = received.get(name);
            if (values == null || values.length == 0) {
                String message = String.format("Optional parameter %s not found", name);
                log.debug(message);
                continue;
            }
            try {
                Parameter param = optional.get(name);
                if (param.isArray()) {
                    readArray(dataStore, name, values, param);
                } else {
                    readParam(dataStore, name, values, param);
                }
            } catch (Exception e) {
                log.debug(e, e);
            }
        }
    }

    private static void convertRequiredParams(DataStore dataStore, Parameters required, Map<String, String[]> received)
            throws ConversionException {
        for (String name : required.getNames()) {
            String[] values = received.get(name);
            if (values == null || values.length == 0) {
                if (name.equals(RESTEndpoint.RESOURCE_ID) && !StringUtils.isBlank(dataStore.getResourceId())) {
                    values = new String[] { dataStore.getResourceId() };
                } else {
                    String message = String.format("Parameter %s not found", name);
                    throw new ConversionException(message);
                }
            }
            Parameter param = required.get(name);

            if (param.isArray()) {
                readArray(dataStore, name, values, param);
            } else {
                readParam(dataStore, name, values, param);
            }
        }
    }

    private static void readParam(DataStore dataStore, String name, String[] values, Parameter param)
            throws ConversionException {
        Data data = new Data(param);
        data.setRawString(values[0]);
        try {
            data.earlyInitialization();
        } catch (Exception e) {
            log.error(e, e);
            throw new ConversionException(e.getLocalizedMessage());
        }
        dataStore.addData(name, data);
    }

    private static void readArray(DataStore dataStore, String name, String[] values, Parameter param) {
        Data data = new Data(param);
        ArrayNode node = mapper.createArrayNode();
        for (int i = 0; i < values.length; i++) {
            node.add(values[i]);
        }
        data.setRawString(node.toString());
        dataStore.addData(name, data);
    }

    public static void convert(HttpServletResponse response, Procedure action, DataStore dataStore)
            throws ConversionException {
        throw new ConversionException("Not implemented!");
    }

}
