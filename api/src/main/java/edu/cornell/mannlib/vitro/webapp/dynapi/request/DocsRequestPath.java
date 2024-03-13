/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dynapi.request;

import static edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.API_SERVLET_PATH;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.dynapi.request.ApiRequestPath.RequestType;

public class DocsRequestPath {

    public static final String DOCS_SERVLET_PATH = API_SERVLET_PATH + "/docs";
    public static final String RPC_DOCS_SERVLET_PATH = DOCS_SERVLET_PATH + "/rpc";
    public static final String REST_DOCS_SERVLET_PATH = DOCS_SERVLET_PATH + "/rest";

    private final RequestType type;

    private final String servletPath;

    private final String pathInfo;

    private final String[] pathParts;

    private final String apiVersion;

    private final String resourceName;

    private final String rpcName;

    private final String serverUrl;

    private DocsRequestPath(HttpServletRequest request) {
        servletPath = request != null && request.getServletPath() != null ? request.getServletPath() : EMPTY;
        pathInfo = request != null && request.getPathInfo() != null ? request.getPathInfo() : EMPTY;

        pathParts = pathInfo.split("/");

        if (servletPath.toLowerCase().startsWith(RPC_DOCS_SERVLET_PATH)) {
            type = RequestType.RPC;
            apiVersion = null;
            resourceName = null;
            rpcName = pathParts.length > 1 ? pathParts[1] : null;
        } else if (servletPath.toLowerCase().startsWith(REST_DOCS_SERVLET_PATH)) {
            type = RequestType.REST;
            apiVersion = pathParts.length > 1 ? pathParts[1] : null;
            resourceName = pathParts.length > 2 ? pathParts[2] : null;
            rpcName = null;
        } else {
            type = RequestType.UNKNOWN;
            apiVersion = null;
            resourceName = null;
            rpcName = null;
        }

        serverUrl = getServerUrl(request);
    }

    private String getServerUrl(HttpServletRequest request) {
        String scheme = request.getHeader("x-forwarded-proto");
        if (scheme == null) {
            scheme = request.getScheme();
        }
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String portPart;
        if ((scheme.equals("https") && port == 443) || (scheme.equals("http") && port == 80)) {
            portPart = "";
        } else {
            portPart = ":" + port;
        }
        String servletPart = request.getContextPath();
        return scheme + "://" + serverName + portPart + servletPart;
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

    public String[] getPathParts() {
        return pathParts;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getRPCName() {
        return rpcName;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public boolean isValid() {
        boolean isValid = false;
        switch (type) {
            case REST:
                boolean hasVersionAndName = isNotEmpty(apiVersion);

                isValid = hasVersionAndName;
                break;
            case RPC:
                isValid = true;
                break;
            case UNKNOWN:
            default:
        }

        return isValid;
    }

    public static DocsRequestPath from(HttpServletRequest request) {
        return new DocsRequestPath(request);
    }

}
