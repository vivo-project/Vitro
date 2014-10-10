/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils.logging;

import java.util.Stack;

/**
 * <pre>
 * The string form of a data structure might look like this:
 * HolderOfSeveralComponents[@1023948fe, 
 *     AComponentThing[@dc0048ba],
 *     AThingWithSurprisingFormatting,
 *     ThingWithOneComponent[@120985093,
 *         HoldsNoComponents[@99999999]
 *         ], 
 *     ThingWithNoHashCodeAndAMap[
 *         map={
 *             one,
 *             two,
 *             three
 *             }
 *         ],
 *     AnotherThingWithSurprisingFormatting
 *     ]
 *    
 * Created by following these rules:
 *    create stack holding line number: size of stack is indent level
 *    on [ or { -- write "[" or "{", add line number to stack. If next character is not @, } or ], line-break.
 *    on , -- write ",", line-break
 *    on ] or } -- pop, if same line number, write "]" or "}"
 *                 if different line number, line-break (indent plus 1), write "]" or "}"
 *    where each line-break includes indentation, and skipping any subsequent spaces.
 * </pre>
 */
public class ComplexStringFormatter {
	private final String rawString;
	private final String indentText;

	private int cursor;
	private Stack<Integer> stack = new Stack<>();
	private StringBuilder buffer = new StringBuilder();
	private int lineNumber;

	public ComplexStringFormatter(String rawString) {
		this(rawString, "    ");
	}

	public ComplexStringFormatter(String rawString, String indentText) {
		this.rawString = rawString;
		this.indentText = indentText;
	}

	@Override
	public String toString() {
		while (cursor < rawString.length()) {
			char c = currentChar();
			switch (c) {
			case '[':
			case '{':
				addChar();
				pushDelimiter();
				skipWhiteSpace();
				if (!isNextCharacter('@', '}', ']')) {
					lineBreak();
				}
				break;
			case ',':
				addChar();
				lineBreak();
				break;
			case ']':
			case '}':
				boolean multiline = popDelimiter();
				if (multiline) {
					lineBreak();
					addOneIndent();
				}
				addChar();
				break;
			default:
				addChar();
			}
			next();
		}
		return buffer.toString();
	}

	private void next() {
		cursor++;
	}

	private char currentChar() {
		return rawString.charAt(cursor);
	}

	private void addChar() {
		buffer.append(currentChar());
	}

	private void addOneIndent() {
		buffer.append(indentText);
	}

	private void pushDelimiter() {
		stack.push(lineNumber);
	}

	/** Return true if there was a line break since the delimiter was pushed. */
	private boolean popDelimiter() {
		return lineNumber != stack.pop();
	}

	private boolean isNextCharacter(char... chars) {
		if (cursor + 1 >= rawString.length()) {
			return false;
		}
		char nextChar = rawString.charAt(cursor + 1);
		for (char c : chars) {
			if (nextChar == c) {
				return true;
			}
		}
		return false;
	}

	/** Advance the cursor to before the next non-white character. */
	private void skipWhiteSpace() {
		while (isNextCharacter(' ', '\r', '\n', '\t')) {
			next();
		}
	}

	private void lineBreak() {
		buffer.append('\n');
		lineNumber++;
		for (int i = 0; i < stack.size(); i++) {
			addOneIndent();
		}
		skipWhiteSpace();
	}
}
