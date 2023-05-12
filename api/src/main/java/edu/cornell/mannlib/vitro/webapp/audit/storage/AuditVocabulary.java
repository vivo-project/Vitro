/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.audit.storage;

/**
 * URIs used by the Jena storage engine
 */
public final class AuditVocabulary {
    public final static String TYPE_CHANGESET      = "http://vivoweb.org/audit/types/ChangeSet";
    public final static String TYPE_CHANGESETGRAPH = "http://vivoweb.org/audit/types/ChangeSetForGraph";

    public final static String PROP_UUID = "http://vivoweb.org/audit/properties#uuid";
    public final static String PROP_USER = "http://vivoweb.org/audit/properties#user";
    public final static String PROP_DATE = "http://vivoweb.org/audit/properties#date";

    public final static String PROP_HASGRAPH = "http://vivoweb.org/audit/properties#hasGraph";

    public final static String PROP_GRAPH = "http://vivoweb.org/audit/properties#graph";
    public final static String PROP_GRAPH_ADDED = "http://vivoweb.org/audit/properties#added";
    public final static String PROP_GRAPH_REMOVED = "http://vivoweb.org/audit/properties#removed";

    public final static String RESOURCE_UNKNOWN= "http://vivoweb.org/audit/resource/unknown";
}
