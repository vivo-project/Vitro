/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.context;

/**
 * When querying for application configuration information,
 * this class can be used to represent the application display
 * context that the information will be used in.
 * 
 *  Ex.  If querying about how to display a foaf:Person on
 *  the search results page then a ConfigContext object could be
 *  created that encodes this specific display context.
 * 
 * Represents Individuals of type 
 * <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration:ConfigContext>
 * from the application configuration data.
 *  
 *  Not sure how to implement this yet or even what it will do.
 *  
 */
public interface ConfigContext {
    /**
     * Use this method to set the URI of the object property,
     * datatype property or class that the context is for.
     * @param uri - URI of the object property,
     * datatype property or class that the context is for.
     * @return 
     */
    public ConfigContext configContextFor( String uri );
    
    public String getConfigContextFor( );
    
    //TODO: are these needed?
    //public ConfigContext inheritingConfigContextFor( String uri );
    //public ConfigContext nonInheritingConfigContextFor( String uri );
    
    /**
     * Use this method to set the URI of the object property,
     * datatype property or class that the context is qualified by.
     * @param uri - URI of the object property,
     * datatype property or class that the context is qualified by.
     * @return 
     */
    public ConfigContext qualifiedBy( String uri );
    
    public String getQuifiedBy();
    
    //TODO: are these needed?
    //public ConfigContext inheritingQualifiedBy( String uri );
    //public ConfigContext nonInheritingQualifiedBy( String uri );
}
