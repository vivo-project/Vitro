/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import javax.servlet.http.HttpSession;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.controller.EditRequestDispatchController;

/**
 * Generates the edit configuration for a default property form.
 *
 */
public class NewIndividualFormGenerator implements EditConfigurationGenerator {
			  
    @SuppressWarnings("rawtypes")
    @Override
    public EditConfigurationVTwo getEditConfiguration(VitroRequest vreq, HttpSession session) throws Exception {
        String editConfGeneratorName = EditRequestDispatchController.processEditConfGeneratorName(vreq);
        if( editConfGeneratorName == null ){
            editConfGeneratorName = DefaultNewIndividualFormGenerator.class.getName();
        }
        
        EditConfigurationGenerator EditConfigurationVTwoGenerator = null;
        
        Object object = null;
        try {
            Class classDefinition = Class.forName(editConfGeneratorName);
            object = classDefinition.newInstance();
            EditConfigurationVTwoGenerator = (EditConfigurationGenerator) object;
        } catch (InstantiationException e) {
            System.out.println(e);
        } catch (IllegalAccessException e) {
            System.out.println(e);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }       
        
        if(EditConfigurationVTwoGenerator == null){
            throw new Error("Could not find EditConfigurationVTwoGenerator " + editConfGeneratorName);          
        } else {
            return EditConfigurationVTwoGenerator.getEditConfiguration(vreq, session);
        }
        
    }    

}
