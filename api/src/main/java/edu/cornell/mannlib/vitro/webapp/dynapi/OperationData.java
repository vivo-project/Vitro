package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class OperationData {

	private final Map<String, String[]> params;
	private final ServletContext context;

	public OperationData(HttpServletRequest request) {
		params = request.getParameterMap();
		context = request.getServletContext();
	}

	public ServletContext getContext() {
		return context;
	}

	public boolean has(String paramName) {
		return params.containsKey(paramName);
	}

	public String[] get(String paramName) {
		return params.get(paramName);
	}

}
