/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.dao.appConfig.jena;


import static edu.cornell.mannlib.vitro.webapp.dao.appConfig.ApplicationConfiguration.*;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ClassDisplayConfig;
import edu.cornell.mannlib.vitro.webapp.dao.appConfig.beans.ShortView;

public class ClassDisplayConfigJenaTest {
    
    private static final String CDCJ_URI = "http://example.com/classDisplayConfig23";
    private static final String SV_URI = "http://example.com/shortDisplayView4433";
    private static final String LOCAL_APP_CONFIG_NS = "http://example.com/appConfNS/";
 
    @Test
    public void readFromRDFTest(){
        
        //create model to read ClassDisplayConfigJena from        
        OntModel m = ModelFactory.createOntologyModel();
        
        m.createIndividual( CDCJ_URI , ClassDisplayConfig )
         .addLiteral(displayRank, 44)
         .addLiteral(displayLimit, 423)
         .addLiteral(displayName, "TEST NAME")
         .addLiteral(publicDescription, "PUBLIC DESC")
         .addProperty(hasShortView, 
             m.createIndividual( SV_URI, ShortDisplayView )
        );
                 
        //m.write(System.out,"N3");        
        Assert.assertNotNull( m.getIndividual(CDCJ_URI));
         
        ClassDisplayConfigDaoJena dao = new ClassDisplayConfigDaoJena( m, LOCAL_APP_CONFIG_NS);
        
        ClassDisplayConfig cdc = dao.getClassDisplayConfigByURI( CDCJ_URI );
        
        Assert.assertNotNull( cdc );
        Assert.assertEquals( CDCJ_URI, cdc.getURI());
        Assert.assertEquals( 44, cdc.getDisplayRank() );
        Assert.assertEquals( 423 , cdc.getDisplayLimit() );
        Assert.assertEquals( "TEST NAME", cdc.getDisplayName());
        Assert.assertEquals( "PUBLIC DESC", cdc.getPublicDescription());                                  
    }
    
    @Test
    public void roundTripTest() throws InsertException{
        ClassDisplayConfig cdc = new ClassDisplayConfig();
        
        cdc.setDisplayLimit(222);
        cdc.setDisplayRank(999);
        cdc.setPublicDescription( "PUBD");
        cdc.setDisplayName("DISPLAY NAME");

        OntModel m = ModelFactory.createOntologyModel();               
        ClassDisplayConfigDaoJena dao = new ClassDisplayConfigDaoJena( m, LOCAL_APP_CONFIG_NS);

        dao.insertNewClassDisplayConfig(cdc);
        
        Assert.assertNotNull( cdc.getURI());
        
        dao = new ClassDisplayConfigDaoJena( m, LOCAL_APP_CONFIG_NS);
        ClassDisplayConfig cdcFromSaved = dao.getClassDisplayConfigByURI( cdc.getURI() );         
        
        Assert.assertEquals( cdc.getURI(), cdcFromSaved.getURI() );
        Assert.assertEquals( cdc.getDisplayRank(), cdcFromSaved.getDisplayRank() );
        Assert.assertEquals( cdc.getDisplayLimit(), cdcFromSaved.getDisplayLimit()  );
        Assert.assertEquals(  cdc.getDisplayName(), cdcFromSaved.getDisplayName());
        Assert.assertEquals(  cdc.getPublicDescription(), cdcFromSaved.getPublicDescription());
        
        //m.write(System.out,"N3");
    }
}
