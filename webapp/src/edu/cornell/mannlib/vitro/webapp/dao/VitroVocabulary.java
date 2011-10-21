/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao;


public class VitroVocabulary {

	
    public static final String vitroURI = "http://vitro.mannlib.cornell.edu/ns/vitro/0.7#";
    
    public static final String VITRO_AUTH = "http://vitro.mannlib.cornell.edu/ns/vitro/authorization#";
    public static final String VITRO_PUBLIC = "http://vitro.mannlib.cornell.edu/ns/vitro/public#";
    public static final String VITRO_PUBLIC_ONTOLOGY = "http://vitro.mannlib.cornell.edu/ns/vitro/public";
    
    
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
    
    public static final String AFN = "http://jena.hpl.hp.com/ARQ/function#";

    public static final String label = vitroURI + "label";
    
    // an OWL DL-compatible surrogate for rdf:value for use with boxing idiom
    public static final String value = vitroURI + "value";
    
    public static final String DISPLAY = DisplayVocabulary.DISPLAY_NS;

    // properties found on the beans
    
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
    
    public static final String MOST_SPECIFIC_TYPE = vitroURI + "mostSpecificType";

    // roles
    public static final String PUBLIC = "http://vitro.mannlib.cornell.edu/ns/vitro/role#public";
    public static final String SELF = "http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor";
    public static final String EDITOR = "http://vitro.mannlib.cornell.edu/ns/vitro/role#editor";
    public static final String CURATOR = "http://vitro.mannlib.cornell.edu/ns/vitro/role#curator";
    public static final String DB_ADMIN = "http://vitro.mannlib.cornell.edu/ns/vitro/role#dbAdmin";
    public static final String NOBODY = "http://vitro.mannlib.cornell.edu/ns/vitro/role#nobody";
    
    public static final String SEARCH_BOOST_ANNOT = vitroURI + "searchBoostAnnot";

    public static final String DEPENDENT_RESORUCE = "http://vivoweb.org/ontology/core#DependentResource";
    
    //////////////////////////////////////////

    public static final String MONIKER = vitroURI+"moniker";

    public static final String CLASSGROUP = vitroURI+"ClassGroup";
    public static final String IN_CLASSGROUP = vitroURI+"inClassGroup";

    public static final String MODTIME = vitroURI+"modTime";

    public static final String DISPLAY_RANK = vitroURI+"displayRank";
    public static final String DISPLAY_RANK_ANNOT = vitroURI+"displayRankAnnot";
    public static final String DISPLAY_LIMIT = vitroURI+"displayLimitAnnot";
    
    // ================== property related =================================

    public static final String PROPERTY_ENTITYSORTDIRECTION = vitroURI+"individualSortDirectionAnnot";
    public static final String PROPERTY_ENTITYSORTFIELD = vitroURI+"individualSortFieldAnnot";
    public static final String PROPERTY_OBJECTINDIVIDUALSORTPROPERTY = vitroURI+"objectIndividualSortProperty";
    public static final String PROPERTY_FULLPROPERTYNAMEANNOT = vitroURI+"fullPropertyNameAnnot";
    public static final String PROPERTY_CUSTOMSEARCHVIEWANNOT = vitroURI+"customSearchViewAnnot";
    public static final String PROPERTY_CUSTOMENTRYFORMANNOT = vitroURI+"customEntryFormAnnot";
    public static final String PROPERTY_CUSTOMDISPLAYVIEWANNOT = vitroURI+"customDisplayViewAnnot";
    public static final String PROPERTY_CUSTOMSHORTVIEWANNOT = vitroURI+"customShortViewAnnot";
    public static final String PROPERTY_CUSTOM_LIST_VIEW_ANNOT = vitroURI + "customListViewAnnot";
    public static final String PROPERTY_SELECTFROMEXISTINGANNOT = vitroURI+"selectFromExistingAnnot";
    public static final String PROPERTY_OFFERCREATENEWOPTIONANNOT = vitroURI+"offerCreateNewOptionAnnot";
    public static final String PROPERTY_INPROPERTYGROUPANNOT = vitroURI+"inPropertyGroupAnnot";
    public static final String PROPERTYGROUP = vitroURI + "PropertyGroup";
    public static final String MASKS_PROPERTY = vitroURI + "masksProperty";
    public static final String SKIP_EDIT_FORM = vitroURI + "skipEditForm";
    public static final String PROPERTY_STUBOBJECTPROPERTYANNOT = vitroURI + "stubObjectPropertyAnnot";
	public static final String PROPERTY_COLLATEBYSUBCLASSANNOT = vitroURI + "collateBySubclassAnnot";
    public static final String IS_INTERNAL_CLASSANNOT = vitroURI + "isMenuPageIntersectionClass";
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
    public static final String APPLICATION_KEYWORDHEADING = vitroURI+"keywordHeading";
    public static final String APPLICATION_ROOTLOGOTYPEIMAGE = vitroURI+"rootLogotypeImage";
    
    // ================== Vitro Portal vocabulary ===========================

    public static final String PORTAL = vitroURI+"Portal";
    public static final String PORTAL_THEMEDIR = vitroURI+"themeDir";
    public static final String PORTAL_BANNERIMAGE = vitroURI+"bannerImage";
    public static final String PORTAL_CONTACTMAIL = vitroURI+"contactMail";
    public static final String PORTAL_CORRECTIONMAIL = vitroURI+"correctionMail";
    public static final String PORTAL_SHORTHAND = vitroURI+"shortHand";
    public static final String PORTAL_ABOUTTEXT = vitroURI+"aboutText";
    public static final String PORTAL_ACKNOWLEGETEXT = vitroURI+"acknowledgeText";
    public static final String PORTAL_BANNERWIDTH = vitroURI+"bannerWidth";
    public static final String PORTAL_BANNERHEIGHT = vitroURI+"bannerHeight";
    public static final String PORTAL_COPYRIGHTURL = vitroURI+"copyrightURL";
    public static final String PORTAL_COPYRIGHTANCHOR = vitroURI+"copyrightAnchor";
    public static final String PORTAL_ROOTBREADCRUMBURL = vitroURI+"rootBreadCrumbURL";
    public static final String PORTAL_ROOTBREADCRUMBANCHOR = vitroURI+"rootBreadCrumbAnchor";
    public static final String PORTAL_LOGOTYPEIMAGE = vitroURI+"logotypeImage";
    public static final String PORTAL_LOGOTYPEHEIGHT = vitroURI+"logotypeHeight";
    public static final String PORTAL_LOGOTYPEWIDTH = vitroURI+"logotypeWidth";
    public static final String PORTAL_IMAGETHUMBWIDTH = vitroURI+"imageThumbWidth";
    // reusing displayRank property above
    public static final String PORTAL_URLPREFIX = vitroURI + "urlPrefix";

    // =============== Vitro User vocabulary =================================

    // TODO JB This should go away when the new method of associating UserAccounts with Individuals is in place.
    public static final String MAY_EDIT_AS = vitroURI+"mayEditAs";

    // =============== Vitro UserAccount and PermissionSet vocabulary ===========
    
    public static final String USERACCOUNT = VITRO_AUTH + "UserAccount";
    public static final String USERACCOUNT_ROOT_USER = VITRO_AUTH + "RootUserAccount";
    public static final String USERACCOUNT_EMAIL_ADDRESS = VITRO_AUTH + "emailAddress";
    public static final String USERACCOUNT_FIRST_NAME = VITRO_AUTH + "firstName";
    public static final String USERACCOUNT_LAST_NAME = VITRO_AUTH + "lastName";
    public static final String USERACCOUNT_MD5_PASSWORD = VITRO_AUTH + "md5password";
    public static final String USERACCOUNT_OLD_PASSWORD = VITRO_AUTH + "oldpassword";
    public static final String USERACCOUNT_LOGIN_COUNT = VITRO_AUTH + "loginCount";
    public static final String USERACCOUNT_LAST_LOGIN_TIME = VITRO_AUTH + "lastLoginTime";
    public static final String USERACCOUNT_STATUS = VITRO_AUTH + "status";
    public static final String USERACCOUNT_PASSWORD_LINK_EXPIRES = VITRO_AUTH + "passwordLinkExpires";
    public static final String USERACCOUNT_PASSWORD_CHANGE_REQUIRED = VITRO_AUTH + "passwordChangeRequired";
    public static final String USERACCOUNT_EXTERNAL_AUTH_ID = VITRO_AUTH + "externalAuthId";
    public static final String USERACCOUNT_EXTERNAL_AUTH_ONLY = VITRO_AUTH + "externalAuthOnly";
    public static final String USERACCOUNT_HAS_PERMISSION_SET = VITRO_AUTH + "hasPermissionSet";
    public static final String USERACCOUNT_PROXY_EDITOR_FOR = VITRO_AUTH + "proxyEditorFor";

    public static final String PERMISSIONSET = VITRO_AUTH + "PermissionSet";
    public static final String PERMISSIONSET_HAS_PERMISSION = VITRO_AUTH + "hasPermission";

    public static final String PERMISSION = VITRO_AUTH + "Permission";

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

    public static final String BULK_UPDATE_EVENT = vitroURI+"BulkUpdateEvent";
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
  
    // =============== file storage vocabulary ================================
    
    public static final String FS_FILE_CLASS = VITRO_PUBLIC + "File";
    public static final String FS_BYTESTREAM_CLASS = VITRO_PUBLIC + "FileByteStream";
    
    public static final String FS_FILENAME = VITRO_PUBLIC + "filename";
    public static final String FS_MIME_TYPE = VITRO_PUBLIC + "mimeType";
    public static final String FS_ATTRIBUTION = VITRO_PUBLIC + "attribution";
    public static final String FS_DOWNLOAD_LOCATION = VITRO_PUBLIC + "downloadLocation";
    public static final String FS_THUMBNAIL_IMAGE = VITRO_PUBLIC + "thumbnailImage";
    public static final String FS_ALIAS_URL = VITRO_PUBLIC + "directDownloadUrl";

    public static final String IND_MAIN_IMAGE = VITRO_PUBLIC + "mainImage";
    public static final String IND_IMAGE = VITRO_PUBLIC + "image";

    // =============== Date Time with Precision vocabulary ===============
    private static final String DATETIME_NS = "http://vivoweb.org/ontology/core#";
    
    protected  static final String[] PRECISIONS = {
        DATETIME_NS+"noPrecision", // this individual doesn't actually exist in the ontology
        DATETIME_NS+"yearPrecision",
        DATETIME_NS+"yearMonthPrecision",
        DATETIME_NS+"yearMonthDayPrecision",
        DATETIME_NS+"yearMonthDayHourPrecision",
        DATETIME_NS+"yearMonthDayHourMinutePrecision",
        DATETIME_NS+"yearMonthDayTimePrecision"};

    //The Precision.ordinal method is used so do 
    //not change the order of these enums.
    public enum Precision {
        NONE(PRECISIONS[0]),
        YEAR(PRECISIONS[1]),
        MONTH(PRECISIONS[2]),
        DAY(PRECISIONS[3]),
        HOUR(PRECISIONS[4]),
        MINUTE(PRECISIONS[5]),
        SECOND(PRECISIONS[6]);        
        
        private final String URI;
        Precision(String uri){
            URI=uri;
        }
        public String uri(){return URI;}
    }
}
