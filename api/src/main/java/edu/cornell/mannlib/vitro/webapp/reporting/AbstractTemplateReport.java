/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.reporting;

import static edu.cornell.mannlib.vitro.webapp.dao.ReportingDao.PROPERTY_TEMPLATE;

import java.util.Base64;

import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Base implementation for reports that use a template
 */
public abstract class AbstractTemplateReport extends AbstractReport {
    protected byte[] template;

    public void setTemplate(byte[] template) {
        this.template = template;
    }

    public byte[] getTemplate() {
        return template;
    }

    @Property(uri = PROPERTY_TEMPLATE)
    public void setTemplateBase64(String base64) {
        template = Base64.getDecoder().decode(base64);
    }

    public String getTemplateBase64() {
        return template == null ? null : Base64.getEncoder().encodeToString(template);
    }

    public boolean getImplementsTemplate() {
        return true;
    }

    /**
     * Report is runnable if the template is defined and all other conditions are
     * met
     */
    public boolean isRunnable() {
        if (ArrayUtils.isEmpty(template)) {
            return false;
        }

        return super.isRunnable();
    }
}
