package edu.cornell.mannlib.vitro.webapp.auth.attributes;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MutableAttributeValueSet implements AttributeValueSet {

    private Set<String> values = ConcurrentHashMap.newKeySet();
    private String valueSetUri;
    private String type;
    private AttributeValueKey key;
    private String dataSetUri;

    public void setValueSetUri(String valueSetUri) {
        this.valueSetUri = valueSetUri;
    }

    public MutableAttributeValueSet(String value) {
        values.add(value);
    }

    @Override
    public String getValueSetUri() {
        return valueSetUri;
    }

    @Override
    public void add(String value) {
        if (value == null) {
            return;
        }
        values.add(value);
    }

    @Override
    public boolean contains(String value) {
        if (value == null) {
            return false;
        }
        return values.contains(value);
    }

    @Override
    public boolean containsSingleValue() {
        return values.size() == 1;
    }

    @Override
    public String getSingleValue() {
        Iterator<String> iterator = values.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return "";
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public void remove(String value) {
        values.remove(value);
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public void setKey(AttributeValueKey valueSetKey) {
        this.key = valueSetKey;
    }

    @Override
    public void setDataSetUri(String dataSetUri) {
        this.dataSetUri = dataSetUri;
    }

}
