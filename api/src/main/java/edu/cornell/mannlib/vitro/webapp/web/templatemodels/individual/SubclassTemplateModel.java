/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

public class SubclassTemplateModel extends BaseTemplateModel implements Comparable<SubclassTemplateModel> { 
    
    private final VClass vclass;
    private final List<ObjectPropertyStatementTemplateModel> statements;
    
    SubclassTemplateModel(VClass vclass, List<ObjectPropertyStatementTemplateModel> statements) {
        // NB vclass may be null. If the statements don't belong to any subclass, a dummy SubclassTemplateModel
        // is created with a null vclass, so that the data can be presented in a uniform way to the template.
        this.vclass = vclass; 
        this.statements = statements;
    }

    @Override
    public int compareTo(SubclassTemplateModel other) {
        
        if (other == null) {
            return -1;
        }
        
        VClass vclassOther = other.getVClass();
        if (vclass == null) {
            return vclassOther == null ? 0 : 1;
        }
        if (vclassOther == null) {
            return -1;
        }
        
        int rank = vclass.getDisplayRank();
        int rankOther = vclassOther.getDisplayRank();
        
        int intCompare = 0;
        // Values < 1 are undefined and go at end, not beginning
        if (rank < 1) {
            intCompare = rankOther < 1 ? 0 : 1;
        } else if (rankOther < 1) {
            intCompare = -1;
        } else {           
            intCompare = ((Integer)rank).compareTo(rankOther);
        }

        if (intCompare != 0) {
            return intCompare;        
        }
        
        // If display ranks are equal, sort by name     
        String name = getName();
        String nameOther = vclassOther.getName();
               
        if (name == null) {
            return nameOther == null ? 0 : 1;
        } 
        if (nameOther == null) {
            return -1;
        }
        return name.compareToIgnoreCase(nameOther);

    }
    
    protected VClass getVClass() {
        return vclass;
    }
    
    /* Accessor methods for templates */
    
    public String getName() {
        return vclass == null ? "" : vclass.getName();
    }
    
    public List<ObjectPropertyStatementTemplateModel> getStatements() {
        return statements;
    }
    
}
