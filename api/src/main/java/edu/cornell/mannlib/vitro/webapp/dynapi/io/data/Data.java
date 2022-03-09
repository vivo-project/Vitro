package edu.cornell.mannlib.vitro.webapp.dynapi.io.data;

import java.util.List;

public interface Data {

    int IOUnknown = 0;
    int IOObject = 1;
    int IOArray = 2;
    int IOInteger = 3;
    int IODecimal = 4;
    int IOBoolean = 5;
    int IOString = 6;

    List<String> getAsString();

}
