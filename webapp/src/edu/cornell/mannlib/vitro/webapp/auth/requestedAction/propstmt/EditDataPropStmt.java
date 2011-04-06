/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.requestedAction.propstmt;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;

/** Should we allow the user to edit this DataPropertyStatement? */
public class EditDataPropStmt extends AbstractDataPropertyAction {

    private final DataPropertyStatement dataPropStmt;
    
    public EditDataPropStmt(DataPropertyStatement dps){
    	super(dps.getIndividualURI(), dps.getDatapropURI());
        this.dataPropStmt = dps;
    }
    
    public String data(){ return dataPropStmt.getData(); }
    public String lang(){ return dataPropStmt.getLanguage(); }
    public String datatype(){return dataPropStmt.getDatatypeURI(); }
    
}
