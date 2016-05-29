/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.MultiValueEditSubmission;

public interface EditSubmissionVTwoPreprocessor {
	//certain preprocessors might require the vreq - which should be passed at the time this method is executed
    public void preprocess(MultiValueEditSubmission editSubmission, VitroRequest vreq);
}
