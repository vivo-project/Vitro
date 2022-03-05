package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultAction extends Action {

 	private static final Log log = LogFactory.getLog(DefaultAction.class);

	public OperationResult run(Map<String, String[]> map) {
		for (Map.Entry<String, String[]> entry : map.entrySet()) {
			log.debug("param name " + entry.getKey() );
			String[] values = entry.getValue();
			for (String value: values) {
				log.debug("param value " + value);
			}
		}

		return OperationResult.badRequest();
	}
}
