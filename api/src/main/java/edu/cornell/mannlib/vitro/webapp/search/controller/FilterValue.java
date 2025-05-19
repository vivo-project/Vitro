package edu.cornell.mannlib.vitro.webapp.search.controller;

import org.apache.jena.rdf.model.RDFNode;

public class FilterValue {

    private String id;
    private String name = "";
    private int order;

    private long count;

    private boolean selected = false;

    private boolean isDefaultValue;

    private boolean display = false;

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean value) {
        display = value;
    }

    public FilterValue(String id) {
        this.id = id;
    }

    public boolean isDefault() {
        return isDefaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String label) {
        this.name = label;
    }

    public void setName(RDFNode rdfNode) {
        if (rdfNode != null) {
            name = rdfNode.asLiteral().getLexicalForm();
        }
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(RDFNode rdfNode) {
        if (rdfNode != null) {
            order = rdfNode.asLiteral().getInt();
        }
    }

    public String getId() {
        return id;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setSelected(boolean value) {
        this.selected = value;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setDefault(boolean b) {
        isDefaultValue = b;
    }
}
