/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields;

import java.util.Comparator;
import java.util.Map;

import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;

public class IndividualsViaClassGroupOptions implements FieldOptions {

    String classGroupUri;
    String defualtOptionLabel=null;


    public IndividualsViaClassGroupOptions(String classGroupUri) {
        super();
        this.classGroupUri = classGroupUri;
    }

    public IndividualsViaClassGroupOptions setDefaultOptionLabel(String label){
        defualtOptionLabel = label;
        return this;
    }

    @Override
    public Map<String, String> getOptions(
            EditConfigurationVTwo editConfig,
            String fieldName,
            WebappDaoFactory wDaoFact,
            I18nBundle i18n) throws Exception {
        throw new Error("not implemented");
    }


    public String getClassGroupUri(){
        return classGroupUri;
    }

    public Comparator<String[]> getCustomComparator() {
    	return null;
    }

}
