package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.util.Locale;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;

public class SortConfiguration {

    private String id = "";
    private String field = "";
    private boolean multilingual = false;
    private boolean ascOrder = false;
    private boolean selected = false;
    private String label = "";
    private int order = 0;
    private String fallback;
    private boolean display = false;

    public SortConfiguration(String id, String label, String field) {
        this.id = id;
        this.setLabel(label);
        this.field = field;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public boolean isMultilingual() {
        return multilingual;
    }

    public void setMultilingual(boolean multilingual) {
        this.multilingual = multilingual;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getField(Locale locale) {
        String languageTag = locale.toLanguageTag();
        if (multilingual) {
            return languageTag + field;
        }
        return field;
    }

    public Order getSortOrder() {
        if (ascOrder) {
            return Order.ASC;
        }
        return Order.DESC;
    }

    public void setAscOrder(boolean ascOrder) {
        this.ascOrder = ascOrder;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String id) {
        this.fallback = id;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public boolean isDisplay() {
        return display;
    }
}
