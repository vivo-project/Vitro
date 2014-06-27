/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

/**
 * TODO
 */
public class DumpQuad {
	private final DumpTriple triple;
	private final DumpNode g;

	public DumpQuad(DumpNode s, DumpNode p, DumpNode o, DumpNode g) {
		this.triple = new DumpTriple(s, p, o);
		this.g = g;
	}

	public DumpTriple getTriple() {
		return triple;
	}

	public DumpNode getG() {
		return g;
	}

}
