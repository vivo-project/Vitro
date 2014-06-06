/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.datatools.dumprestore;

/**
 * TODO
 */
public class DumpTriple {
	private final DumpNode s;
	private final DumpNode p;
	private final DumpNode o;

	public DumpTriple(DumpNode s, DumpNode p, DumpNode o) {
		this.s = s;
		this.p = p;
		this.o = o;
	}

	public DumpNode getS() {
		return s;
	}

	public DumpNode getP() {
		return p;
	}

	public DumpNode getO() {
		return o;
	}

	public String toNtriples() {
		return String.format("%s %s %s .\n", s.toNquad(), p.toNquad(), o.toNquad());
	}

}
