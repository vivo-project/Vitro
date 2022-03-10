package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Range;

import edu.cornell.mannlib.vitro.webapp.dynapi.OperationData;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.converters.IOJsonMessageConverter;
import edu.cornell.mannlib.vitro.webapp.dynapi.io.data.ObjectData;
import edu.cornell.mannlib.vitro.webapp.web.ContentType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OperationResult {

    private static final Log log = LogFactory.getLog(OperationResult.class);

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

    public void prepareResponse(HttpServletResponse response, String contentType, Action action,
            OperationData operationData) {
        if (responseCode >= 200 && responseCode < 300) {
            if (action.isOutputValid(operationData)) {
                response.setStatus(responseCode);
                if (contentType != null && contentType.equalsIgnoreCase(ContentType.JSON.getMediaType())) {
                    PrintWriter out = null;
                    try {
                        out = response.getWriter();
                    } catch (IOException e) {
                        log.error(e.getLocalizedMessage());
                    }
                    response.setContentType(ContentType.JSON.getMediaType());
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    if (out != null) {
                        ObjectData resultData = operationData.getRootData().filter(action.getProvidedParams().getNames());
                        out.print(IOJsonMessageConverter.getInstance().exportDataToResponseBody(resultData));
                        out.flush();
                    }
                }
            } else {
                response.setStatus(500);
            }
        } else {
            prepareResponse(response);
        }
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
