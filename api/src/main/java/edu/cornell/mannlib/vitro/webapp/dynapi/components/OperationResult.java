package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.converters.IOJsonMessageConverter;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;
import org.apache.commons.lang3.Range;

import java.io.IOException;
import java.io.PrintWriter;

public class OperationResult {

	private int responseCode;
	private static Range<Integer> errors = Range.between(400, 599);
	private OperationData operationData = null;

	public OperationResult(int responseCode) {
		this.responseCode = responseCode;
	}

	public OperationResult(int responseCode, OperationData operationData) {
		this.responseCode = responseCode;
		this.operationData = operationData;
	}

	public boolean hasError() {
		if (errors.contains(responseCode)) {
			return true;
		}
		return false;
	}

	public void prepareResponse(HttpServletResponse response) {
		response.setStatus(responseCode);
		if(responseCode >= 200 && responseCode < 300){
			PrintWriter out = null;
			try {
				out = response.getWriter();
			} catch (IOException e) {
			}
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			if (out != null){
				out.print(IOJsonMessageConverter.getInstance().exportDataToResponseBody(operationData.getRootData()));
				out.flush();
			}
		}
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
