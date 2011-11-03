/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.accounts.manageproxies;


/**
 * On what basis are we selecting proxy relationships?
 * 
 * Are we viewing by Proxy or by Profile? What is the search term, if any? How
 * many results per page, and what page are we on?
 * 
 * Search terms are matched against last name combined with first name, of
 * either UserAccount(Proxy) or Individual(Profile), depending on how we are
 * listing. Searches are case-insensitive.
 */
public class ProxyRelationshipSelectionCriteria {
	public static final int DEFAULT_RELATIONSHIPS_PER_PAGE = 20;

	public static final ProxyRelationshipSelectionCriteria DEFAULT_CRITERIA = new ProxyRelationshipSelectionCriteria(
			DEFAULT_RELATIONSHIPS_PER_PAGE, 1, ProxyRelationshipView.BY_PROXY,
			"");

	public enum ProxyRelationshipView {
		BY_PROXY, BY_PROFILE;

		public static ProxyRelationshipView DEFAULT_VIEW = BY_PROXY;

		public static ProxyRelationshipView fromKeyword(String keyword) {
			if (keyword == null) {
				return DEFAULT_VIEW;
			}

			for (ProxyRelationshipView v : ProxyRelationshipView.values()) {
				if (v.toString().equals(keyword)) {
					return v;
				}
			}

			return DEFAULT_VIEW;
		}

	}

	/** How many relationships should we bring back, at most? */
	private final int relationshipsPerPage;

	/** What page are we on? (1-origin) */
	private final int pageIndex;

	/** What view are we using? */
	private final ProxyRelationshipView viewBy;

	/** What term are we searching on, if any? */
	private final String searchTerm;

	public ProxyRelationshipSelectionCriteria(int relationshipsPerPage,
			int pageIndex, ProxyRelationshipView viewBy, String searchTerm) {
		if (relationshipsPerPage <= 0) {
			throw new IllegalArgumentException("relationshipsPerPage "
					+ "must be a positive integer, not " + relationshipsPerPage);
		}
		this.relationshipsPerPage = relationshipsPerPage;

		if (pageIndex <= 0) {
			throw new IllegalArgumentException("pageIndex must be a "
					+ "positive integer, not " + pageIndex);
		}
		this.pageIndex = pageIndex;

		this.viewBy = nonNull(viewBy, ProxyRelationshipView.DEFAULT_VIEW);
		this.searchTerm = nonNull(searchTerm, "");
	}

	public int getRelationshipsPerPage() {
		return relationshipsPerPage;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public ProxyRelationshipView getViewBy() {
		return viewBy;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	private <T> T nonNull(T t, T nullValue) {
		return (t == null) ? nullValue : t;
	}

	@Override
	public String toString() {
		return "ProxyRelationshipSelectionCriteria[relationshipsPerPage="
				+ relationshipsPerPage + ", pageIndex=" + pageIndex
				+ ", viewBy=" + viewBy + "', searchTerm='" + searchTerm + "']";
	}
}
