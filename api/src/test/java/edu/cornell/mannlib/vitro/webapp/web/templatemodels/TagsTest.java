package edu.cornell.mannlib.vitro.webapp.web.templatemodels;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.Tags.TagVersionInfo;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.Tags.TagVersionInfo.MatchResult;
import stubs.edu.cornell.mannlib.vitro.webapp.modules.ApplicationStub;
import stubs.javax.servlet.ServletContextStub;

public class TagsTest extends AbstractTestClass {
    private ServletContextStub ctx;
    private File resource;

    @Before
    public void setup() throws IOException {
        resource = File.createTempFile("resource", "");

        ctx = new ServletContextStub();
        ctx.setRealPath("/base/sub/file.js", resource.getPath());
        ctx.setRealPath("/base/sub/file.css", resource.getPath());

        ApplicationStub.setup(ctx, null);

        setContextPath("/context");
    }

    // ----------------------------------------------------------------------
    // Parsing tests
    //
    // Reference for parsing attributes:
    // https://www.w3.org/TR/html/syntax.html#elements-attributes
    // ----------------------------------------------------------------------

    @Test
    public void noAttribute_failure() {
        assertNoMatch("<div height='value'></div>");
    }

    @Test
    public void singleQuote_noTerminator_failure() {
        assertNoMatch("<link rel='stylesheet' href='problem></link>");
    }

    @Test
    public void singleQuotes_embeddedSingleQuote_failure() {
        assertNoMatch("<script src='value'problem'></script");
    }

    @Test
    public void singleQuotes_embeddedDoubleQuote_success() {
        assertMatch("<script src='value\"noproblem'></script",
                "value\"noproblem");
    }

    @Test
    public void doubleQuote_noTerminator_failure() {
        assertNoMatch("<link rel=\"stylesheet\" href=\"problem ></link>");
    }

    @Test
    public void doubleQuotes_embeddedDoubleQuote_failure() {
        assertNoMatch("<link href=\"value\"problem\"></link>");
    }

    @Test
    public void doubleQuotes_embeddedSingleQuote_success() {
        assertMatch("<link href=\"value'noproblem\"></link>",
                "value'noproblem");
    }

    @Test
    public void unquotedBadTerminator_failure() {
        assertNoMatch("<script src=problem");
    }

    @Test
    public void unquoted_embeddedEquals_failure() {
        assertNoMatch("<script src=value=problem>");
    }

    @Test
    public void unquoted_embeddedSingleQuote_failure() {
        assertNoMatch("<script src=value'problem>");
    }

    @Test
    public void unquoted_embeddedDoubleQuote_failure() {
        assertNoMatch("<script src=value\"problem>");
    }

    @Test
    public void unquoted_embeddedBackTick_failure() {
        assertNoMatch("<script src=value`problem>");
    }

    @Test
    public void unquoted_embeddedLessThan_failure() {
        assertNoMatch("<script src=value<problem>");
    }

    @Test
    public void spacesBeforeEquals_success() {
        assertMatch("<link href =value rel='stylesheet'>", "value");
    }

    @Test
    public void spacesAfterEquals_success() {
        assertMatch("<script src= 'value'></script>", "value");
    }

    @Test
    public void noSpacesAroundEquals_success() {
        assertMatch("<script src=\"value\" ></script>", "value");
    }

    // ----------------------------------------------------------------------
    // Substitution tests
    // ----------------------------------------------------------------------

    @Test
    public void noMatch_noChange() {
        assertVersionNotAdded(
                "<script junk='/context/base/sub/file.js' ></script>",
                "no match");
    }

    @Test
    public void alreadyHasQueryString_noChange() {
        assertVersionNotAdded(
                "<script src='/context/base/sub/file.js?why' ></script>",
                "has query");
    }

    @Test
    public void doesntStartWithContextPath_noChange() {
        assertVersionNotAdded(
                "<script src='/notContext/base/sub/file.js' ></script>",
                "context path");
    }

    @Test
    public void noRealPath_noChange() {
        assertVersionNotAdded(
                "<script src='/context/base/sub/nofile.js' ></script>",
                "real path");
    }

    @Test
    @Ignore
    public void exception_noChange() {
        fail("exception_noChange not implemented");
    }

    @Test
    public void doubleQuotes_substitution() {
        assertVersionAdded( //
                "<link href=\"/context/base/sub/file.css\" rel=stylesheet></link>", //
                "<link href=\"/context/base/sub/file.css?version=9999\" rel=stylesheet></link>");
    }

    @Test
    public void singleQuotes_substitution() {
        assertVersionAdded( //
                "<script src='/context/base/sub/file.js' ></script>", //
                "<script src='/context/base/sub/file.js?version=9999' ></script>");
    }

    @Test
    public void unquoted_substitution() {
        assertVersionAdded( //
                "<script type=text/javascript src=/context/base/sub/file.js ></script>", //
                "<script type=text/javascript src=/context/base/sub/file.js?version&eq;9999 ></script>");
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    private void setContextPath(String contextPath) {
        try {
            Field f = UrlBuilder.class.getDeclaredField("contextPath");
            f.setAccessible(true);
            f.set(null, contextPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertMatch(String tag, String expected) {
        TagVersionInfo info = new TagVersionInfo(tag);

        try {
            Field f = TagVersionInfo.class.getDeclaredField("match");
            f.setAccessible(true);
            MatchResult match = (MatchResult) f.get(info);

            assertEquals(expected, match.group);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void assertNoMatch(String tag) {
        TagVersionInfo info = new TagVersionInfo(tag);
        assertFalse(info.hasVersion());
    }

    private void assertVersionAdded(String rawTag, String expected) {
        String actual = createTag(rawTag);
        String canonicalActual = actual.replaceAll("=[0-9a-f]{4}", "=9999")
                .replaceAll("&eq;[0-9a-f]{4}", "&eq;9999");
        assertEquals(expected, canonicalActual);
    }

    private void assertVersionNotAdded(String rawTag, String debugMessage) {
        StringWriter writer = new StringWriter();
        captureLogOutput(Tags.class, writer, true);
        setLoggerLevel(Tags.class, Level.DEBUG);

        String actual = createTag(rawTag);
        assertEquals(rawTag, actual);
        assertThat(writer.toString(), containsString(debugMessage));
    }

    private String createTag(String rawTag) {
        Tags t = new Tags();
        t.add(rawTag);
        return t.list();
    }

}
