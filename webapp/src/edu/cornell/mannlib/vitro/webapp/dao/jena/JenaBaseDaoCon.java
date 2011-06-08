/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.ontology.AnnotationProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
    }

    
    private OntModel _constModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    
    /* ***************** Vitro ontology constants ***************** */

    public    DatatypeProperty   SUNRISE = _constModel.createDatatypeProperty(VitroVocabulary.SUNRISE);
    public    DatatypeProperty   SUNSET = _constModel.createDatatypeProperty(VitroVocabulary.SUNSET);
    protected AnnotationProperty DATAPROPERTY_ISEXTERNALID = _constModel.createAnnotationProperty(VitroVocabulary.DATAPROPERTY_ISEXTERNALID);
        
    protected AnnotationProperty HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);    
    //protected AnnotationProperty PROHIBITED_FROM_CREATE_BELOW_ROLE_LEVEL_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROHIBITED_FROM_CREATE_BELOW_ROLE_LEVEL_ANNOT);
    protected AnnotationProperty PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);    
    //protected AnnotationProperty PROHIBITED_FROM_DELETE_BELOW_ROLE_LEVEL_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROHIBITED_FROM_DELETE_BELOW_ROLE_LEVEL_ANNOT);
    
    protected AnnotationProperty   SEARCH_BOOST_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.SEARCH_BOOST_ANNOT);
    
    protected AnnotationProperty EXAMPLE = _constModel.createAnnotationProperty(VitroVocabulary.EXAMPLE_ANNOT);
    protected AnnotationProperty DESCRIPTION_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.DESCRIPTION_ANNOT);
    protected AnnotationProperty PUBLIC_DESCRIPTION_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PUBLIC_DESCRIPTION_ANNOT);
    protected AnnotationProperty SHORTDEF = _constModel.createAnnotationProperty(VitroVocabulary.SHORTDEF);

    protected DatatypeProperty   CURATOR_NOTE = _constModel.createDatatypeProperty(VitroVocabulary.CURATOR_NOTE);
    protected DatatypeProperty   MONIKER = _constModel.createDatatypeProperty(VitroVocabulary.MONIKER);
    protected DatatypeProperty   DESCRIPTION = _constModel.createDatatypeProperty(VitroVocabulary.DESCRIPTION);
    protected DatatypeProperty   BLURB = _constModel.createDatatypeProperty(VitroVocabulary.BLURB);
    protected OntClass           CLASSGROUP = _constModel.createClass(VitroVocabulary.CLASSGROUP);
    protected AnnotationProperty IN_CLASSGROUP = _constModel.createAnnotationProperty(VitroVocabulary.IN_CLASSGROUP);
    protected DatatypeProperty   MODTIME = _constModel.createDatatypeProperty(VitroVocabulary.MODTIME);
    protected DatatypeProperty   TIMEKEY = _constModel.createDatatypeProperty(VitroVocabulary.TIMEKEY);
    protected DatatypeProperty   CITATION = _constModel.createDatatypeProperty(VitroVocabulary.CITATION);

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
    //protected AnnotationProperty PROPERTY_FORCESTUBDELETIONANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_FORCESTUBDELETIONANNOT);
    protected AnnotationProperty PROPERTY_SELECTFROMEXISTINGANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_SELECTFROMEXISTINGANNOT);
    protected AnnotationProperty PROPERTY_OFFERCREATENEWOPTIONANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_OFFERCREATENEWOPTIONANNOT);
    protected AnnotationProperty PROPERTY_INPROPERTYGROUPANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_INPROPERTYGROUPANNOT);
    protected AnnotationProperty PROPERTY_COLLATEBYSUBCLASSANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_COLLATEBYSUBCLASSANNOT);
    protected AnnotationProperty PROPERTY_STUBOBJECTPROPERTYANNOT = _constModel.createAnnotationProperty(VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT);
    
    protected OntClass 		     PROPERTYGROUP = _constModel.createClass(VitroVocabulary.PROPERTYGROUP);

    protected OntClass           KEYWORD = _constModel.createClass(VitroVocabulary.KEYWORD);
    protected DatatypeProperty   KEYWORD_STEM = _constModel.createDatatypeProperty(VitroVocabulary.KEYWORD_STEM);
    protected DatatypeProperty   KEYWORD_TYPE = _constModel.createDatatypeProperty(VitroVocabulary.KEYWORD_TYPE);
    protected DatatypeProperty   KEYWORD_SOURCE = _constModel.createDatatypeProperty(VitroVocabulary.KEYWORD_SOURCE);
    protected DatatypeProperty   KEYWORD_ORIGIN = _constModel.createDatatypeProperty(VitroVocabulary.KEYWORD_ORIGIN);
    protected DatatypeProperty   KEYWORD_COMMENTS = _constModel.createDatatypeProperty(VitroVocabulary.KEYWORD_COMMENTS);
    protected OntClass           KEYWORD_INDIVIDUALRELATION = _constModel.createClass(VitroVocabulary.KEYWORD_INDIVIDUALRELATION);
    protected ObjectProperty     KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD = _constModel.createObjectProperty(VitroVocabulary.KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD);
    protected ObjectProperty     KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL = _constModel.createObjectProperty(VitroVocabulary.KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL);
    protected DatatypeProperty   KEYWORD_INDIVIDUALRELATION_MODE = _constModel.createDatatypeProperty(VitroVocabulary.KEYWORD_INDIVIDUALRELATION_MODE);

    protected OntClass           LINK = _constModel.createClass(VitroVocabulary.LINK);
    protected ObjectProperty     PRIMARY_LINK = _constModel.createObjectProperty(VitroVocabulary.PRIMARY_LINK);
    protected ObjectProperty     ADDITIONAL_LINK = _constModel.createObjectProperty(VitroVocabulary.ADDITIONAL_LINK);
    protected DatatypeProperty   LINK_ANCHOR = _constModel.createDatatypeProperty(VitroVocabulary.LINK_ANCHOR);
    protected DatatypeProperty   LINK_URL = _constModel.createDatatypeProperty(VitroVocabulary.LINK_URL);
    protected DatatypeProperty   LINK_TYPE = _constModel.createDatatypeProperty(VitroVocabulary.LINK_TYPE);
    protected DatatypeProperty   LINK_DISPLAYRANK = _constModel.createDatatypeProperty(VitroVocabulary.LINK_DISPLAYRANK_URL);

//    protected  OntClass           APPLICATION = null;
//    protected  DatatypeProperty   APPLICATION_FLAG1NAME = null;
//    protected  DatatypeProperty   APPLICATION_FLAG2NAME = null;
//    protected  DatatypeProperty   APPLICATION_FLAG3NAME = null;
//    protected  DatatypeProperty   APPLICATION_MINSHAREDPORTALID = null;
//    protected  DatatypeProperty   APPLICATION_MAXSHAREDPORTALID = null;
//    protected  DatatypeProperty   APPLICATION_KEYWORDHEADING = null;
//    protected  DatatypeProperty   APPLICATION_ROOTLOGOTYPEIMAGE = null;
//    protected  DatatypeProperty   APPLICATION_ONLYCURRENT = null;
//    protected  DatatypeProperty   APPLICATION_MAXPORTALID = null;

    protected  OntClass           PORTAL = _constModel.createClass(VitroVocabulary.PORTAL);
    protected  DatatypeProperty   APPLICATION_THEMEDIR = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_THEMEDIR);
    protected  DatatypeProperty   PORTAL_BANNERIMAGE = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_BANNERIMAGE);
    //protected  DatatypeProperty   PORTAL_FLAG2VALUES = null;
    //protected  DatatypeProperty   PORTAL_FLAG1VALUES = null;
    protected  DatatypeProperty   APPLICATION_CONTACTMAIL = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_CONTACTMAIL);
    protected  DatatypeProperty   APPLICATION_CORRECTIONMAIL = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_CORRECTIONMAIL);
    protected  DatatypeProperty   PORTAL_SHORTHAND = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_SHORTHAND);
    protected  DatatypeProperty   APPLICATION_ABOUTTEXT = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_ABOUTTEXT);
    protected  DatatypeProperty   APPLICATION_ACKNOWLEGETEXT = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_ACKNOWLEGETEXT);
    protected  DatatypeProperty   PORTAL_BANNERWIDTH = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_BANNERWIDTH);
    protected  DatatypeProperty   PORTAL_BANNERHEIGHT = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_BANNERHEIGHT);
    //protected  DatatypeProperty   PORTAL_FLAG3VALUES = null;
    protected  DatatypeProperty   APPLICATION_COPYRIGHTURL = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_COPYRIGHTURL);
    protected  DatatypeProperty   APPLICATION_COPYRIGHTANCHOR = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_COPYRIGHTANCHOR);
    protected  DatatypeProperty   PORTAL_ROOTBREADCRUMBURL = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_ROOTBREADCRUMBURL);
    protected  DatatypeProperty   PORTAL_ROOTBREADCRUMBANCHOR = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_ROOTBREADCRUMBANCHOR);
    protected  DatatypeProperty   PORTAL_LOGOTYPEIMAGE = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_LOGOTYPEIMAGE);
    protected  DatatypeProperty   PORTAL_LOGOTYPEHEIGHT = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_LOGOTYPEHEIGHT);
    protected  DatatypeProperty   PORTAL_LOGOTYPEWIDTH = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_LOGOTYPEWIDTH);
    protected  DatatypeProperty   PORTAL_IMAGETHUMBWIDTH = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_IMAGETHUMBWIDTH);
    protected  DatatypeProperty   PORTAL_URLPREFIX = _constModel.createDatatypeProperty(VitroVocabulary.PORTAL_URLPREFIX);

    protected AnnotationProperty  ONTOLOGY_PREFIX_ANNOT = _constModel.createAnnotationProperty(VitroVocabulary.ONTOLOGY_PREFIX_ANNOT);
    
    protected  OntClass           FS_FILE = _constModel.createClass(VitroVocabulary.FS_FILE_CLASS);
    protected  OntClass           FS_BYTESTREAM = _constModel.createClass(VitroVocabulary.FS_BYTESTREAM_CLASS);
    protected  ObjectProperty     FS_DOWNLOAD_LOCATION = _constModel.createObjectProperty(VitroVocabulary.FS_DOWNLOAD_LOCATION);
    protected  ObjectProperty     FS_THUMBNAIL_IMAGE = _constModel.createObjectProperty(VitroVocabulary.FS_THUMBNAIL_IMAGE);
    protected  DatatypeProperty   FS_FILENAME = _constModel.createDatatypeProperty(VitroVocabulary.FS_FILENAME);
    protected  DatatypeProperty   FS_MIME_TYPE = _constModel.createDatatypeProperty(VitroVocabulary.FS_MIME_TYPE);
    protected  DatatypeProperty   FS_ATTRIBUTION = _constModel.createDatatypeProperty(VitroVocabulary.FS_ATTRIBUTION);
    protected  DatatypeProperty   FS_ALIAS_URL = _constModel.createDatatypeProperty(VitroVocabulary.FS_ALIAS_URL);

    protected  ObjectProperty     IND_MAIN_IMAGE = _constModel.createObjectProperty(VitroVocabulary.IND_MAIN_IMAGE);
    protected  ObjectProperty     IND_IMAGE = _constModel.createObjectProperty(VitroVocabulary.IND_IMAGE);

    /* ***************** User Account Model constants ***************** */

    protected  OntClass           USERACCOUNT = _constModel.createClass(VitroVocabulary.USERACCOUNT);
    protected  OntClass           USERACCOUNT_ROOT_USER = _constModel.createClass(VitroVocabulary.USERACCOUNT_ROOT_USER);
    protected  DatatypeProperty   USERACCOUNT_EMAIL_ADDRESS = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_EMAIL_ADDRESS);
    protected  DatatypeProperty   USERACCOUNT_FIRST_NAME = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_FIRST_NAME);
    protected  DatatypeProperty   USERACCOUNT_LAST_NAME = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_LAST_NAME);
    protected  DatatypeProperty   USERACCOUNT_MD5_PASSWORD = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_MD5_PASSWORD);
    protected  DatatypeProperty   USERACCOUNT_OLD_PASSWORD = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_OLD_PASSWORD);
    protected  DatatypeProperty   USERACCOUNT_LOGIN_COUNT = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_LOGIN_COUNT);
    protected  DatatypeProperty   USERACCOUNT_STATUS = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_STATUS);
    protected  DatatypeProperty   USERACCOUNT_PASSWORD_LINK_EXPIRES = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_PASSWORD_LINK_EXPIRES);
    protected  DatatypeProperty   USERACCOUNT_PASSWORD_CHANGE_REQUIRED = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_PASSWORD_CHANGE_REQUIRED);
    protected  DatatypeProperty   USERACCOUNT_EXTERNAL_AUTH_ID = _constModel.createDatatypeProperty(VitroVocabulary.USERACCOUNT_EXTERNAL_AUTH_ID);
    protected  ObjectProperty     USERACCOUNT_HAS_PERMISSION_SET = _constModel.createObjectProperty(VitroVocabulary.USERACCOUNT_HAS_PERMISSION_SET);

    protected  OntClass           PERMISSIONSET = _constModel.createClass(VitroVocabulary.PERMISSIONSET);
    protected  ObjectProperty     PERMISSIONSET_HAS_PERMISSION = _constModel.createObjectProperty(VitroVocabulary.PERMISSIONSET_HAS_PERMISSION);

    protected  OntClass           PERMISSION = _constModel.createClass(VitroVocabulary.PERMISSION);
    

    public OntModel getConstModel() {
        return _constModel;
    }

}
