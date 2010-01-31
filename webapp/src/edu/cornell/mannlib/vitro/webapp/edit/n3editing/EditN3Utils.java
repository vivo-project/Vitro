/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.RoleIdentifier;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ServletIdentifierBundleFactory;
import edu.cornell.mannlib.vitro.webapp.filters.VitroRequestPrep;

public class EditN3Utils {

    public static String getEditorUri(HttpServletRequest request, HttpSession session, ServletContext context){
        String editorUri = "Unknown N3 Editor";
        boolean selfEditing = VitroRequestPrep.isSelfEditing(request);
        IdentifierBundle ids =
            ServletIdentifierBundleFactory.getIdBundleForRequest(request,session,context);           
        
        if( selfEditing )
            editorUri = SelfEditingIdentifierFactory.getSelfEditingUri(ids);
        else
            editorUri = RoleIdentifier.getUri(ids);
        
        return editorUri;        
    }
}
