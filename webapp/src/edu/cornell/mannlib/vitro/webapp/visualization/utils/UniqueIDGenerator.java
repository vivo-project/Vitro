package edu.cornell.mannlib.vitro.webapp.visualization.utils;

public class UniqueIDGenerator {
	
	private int nextNumericID = 1;

	public int getNextNumericID() {
		int nextNumericID = this.nextNumericID;
		this.nextNumericID++;

		return nextNumericID;
	}

}
