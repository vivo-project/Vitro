/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

/**
 * a class representing a namespace for URI construction
 * @author bjl23
 *
 */
public class Namespace {

    private int     id              =   -1;
    private String  name            = null;
    private String  namespaceURI    = null;
    private String  prefix          = null;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }
    public void setNamespaceURI(String nsuri) {
        this.namespaceURI = nsuri;
    }

    public String getPrefix() {
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}
