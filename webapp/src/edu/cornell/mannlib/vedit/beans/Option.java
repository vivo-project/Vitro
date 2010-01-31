/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

import java.io.Serializable;

public class Option implements Serializable {

    private String value = null;
    private String body = null;
    private boolean selected = false;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean getSelected (){
        return selected;
    }

    public void setSelected (boolean selected){
        this.selected = selected;
    }

    //default constructor
    public Option() {
    }
    
    public Option (String value, String body, boolean selected) {
        this.value = value;
        this.body = body;
        this.selected = selected;
    }

    // construct an Option with body and value
    public Option(String value, String body) {
        this(value, body, false);
    }

    // construct an Option with equal body and value
    public Option (String name){
    	this(name, name, false);
    }



}
