package edu.cornell.mannlib.vitro.webapp.search.controller;

import java.util.LinkedHashSet;

public class SearchFilterGroup {

	private String id;
	private String label;
	private boolean isPublic = false;
	private boolean hidden = true;

	private LinkedHashSet<String> filters = new LinkedHashSet <>();

	public LinkedHashSet<String> getFilters() {
		return filters;
	}

	public void setFilters(LinkedHashSet<String> filters) {
		this.filters = filters;
	}

	public SearchFilterGroup(String groupId, String groupLabel) {
		this.setId(groupId);
		this.setLabel(groupLabel);
	}

	public void addFilterId(String filterId) {
		if(!filters.contains(filterId)) {
			filters.add(filterId);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public void setPublic(boolean isPublic) {
		this.isPublic = isPublic;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
