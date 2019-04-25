/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;

import org.apache.jena.ontology.OntModel;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.IdModelSelector;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.StandardModelSelector;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

public abstract class BaseEditConfigurationGenerator implements EditConfigurationGenerator {

    /* constants */
    public static final String DEFAULT_NS_FOR_NEW_RESOURCE= "";

    /* Utility Methods */

    /**
     * Sets up the things that should be done for just about every form.
     */
    void initBasics(EditConfigurationVTwo editConf, VitroRequest vreq){
        String editKey = EditConfigurationUtils.getEditKey(vreq);
        editConf.setEditKey(editKey);

        String formUrl = EditConfigurationUtils.getFormUrlWithoutContext(vreq);
        editConf.setFormUrl(formUrl);
    }

    /**
     * Method that setups up a form for basic object or data property editing.
     */
    void  initPropertyParameters(VitroRequest vreq, HttpSession session, EditConfigurationVTwo editConfiguration) {
        //set up the subject URI based on request
        String subjectUri = EditConfigurationUtils.getSubjectUri(vreq);
        editConfiguration.setSubjectUri(subjectUri);

        //set up predicate URI based on request
        String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
        editConfiguration.setPredicateUri(predicateUri);

        editConfiguration.setUrlPatternToReturnTo("/individual");
        editConfiguration.setEntityToReturnTo(subjectUri);
    }

    void initObjectPropForm(EditConfigurationVTwo editConfiguration,VitroRequest vreq) {
        editConfiguration.setObject( EditConfigurationUtils.getObjectUri(vreq) );
    }

    //Prepare for update or non-update
    //Originally included in edit request dispatch controller but moved here due to
    //exceptions such as default add missing individual form
    void prepare(VitroRequest vreq, EditConfigurationVTwo editConfig) {
        //setup the model selectors for query, write and display models on editConfig
        setupModelSelectorsFromVitroRequest(vreq, editConfig);

        OntModel queryModel = ModelAccess.on(vreq).getOntModel();

        if( editConfig.getSubjectUri() == null)
            editConfig.setSubjectUri( EditConfigurationUtils.getSubjectUri(vreq));
        if( editConfig.getPredicateUri() == null )
            editConfig.setPredicateUri( EditConfigurationUtils.getPredicateUri(vreq));

        String objectUri = EditConfigurationUtils.getObjectUri(vreq);
        Integer dataKey = EditConfigurationUtils.getDataHash(vreq);
        if (objectUri != null && ! objectUri.trim().isEmpty()) {
            // editing existing object
            if( editConfig.getObject() == null)
                editConfig.setObject( EditConfigurationUtils.getObjectUri(vreq));
            editConfig.prepareForObjPropUpdate(queryModel);
        } else if( dataKey != null ) { // edit of a data prop statement
            //do nothing since the data prop form generator must take care of it
            editConfig.prepareForDataPropUpdate(queryModel, vreq.getWebappDaoFactory().getDataPropertyDao());
        } else{
            //this might be a create new or a form
            editConfig.prepareForNonUpdate(queryModel);
        }
    }

    /**
     * Setup the model selectors using the models set in the VitroRequest. Call this
     * if the form should use the selectors from the VitroRequest.  Don't call this
     * and setup specific selectors if the custom form needs to always target specific
     * models.
     */
    public void setupModelSelectorsFromVitroRequest(VitroRequest vreq, EditConfigurationVTwo editConfig){
        if( ! StringUtils.isEmpty( vreq.getNameForWriteModel() )  ){
            editConfig.setWriteModelSelector(new IdModelSelector( vreq.getNameForWriteModel() ));
            editConfig.setWriteModelId( vreq.getNameForWriteModel());
        }else{
            editConfig.setWriteModelSelector( StandardModelSelector.selector );
        }

        if( ! StringUtils.isEmpty( vreq.getNameForABOXModel() )){
            editConfig.setQueryModelSelector( new IdModelSelector(vreq.getNameForABOXModel() ));
            editConfig.setResourceModelSelector( new IdModelSelector(vreq.getNameForABOXModel() ));
            editConfig.setAboxModelId(vreq.getNameForABOXModel());
        }else{
            editConfig.setQueryModelSelector( StandardModelSelector.selector );
            editConfig.setResourceModelSelector( StandardModelSelector.selector );
        }

        if( ! StringUtils.isEmpty( vreq.getNameForTBOXModel() )){
            editConfig.setTboxModelId(vreq.getNameForTBOXModel());
        }
    }

    /**
     * Method to turn Strings or multiple List<String> to List<String>.
     * Only accepts String and List<String> as multi args.
     */
    static List<String> list( Object ... objs){
        List<String> rv = new ArrayList<String>();
        for( Object obj: objs){
            if( obj instanceof String)
                rv.add((String)obj);
            else if( obj instanceof Iterable){
                for( Object innerObj: (Iterable)obj){
                    if( innerObj instanceof String){
                        rv.add((String)innerObj);
                    }else{
                        throw new Error("list may only take String " +
                        		"and List<String>. It does not accept List<"
                                + innerObj.getClass().getName() + ">");
                    }
                }
            }else{
                throw new Error("list may only take String " +
                        "and List<String>. It does not accept "
                        + obj.getClass().getName() );
            }
        }
        return rv;
    }
}
