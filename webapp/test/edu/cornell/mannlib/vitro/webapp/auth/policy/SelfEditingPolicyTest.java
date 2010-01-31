/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.auth.policy;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.ArrayIdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.IdentifierBundle;
import edu.cornell.mannlib.vitro.webapp.auth.identifier.SelfEditingIdentifierFactory;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.Authorization;
import edu.cornell.mannlib.vitro.webapp.auth.policy.ifaces.PolicyDecision;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.AddObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.DropObjectPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.EditDataPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.EditObjPropStmt;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.AddNewUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.LoadOntology;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RebuildTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.RemoveUser;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.ServerStatus;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.admin.UpdateTextIndex;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ifaces.RequestedAction;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.CreateOwlClass;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineDataProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.DefineObjectProperty;
import edu.cornell.mannlib.vitro.webapp.auth.requestedAction.ontology.RemoveOwlClass;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class SelfEditingPolicyTest extends AbstractTestClass {

    String SAFE_NS = "http://test.mannlib.cornell.edu/ns/01#";
    String UNSAFE_NS = VitroVocabulary.vitroURI;
    
    String SELFEDITOR_URI =SAFE_NS + "individual244";
    String SAFE_RESOURCE = SAFE_NS + "otherIndividual77777";
    String UNSAFE_RESOURCE = UNSAFE_NS + "otherIndividual99999";
    
    String SAFE_PREDICATE= SAFE_NS + "hasHairStyle";
    String UNSAFE_PREDICATE = UNSAFE_NS + "hasSuperPowers";
       
    SelfEditingPolicy policy ;
    IdentifierBundle ids;
    
    @Before
    public void setUp() throws Exception {
        policy = new SelfEditingPolicy(null,null,null,null);
        
        
        ids = new ArrayIdentifierBundle();
        //ids.add( new NetIdIdentifierFactory.NetId("test223") );
        
        IndividualImpl ind = new IndividualImpl();
        ind.setURI( SELFEDITOR_URI );        
        ids.add( new SelfEditingIdentifierFactory.SelfEditing( ind, SelfEditingIdentifierFactory.NOT_BLACKLISTED ) );
        
    }
    
    @Test
    public void testCanModifiyNs(){
        Assert.assertTrue( policy.canModifyResource("http://bobs.com#hats") );        
        Assert.assertTrue( policy.canModifyResource("ftp://bobs.com#hats"));
        Assert.assertTrue( policy.canModifyResource( SAFE_RESOURCE ));
        Assert.assertTrue( policy.canModifyPredicate( SAFE_PREDICATE ));        
        Assert.assertTrue( policy.canModifyResource("http://bobs.com/hats"));
        
        
        Assert.assertTrue( ! policy.canModifyResource(""));
        Assert.assertTrue( ! policy.canModifyResource(VitroVocabulary.vitroURI + "something"));
        Assert.assertTrue( ! policy.canModifyResource(VitroVocabulary.OWL + "Ontology"));    
        Assert.assertTrue( ! policy.canModifyPredicate( UNSAFE_PREDICATE ));
        Assert.assertTrue( ! policy.canModifyResource( UNSAFE_RESOURCE  ));
        Assert.assertTrue( ! policy.canModifyResource( UNSAFE_NS ));        
        
    }

    @Test
    public void testProhibitedProperties(){
        Set<String> badProps = new HashSet<String>();
        badProps.add("http://mannlib.cornell.edu/bad#prp234");
        badProps.add("http://mannlib.cornell.edu/bad#prp999");
        badProps.add("http://mannlib.cornell.edu/bad#prp333");
        badProps.add("http://mannlib.cornell.edu/bad#prp777");
        badProps.add("http://mannlib.cornell.edu/bad#prp0020");
        SelfEditingPolicy badPropPolicy = new SelfEditingPolicy(badProps,null,null,null);
        
        RequestedAction whatToAuth = new AddObjectPropStmt(
                SELFEDITOR_URI,"http://mannlib.cornell.edu/bad#prp234" ,SAFE_RESOURCE);                
        PolicyDecision dec = badPropPolicy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        whatToAuth = new AddObjectPropStmt(
                SAFE_RESOURCE ,"http://mannlib.cornell.edu/bad#prp234", SELFEDITOR_URI);                
        dec = badPropPolicy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
         whatToAuth = new AddObjectPropStmt(
                SELFEDITOR_URI,"http://mannlib.cornell.edu/bad#prp999" ,SAFE_RESOURCE);                
         dec = badPropPolicy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        whatToAuth = new AddObjectPropStmt(
                SAFE_RESOURCE ,"http://mannlib.cornell.edu/bad#prp999", SELFEDITOR_URI);                
        dec = badPropPolicy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
            
        whatToAuth = new AddObjectPropStmt(
                SAFE_RESOURCE ,SAFE_PREDICATE, SELFEDITOR_URI);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());
        
        whatToAuth = new AddObjectPropStmt(
                SELFEDITOR_URI,SAFE_PREDICATE,SAFE_RESOURCE);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());
        
        whatToAuth = new AddObjectPropStmt(
                SELFEDITOR_URI, UNSAFE_PREDICATE, SAFE_RESOURCE);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        //now with dataprop statements
        whatToAuth = new AddDataPropStmt(
                SELFEDITOR_URI,"http://mannlib.cornell.edu/bad#prp234" ,SAFE_RESOURCE, null, null);                
        dec = badPropPolicy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        whatToAuth = new AddDataPropStmt(
                SAFE_RESOURCE ,"http://mannlib.cornell.edu/bad#prp234", SELFEDITOR_URI, null, null);                
        dec = badPropPolicy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
         whatToAuth = new AddDataPropStmt(
                SELFEDITOR_URI,"http://mannlib.cornell.edu/bad#prp999" ,SAFE_RESOURCE, null, null);                
         dec = badPropPolicy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        whatToAuth = new AddDataPropStmt(
                SAFE_RESOURCE ,"http://mannlib.cornell.edu/bad#prp999", SELFEDITOR_URI, null, null);                
        dec = badPropPolicy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        whatToAuth = new AddDataPropStmt(
                SAFE_RESOURCE ,SAFE_PREDICATE, SELFEDITOR_URI, null, null);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        whatToAuth = new AddDataPropStmt(
                SELFEDITOR_URI,SAFE_PREDICATE,SAFE_RESOURCE, null, null);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());
        
        whatToAuth = new AddDataPropStmt(
                SELFEDITOR_URI, UNSAFE_PREDICATE, SAFE_RESOURCE, null, null);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
    }

    @Test
    public void testVisitIdentifierBundleAddObjectPropStmt() {
        AddObjectPropStmt whatToAuth = new AddObjectPropStmt(
                SELFEDITOR_URI,SAFE_PREDICATE,SAFE_RESOURCE);                
        PolicyDecision dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());
        
        whatToAuth = new AddObjectPropStmt(
                SAFE_RESOURCE ,SAFE_PREDICATE, SELFEDITOR_URI);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());
        
        //this is the case where the editor is not part of the stmt
        whatToAuth = new AddObjectPropStmt(
                SAFE_RESOURCE, SAFE_PREDICATE, SAFE_RESOURCE);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        whatToAuth = new AddObjectPropStmt(
                SELFEDITOR_URI, UNSAFE_PREDICATE, SAFE_RESOURCE);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
        
        whatToAuth = new AddObjectPropStmt(
                SELFEDITOR_URI, SAFE_PREDICATE, UNSAFE_RESOURCE);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
    }
//
//    @Test
//    public void testVisitIdentifierBundleDropResource() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testVisitIdentifierBundleDropDataPropStmt() {
//        fail("Not yet implemented");
//    }
//
    @Test
    public void testVisitIdentifierBundleDropObjectPropStmt() {
        DropObjectPropStmt whatToAuth = new DropObjectPropStmt(
                SELFEDITOR_URI,SAFE_PREDICATE,SAFE_RESOURCE);                
        PolicyDecision dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());

        whatToAuth = new DropObjectPropStmt(
                SAFE_RESOURCE ,SAFE_PREDICATE, SELFEDITOR_URI);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());

//      this is the case where the editor is not part of the stmt
        whatToAuth = new DropObjectPropStmt(
                SAFE_RESOURCE, SAFE_PREDICATE, SAFE_RESOURCE);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());

        whatToAuth = new DropObjectPropStmt(
                SELFEDITOR_URI, UNSAFE_PREDICATE, SAFE_RESOURCE);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());

        whatToAuth = new DropObjectPropStmt(
                SELFEDITOR_URI, SAFE_PREDICATE, UNSAFE_RESOURCE);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
    }
//
//    @Test
//    public void testVisitIdentifierBundleAddResource() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testVisitIdentifierBundleAddDataPropStmt() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testVisitIdentifierBundleUploadFile() {
//        fail("Not yet implemented");
//    }
//
//
    @Test
    public void testVisitIdentifierBundleEditDataPropStmt() {

        DataPropertyStatement dps = new DataPropertyStatementImpl();
        dps.setIndividualURI(SELFEDITOR_URI);
        dps.setDatapropURI(SAFE_PREDICATE);
        dps.setData("junk");        
        EditDataPropStmt whatToAuth = new EditDataPropStmt(dps);                
        PolicyDecision dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());
        
        dps = new DataPropertyStatementImpl();
        dps.setIndividualURI(SELFEDITOR_URI);
        dps.setDatapropURI(UNSAFE_PREDICATE);
        dps.setData("junk");
        whatToAuth = new EditDataPropStmt(dps);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());

        dps = new DataPropertyStatementImpl();
        dps.setIndividualURI(UNSAFE_RESOURCE);
        dps.setDatapropURI(SAFE_PREDICATE);        
        whatToAuth = new EditDataPropStmt(dps);                
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());

        dps = new DataPropertyStatementImpl();
        dps.setIndividualURI(SAFE_RESOURCE);
        dps.setDatapropURI(SAFE_PREDICATE);        
        whatToAuth = new EditDataPropStmt(dps);                      
        dec = policy.isAuthorized(ids, whatToAuth);
        Assert.assertNotNull(dec);
        Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
    }
//
    @Test
    public void testVisitIdentifierBundleEditObjPropStmt() {
    EditObjPropStmt whatToAuth = new EditObjPropStmt(
            SELFEDITOR_URI,SAFE_PREDICATE,SAFE_RESOURCE);                
    PolicyDecision dec = policy.isAuthorized(ids, whatToAuth);
    Assert.assertNotNull(dec);
    Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());
    
    whatToAuth = new EditObjPropStmt(
            SAFE_RESOURCE ,SAFE_PREDICATE, SELFEDITOR_URI);                
    dec = policy.isAuthorized(ids, whatToAuth);
    Assert.assertNotNull(dec);
    Assert.assertEquals(Authorization.AUTHORIZED, dec.getAuthorized());
    
    //this is the case where the editor is not part of the stmt
    whatToAuth = new EditObjPropStmt(
            SAFE_RESOURCE, SAFE_PREDICATE, SAFE_RESOURCE);                
    dec = policy.isAuthorized(ids, whatToAuth);
    Assert.assertNotNull(dec);
    Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
    
    whatToAuth = new EditObjPropStmt(
            SELFEDITOR_URI, UNSAFE_PREDICATE, SAFE_RESOURCE);                
    dec = policy.isAuthorized(ids, whatToAuth);
    Assert.assertNotNull(dec);
    Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
    
    whatToAuth = new EditObjPropStmt(
            SELFEDITOR_URI, SAFE_PREDICATE, UNSAFE_RESOURCE);                
    dec = policy.isAuthorized(ids, whatToAuth);
    Assert.assertNotNull(dec);
    Assert.assertEquals(Authorization.INCONCLUSIVE, dec.getAuthorized());
}


    
  @Test
  public void testVisitIdentifierBundleServerStatus() {
      PolicyDecision dec = policy.isAuthorized(ids, new ServerStatus() );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

  @Test
  public void testVisitIdentifierBundleCreateOwlClass() {
      CreateOwlClass a = new CreateOwlClass();
      a.setSubjectUri("http://someClass/test");
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);      
  }

  @Test
  public void testVisitIdentifierBundleRemoveOwlClass() {
      RemoveOwlClass a = new RemoveOwlClass();
      a.setSubjectUri("http://someClass/test");
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

  @Test
  public void testVisitIdentifierBundleDefineDataProperty() {
      DefineDataProperty a = new DefineDataProperty();
      a.setSubjectUri("http://someClass/test");
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

  @Test
  public void testVisitIdentifierBundleDefineObjectProperty() {
      DefineObjectProperty a = new DefineObjectProperty();
      a.setSubjectUri("http://someClass/test");
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

  @Test
  public void testVisitIdentifierBundleAddNewUser() {
      AddNewUser a = new AddNewUser();      
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

  @Test
  public void testVisitIdentifierBundleRemoveUser() {
      RemoveUser a = new RemoveUser();      
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

  @Test
  public void testVisitIdentifierBundleLoadOntology() {
      LoadOntology a = new LoadOntology();      
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

  @Test
  public void testVisitIdentifierBundleRebuildTextIndex() {
      RebuildTextIndex a = new RebuildTextIndex();      
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

  @Test
  public void testVisitIdentifierBundleUpdateTextIndex() {
      UpdateTextIndex a = new UpdateTextIndex();      
      PolicyDecision dec = policy.visit(ids, a );      
      Assert.assertTrue(dec.getAuthorized() == Authorization.INCONCLUSIVE);
  }

}
