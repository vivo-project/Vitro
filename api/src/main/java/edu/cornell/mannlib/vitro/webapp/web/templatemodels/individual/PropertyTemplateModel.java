/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.Route;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/**
 * Represents the property statement list for a single property of an individual.
 */
public abstract class PropertyTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyTemplateModel.class);

    protected final VitroRequest vreq;
    protected final String subjectUri;
    protected final Property property;
    protected final String propertyUri;
    protected String domainUri;
    protected String rangeUri;
    private final String localName;

    protected Map<String, Object> verboseDisplay;
    protected String addUrl;

    private String name;
    private int displayLimit;

    PropertyTemplateModel(Property property, Individual subject, VitroRequest vreq, String name) {
        this.vreq = vreq;
        subjectUri = subject.getURI();
        this.property = property;
        if (isFauxProperty(property)) {
            FauxProperty fauxProperty = getFauxProperty(property);
            this.name = fauxProperty.getDisplayName();
            this.displayLimit = fauxProperty.getDisplayLimit();
            propertyUri = fauxProperty.getBaseURI();
        } else {
            propertyUri = property.getURI();
            this.name = name;
        }
        localName = property.getLocalName();
        addUrl = "";
        setVerboseDisplayValues(property);
    }

    private FauxProperty getFauxProperty(Property property) {
        return ((FauxPropertyWrapper) property).getFauxProperty();
    }

    protected void setVerboseDisplayValues(Property property) {

        // No verbose display for vitro and vitro public properties.
        // This models previous behavior. In theory the verbose display can be provided,
        // but we may not want to give anyone access to these properties, since the
        // application is dependent on them.
        String namespace = property.getNamespace();
        if (VitroVocabulary.vitroURI.equals(namespace)
            || VitroVocabulary.VITRO_PUBLIC.equals(namespace)) {
            return;
        }

        Boolean verboseDisplayValue =
            (Boolean) vreq.getSession().getAttribute("verbosePropertyDisplay");

        if ( ! Boolean.TRUE.equals(verboseDisplayValue))  {
            return;
        }

		if (!PolicyHelper.isAuthorizedForActions(vreq,
				SimplePermission.SEE_VERBOSE_PROPERTY_INFORMATION.ACTION)) {
            return;
        }

        verboseDisplay = new HashMap<String, Object>();

        verboseDisplay.put("localName", property.getLocalNameWithPrefix());
        verboseDisplay.put("displayRank", getPropertyDisplayTier(property));

        String editUrl = UrlBuilder.getUrl(getPropertyEditRoute(), "uri", property.getURI());
        verboseDisplay.put("propertyEditUrl", editUrl);

        if (isFauxProperty(property)) {
            verboseDisplay.put("fauxProperty", assembleFauxPropertyValues(getFauxProperty(property)));
        }
    }

    protected boolean isFauxProperty(Property prop) {
        if (prop instanceof FauxPropertyWrapper) {
            return true;
        }
        return false;
    }

	private Map<String, Object> assembleFauxPropertyValues(FauxProperty fp) {
		Map<String, Object> map = new HashMap<>();
		String editUrl = UrlBuilder.getUrl("/editForm",
				"controller", "FauxProperty",
				"baseUri", fp.getBaseURI(),
				"domainUri", fp.getDomainURI(),
				"rangeUri", fp.getRangeURI());
		map.put("propertyEditUrl", editUrl);
		map.put("displayName", fp.getDisplayName());
		return map;
	}

	protected abstract int getPropertyDisplayTier(Property p);
    protected abstract Route getPropertyEditRoute();

    public String toString() {
        return String.format("%s on %s",
                             propertyUri != null ? propertyUri : "null Prop URI",
                             subjectUri != null ? subjectUri : "null Sub URI" );
    }

    /* Template properties */

    public abstract String getType();

    public String getName() {
        return name;
    }

    public int getDisplayLimit() {
        return displayLimit;
    }

    public String getLocalName() {
        return localName;
    }

    public String getUri() {
        return propertyUri;
    }

    public String getRangeUri() {
        return rangeUri;
    }

    public String getDomainUri() {
        return domainUri;
    }

    public String getAddUrl() {
        //log.info("addUrl=" + addUrl);
        return (addUrl != null) ? addUrl : "";
    }

    public Map<String, Object> getVerboseDisplay() {
        return verboseDisplay;
    }
}
