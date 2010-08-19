/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

public class Keyword {

    private int     id      = -1;
    private String  term    = null;
    private String  stem    = null;
    private String  type    = null;
    private String  source  = null;
    private String  comments= null;
    private String  origin  = null;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }

    public String getStem() {
        return stem;
    }
    public void setStem(String stem) {
        this.stem = stem;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }

    public String getComments() {
        return comments;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getOrigin() {
        return origin;
    }
    public void setOrigin(String origin) {
        this.origin = origin;
    }

}
