package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import javax.servlet.http.HttpServletResponse;

public class ProcessResult {
	
	private int responseCode;

	public ProcessResult(int responseCode) {
		this.responseCode = responseCode;
	}

	public void prepareResponse(HttpServletResponse response) {
		response.setStatus(responseCode);
	}
	
}
