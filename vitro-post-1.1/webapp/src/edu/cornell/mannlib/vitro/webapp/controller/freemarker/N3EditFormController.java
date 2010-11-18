/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditSubmission;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateModelException;

/**
 * This controller is intended to place N3 editing data into the 
 * FM data model and output the FM template for the form.
 */
public class N3EditFormController extends FreemarkerHttpServlet{

    @Override
    protected ResponseValues processRequest(VitroRequest vreq) {
        try{
            //get edit objects 
            HttpSession session = vreq.getSession(false);
            if( session == null )
                throw new Exception("Cannot get session");
            
            EditConfiguration editConfig = EditConfiguration.getConfigFromSession(session, vreq);
            if(editConfig == null )
                throw new Exception("Cannot get EditConfiguration from session");
                        
            EditSubmission editSubmission = EditSubmission.getEditSubmissionFromSession(session, editConfig);
                        
            //add edit info to the template data model and call template
            Map<String,Object> map = makeEditDataMap(editConfig, editSubmission);
            
            //how do I add css or js?
            //The jsp is adding css and js like this:
            /*
             List<String> customJs = new ArrayList<String>(Arrays.asList(JavaScript.JQUERY_UI.path(),
                                                                JavaScript.CUSTOM_FORM_UTILS.path(),
                                                                "/edit/forms/js/customFormWithAutocomplete.js"                                                    
                                                               ));            
            request.setAttribute("customJs", customJs);

            List<String> customCss = new ArrayList<String>(Arrays.asList(Css.JQUERY_UI.path(),
                                                   Css.CUSTOM_FORM.path(),
                                                   "/edit/forms/css/customFormWithAutocomplete.css"
                                                  ));                                                                                                                                   
            request.setAttribute("customCss", customCss);            
             */
            
            //What needs to happen??
            //map.put(???);
            
            return new TemplateResponseValues(editConfig.getTemplate(), map);
        }catch(Exception ex){
            return new ExceptionResponseValues(ex);
        }        
    }

    /**
     * This method get data out of the editConfig and editSubmission for the template. 
     * @throws TemplateModelException 
     */
    private Map<String, Object> makeEditDataMap(EditConfiguration editConfig,
            EditSubmission editSubmission) throws TemplateModelException {
        
        Map<String,Object> map = new HashMap<String,Object>();
        
        //This dosen't seem to be the right thing:
        map.put("editConfig", ObjectWrapper.BEANS_WRAPPER.wrap(editConfig));
        
        //<@dump var="editConfig"/> outputs: 
        /*
         * Template variable dump
            Variable name: editConfig
            Type: string
            Value: edu.cornell.mannlib.vitro.webapp.edit.n3editing.EditConfiguration@1e60601
         */
        
        if( editSubmission != null)
            map.put("editSubmission", ObjectWrapper.BEANS_WRAPPER.wrap(editSubmission));                
        return map;                
    }
}
