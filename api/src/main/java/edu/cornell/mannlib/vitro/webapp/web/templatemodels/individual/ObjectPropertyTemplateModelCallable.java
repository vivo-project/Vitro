package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class ObjectPropertyTemplateModelCallable 
implements Callable<ObjectPropertyTemplateModel> 
 {
	final ObjectProperty op;
	Individual subject;
	final VitroRequest vreq;
	final boolean editing;
	final List<ObjectProperty> populatedObjectPropertyList;
	final String callableName;
	
	public ObjectPropertyTemplateModelCallable(ObjectProperty op, Individual subject, VitroRequest vreq,
			boolean editing, List<ObjectProperty> populatedObjectPropertyList,	String callableName) {
		super();
		this.op = op;
		this.subject = subject;
		this.vreq = vreq;
		this.editing = editing;
		this.populatedObjectPropertyList = populatedObjectPropertyList;
		this.callableName = callableName;
	}


	@Override
	public ObjectPropertyTemplateModel call() throws Exception {

		ObjectPropertyTemplateModel tm = 
		 ObjectPropertyTemplateModel.getObjectPropertyTemplateModel(
                op, subject, vreq, editing, populatedObjectPropertyList);
		
		if (!tm.isEmpty() || (editing && !tm.getAddUrl().isEmpty())) {
			List<Map<String, String>> l = tm.getStatementData();
       }

		return tm;
		
	}

}
