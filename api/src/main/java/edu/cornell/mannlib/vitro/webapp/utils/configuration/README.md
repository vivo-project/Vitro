# package edu.cornell.mannlib.vitro.webapp.utils.configuration;
## Overview
### Purpose
This package consists of `ConfigurationBeanLoader` and associated classes.
`ConfigurationBeanLoader` will instantiate and populate objects according to a 
description encoded in the triples of an RDF Graph, 
and annotations within the Java class of the instantiated object.

The description must include 

+ the URI of exactly one concrete Java class, from which the instance will be created.

The description may also include

+ URIs of Java interfaces which the concrete class implements. 
The description may be use to satisfy a request
for any of those interfaces, as well as a request for the concrete class.

+ Data properties. These will be passed to "property methods" in the instance
as part of the creation/initialization. The data value must be an untyped
literal (String) or a numeric literal (Float).

+ Object properties. The URI is assumed to be that of another loader description.
The loader will attempt to instantiate the described object, and pass it to 
the appropriate property method on the original instance. The result may be a
network of instances, nested to an arbitrary level.

The loader also recognizes two special interfaces: `RequestModelsUser` and `ContextModelsUser`.
If a created instance implements these interfaces, 
the loader will provide the appropriate `ModelAccess` object, 
allowing the instance to access the Vitro triple-stores.

### Examples of use

#### ApplicationSetup

When Vitro starts up, `ApplicationSetup` uses a `ConfigurationBeanLoader` to instantiate the Vitro's component modules. 
The loader creates an RDF Graph from the file `applicationSetup.n3` and instantiates a `SearchEngine` instance, 
a `FileStorage` instance, etc.

Here is some RDF that might be used by `ApplicationSetup`:

      @prefix : <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#> .
      :application 
          a   <java:edu.cornell.mannlib.vitro.webapp.application.ApplicationImpl> ,
              <java:edu.cornell.mannlib.vitro.webapp.modules.Application> ;
          :hasSearchEngine              :instrumentedSearchEngineWrapper ;
          :hasFileStorage               :ptiFileStorage .
      
      :ptiFileStorage 
          a   <java:edu.cornell.mannlib.vitro.webapp.filestorage.impl.FileStorageImplWrapper> ,
              <java:edu.cornell.mannlib.vitro.webapp.modules.fileStorage.FileStorage> .
      
      :instrumentedSearchEngineWrapper 
          a   <java:edu.cornell.mannlib.vitro.webapp.searchengine.InstrumentedSearchEngineWrapper> , 
              <java:edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine> ;
          :wraps :solrSearchEngine .
      
      :solrSearchEngine
          a   <java:edu.cornell.mannlib.vitro.webapp.searchengine.solr.SolrSearchEngine> ,
              <java:edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine> .

In this case, the `ConfigurationBeanLoader` would be asked to load all instances of 
`edu.cornell.mannlib.vitro.webapp.modules.Application`. 
The application individual is declared to be both an `Application` and an `ApplicationImpl`. 
This is valid because `Application` is an interface, and `ApplicationImpl` implements that interface.
An instance of `ApplicationImpl` will be created.

The application instance has two child objects: a `SearchEngine` and a `FileStorage`. 
These objects will also be created, and calls will be made to the application's "property methods" (see below).

The `SearchEngine` in turn has a child object, so that also will be created, and provided to the `SearchEngine`.

#### SearchIndexer

When Vitro's `SearchIndexer` is initialized, it uses a `ConfigurationBeanLoader` to create 
lists of `SearchIndexExcluder`s, `DocumentModifier`s, and `IndexingUriFinder`s. 
Descriptions of these are taken from Vitro's display model.

## Specifications

### ConfigurationBeanLoader
The principal methods are:

+ `public <T> T loadInstance(String uri, Class<T> resultClass) throws ConfigurationBeanLoaderException`
 + Search the graph for triples that describe the `uri`. 
   If the description indicates that the individual is of type `resultClass`, create an instance and populate it.
   Return a reference to the created instance. Throw an exception if the `uri` does not exist in the graph,
   or if the description does not correctly describe an individual.
   
     The `resultClass` may be an interface. In that case, each individual must also have a type statement that refers
     to a concrete class that satisfies the interface. An instance of the concrete class will be created. 
			
+ `public <T> Set<T> loadAll(Class<T> resultClass) throws ConfigurationBeanLoaderException`
 + Search the graph for all individuals of type `resultClass`. For each such individual, call `loadInstance`. 
   Return a set containing the created instances. If no individuals are found, return an empty `Set`.
   
### Restrictions on instantiated classes.
Each class to be instantiated must have a niladic constructor.

### Property methods
When the loader encounters a data property or an object property in a description, 
it will look in the instantiated class for a method tagged with the 
`edu.cornell.mannlib.vitro.webapp.utils.configuration.Property` annotation.

For example:

    	@Property(
    	    uri = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationSetup#hasConfigurationTripleSource" 
    	    minOccurs = 1,
    	    maxOccurs = 3)
    	public void setTBoxReasonerModule(TBoxReasonerModule module) {
    	    this.module = module;
    	}
    	    
     
In more detail:

+ A class must contain exactly one method that serves each property URI in the description.
+ The description need not include properies for all of the property methods in the class.
+ Each property method must be public, must have exactly one parameter, and must return null.
+ The name of the property method is immaterial, except that there must not be another method
with the same name in the class.
+ Property methods in superclasses will be recognized and accepted, but they may not be
overridden in a subclass.
+ If `minOccurs` is omitted, the default is `0`. If `minOccurs` is provided, it must be non-negative.
+ If `maxOccurs` is omitted, the default is `MAXINT`. If `maxOccurs` is provided, it must not be less than `minOccurs`.

When instantiating:

+ The parameter on a property method must match the value supplied in the RDF description.
 + If the type of the parameter is `Float`, the object of the triple must be a numeric literal, or
an untyped literal that can be parsed as a number.
 + If the type of the parameter is `String`, the object of the triple must be a String literal or an untyped literal.
 + If the type of the parameter is another class, then the object of the triple must be the URI of
another RDF description, from which the loader can create an instance of the required class.
+ The number of values for a given property URI must not be less than the `minOccurs` value on the corresponding property method.
+ The number of values for a given property URI must not be greater than the `maxOccurs` value on the corresponding property method.

### Validation methods
When the loader has satisfied all of the properties in an instance, it will
look in the instantiated class for any methods tagged with the
`edu.cornell.mannlib.vitro.webapp.utils.configuration.Validation` annotation.

For example:

    	@Validation
    	public void validate() throws Exception {
        	if (baseUri == null) {
            	throw new IllegalStateException(
                    	"Configuration did not include a BaseURI.");
        	}
    	}

Each such method will be called by the loader, and provides a opportunity to
confirm that the bean has been properly initialized.

Again, in detail:

+ Each validation method must be public, must accept no parameters, and must return null.
+ The name of the validation method is immaterial, except that there must not be another 
+ method with the same name in the lass.
+ Validation methods in superclasses will be called, but may not be overridden in a subclass.

### Life cycle
For each instance that the loader creates, the loader will:

+ Call the appropriate property method for each property in the description. 
For object properties, this includes recursive calls to create subordinate objects.
The order of property method calls is undefined.
+ Call the validation methods on the class. The order of validation method calls is undefined.  

If any property method or validation method throws an exception, the process stops,
and the exception is propagated upward.