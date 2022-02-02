package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class OperationData {

	private Map<String, String[]> params;

	public OperationData(HttpServletRequest request) {
		params = request.getParameterMap();
	}

	public Map<String, String[]> getParams() {
		return params;
	}

}
