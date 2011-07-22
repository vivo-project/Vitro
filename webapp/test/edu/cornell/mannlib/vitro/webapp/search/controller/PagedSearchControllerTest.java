/* $This file is distributed under the terms of the license in /doc/license.txt$ */
/**
 * 
 */
package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder.ParamMap;
import edu.cornell.mannlib.vitro.webapp.search.controller.PagedSearchController.PagingLink;

public class PagedSearchControllerTest {

    @Test
    public void testGetPagingLinks() {
        ParamMap pm = new ParamMap();         
        int hitsPerPage = 25;
        int totalHits = 500;
        int currentStartIndex = 0;
        List<PagingLink> pageLinks = PagedSearchController.getPagingLinks(currentStartIndex, hitsPerPage, totalHits, "baseURL", pm);
        Assert.assertNotNull(pageLinks);
        Assert.assertEquals(500 / 25, pageLinks.size());
        
        //test for no page links on a very short result
        hitsPerPage = 25;
        totalHits = 10;
        currentStartIndex = 0;
        pageLinks = PagedSearchController.getPagingLinks(currentStartIndex, hitsPerPage, totalHits, "baseURL", pm);
        Assert.assertNotNull(pageLinks);
        Assert.assertEquals(0, pageLinks.size());
    }
    
    @Test
    public void testGetPagingLinksForLargeResults() {
        ParamMap pm = new ParamMap();         
        int hitsPerPage = 25;
        int totalHits = 349909;
        int currentStartIndex = 0;
        List<PagingLink> pageLinks =  PagedSearchController.getPagingLinks(currentStartIndex, hitsPerPage, totalHits, "baseURL", pm);
        Assert.assertNotNull(pageLinks);
        Assert.assertEquals( PagedSearchController.DEFAULT_MAX_HIT_COUNT / hitsPerPage, pageLinks.size());
        
        //test for large sets of results with high start index
        hitsPerPage = 25;
        totalHits = PagedSearchController.DEFAULT_MAX_HIT_COUNT + 20329;
        currentStartIndex = PagedSearchController.DEFAULT_MAX_HIT_COUNT + 5432;
        pageLinks = PagedSearchController.getPagingLinks(currentStartIndex, hitsPerPage, totalHits, "baseURL", pm);
        Assert.assertNotNull(pageLinks);
        Assert.assertEquals( 
                (currentStartIndex / hitsPerPage) + //all the pages that are before the current page 
                (PagedSearchController.DEFAULT_MAX_HIT_COUNT / hitsPerPage) + //some pages after the current apge
                1, //for the more... page
                pageLinks.size());
    }

}
