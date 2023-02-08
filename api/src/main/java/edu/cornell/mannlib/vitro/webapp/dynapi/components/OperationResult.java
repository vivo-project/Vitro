package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class OperationResult {

    private static final OperationResult OK = new OperationResult(HttpServletResponse.SC_OK);
	private static final OperationResult INTERNAL_SERVER_ERROR = new OperationResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	private static final OperationResult METHOD_NOT_ALLOWED = new OperationResult(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	private static final OperationResult NOT_AUTHORIZED = new OperationResult(HttpServletResponse.SC_UNAUTHORIZED);
	private static final OperationResult NOT_FOUND = new OperationResult(HttpServletResponse.SC_NOT_FOUND);
	private static final OperationResult BAD_REQUEST = new OperationResult(HttpServletResponse.SC_BAD_REQUEST);
	private int responseCode;
    private static Range<Integer> errors = Range.between(400, 599);

    public OperationResult(int responseCode) {
        this.responseCode = responseCode;
    }

    public static boolean hasError(int code) {
        if (errors.contains(code)) {
            return true;
        }
        return false;
    }
    
    public boolean hasError() {
        if (errors.contains(responseCode)) {
            return true;
        }
        return false;
    }
    
	public boolean hasSuccess() {
		return responseCode >= 200 && responseCode < 300;
	}

    public void prepareResponse(HttpServletResponse response) {
        response.setStatus(responseCode);
    }

    public static OperationResult badRequest() {
        return BAD_REQUEST;
    }

    public static OperationResult notFound() {
        return NOT_FOUND;
    }

    public static OperationResult methodNotAllowed() {
        return METHOD_NOT_ALLOWED;
    }

    public static OperationResult internalServerError() {
        return INTERNAL_SERVER_ERROR;
    }
    
    public static OperationResult notAuthorized() {
        return NOT_AUTHORIZED;
    }
    
    public static OperationResult ok() {
        return OK;
    }
    
    @Override
    public boolean equals(Object object) {
    	if (!(object instanceof OperationResult)) {
    		return false;
    	}
    	if (object == this) {
    		return true;
    	}
        OperationResult or = (OperationResult) object;
        
        return new EqualsBuilder()
        	.append(responseCode, or.responseCode)
        	.isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(41, 505)
        	.append(responseCode)
        	.toHashCode();
    }
    
    @Override
    public String toString() {
    	return String.valueOf(responseCode);
    }
}
