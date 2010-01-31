/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

public class Checkbox {

    private String name = null;
    private String value = null;
    private String body = null;
    private boolean checked = false;

    public String getName(){
        return name;
    }

    private void setName(String name){
        this.name = name;
    }

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

    public boolean getChecked (){
        return checked;
    }

    public void setChecked (boolean checked){
        this.checked = checked;
    }

}
