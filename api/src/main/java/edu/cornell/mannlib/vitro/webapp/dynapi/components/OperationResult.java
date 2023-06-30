package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Range;

public class OperationResult {

	private int responseCode;
	private static Range<Integer> errors = Range.between(400, 599);

	public OperationResult(int responseCode) {
		this.responseCode = responseCode;
	}

	public boolean hasError() {
		if (errors.contains(responseCode)) {
			return true;
		}
		return false;
	}

	public void prepareResponse(HttpServletResponse response) {
		response.setStatus(responseCode);
	}

	public static OperationResult badRequest() {
		return new OperationResult(HttpServletResponse.SC_BAD_REQUEST);
	}

	public static OperationResult notFound() {
		return new OperationResult(HttpServletResponse.SC_NOT_FOUND);
	}

	public static OperationResult methodNotAllowed() {
		return new OperationResult(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	public static OperationResult internalServerError() {
		return new OperationResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

}
