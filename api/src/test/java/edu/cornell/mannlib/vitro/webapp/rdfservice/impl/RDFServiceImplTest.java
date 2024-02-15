package edu.cornell.mannlib.vitro.webapp.rdfservice.impl;

import java.util.Iterator;
import java.util.List;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.adapters.VitroModelFactory;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;

public class RDFServiceImplTest {

    @Test
    public void getConcurrentGraphUrisTest() throws RDFServiceException, InterruptedException {
        Dataset testDataSet = DatasetFactory.createGeneral();
        Model m1 = VitroModelFactory.createModel();
        testDataSet.addNamedModel("test:init1", m1);
        Model m2 = VitroModelFactory.createModel();
        testDataSet.addNamedModel("test:init2", m2);
        RDFServiceImpl rdfService = new RDFServiceModel(testDataSet);
        rdfService.getGraphURIs();
        long i = 0;
        while (rdfService.rebuildGraphURICache) {
            Thread.sleep(10);
            i += 10;
            if (i > 10000) {
                throw new RuntimeException();
            }
        }
        List<String> uris = rdfService.getGraphURIs();
        for (Iterator<String> iterator = uris.iterator(); iterator.hasNext();) {
            Model m = VitroModelFactory.createModel();
            testDataSet.addNamedModel("test" + i, m);
            rdfService.rebuildGraphURICache = true;
            rdfService.getGraphURIs();
            while (rdfService.rebuildGraphURICache) {
                Thread.sleep(10);
                i += 10;
                if (i > 20000) {
                    throw new RuntimeException();
                }
            }
            iterator.next();
            i++;
        }
    }

}
