package edu.cornell.mannlib.vitro.webapp.searchengine.searchquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.CustomQueryBuilder;
import edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch.SearchType;
import org.junit.Test;

public class CustomQueryBuilderTest {

    @Test
    public void testBuildQueryMatchType() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.MATCH, "title", "test value");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be MatchQuery", query.isMatch());
        assertEquals("Field should match", "title", getQueryField(query));
        assertEquals("Value should match", "test value", getQueryValue(query));
    }

    @Test
    public void testBuildQueryFuzzyType() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.FUZZY, "name", "test~");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be FuzzyQuery", query.isFuzzy());
        assertEquals("Field should match", "name", query.fuzzy().field());
        assertEquals("Value should have tilde removed", "test", getQueryValue(query));
        assertEquals("Fuzziness should be 2", "2", query.fuzzy().fuzziness());
    }

    @Test
    public void testBuildQueryPrefixType() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.PREFIX, "category", "pref");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be PrefixQuery", query.isPrefix());
        assertEquals("Field should match", "category", getQueryField(query));
        assertEquals("Value should match", "pref", getQueryValue(query));
    }

    @Test
    public void testBuildQueryRangeType() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.RANGE, "price", "[100 TO 200]");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be RangeQuery", query.isRange());
        assertEquals("Field should match", "price", query.range().field());
        assertEquals("From value should be cleaned", "100", query.range().from());
        assertEquals("To value should be cleaned", "200", query.range().to());
    }

    @Test
    public void testBuildQueryExistsType() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.EXISTS, "description", "anyValue");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be ExistsQuery", query.isExists());
        assertEquals("Field should match", "description", getQueryField(query));
    }

    @Test
    public void testBuildQueryMatchAllType() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.MATCH_ALL, "anyField", "anyValue");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be MatchAllQuery", query.isMatchAll());
    }

    @Test
    public void testBuildQueryRegexpTypeWithSolrFormat() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.REGEXP, "content", "/test.*pattern/");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be RegexpQuery", query.isRegexp());
        assertEquals("Field should match", "content", getQueryField(query));
        assertEquals("Value should be cleaned", "test.*pattern", getQueryValue(query));
    }

    @Test
    public void testBuildQueryRegexpTypeWithoutSolrFormat() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.REGEXP, "content", "test.*pattern");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be RegexpQuery", query.isRegexp());
        assertEquals("Value should remain unchanged", "test.*pattern", getQueryValue(query));
    }

    @Test
    public void testBuildQueryWildcardType() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.WILDCARD, "title", "test.*");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be WildcardQuery", query.isWildcard());
        assertEquals("Field should match", "title", getQueryField(query));
        assertEquals("Value should be converted", "test*", getQueryValue(query));
    }

    @Test
    public void testBuildQueryWildcardTypeWithStarField() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.WILDCARD, "*", "anyValue");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be MatchAllQuery when field is *", query.isMatchAll());
    }

    @Test
    public void testBuildQueryDefaultType() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.PHRASE, "content", "\"exact phrase\"");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be MatchPhraseQuery", query.isMatchPhrase());
        assertEquals("Field should match", "content", getQueryField(query));
        assertEquals("Value should be cleaned", "exact phrase", getQueryValue(query));
    }

    @Test
    public void testBuildQueryDefaultTypeWithShortPhrase() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.PHRASE, "content", "\"a\"");

        assertNotNull("Query should not be null", query);
        assertEquals("Short phrase should remain", "a", getQueryValue(query));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildQueryWithNullField() {
        CustomQueryBuilder.buildQuery(SearchType.MATCH, null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildQueryWithEmptyField() {
        CustomQueryBuilder.buildQuery(SearchType.MATCH, "", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildQueryWithNullValue() {
        CustomQueryBuilder.buildQuery(SearchType.MATCH, "field", null);
    }

    @Test
    public void testBuildQueryWithEmptyValue() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.MATCH, "field", "");

        assertNotNull("Query should not be null", query);
        assertTrue("Should be MatchQuery", query.isMatch());
        assertEquals("Empty value should be preserved", "", getQueryValue(query));
    }

    @Test
    public void testBuildQueryRangeTypeWithDifferentBrackets() {
        Query query = CustomQueryBuilder.buildQuery(SearchType.RANGE, "date", "(2020 TO 2023]");

        assertNotNull("Query should not be null", query);
        assertEquals("From value should be cleaned", "2020", query.range().from());
        assertEquals("To value should be cleaned", "2023", query.range().to());
    }

    // ----------------------------------------------------------------------
    // Helper methods
    // ----------------------------------------------------------------------

    private String getQueryValue(Query query) {
        if (query.isMatch()) {
            return query.match().query().stringValue();
        } else if (query.isFuzzy()) {
            return query.fuzzy().value().stringValue();
        } else if (query.isPrefix()) {
            return query.prefix().value();
        } else if (query.isRegexp()) {
            return query.regexp().value();
        } else if (query.isWildcard()) {
            return query.wildcard().value();
        } else if (query.isMatchPhrase()) {
            return query.matchPhrase().query();
        }
        return null;
    }

    private String getQueryField(Query query) {
        if (query.isMatch()) {
            return query.match().field();
        } else if (query.isFuzzy()) {
            return query.fuzzy().field();
        } else if (query.isPrefix()) {
            return query.prefix().field();
        } else if (query.isRegexp()) {
            return query.regexp().field();
        } else if (query.isWildcard()) {
            return query.wildcard().field();
        } else if (query.isMatchPhrase()) {
            return query.matchPhrase().field();
        } else if (query.isRange()) {
            return query.range().field();
        } else if (query.isExists()) {
            return query.exists().field();
        }
        return null;
    }
}
