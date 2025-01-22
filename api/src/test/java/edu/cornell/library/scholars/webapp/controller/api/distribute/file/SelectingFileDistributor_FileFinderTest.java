/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.file;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.library.scholars.webapp.controller.api.distribute.file.SelectingFileDistributor.FileFinder;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

/**
 * TODO
 */
public class SelectingFileDistributor_FileFinderTest extends AbstractTestClass {
    private static final Path SCHOLARS_HOME = Paths
            .get("//scholars/home/directory");
    
    private static final String NAME_NONE = "noValue";

    private static final String NAME_MULTIPLE = "multiValue";
    private static final String[] VALUE_MULTIPLE = { "first_value",
            "second_value" };

    private static final String NAME_SINGLE = "oneValue";
    private static final String[] VALUE_SINGLE = {
            "how much wood would a woodchuck chuck" };

    private static final Pattern ANY_MATCHER = Pattern.compile("whatever");
    private static final String ANY_TEMPLATE = "/whatever";

    private static final Pattern MATCH_FAILS = Pattern.compile("no match");

    private static final Pattern MATCH_ALL = Pattern.compile("would");
    private static final String PATH_TEMPLATE_ONE_SUB = "/users/scholars/\\0";
    private static final File RESULT_ALL = new File("/users/scholars/would");

    private static final Pattern MATCH_TWO_GROUPS = Pattern
            .compile("(how).*(would)");
    private static final String PATH_TEMPLATE_TWO_SUB = "/users/scholars/\\1/\\2";
    private static final File RESULT_TWO_GROUPS = new File(
            "/users/scholars/how/would");

    private static final String PATH_TEMPLATE_MULTI_SUB = "/users/scholars/\\2/\\1/\\2";
    private static final File RESULT_MULTI_SUB = new File(
            "/users/scholars/would/how/would");

    private Map<String, String[]> parameters;
    private File actual;

    @Before
    public void setup() {
        parameters = new HashMap<>();
        parameters.put(NAME_SINGLE, VALUE_SINGLE);
        parameters.put(NAME_MULTIPLE, VALUE_MULTIPLE);
    }

    // ----------------------------------------------------------------------
    // The tests
    // ----------------------------------------------------------------------

    @Test
    public void noParameterValue_producesWarning_returnsNull()
            throws IOException {
        failWithWarning(NAME_NONE, ANY_MATCHER, ANY_TEMPLATE,
                containsString("No value provided"));
    }

    @Test
    public void multipleParameterValues_producesWarning_returnsNull()
            throws IOException {
        failWithWarning(NAME_MULTIPLE, ANY_MATCHER, ANY_TEMPLATE,
                containsString("Multiple values"));
    }

    @Test
    public void noMatch_producesWarning_returnsNull() throws IOException {
        failWithWarning(NAME_SINGLE, MATCH_FAILS, ANY_TEMPLATE,
                containsString("Failed to parse"));
    }

    @Test
    public void oneMatchGroup_success() {
        assertEquals(RESULT_ALL,
                findIt(NAME_SINGLE, MATCH_ALL, PATH_TEMPLATE_ONE_SUB));
    }

    @Test
    public void twoMatchGroup_success() {
        assertEquals(RESULT_TWO_GROUPS,
                findIt(NAME_SINGLE, MATCH_TWO_GROUPS, PATH_TEMPLATE_TWO_SUB));
    }

    @Test
    public void twoMatchGroupWithMultipleSubstitutions_success() {
        assertEquals(RESULT_MULTI_SUB,
                findIt(NAME_SINGLE, MATCH_TWO_GROUPS, PATH_TEMPLATE_MULTI_SUB));
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    private File findIt(String parameterName, Pattern parameterParser,
            String pathTemplate) {
        return new FileFinder(parameters, parameterName, parameterParser,
                pathTemplate, SCHOLARS_HOME).find();
    }

    private void failWithWarning(String parameterName, Pattern parameterParser,
            String pathTemplate, Matcher<String> warningMatcher)
            throws IOException {
        try (Writer logCapture = new StringWriter()) {
            captureLogOutput(SelectingFileDistributor.class, logCapture, true);

            actual = findIt(parameterName, parameterParser, pathTemplate);

            assertThat(logCapture.toString(), warningMatcher);
            assertNull(actual);
        }
    }

}
