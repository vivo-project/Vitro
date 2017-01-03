/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.sparqlrunner;

import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.dataProperty;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.model;
import static edu.cornell.mannlib.vitro.testing.ModelUtilitiesTestHelper.objectProperty;
import static org.apache.jena.datatypes.xsd.XSDDatatype.XSDinteger;
import static org.apache.jena.datatypes.xsd.XSDDatatype.XSDlong;
import static org.junit.Assert.assertEquals;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

public class ResultSetParserTest extends AbstractTestClass {

	private static final String NAMESPACE = "http://namespace#";

	private static final String SUBJECT = NAMESPACE + "subject";

	private static final String PREDICATE_URI = NAMESPACE + "uri";
	private static final String OBJECT_URI = NAMESPACE + "objectUri";
	private static final String OBJECT_URI_DEFAULT = NAMESPACE
			+ "objectUriDefault";

	private static final String PREDICATE_ANON = NAMESPACE + "anonymous";
	private static final String OBJECT_ANON_DEFAULT = NAMESPACE + "anonDefault";

	private static final String PREDICATE_STRING = NAMESPACE + "string";
	private static final String OBJECT_STRING = "objectString";
	private static final String OBJECT_STRING_DEFAULT = "objectStringDefault";

	private static final String PREDICATE_INT = NAMESPACE + "int";
	private static final Integer OBJECT_INT = 4;
	private static final Integer OBJECT_INT_DEFAULT = -1;

	private static final String PREDICATE_LONG = NAMESPACE + "long";
	private static final Long OBJECT_LONG = 888L;
	private static final Long OBJECT_LONG_DEFAULT = -333L;

	private static final Object PARSING_FAILURE = "PARSING_FAILURE";
	private static final Object NO_RECORDS_FOUND = "NO_RECORDS_FOUND";

	private static final String SELECT_QUERY = "" //
			+ "SELECT ?uri ?anonymous ?string ?int ?long \n" //
			+ "WHERE { \n" //
			+ "  ?s <" + PREDICATE_URI + "> ?uri . \n" //
			+ "  ?s <" + PREDICATE_ANON + "> ?anon . \n" //
			+ "  ?s <" + PREDICATE_STRING + "> ?string . \n" //
			+ "  ?s <" + PREDICATE_INT + "> ?int . \n" //
			+ "  ?s <" + PREDICATE_LONG + "> ?long . \n" //
			+ "} \n";

	private Model model;

	@Before
	public void setup() {
		setLoggerLevel(ModelSelectQueryContext.class, Level.OFF);

		model = model(objectProperty(SUBJECT, PREDICATE_URI, OBJECT_URI),
				objectProperty(SUBJECT, PREDICATE_ANON),
				dataProperty(SUBJECT, PREDICATE_STRING, OBJECT_STRING),
				dataProperty(SUBJECT, PREDICATE_INT, OBJECT_INT, XSDinteger),
				dataProperty(SUBJECT, PREDICATE_LONG, OBJECT_LONG, XSDlong));
	}

	// ----------------------------------------------------------------------
	// The tests
	// ----------------------------------------------------------------------

	@Test
	public void errorInParse_yieldsDefaultValue() {
		assertParsingResult(PARSING_FAILURE, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				throw new RuntimeException("I refuse to parse this!");
			}
		});
	}

	// ----------------------------------------------------------------------

	@Test
	public void expectedUriFoundUri() {
		assertParsingResult(OBJECT_URI, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifResourcePresent(solution, "uri", OBJECT_URI_DEFAULT);
			}
		});
	}

	@Test
	public void expectedUriFoundNothing_returnsDefault() {
		assertParsingResult(OBJECT_URI_DEFAULT, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifResourcePresent(solution, "nothing",
						OBJECT_URI_DEFAULT);
			}
		});
	}

	/**
	 * TODO Ignoring because the anonymous node isn't showing up in the
	 * ResultSet. Why not? Anyway, this behaves like "foundNothing".
	 */
	@Test
	@Ignore
	public void expectedUriFoundAnonymous_returnsDefault() {
		assertParsingResult(OBJECT_URI_DEFAULT, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifResourcePresent(solution, "anon", OBJECT_URI_DEFAULT);
			}
		});
	}

	@Test
	public void expectedUriFoundString_returnsDefault() {
		assertParsingResult(OBJECT_URI_DEFAULT, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifResourcePresent(solution, "string", OBJECT_URI_DEFAULT);
			}
		});
	}

	// ----------------------------------------------------------------------

	@Test
	public void expectedStringFoundString() {
		assertParsingResult(OBJECT_STRING, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLiteralPresent(solution, "string",
						OBJECT_STRING_DEFAULT);
			}
		});
	}

	@Test
	public void expectedStringFoundNothing_returnsDefault() {
		assertParsingResult(OBJECT_STRING_DEFAULT, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLiteralPresent(solution, "nothing",
						OBJECT_STRING_DEFAULT);
			}
		});
	}

	@Test
	public void expectedStringFoundResource_fails() {
		assertParsingResult(PARSING_FAILURE, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLiteralPresent(solution, "uri", OBJECT_STRING_DEFAULT);
			}
		});
	}

	@Test
	public void expectedStringFoundInt_returnsStringValueOfInt() {
		assertParsingResult(OBJECT_INT.toString(), new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLiteralPresent(solution, "int", OBJECT_STRING_DEFAULT);
			}
		});
	}

	// ----------------------------------------------------------------------

	@Test
	public void expectedIntFoundInt() {
		assertParsingResult(OBJECT_INT, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifIntPresent(solution, "int", OBJECT_INT_DEFAULT);
			}
		});
	}

	@Test
	public void expectedIntFoundNothing() {
		assertParsingResult(OBJECT_INT_DEFAULT, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifIntPresent(solution, "nothing", OBJECT_INT_DEFAULT);
			}
		});
	}

	@Test
	public void expectedIntFoundResource_fails() {
		assertParsingResult(PARSING_FAILURE, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifIntPresent(solution, "uri", OBJECT_INT_DEFAULT);
			}
		});
	}

	@Test
	public void expectedIntFoundString_fails() {
		assertParsingResult(PARSING_FAILURE, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifIntPresent(solution, "string", OBJECT_INT_DEFAULT);
			}
		});
	}

	@Test
	public void expectedIntFoundLong() {
		assertParsingResult(new Integer(OBJECT_LONG.intValue()),
				new BaseParser() {
					@Override
					Object parseOneLine(QuerySolution solution) {
						return ifIntPresent(solution, "long",
								OBJECT_INT_DEFAULT);
					}
				});
	}

	// ----------------------------------------------------------------------

	@Test
	public void expectedLongFoundLong() {
		assertParsingResult(OBJECT_LONG, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLongPresent(solution, "long", OBJECT_LONG_DEFAULT);
			}
		});
	}

	@Test
	public void expectedLongFoundNothing() {
		assertParsingResult(OBJECT_LONG_DEFAULT, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLongPresent(solution, "nothing", OBJECT_LONG_DEFAULT);
			}
		});
	}

	@Test
	public void expectedLongFoundResource_fails() {
		assertParsingResult(PARSING_FAILURE, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLongPresent(solution, "uri", OBJECT_LONG_DEFAULT);
			}
		});
	}

	@Test
	public void expectedLongFoundString_fails() {
		assertParsingResult(PARSING_FAILURE, new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLongPresent(solution, "string", OBJECT_LONG_DEFAULT);
			}
		});
	}

	@Test
	public void expectedLongFoundInt() {
		assertParsingResult(new Long(OBJECT_INT), new BaseParser() {
			@Override
			Object parseOneLine(QuerySolution solution) {
				return ifLongPresent(solution, "int", OBJECT_LONG_DEFAULT);
			}
		});
	}

	/**
	 * <pre>
	 * ifLongPresent, return value or default.
	 *   present, absent
	 * </pre>
	 */
	// ----------------------------------------------------------------------
	// Helper methods
	// ----------------------------------------------------------------------

	private void assertParsingResult(Object expected, BaseParser parser) {
		Object actual = SparqlQueryRunner
				.createSelectQueryContext(model, SELECT_QUERY).execute()
				.parse(parser);
		assertEquals(expected, actual);
	}

	private abstract static class BaseParser extends ResultSetParser<Object> {
		@Override
		protected Object defaultValue() {
			return PARSING_FAILURE;
		}

		@Override
		protected Object parseResults(String queryStr, ResultSet results) {
			if (results.hasNext()) {
				return parseOneLine(results.next());
			} else {
				return NO_RECORDS_FOUND;
			}
		}

		abstract Object parseOneLine(QuerySolution solution);
	}

	// private String dumpSolution(QuerySolution solution) {
	// Map<String, Object> fields = new HashMap<>();
	// Iterator<String> names = solution.varNames();
	// while (names.hasNext()) {
	// String name = names.next();
	// fields.put(name, solution.get(name));
	// }
	// return fields.toString();
	// }
	//
}
