package edu.cornell.mannlib.vitro.webapp.visualization.valueobjects;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * This is the Value Object equivalent for vivo:AcademicDepartmentOrDivision object type.
 * @author cdtank
 *
 */
public class VivoDepartmentOrDivision {

	private String departmentURL;
	private String departmentLabel;
	private Set<VivoCollegeOrSchool> parentColleges = new HashSet<VivoCollegeOrSchool>();

	public VivoDepartmentOrDivision(String departmentURL, VivoCollegeOrSchool parentCollege) {
		this.departmentURL = departmentURL;
		addParentCollege(parentCollege);
	}

	public Set<VivoCollegeOrSchool> getParentCollege() {
		return parentColleges;
	}

	public void addParentCollege(VivoCollegeOrSchool parentCollege) {
		this.parentColleges.add(parentCollege);
	}

	public String getDepartmentURL() {
		return departmentURL;
	}

	public String getDepartmentLabel() {
		if (departmentLabel != null) {
			return departmentLabel;
		} else {
			return "";
		}
	}

	public void setDepartmentLabel(String departmentLabel) {
		this.departmentLabel = departmentLabel;
	}

}
