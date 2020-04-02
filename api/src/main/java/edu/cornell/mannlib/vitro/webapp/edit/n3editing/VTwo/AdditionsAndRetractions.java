/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo;

import java.io.StringWriter;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * This is a data structure to allow a method to return
 * a pair of Model objects for additions and retractions.
 *
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

    @Override
    public String toString(){
        String str = "{";

        str += "\nadditions:[";
        if( getAdditions() != null ) {
           StringWriter writer = new StringWriter();
           // UQAM-Bug Replacing N3-PP by N3 (N3-PP does not exist in Jena
           getAdditions().write(writer, "N3");
           str += "\n" + writer.toString() + "\n";
        }
        str += "],\n";

        str += "\nretractions:[";
        if( getRetractions() != null ) {
           StringWriter writer = new StringWriter();
           getRetractions().write(writer, "N3");
           str += "\n" + writer.toString() + "\n";
        }
        str += "],\n";

        return str;
    }

}
