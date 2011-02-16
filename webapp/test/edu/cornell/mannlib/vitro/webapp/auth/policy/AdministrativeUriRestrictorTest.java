/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * TODO
 */
public class AdministrativeUriRestrictorTest extends AbstractTestClass {
	private static final String SAFE_NS = "http://test.mannlib.cornell.edu/ns/01#";
	private static final String UNSAFE_NS = VitroVocabulary.vitroURI;

	private static final String SAFE_RESOURCE = SAFE_NS + "otherIndividual77777";
	private static final String UNSAFE_RESOURCE = UNSAFE_NS	+ "otherIndividual99999";

	private static final String SAFE_PREDICATE = SAFE_NS + "hasHairStyle";
	private static final String UNSAFE_PREDICATE = UNSAFE_NS + "hasSuperPowers";

	private AdministrativeUriRestrictor restrictor;
	@Before
	public void setup() {
		restrictor = new AdministrativeUriRestrictor(null, null, null, null);
	}
	@Test
    public void testCanModifiyNs(){
        Assert.assertTrue( restrictor.canModifyResource("http://bobs.com#hats") );        
        Assert.assertTrue( restrictor.canModifyResource("ftp://bobs.com#hats"));
        Assert.assertTrue( restrictor.canModifyResource( SAFE_RESOURCE ));
        Assert.assertTrue( restrictor.canModifyPredicate( SAFE_PREDICATE ));        
        Assert.assertTrue( restrictor.canModifyResource("http://bobs.com/hats"));
        
        Assert.assertTrue( ! restrictor.canModifyResource(""));
        Assert.assertTrue( ! restrictor.canModifyResource(VitroVocabulary.vitroURI + "something"));
        Assert.assertTrue( ! restrictor.canModifyResource(VitroVocabulary.OWL + "Ontology"));    
        Assert.assertTrue( ! restrictor.canModifyPredicate( UNSAFE_PREDICATE ));
        Assert.assertTrue( ! restrictor.canModifyResource( UNSAFE_RESOURCE  ));
        Assert.assertTrue( ! restrictor.canModifyResource( UNSAFE_NS ));        
    }
}
