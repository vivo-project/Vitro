/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.i18n.freemarker;

import static edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation.EXECUTE;
import static edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper.isAuthorizedForActions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.cornell.mannlib.vitro.webapp.auth.objects.ReportGeneratorAccessObject;
import freemarker.core.Environment;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class ReportAuthMethodModel implements TemplateMethodModelEx {

    @Override
    public Object exec(List args) throws TemplateModelException {
        if (args.isEmpty() || args.get(0) == null) {
            return false;
        }
        Environment env = Environment.getCurrentEnvironment();
        HttpServletRequest request = (HttpServletRequest) env.getCustomAttribute("request");
        String uri = args.get(0).toString();
        return isAuthorizedForActions(request, new ReportGeneratorAccessObject(uri), EXECUTE);
    }

}
