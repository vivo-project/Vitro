package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.dynapi.io.converters.IOJsonMessageConverter;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.converters.IOParametersMessageConverter;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;

public class OperationData {

    public final static String RESOURCE_ID = "resource_id";

    private final Map<String, String[]> params;
    private final ServletContext context;
    private ObjectData data;

    public OperationData(HttpServletRequest request) {
        params = request.getParameterMap();
        context = request.getServletContext();
        // if (ContentType.APPLICATION_JSON.toString().equalsIgnoreCase(request.getContentType()))
        //   data = IOJsonMessageConverter.getInstance().loadDataFromRequest(request);
        // else
        //   data = IOParametersMessageConverter.getInstance().loadDataFromRequest(request);
        data = IOJsonMessageConverter.getInstance().loadDataFromRequest(request);
        if ((data == null) || (data.getContainer().size() == 0)) {
            data = IOParametersMessageConverter.getInstance().loadDataFromRequest(request);
        }
    }

    public ServletContext getContext() {
        return context;
    }

    public ObjectData getRootData() {
        return data;
    }

    public Data getData(String paramName) {
        return data.getElement(paramName);
    }

    public boolean has(String paramName) {
        return getData(paramName) != null;
    }

    public String[] get(String paramName) {
        String[] retVal = new String[0];
        Data internalData = getData(paramName);
        if (internalData != null) {
            List<String> listString = internalData.getAsString();
            retVal = (listString != null) ? listString.toArray(new String[0]) : retVal;
        }
        return retVal;
    }

    public void add(String key, Data newData) {
        data.setElement(key, newData);
    }

}
