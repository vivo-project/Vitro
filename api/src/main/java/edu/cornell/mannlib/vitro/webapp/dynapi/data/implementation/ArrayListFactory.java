/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.ConversionException;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.JSONConverter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ArrayParameterType;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.types.ParameterType;

public class DynapiArrayList {

    public static String serialize(List list) {
        return list.toString();
    }

    public static String serialize(ArrayList list) {
        return serialize((List) list);
    }

    public static List deserialize(String input, ArrayParameterType type) throws ConversionException {
        List listData = new ArrayList();
        JsonNode jsonNode = JSONConverter.readJson(input);
        if (jsonNode == null) {
            throw new ConversionException("Input serialized json malformed :" + input);
        }
        if (!jsonNode.isArray()) {
            throw new ConversionException("Input serialized json is not an array :" + input);
        }
        ArrayNode array = (ArrayNode) jsonNode;
        for (int i = 0; i < array.size(); i++) {
            JsonNode item = array.get(i);
            final ParameterType valuesType = type.getValuesType();
            Object itemObject = valuesType.getImplementationType().deserialize(valuesType, item.toString());
            listData.add(itemObject);
        }
        return listData;
    }
}
