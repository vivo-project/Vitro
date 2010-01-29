package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */


// TODO bjl23 this is the link method, nothing to do here, just drawing your attention to it.

public class Link extends BaseResourceBean {
    private String url = null;
    private String anchor = null;
    private String entityURI = null;
    private String typeURI = null;
    private String displayRank = "-1";
    private ObjectPropertyStatement objectPropertyStatement = null;

    public String getAnchor() {
        return anchor;
    }
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }
    public String getEntityURI() {
        return entityURI;
    }
    public void setEntityURI(String entityURI) {
        this.entityURI = entityURI;
    }
    public String getTypeURI() {
        return typeURI;
    }
    public void setTypeURI(String typeURI) {
        this.typeURI = typeURI;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getDisplayRank() {
        return displayRank;
    }
    
    public void setDisplayRank(int rank) {
        this.displayRank = String.valueOf(rank);
    }
    public void setDisplayRank(String rank) {
        this.displayRank = rank;
    /*  try {
            this.displayRank = Integer.parseInt(rank);
        } catch (NumberFormatException ex) {
            this.displayRank = 10;
        } */
    }

    public ObjectPropertyStatement getObjectPropertyStatement() {
        return objectPropertyStatement;
    }
    public void setObjectPropertyStatement(ObjectPropertyStatement op) {
        this.objectPropertyStatement = op;
    }
}
