/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.directives;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;
import freemarker.core.Environment;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;

/**
 * Freemarker directive to make substitutions in DataGetter and return the data
 * in variable.
 */
public class DataGetterDirective extends BaseTemplateDirectiveModel {
    private static final Log log = LogFactory.getLog(DataGetterDirective.class);

    @Override
    public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
            throws TemplateException, IOException {
        long startTime = System.nanoTime();
        String dataGetterUri = getRequiredSimpleScalarParameter(params, "uri");
        String variableName = getOptionalSimpleScalarParameter(params, "var");
        HttpServletRequest req = (HttpServletRequest) env.getCustomAttribute("request");
        VitroRequest vreq = new VitroRequest(req);
        debug(getTimeSince(startTime) + "ms .1");
        try {
            debug(getTimeSince(startTime) + "ms .2");
            OntModel model = vreq.getDisplayModel();
            debug(getTimeSince(startTime) + "ms .3");
            DataGetter dataGetter = DataGetterUtils.dataGetterForURI(vreq, model, dataGetterUri);
            debug(getTimeSince(startTime) + "ms .4");
            Map<String, Object> parameters = getOptionalHashModelParameter(params, "parameters");
            debug(getTimeSince(startTime) + "ms .5");
            applyDataGetter(dataGetter, env, parameters, variableName);
            debug(getTimeSince(startTime) + "ms .6");
        } catch (Exception e) {
            handleException(dataGetterUri, "Could not process data getter '%s'", e);
        }
    }

    /**
     * Get the data from a DataGetter, provide variable values for substitution and
     * store results in Freemarker environment variable.
     * 
     * @param dataGetter - DataGetter to execute
     * @param env - Freemarker environment
     * @param parameters - parameters to substitute in DataGetter
     * @param overrideVariableName - name of Freemarker variable
     * 
     */
    private static void applyDataGetter(DataGetter dataGetter, Environment env, Map<String, Object> parameters,
            String overrideVariableName) throws TemplateModelException {
        Map<String, Object> data = dataGetter.getData(parameters);
        if (data != null) {
            Object key = data.get("variableName");
            if (key != null) {
                Object value = data.get(key.toString());
                setVariable(env, overrideVariableName, key.toString(), value);
            }
        }
    }

    /**
     * Wrap DataGetter results and assign it to Freemarker environment variable.
     * 
     * @param env - Freemarker environment
     * @param overrideVariableName - name of Freemarker variable
     * @param key - name of data returned by DataGetter
     * @param value - value of data returned by DataGetter
     * 
     */
    private static void setVariable(Environment env, String overrideVariableName, String key, Object value)
            throws TemplateModelException {
        ObjectWrapper wrapper = env.getObjectWrapper();
        if (!StringUtils.isBlank(overrideVariableName)) {
            env.setVariable(overrideVariableName, wrapper.wrap(value));
            debug(String.format("Stored in environment: '%s' = '%s'", overrideVariableName, value));
        } else {
            env.setVariable(key, wrapper.wrap(value));
            debug(String.format("Stored in environment: '%s' = '%s'", key, value));
        }
    }

    /**
     * Handle exceptions that could happen during DataGetter execution
     */
    private void handleException(String templateName, String messageTemplate, Exception e) {
        log.error(String.format(messageTemplate, templateName));
        log.error(e, e);
    }

    @Override
    public Map<String, Object> help(String name) {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        map.put("effect", "Find the freemarker template and optional DataGetters. "
                + "Apply parameter substitutions in DataGetters." + "Execute the DataGetters and render the template.");
        map.put("comments", "");
        Map<String, String> params = new HashMap<String, String>();
        params.put("template", "Freemarker template file name");
        params.put("parameters", "Map of parameters and values");
        map.put("parameters", params);

        List<String> examples = new ArrayList<String>();
        examples.add("<@dataGetter uri = \"http://dataGetterUri\" \n" + "var = \"foobar\" \n"
                + "parameters = { \"object\": \"http://objUri\", \"property\": \"http://propUri\" } />");
        map.put("examples", examples);

        return map;
    }

    private static long getTimeSince(long previousTime) {
        return (System.nanoTime() - previousTime) / 1000000;
    }

    private static void debug(String message) {
        if (log.isDebugEnabled()) {
            log.debug(message);
        }
    }

}
