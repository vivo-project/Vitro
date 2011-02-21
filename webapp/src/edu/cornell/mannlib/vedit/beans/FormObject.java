/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormObject implements Serializable {

    private HashMap<String, String> values = new HashMap<String, String>();
    private HashMap<String, List<Option>> optionLists = new HashMap<String, List<Option>>();
    private HashMap<String, List<Checkbox>> checkboxLists = new HashMap<String, List<Checkbox>>();
    private Map<String, String> errorMap = new HashMap<String, String>();
    private List<DynamicField> dynamicFields = new ArrayList<DynamicField>();

    public HashMap<String, String> getValues(){
        return values;
    }

    public void setValues(HashMap<String, String> values){
        this.values = values;
    }

    public String valueByName(String name){
        return values.get(name);
    }

    public HashMap<String, List<Option>> getOptionLists() {
        return optionLists;
    }

    public void setOptionLists(HashMap<String, List<Option>> optionLists) {
        this.optionLists = optionLists;
    }

    public List<Option> optionListByName(String key){
        return optionLists.get(key);
    }

    public HashMap<String, List<Checkbox>> getCheckboxLists(){
        return checkboxLists;
    }

    public Map<String, String> getErrorMap(){
        return errorMap;
    }

    public void setErrorMap(Map<String, String> errorMap){
        this.errorMap = errorMap;
    }

    public List<DynamicField> getDynamicFields() {
        return dynamicFields;
    }

    public void setDynamicFields(List<DynamicField> dynamicFields){
        this.dynamicFields = dynamicFields;
    }

}
