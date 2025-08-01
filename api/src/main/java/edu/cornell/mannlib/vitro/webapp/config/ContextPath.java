package edu.cornell.mannlib.vitro.webapp.config;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContextPath {
    private static final String CONTEXT_PATH_EXCLUDE = "context.path.exclude";
    static final Log log = LogFactory.getLog(ContextPath.class);

    public static String getPath(HttpServletRequest request) {
        if (isContextPathExcluded()) {
            return "";
        } else {
            return request.getContextPath();
        }
    }

    public static String getPath(ServletContext ctx) {
        if (isContextPathExcluded()) {
            return "";
        } else {
            return ctx.getContextPath();
        }
    }

    private static boolean isContextPathExcluded() {
        String value = ConfigurationProperties.getInstance().getProperty(CONTEXT_PATH_EXCLUDE);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
