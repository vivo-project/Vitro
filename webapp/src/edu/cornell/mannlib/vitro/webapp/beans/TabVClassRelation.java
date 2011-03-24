package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class TabVClassRelation {

    private int id      = -1;
    private int tabId   = -1;
    private String  vClassURI= null;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getTabId() {
        return tabId;
    }
    public void setTabId(int tabId) {
        this.tabId = tabId;
    }

    public String getVClassURI() {
        return vClassURI;
    }
    public void setVClassURI(String vClassURI) {
        this.vClassURI = vClassURI;
    }

}
