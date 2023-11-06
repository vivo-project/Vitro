package edu.cornell.mannlib.vitro.webapp.auth.attributes;

public interface AttributeValueContainer {

    void add(String value);

    void remove(String value);

    void clear();

    boolean contains(String value);

    boolean containsSingleValue();

    String getSingleValue();

    boolean isEmpty();

    void setContainerUri(String containerUri);

    String getContainerUri();

    void setType(String containerType);

    void setDataSetUri(String dataSetUri);

    void setKey(AttributeValueKey key);

}
