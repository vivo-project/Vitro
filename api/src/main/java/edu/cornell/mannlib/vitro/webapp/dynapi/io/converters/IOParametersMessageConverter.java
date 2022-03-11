package edu.cornell.mannlib.vitro.webapp.dynapi.io.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.types.ParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.*;

public class IOParametersMessageConverter extends IOMessageConverter {

    private static final IOParametersMessageConverter INSTANCE = new IOParametersMessageConverter();

    public static IOParametersMessageConverter getInstance() {
        return INSTANCE;
    }

    public ObjectData loadDataFromRequest(HttpServletRequest request, Parameters parameters) {
        Map<String, Data> ioDataMap = new HashMap<String, Data>();
        Map<String, String[]> params = request.getParameterMap();
        if (params != null)
            for (String key : params.keySet()) {
                String[] values = params.get(key);
                Parameter parameter = parameters.get(key);
                if (parameter != null) {
                    Data data = fromRequest(values, parameter.getType());
                    if (data != null) {
                        ioDataMap.put(key, data);
                    }
                }
            }
        return new ObjectData(ioDataMap);
    }

    public String exportDataToResponseBody(ObjectData data) {
        StringBuilder retVal = new StringBuilder();
        Map<String, Data> ioDataMap = data.getContainer();
        for (String key : ioDataMap.keySet()) {
            Data value = ioDataMap.get(key);
            retVal.append(key);
            retVal.append("=");
            retVal.append(toString(value));
            retVal.append("\n");
        }
        return retVal.toString();
    }

    public Data fromRequest(String[] values, ParameterType type) {
        Data retVal = null;
        if ((values != null) && (values.length != 0) && (type != null)) {
            if (type instanceof ArrayParameterType) {
                List<Data> dataItems = new ArrayList<Data>();
                ParameterType innerType = ((ArrayParameterType) type).getElementsType() ;
                for (String value : values) {
                    dataItems.add(IOMessageConverterUtils.getPrimitiveDataFromString(value, innerType));
                }
                retVal = new ArrayData(dataItems);
            } else
                retVal = IOMessageConverterUtils.getPrimitiveDataFromString(values[0], type);
        }
        return retVal;
    }

    public String toString(Data data) {
        String retVal = null;
        if (data instanceof ArrayData) {
            StringBuilder allValues = new StringBuilder();
            for (Data arrayItem : ((ArrayData) data).getContainer()) {
                allValues.append(arrayItem.toString());
            }
            retVal = allValues.toString();
        } else if (data instanceof PrimitiveData)
            retVal = data.toString();
        return retVal;
    }


}
