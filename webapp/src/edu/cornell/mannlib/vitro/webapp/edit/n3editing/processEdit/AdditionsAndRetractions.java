package edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit;

import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * This is a data structure to allow a method to return
 * a pair of Model objects for additions and retractions.
 * 
 * Move this to its own class    
 */
public class AdditionsAndRetractions {
    Model additions;
    Model retractions;
    
    public AdditionsAndRetractions(List<Model>adds, List<Model>retractions){
        Model allAdds = ModelFactory.createDefaultModel();
        Model allRetractions = ModelFactory.createDefaultModel();
        
        for( Model model : adds ) {
            allAdds.add( model );
        }
        for( Model model : retractions ){
            allRetractions.add( model );
        }
        
        this.setAdditions(allAdds);
        this.setRetractions(allRetractions);
    }
    
    public AdditionsAndRetractions(Model add, Model retract){
        this.additions = add;
        this.retractions = retract;
    }
    
    public Model getAdditions() {
        return additions;
    }
    public void setAdditions(Model additions) {
        this.additions = additions;
    }
    public Model getRetractions() {
        return retractions;
    }
    public void setRetractions(Model retractions) {
        this.retractions = retractions;
    }
    
}