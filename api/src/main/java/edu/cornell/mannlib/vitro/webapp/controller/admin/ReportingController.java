/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.admin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.SelectFromContentDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.rdf.SelectFromGraphDistributor;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.FreemarkerHttpServlet;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.RedirectResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DataDistributorDao;
import edu.cornell.mannlib.vitro.webapp.dao.ReportingDao;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.reporting.AbstractTemplateReport;
import edu.cornell.mannlib.vitro.webapp.reporting.DataDistributorEndpoint;
import edu.cornell.mannlib.vitro.webapp.reporting.DataSource;
import edu.cornell.mannlib.vitro.webapp.reporting.ReportGenerator;
import edu.cornell.mannlib.vitro.webapp.reporting.ReportGeneratorException;
import edu.cornell.mannlib.vitro.webapp.reporting.XmlGenerator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reflections.Reflections;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * Main controller for the Reporting interface - editing and running reports
 * that use DataDistributor sources
 */
@WebServlet(name = "ReportingConf", urlPatterns = { "/admin/reporting", "/admin/reporting/*" })
public class ReportingController extends FreemarkerHttpServlet {
    private static final Log log = LogFactory.getLog(ReportingController.class);

    private static List<Class<? extends ReportGenerator>> reportTypes = new ArrayList<>();

    private static final String SETUP_URI_BASE = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#";

    private static final String LIST_TEMPLATE_NAME = "admin-reporting.ftl";
    private static final String EDIT_TEMPLATE_NAME = "admin-reporting-edit-report.ftl";
    private static final String SUBMIT_URL_BASE = UrlBuilder.getUrl("admin/reporting");
    private static final String REDIRECT_PATH = "/admin/reporting";

    private static final String EXECUTE_ONLY_ATTR = "executeOnly";

    private static final String DISTRIBUTOR_SELECT_FROM_CONTENT = SelectFromContentDistributor.class.getName();
    private static final String DISTRIBUTOR_SELECT_FROM_GRAPH = SelectFromGraphDistributor.class.getName();

    private static String defaultDataDistributorBaseUri;

    // Static initialiser uses reflection to find out what Java classes are present,
    // as this only needs to be done
    // when the application is started
    static {
        Reflections reportReflections = new Reflections("org.vivoweb", "edu.cornell");
        for (Class<? extends ReportGenerator> report : reportReflections.getSubTypesOf(ReportGenerator.class)) {
            // As long as it is not an abstract class, add it to the list
            if (!Modifier.isAbstract(report.getModifiers())) {
                reportTypes.add(report);
            }
        }
        reportTypes.sort(new Comparator<Class<? extends ReportGenerator>>() {
            @Override
            public int compare(Class<? extends ReportGenerator> o1, Class<? extends ReportGenerator> o2) {
                return o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
            }
        });
    }

    /**
     * Generate the default data distributor uri given the current request The URL
     * must be fully formed and actionable - hence this method Should be replaced
     * with configuration so that it can work with a scheduler
     * 
     * @param req
     */
    private void setDefaultDataDistributorBaseUri(HttpServletRequest req) {
        if (StringUtils.isEmpty(defaultDataDistributorBaseUri)) {
            String scheme = req.getScheme(); // http
            String serverName = req.getServerName(); // hostname.com
            int serverPort = req.getLocalPort(); // 80
            // Reconstruct original requesting URL
            StringBuilder url = new StringBuilder();
            url.append(scheme).append("://").append(serverName);
            if (serverPort != 80 && serverPort != 443) {
                url.append(":").append(serverPort);
            }

            url.append(UrlBuilder.getUrl("/api/dataRequest/"));
            defaultDataDistributorBaseUri = url.toString();
        }

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        setDefaultDataDistributorBaseUri(request);
        DataDistributorEndpoint.setDefault(new DataDistributorEndpoint(defaultDataDistributorBaseUri));

        // First, check to see if we have rights to administer the reports
        if (!PolicyHelper.isAuthorizedForActions(request, SimplePermission.MANAGE_REPORTS.ACTION)) {
            // Can't admin, but may be able to run reports
            if (isAuthorizedToDisplayPage(request, response, SimplePermission.EXECUTE_REPORTS.ACTION)) {
                request.setAttribute(EXECUTE_ONLY_ATTR, Boolean.TRUE);
            } else {
                // Can't run or administer reports, so bail out here
                return;
            }
        }

        response.addHeader("X-XSS-Protection", "0");

        // Determine if we are running a report or downloading intermediate XML or
        // template
        String reportName = request.getPathInfo();
        if (reportName == null || reportName.isEmpty()) {
            // Not processing a report, so carry on
        } else {
            // Retrieve the report name
            if (reportName.startsWith("/")) {
                reportName = reportName.substring(1);
            }
            if (!StringUtils.isEmpty(reportName)) {
                // Check if we are requesting a download format
                String[] download = request.getParameterValues("download");
                if (!ArrayUtils.isEmpty(download)) {
                    for (String format : download) {
                        if ("xml".equalsIgnoreCase(format)) {
                            if (processDownloadXml(reportName, request, response)) {
                                return;
                            }
                        }

                        if ("template".equalsIgnoreCase(format)) {
                            if (processDownloadTemplate(reportName, request, response)) {
                                return;
                            }
                        }
                    }
                } else {
                    // Not a download request, so run the report
                    if (processReport(reportName, request, response)) {
                        return;
                    }
                }

                // Add an error message
            }
        }

        super.doGet(request, response);
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) throws Exception {
        ResponseValues response = null;

        // If we are able to administer the reports, process any admin functions
        if (vreq.getAttribute(EXECUTE_ONLY_ATTR) == null) {
            // Determine whether we are adding, editing or deleting an object
            if (!StringUtils.isEmpty(vreq.getParameter("addType"))) {
                response = processAdd(vreq);
            } else if (!StringUtils.isEmpty(vreq.getParameter("editUri"))) {
                response = processEdit(vreq);
            } else if (!StringUtils.isEmpty(vreq.getParameter("deleteUri"))) {
                response = processDelete(vreq);
            }
        }

        // If we haven't determined a response, show a list of reports
        return response != null ? response : processList(vreq);
    }

    private ResponseValues processList(VitroRequest vreq) {
        Map<String, Object> bodyMap = new HashMap<>();

        bodyMap.put("title", I18n.text(vreq, "page_reporting_config"));
        bodyMap.put("submitUrlBase", SUBMIT_URL_BASE);
        if (vreq.getAttribute(EXECUTE_ONLY_ATTR) == null) {
            bodyMap.put("adminControls", true);
        }

        ReportingDao reportDao = vreq.getWebappDaoFactory().getReportingDao();

        bodyMap.put("reports", reportDao.getAllReports());
        bodyMap.put("reportTypes", reportTypes);
        bodyMap.put("reportTypeBase", ReportGenerator.class);

        return new TemplateResponseValues(LIST_TEMPLATE_NAME, bodyMap);
    }

    private ResponseValues processAdd(VitroRequest vreq) {
        // Get the class from the parameter
        Class objectClass = findClass(vreq.getParameter("addType"));

        if (objectClass != null) {
            // If we are processing a submitted form
            if (!StringUtils.isEmpty(vreq.getParameter("submitted"))) {
                ReportingDao reportDao = vreq.getWebappDaoFactory().getReportingDao();

                // Generate a unique ID for the object
                String uri = SETUP_URI_BASE + UUID.randomUUID().toString();

                // Generate a model from the submitted form
                ReportGenerator report = getReportFromRequest(vreq, uri, objectClass);
                report.setUri(uri);

                // Update the model in the triple store with the submitted form
                reportDao.updateReport(uri, report);

                // Redirect to the list
                return new RedirectResponseValues(REDIRECT_PATH);
            }
        }

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("addType", vreq.getParameter("addType"));

        // Adding a new object, so pass an empty field map to the UI for a blank form
        try {
            bodyMap.put("report", objectClass.getDeclaredConstructor().newInstance());
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException
                | InvocationTargetException e) {
            log.error("Unable to create an empty report instance", e);
        }

        // Create the response
        return makeEditFormResponseValues(vreq, objectClass, bodyMap);
    }

    private ResponseValues processEdit(VitroRequest vreq) {
        String uri = vreq.getParameter("editUri");

        ReportingDao reportDao = vreq.getWebappDaoFactory().getReportingDao();

        // Retrieve the report from the triple store
        ReportGenerator report = reportDao.getReportByUri(uri);

        if (report != null) {
            // If we are processing a submitted form
            if (!StringUtils.isEmpty(vreq.getParameter("submitted"))) {
                // Generate a model from the submitted form
                ReportGenerator submittedReport = getReportFromRequest(vreq, uri, report.getClass());

                // If this is a report with a template
                if (submittedReport instanceof AbstractTemplateReport) {
                    // Copy the existing template if a new one hasn't been submitted
                    if (ArrayUtils.isEmpty(((AbstractTemplateReport) submittedReport).getTemplate())) {
                        ((AbstractTemplateReport) submittedReport)
                                .setTemplate(((AbstractTemplateReport) report).getTemplate());
                    }
                }

                // Update the model in the triple store with the submitted form
                reportDao.updateReport(uri, submittedReport);

                // Redirect to the list
                return new RedirectResponseValues(REDIRECT_PATH);
            }

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("editUri", uri);
            bodyMap.put("report", report);

            // If the uri is not for a "persistent" object (it is in a temporary submodel)
            if (!report.isPersistent()) {
                // Tell the UI to display in readonly form
                bodyMap.put("readOnly", true);
            }

            // Create the response
            return makeEditFormResponseValues(vreq, report.getClass(), bodyMap);
        }

        return null;
    }

    private ResponseValues processDelete(VitroRequest vreq) {
        String uri = vreq.getParameter("deleteUri");

        ReportingDao reportingDao = vreq.getWebappDaoFactory().getReportingDao();

        // A delete is simply an update with an empty model
        reportingDao.deleteReport(uri);

        return new RedirectResponseValues(REDIRECT_PATH);
    }

    /**
     * Convert the submitted values into a Jena model
     */
    private ReportGenerator getReportFromRequest(VitroRequest vreq, String subjectUri, Class objectClass) {
        ReportGenerator report;
        try {
            report = (ReportGenerator) objectClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException
                | IllegalAccessException e) {
            log.error("Unable to create a report instance", e);
            return null;
        }

        report.setUri(subjectUri);

        report.setReportName(vreq.getParameter("reportName"));

        // Add DataSources
        String[] dsIndices = vreq.getParameterValues("dataSourceIndex");
        for (String dsIdx : dsIndices) {
            DataSource dataSource = new DataSource();

            dataSource.setDistributorName(vreq.getParameter("dataSource" + dsIdx + "_distributor"));
            dataSource.setOutputName(vreq.getParameter("dataSource" + dsIdx + "_outputName"));
            try {
                dataSource.setRank(Integer.parseInt(vreq.getParameter("dataSource" + dsIdx + "_rank"), 10));
            } catch (NumberFormatException nfe) {
                log.error("Rank must be a number");
            }

            report.addDatasource(dataSource);
        }

        // If this is a template based report, add the template
        if (report instanceof AbstractTemplateReport) {
            FileItem item = vreq.getFileItem("template");
            if (item != null) {
                byte[] templateBytes = item.get();
                if (templateBytes != null && templateBytes.length > 0) {
                    ((AbstractTemplateReport) report).setTemplate(templateBytes);
                }
            }
        }

        return report;
    }

    private ResponseValues makeEditFormResponseValues(VitroRequest vreq, Class objectClass,
            Map<String, Object> bodyMap) {
        bodyMap.put("objectClass", objectClass);

        DataDistributorDao ddDao = vreq.getWebappDaoFactory().getDataDistributorDao();

        // Pass existing distributors to the UI for any drop downs to select a child
        // distributor
        List<DataDistributorDao.Entry> distributors = new ArrayList<>();
        for (DataDistributorDao.Entry distributor : ddDao.getAllDistributors()) {
            // Currently, wr only support SELECT distributors
            if (DISTRIBUTOR_SELECT_FROM_CONTENT.equals(distributor.getClassName())
                    || DISTRIBUTOR_SELECT_FROM_GRAPH.equals(distributor.getClassName())) {
                distributors.add(distributor);
            }
        }

        bodyMap.put("datadistributors", distributors);
        bodyMap.put("submitUrlBase", SUBMIT_URL_BASE);

        return new TemplateResponseValues(EDIT_TEMPLATE_NAME, bodyMap);
    }

    private boolean processReport(String reportName, HttpServletRequest request, HttpServletResponse response) {
        // Need to wrap as a VitroRequest to get the DAO factory
        VitroRequest vreq = new VitroRequest(request);

        // Get the reporting DAO
        ReportingDao reportingDao = vreq.getWebappDaoFactory().getReportingDao();

        // Get the named report
        ReportGenerator report = reportingDao.getReportByName(reportName);
        try {
            // Set the content type for this report
            response.setContentType(report.getContentType());

            // Generate the report directly into the output stream
            report.generateReport(response.getOutputStream());
        } catch (IOException | ReportGeneratorException e) {
            log.error("Unable to generate the report", e);
        }

        return true;
    }

    private boolean processDownloadTemplate(String action, HttpServletRequest request, HttpServletResponse response) {
        // Need to wrap as a VitroRequest to get the DAO factory
        VitroRequest vreq = new VitroRequest(request);

        // Get the reporting DAO
        ReportingDao reportingDao = vreq.getWebappDaoFactory().getReportingDao();

        // Get the named report
        ReportGenerator report = reportingDao.getReportByName(action);
        try {
            // All template driven report should be based off of AbstractTemplateReport
            // So ensure the is report is, and use it to obtain the template
            if (report instanceof AbstractTemplateReport) {
                // The template should have the same type as the report, so set that
                response.setContentType(report.getContentType());

                // Return the template contents
                response.getOutputStream().write(((AbstractTemplateReport) report).getTemplate());
                return true;
            }
        } catch (IOException | ReportGeneratorException e) {
            log.error("Unable to retrieve template", e);
        }

        return false;
    }

    private boolean processDownloadXml(String action, HttpServletRequest request, HttpServletResponse response) {
        // Need to wrap as a VitroRequest to get the DAO factory
        VitroRequest vreq = new VitroRequest(request);

        // Get the reporting DAO
        ReportingDao reportingDao = vreq.getWebappDaoFactory().getReportingDao();

        // Get the named report
        ReportGenerator report = reportingDao.getReportByName(action);
        try {
            // Only do this if the report has an XmlGenerator
            if (report instanceof XmlGenerator) {
                // Set the response type as xml
                response.setContentType("text/xml");

                // Get the xml from the report
                Document xml = ((XmlGenerator) report).generateXml();

                // Create an xml serializer
                DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
                DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("XML 3.0 LS 3.0");
                LSSerializer serializer = impl.createLSSerializer();
                LSOutput output = impl.createLSOutput();
                output.setEncoding("UTF-8");
                output.setByteStream(response.getOutputStream());

                // Write the XML to the output stream
                serializer.write(xml, output);
                return true;
            }
        } catch (IOException | ReportGeneratorException | IllegalAccessException | InstantiationException
                | ClassNotFoundException e) {
            log.error("Unable to generate the xml", e);
        }

        return false;
    }

    private Class findClass(String name) {
        Class objectClass = null;

        if (!StringUtils.isEmpty(name)) {
            if (name.contains(".")) {
                try {
                    objectClass = Class.forName(name);
                } catch (ClassCastException | ClassNotFoundException ce) {
                }
            }
        }

        if (objectClass != null) {
            if (ReportGenerator.class.isAssignableFrom(objectClass)) {
                return objectClass;
            }
        }

        return null;
    }
}
