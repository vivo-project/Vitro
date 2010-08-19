/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vedit.beans;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class DynamicField {

    private String name = null;

    private String table = null;

    private int maxCardinality = 1;
    private int minCardinality = -1;
    private int visible = -1;

    private List<DynamicFieldRow> rowList = null;
    private DynamicFieldRow rowTemplate = null;

    private HashMap metadata = new HashMap();

    private Boolean deleteable = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public void setMaxCardinality (int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public int getMinCardinality () {
        return minCardinality;
    }

    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    public int getVisible() {
        return visible;
    }

    public void setVisible(int visible) {
        this.visible = visible;
    }

    public boolean getDeleteable() {
        return deleteable;
    }

    public void setDeleteable(boolean deleteable) {
        this.deleteable = deleteable;
    }

    public HashMap getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap metadata) {
        this.metadata = metadata;
    }

    public List<DynamicFieldRow> getRowList() {
        return rowList;
    }

    public void setRowList (List<DynamicFieldRow> rowList) {
        this.rowList = rowList;
    }

    public DynamicFieldRow getRowTemplate() {
        return rowTemplate;
    }

    public void setRowTemplate(DynamicFieldRow dfr) {
        rowTemplate = dfr;
    }

}
