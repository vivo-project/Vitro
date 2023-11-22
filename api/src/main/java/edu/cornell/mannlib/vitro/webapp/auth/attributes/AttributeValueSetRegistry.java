package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeValueSetRegistry {

    private static AttributeValueSetRegistry INSTANCE = new AttributeValueSetRegistry();
    private Map<AttributeValueKey, AttributeValueSet> valuesMap = new ConcurrentHashMap<>();

    private AttributeValueSetRegistry() {
        INSTANCE = this;
    }

    public static AttributeValueSetRegistry getInstance() {
        return INSTANCE;
    }

    public AttributeValueSet get(AttributeValueKey key) {
        return valuesMap.get(key);
    }

    public void put(AttributeValueKey key, AttributeValueSet values) {
        valuesMap.put(key, values);
    }

    public void clear() {
        valuesMap.clear();
    }
}
