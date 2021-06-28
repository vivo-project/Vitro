/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.concurrent.Callable;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class DataPropertyTemplateModelCallable implements Callable<PropertyTemplateModel> {

    private final DataProperty dp;
    private Individual subject;
    private final VitroRequest vreq;
    private final boolean editing;
    private final List<DataProperty> populatedDataPropertyList;

    public DataPropertyTemplateModelCallable(DataProperty dp, Individual subject, VitroRequest vreq, boolean editing,
            List<DataProperty> populatedDataPropertyList) {
        super();
        this.dp = dp;
        this.subject = subject;
        this.vreq = vreq;
        this.editing = editing;
        this.populatedDataPropertyList = populatedDataPropertyList;
    }

    @Override
    public DataPropertyTemplateModel call() {
        return new DataPropertyTemplateModel(dp, subject, vreq, editing, populatedDataPropertyList);
    }

}
