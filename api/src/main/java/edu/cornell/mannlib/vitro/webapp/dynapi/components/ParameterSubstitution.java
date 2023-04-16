package edu.cornell.mannlib.vitro.webapp.dynapi.components;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.EnumUtils;

import edu.cornell.mannlib.vitro.webapp.dynapi.data.conversion.InitializationException;
import edu.cornell.mannlib.vitro.webapp.utils.configuration.Property;

public class ParameterSubstitution {
    
    public enum Direction { FORWARD, BACKWARD }

    private Parameter source;
    private Parameter target;
    private Set<Direction> directions = new HashSet<>(Arrays.asList( Direction.FORWARD, Direction.BACKWARD ));

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#substitutionDirection", minOccurs = 0, maxOccurs = 1)
    public void setDirection(String direction) throws InitializationException{
        String upperCase = direction.toUpperCase();
        if (EnumUtils.isValidEnum(Direction.class, upperCase)) {
            Direction directionValue = EnumUtils.getEnum(Direction.class, upperCase);
            directions.clear();
            directions.add(directionValue);
        } else {
            String message = "Provided direction '" + direction + "' is not supported. Supported directions: "
                    + Arrays.asList(Direction.values());
            throw new InitializationException(message);
        }
    }
    
    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#substitutionSource", minOccurs = 1, maxOccurs = 1)
    public void setSource(Parameter source) {
        this.source = source;
    }
    
    public Parameter getSource() {
        return source;
    }

    @Property(uri = "https://vivoweb.org/ontology/vitro-dynamic-api#substitutionTarget", minOccurs = 1, maxOccurs = 1)
    public void setTarget(Parameter param) {
        target = param;
    }

    public Parameter getTarget() {
        return target;
    }

    public boolean containsDirection(Direction direction) {
        return directions.contains(direction);
    }
}
