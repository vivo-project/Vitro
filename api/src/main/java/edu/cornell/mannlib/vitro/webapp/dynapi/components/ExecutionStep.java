package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.ProcessInput;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ExecutionStep implements Step{

 	private static final Log log = LogFactory.getLog(ExecutionStep.class);

 	Template template = null;
 	
	@Override
	public void dereference() {
		if (template != null) {
			template.dereference();
			template = null;
		}
	}
	
	public void setTemplate(Template template) {
		this.template = template;
	}

	public ProcessResult run(ProcessInput input) {
		log.debug("Processing in STEP");
		ProcessResult result = new ProcessResult(HttpServletResponse.SC_NOT_IMPLEMENTED);
		return result;
	}

}
