package edu.cornell.mannlib.vitro.webapp.dao;


public class DisplayVocabulary {
    
    /* Namespace for display vocabulary */
    public static final String DISPLAY_NS = "http://vitro.mannlib.cornell.edu/ontologies/display/1.1#";
    private static final String NS = DISPLAY_NS;
    
    /* Page types */
    public static final String PAGE_TYPE = NS + "Page";
    public static final String HOME_PAGE_TYPE = NS + "HomePage";
    public static final String CLASSGROUP_PAGE_TYPE = NS + "ClassGroupPage";
    
    /* Object Properties */
    public static final String FOR_CLASSGROUP = NS + "forClassGroup";
    
    /* Data Properties */
    public static final String URL_MAPPING = NS + "urlMapping";
    public static final String TITLE = NS + "title";
    public static final String REQUIRES_BODY_TEMPLATE = NS + "requiresBodyTemplate"; 
}
