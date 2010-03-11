package edu.cornell.mannlib.vitro.webapp.utils;

public class StringUtils {
    
    public static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

}
