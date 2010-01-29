package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class KeywordIndividualRelation extends BaseResourceBean {

    private     int     keyId   = -1;
    private     String  entURI  = null;
    private     String  mode    = "visible";

    public int getKeyId() {
        return keyId;
    }
    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public String getEntURI() {
        return entURI;
    }
    public void setEntURI(String entURI) {
        this.entURI = entURI;
    }

    public String getMode() {
        return mode;
    }
    public void setMode(String mode) {
        this.mode = mode;
    }

}
