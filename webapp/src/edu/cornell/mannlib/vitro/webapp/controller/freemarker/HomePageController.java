/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.dao.DisplayVocabulary;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.PageDataGetter;
import edu.cornell.mannlib.vitro.webapp.utils.pageDataGetter.DataGetterUtils;

public class HomePageController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(HomePageController.class);
    private static final String PAGE_TEMPLATE = "page-home.ftl";
    private static final String BODY_TEMPLATE = "home.ftl";

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) { 
        
        Map<String, Object> body = new HashMap<String, Object>();    
        List<PageDataGetter> dataGetters = DataGetterUtils.getDataGetterObjects(vreq, DisplayVocabulary.HOME_PAGE_URI);
        for(PageDataGetter dataGetter: dataGetters) {
	        if( dataGetter != null ){
	            String uriOfPageInDisplayModel = "not defined";            
	            Map<String, Object> pageData = 
	                dataGetter.getData(getServletContext(), vreq, 
	                        uriOfPageInDisplayModel, body);
	            if(pageData != null)
	                body.putAll(pageData);            
	        }
        }
        body.put("dataServiceUrlVClassesForVClassGroup", UrlBuilder.getUrl("/dataservice?getVClassesForVClassGroup=1&classgroupUri="));
        
        return new TemplateResponseValues(BODY_TEMPLATE, body);
    }
    
    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return siteName;
    }

    @Override
    protected String getPageTemplateName() {
        return PAGE_TEMPLATE;
    }
}
