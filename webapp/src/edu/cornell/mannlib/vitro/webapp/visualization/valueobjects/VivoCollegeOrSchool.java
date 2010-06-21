package edu.cornell.mannlib.vitro.webapp.visualization.valueobjects;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 * This is the Value Object equivalent for vivo:CollegeOrSchoolWithinUniversity object type.
 * @author cdtank
 *
 */
public class VivoCollegeOrSchool {

	private String collegeURL;
	private String collegeLabel;
	private Set<VivoDepartmentOrDivision> departments = new HashSet<VivoDepartmentOrDivision>();

	public VivoCollegeOrSchool(String collegeURL) {
		this.collegeURL = collegeURL;
	}
	
	public Set<VivoDepartmentOrDivision> getDepartments() {
		return departments;
	}

	public void addDepartment(VivoDepartmentOrDivision department) {
		this.departments.add(department);
	}

	public String getCollegeURL() {
		return collegeURL;
	}

	public String getCollegeLabel() {
		if (collegeLabel != null) {
			return collegeLabel;
		} else {
			return "";
		}
	}

	public void setCollegeLabel(String collegeLabel) {
		this.collegeLabel = collegeLabel;
	}

}
