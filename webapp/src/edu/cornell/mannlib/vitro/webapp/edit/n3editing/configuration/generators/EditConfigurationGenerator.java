package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.EditConfiguration;

public interface EditConfigurationGenerator {
    public EditConfiguration getEditConfiguration( VitroRequest vreq, HttpSession session );
}
