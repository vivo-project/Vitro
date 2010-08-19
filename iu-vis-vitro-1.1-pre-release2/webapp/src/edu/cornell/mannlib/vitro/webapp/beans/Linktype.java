/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

public class Linktype extends BaseResourceBean {

    private String  type    = null;
    private String  generic = null;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getGeneric() {
        return generic;
    }
    public void setGeneric(String generic) {
        this.generic = generic;
    }

}
