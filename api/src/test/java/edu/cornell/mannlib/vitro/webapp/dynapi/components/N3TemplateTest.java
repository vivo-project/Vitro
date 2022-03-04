package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.ProcessRdfForm;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class N3TemplateTest {

    @Test
    public void testtest() throws Exception {
        String testN3 = "\"<http://meetings.example.com/cal#m1> <homePage> <-3.15a> .\"^^xsd:string";
        List<Model> returnedModels= ProcessRdfForm.parseN3ToRDF(Arrays.asList(testN3), ProcessRdfForm.N3ParseType.REQUIRED);
        System.out.println("stall");
    }
}
