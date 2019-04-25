package edu.cornell.mannlib.vitro.webapp.utils;

/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
//import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * Utility to convert JSON to Freemarker-compatible data model
 *
 * @author janv
 */
public final class JsonToFmModel
{
    public static String ROOT_ARRAY = "root";

    // note: current format is dependent on ISO8601DateFormat.parser, eg. YYYY-MM-DDThh:mm:ss.sssTZD
//    private static String REGEXP_ISO8061 = "^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})(.([0-9]){3})?(Z|[\\+\\-]([0-9]{2}):([0-9]{2}))$";
//    private static Pattern matcherISO8601 = Pattern.compile(REGEXP_ISO8061);
//
//    public static boolean autoConvertISO8601 = true;

    /**
     * JSONObject is an unordered collection of name/value pairs -&gt; convert to Map (equivalent to Freemarker "hash")
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> convertJSONObjectToMap(ObjectNode jo)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        Iterator<String> itr = (Iterator<String>)jo.fieldNames();
        while (itr.hasNext())
        {
            String key = (String)itr.next();

            Object o = jo.get(key);
            if (o instanceof ObjectNode)
            {
                model.put(key, convertJSONObjectToMap((ObjectNode)o));
            }
            else if (o instanceof ArrayNode)
            {
                model.put(key, convertJSONArrayToList((ArrayNode)o));
            }
            else
            {
//                if ((o instanceof String) && autoConvertISO8601 && (matcherISO8601.matcher((String)o).matches()))
//                {
//                    o = ISO8601DateFormat.parse((String)o);
//                }

                model.put(key, o);
            }
        }

        return model;
    }

    /**
     * JSONArray is an ordered sequence of values -&gt; convert to List (equivalent to Freemarker "sequence")
     */
    public static List<Object> convertJSONArrayToList(ArrayNode ja)
    {
        List<Object> model = new ArrayList<Object>();

        for (int i = 0; i < ja.size(); i++)
        {
            JsonNode o = ja.get(i);

            if (o instanceof ArrayNode)
            {
                model.add(convertJSONArrayToList((ArrayNode) o));
            }
            else if (o instanceof ObjectNode)
            {
                model.add(convertJSONObjectToMap((ObjectNode) o));
            }
            else
            {
//                if ((o instanceof String) && autoConvertISO8601 && (matcherISO8601.matcher((String)o).matches()))
//                {
//                    o = ISO8601DateFormat.parse((String)o);
//                }

                model.add(o);
            }
        }

        return model;
    }

    // for debugging only
    public static String toString(Map<String, Object> map)
    {
        return JsonToFmModel.toStringBuffer(map, 0).toString();
    }

    @SuppressWarnings("unchecked")
    private static StringBuffer toStringBuffer(Map<String, Object> unsortedMap, int indent)
    {
        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < indent; i++)
        {
            tabs.append("\t");
        }

        StringBuffer sb = new StringBuffer();

        SortedMap<String, Object> map = new TreeMap<String, Object>();
        map.putAll(unsortedMap);

        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if (entry.getValue() instanceof Map)
            {
                sb.append(tabs).append(entry.getKey()).append(":").append(entry.getValue().getClass()).append("\n");
                sb.append(JsonToFmModel.toStringBuffer((Map<String, Object>)entry.getValue(), indent+1));
            }
            else if (entry.getValue() instanceof List)
            {
                sb.append(tabs).append("[\n");
                List l = (List)entry.getValue();
                for (Object aL : l) {
                    sb.append(tabs).append(aL).append(":").append((aL != null) ? aL.getClass() : "null").append("\n");
                }
                sb.append(tabs).append("]\n");
            }
            else
            {
                sb.append(tabs).append(entry.getKey()).append(":").append(entry.getValue()).append(":").append((entry.getValue() != null ? entry.getValue().getClass() : "null")).append("\n");
            }
        }

        return sb;
    }
}
