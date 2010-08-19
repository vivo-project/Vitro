/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.text.Collator;
import java.util.Date;

public class User implements Comparable {
    
    public final static int MIN_PASSWORD_LENGTH = 5;
    public final static int MAX_PASSWORD_LENGTH = 99;

    private String URI = null;
    private String namespace = null;
    private String localName = null;
    private String username = null;
    private String oldPassword = null;
    private String md5password = null;
    private Date modTime = null;
    private Date firstTime = null;
    private int loginCount = 0;
    private String roleURI = null;
    private String lastName = null;
    private String firstName = null;

    public String getURI() {
        return URI;
    }
    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getLocalName() {
        return localName;
    }
    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldPassword() {
        return oldPassword;
    }
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getMd5password() {
        return md5password;
    }
    public void setMd5password(String md5password) {
        this.md5password = md5password;
    }

    public Date getModTime() {
        return modTime;
    }
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    public Date getFirstTime() {
        return firstTime;
    }
    public void setFirstTime(Date firstTime) {
        this.firstTime = firstTime;
    }

    public int getLoginCount() {
        return loginCount;
    }
    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public String getRoleURI() {
        return roleURI;
    }
    public void setRoleURI(String roleURI) {
        this.roleURI = roleURI;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int compareTo(Object o) {
        Collator collator = Collator.getInstance();
        return collator.compare(this.getUsername(),((User)o).getUsername());
    }

}
