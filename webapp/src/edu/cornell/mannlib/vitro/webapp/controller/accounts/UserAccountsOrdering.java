/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts;

/**
 * How are the accounts to be sorted?
 */
public class UserAccountsOrdering {
	public enum Direction {
		ASCENDING("ASC"), DESCENDING("DESC");

		public static Direction DEFAULT_DIRECTION = ASCENDING;

		public static Direction fromKeyword(String keyword) {
			if (keyword == null) {
				return DEFAULT_DIRECTION;
			}

			for (Direction d : Direction.values()) {
				if (d.keyword.equals(keyword)) {
					return d;
				}
			}

			return DEFAULT_DIRECTION;
		}

		public final String keyword;

		Direction(String keyword) {
			this.keyword = keyword;
		}
	}

	public enum Field {
		EMAIL("email"), FIRST_NAME("firstName"), LAST_NAME("lastName"), STATUS(
				"status"), ROLE("ps"), LOGIN_COUNT("count"), LAST_LOGIN_TIME(
				"lastLogin");

		public static Field DEFAULT_FIELD = EMAIL;

		public static Field fromName(String name) {
			if (name == null) {
				return DEFAULT_FIELD;
			}

			for (Field f : Field.values()) {
				if (f.name.equals(name)) {
					return f;
				}
			}

			return DEFAULT_FIELD;
		}

		public final String name;

		Field(String name) {
			this.name = name;
		}
	}

	public static final UserAccountsOrdering DEFAULT_ORDERING = new UserAccountsOrdering(
			Field.DEFAULT_FIELD, Direction.DEFAULT_DIRECTION);

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

	@Override
	public String toString() {
		return "UserAccountsOrdering[field=" + field + ", direction="
				+ direction + "]";
	}
}
