package edu.cornell.mannlib.vitro.webapp.auth.attributes;

public interface AttributeValueContainer {

    public void add(String value);

    public void remove(String value);

    public void clear();

    public boolean contains(String value);

    public boolean containsSingleValue();

    public String getSingleValue();

    public boolean isEmpty();

    public void setContainerUri(String containerUri);

    public String getContainerUri();

    public void setType(String containerType);

    public void setDataSetUri(String dataSetUri);

    public void setKey(AttributeValueKey key);

}
