/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.NamedAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.SimpleAuthorizationRequest;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.dataGetter.DataGetterUtils;
/**
 * Controller for getting data for pages defined in the display model.
 *
 * This controller passes these variables to the template:
 * page: a map with information about the page from the display model.
 * pageUri: the URI of the page that identifies the page in the model
 *  (note that this is not the URL address of the page).
 *
 * See implementations of PageDataGetter for more variables.
 */
@WebServlet(name = "PageController", urlPatterns = {"/pageController", PageController.PAGE_NOT_FOUND})
public class PageController extends FreemarkerHttpServlet{
    static final String PAGE_NOT_FOUND = "/page_not_found";

    private static final Log log = LogFactory.getLog(PageController.class);

    protected final static String DEFAULT_TITLE = "Page";
    protected final static String DEFAULT_BODY_TEMPLATE = "emptyPage.ftl";

    protected static final String DATA_GETTER_MAP = "pageTypeToDataGetterMap";

    /**
     * Get the required actions for all the data getters then
     * AND them together.
     */
    @Override
    protected AuthorizationRequest requiredActions(VitroRequest vreq) {
        try {
			return getActionsForPage(vreq);
        } catch (Exception e) {
            log.warn(e);
            return AuthorizationRequest.UNAUTHORIZED;
        }
    }

    /**
     * Get all the required actions directly required for the page.
     */
    private AuthorizationRequest getActionsForPage( VitroRequest vreq ) throws Exception{
        List<String> simplePremUris = vreq.getWebappDaoFactory().getPageDao().getRequiredActions( getPageUri(vreq) );
        AuthorizationRequest auth = AuthorizationRequest.AUTHORIZED;
        for( String uri : simplePremUris ){
            if (StringUtils.isBlank(uri)) {
                continue;
            }
            NamedAccessObject ao = new NamedAccessObject(uri);
            auth = auth.and( new SimpleAuthorizationRequest(ao, AccessOperation.EXECUTE));
        }
        return auth;
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) throws Exception {

        Map<String,Object> mapForTemplate = new HashMap<String,Object>();
        Map<String,Object>page;

        //figure out what page we are trying to get
        String pageUri = getPageUri( vreq );
        if( StringUtils.isEmpty( pageUri ) )
            return doNoPageSpecified(vreq);
        else
            mapForTemplate.put("pageUri", pageUri);

        //try to get the page RDF from the model
        try{
            page =  vreq.getWebappDaoFactory().getPageDao().getPage(pageUri);
            mapForTemplate.put( "page", page);
            if( page.containsKey("title") ){
                mapForTemplate.put("title", page.get("title"));
            }
        }catch( Throwable th){
            return doNotFound(vreq);
        }

        //these should all be DataGetters now, not PageDataGetters
        executeDataGetters( pageUri, vreq, mapForTemplate);

        mapForTemplate.putAll( getPageControllerValues( pageUri, vreq, getServletContext(), mapForTemplate));

        ResponseValues rv = new TemplateResponseValues(getTemplate( mapForTemplate ), mapForTemplate);
        return rv;
    }

    private void executeDataGetters(String pageUri, VitroRequest vreq, Map<String, Object> mapForTemplate)
    throws Exception {
        List<DataGetter> dgList = DataGetterUtils.getDataGettersForPage(vreq, vreq.getDisplayModel(), pageUri);

        for( DataGetter dg : dgList){
            Map<String,Object> moreData = dg.getData(mapForTemplate);
            if( moreData != null ){
                mapForTemplate.putAll(moreData);
            }
        }
    }
/*
    private void executePageDataGetters(String pageUri, VitroRequest vreq, ServletContext context, Map<String, Object> mapForTemplate)
    throws Exception{
        mapForTemplate.putAll( DataGetterUtils.getDataForPage(pageUri, vreq, context) );
    }
*/
    /**
     * Add any additional values to the template variable map that are related to the page.
     * For example, editing links.
     */
    private Map<String,Object> getPageControllerValues(
            String pageUri, VitroRequest vreq, ServletContext servletContext,
            Map<String, Object> mapForTemplate) {
        Map<String,Object> map = new HashMap<String,Object>();

        //Add editing link for page if authorized
        Map<String,Object> pageMap = (Map<String, Object>) mapForTemplate.get("page");
        if( PolicyHelper.isAuthorizedForActions(vreq, SimplePermission.MANAGE_MENUS.ACTION) ){
            String editPageUrl = UrlBuilder.getIndividualProfileUrl(pageUri, vreq);
            editPageUrl = UrlBuilder.addParams(editPageUrl, DisplayVocabulary.SWITCH_TO_DISPLAY_MODEL , "1");
            pageMap.put("URLToEditPage", editPageUrl);
        }

        return map;
    }

    private String getTemplate(Map<String, Object> mapForTemplate) {
        //first try to get the body template from the display model RDF
        if( mapForTemplate.containsKey("page") ){
            Map page = (Map) mapForTemplate.get("page");
            if( page != null && page.containsKey("bodyTemplate")){
                return (String) page.get("bodyTemplate");
            }
        }
        //next, try to get body template from the data getter values
        if( mapForTemplate.containsKey("bodyTemplate") ){
            return (String) mapForTemplate.get("bodyTemplate");
        }

        //Nothing? then use a default empty page
        return DEFAULT_BODY_TEMPLATE;
    }


    private ResponseValues doError(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title", I18n.text(vreq, "page_not_created"));
        body.put("errorMessage", I18n.text(vreq, "page_not_created_msg"));
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    private ResponseValues doNotFound(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title", I18n.text(vreq, "page_not_found"));
        body.put("errorMessage", I18n.text(vreq, "page_not_found_msg"));
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_NOT_FOUND);
    }


    private ResponseValues doNoPageSpecified(VitroRequest vreq) {
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("title",I18n.text(vreq, "page_uri_missing"));
        body.put("errorMessage", I18n.text(vreq, "page_uri_missing_msg"));
        return new TemplateResponseValues(Template.TITLED_ERROR_MESSAGE.toString(), body, HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Gets the page URI from the request.  The page must be defined in the display model.
     * @throws Exception
     */
    private String getPageUri(VitroRequest vreq) throws Exception {
        // Check if there is a page URI in the request.
        // This would have been added by a servlet Filter.
        String pageURI = (String) vreq.getAttribute("pageURI");
        if (DispatcherType.ERROR.equals(vreq.getDispatcherType())) {
            pageURI = vreq.getWebappDaoFactory().getPageDao().getPageMappings().get(PAGE_NOT_FOUND);
        }
        return pageURI;
    }


    public static void putPageUri(HttpServletRequest req, String pageUri){
        req.setAttribute("pageURI", pageUri);
    }


}
