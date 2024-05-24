/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;

import edu.cornell.mannlib.vitro.webapp.beans.ApplicationBean;
import edu.cornell.mannlib.vitro.webapp.beans.CaptchaServiceBean;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.config.ContextPath;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.email.FreemarkerEmailFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  Controller for comments ("contact us") page
 *  * @author bjl23
 */
@WebServlet(name = "ContactFormController", urlPatterns = {"/contact"} )
public class ContactFormController extends FreemarkerHttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ContactFormController.class.getName());

    private static final String TEMPLATE_DEFAULT = "contactForm-form.ftl";
    private static final String TEMPLATE_ERROR = "contactForm-error.ftl";


    @Override
    protected String getTitle(String siteName, VitroRequest vreq) {
        return siteName + " Feedback Form";
    }

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) throws IOException {
        ApplicationBean appBean = vreq.getAppBean();

        String templateName;
        Map<String, Object> body = new HashMap<String, Object>();

        if (!FreemarkerEmailFactory.isConfigured(vreq)) {
            body.put("errorMessage",
                     "This application has not yet been configured to send mail. " +
                     "Email properties must be specified in the configuration properties file.");
            templateName = TEMPLATE_ERROR;
        }

        else if (StringUtils.isBlank(appBean.getContactMail())) {
            body.put("errorMessage",
            		"The feedback form is currently disabled. In order to activate the form, a site administrator must provide a contact email address in the <a href='editForm?home=1&amp;controller=ApplicationBean&amp;id=1'>Site Configuration</a>");

            templateName = TEMPLATE_ERROR;
        }

        else {
            CaptchaServiceBean.addCaptchaRelatedFieldsToPageContext(body);

            body.put("contextPath", ContextPath.getPath(vreq));
            body.put("formAction", "submitFeedback");

            if (vreq.getHeader("Referer") == null) {
                vreq.getSession().setAttribute("contactFormReferer","none");
            } else {
                vreq.getSession().setAttribute("contactFormReferer",vreq.getHeader("Referer"));
            }

            templateName = TEMPLATE_DEFAULT;
        }

        return new TemplateResponseValues(templateName, body);
    }
}
