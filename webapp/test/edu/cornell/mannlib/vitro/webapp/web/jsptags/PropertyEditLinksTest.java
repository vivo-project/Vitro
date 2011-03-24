package edu.cornell.mannlib.vitro.webapp.web.jsptags;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.cornell.mannlib.vitro.webapp.web.jsptags.PropertyEditLinks.EditLinkAccess;

public class PropertyEditLinksTest {

    @Before
    public void setUp() throws Exception {
    }

    @Ignore
    @Test
    public void testDoDataProp() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testDoObjProp() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testDoDataPropStmt() {
        fail("Not yet implemented");
    }

    @Ignore
    @Test
    public void testDoObjPropStmt() {
        fail("Not yet implemented");
    }

    @Test
    public void testMakeRelativeHrefStringStringArray() {
        Assert.assertEquals("base?bob=true",            PropertyEditLinks.makeRelativeHref("base","bob","true"));
        Assert.assertEquals("base?bob=true+enough",     PropertyEditLinks.makeRelativeHref("base","bob","true enough"));
        Assert.assertEquals("base?bob=where+did+you+go%3F",PropertyEditLinks.makeRelativeHref("base","bob","where did you go?"));
        
        Assert.assertEquals("base?bob=true&amp;hank=false",
                PropertyEditLinks.makeRelativeHref("base","bob","true","hank","false"));
        
        Assert.assertEquals("base?bob=true&amp;hank=he%27s+great+and+all&amp;sue=%26%24%24%25%5E%40%40%40%09%40%24%5E%25+",PropertyEditLinks.makeRelativeHref("base","bob","true","hank","he's great and all", "sue","&$$%^@@@\t@$^% "));
        Assert.assertEquals("base/hats/gloves.jsp?bob=true",PropertyEditLinks.makeRelativeHref("base/hats/gloves.jsp","bob","true"));
    }

    @Test
    public void unescapedUrl(){        
        Assert.assertEquals(
                "edit/editRequestDispatch.jsp?subjectUri=http%3A%2F%2Fvivo.library.cornell.edu%2Fns%2F0.1%23CarusoBrian&amp;predicateUri=http%3A%2F%2Fvivo.library.cornell.edu%2Fns%2F0.1%23isActivityAdvisorFor&amp;defaultForm=true",
                PropertyEditLinks.makeRelativeHref(
        "edit/editRequestDispatch.jsp",
        "subjectUri" ,
        "http://vivo.library.cornell.edu/ns/0.1#CarusoBrian",
        "predicateUri" ,
        "http://vivo.library.cornell.edu/ns/0.1#isActivityAdvisorFor",
        "defaultForm" ,
        "true"));       
    }
    
    @Test
    public void testContains() {
        EditLinkAccess[] access = {EditLinkAccess.ADDNEW , EditLinkAccess.MODIFY };        
        Assert.assertTrue(PropertyEditLinks.contains(access, EditLinkAccess.ADDNEW));
        Assert.assertTrue(PropertyEditLinks.contains(access, EditLinkAccess.MODIFY));
        Assert.assertTrue( ! PropertyEditLinks.contains(access, EditLinkAccess.DELETE));
        Assert.assertTrue( ! PropertyEditLinks.contains(access, null));
        Assert.assertTrue( ! PropertyEditLinks.contains(null , EditLinkAccess.MODIFY));
        Assert.assertTrue( ! PropertyEditLinks.contains(null, null));
    }

}
