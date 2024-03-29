/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import static edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject.SOME_LITERAL;
import static edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject.SOME_URI;

import java.util.ArrayList;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.auth.attributes.AccessOperation;
import edu.cornell.mannlib.vitro.webapp.auth.objects.AccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.DataPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.FauxDataPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.FauxObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.objects.ObjectPropertyStatementAccessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.PolicyHelper;
import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyGroupTemplateModel extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(PropertyGroupTemplateModel.class);

    private final String name;
    private final List<PropertyTemplateModel> properties;

    PropertyGroupTemplateModel(VitroRequest vreq, PropertyGroup group,
            Individual subject, boolean editing,
            List<DataProperty> populatedDataPropertyList,
            List<ObjectProperty> populatedObjectPropertyList) {

        this.name = group.getName();

        List<Property> propertyList = group.getPropertyList();
        properties = new ArrayList<PropertyTemplateModel>(propertyList.size());

        for (Property p : propertyList)  {
            if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty) p;
                if (!allowedToDisplay(vreq, op, subject)) {
                    continue;
                }
                ObjectPropertyTemplateModel tm = ObjectPropertyTemplateModel.getObjectPropertyTemplateModel(
                        op, subject, vreq, editing, populatedObjectPropertyList);
                if (!tm.isEmpty() || (editing && !tm.getAddUrl().isEmpty())) {
                    properties.add(tm);
                }

            } else if (p instanceof DataProperty){
                DataProperty dp = (DataProperty) p;
                if (!allowedToDisplay(vreq, dp, subject))  {
                    continue;
                }
                properties.add(new DataPropertyTemplateModel(dp, subject, vreq, editing, populatedDataPropertyList));
            } else {
                log.debug(p.getURI() + " is neither an ObjectProperty nor a DataProperty; skipping display");
            }
        }
    }

	/**
	 * See if the property is permitted in its own right. If not, the property
	 * statement might still be permitted to a self-editor.
	 */
	private boolean allowedToDisplay(VitroRequest vreq, ObjectProperty op, Individual subject) {
	    AccessObject ao;
		if (op instanceof FauxObjectPropertyWrapper) {
			final FauxProperty fauxProperty = ((FauxObjectPropertyWrapper) op).getFauxProperty();
            ao = new FauxObjectPropertyStatementAccessObject(vreq.getJenaOntModel(), subject.getURI(), fauxProperty, SOME_URI);
		} else {
			ao = new ObjectPropertyStatementAccessObject(vreq.getJenaOntModel(), subject.getURI(), op, SOME_URI);
		}
		return PolicyHelper.isAuthorizedForActions(vreq, ao, AccessOperation.DISPLAY);
    }

	/**
	 * See if the property is permitted in its own right. If not, the property
	 * statement might still be permitted to a self-editor.
	 */
	private boolean allowedToDisplay(VitroRequest vreq, DataProperty dp, Individual subject) {
        AccessObject ao;
		if (dp instanceof FauxDataPropertyWrapper) {
		    final FauxProperty fauxProperty = ((FauxDataPropertyWrapper) dp).getFauxProperty();
			ao = new FauxDataPropertyStatementAccessObject(vreq.getJenaOntModel(), subject.getURI(), fauxProperty, SOME_LITERAL);
		} else {
			ao = new DataPropertyStatementAccessObject(vreq.getJenaOntModel(), subject.getURI(), dp, SOME_LITERAL);
		}
        return PolicyHelper.isAuthorizedForActions(vreq, ao, AccessOperation.DISPLAY);	
    }

	protected boolean isEmpty() {
        return properties.isEmpty();
    }

    protected void remove(PropertyTemplateModel ptm) {
        properties.remove(ptm);
    }


    @Override
	public String toString(){
        StringBuilder ptmStr = new StringBuilder();
        for (PropertyTemplateModel ptm : properties) {
            String spacer = "\n  ";
            if (ptm != null)
                ptmStr.append(spacer).append(ptm.toString());
        }
        return String.format("\nPropertyGroupTemplateModel %s[%s] ",name, ptmStr.toString());
    }

    /* Accessor methods for templates */
    // Add this so it's included in dumps for debugging. The templates will want to display
    // name using getName(String)
    public String getName() {
        return name;
    }

    public String getName(String otherGroupName) {
        if (name == null || name.isEmpty()) {
            return otherGroupName;
        } else {
            return name;
        }
    }

    public List<PropertyTemplateModel> getProperties() {
        return properties;
    }
}
