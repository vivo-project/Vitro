package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.HashSet;
import java.util.Set;

public class Scopes {

    public static Parameters computeInitialRequirements(Action action) {
        Parameters actionResult = action.getProvidedParams();
        Set<Link> links = action.getNextLinks();
        Set<Parameters> requirements = new HashSet<Parameters>();

        for (Link link : links) {
            computeRequirements(link, actionResult);
        }

        return new Parameters();
    }

    private static void computeRequirements(Link link, Parameters actionResult) {
        // TODO Auto-generated method stub
    }

}
