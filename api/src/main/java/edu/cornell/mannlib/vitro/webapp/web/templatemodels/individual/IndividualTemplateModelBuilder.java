package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individuallist.ListedIndividual;

public class IndividualTemplateModelBuilder {
    static IIndividualTemplateModelBuilder customBuilder = null;

    static public IndividualTemplateModel build(Individual individual, VitroRequest vreq) {
        if (customBuilder != null) {
            return customBuilder.build(individual, vreq);
        }
        
        return new IndividualTemplateModel(individual, vreq);
    }

    static public void setCustomBuilder(IIndividualTemplateModelBuilder builder) {
        customBuilder = builder;
    }

    public interface IIndividualTemplateModelBuilder {
        public IndividualTemplateModel build(Individual individual, VitroRequest vreq);
    }
}
