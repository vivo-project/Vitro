/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans;

public class DisplayConfig extends ApplicationConfig {
    String displayName;
    Integer displayRank;
    Integer displayLimit;
    Boolean supressDisplay;
    
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }  
    
    public boolean getSupressDisplay() {
        if( supressDisplay == null )
            return false;
        else
            return supressDisplay;
    }
    public void setSupressDisplay(Boolean supressDisplay) {
        this.supressDisplay = supressDisplay;
    }
    public Boolean getSupressDisplayBoolean(){
        return supressDisplay;
    }
    
    public int getDisplayRank() {
        return displayRank != null? displayRank:-1;
    }
    public Integer getDisplayRankInteger(){
        return displayRank;
    }
    public void setDisplayRank(Integer displayRank) {
        this.displayRank = displayRank;
    }
    
    public int getDisplayLimit() {
        return displayLimit != null? displayLimit:-1;        
    }    
    public Integer getDisplayLimitInteger(){
        return displayLimit;
    }
    public void setDisplayLimit(Integer displayLimit) {
        this.displayLimit = displayLimit;
    }  
    
}
