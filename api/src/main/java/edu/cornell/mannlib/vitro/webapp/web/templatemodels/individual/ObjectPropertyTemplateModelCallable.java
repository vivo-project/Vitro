package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.concurrent.Callable;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class ObjectPropertyTemplateModelCallable implements Callable<ObjectPropertyTemplateModel> {
    private final ObjectProperty op;
    private Individual subject;
    private final VitroRequest vreq;
    private final boolean editing;
    private final List<ObjectProperty> populatedObjectPropertyList;

    public ObjectPropertyTemplateModelCallable(ObjectProperty op, Individual subject, VitroRequest vreq,
            boolean editing, List<ObjectProperty> populatedObjectPropertyList) {
        super();
        this.op = op;
        this.subject = subject;
        this.vreq = vreq;
        this.editing = editing;
        this.populatedObjectPropertyList = populatedObjectPropertyList;
    }

    @Override
    public ObjectPropertyTemplateModel call() {

        ObjectPropertyTemplateModel tm = ObjectPropertyTemplateModel.getObjectPropertyTemplateModel(op, subject, vreq,
                editing, populatedObjectPropertyList);

        if (!tm.isEmpty() || (editing && !tm.getAddUrl().isEmpty())) {
            tm.getStatementData();
        }

        return tm;

    }

}
