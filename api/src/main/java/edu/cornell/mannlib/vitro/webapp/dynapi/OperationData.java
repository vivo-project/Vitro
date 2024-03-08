package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class OperationData {

	private Map<String, String[]> params;
	private ServletContext context;

	public OperationData(HttpServletRequest request) {
		params = request.getParameterMap();
		context = request.getServletContext();
	}

	public ServletContext getContext() {
		return context;
	}

	public boolean has(String paramName) {
		if (params.containsKey(paramName)) {
			return true;
		}
		return false;
	}

	public String[] get(String paramName) {
		return params.get(paramName);
	}

}
