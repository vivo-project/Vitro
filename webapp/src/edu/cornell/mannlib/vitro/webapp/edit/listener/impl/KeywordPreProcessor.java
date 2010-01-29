package edu.cornell.mannlib.vitro.webapp.edit.listener.impl;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.listener.EditPreProcessor;
import edu.cornell.mannlib.vitro.webapp.beans.Keyword;

public class KeywordPreProcessor implements EditPreProcessor {

    private String stemStr = null;

    public KeywordPreProcessor(String stem) {
        this.stemStr = stem;
    }

    public void process(Object o, EditProcessObject epo) {
        try {
            ((Keyword) o).setStem(stemStr);
        } catch (ClassCastException e) {}
    }

}
