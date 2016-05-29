package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

public class ListedIndividualBuilder {
    static ILIstedIndividualBuilder customBuilder = null;

    static public ListedIndividual build(Individual individual, VitroRequest vreq) {
        if (customBuilder != null) {
            return customBuilder.build(individual, vreq);
        }

        return new ListedIndividual(individual, vreq);
    }

    static public void setCustomBuilder(ILIstedIndividualBuilder builder) {
        customBuilder = builder;
    }

    public interface ILIstedIndividualBuilder {
        public ListedIndividual build(Individual individual, VitroRequest vreq);
    }
}
