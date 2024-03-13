/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.request;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class ApiRequestPath {

    public static final String API_SERVLET_PATH = "/api";
    public static final String RPC_SERVLET_PATH = API_SERVLET_PATH + "/rpc";
    public static final String REST_SERVLET_PATH = API_SERVLET_PATH + "/rest";
    public static final String API_SERVLET_LOGIN = API_SERVLET_PATH + "/login";

    private final RequestType type;

    private final String servletPath;

    private final String pathInfo;

    private final String[] pathParts;

    private final String resourceVersion;

    private final String resourceName;

    private final String resourceId;

    private final String rpcKey;

    private final String customRestActionName;

    private ApiRequestPath(HttpServletRequest request) {
        servletPath = request != null && request.getServletPath() != null ? request.getServletPath() : EMPTY;
        pathInfo = request != null && request.getPathInfo() != null ? request.getPathInfo() : EMPTY;

        pathParts = pathInfo.split("/");

        if (servletPath.toLowerCase().contains(RPC_SERVLET_PATH)) {
            type = RequestType.RPC;
            rpcKey = pathParts.length > 1 ? pathParts[1] : null;
            resourceVersion = null;
            resourceName = null;
            resourceId = null;
            customRestActionName = null;
        } else if (servletPath.toLowerCase().contains(REST_SERVLET_PATH)) {
            type = RequestType.REST;
            resourceVersion = pathParts.length > 1 ? pathParts[1] : null;
            resourceName = pathParts.length > 2 ? pathParts[2] : null;

            if (pathParts.length > 3) {
                if (pathParts[3].toLowerCase().startsWith("resource:")) {
                    resourceId = decode(pathParts[3]);
                    customRestActionName = pathParts.length > 4 ? pathParts[4] : null;
                } else {
                    resourceId = null;
                    customRestActionName = pathParts[3];
                }
            } else {
                resourceId = null;
                customRestActionName = null;
            }
            rpcKey = null;
        } else {
            type = RequestType.UNKNOWN;
            customRestActionName = null;
            resourceVersion = null;
            resourceName = null;
            resourceId = null;
            rpcKey = null;
        }
    }

    private String decode(String pathParameter) {
        return new String(Base64.getDecoder().decode(pathParameter.split(":")[1]));
    }

    public RequestType getType() {
        return type;
    }

    public String getServletPath() {
        return servletPath;
    }

    public String getPathInfo() {
        return pathInfo;
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

    public String getRpcKey() {
        return rpcKey;
    }

    public boolean isResourceRequest() {
        return StringUtils.isNotEmpty(resourceId);
    }

    public boolean isValid() {
        boolean isValid = false;
        switch (type) {
            case REST:
                boolean hasVersionAndName = isNotEmpty(resourceVersion) && isNotEmpty(resourceName);
                if (pathParts.length == 3) {
                    isValid = hasVersionAndName;
                } else if (pathParts.length > 3) {
                    boolean hasActionName = isNotEmpty(customRestActionName);
                    boolean hasResourceId = isNotEmpty(resourceId);

                    if (pathParts.length == 4) {
                        isValid = hasVersionAndName && (hasActionName || hasResourceId);
                    } else if (pathParts.length == 5) {
                        isValid = hasVersionAndName && hasResourceId && hasActionName;
                    }
                }
                break;
            case RPC:
                boolean hasActionName = isNotEmpty(rpcKey);

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
        return isNotEmpty(resourceVersion) && isNotEmpty(resourceName) && isNotEmpty(customRestActionName);
    }

    public static ApiRequestPath from(HttpServletRequest request) {
        return new ApiRequestPath(request);
    }

    public String getCustomRestActionName() {
        return customRestActionName;
    }

    public enum RequestType {
        RPC,
        REST,
        UNKNOWN
    }

}
