package edu.cornell.mannlib.vitro.webapp.auth.attributes;

public interface AttributeValueSet {

    void add(String value);

    void remove(String value);

    void clear();

    boolean contains(String value);

    boolean containsSingleValue();

    String getSingleValue();

    boolean isEmpty();

    void setValueSetUri(String valueSetUri);

    String getValueSetUri();

    void setType(String type);

    void setDataSetUri(String dataSetUri);

    void setKey(AttributeValueKey key);

}
