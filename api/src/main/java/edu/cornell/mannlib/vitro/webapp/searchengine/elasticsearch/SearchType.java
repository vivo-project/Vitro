package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

public enum SearchType {
    MATCH,
    FUZZY,
    PHRASE,
    RANGE,
    PREFIX,
    WILDCARD,
    EXISTS,
    MATCH_ALL,
    REGEXP
}
