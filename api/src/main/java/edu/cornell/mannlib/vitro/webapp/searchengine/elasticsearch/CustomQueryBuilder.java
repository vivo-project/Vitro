package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import co.elastic.clients.elasticsearch._types.query_dsl.FuzzyQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.PrefixQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;

public class CustomQueryBuilder {

    private static final String MAX_FUZZY_EDITS = "2";

    public static Query buildQuery(SearchType queryType, String field, String value) {
        validateInput(field, value);

        switch (queryType) {
            case MATCH:
                return MatchQuery.of(m -> m
                    .field(field)
                    .query(value)
                )._toQuery();
            case FUZZY:
                return FuzzyQuery.of(m -> m
                    .field(field)
                    .value(value.replace("~", ""))
                    .fuzziness(MAX_FUZZY_EDITS)
                )._toQuery();
            case PREFIX:
                return PrefixQuery.of(m -> m
                    .field(field)
                    .value(value)
                )._toQuery();
            case RANGE:
                String[] values = value.split("TO");
                return RangeQuery.of(m -> m
                    .field(field)
                    .from(values[0])
                    .to(values[1])
                )._toQuery();
            case WILDCARD:
                return WildcardQuery.of(m -> m
                    .field(field)
                    .value(value)
                )._toQuery();
            default:
                return MatchPhraseQuery.of(m -> m
                    .field(field)
                    .query(value.substring(1, value.length() - 1)) // Remove leading and trailing '"' character
                )._toQuery();
        }
    }

    private static void validateInput(String field, String value) {
        if (field == null || field.isEmpty()) {
            throw new IllegalArgumentException("Field not specified");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value not specified");
        }
    }
}
