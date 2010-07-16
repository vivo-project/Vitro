/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing;

public abstract class BaseEditSubmissionPreprocessor implements
        EditSubmissionPreprocessor {

    protected EditConfiguration editConfiguration;
    
    public BaseEditSubmissionPreprocessor(EditConfiguration editConfig) {
        editConfiguration = editConfig;
    }
    

}
