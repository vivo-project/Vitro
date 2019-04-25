/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.RDFSLabelGenerator;

public class JspToGeneratorMapping {
    static Log log = LogFactory.getLog( JspToGeneratorMapping.class );

    public static Map<String,String> jspsToGenerators;

    static{
        jspsToGenerators = new HashMap<String,String>();
        Map<String, String> map = jspsToGenerators;

        // vitro forms:
//        map.put("autoCompleteDatapropForm.jsp",
//                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.AutoCompleteDatapropFormGenerator.class.getName());
//        map.put("autoCompleteObjPropForm.jsp",
//                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.AutoCompleteObjPropFormGenerator.class.getName());
        map.put("datapropStmtDelete.jsp",
                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultDeleteGenerator.class.getName());
        map.put("dateTimeIntervalForm.jsp",
                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DateTimeIntervalFormGenerator.class.getName());
        map.put("dateTimeValueForm.jsp",
                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DateTimeValueFormGenerator.class.getName());
        map.put("defaultAddMissingIndividualForm.jsp",
                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultAddMissingIndividualFormGenerator.class.getName());
        map.put("defaultDatapropForm.jsp",
                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultDataPropertyFormGenerator.class.getName());
        map.put("defaultObjPropForm.jsp",
                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultObjectPropertyFormGenerator.class.getName());
        map.put("newIndividualForm.jsp",
                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.NewIndividualFormGenerator.class.getName());
        map.put("propDelete.jsp",
                edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.DefaultDeleteGenerator.class.getName());
        map.put("rdfsLabelForm.jsp",
                RDFSLabelGenerator.class.getName());

        //add in the vivo mappings if they exist
        Object object = null;
        try {
            Class classDefinition =
                Class.forName("edu.cornell.mannlib.vitro.webapp.edit.n3editing.N3TransitionToV2Mapping");
            object = classDefinition.newInstance();
            Map<String,String> vivoJspsToGenerators = (Map) object;
            if( vivoJspsToGenerators != null )
            map.putAll( vivoJspsToGenerators );

        } catch (Throwable th){
            log.error( "could not load VIVO jsp mappings",th );
        }
    }

    public static <T> T createFor(String jsp, Class<T> clazz) {
        try {
            if (jspsToGenerators.containsKey(jsp)) {
                Class classDefinition = Class.forName(jspsToGenerators.get(jsp));
                if (clazz.isAssignableFrom(classDefinition)) {
                    return (T) classDefinition.newInstance();
                }
            }
        } catch (Throwable th) {
        }

        return null;
    }
}
