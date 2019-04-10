package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;
import java.util.concurrent.Callable;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class DataPropertyTemplateModelCallable 
implements Callable<DataPropertyTemplateModel> {

	final DataProperty dp;
	Individual subject;
	final VitroRequest vreq;
	final boolean editing;
	final List<DataProperty> populatedDataPropertyList;
	final String callableName;
	
	
	public DataPropertyTemplateModelCallable(DataProperty dp, Individual subject, VitroRequest vreq,
			boolean editing, List<DataProperty> populatedDataPropertyList,	String callableName) {
		super();
		this.dp = dp;
		this.subject = subject;
		this.vreq = vreq;
		this.editing = editing;
		this.populatedDataPropertyList = populatedDataPropertyList;
		this.callableName = callableName;
	}

	@Override
	public DataPropertyTemplateModel call() throws Exception {
		DataPropertyTemplateModel dptm = new DataPropertyTemplateModel(dp, subject, vreq, editing, populatedDataPropertyList);
		return dptm;
		
	}

}
