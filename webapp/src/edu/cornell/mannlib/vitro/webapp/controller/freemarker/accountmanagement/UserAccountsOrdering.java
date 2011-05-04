/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.freemarker.accountmanagement;

/**
 * How are the accounts to be sorted?
 */
public class UserAccountsOrdering {
	public enum Direction {
		ASCENDING("ASC"), DESCENDING("DESC");

		public final String keyword;

		Direction(String keyword) {
			this.keyword = keyword;
		}
	}

	public enum Field {
		EMAIL("email"), FIRST_NAME("firstName"), LAST_NAME("lastName"), STATUS(
				"status"), ROLE("ps"), LOGIN_COUNT("count");

		public final String variableName;

		Field(String variableName) {
			this.variableName = variableName;
		}
	}

	public static final UserAccountsOrdering DEFAULT_ORDERING = new UserAccountsOrdering(
			Field.EMAIL, Direction.ASCENDING);

	private final Field field;
	private final Direction direction;

	public UserAccountsOrdering(Field field, Direction direction) {
		this.field = field;
		this.direction = direction;
	}

	public Field getField() {
		return field;
	}

	public Direction getDirection() {
		return direction;
	}
}
