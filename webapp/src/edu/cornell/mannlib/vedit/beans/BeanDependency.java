package edu.cornell.mannlib.vedit.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

public class BeanDependency {

    private Object bean;
    private String nearKey;
    private String farKey;

    public Object getBean(){
        return bean;
    }

    public void setBean(Object bean){
        this.bean = bean;
    }

    public String getNearKey(){
        return nearKey;
    }

    public void setNearKey (String nearKey){
        this.nearKey = nearKey;
    }

    public String getFarKey(){
        return farKey;
    }

    public void setFarKey(String farKey){
        this.farKey = farKey;
    }

}
