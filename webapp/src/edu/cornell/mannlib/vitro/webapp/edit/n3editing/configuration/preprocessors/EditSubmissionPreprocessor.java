/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.preprocessors;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.EditSubmission;

public interface EditSubmissionPreprocessor {
    public void preprocess(EditSubmission editSubmission);
}
