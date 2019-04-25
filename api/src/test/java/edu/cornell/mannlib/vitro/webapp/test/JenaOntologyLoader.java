/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * this is a class to load owl files for testing.
 * @author bdc34
 *
 */
public class JenaOntologyLoader {
    public OntModel ontModel = null;

    /**
     * This should load the system with classes, data properties and
     * object properties that the vitro systems needs.
     *
     * @throws Exception
     */
    OntModel loadSystemAndUsers() throws Exception{
        Model model = ModelFactory.createDefaultModel();
        for( String ont : systemOnts){
            InputStream in = this.getClass().getResourceAsStream(ont);
            model.read(in,null);
            in.close();
        }
        ontModel = ModelFactory.createOntologyModel(ONT_MODEL_SPEC,model);
        ontModel.prepare();
        return ontModel;
    }


    /**
     * Loads a owl file into the ontModel.  Looks for files on classpath.
     * example: loadSpecialVivoModel("/testontologies/smallVivo-20070809.owl")
     *
     * @param junk Junk value
     * @throws IOException
     */
    OntModel loadSpecialVivoModel(String junk) throws IOException{
        InputStream in = this.getClass().getResourceAsStream(junk);
        Model model = ModelFactory.createDefaultModel();
        model.read(in,null);
        in.close();

        ontModel.add(model);
        ontModel.prepare();
        return ontModel;
    }

    static String systemOnts[] ={
        "/testontologies/vitro1.owl",
        "/testontologies/vivo-users.owl" };

    static String testOnt[] ={
        "/testontologies/smallVivo-20070809.owl" };

    static OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_DL_MEM; // no additional entailment reasoning

}
