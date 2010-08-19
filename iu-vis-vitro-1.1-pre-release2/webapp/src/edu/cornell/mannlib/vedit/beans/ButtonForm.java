/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;
import java.util.HashMap;

public class ButtonForm {
    private String action = "";
    private String label  = "no label specified";
    private String cssClass = null;
    private HashMap<String,String> params = null;
    
    public ButtonForm() {
        action = ""; // submits to same page
        cssClass = null;
        label = "no label specified";
        params = null;
    }
    
    public ButtonForm(String actionStr, String classStr, String labelStr, HashMap<String,String> paramMap) {
        action = actionStr;
        cssClass = classStr; // can be null
        label = labelStr;
        params = paramMap;
    }
    
    public String getAction(){
        return action;
    }
    public void setAction(String s){
        action = s;
    }
    
    public String getLabel(){
        return label;
    }
    public void setLabel(String s){
        label = s;
    }
    
    public String getCssClass(){
        if (cssClass==null){
            return "";
        }
        return "class=\""+cssClass+"\"";
    }
    public void setCssClass(String s){
        cssClass=s;
    }
    
    public HashMap<String,String> getParams(){
        return params;
    }
    public void setParams(HashMap<String,String> p){
        params = p;
    }
    public void addParam(String key, String value){
        if (params==null){
            params = new HashMap<String,String>();
        }
        params.put(key, value);
    }
}
