package edu.cornell.mannlib.vitro.webapp.auth.attributes;

public interface AttributeValues {

    public AttributeValueContainer get(AttributeValueKey key);

    public void put(AttributeValueKey key, AttributeValueContainer values);

    public void clear();

}
