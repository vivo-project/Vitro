/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.beans;

import java.text.Collator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyGroup extends BaseResourceBean implements Comparable<PropertyGroup> {
    
    private static final Log log = LogFactory.getLog(PropertyGroup.class.getName());
    
	private int displayRank;
	private String name;
	private List<Property> propertyList;
	private int statementCount;
	private String publicDescription;
	
	public int getDisplayRank() {
		return this.displayRank;
	}
	
	public void setDisplayRank(int in) {
		this.displayRank = in;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String in) {
		this.name = in;
	}
	
	public List<Property> getPropertyList() {
		return this.propertyList;
	}
	
	public void setPropertyList(List<Property> in) {
		this.propertyList = in;
	}
	
	public int getStatementCount() {
	    return statementCount;
	}
	
	public void setStatementCount(int count) {
	    statementCount = count;
	}
	
	public String getPublicDescription() {
	    return publicDescription;
	}
	
	public void setPublicDescription(String s) {
	    publicDescription = s;
	}
			


    /**
     * Sorts PropertyGroup objects by group rank, then alphanumeric.
     * @author bdc34 modified by jc55, bjl23
     */
    public int compareTo(PropertyGroup o2) {
    	Collator collator = Collator.getInstance();
        if (o2 == null) {
            log.error("object NULL in DisplayComparator()");
            return 0;
        }
        int diff = (this.getDisplayRank() - o2.getDisplayRank());
        if (diff == 0 ) {
            return collator.compare(this.getName(),o2.getName());
        }
        return diff;
    }
	
}
