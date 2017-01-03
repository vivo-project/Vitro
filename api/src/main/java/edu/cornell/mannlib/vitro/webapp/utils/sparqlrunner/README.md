# A conversational API for running SPARQL queries

`edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner` provides a syntactically simple
way to manipulate and execute SPARQL queries. The query may be run against a Jena `Model`
or a Vitro `RDFService`.  

The API supports the most common tasks:

+ bind URI values to variables in the query.
+ bind plain literal values to variables in the query.
+ execute a CONSTRUCT query and return a Jena model.
+ execute a SELECT query and return the results in a variety of formats:
  + a List of String values
  + a Set of String values
  + a List of Maps of String values
  + a JSON-formatted output stream
  + the output of a custom parser class.
  
The API could reasonably be extended to perform other operations, or to produce
additional forms of output. It currently provides only the functionality that
was immediately useful at the time of writing.

## Usage Examples

In general, the usage pattern is to

+ create the query context
+ optionally bind values to variables in the query
+ execute
+ transform the results

### SELECT to a List of Strings

Run a SELECT query, binding a value to the variable `?uri` and returning
a List of the values that are returned for the variable `?partner`.

    List<String> values = createSelectQueryContext(rdfService, queryString)
                               .bindVariableToUri("uri", uri)
 				               .execute()
 				               .toStringFields("partner")
 				               .flatten();
 
### SELECT to a List of Maps
 
 Example: Run a SELECT query, returning a list of maps of strings. Each map
 represents a record in the ResultSet, with variable names mapped to the 
 value in that record, translated to a String.

    List<Map<String, String>> maps = createSelectQueryContext(model, queryString)
 				                         .execute()
 				                         .toStringFields()
 				                         .getListOfMaps();

>_Note: Null or empty values are omitted from the maps; empty maps are omitted from the list._

 
### SELECT to a Parser
 
    MyQueryResult mqr = createSelectQueryContext(model, queryString)
                            .bindVariableToUri("uri", uri)
                            .bindVariableToPlainLiteral("id", "PW-4250")
 				            .execute()
 				            .parse(new MyQueryParser());
 
### CONSTRUCT to a Model
 
     Model m = createConstructQueryContext(rdfService, queryString)
                   .execute()
                   .toModel();
                   
## Using the QueryHolder

The `QueryHolder` class can be useful in itself, for manipulating a query string.

### Bind variables in a query

    String boundQuery = queryHolder(rawQuery)
                          .bindToUri("uri", uri)
                          .getQueryString();
                          
### Inquire about unbound variables

    boolean foundIt = queryHolder(rawQuery)
                        .hasVariable("name");
                        
### Prepare a query in advance of executing it.
    QueryHolder qh = queryHolder(queryString)
                             .bindToUri("uri", uri));
                             .bindVariableToPlainLiteral("id", "PW-4250")
    List<Map<String, String> map = createSelectQueryContext(model, qh)
                             .execute()
                             .toStringFields()
                             .getListOfMaps();

## Parsing a ResultSet

By writing a parser, you can translate the `ResultSet` from a SPARQL query 
into any object, according to the translation algorithm you specify. You must 
provide the parser with both a parsing method and a default value. The parser
will return the default value if the parsing method fails for any reason. In
this way, you are assured that the parser will not throw an exception.

Here is an example of a simple parser. It inspects the first record in the 
result set and builds an `ItemInfo` object.

	private static class ExpandProfileParser extends ResultSetParser<ItemInfo> {
		@Override
		protected ItemInfo defaultValue() {
			return new ItemInfo();
		}

		@Override
		protected ItemInfo parseResults(String queryStr, ResultSet results) {
			ItemInfo item = new ItemInfo();
			if (results.hasNext()) {
				QuerySolution solution = results.next();
				item.label = ifLiteralPresent(solution, "label", "");
				item.classLabel = ifLiteralPresent(solution, "classLabel", "");
				item.imageUrl = ifLiteralPresent(solution, "imageUrl", "");
			}
			return item;
		}
	}

This parser would be used in this way:

    ItemInfo info = createSelectQueryContext(model, queryString)
 				        .execute()
 				        .parse(new ExpandProfileParser());

