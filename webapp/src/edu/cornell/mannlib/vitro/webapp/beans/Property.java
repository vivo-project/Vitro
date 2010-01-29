package edu.cornell.mannlib.vitro.webapp.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Property extends BaseResourceBean {
    
    private static Log log = LogFactory.getLog( Property.class );

    private String  customEntryForm       = null;
    private String  groupURI              = null;
    private String  editLabel             = null; // keep so can set in a context-specific way
    private final boolean subjectSide     = true; // only relevant to ObjectProperty
    
    public Property() {
        this.groupURI              = null;
        this.editLabel             = null;
    }

    public String getCustomEntryForm() {
        return customEntryForm;
    }
    
    public void setCustomEntryForm(String s) {
        this.customEntryForm = s;
    }
    
	public String getGroupURI() { 
		return groupURI;
	}
	public void setGroupURI(String in) {
		this.groupURI = in;
	}
	
    public String getEditLabel() {
        return editLabel;
    }
    public void setEditLabel(String label) {
        this.editLabel = label;
    }
    
    public boolean isSubjectSide() {
        return subjectSide;
    }
    
    /**
     * Sorts Property objects, by property rank, then alphanumeric.
     * @author bdc34
     */
    public static class DisplayComparatorIgnoringPropertyGroup implements Comparator {
        
        public int compare(Object o1, Object o2) {
            //log.warn("starting property display comparator; ignoring group ");
            Property p1 = o1 == null ? null : (Property) o1;
            Property p2 = o2 == null ? null : (Property) o2;
            if (p1==null || p2==null) {
                log.error("Null property passed to DisplayComparatorIgnoringPropertyGroup");
                return 0;
            }
            //log.warn("comparing property "+p1.getLocalName()+" (rank "+determineDisplayRank(p1)+") to property "+p2.getLocalName()+" (rank "+determineDisplayRank(p2)+") ...");
            int diff = determineDisplayRank(p1) - determineDisplayRank(p2);
            if (diff==0) {
                String p1Str = p1.getEditLabel() == null ? p1.getURI() : p1.getEditLabel();
                if (p1.getEditLabel()==null) {
                    log.warn("null edit label for property "+p1.getURI());
                }
                String p2Str = p2.getEditLabel() == null ? p2.getURI() : p2.getEditLabel();
                if (p2.getEditLabel() == null) {
                    log.warn("null edit label for property "+p2.getURI());
                }
                return p1Str.compareTo(p2Str);
            }
            return diff;
        }
        
        private int determineDisplayRank(Property p) {
            if (p instanceof DataProperty) {
                DataProperty dp = (DataProperty)p;
                return dp.getDisplayTier();
            } else if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty)p;
                String tierStr = p.isSubjectSide() ? op.getDomainDisplayTier() : op.getRangeDisplayTier();
                try {
                    return Integer.parseInt(tierStr);
                } catch (NumberFormatException ex) {
                    log.error("Cannot decode object property display tier value "+tierStr+" as an integer");
                }
            } else if (p instanceof KeywordProperty) {
                KeywordProperty kp = (KeywordProperty)p;
                return kp.getDisplayRank();
            } else {
                log.error("Property is of unknown class in PropertyRanker()");  
            }
            return 0;
        }
    }
}
