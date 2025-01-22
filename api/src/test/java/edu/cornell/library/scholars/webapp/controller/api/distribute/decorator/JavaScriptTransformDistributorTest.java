/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.decorator;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.script.ScriptException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.library.scholars.webapp.controller.api.distribute.AbstractDataDistributor;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributor.DataDistributorException;
import edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContext;
import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import stubs.edu.cornell.library.scholars.webapp.controller.api.distribute.DataDistributorContextStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modules.ApplicationStub;
import stubs.javax.servlet.ServletContextStub;

/**
 * Test the basic functions of JavaScriptTransformDistributor.
 * 
 * The simple transform just returns a hard-coded String.
 * 
 * The full script accepts a JSON string, parses it, substitutes a value, and
 * returns the stringified result.
 * 
 * The multi-script requires two additional scripts in order to assemble a
 * hard-coded String.
 */
public class JavaScriptTransformDistributorTest extends AbstractTestClass {
    private static final String ACTION_NAME = "tester";
    private static final String JAVASCRIPT_TYPE = "text/javascript";
    private static final String TEXT_TYPE = "text/plain";

    private static final String BAD_SYNTAX_SCRIPT = "" //
            + "function transform( {}";

    private static final String WRONG_FUNCTION_SCRIPT = "" //
            + "function notTransform() { \n" //
            + "    return 'true'; \n" //
            + "}";

    private static final String WRONG_RETURN_TYPE_SCRIPT = "" //
            + "function transform() { \n" //
            + "    return 3; \n" //
            + "}";

    private static final String SIMPLE_SCRIPT = "" //
            + "function transform() { \n" //
            + "    return 'true'; \n" //
            + "}";

    private static final String SIMPLE_EXPECTED_RESULT = "true";

    private static final String ECHO_SCRIPT = "" //
            + "function transform(data) { \n" //
            + "    return data; \n" //
            + "}";

    private static final String UNICODE_STRING = "Lévesque";

    private static final String FULL_SCRIPT = "" //
            + "function transform(data) { \n" //
            + "    var initial = JSON.parse(data); \n" //
            + "    var result = {}; \n" //
            + "    Object.keys(initial).forEach(populate); \n" //
            + "    return JSON.stringify(result); \n" //
            + "\n" //
            + "    function populate(key) { \n" //
            + "        result[key] = (initial[key] == 'initial') ? \n" //
            + "                      'transformed' : initial[key]; \n" //
            + "    } \n" //
            + "}";

    private static final String TRANSFORMED_STRUCTURE = "{ 'a': 'transformed', 'b': 'constant' }"
            .replace('\'', '\"');

    private static final String INITIAL_STRUCTURE = "{ 'a': 'initial', 'b': 'constant' }"
            .replace('\'', '"');

    private static final String PATH_TO_MISSING_SCRIPT = "/no/script/here";
    private static final String PATH_TO_BAD_SYNTAX_SCRIPT = "/bad/syntax/script.js";

    private static final String PATH_TO_SUPPORTING_SCRIPT_1 = "/support/script1.js";
    private static final String PATH_TO_SUPPORTING_SCRIPT_2 = "/support/script2.js";

    private static final String SUPPORTING_SCRIPT_1 = "" //
            + "function one() { \n" //
            + "    return '1'; \n" //
            + "}";
    private static final String SUPPORTING_SCRIPT_2 = "" //
            + "function two() { \n" //
            + "    return '2'; \n" //
            + "}";
    private static final String SUPPORTED_SCRIPT = "" //
            + "function transform() { \n" //
            + "    return one() + ' ' + two() + ' 3'; \n" //
            + "}";
    private static final String SUPPORTED_EXPECTED_RESULT = "1 2 3";

    private static final String LOGGING_SCRIPT = "" //
            + "function transform() { \n" //
            + "    logger.debug('debug message'); \n" //
            + "    logger.info('info message'); \n" //
            + "    logger.warn('warn message'); \n" //
            + "    logger.error('error message'); \n" //
            + "    return ''; \n" //
            + "}";
    private static final String LOGGER_NAME = JavaScriptTransformDistributor.class
            .getName() + "." + ACTION_NAME;

    public JavaScriptTransformDistributor transformer;
    public TestDistributor child;
    public DataDistributorContext ddc;
    public ByteArrayOutputStream outputStream;
    public String logOutput;

    @Before
    public void setup() {
        ServletContextStub ctx = new ServletContextStub();
        ctx.setMockResource(PATH_TO_BAD_SYNTAX_SCRIPT, BAD_SYNTAX_SCRIPT);
        ctx.setMockResource(PATH_TO_SUPPORTING_SCRIPT_1, SUPPORTING_SCRIPT_1);
        ctx.setMockResource(PATH_TO_SUPPORTING_SCRIPT_2, SUPPORTING_SCRIPT_2);
        ApplicationStub.setup(ctx, null);

        ddc = new DataDistributorContextStub(null);
        outputStream = new ByteArrayOutputStream();

        transformer = new JavaScriptTransformDistributor();
        transformer.setContentType(JAVASCRIPT_TYPE);
        transformer.setActionName(ACTION_NAME);
    }

    // ----------------------------------------------------------------------
    // Basic tests
    // ----------------------------------------------------------------------

    @Test
    public void scriptSuntaxError_throwsException()
            throws DataDistributorException {
        expectException(DataDistributorException.class, "syntax",
                ScriptException.class, "but found");
        transformAndCheck("", BAD_SYNTAX_SCRIPT, "");
    }

    @Test
    public void noTransformFunction_throwsException()
            throws DataDistributorException {
        expectException(DataDistributorException.class, "must have",
                NoSuchMethodException.class, "transform");
        transformAndCheck("", WRONG_FUNCTION_SCRIPT, "");
    }

    @Test
    public void childThrowsException_throwsException()
            throws DataDistributorException {
        expectException(DataDistributorException.class, "Child",
                DataDistributorException.class, "forced");
        child = new TestDistributor(JAVASCRIPT_TYPE, "", true);
        transformAndCheck(SIMPLE_SCRIPT, "");
    }

    @Test
    public void wrongReturnType_throwsException()
            throws DataDistributorException {
        expectException(DataDistributorException.class, "must return a String");
        transformAndCheck("", WRONG_RETURN_TYPE_SCRIPT, "");
    }

    @Test
    public void mostBasicTransform() throws DataDistributorException {
        transformAndCheck("", SIMPLE_SCRIPT, SIMPLE_EXPECTED_RESULT);
    }

    @Test
    public void parseTransformAndStringify() throws DataDistributorException {
        transformAndCheck(INITIAL_STRUCTURE, FULL_SCRIPT,
                TRANSFORMED_STRUCTURE);
    }

    /**
     * This test is intended to check whether Unicode is handled properly
     * regardless of the system's default file encoding.
     * 
     * However there might be failures that only show up if the default value of
     * the system property file.encoding is not UTF-8.
     * 
     * The commented code is a hacky way of ensuring that the file encoding is
     * not UTF-8, but in Java 9 or later, it will cause warning messages.
     * 
     * So we have a test that might pass in some environments and fail in
     * others.
     */
    @Test
    public void unicodeCharactersArePreserved()
            throws DataDistributorException, UnsupportedEncodingException {
        // try {
        // System.setProperty("file.encoding", "ANSI_X3.4-1968");
        // Field charset = Charset.class.getDeclaredField("defaultCharset");
        // charset.setAccessible(true);
        // charset.set(null, null);
        // } catch (Exception e) {
        // throw new RuntimeException(e);
        // }

        child = new TestDistributor(TEXT_TYPE, UNICODE_STRING);
        runTransformer(ECHO_SCRIPT);
        assertEquals(UNICODE_STRING,
                new String(outputStream.toByteArray(), "UTF-8"));
    }

    // ----------------------------------------------------------------------
    // Tests with additional scripts
    // ----------------------------------------------------------------------

    @Test
    public void additionalScriptNotFound_throwsException()
            throws DataDistributorException {
        expectException(DataDistributorException.class, "Can't locate");
        addScripts(PATH_TO_MISSING_SCRIPT);
        transformAndCheck("", SIMPLE_SCRIPT, "");
    }

    @Test
    public void additionalScriptSyntaxError_throwsException()
            throws DataDistributorException {
        expectException(DataDistributorException.class,
                PATH_TO_BAD_SYNTAX_SCRIPT, ScriptException.class, "but found");
        addScripts(PATH_TO_BAD_SYNTAX_SCRIPT);
        transformAndCheck("", SIMPLE_SCRIPT, "");
    }

    @Test
    public void useAdditionalScripts() throws DataDistributorException {
        addScripts(PATH_TO_SUPPORTING_SCRIPT_1, PATH_TO_SUPPORTING_SCRIPT_2);
        transformAndCheck("", SUPPORTED_SCRIPT, SUPPORTED_EXPECTED_RESULT);
    }

    // ----------------------------------------------------------------------
    // Tests with logging
    // ----------------------------------------------------------------------

    @Test
    public void loggingAtDebug_producedFourMessages()
            throws DataDistributorException {
        transformAndCountLogLines("", LOGGING_SCRIPT, Level.DEBUG, 4);
    }

    @Test
    public void loggingAtInfo_producesThreeMessages()
            throws DataDistributorException {
        transformAndCountLogLines("", LOGGING_SCRIPT, Level.INFO, 3);
    }

    @Test
    public void loggingAtWarn_producesTwoMessages()
            throws DataDistributorException {
        transformAndCountLogLines("", LOGGING_SCRIPT, Level.WARN, 2);
    }

    @Test
    public void loggingAtError_producesOneMessage()
            throws DataDistributorException {
        transformAndCountLogLines("", LOGGING_SCRIPT, Level.ERROR, 1);
    }

    @Test
    public void loggingAtOff_producesNoMessages()
            throws DataDistributorException {
        transformAndCountLogLines("", LOGGING_SCRIPT, Level.OFF, 0);
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    private void transformAndCheck(String initial, String script,
            String expected) throws DataDistributorException {
        child = new TestDistributor(JAVASCRIPT_TYPE, initial);
        transformAndCheck(script, expected);
    }

    private void transformAndCheck(String script, String expected)
            throws DataDistributorException {
        runTransformer(script);
        assertEquivalentJson(expected, new String(outputStream.toByteArray()));
    }

    private void transformAndCountLogLines(String initial, String script,
            Level level, int expectedCount) throws DataDistributorException {
        setLoggerLevel(LOGGER_NAME, level);
        logOutput = runTransformer(initial, script);
        assertNumberOfLines(logOutput, expectedCount);
    }

    private String runTransformer(String initial, String script)
            throws DataDistributorException {
        child = new TestDistributor(JAVASCRIPT_TYPE, initial);
        return runTransformer(script);
    }

    private String runTransformer(String script)
            throws DataDistributorException {
        try (Writer logCapture = new StringWriter()) {
            captureLogOutput(LOGGER_NAME, logCapture, true);

            transformer.setScript(script);
            transformer.setChild(child);
            transformer.init(ddc);
            transformer.writeOutput(outputStream);

            return logCapture.toString();
        } catch (IOException e) {
            throw new DataDistributorException(e);
        }
    }

    private void addScripts(String... paths) {
        for (String path : paths) {
            transformer.addScriptPath(path);
        }
    }

    private void assertEquivalentJson(String expected, String actual) {
        try {
            JsonNode expectedNode = new ObjectMapper().readTree(expected);
            JsonNode actualNode = new ObjectMapper().readTree(actual);
            assertEquals(expectedNode, actualNode);
        } catch (IOException e) {
            throw new RuntimeException("Failed to compare JSON", e);
        }
    }

    private void assertNumberOfLines(String s, int expected) {
        int actual = s.isEmpty() ? 0 : s.split("[\\n\\r]").length;
        assertEquals("number of lines", expected, actual);
    }

    /**
     * AbstractTextClasss has this method, but is not overloaded to accept a
     * String for the logger category.
     * 
     * Capture the log for this class to this Writer. Choose whether or not to
     * suppress it from the console.
     */
    protected void captureLogOutput(String category, Writer writer,
            boolean suppress) {
        PatternLayout layout = new PatternLayout("%p %m%n");

        ConsoleAppender appender = new ConsoleAppender();
        appender.setWriter(writer);
        appender.setLayout(layout);

        Logger logger = Logger.getLogger(category);
        logger.removeAllAppenders();
        logger.setAdditivity(!suppress);
        logger.addAppender(appender);
    }

    // ----------------------------------------------------------------------
    // Helper classes
    // ----------------------------------------------------------------------

    /**
     * Just echoes a given string with a given contentType, or throws an
     * Exception, if you prefer.
     */
    private static class TestDistributor extends AbstractDataDistributor {
        private String contentType;
        private String outputString;
        private boolean throwException;

        public TestDistributor(String contentType, String outputString) {
            this(contentType, outputString, false);
        }

        public TestDistributor(String contentType, String outputString,
                boolean throwException) {
            this.contentType = contentType;
            this.outputString = outputString;
            this.throwException = throwException;
        }

        @Override
        public String getContentType() throws DataDistributorException {
            return contentType;
        }

        @Override
        public void writeOutput(OutputStream output)
                throws DataDistributorException {
            try {
                if (throwException) {
                    throw new DataDistributorException("forced exception.");
                }
                output.write(outputString.getBytes("UTF-8"));
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }

        @Override
        public void close() throws DataDistributorException {
            // Nothing to do.
        }

    }

}
