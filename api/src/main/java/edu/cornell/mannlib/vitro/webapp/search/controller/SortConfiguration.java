package edu.cornell.mannlib.vitro.webapp.search.controller;

import static edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order.ASC;
import static edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order.DESC;

import java.util.Locale;

import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery.Order;

public class SortConfiguration {

    private String id = "";
    private String field = "";
    private boolean multilingual = false;
    private Order sortDirection = DESC;
    private boolean selected = false;
    private String label = "";
    private int rank = 0;
    private String fallback;
    private boolean displayed = false;

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

    public Order getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(boolean isAscending) {
        if (isAscending) {
            sortDirection = ASC;
        } else {
            sortDirection = DESC;
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String id) {
        this.fallback = id;
    }

    public void setDisplayed(boolean display) {
        this.displayed = display;
    }

    public boolean isDisplayed() {
        return displayed;
    }
}
