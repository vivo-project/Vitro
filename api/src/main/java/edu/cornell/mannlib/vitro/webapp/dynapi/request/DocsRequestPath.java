package edu.cornell.mannlib.vitro.webapp.dynapi.request;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RequestType;

public class DocsRequestPath {

    public static final String DOCS_SERVLET_PATH = "/docs";
    public static final String RPC_DOCS_SERVLET_PATH = DOCS_SERVLET_PATH + "/rpc";
    public static final String REST_DOCS_SERVLET_PATH = DOCS_SERVLET_PATH + "/rest";

    private final String apiVersion;

    private final String resourceName;

    private final RequestType type;

    private final String[] pathParts;

    private final String servletPath;

    private DocsRequestPath(HttpServletRequest request) {
        servletPath = request != null && request.getServletPath() != null
                ? request.getServletPath()
                : EMPTY;
        String pathInfo = request != null && request.getPathInfo() != null
                ? request.getPathInfo()
                : EMPTY;

        pathParts = pathInfo.split("/");

        if (servletPath.toLowerCase().startsWith(RPC_DOCS_SERVLET_PATH)) {
            type = RequestType.RPC;
            apiVersion = pathParts.length > 1 ? pathParts[1] : null;
            resourceName = null;
        } else if (servletPath.toLowerCase().startsWith(REST_DOCS_SERVLET_PATH)) {
            type = RequestType.REST;
            apiVersion = pathParts.length > 1 ? pathParts[1] : null;
            resourceName = pathParts.length > 2 ? pathParts[2] : null;
        } else {
            type = RequestType.UNKNOWN;
            apiVersion = null;
            resourceName = null;
        }
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getServletPath() {
        return servletPath;
    }

    public RequestType getType() {
        return type;
    }

    public static DocsRequestPath from(HttpServletRequest request) {
        return new DocsRequestPath(request);
    }

}
