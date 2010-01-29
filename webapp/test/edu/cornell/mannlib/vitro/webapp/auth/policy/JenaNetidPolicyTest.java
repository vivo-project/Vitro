package edu.cornell.mannlib.vitro.webapp.auth.policy;

/* $This file is distributed under the terms of the license in /doc/license.txt$ */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;

/**
 * Simple test of JenaNetidPolicyTest that uses the ExamplePolicy.xml
 * It expects that the model will have the resource
 * <http://vivo.library.cornell.edu/abox#entity11821> will have
 * the datatype property vitro:netid of "bdc34".
 * 
 * @author bdc34
 *
 */

public class JenaNetidPolicyTest extends AbstractTestClass {
    static transient JenaNetidPolicy jniPolicy;
    static transient JenaNetidPolicy unAuthPolicy;
    static transient Model model;    
    static IdentifierBundle idb;

    static String onts[] ={
            "/testontologies/smallVivo-20070809.owl",
            "/testontologies/vitro1.owl",
            "/testontologies/vivo-users.owl" 
    };

    
    /*
     * Loading files with this.getClass().getResourceAsStream()
     * Notice that / is the path seperator and strings that lack
     * a leading slash are relative to the package of the this.getClass(). 
     */
    @BeforeClass
    public static void setUpForClass() throws Exception {  
    	// Suppress warnings from creating default model.
    	setLoggerLevel(RDFDefaultErrorHandler.class, Level.OFF);
        model = ModelFactory.createDefaultModel();

        for( String ont : onts){                    
            InputStream in = JenaNetidPolicyTest.class.getResourceAsStream(ont);
            model.read(in,null);
            in.close();
        }
        OntModel ontModel = ModelFactory.createOntologyModel(ONT_MODEL_SPEC,model);        
        ontModel.prepare();
                
        InputStream in = JenaNetidPolicyTest.class.getResourceAsStream("resources/examplePolicy.xml");
        jniPolicy = new JenaNetidPolicy(model,in);
        in.close();
        
        in = JenaNetidPolicyTest.class.getResourceAsStream("resources/examplePolicy.xml");
        unAuthPolicy = new JenaNetidPolicy(model,in, Authorization.UNAUTHORIZED);
        in.close();
        
        idb = new ArrayIdentifierBundle();
        idb.add(new SelfEditingIdentifierFactory.NetId("bdc34"));       
    }
        
    @Test public void testOfSetupFromXml(){        
        assertNotNull(model);
        JenaNetidPolicy j = jniPolicy;                        
        assertNotNull(j);
        assertNotNull(j.model);
        assertNotNull(j.prefixes);
        assertNotNull( j.actionToQueryStr );
        assertNotNull(j.name);
        assertEquals(j.name, "Example Policy");
        assertTrue(j.prefixes.length() > 0);
        assertTrue( j.actionToQueryStr.size() > 0);        
    }

    @Test public void testAddDataProps(){
        RequestedAction act; PolicyDecision pd;
               
        act = new AddDataPropStmt(
                "http://some.non.existing.resource", 
                "http://some.non.existing.dataproperty", 
                "bogus value", null, null);
        pd = jniPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue( "authorization was " + pd.getAuthorized() + 
                    '\n' + pd.getDebuggingInfo(), 
                    pd.getAuthorized() == Authorization.INCONCLUSIVE);
        
        pd = unAuthPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue( "authorization was " + pd.getAuthorized() + 
                '\n' + pd.getDebuggingInfo(), 
                pd.getAuthorized() == Authorization.INCONCLUSIVE);        
    }
    
    @Test public void testAddDataProps2(){
        RequestedAction act; PolicyDecision pd;
        
        act = new AddDataPropStmt(
                "http://vivo.library.cornell.edu/abox#entity11821",
                "vitro:description",
                "a description of some kind.", null, null);
        pd = jniPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue("authorization was " + pd.getAuthorized() + 
                    '\n' + pd.getDebuggingInfo(), 
                    pd.getAuthorized() == Authorization.AUTHORIZED);
        
        pd = unAuthPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue( "authorization was " + pd.getAuthorized() + 
                '\n' + pd.getDebuggingInfo(), 
                pd.getAuthorized() == Authorization.UNAUTHORIZED);        
    }
    
    @Test public void testDropDataProps1(){
        RequestedAction act; PolicyDecision pd;
        
        DataPropertyStatementImpl dp = new DataPropertyStatementImpl();
        dp.setIndividualURI("http://vivo.library.cornell.edu/abox#entity11821");
        dp.setData("a description of some kind.");
        dp.setDatapropURI("vitro:description");        
        act = new DropDataPropStmt( dp );
        
        pd = jniPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue("authorization was " + pd.getAuthorized() + 
                    '\n' + pd.getDebuggingInfo(), 
                    pd.getAuthorized() == Authorization.AUTHORIZED);
        
        pd = unAuthPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue( "authorization was " + pd.getAuthorized() + 
                '\n' + pd.getDebuggingInfo(), 
                pd.getAuthorized() == Authorization.UNAUTHORIZED);        
    }
    
    @Test public void testDropDataProps2(){
        RequestedAction act; PolicyDecision pd;
        
        DataPropertyStatementImpl dp = new DataPropertyStatementImpl();
        dp.setIndividualURI("http://mannlib.cornell.edu/non.existing.resource");
        dp.setData("a description of some kind.");
        dp.setDatapropURI("vitro:description");        
        act = new DropDataPropStmt( dp );
        
        pd = jniPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue("authorization was " + pd.getAuthorized() + 
                    '\n' + pd.getDebuggingInfo(), 
                    pd.getAuthorized() == Authorization.INCONCLUSIVE);    
        
        pd = unAuthPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue( "authorization was " + pd.getAuthorized() + 
                '\n' + pd.getDebuggingInfo(), 
                pd.getAuthorized() == Authorization.INCONCLUSIVE);        

    }
    
    @Test public void testObjectProps(){
        RequestedAction act = new AddObjectPropStmt(
                "http://vivo.library.cornell.edu/abox#entity11821",
                "vitro:headOf",
                "http://vivo.library.cornell.edu/abox#entity1");
        PolicyDecision pd = jniPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue("authorization was " + pd.getAuthorized(), 
                    pd.getAuthorized() == Authorization.AUTHORIZED);
        
        pd = unAuthPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue( "authorization was " + pd.getAuthorized() + 
                '\n' + pd.getDebuggingInfo(), 
                pd.getAuthorized() == Authorization.UNAUTHORIZED);        
       
        act = new AddObjectPropStmt(
                "http://vivo.library.cornell.edu/abox#entity123",
                "vitro:headOf",
                "http://vivo.library.cornell.edu/abox#entity1");
        pd = jniPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue("authorization was " + pd.getAuthorized(), 
                    pd.getAuthorized() == Authorization.INCONCLUSIVE);
        
        pd = unAuthPolicy.isAuthorized(idb, act);
        assertNotNull(pd);
        assertTrue( "authorization was " + pd.getAuthorized() + 
                '\n' + pd.getDebuggingInfo(), 
                pd.getAuthorized() == Authorization.INCONCLUSIVE);        
    }
    
//    static String ONTOLOGY_ADDR = "http://caruso.mannlib.cornell.edu/xml/rdf/smallVivo-20070809.owl";
//    static String VITRO_ADDR = "http://ivy.mannlib.cornell.edu/ontologies/vitro/vitro1.owl";
//    static String USERS_ADDR = "http://ivy.mannlib.cornell.edu/ontologies/vivo/vivo-users.owl";
    //String ONTOLOGY_ADDR = "http://lowe.mannlib.cornell.edu/ontologies/fao/geopolitical_Ontology_v_0_2.owl";
    //String ONTOLOGY_ADDR = "http://lowe.mannlib.cornell.edu/ontologies/fao/languagecode.owl";
    //String ONTOLOGY_ADDR = "http://localhost/~bjl23/ontologies/VitroFacultyReporting.0.2.owl";
    
    static OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_DL_MEM; // no additional entailment reasoning
    //OntModelSpec ONT_MODEL_SPEC = OntModelSpec.OWL_MEM_MICRO_RULE_INF; // some additional OWL entailment reasoning
    //OntModelSpec ONT_MODEL_SPEC = OntModelSpec.RDFS_MEM_RDFS_INF;

}
