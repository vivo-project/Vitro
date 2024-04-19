/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.data.implementation;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.data.Data;

public interface JsonContainer {

    Data getItem(String key, Parameter parameter);

    JsonNode asJsonNode();

    void addKeyValue(String var, Data data);

    void addValue(Data data);

    List<String> getDataAsStringList();

    boolean contains(String key);

}
