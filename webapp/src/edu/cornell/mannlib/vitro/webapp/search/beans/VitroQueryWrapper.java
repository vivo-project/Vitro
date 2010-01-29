package edu.cornell.mannlib.vitro.webapp.search.beans;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

/**
 * VitroQueryWrapper holds the information about the last query that the
 * user made.
 *
 * @author bdc34
 *
 */
public class VitroQueryWrapper {
    private VitroQuery query = null;
    private int requestCount = 0;
    private long searchTime = 0;
    private VitroHighlighter highlighter;


    public VitroQueryWrapper(VitroQuery q, VitroHighlighter hi, int reqCount, long d){
        this.setSearchTime(d);
        this.setQuery(q);
        this.setRequestCount(reqCount);
        this.highlighter = hi;
    }

    public long getSearchTime() {
        return searchTime;
    }
    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }
    public VitroQuery getQuery() {
        return query;
    }
    public void setQuery(VitroQuery query) {
        this.query = query;
    }
    public int getRequestCount() {
        return requestCount;
    }
    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public VitroHighlighter getHighlighter(){ return highlighter; }

}
