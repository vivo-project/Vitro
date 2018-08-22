package edu.cornell.mannlib.vitro.webapp.audit.controller;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.audit.AuditResults;
import edu.cornell.mannlib.vitro.webapp.audit.ListAddedStatementsMethod;
import edu.cornell.mannlib.vitro.webapp.audit.ListRemovedStatementsMethod;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAO;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAOFactory;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import java.util.HashMap;
import java.util.Map;

/**
 * UI for browsing audit entries
 */
@WebServlet(name = "AuditViewer", urlPatterns = {"/audit/*"} )
public class AuditController extends FreemarkerHttpServlet {
    private static final Log log = LogFactory.getLog(AuditController.class);

    // Template for the UI
    private static final String TEMPLATE_DEFAULT = "auditHistory.ftl";

    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_LIMIT = "limit";

    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        return SimplePermission.EDIT_OWN_ACCOUNT.ACTION;
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        if (log.isDebugEnabled()) {
            dumpRequestParameters(vreq);
        }

        Map<String, Object> body = new HashMap<>();

        // Get the current user
        UserAccount acc = LoginStatusBean.getCurrentUser(vreq);
        if (acc != null) {
            // Get the URI of the user
            String uri = acc.getUri();

            // Get the offset / limit parameters (or default if unset)
            int offset = getOffset(vreq);
            int limit  = getLimit(vreq);

            // Get the Audit DAO
            AuditDAO auditDAO = AuditDAOFactory.getAuditDAO(vreq);

            // Find a page of audit entries for the current user
            AuditResults results = auditDAO.findForUser(uri, offset, limit);

            // Pass the results to Freemarker
            body.put("results", results);

            // Create next / previous links
            if (offset > 0) {
                body.put("prevPage", getPreviousPageLink(offset, limit, vreq.getServletPath()));
            }
            if (offset < (results.getTotal() - limit)) {
                body.put("nextPage", getNextPageLink(offset, limit, vreq.getServletPath()));
            }

            // Pass the user name to Freemarker
            if (StringUtils.isNotEmpty(acc.getFirstName())) {
                if (StringUtils.isNoneEmpty(acc.getLastName())) {
                    body.put("username", acc.getFirstName() + " " + acc.getLastName());
                } else {
                    body.put("username", acc.getFirstName());
                }
            } else if (StringUtils.isNotEmpty(acc.getEmailAddress())) {
                body.put("username", acc.getEmailAddress());
            } else {
                body.put("username", "");
            }
        }

        // Pass the helper methods to Freemaker
        body.put("listAddedStatements", new ListAddedStatementsMethod());
        body.put("listRemovedStatements", new ListRemovedStatementsMethod());

        // Return the default template and parameters
        return new TemplateResponseValues(TEMPLATE_DEFAULT, body);
    }

    /**
     * Get the number of entries to show per page, or a default value of 10
     *
     * @param vreq
     * @return
     */
    private int getLimit(VitroRequest vreq) {
        int limit = 0;
        try{
            limit = Integer.parseInt(vreq.getParameter(PARAM_LIMIT));
        }catch (Throwable e) {
            limit = 10;
        }
        return limit;
    }

    /**
     * Get the page offset, or a default of 0
     *
     * @param vreq
     * @return
     */
    private int getOffset(VitroRequest vreq) {
        int offset = 0;
        try{
            offset = Integer.parseInt(vreq.getParameter(PARAM_OFFSET));
        }catch (Throwable e) {
            offset = 0;
        }
        return offset;
    }

    /**
     * Generate the link to the previous page
     *
     * @param offset
     * @param limit
     * @param baseUrl
     * @return
     */
    private String getPreviousPageLink(int offset, int limit, String baseUrl) {
        UrlBuilder.ParamMap params = new UrlBuilder.ParamMap();
        params.put(PARAM_OFFSET, String.valueOf(offset-limit));
        return UrlBuilder.getUrl(baseUrl, params);
    }

    /**
     * Generate the link the next page
     *
     * @param offset
     * @param limit
     * @param baseUrl
     * @return
     */
    private String getNextPageLink(int offset, int limit, String baseUrl) {
        UrlBuilder.ParamMap params = new UrlBuilder.ParamMap();
        params.put(PARAM_OFFSET, String.valueOf(offset+limit));
        return UrlBuilder.getUrl(baseUrl, params);
    }

}
