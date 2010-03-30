/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;

import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;
import com.hp.hpl.jena.ontology.AnnotationProperty;

public class VitroVocabulary {

	
    public static final String vitroURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#";
    
    
    /** BJL23 2008-02-25:
     * This is a hack.  The classic Vitro code is heavily reliant on simple identifiers, and it will take some doing to completely
     * eliminate this.  Prior to version 0.7, identifiers were all integers; now they're URIs.
     * There are a lot of places we'd like to be able to use a bnode ID instead of a URI.  The following special string
     * indicates that the local name of a 'URI' should actually be treated as a bnode ID.
     */
    public static final String PSEUDO_BNODE_NS = "http://vitro.mannlib.cornell.edu/ns/bnode#"; 
    
    public static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String RDF_TYPE = RDF+"type";
    public static final String LABEL = RDFS + "label";
    
    public static final String SUBCLASS_OF = RDFS+"subClassOf";

    public static final String OWL = "http://www.w3.org/2002/07/owl#";
    public static final String OWL_ONTOLOGY = OWL+"Ontology";
    public static final String OWL_THING = OWL+"Thing";

    public static final String label = vitroURI + "label";
    
    // an OWL DL-compatible surrogate for rdf:value for use with boxing idiom
    public static final String value = vitroURI + "value";

    // properties found on the beans

    public static final String DESCRIPTION = vitroURI+"description";
    public static final String DESCRIPTION_ANNOT = vitroURI + "descriptionAnnot";
    public static final String PUBLIC_DESCRIPTION_ANNOT = vitroURI + "publicDescriptionAnnot";
    public static final String SHORTDEF = vitroURI+"shortDef";
    public static final String EXAMPLE_ANNOT = vitroURI+"exampleAnnot";

    public static final String EXTERNALID = vitroURI+"externalId";
    public static final String DATAPROPERTY_ISEXTERNALID = vitroURI+"isExternalId";
        
    public static final String HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT = vitroURI+"hiddenFromDisplayBelowRoleLevelAnnot";
    
    //public static final String PROHIBITED_FROM_CREATE_BELOW_ROLE_LEVEL_ANNOT = vitroURI+"prohibitedFromCreateBelowRoleLevelAnnot";
    public static final String PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT = vitroURI+"prohibitedFromUpdateBelowRoleLevelAnnot";
    //public static final String PROHIBITED_FROM_DELETE_BELOW_ROLE_LEVEL_ANNOT = vitroURI+"prohibitedFromDeleteBelowRoleLevelAnnot";

    // roles
    public static final String PUBLIC = "http://vitro.mannlib.cornell.edu/ns/vitro/role#public";
    public static final String SELF = "http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor";
    public static final String EDITOR = "http://vitro.mannlib.cornell.edu/ns/vitro/role#editor";
    public static final String CURATOR = "http://vitro.mannlib.cornell.edu/ns/vitro/role#curator";
    public static final String DB_ADMIN = "http://vitro.mannlib.cornell.edu/ns/vitro/role#dbAdmin";
    public static final String NOBODY = "http://vitro.mannlib.cornell.edu/ns/vitro/role#nobody";
    
    public static final String SEARCH_BOOST_ANNOT = vitroURI + "searchBoostAnnot";
    
    public static final String SUNRISE = vitroURI+"sunrise";
    public static final String SUNSET = vitroURI+"sunset";


    //////////////////////////////////////////


    public static final String CURATOR_NOTE = vitroURI+"curatorNote";

    public static final String MONIKER = vitroURI+"moniker";

    public static final String BLURB = vitroURI+"blurb";

    public static final String CLASSGROUP = vitroURI+"ClassGroup";
    public static final String IN_CLASSGROUP = vitroURI+"inClassGroup";

    public static final String MODTIME = vitroURI+"modTime";
    public static final String TIMEKEY = vitroURI+"timekey";

    public static final String DISPLAY_RANK = vitroURI+"displayRank";
    public static final String DISPLAY_RANK_ANNOT = vitroURI+"displayRankAnnot";
    public static final String DISPLAY_LIMIT = vitroURI+"displayLimitAnnot";

    public static final String CITATION = vitroURI+"citation";
    public static final String IMAGEFILE = vitroURI+"imageFile";
    public static final String IMAGETHUMB = vitroURI+"imageThumb";
    
    // ================== property related =================================

    public static final String PROPERTY_ENTITYSORTDIRECTION = vitroURI+"individualSortDirectionAnnot";
    public static final String PROPERTY_ENTITYSORTFIELD = vitroURI+"individualSortFieldAnnot";
    public static final String PROPERTY_OBJECTINDIVIDUALSORTPROPERTY = vitroURI+"objectIndividualSortProperty";
    public static final String PROPERTY_FULLPROPERTYNAMEANNOT = vitroURI+"fullPropertyNameAnnot";
    public static final String PROPERTY_CUSTOMSEARCHVIEWANNOT = vitroURI+"customSearchViewAnnot";
    //public static final String PROPERTY_SELFEDITPROHIBITEDANNOT = vitroURI+"selfEditProhibitedAnnot";
    //public static final String PROPERTY_CURATOREDITPROHIBITEDANNOT = vitroURI+"curatorEditProhibitedAnnot";
    public static final String PROPERTY_CUSTOMENTRYFORMANNOT = vitroURI+"customEntryFormAnnot";
    public static final String PROPERTY_CUSTOMDISPLAYVIEWANNOT = vitroURI+"customDisplayViewAnnot";
    public static final String PROPERTY_CUSTOMSHORTVIEWANNOT = vitroURI+"customShortViewAnnot";
    public static final String PROPERTY_SELECTFROMEXISTINGANNOT = vitroURI+"selectFromExistingAnnot";
    public static final String PROPERTY_OFFERCREATENEWOPTIONANNOT = vitroURI+"offerCreateNewOptionAnnot";
    public static final String PROPERTY_INPROPERTYGROUPANNOT = vitroURI+"inPropertyGroupAnnot";
    public static final String PROPERTYGROUP = vitroURI + "PropertyGroup";
    public static final String MASKS_PROPERTY = vitroURI + "masksProperty";
    public static final String SKIP_EDIT_FORM = vitroURI + "skipEditForm";
    public static final String PROPERTY_STUBOBJECTPROPERTYANNOT = vitroURI + "stubObjectPropertyAnnot";
	public static final String PROPERTY_COLLATEBYSUBCLASSANNOT = vitroURI + "collateBySubclassAnnot";
	
    // ================== keyword related ==================================

    public static final String KEYWORD = vitroURI+"Keyword";
    public static final String KEYWORD_STEM = vitroURI+"keywordStem";
    public static final String KEYWORD_TYPE = vitroURI+"keywordType";
    public static final String KEYWORD_SOURCE = vitroURI+"keywordSource";
    public static final String KEYWORD_ORIGIN = vitroURI+"keywordOrigin";
    public static final String KEYWORD_COMMENTS = vitroURI+"keywordComment";
    public static final String KEYWORD_INDIVIDUALRELATION = vitroURI+"KeywordRelation";
    public static final String KEYWORD_INDIVIDUALRELATION_INVOLVESKEYWORD = vitroURI+"involvesKeyword";
    public static final String KEYWORD_INDIVIDUALRELATION_INVOLVESINDIVIDUAL = vitroURI+"involvesIndividual";
    public static final String KEYWORD_INDIVIDUALRELATION_MODE = vitroURI+"keywordMode";

    // ================== link related =====================================

    public static final String LINK = vitroURI+"Link";
    public static final String PRIMARY_LINK = vitroURI+"primaryLink";
    public static final String ADDITIONAL_LINK = vitroURI+"additionalLink";
    public static final String LINK_ANCHOR = vitroURI+"linkAnchor";
    public static final String LINK_URL = vitroURI+"linkURL";
    public static final String LINK_TYPE = vitroURI+"linkType";
    public static final String LINK_DISPLAYRANK_URL = vitroURI+"linkDisplayRank";

    // ================== Vitro Application vocabulary =====================

    public static final String APPLICATION = vitroURI + "Application";
    public static final String APPLICATION_FLAG1NAME = vitroURI+"flag1Name";
    public static final String APPLICATION_FLAG2NAME = vitroURI+"flag2Name";
    public static final String APPLICATION_FLAG3NAME = vitroURI+"flag3Name";
    public static final String APPLICATION_MINSHAREDPORTALID = vitroURI+"minSharedPortalId";
    public static final String APPLICATION_MAXSHAREDPORTALID = vitroURI+"maxSharedPortalId";
    public static final String APPLICATION_KEYWORDHEADING = vitroURI+"keywordHeading";
    public static final String APPLICATION_ROOTLOGOTYPEIMAGE = vitroURI+"rootLogotypeImage";
    public static final String APPLICATION_ONLYCURRENT = vitroURI+"onlyCurrent";
    public static final String APPLICATION_MAXPORTALID = vitroURI+"maxPortalId";

    // ================== Vitro Portal vocabulary ===========================

    public static final String PORTAL = vitroURI+"Portal";
    public static final String PORTAL_ROOTTAB = vitroURI+"rootTab";
    public static final String PORTAL_THEMEDIR = vitroURI+"themeDir";
    public static final String PORTAL_BANNERIMAGE = vitroURI+"bannerImage";
    public static final String PORTAL_FLAG2VALUES = vitroURI+"flag2Values";
    public static final String PORTAL_FLAG1VALUES = vitroURI+"flag1Values";
    public static final String PORTAL_CONTACTMAIL = vitroURI+"contactMail";
    public static final String PORTAL_CORRECTIONMAIL = vitroURI+"correctionMail";
    public static final String PORTAL_SHORTHAND = vitroURI+"shortHand";
    public static final String PORTAL_ABOUTTEXT = vitroURI+"aboutText";
    public static final String PORTAL_ACKNOWLEGETEXT = vitroURI+"acknowledgeText";
    public static final String PORTAL_BANNERWIDTH = vitroURI+"bannerWidth";
    public static final String PORTAL_BANNERHEIGHT = vitroURI+"bannerHeight";
    public static final String PORTAL_FLAG3VALUES = vitroURI+"flag3Values";
    public static final String PORTAL_FLAG2NUMERIC = vitroURI+"flag2Numeric";
    public static final String PORTAL_FLAG3NUMERIC = vitroURI+"flag3Numeric";
    public static final String PORTAL_COPYRIGHTURL = vitroURI+"copyrightURL";
    public static final String PORTAL_COPYRIGHTANCHOR = vitroURI+"copyrightAnchor";
    public static final String PORTAL_ROOTBREADCRUMBURL = vitroURI+"rootBreadCrumbURL";
    public static final String PORTAL_ROOTBREADCRUMBANCHOR = vitroURI+"rootBreadCrumbAnchor";
    public static final String PORTAL_LOGOTYPEIMAGE = vitroURI+"logotypeImage";
    public static final String PORTAL_LOGOTYPEHEIGHT = vitroURI+"logotypeHeight";
    public static final String PORTAL_LOGOTYPEWIDTH = vitroURI+"logotypeWidth";
    public static final String PORTAL_IMAGETHUMBWIDTH = vitroURI+"imageThumbWidth";
    // reusing displayRank property above
    public static final String PORTAL_FLAG1SEARCHFILTERING = vitroURI+"flag1SearchFiltering";
    public static final String PORTAL_FLAG2SEARCHFILTERING = vitroURI+"flag2SearchFiltering";
    public static final String PORTAL_FLAG3SEARCHFILTERING = vitroURI+"flag3SearchFiltering";
    public static final String PORTAL_URLPREFIX = vitroURI + "urlPrefix";
    public static final String PORTAL_FLAG1FILTERING = vitroURI+"flag1Filtering";

    // ================ Vitro Tab vocabulary ================================

    public static final String TAB = vitroURI+"Tab";
    public static final String TAB_AUTOLINKABLETAB = vitroURI+"AutoLinkableTab";
    public static final String TAB_MANUALLYLINKABLETAB = vitroURI+"ManuallyLinkableTab";
    public static final String TAB_MIXEDTAB = vitroURI+"MixedTab";
    public static final String TAB_PRIMARYTAB = vitroURI+"PrimaryTab";
    public static final String TAB_SUBCOLLECTIONCATEGORY = vitroURI+"SubcollectionCategory";
    public static final String TAB_SECONDARYTAB = vitroURI+"SecondaryTab";
    public static final String TAB_PRIMARYTABCONTENT = vitroURI+"PrimaryTabContent";
    public static final String TAB_SUBCOLLECTION = vitroURI+"Subcollection";
    public static final String TAB_SUBTABOF = vitroURI+"subTabOf";
    public static final String TAB_COLLECTION = vitroURI+"Collection";

    public static final String TAB_INDIVIDUALRELATION= vitroURI+"TabIndividualRelation";
    public static final String TAB_INDIVIDUALRELATION_INVOLVESINDIVIDUAL= vitroURI+"involvesIndividual";
    public static final String TAB_INDIVIDUALRELATION_INVOLVESTAB = vitroURI+"involvesTab";


    public static final String TAB_AUTOLINKEDTOTAB = vitroURI + "autoLinkedToTab"; // annotation on class
    public static final String TAB_TABTYPE = vitroURI+"tabType";
    public static final String TAB_STATUSID = vitroURI+"statusId";
    public static final String TAB_DAYLIMIT = vitroURI+"dayLimit";
    public static final String TAB_BODY = vitroURI+"tabBody";
    public static final String TAB_GALLERYROWS = vitroURI+"galleryRows";
    public static final String TAB_GALLERYCOLS = vitroURI+"galleryCols";
    public static final String TAB_MORETAG = vitroURI+"moreTag";
    public static final String TAB_IMAGEWIDTH = vitroURI+"imageWidth";
    public static final String TAB_PORTAL = vitroURI+"inPortal";
    public static final String TAB_ENTITYSORTFIELD = vitroURI+"individualSortField";
    public static final String TAB_ENTITYSORTDIRECTION = vitroURI+"individualSortDirection";
    public static final String TAB_ENTITYLINKMETHOD = vitroURI+"individualLinkMethod";
    public static final String TAB_RSSURL = vitroURI+"rssUrl";
    public static final String TAB_FLAG2SET = vitroURI+"flag2Set";
    public static final String TAB_FLAG3SET = vitroURI+"flag3Set";
    public static final String TAB_FLAG2MODE = vitroURI+"flag2Mode";
    public static final String TAB_FLAG3MODE = vitroURI+"flag3Mode";

    // =============== Vitro User vocabulary =================================

    public static final String USER = vitroURI+"User";
    public static final String USER_USERNAME = vitroURI+"username";
    public static final String USER_MD5PASSWORD = vitroURI+"md5password";
    public static final String USER_OLDPASSWORD = vitroURI+"oldpassword";
    public static final String USER_FIRSTTIME = vitroURI+"firstTime";
    public static final String USER_LOGINCOUNT = vitroURI+"loginCount";
    public static final String USER_ROLE = vitroURI+"roleURI";
    public static final String USER_LASTNAME = vitroURI+"lastName";
    public static final String USER_FIRSTNAME = vitroURI+"firstName";
    public static final String MAY_EDIT_AS = vitroURI+"mayEditAs";

    // =============== model auditing vocabulary =============================

    public static final String STATEMENT_EVENT = vitroURI+"StatementEvent";
    public static final String STATEMENT_ADDITION_EVENT = vitroURI+"StatementAdditionEvent";
    public static final String STATEMENT_REMOVAL_EVENT = vitroURI+"StatementRemovalEvent";
    public static final String STATEMENT_EVENT_STATEMENT = vitroURI+"involvesStatement";
    public static final String STATEMENT_EVENT_DATETIME = vitroURI+"statementEventDateTime";

    public static final String PART_OF_EDIT_EVENT = vitroURI+"partOfEditEvent";

    public static final String EDIT_EVENT = vitroURI+"EditEvent";
    public static final String EDIT_EVENT_AGENT = vitroURI+"editEventAgent";
    public static final String EDIT_EVENT_DATETIME = vitroURI+"editEventDateTime";

    public static final String INDIVIDUAL_EDIT_EVENT = vitroURI+"IndividualEditEvent";
    public static final String INDIVIDUAL_CREATION_EVENT = vitroURI+"IndividualCreationEvent";
    public static final String INDIVIDUAL_UPDATE_EVENT = vitroURI+"IndividualUpdateEvent";
    public static final String INDIVIDUAL_DELETION_EVENT = vitroURI+"IndividualDeletionEvent";
    public static final String EDITED_INDIVIDUAL = vitroURI+"editedIndividual";

    public static final String LOGIN_EVENT = vitroURI + "LoginEvent";
    public static final String LOGIN_DATETIME = vitroURI + "loggedInAt";
    public static final String LOGIN_AGENT = vitroURI + "loggedInAgent";
    
    // =============== file vocabulary ========================================
    
    public static final String VITRO_FEDORA = "http://vitro.mannlib.cornell.edu/ns/fedora/0.1#";
    public static final String FILE_CLASS = VITRO_FEDORA + "File";
    public static final String FILE_NAME = VITRO_FEDORA + "fileName";
    public static final String FEDORA_PID = VITRO_FEDORA + "fedoraPid";
    public static final String CONTENT_TYPE = VITRO_FEDORA + "contentType";
    public static final String FILE_SIZE = VITRO_FEDORA + "fileSize";    
    public static final String HAS_FILE = VITRO_FEDORA + "hasFile";
    public static final String MD5_CHECKSUM = VITRO_FEDORA + "md5checksum";
    
    public static final String FILE_LOCATION = vitroURI  + "fileLocation";
    public static final String FILE_SAVED_NAME = vitroURI + "FileSavedName";
    
    // =============== namespace vocabulary ===================================
    
    public static final String NAMESPACE = vitroURI + "Namespace";
    public static final String NAMESPACE_PREFIX_MAPPING = vitroURI + "NamespacePrefixMapping";
    public static final String NAMESPACE_HASPREFIXMAPPING = vitroURI + "hasPrefixMapping";
    public static final String NAMESPACE_NAMESPACEURI = vitroURI + "namespaceURI";
    public static final String NAMESPACE_PREFIX = vitroURI + "namespacePrefix";
    public static final String NAMESPACE_ISCURRENTPREFIXMAPPING = vitroURI + "isCurrentPrefixMapping";
    
    public static final String ONTOLOGY_PREFIX_ANNOT = vitroURI + "ontologyPrefixAnnot";
  

}
