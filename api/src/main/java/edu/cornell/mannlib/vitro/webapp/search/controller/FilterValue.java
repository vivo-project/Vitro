package edu.cornell.mannlib.vitro.webapp.search.controller;

import org.apache.jena.rdf.model.RDFNode;

public class FilterValue {

    private String id;
    private String name = "";
    private int rank;
    private long count;
    private boolean selected = false;
    private boolean isDefaultValue;
    private boolean displayed = false;

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean value) {
        displayed = value;
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

    public Integer getRank() {
        return rank;
    }

    public void setRank(RDFNode rdfNode) {
        if (rdfNode != null) {
            rank = rdfNode.asLiteral().getInt();
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

    public boolean isSelected() {
        return selected;
    }

    public void setDefault(boolean b) {
        isDefaultValue = b;
    }
}
