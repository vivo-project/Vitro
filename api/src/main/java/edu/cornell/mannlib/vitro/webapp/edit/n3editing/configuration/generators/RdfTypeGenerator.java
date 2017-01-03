/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators;

import java.util.Arrays;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationVTwo;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.ChildVClassesOptions;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.fields.FieldVTwo;

public class RdfTypeGenerator extends BaseEditConfigurationGenerator implements EditConfigurationGenerator  {
    
    private Log log = LogFactory.getLog(RdfTypeGenerator.class);
    
    @Override
    public EditConfigurationVTwo getEditConfiguration( VitroRequest vreq, HttpSession session) {        
        EditConfigurationVTwo editConfig = new EditConfigurationVTwo();
        
        //get editkey and url of form
        initBasics(editConfig, vreq);        
        initPropertyParameters(vreq, session, editConfig);
        initObjectPropForm(editConfig, vreq);                         
        
        editConfig.addUrisInScope("rdfTypeUri", Arrays.asList( RDF.type.getURI() ) );       
        
        editConfig.setN3Required( " ?subject ?rdfTypeUri ?object . ");                        
                
        //set fields
        editConfig.setUrisOnForm("object");
        editConfig.addField( new FieldVTwo( )
            .setName("object")
            .setOptions( 
                    new ChildVClassesOptions(OWL.Class.getURI()) )            
        );
                      
        editConfig.setTemplate("rdfTypeForm.ftl");
        return editConfig;
    }

}
