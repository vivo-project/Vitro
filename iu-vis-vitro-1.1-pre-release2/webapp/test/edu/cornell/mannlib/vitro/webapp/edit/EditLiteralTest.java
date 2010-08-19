/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Literal;

public class EditLiteralTest {

    @Test
    public void testEqual(){
        Assert.assertTrue( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)null ),
                new EditLiteral("dog", (String)null, (String)null )
        ));
        Assert.assertTrue( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri", (String)null ),
                new EditLiteral("dog", (String)"http://someUri", (String)null )
        ));
        Assert.assertTrue( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)"SOMELANG" ),
                new EditLiteral("dog", (String)null, (String)"SOMELANG" )
        ));

        //datatype trumps lang
        Assert.assertTrue( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri", (String)"SOMELANG" ),
                new EditLiteral("dog", (String)"http://someUri", (String)null )
        ));
        Assert.assertTrue( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri", (String)null ),
                new EditLiteral("dog", (String)"http://someUri", (String)"OTHERLANG" )
        ));
        Assert.assertTrue( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri", (String)"SOMELANG" ),
                new EditLiteral("dog", (String)"http://someUri", (String)"SOMELANG" )
        ));
        Assert.assertTrue( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri", (String)"SOMELANG" ),
                new EditLiteral("dog", (String)"http://someUri", (String)"OTHERLANG" )
        ));

        Assert.assertTrue( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)"SOMELANG" ),
                new EditLiteral("dog", (String)null, (String)"SOMELANG" )
        ));


        //NOT EQUAL CASES
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)null ),
                new EditLiteral("catFood", (String)null, (String)null )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri",      (String)null ),
                new EditLiteral("dog", (String)"http://otherUri.com", (String)null )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)"SOMELANG" ),
                new EditLiteral("dog", (String)null, (String)"OTHERLANG" )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri",      (String)"SOMELANG" ),
                new EditLiteral("dog", (String)"http://otherUri.com", (String)"SOMELANG" )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri",      (String)"SOMELANG" ),
                new EditLiteral("dog", (String)"http://otherUri.com", (String)null )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri",      (String)null ),
                new EditLiteral("dog", (String)"http://otherUri.com", (String)"SOMELANG" )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri", (String)null ),
                new EditLiteral("dog", (String)null, (String)null )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)null ),
                new EditLiteral("dog", (String)"http://someUri", (String)null )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)"SOMELANG" ),
                new EditLiteral("dog", (String)null, (String)null )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)null ),
                new EditLiteral("dog", (String)null, (String)"SOMELANG" )
        ));

        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)null ),
                new EditLiteral(null, (String)null, (String)null )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral(null, (String)null, (String)null ),
                new EditLiteral("dog", (String)null, (String)null )
        ));

        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)null, (String)"SOMELANG" ),
                new EditLiteral("catFood", (String)null, (String)"SOMELANG" )
        ));
        Assert.assertFalse( EditLiteral.equalLiterals(
                new EditLiteral("dog", (String)"http://someUri", (String)null ),
                new EditLiteral("catFood", (String)"http://someUri", (String)null )
        ));           
    }
    
    
    @Test
    public void testBug2(){
        
        Literal a2 = new EditLiteral("<ul><li>Stephen G. Yusem is the senior partner of High, Swartz, Roberts &amp; Seidel in Norristown, Pennsylvania. <br /></li><li>He has been certified as a mediator for the United States District Court for the Eastern District of Pennsylvania since 1991. <br /></li><li>He is a Fellow of the College of Commercial Arbitrators, a member of the Chartered Institute of Arbitrators, a panelist on the CPR Roster of Distinguished Neutrals and a past director of the Commercial Section of the Association for Conflict Resolution. <br /></li><li>He co-chairs the Arbitration Committee of the Dispute Resolution Section of the American Bar Association and is a former member of the Arbitration Faculty of the American Arbitration Association. <br /></li><li>He is a past president of the Montgomery County (PA) Bar Association and was Founding President of the Montgomery County Bar Foundation. He co-teaches Dispute Resolution: Negotiation, Mediation and Arbitration in the fall semester.</li></ul>",
                "http://www.w3.org/2001/XMLSchema#string","");
        Literal b2 = new EditLiteral("<ul><li>Stephen G. Yusem is the senior partner of High, Swartz, Roberts &amp; Seidel in Norristown, Pennsylvania. <br /></li><li>He has been certified as a mediator for the United States District Court for the Eastern District of Pennsylvania since 1991. <br /></li></ul>",
                "http://www.w3.org/2001/XMLSchema#string",null);
        Assert.assertFalse( EditLiteral.equalLiterals( a2, b2 ) );
                  
    }
    
}
