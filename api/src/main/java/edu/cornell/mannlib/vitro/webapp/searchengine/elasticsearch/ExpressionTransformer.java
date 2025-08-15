package edu.cornell.mannlib.vitro.webapp.searchengine.elasticsearch;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
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
        String[] tokens = removeInvalidParentheses(List.of(query.split(" "))).toArray(new String[0]);
        StringBuilder modifiedQuery = new StringBuilder();

        boolean insidePhrase = false;
        int parenBalance = 0;
        String currentToken = "";

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].trim().isEmpty()) {
                continue;
            }

            currentToken = insidePhrase ? currentToken + " " + tokens[i] : tokens[i];

            if (currentToken.startsWith("\"") && !insidePhrase && hasClosingQuote(tokens, i)) {
                insidePhrase = true;
            } else if (currentToken.endsWith("\"") && insidePhrase) {
                insidePhrase = false;
            }

            boolean isOpeningParen = currentToken.equals("(");
            boolean isClosingParen = currentToken.equals(")");
            boolean isLogicalOperator = priorities.containsKey(currentToken);
            boolean isFieldQuery = isTokenAPredefinedFieldQuery(currentToken);

            if (isFieldQuery && currentToken.contains(":\"") && !currentToken.endsWith("\"")) {
                modifiedQuery.append(currentToken.split(":")[0]).append(":");
                currentToken = currentToken.split(":")[1];
                insidePhrase = true;
            }

            if (isOpeningParen) {
                parenBalance++;
            } else if (isClosingParen) {
                if (parenBalance == 0) {
                    continue;
                }
                parenBalance--;
            }

            if (i > 0 && isOpeningParen && !priorities.containsKey(tokens[i - 1]) && !tokens[i - 1].equals("(") &&
                hasClosingParen(tokens, i)) {
                modifiedQuery.append(" AND ");
            }

            if (i > 0 && tokens[i - 1].equals(")") && !insidePhrase && !isLogicalOperator && !isClosingParen) {
                modifiedQuery.append(" AND ");
            }

            if (i > 0 && isFieldQuery && !priorities.containsKey(tokens[i - 1]) && !tokens[i - 1].equals("(")) {
                modifiedQuery.append(" OR ");
            }

            if (!isLogicalOperator && !isFieldQuery && !isOpeningParen && !isClosingParen && !insidePhrase) {
                if (i > 0 && modifiedQuery.toString().trim().endsWith("(")) {
                    modifiedQuery.append(" OR ");
                } else if (!modifiedQuery.toString().endsWith(":")) {
                    currentToken = "( ALLTEXT:" + currentToken + " OR nameLowercaseSingleValued:" + currentToken + " )";
                }
            }

            if (!insidePhrase) {
                modifiedQuery.append(currentToken);
                if (i < tokens.length - 1) {
                    modifiedQuery.append(" ");
                }
            }
        }

        if (modifiedQuery.length() == 0 && insidePhrase) {
            return "ALLTEXT: " + currentToken + "\"";
        }

        return modifiedQuery.toString();
    }

    public static List<String> removeInvalidParentheses(List<String> tokens) {
        List<String> result = new ArrayList<>();
        Deque<Integer> openParenIndexes = new ArrayDeque<>();

        boolean insideQuotes = false;

        Set<Integer> invalidIndexes = new HashSet<>();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            long quoteCount = token.chars().filter(ch -> ch == '"').count();
            if (quoteCount % 2 != 0) {
                insideQuotes = !insideQuotes;
            }

            if (!insideQuotes) {
                if (token.equals("(")) {
                    openParenIndexes.push(i);
                } else if (token.equals(")")) {
                    if (!openParenIndexes.isEmpty()) {
                        openParenIndexes.pop(); // matched
                    } else {
                        invalidIndexes.add(i); // unmatched closing paren
                    }
                }
            }
        }

        invalidIndexes.addAll(openParenIndexes);

        for (int i = 0; i < tokens.size(); i++) {
            if (!invalidIndexes.contains(i) && !tokens.get(i).isBlank()) {
                result.add(tokens.get(i));
            }
        }

        return result;
    }

    private static boolean hasClosingParen(String[] tokens, int fromIndex) {
        boolean insideQuotes = false;

        for (int j = fromIndex + 1; j < tokens.length; j++) {
            String token = tokens[j];

            long quoteCount = token.chars().filter(ch -> ch == '"').count();
            if (quoteCount % 2 != 0) {
                insideQuotes = !insideQuotes;
            }

            if (!insideQuotes && token.equals(")")) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasClosingQuote(String[] tokens, int fromIndex) {
        boolean insideQuotes = false;
        for (int j = fromIndex; j < tokens.length; j++) {
            long quoteCount = tokens[j].chars().filter(ch -> ch == '"').count();
            if (quoteCount % 2 != 0) {
                insideQuotes = !insideQuotes;
            }
        }
        return !insideQuotes;
    }

    public static boolean isTokenAPredefinedFieldQuery(String token) {
        if (!token.contains(":")) {
            return false;
        }

        return !token.startsWith(":") && !token.endsWith(":");
    }

    private static SearchType decideQueryType(String field, String value) {
        SearchType searchType = SearchType.MATCH;

        if (value.startsWith("\"") && value.endsWith("\"")) {
            searchType = SearchType.PHRASE;
        } else if (value.contains("TO") && !value.equals("TO")) {
            if (value.replace(" ", "").equals("[*TO*]")) {
                searchType = SearchType.EXISTS;
            } else {
                searchType = SearchType.RANGE;
            }
        } else if (field.contains("*") || value.contains("*")) {
            if (field.trim().equals("*")) {
                searchType = SearchType.MATCH_ALL;
            } else if (value.startsWith("/") && value.endsWith("/") && value.length() > 1) {
                searchType = SearchType.REGEXP;
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

        for (int i = 0; i < expression.size(); i++) {
            String token = expression.get(i);

            if ("TO".equals(token) && i > 0 && i < expression.size() - 1) {
                String before = fixedTokens.remove(fixedTokens.size() - 1);
                String after = expression.get(i + 1);
                fixedTokens.add((before + " TO " + after).replace("\"", ""));
                i++;
                continue;
            }

            if (!fixedTokens.isEmpty()) {
                String prev = fixedTokens.get(fixedTokens.size() - 1);

                if (prev.contains(":") &&
                    !token.contains(":") &&
                    !specialCharacters.contains(token) &&
                    !"TO".equals(token)) {

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
