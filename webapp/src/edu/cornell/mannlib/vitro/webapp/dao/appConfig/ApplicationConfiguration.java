/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.appConfig;
 
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
 
/**
 * Vocabulary definitions from vitrotrunk/webapp/web/WEB-INF/ontologies/app/ApplicationConfiguration.n3    
 */
public class ApplicationConfiguration {
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static OntModel m_model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    
    /** <p>The namespace of the vocabulary as a string</p> */
    public static final String NS = "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#";
    
    /** <p>The namespace of the vocabulary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabulary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    public static final Property aggregateEdit = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#aggregateEdit" );
    
    public static final Property collateBySubclass = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#collateBySubclass" );
    
    public static final Property configContextFor = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#configContextFor" );
    
    public static final Property dataGetter = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#dataGetter" );
    
    public static final Property dataSelector = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#dataSelector" );
    
    public static final Property displayLimit = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#displayLimit" );
    
    /** <p>range is intended to be plain literal with language tag.</p> */
    public static final Property displayName = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#displayName" );
    
    public static final Property displayRank = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#displayRank" );
    
    public static final Property entryForm = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#entryForm" );
    
    public static final Property entryLimit = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#entryLimit" );
    
    /** <p>range is intended to be plain literal with language tag.</p> */
    public static final Property fileName = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#fileName" );
    
    public static final Property hasConfiguration = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasConfiguration" );
    
    public static final Property hasDisplayView = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasDisplayView" );
    
    public static final ObjectProperty hasListView = m_model.createObjectProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasListView" );
    
    public static final Property hasShortView = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#hasShortView" );
    
    public static final ObjectProperty inheritingConfigurationFor = m_model.createObjectProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#inheritingConfigurationFor" );
    
    public static final ObjectProperty inheritingQualifiedBy = m_model.createObjectProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#inheritingQualifiedBy" );
    
    /** <p>'text/html' for html contentrange is intended to be plain literal with a language 
     *  tag.</p>
     */
    public static final Property mediaType = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#mediaType" );
    
    public static final Property modelConstructor = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#modelConstructor" );
    
    public static final ObjectProperty nonInheritingConfigurationFor = m_model.createObjectProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#nonInheritingConfigurationFor" );
    
    public static final ObjectProperty nonInheritingQualifiedBy = m_model.createObjectProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#nonInheritingQualifiedBy" );
    
    public static final Property offerCreate = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#offerCreate" );
    
    public static final Property offerEdit = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#offerEdit" );
    
    /** <p>range is intended to be plain literal with language tag.</p> */
    public static final Property publicDescription = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#publicDescription" );
    
    public static final Property qualifiedBy = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#qualifiedBy" );
    
    /** <p>range is intended to be plain literal with language tag.</p> */
    public static final Property queryString = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#queryString" );
    
    public static final Property selectFromExisting = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#selectFromExisting" );
    
    public static final Property suppressDisplay = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#suppressDisplay" );
    
    public static final Property suppressEditControl = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#suppressEditControl" );
    
    public static final Property templateFile = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#templateFile" );
    
    public static final Property valuesOfType = m_model.createProperty( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#valuesOfType" );
    
    public static final Resource AddControl = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#AddControl" );
    
    public static final Resource Application = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#Application" );
    
    public static final Resource ApplicationConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ApplicationConfig" );
    
    public static final Resource ChangeControl = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ChangeControl" );
    
    public static final Resource ClassDisplayConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ClassDisplayConfig" );
    
    public static final Resource ClassEditConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ClassEditConfig" );
    
    public static final Resource ConfigContext = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ConfigContext" );
    
    public static final Resource DatatypePropertyDisplayConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#DatatypePropertyDisplayConfig" );
    
    public static final Resource DatatypePropertyEditConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#DatatypePropertyEditConfig" );
    
    public static final Resource DeleteControl = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#DeleteControl" );
    
    public static final Resource DisplayConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#DisplayConfig" );
    
    public static final Resource DisplayView = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#DisplayView" );
    
    public static final Resource EditConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#EditConfig" );
    
    public static final Resource EditControl = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#EditControl" );
    
    public static final Resource FreemarkerTemplate = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#FreemarkerTemplate" );
    
    public static final Resource IndexPage = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#IndexPage" );
    
    public static final Resource ListDisplayView = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ListDisplayView" );
    
    public static final Resource MenuPage = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#MenuPage" );
    
    public static final Resource ObjectPropertyDisplayConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ObjectPropertyDisplayConfig" );
    
    public static final Resource ObjectPropertyEditConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ObjectPropertyEditConfig" );
    
    public static final Resource OfferEditOption = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#OfferEditOption" );
    
    public static final Resource Page = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#Page" );
    
    public static final Resource ProfilePage = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ProfilePage" );
    
    public static final Resource PropertyDisplayConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#PropertyDisplayConfig" );
    
    public static final Resource PropertyEditConfig = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#PropertyEditConfig" );
    
    public static final Resource SearchPage = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#SearchPage" );
    
    public static final Resource ShortDisplayView = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ShortDisplayView" );
    
    public static final Resource SparqlConstructQuery = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#SparqlConstructQuery" );
    
    public static final Resource SparqlQuery = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#SparqlQuery" );
    
    public static final Resource SparqlSelectQuery = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#SparqlSelectQuery" );
    
    public static final Resource Template = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#Template" );
    
    public static final Resource defer = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#defer" );
    
    public static final Resource doNotOfferForEdit = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#doNotOfferForEdit" );
    
    public static final Resource ifStatement = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#ifStatement" );
    
    public static final Resource offerForEdit = m_model.createResource( "http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#offerForEdit" );
    
}
