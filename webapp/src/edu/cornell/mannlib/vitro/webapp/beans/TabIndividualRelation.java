package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class TabIndividualRelation {

    private String uri      = null;
    private int tabId       = -1;
    private String entURI   = null;
    private int displayRank = -1;

    public String getURI() {
        return uri;
    }
    public void setURI(String URI) {
        this.uri = URI;
    }

    public int getTabId() {
        return tabId;
    }
    public void setTabId(int tabId) {
        this.tabId = tabId;
    }

    public String getEntURI() {
        return entURI;
    }
    public void setEntURI(String entURI) {
        this.entURI = entURI;
    }

    public int getDisplayRank() {
        return displayRank;
    }
    public void setDisplayRank(int displayRank) {
        this.displayRank = displayRank;
    }

}
