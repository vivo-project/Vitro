package edu.cornell.mannlib.vitro.webapp.searchengine.searchquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.ExpressionTransformer;
import org.junit.Test;

public class ExpressionTransformerTest {

    @Test
    public void testRemoveWhitespacesFromRangeExpression_RemovesSpaces() {
        String input = "[  2020-01-01   TO   2020-12-31 ]";
        String expected = "[2020-01-01TO2020-12-31]";

        String actual = ExpressionTransformer.removeWhitespacesFromRangeExpression(input);

        assertEquals(expected, actual);
    }

    @Test
    public void testRemoveWhitespacesFromRangeExpression_NoRange() {
        String input = "title:hello";
        assertEquals(input, ExpressionTransformer.removeWhitespacesFromRangeExpression(input));
    }

    @Test
    public void testRemoveInvalidParentheses_RemovesUnmatchedClosing() {
        List<String> tokens = java.util.Arrays.asList("(", "foo", ")", ")");
        List<String> cleaned = ExpressionTransformer.removeInvalidParentheses(tokens);

        assertEquals(java.util.Arrays.asList("(", "foo", ")"), cleaned);
    }

    @Test
    public void testRemoveInvalidParentheses_PreservesInsideQuotes() {
        List<String> tokens = java.util.Collections.singletonList("\"(hello world)\"");
        List<String> cleaned = ExpressionTransformer.removeInvalidParentheses(tokens);

        assertEquals(java.util.Collections.singletonList("\"(hello world)\""), cleaned);
    }

    @Test
    public void testIsTokenAPredefinedFieldQuery_TrueForField() {
        assertTrue(ExpressionTransformer.isTokenAPredefinedFieldQuery("title:hello"));
    }

    @Test
    public void testIsTokenAPredefinedFieldQuery_FalseNoColon() {
        assertFalse(ExpressionTransformer.isTokenAPredefinedFieldQuery("hello"));
    }

    @Test
    public void testIsTokenAPredefinedFieldQuery_FalseWhenStartsOrEndsWithColon() {
        assertFalse(ExpressionTransformer.isTokenAPredefinedFieldQuery(":bad"));
        assertFalse(ExpressionTransformer.isTokenAPredefinedFieldQuery("bad:"));
    }

    @Test
    public void testFillInMissingOperators_WrapsPlainTerms() {
        String query = "alpha beta";
        String result = ExpressionTransformer.fillInMissingOperators(query);

        assertTrue(result.contains("ALLTEXT:alpha"));
        assertTrue(result.contains("nameLowercaseSingleValued:alpha"));
    }

    @Test
    public void testFillInMissingOperators_LeavesFieldQuery() {
        String query = "title:java";
        String result = ExpressionTransformer.fillInMissingOperators(query);

        assertTrue(result.contains("title:java"));
        assertFalse(result.contains("ALLTEXT"));
    }

    @Test
    public void testParseAdvancedQuery_ReturnsNonNullForSimpleExpression() {
        List<String> expression = java.util.Collections.singletonList("ALLTEXT:java");

        assertNotNull(new ExpressionTransformer().parseAdvancedQuery(expression));
    }

    @Test
    public void testParseAdvancedQuery_WithLogicalOperators() {
        List<String> expression = java.util.Arrays.asList("field1:value1", "AND", "field2:value2");

        assertNotNull(new ExpressionTransformer().parseAdvancedQuery(expression));
    }
}
