package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.text.Collator;

public class KeywordProperty extends Property implements Comparable<KeywordProperty> {
    public final static int DEFAULT_KEYWORDS_DISPLAY_RANK = 99;
    
    private String displayLabel = null;
    private int    displayRank  = 0;
    
    public KeywordProperty(String displayText,String editText,int rank, String groupUri) {
        super();
        this.setDisplayLabel(displayText);
        this.setEditLabel(editText);
        this.setDisplayRank(rank);
        this.setGroupURI(groupUri);
        this.setLocalName("keywords");
    }

    public String getDisplayLabel() {
        return displayLabel;
    }
    public void setDisplayLabel(String label) {
        displayLabel = label;
    }
    
    public int getDisplayRank() {
        return displayRank;
    }
    public void setDisplayRank(int rank) {
        displayRank = rank;
    }
    
    /**
     * Sorts alphabetically by non-public name
     */
    public int compareTo (KeywordProperty kp) {
        Collator collator = Collator.getInstance();
        return collator.compare(this.getDisplayLabel(),(kp).getDisplayLabel());
    }

}
