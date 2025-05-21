package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public class ExpressionTransformer {

    private static final Map<String, Integer> priorities;

    static {
        Map<String, Integer> priorityTempMap = new HashMap<>();
        priorityTempMap.put("AND", 2);
        priorityTempMap.put("OR", 1);
        priorityTempMap.put("NOT", 3);
        priorityTempMap.put("(", 0);
        priorities = Collections.unmodifiableMap(priorityTempMap);
    }

    public static String removeWhitespacesFromRangeExpression(String expression) {
        String regex = "\\[\\s*([^\\s]+)\\s+TO\\s+([^\\s]+)\\s*\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expression);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = "[" + matcher.group(1) + "TO" + matcher.group(2) + "]";
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static String fillInMissingOperators(String query) {
        String[] tokens = query.split(" ");
        StringBuilder modifiedQuery = new StringBuilder();

        for (int i = 0; i < tokens.length; i++) {
            String currentToken = tokens[i];

            if (!priorities.containsKey(currentToken) && !currentToken.contains(":") && !currentToken.equals(")")) {
                currentToken = "ALLTEXT:" + currentToken + " OR nameLowercaseSingleValued:" + currentToken;
            }

            if (i > 0 && currentToken.contains(":") && !priorities.containsKey(tokens[i - 1])) {
                modifiedQuery.append(" OR ");
            }

            if (i > 0 && currentToken.startsWith("(") && !priorities.containsKey(tokens[i - 1])) {
                modifiedQuery.append(" AND ");
            }

            modifiedQuery.append(currentToken);

            if (i < tokens.length - 1) {
                modifiedQuery.append(" ");
            }
        }

        return modifiedQuery.toString();
    }

    private static SearchType decideQueryType(String field, String value) {
        SearchType searchType = SearchType.MATCH;

        if (value.startsWith("\"") && value.endsWith("\"")) {
            searchType = SearchType.PHRASE;
        } else if (value.contains("TO") && !value.equals("TO")) {
            if (value.equals("[*TO*]")) {
                searchType = SearchType.EXISTS;
            } else {
                searchType = SearchType.RANGE;
            }
        } else if (field.contains("*") || value.contains("*")) {
            if (field.trim().equals("*")) {
                searchType = SearchType.MATCH_ALL;
            } else {
                searchType = SearchType.WILDCARD;
            }
        } else if (value.endsWith("~")) {
            searchType = SearchType.FUZZY;
        }

        return searchType;
    }

    public Query parseAdvancedQuery(List<String> expression) {
        return buildQueryFromPostFixExpression(transformToPostFixNotation(expression));
    }

    private List<String> transformToPostFixNotation(List<String> expression) {
        Stack<String> tokenStack = new Stack<>();
        ArrayList<String> postfixExpression = new ArrayList<>();

        List<String> fixedTokens = fixDisjointFieldTokens(expression);

        for (String token : fixedTokens) {
            if (!priorities.containsKey(token) && !token.equals(")")) {
                postfixExpression.add(token);
            } else if (token.equals("(")) {
                tokenStack.push(token);
            } else if (token.equals(")")) {
                while (!tokenStack.isEmpty() && !tokenStack.peek().equals("(")) {
                    postfixExpression.add(tokenStack.pop());
                }
                tokenStack.pop(); // Remove the '(' from stack
            } else {
                while (!tokenStack.isEmpty() &&
                    priorities.get(token) <= priorities.getOrDefault(tokenStack.peek(), 0)) {
                    postfixExpression.add(tokenStack.pop());
                }
                tokenStack.push(token);
            }
        }

        while (!tokenStack.isEmpty()) {
            postfixExpression.add(tokenStack.pop());
        }

        return postfixExpression;
    }

    private List<String> fixDisjointFieldTokens(List<String> expression) {
        List<String> fixedTokens = new ArrayList<>();
        Set<String> specialCharacters = Set.of("AND", "OR", "NOT", "(", ")");

        for (String token : expression) {
            if (!fixedTokens.isEmpty()) {
                String prev = fixedTokens.get(fixedTokens.size() - 1);

                // Check if previous token contains ":" which indicates a field query
                if (prev.contains(":") &&
                    !token.contains(":") &&
                    !specialCharacters.contains(token)) {

                    // Merge with previous token
                    fixedTokens.set(fixedTokens.size() - 1, prev + " " + token);
                    continue;
                }
            }

            fixedTokens.add(token);
        }
        return fixedTokens;
    }

    private Query buildQueryFromPostFixExpression(List<String> postfixExpression) {
        Stack<Query> queryStack = new Stack<>();

        for (String token : postfixExpression) {
            switch (token.toUpperCase()) {
                case "AND":
                    Query mustContain = queryStack.pop();
                    queryStack.push(BoolQuery.of(q -> {
                        q.must(mustContain);
                        q.must(queryStack.pop());
                        return q;
                    })._toQuery());
                    break;
                case "OR":
                    Query shouldContain = queryStack.pop();
                    queryStack.push(BoolQuery.of(q -> {
                        q.should(shouldContain);
                        q.should(queryStack.pop());
                        return q;
                    })._toQuery());
                    break;
                case "NOT":
                    Query mustNotContain = queryStack.pop();
                    queryStack.push(BoolQuery.of(q -> {
                        q.must(queryStack.pop());
                        q.mustNot(mustNotContain);
                        return q;
                    })._toQuery());
                    break;
                default:
                    String[] fieldValueTuple = token.split(":", 2);

                    if (fieldValueTuple[0].startsWith("-")) {
                        queryStack.push(BoolQuery.of(q -> {
                            q.mustNot(CustomQueryBuilder.buildQuery(
                                decideQueryType(fieldValueTuple[0], fieldValueTuple[1]),
                                fieldValueTuple[0].replaceFirst("-", ""),
                                fieldValueTuple[1]));
                            return q;
                        })._toQuery());
                        break;
                    }

                    SearchType searchType = decideQueryType(fieldValueTuple[0], fieldValueTuple[1]);

                    queryStack.push(CustomQueryBuilder.buildQuery(
                        searchType,
                        fieldValueTuple[0],
                        fieldValueTuple[1]));
            }
        }

        return queryStack.pop();
    }
}
