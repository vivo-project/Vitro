package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeValueContainerRegistry {

    private static AttributeValueContainerRegistry INSTANCE = new AttributeValueContainerRegistry();
    private Map<AttributeValueKey, AttributeValueContainer> valuesMap = new ConcurrentHashMap<>();

    private AttributeValueContainerRegistry() {
        INSTANCE = this;
    }

    public static AttributeValueContainerRegistry getInstance() {
        return INSTANCE;
    }

    public AttributeValueContainer get(AttributeValueKey key) {
        return valuesMap.get(key);
    }

    public void put(AttributeValueKey key, AttributeValueContainer values) {
        valuesMap.put(key, values);
    }

    public void clear() {
        valuesMap.clear();
    }
}
