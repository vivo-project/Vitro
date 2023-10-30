package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeValuesRegistry implements AttributeValues {

    private static AttributeValues INSTANCE = new AttributeValuesRegistry();
    private Map<AttributeValueKey, AttributeValueContainer> valuesMap = new ConcurrentHashMap<>();

    private AttributeValuesRegistry() {
        INSTANCE = this;
    }

    public static AttributeValues getInstance() {
        return INSTANCE;
    }

    @Override
    public AttributeValueContainer get(AttributeValueKey key) {
        return valuesMap.get(key);
    }

    @Override
    public void put(AttributeValueKey key, AttributeValueContainer values) {
        valuesMap.put(key, values);
    }

    @Override
    public void clear() {
        valuesMap.clear();
    }
}
