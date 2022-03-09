package edu.cornell.mannlib.vitro.webapp.dynapi.io.converters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;

import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ArrayData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.BooleanData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.Data;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.DecimalData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.IntegerData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.PrimitiveData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.StringData;

public class IOParametersMessageConverter implements IOMessageConverter {

    private static IOParametersMessageConverter INSTANCE = new IOParametersMessageConverter();

    public static IOParametersMessageConverter getInstance() {
        return INSTANCE;
    }

    public ObjectData loadDataFromRequest(HttpServletRequest request) {
        Map<String, Data> ioDataMap = new HashMap<String, Data>();
        Map<String, String[]> params = request.getParameterMap();
        if (params != null)
            for (String key : params.keySet()) {
                String[] values = params.get(key);
                Data data = fromRequest(values);
                if (data != null) {
                    ioDataMap.put(key, data);
                }
            }
        return new ObjectData(ioDataMap);
    }

    public String exportDataToResponseBody(ObjectData data) {
        StringBuilder retVal = new StringBuilder();
        Map<String, Data> ioDataMap = data.getContainer();
        for (String key : ioDataMap.keySet()) {
            Data value = ioDataMap.get(key);
            retVal.append(key + "=");
            retVal.append(toString(value));
            retVal.append("\n");
        }
        return retVal.toString();
    }

    public Data fromRequest(String[] values) {
        Data retVal = null;
        switch (getDataType(values)) {
            case Data.IOArray:
                List<Data> dataItems = new ArrayList<Data>();
                for (String value : values) {
                    dataItems.add(fromRequest(new String[] { value }));
                }
                retVal = new ArrayData(dataItems);
                break;
            case Data.IOInteger:
                retVal = new IntegerData(Integer.parseInt(values[0]));
                break;
            case Data.IODecimal:
                retVal = new DecimalData(Double.parseDouble(values[0]));
                break;
            case Data.IOBoolean:
                retVal = new BooleanData(Boolean.parseBoolean(values[0]));
                break;
            case Data.IOString:
                retVal = new StringData(values[0]);
                break;
        }
        return retVal;
    }

    public String toString(Data data) {
        String retVal = null;
        switch (getDataType(data)) {
            case Data.IOArray:
                StringBuilder allValues = new StringBuilder();
                for (Data arrayItem : ((ArrayData) data).getContainer()) {
                    allValues.append(((PrimitiveData<?>) arrayItem).toString());
                }
                retVal = allValues.toString();
                break;
            case Data.IOInteger:
            case Data.IODecimal:
            case Data.IOBoolean:
            case Data.IOString:
                retVal = ((PrimitiveData<?>) data).toString();
                break;
        }
        return retVal;
    }

    private int getDataType(String[] values) {
        if ((values == null) || (values.length == 0))
            return Data.IOUnknown;
        else if (values.length > 1)
            return Data.IOArray;
        else if (NumberUtils.isDigits(values[0]))
            return Data.IOInteger;
        else if (NumberUtils.isParsable(values[0]))
            return Data.IODecimal;
        else if ("true".equalsIgnoreCase(values[0]) || "false".equalsIgnoreCase(values[0]))
            return Data.IOBoolean;
        else
            return Data.IOString;
    }

    private int getDataType(Data data) {
        if (data instanceof ArrayData)
            return Data.IOArray;
        else if (data instanceof IntegerData)
            return Data.IOInteger;
        else if (data instanceof DecimalData)
            return Data.IODecimal;
        else if (data instanceof BooleanData)
            return Data.IOBoolean;
        else if (data instanceof StringData)
            return Data.IOString;
        else
            return Data.IOUnknown;
    }

}
