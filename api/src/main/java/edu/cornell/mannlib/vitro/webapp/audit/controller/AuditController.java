package edu.cornell.mannlib.vitro.webapp.audit.controller;

import edu.cornell.mannlib.vedit.beans.LoginStatusBean;
import edu.cornell.mannlib.vitro.webapp.audit.AuditChangeSet;
import edu.cornell.mannlib.vitro.webapp.audit.AuditResults;
import edu.cornell.mannlib.vitro.webapp.audit.ListAddedStatementsMethod;
import edu.cornell.mannlib.vitro.webapp.audit.ListRemovedStatementsMethod;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAO;
import edu.cornell.mannlib.vitro.webapp.audit.storage.AuditDAOFactory;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.PermissionSets;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.beans.UserAccount;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.UserAccountsDao;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * UI for browsing audit entries
 */
@WebServlet(name = "AuditViewer", urlPatterns = {"/audit/*"} )
public class AuditController extends FreemarkerHttpServlet {
    private static final String DESC_ORDER = "DESC";

    private static final String ASC_ORDER = "ASC";

    private static final Log log = LogFactory.getLog(AuditController.class);

    // Template for the UI
    private static final String TEMPLATE_DEFAULT = "auditHistory.ftl";

    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_LIMIT = "limit";
    private static final String PARAM_GRAPH = "graph";
    private static final String PARAM_ORDER = "order";
    private static final String PARAM_START_DATE = "start_date";

    private static final String PARAM_USER = "user";
    private static final String[] limits = { "10", "30", "50", "100", "1000" };
    private static final String[] orders = { ASC_ORDER, DESC_ORDER };

    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        return SimplePermission.EDIT_OWN_ACCOUNT.ACTION;
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        if (log.isDebugEnabled()) {
            dumpRequestParameters(vreq);
        }
        UserAccountsDao uad = ModelAccess.on(vreq).getWebappDaoFactory().getUserAccountsDao();
        Map<String, Object> body = new HashMap<>();

        // Get the current user
        UserAccount acc = LoginStatusBean.getCurrentUser(vreq);
        if (acc != null && isAdmin(acc)) {

            // Get the offset / limit parameters (or default if unset)
            int offset = getOffset(vreq);
            body.put("offset", offset);
            int limit  = getLimit(vreq);
            body.put("limit", String.valueOf(limit));
            body.put("limits", limits);
            long startDate = getStartDate(vreq);
            body.put("start_date", startDate);
            String userUri = getUser(vreq);
            body.put("userUri", userUri);
            String graphUri = getGraph(vreq);
            body.put("selectedGraphUri", graphUri);
            String order = getOrder(vreq);
            body.put("order", order);
            body.put("orders", orders);
            body.put("selectedGraphUri", graphUri);
            // Get the Audit DAO
            AuditDAO auditDAO = AuditDAOFactory.getAuditDAO();
            // Find a page of audit entries for the current user
            AuditResults results = auditDAO.find(offset, limit, startDate, userUri, graphUri, ASC_ORDER.equals(order));
            List <String> users = auditDAO.getUsers();
            body.put("users", users);
            List <String> graphUris = auditDAO.getGraphs();
            body.put("graphs", graphUris);

            setUserData(results.getDatasets(), uad);
            // Pass the results to Freemarker
            body.put("results", results);

            // Create next / previous links
            if (offset > 0) {
                body.put("prevPage", getPreviousPageLink(offset, limit, startDate, vreq.getServletPath()));
            }
            if (offset < (results.getTotal() - limit)) {
                body.put("nextPage", getNextPageLink(offset, limit, startDate, vreq.getServletPath()));
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

    private boolean isAdmin(UserAccount acc) {
        if (acc.isRootUser()) {
            return true;
        }
        Set<String> roles = acc.getPermissionSetUris();
        return (roles.contains(PermissionSets.URI_DBA));
    }

    private void setUserData(List<AuditChangeSet> list, UserAccountsDao uad) {
        for (AuditChangeSet acs : list) {
            UserAccount account = uad.getUserAccountByUri(acs.getUserId());
            if (account == null) {
                continue;
            }
            acs.setUserFirstName(account.getFirstName());
            acs.setUserLastName(account.getLastName());
            acs.setUserEmail(account.getEmailAddress());
        }
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
            log.debug(e, e);
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
            log.debug(e, e);
            offset = 0;
        }
        return offset;
    }

    /**
     * Get start date
     *
     * @param vreq
     * @return
     */
    private long getStartDate(VitroRequest vreq) {
        long startDate = 0;
        try{
            startDate = Long.parseLong(vreq.getParameter(PARAM_START_DATE));
        }catch (Throwable e) {
            log.debug(e, e);
        }
        return startDate;
    }
    
    /**
     * Get user
     *
     * @param vreq
     * @return empty string if user wasn't set
     */
    private String getUser(VitroRequest vreq) {
        String user = vreq.getParameter(PARAM_USER);
        if ( user == null) {
            return "";
        }
        return user;
    }
    
    /**
     * Get graph
     *
     * @param vreq
     * @return empty string if graph wasn't set
     */
    private String getGraph(VitroRequest vreq) {
        String graph = vreq.getParameter(PARAM_GRAPH);
        if ( graph == null) {
            return "";
        }
        return graph;
    }
    /*
    * Get order
    *
    * @param vreq
    * @return empty string if graph wasn't set
    */
   private String getOrder(VitroRequest vreq) {
       String order = vreq.getParameter(PARAM_ORDER);
       if ( StringUtils.isBlank(order) || DESC_ORDER.equals(order)) {
           return DESC_ORDER;
       }
       return ASC_ORDER;
   }
    
    /**
     * Generate the link to the previous page
     *
     * @param offset
     * @param limit
     * @param baseUrl
     * @return
     */
    private String getPreviousPageLink(int offset, int limit, long startDate, String baseUrl) {
        UrlBuilder.ParamMap params = new UrlBuilder.ParamMap();
        params.put(PARAM_OFFSET, String.valueOf(offset-limit));
        params.put(PARAM_LIMIT, String.valueOf(limit));
        params.put(PARAM_START_DATE, String.valueOf(startDate));
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
    private String getNextPageLink(int offset, int limit, long startDate, String baseUrl) {
        UrlBuilder.ParamMap params = new UrlBuilder.ParamMap();
        params.put(PARAM_OFFSET, String.valueOf(offset+limit));
        params.put(PARAM_LIMIT, String.valueOf(limit));
        params.put(PARAM_START_DATE, String.valueOf(startDate));
        return UrlBuilder.getUrl(baseUrl, params);
    }

}
