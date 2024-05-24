package edu.cornell.mannlib.vitro.webapp.config;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContextPath {
    static final Log log = LogFactory.getLog(ContextPath.class);

    public static String getPath(HttpServletRequest request) {
        String path = ConfigurationProperties.getInstance().getProperty("context.path");
        log.debug(String.format("Custom path %s, request path %s", path, request.getContextPath()));
        return path == null ? request.getContextPath() : path;
    }

    public static String getPath(ServletContext ctx) {
        String path = ConfigurationProperties.getInstance().getProperty("context.path");
        log.debug(String.format("Custom path %s, ctx path %s", path, ctx.getContextPath()));
        return path == null ? ctx.getContextPath() : path;
    }
}
