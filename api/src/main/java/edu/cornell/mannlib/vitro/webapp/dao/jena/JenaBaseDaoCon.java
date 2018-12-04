/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class JenaBaseDaoCon {
    
    public JenaBaseDaoCon() {
        LINK.addProperty(PROPERTY_CUSTOMSHORTVIEWANNOT, _constModel.createTypedLiteral("linkShortView.jsp"));
        PRIMARY_LINK.setLabel("Primary Link", "en-US");
        PRIMARY_LINK.addProperty(PROPERTY_STUBOBJECTPROPERTYANNOT,_constModel.createTypedLiteral(true));
        PRIMARY_LINK.addProperty(PROPERTY_CUSTOMENTRYFORMANNOT, _constModel.createTypedLiteral("defaultLinkForm.jsp"));
        PRIMARY_LINK.setRange(LINK);
        PRIMARY_LINK.addProperty(PROPERTY_OFFERCREATENEWOPTIONANNOT, _constModel.createTypedLiteral(true));
        PRIMARY_LINK.addProperty(PROPERTY_SELECTFROMEXISTINGANNOT, _constModel.createTypedLiteral(false));
        
        ADDITIONAL_LINK.setLabel("Additional Link", "en-US");        
        ADDITIONAL_LINK.setRange(LINK); //apparently does not work to have prop.getRangeVClass() return a non-null VClass
        ADDITIONAL_LINK.addProperty(PROPERTY_STUBOBJECTPROPERTYANNOT,_constModel.createTypedLiteral(true));
        ADDITIONAL_LINK.addProperty(PROPERTY_CUSTOMENTRYFORMANNOT, _constModel.createTypedLiteral("defaultLinkForm.jsp"));
        ADDITIONAL_LINK.addProperty(PROPERTY_OFFERCREATENEWOPTIONANNOT, _constModel.createTypedLiteral(true));
        ADDITIONAL_LINK.addProperty(PROPERTY_SELECTFROMEXISTINGANNOT, _constModel.createTypedLiteral(false));
        
        VITRO_PUBLIC_ONTOLOGY.setLabel("Vitro Public Ontology", null);
    }

    
    private OntModel _constModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    
    /* ***************** Vitro ontology constants ***************** */

    protected AnnotationProperty DATAPROPERTY_ISEXTERNALID = _constModel.createAnnotationProperty(VitroVocabulary.DATAPROPERTY_ISEXTERNALID);
        
    protected AnnotationProperty HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);    
    protected AnnotationProperty PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);    
    protected AnnotationProperty HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.HIDDEN_FROM_PUBLISH_BELOW_ROLE_LEVEL_ANNOT);    
    
    protected AnnotationProperty SEARCH_BOOST_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.SEARCH_BOOST_ANNOT);
    
    protected AnnotationProperty EXAMPLE = _constModel.createAnnotationProperty(VitroVocabulary.EXAMPLE_ANNOT);
    protected AnnotationProperty DESCRIPTION_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.DESCRIPTION_ANNOT);
    protected AnnotationProperty PUBLIC_DESCRIPTION_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PUBLIC_DESCRIPTION_ANNOT);
    protected AnnotationProperty SHORTDEF = _constModel.createAnnotationProperty(VitroVocabulary.SHORTDEF);
    
    protected AnnotationProperty EDITING = _constModel.createAnnotationProperty(VitroVocabulary.EDITING);

    protected DatatypeProperty   MONIKER = _constModel.createDatatypeProperty(VitroVocabulary.MONIKER);
    protected OntClass           CLASSGROUP = _constModel.createClass(VitroVocabulary.CLASSGROUP);
    protected AnnotationProperty IN_CLASSGROUP = _constModel.createAnnotationProperty(VitroVocabulary.IN_CLASSGROUP);
    protected DatatypeProperty   MODTIME = _constModel.createDatatypeProperty(VitroVocabulary.MODTIME);

    protected DatatypeProperty   DISPLAY_RANK = _constModel.createDatatypeProperty(VitroVocabulary.DISPLAY_RANK);
    protected AnnotationProperty DISPLAY_RANK_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.DISPLAY_RANK_ANNOT);
    protected AnnotationProperty DISPLAY_LIMIT = _constModel.createAnnotationProperty(VitroVocabulary.DISPLAY_LIMIT);
    protected AnnotationProperty EXAMPLE_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.EXAMPLE_ANNOT);

    protected AnnotationProperty PROPERTY_ENTITYSORTFIELD = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_ENTITYSORTFIELD);
    protected AnnotationProperty PROPERTY_ENTITYSORTDIRECTION = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_ENTITYSORTDIRECTION);
    protected AnnotationProperty PROPERTY_OBJECTINDIVIDUALSORTPROPERTY = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_OBJECTINDIVIDUALSORTPROPERTY);
    protected AnnotationProperty PROPERTY_FULLPROPERTYNAMEANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_FULLPROPERTYNAMEANNOT);
    protected AnnotationProperty PROPERTY_CUSTOMENTRYFORMANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_CUSTOMENTRYFORMANNOT);
    protected AnnotationProperty PROPERTY_CUSTOMDISPLAYVIEWANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_CUSTOMDISPLAYVIEWANNOT);
    protected AnnotationProperty PROPERTY_CUSTOMSHORTVIEWANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_CUSTOMSHORTVIEWANNOT);
    protected AnnotationProperty PROPERTY_CUSTOM_LIST_VIEW_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_CUSTOM_LIST_VIEW_ANNOT);
    protected AnnotationProperty PROPERTY_CUSTOMSEARCHVIEWANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_CUSTOMSEARCHVIEWANNOT);
    protected AnnotationProperty PROPERTY_SELECTFROMEXISTINGANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_SELECTFROMEXISTINGANNOT);
    protected AnnotationProperty PROPERTY_OFFERCREATENEWOPTIONANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_OFFERCREATENEWOPTIONANNOT);
    protected AnnotationProperty PROPERTY_INPROPERTYGROUPANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_INPROPERTYGROUPANNOT);
    protected AnnotationProperty PROPERTY_COLLATEBYSUBCLASSANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_COLLATEBYSUBCLASSANNOT);
    protected AnnotationProperty PROPERTY_STUBOBJECTPROPERTYANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT);
    protected AnnotationProperty PROPERTY_EDITLINKSUPPRESSED = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_EDITLINKSUPPRESSED);
    protected AnnotationProperty PROPERTY_ADDLINKSUPPRESSED = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_ADDLINKSUPPRESSED);
    protected AnnotationProperty PROPERTY_DELETELINKSUPPRESSED = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_DELETELINKSUPPRESSED);
    
    protected OntClass 		     PROPERTYGROUP = _constModel.createClass(VitroVocabulary.PROPERTYGROUP);

    protected OntClass           LINK = _constModel.createClass(VitroVocabulary.LINK);
    protected ObjectProperty     PRIMARY_LINK = _constModel.createObjectProperty(VitroVocabulary.PRIMARY_LINK);
    protected ObjectProperty     ADDITIONAL_LINK = _constModel.createObjectProperty(VitroVocabulary.ADDITIONAL_LINK);
    protected DatatypeProperty   LINK_ANCHOR = _constModel.createDatatypeProperty(VitroVocabulary.LINK_ANCHOR);
    protected DatatypeProperty   LINK_URL = _constModel.createDatatypeProperty(VitroVocabulary.LINK_URL);
    protected DatatypeProperty   LINK_TYPE = _constModel.createDatatypeProperty(VitroVocabulary.LINK_TYPE);
    protected DatatypeProperty   LINK_DISPLAYRANK = _constModel.createDatatypeProperty(VitroVocabulary.LINK_DISPLAYRANK_URL);

    protected  OntClass           PORTAL = _constModel.createClass(VitroVocabulary.PORTAL);
    protected  DatatypeProperty   APPLICATION_THEMEDIR = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_THEMEDIR);
    protected  DatatypeProperty   APPLICATION_CONTACTMAIL = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_CONTACTMAIL);
    protected  DatatypeProperty   APPLICATION_CORRECTIONMAIL = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_CORRECTIONMAIL);   
    protected  DatatypeProperty   APPLICATION_ABOUTTEXT = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_ABOUTTEXT);
    protected  DatatypeProperty   APPLICATION_ACKNOWLEGETEXT = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_ACKNOWLEGETEXT);    
    protected  DatatypeProperty   APPLICATION_COPYRIGHTURL = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_COPYRIGHTURL);
    protected  DatatypeProperty   APPLICATION_COPYRIGHTANCHOR = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_COPYRIGHTANCHOR);
    
    protected AnnotationProperty  ONTOLOGY_PREFIX_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.ONTOLOGY_PREFIX_ANNOT);
    
    protected  Ontology           VITRO_PUBLIC_ONTOLOGY = _constModel.createOntology(VitroVocabulary.VITRO_PUBLIC_ONTOLOGY);

    protected  ObjectProperty     IND_MAIN_IMAGE = _constModel.createObjectProperty(VitroVocabulary.IND_MAIN_IMAGE);

    /* ***************** User Account Model constants ***************** */

    protected  OntClass           USERACCOUNT = _constModel.createClass(VitroVocabulary.USERACCOUNT);
    protected  OntClass           USERACCOUNT_ROOT_USER = _constModel.createClass(VitroVocabulary.USERACCOUNT_ROOT_USER);
    protected  DatatypeProperty   USERACCOUNT_EMAIL_ADDRESS = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_EMAIL_ADDRESS);
    protected  DatatypeProperty   USERACCOUNT_FIRST_NAME = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_FIRST_NAME);
    protected  DatatypeProperty   USERACCOUNT_LAST_NAME = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_LAST_NAME);
    protected  DatatypeProperty   USERACCOUNT_ARGON2_PASSWORD = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_ARGON2_PASSWORD);
    protected  DatatypeProperty   USERACCOUNT_MD5_PASSWORD = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_MD5_PASSWORD);
    protected  DatatypeProperty   USERACCOUNT_OLD_PASSWORD = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_OLD_PASSWORD);
    protected  DatatypeProperty   USERACCOUNT_LOGIN_COUNT = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_LOGIN_COUNT);
    protected  DatatypeProperty   USERACCOUNT_LAST_LOGIN_TIME = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_LAST_LOGIN_TIME);
    protected  DatatypeProperty   USERACCOUNT_STATUS = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_STATUS);
    protected  DatatypeProperty   USERACCOUNT_PASSWORD_LINK_EXPIRES = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_PASSWORD_LINK_EXPIRES);
    protected  DatatypeProperty   USERACCOUNT_PASSWORD_CHANGE_REQUIRED = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_PASSWORD_CHANGE_REQUIRED);
    protected  DatatypeProperty   USERACCOUNT_EXTERNAL_AUTH_ID = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_EXTERNAL_AUTH_ID);
    protected  DatatypeProperty   USERACCOUNT_EXTERNAL_AUTH_ONLY = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_EXTERNAL_AUTH_ONLY);
    protected  ObjectProperty     USERACCOUNT_HAS_PERMISSION_SET = _constModel.createObjectProperty(VitroVocabulary.USERACCOUNT_HAS_PERMISSION_SET);
    protected  ObjectProperty     USERACCOUNT_PROXY_EDITOR_FOR = _constModel.createObjectProperty(VitroVocabulary.USERACCOUNT_PROXY_EDITOR_FOR);

    protected  OntClass           PERMISSIONSET = _constModel.createClass(VitroVocabulary.PERMISSIONSET);
    protected  OntClass           PERMISSION_SET_FOR_NEW_USERS = _constModel.createClass(VitroVocabulary.PERMISSION_SET_FOR_NEW_USERS);
    protected  OntClass           PERMISSION_SET_FOR_PUBLIC = _constModel.createClass(VitroVocabulary.PERMISSION_SET_FOR_PUBLIC);
    protected  ObjectProperty     PERMISSIONSET_HAS_PERMISSION = _constModel.createObjectProperty(VitroVocabulary.PERMISSIONSET_HAS_PERMISSION);

    protected  OntClass           PERMISSION = _constModel.createClass(VitroVocabulary.PERMISSION);
    

    public OntModel getConstModel() {
        return _constModel;
    }

}
