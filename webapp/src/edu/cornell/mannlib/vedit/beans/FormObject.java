package edu.cornell.mannlib.vedit.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import edu.cornell.mannlib.vedit.beans.Option;

public class FormObject implements Serializable {

    private HashMap values = new HashMap();
    private HashMap optionLists = new HashMap();
    private HashMap checkboxLists = new HashMap();
    private HashMap errorMap = new HashMap();
    private List dynamicFields = null;

    public HashMap getValues(){
        return values;
    }

    public void setValues(HashMap values){
        this.values = values;
    }

    public String valueByName(String name){
        return (String) values.get(name);
    }

    public HashMap getOptionLists() {
        return optionLists;
    }

    public void setOptionLists(HashMap optionLists) {
        this.optionLists = optionLists;
    }

    public List optionListByName(String key){
        return (List) optionLists.get(key);
    }

    public HashMap getCheckboxLists(){
        return checkboxLists;
    }

    public HashMap getErrorMap(){
        return errorMap;
    }

    public void setErrorMap(HashMap errorMap){
        this.errorMap = errorMap;
    }

    public List getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(List dynamicFields){
        this.dynamicFields = dynamicFields;
    }

}
