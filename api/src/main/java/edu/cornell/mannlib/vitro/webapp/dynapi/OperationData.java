package edu.cornell.mannlib.vitro.webapp.dynapi;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class OperationData {

	private final Map<String, String[]> params;
	private final ServletContext context;
	private final String method;

	public OperationData(HttpServletRequest request) {
		params = request.getParameterMap();
		context = request.getServletContext();
		method = request.getMethod();
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

	public String getMethod() {
		return method;
	}

}
