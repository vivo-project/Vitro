package edu.cornell.mannlib.vitro.webapp.dynapi.request;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

public class RequestPath {

    public static final String RESOURCE_ID_PARAM = "RESOURCE_ID";

    public static final String API_SERVLET_PATH = "/api";
    public static final String RPC_SERVLET_PATH = API_SERVLET_PATH + "/rpc";
    public static final String REST_SERVLET_PATH = API_SERVLET_PATH + "/rest";

    private final RequestType type;

    private final String[] pathParts;

    private final String resourceVersion;

    private final String resourceName;

    private final String resourceId;

    private final String actionName;

    private RequestPath(HttpServletRequest request) {
        String servletPath = request != null && request.getServletPath() != null
                ? request.getServletPath()
                : EMPTY;
        String pathInfo = request != null && request.getPathInfo() != null
                ? request.getPathInfo()
                : EMPTY;

        pathParts = pathInfo.split("/");

        if (servletPath.toLowerCase().contains(RPC_SERVLET_PATH)) {
            type = RequestType.RPC;
            actionName = pathParts.length > 1 ? pathParts[1] : null;
            resourceVersion = null;
            resourceName = null;
            resourceId = null;
        } else if (servletPath.toLowerCase().contains(REST_SERVLET_PATH)) {
            type = RequestType.REST;
            resourceVersion = pathParts.length > 1 ? pathParts[1] : null;
            resourceName = pathParts.length > 2 ? pathParts[2] : null;

            if (pathParts.length > 3) {
                if (pathParts[3].toLowerCase().startsWith("resource:")) {
                    resourceId = decode(pathParts[3]);
                    actionName = pathParts.length > 4 ? pathParts[4] : null;
                    request.getParameterMap().put(RESOURCE_ID_PARAM, new String[] { resourceId });
                } else {
                    resourceId = null;
                    actionName = pathParts[3];
                }
            } else {
                resourceId = null;
                actionName = null;
            }
        } else {
            type = RequestType.UNKNOWN;
            resourceVersion = null;
            resourceName = null;
            resourceId = null;
            actionName = null;
        }
    }

    private String decode(String pathParameter) {
        return new String(Base64.getDecoder().decode(pathParameter.split(":")[1]));
    }

    public RequestType getType() {
        return type;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getActionName() {
        return actionName;
    }

    public boolean isValid() {
        boolean isValid = false;
        switch (type) {
            case REST:
                boolean hasVersionAndName = isNotEmpty(resourceVersion) && isNotEmpty(resourceName);
                if (pathParts.length == 3) {
                    isValid = hasVersionAndName;
                } else if (pathParts.length > 3) {
                    boolean hasActionName = isNotEmpty(actionName);
                    boolean hasResourceId = isNotEmpty(resourceId);

                    if (pathParts.length == 4) {
                        isValid = hasVersionAndName && (hasActionName || hasResourceId);
                    } else if (pathParts.length == 5) {
                        isValid = hasVersionAndName && hasResourceId && hasActionName;
                    }
                }
                break;
            case RPC:
                boolean hasActionName = isNotEmpty(actionName);

                isValid = hasActionName;
                break;
            case UNKNOWN:
            default:
        }

        return isValid;
    }

    public boolean isMethodAllowed(String method) {
        switch (method.toUpperCase()) {
            case "POST":
                return resourceId == null;
            case "PUT":
            case "PATCH":
            case "DELETE":
                return resourceId != null;
            case "GET":
                return true;
            case "HEAD": // this will likely want to be true when supported
            case "CONNECT":
            case "OPTIONS":
            case "TRACE":
            default:
        }
        return false;
    }

    public boolean isCustomRestAction() {
        return isNotEmpty(resourceVersion) && isNotEmpty(resourceName) && isNotEmpty(actionName);
    }

    public static RequestPath from(HttpServletRequest request) {
        return new RequestPath(request);
    }

    public enum RequestType {
        RPC, REST, UNKNOWN
    }

}
