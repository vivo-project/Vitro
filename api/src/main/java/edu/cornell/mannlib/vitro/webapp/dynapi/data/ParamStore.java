package edu.cornell.mannlib.vitro.webapp.dynapi.data;

import java.util.HashMap;
import java.util.Map;

public class ParamStore {

    protected Map<String,RawData> dataMap = new HashMap<>(); 
    
    public void addData(String name, RawData data) {
        dataMap.put(name, data);
    }
}
