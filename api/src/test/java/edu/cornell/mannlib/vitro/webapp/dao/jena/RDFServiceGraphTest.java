package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static org.junit.Assert.assertEquals;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ChangeListener;
import edu.cornell.mannlib.vitro.webapp.rdfservice.ModelChange;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;
import edu.cornell.mannlib.vitro.webapp.rdfservice.impl.jena.model.RDFServiceModel;


public class RDFServiceGraphTest extends AbstractTestClass {

    @Test
    /**
     * Test that creating a new model with the same underlying RDFServiceGraph
     * does not result in a new listener registered on that graph.  No matter
     * how many models have been created using a given RDFServiceGraph, an event
     * sent to the last-created model should be heard only once by the 
     * RDFService.
     * @throws RDFServiceException
     */
    public void testEventListening() throws RDFServiceException {
        Model m = ModelFactory.createDefaultModel();
        RDFService rdfService = new RDFServiceModel(m);
        EventsCounter counter = new EventsCounter();
        rdfService.registerListener(counter);
        RDFServiceGraph g = new RDFServiceGraph(rdfService);
        Model model = null;
        for (int i = 0; i < 100; i++) {
            model = RDFServiceGraph.createRDFServiceModel(g);
        }
        model.notifyEvent("event");
        assertEquals(1, counter.getCount());
    }
    
    private class EventsCounter implements ChangeListener {

        private int count = 0;
        
        public int getCount() {
            return count;
        }
        
        @Override
        public void notifyModelChange(ModelChange modelChange) {
            // TODO Auto-generated method stub            
        }

        @Override
        public void notifyEvent(String graphURI, Object event) {
            count++;
        }
        
    }
    
}
