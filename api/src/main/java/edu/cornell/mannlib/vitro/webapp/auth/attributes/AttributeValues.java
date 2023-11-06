package edu.cornell.mannlib.vitro.webapp.auth.attributes;

public interface AttributeValues {

    AttributeValueContainer get(AttributeValueKey key);

    void put(AttributeValueKey key, AttributeValueContainer values);

    void clear();

}
