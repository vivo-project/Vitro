/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;

public class EditSubmissionUtils {

    protected static final String MULTI_VALUED_EDIT_SUBMISSION = "MultiValueEditSubmission";
    
    /* *************** Static utility methods to get EditSub from Session *********** */

    public static MultiValueEditSubmission getEditSubmissionFromSession(HttpSession sess, EditConfigurationVTwo editConfig){
        Map<String,MultiValueEditSubmission> submissions = 
            (Map<String,MultiValueEditSubmission>)sess.getAttribute(MULTI_VALUED_EDIT_SUBMISSION);
        if( submissions == null )
          return null;
        if( editConfig != null )
            return submissions.get(  editConfig.getEditKey() ); //this might be null
        else
            return null;
    }

    public static void putEditSubmissionInSession(HttpSession sess, MultiValueEditSubmission editSub){
        Map<String,MultiValueEditSubmission> submissions = (Map<String,MultiValueEditSubmission>)sess.getAttribute(MULTI_VALUED_EDIT_SUBMISSION);
        if( submissions == null ){
            submissions = new HashMap<String,MultiValueEditSubmission>();
            sess.setAttribute(MULTI_VALUED_EDIT_SUBMISSION,submissions);
        }
        submissions.put(editSub.editKey, editSub);
    }


    public static void clearEditSubmissionInSession(HttpSession sess, MultiValueEditSubmission editSub){
        if( sess == null) return;
        if( editSub == null ) return;
        Map<String,MultiValueEditSubmission> submissions = (Map<String,MultiValueEditSubmission>)sess.getAttribute(MULTI_VALUED_EDIT_SUBMISSION);
        if( submissions == null ){
            throw new Error("MultiValueEditSubmission: could not get a Map of MultiValueEditSubmission from the session.");
        }

        submissions.remove( editSub.editKey );
    }

    public static void clearAllEditSubmissionsInSession(HttpSession sess ){
        if( sess == null) return;
        sess.removeAttribute(MULTI_VALUED_EDIT_SUBMISSION);
    }

    public static Map<String, String[]> convertParams(
            Map<String, List<String>> queryParameters) {
        HashMap<String,String[]> out = new HashMap<String,String[]>();
        for( String key : queryParameters.keySet()){
            List item = queryParameters.get(key);            
            out.put(key, (String[])item.toArray(new String[item.size()]));
        }
        return out;
    }                 
}
