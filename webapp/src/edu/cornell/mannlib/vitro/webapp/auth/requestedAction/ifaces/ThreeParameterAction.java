package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */


/**
 * A base class for actions that work with a triple.
 *
 * @author bdc34
 *
 */
public abstract class ThreeParameterAction implements RequestedAction{

    //TODO: these should not be public
    public String uriOfSubject;
    public String uriOfObject;
    public String uriOfPredicate;

    public String getUriOfObject() {
        return uriOfObject;
    }
    public void setUriOfObject(String uriOfObject) {
        this.uriOfObject = uriOfObject;
    }
    public String getUriOfPredicate() {
        return uriOfPredicate;
    }
    public void setUriOfPredicate(String uriOfPredicate) {
        this.uriOfPredicate = uriOfPredicate;
    }
    public String getUriOfSubject() {
        return uriOfSubject;
    }
    public void setUriOfSubject(String uriOfSubject) {
        this.uriOfSubject = uriOfSubject;
    }
}
