/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.triplesource.impl.tdb;

import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDinteger;
import static com.hp.hpl.jena.datatypes.xsd.XSDDatatype.XSDnonNegativeInteger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.triplesource.impl.DefaultTripleStoreQuirks;

/**
 * TDB has some odd behaviors to deal with.
 */
public class TDBTripleStoreQuirks extends DefaultTripleStoreQuirks {
	private static final Log log = LogFactory
			.getLog(TDBTripleStoreQuirks.class);

	/**
	 * When the file graph was previously written to the TDB store, TDB mangled
	 * some of the literal types: any type of XMLSchema#nonNegativeInteger was
	 * changed to XMLSchema#integer.
	 * 
	 * We need to mangle our new model in the same way before comparing to the
	 * previous one.
	 */
	@Override
	public boolean hasFileGraphChanged(Model fromFile, Model previous,
			String graphURI) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			fromFile.write(buffer, "N-TRIPLE");
			String fromString = buffer.toString("UTF-8");

			String mangleString = fromString.replace(
					XSDnonNegativeInteger.getURI(), XSDinteger.getURI());
			InputStream mangleStream = new ByteArrayInputStream(
					mangleString.getBytes("UTF-8"));
			Model mangled = ModelFactory.createDefaultModel();
			mangled.read(mangleStream, null, "N-TRIPLE");

			return !mangled.isIsomorphicWith(previous);
		} catch (Exception e) {
			log.warn("Failed to test for changes in filegraph. "
					+ "Change assumed.", e);
			return true;
		}
	}

}
