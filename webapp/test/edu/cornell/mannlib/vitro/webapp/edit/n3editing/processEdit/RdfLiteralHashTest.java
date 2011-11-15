/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit;



import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.processEdit.RdfLiteralHash;

public class RdfLiteralHashTest {

    final String TEST_VALUE ="this is a test literal string";
    final String TEST_DATA_PROP_URI ="http://this.is.a.test.uri.com/1999/02/blec-ns#test2332";
    final String TEST_INDIVIDUAL_URI ="http://this.is.a.testUri.com/1999/02/bleck-ns#INDIVIDUAL787878";
    final String TEST_DATA_TYPE_URI ="http://this.is.a.uri.com/TEST/DATA/TYPE#e8";
    final String TEST_LANG = "ENG";

    @Test
    public void testEdBackground(){
        String value = "[CELE97] Waldemar Celes and Jonathan Corson-Rikert. &quot;Act: An Easy-to-use and Dynamically Extensible 3D Graphics Library&quot; in Proceedings, Brazilian Symposium on Computer Graphics and Image Processing, Campos do Jordao, SP -Brazil, October, 1997.";
        String propUri = "http://vivo.library.cornell.edu/ns/0.1#publications";
        String subject = "http://vivo.library.cornell.edu/ns/0.1#individual22972";
        String datatypeUri= null;
        String language = null;

        DataPropertyStatement stmt = new DataPropertyStatementImpl();
        stmt.setIndividualURI(subject);
        stmt.setData(value);
        stmt.setDatapropURI(propUri);
        stmt.setDatatypeURI(datatypeUri);
        stmt.setLanguage(language);

        int hash = RdfLiteralHash.makeRdfLiteralHash( stmt);
        Assert.assertTrue(hash != 0);
        Assert.assertEquals(1646037091 , hash);
    }

    @Test
    public void testMakeRdfLiteralHash() {
        DataPropertyStatement stmt = new DataPropertyStatementImpl();

        stmt.setData(TEST_VALUE);
        stmt.setDatapropURI(TEST_DATA_PROP_URI);
        stmt.setIndividualURI(TEST_INDIVIDUAL_URI);
        int hash = RdfLiteralHash.makeRdfLiteralHash(stmt);
        Assert.assertTrue(hash != 0);

        stmt = new DataPropertyStatementImpl();
        stmt.setData(TEST_VALUE);
        stmt.setDatapropURI(TEST_DATA_PROP_URI);
        stmt.setIndividualURI(TEST_INDIVIDUAL_URI);
        stmt.setDatatypeURI(TEST_DATA_TYPE_URI);
        hash = RdfLiteralHash.makeRdfLiteralHash(stmt);
        Assert.assertTrue(hash != 0);

        stmt = new DataPropertyStatementImpl();
        stmt.setData(TEST_VALUE);
        stmt.setDatapropURI(TEST_DATA_PROP_URI);
        stmt.setIndividualURI(TEST_INDIVIDUAL_URI);
        stmt.setLanguage(TEST_LANG);
        hash = RdfLiteralHash.makeRdfLiteralHash(stmt);
        Assert.assertTrue(hash != 0);
    }

    @Test
    public void testDoesStmtMatchHash() {
        DataPropertyStatement stmtA = new DataPropertyStatementImpl();
        DataPropertyStatement stmtB = new DataPropertyStatementImpl();
        int expectedHash =  0;


        stmtA.setData(TEST_VALUE);
        stmtA.setDatapropURI(TEST_DATA_PROP_URI);
        stmtA.setIndividualURI(TEST_INDIVIDUAL_URI);
        expectedHash = RdfLiteralHash.makeRdfLiteralHash(stmtA);
        stmtB.setData(TEST_VALUE);
        stmtB.setDatapropURI(TEST_DATA_PROP_URI);
        stmtB.setIndividualURI(TEST_INDIVIDUAL_URI);
        Assert.assertTrue(expectedHash == RdfLiteralHash.makeRdfLiteralHash(stmtB) );
        Assert.assertTrue( RdfLiteralHash.doesStmtMatchHash(stmtB, expectedHash));


        stmtA = new DataPropertyStatementImpl();
        stmtA.setData(TEST_VALUE);
        stmtA.setDatapropURI(TEST_DATA_PROP_URI);
        stmtA.setIndividualURI(TEST_INDIVIDUAL_URI);
        stmtA.setDatatypeURI(TEST_DATA_TYPE_URI);
        expectedHash = RdfLiteralHash.makeRdfLiteralHash(stmtA);
        stmtB = new DataPropertyStatementImpl();
        stmtB.setData(TEST_VALUE);
        stmtB.setDatapropURI(TEST_DATA_PROP_URI);
        stmtB.setIndividualURI(TEST_INDIVIDUAL_URI);
        stmtB.setDatatypeURI(TEST_DATA_TYPE_URI);
        Assert.assertTrue( expectedHash == RdfLiteralHash.makeRdfLiteralHash(stmtB) );
        Assert.assertTrue( RdfLiteralHash.doesStmtMatchHash(stmtB, expectedHash));

        stmtA = new DataPropertyStatementImpl();
        stmtA.setData(TEST_VALUE);
        stmtA.setDatapropURI(TEST_DATA_PROP_URI);
        stmtA.setIndividualURI(TEST_INDIVIDUAL_URI);
        stmtA.setLanguage(TEST_LANG);
        expectedHash = RdfLiteralHash.makeRdfLiteralHash(stmtA);
        stmtB = new DataPropertyStatementImpl();
        stmtB.setData(TEST_VALUE);
        stmtB.setDatapropURI(TEST_DATA_PROP_URI);
        stmtB.setIndividualURI(TEST_INDIVIDUAL_URI);
        stmtB.setLanguage(TEST_LANG);
        Assert.assertTrue( expectedHash == RdfLiteralHash.makeRdfLiteralHash(stmtB) );
        Assert.assertTrue( RdfLiteralHash.doesStmtMatchHash(stmtB, expectedHash));

        Assert.assertTrue( ! RdfLiteralHash.doesStmtMatchHash(null, expectedHash) );
    }

//    @Test
//    public void testGetDataPropertyStmtByHash() {
//        DataPropertyStatement stmtA = new DataPropertyStatementImpl();
//        IndividualImpl ind = new IndividualImpl();
//        List<DataPropertyStatement> stmts = new ArrayList<DataPropertyStatement>();
//
//        int expectedHash =  0;
//
//        //test to see if the same subURI, predURI and Value can be distinguished by LANG/datatype
//        stmtA.setData(TEST_VALUE);
//        stmtA.setDatapropURI(TEST_DATA_PROP_URI);
//        stmtA.setIndividualURI(TEST_INDIVIDUAL_URI);
//        stmts.add(stmtA);
//        int expectedHashForSimpleStmt = RdfLiteralHash.makeRdfLiteralHash(stmtA);
//
//        stmtA = new DataPropertyStatementImpl();
//        stmtA.setData(TEST_VALUE );
//        stmtA.setDatapropURI(TEST_DATA_PROP_URI);
//        stmtA.setIndividualURI(TEST_INDIVIDUAL_URI);
//        stmtA.setDatatypeURI(TEST_DATA_TYPE_URI);
//        int expectedHashForDatatypeStmt = RdfLiteralHash.makeRdfLiteralHash(stmtA);
//        stmts.add(stmtA);
//
//        stmtA = new DataPropertyStatementImpl();
//        stmtA.setData(TEST_VALUE );
//        stmtA.setDatapropURI(TEST_DATA_PROP_URI);
//        stmtA.setIndividualURI(TEST_INDIVIDUAL_URI);
//        stmtA.setLanguage(TEST_LANG);
//        int expectedHashForLangStmt = RdfLiteralHash.makeRdfLiteralHash(stmtA);
//        stmts.add(stmtA);
//
//        ind.setDataPropertyStatements(stmts);
//
//        DataPropertyStatement stmt = RdfLiteralHash.getDataPropertyStmtByHash(ind, expectedHashForLangStmt);
//        Assert.assertNotNull(stmt);
//        Assert.assertEquals(TEST_DATA_PROP_URI, stmt.getDatapropURI() );
//        Assert.assertEquals(TEST_INDIVIDUAL_URI, stmt.getIndividualURI() );
//        Assert.assertEquals(TEST_LANG, stmt.getLanguage() );
//        Assert.assertEquals(TEST_VALUE, stmt.getData() );
//        Assert.assertNull(stmt.getDatatypeURI());
//
//        stmt = RdfLiteralHash.getDataPropertyStmtByHash(ind.getURI(), expectedHashForSimpleStmt);
//        Assert.assertNotNull(stmt);
//        Assert.assertEquals(TEST_DATA_PROP_URI, stmt.getDatapropURI() );
//        Assert.assertEquals(TEST_INDIVIDUAL_URI, stmt.getIndividualURI() );
//        Assert.assertEquals(TEST_VALUE, stmt.getData() );
//        Assert.assertNull(stmt.getDatatypeURI());
//        Assert.assertNull(stmt.getLanguage());
//
//        stmt = RdfLiteralHash.getDataPropertyStmtByHash(ind, expectedHashForDatatypeStmt);
//        Assert.assertNotNull(stmt);
//        Assert.assertEquals(TEST_DATA_PROP_URI, stmt.getDatapropURI() );
//        Assert.assertEquals(TEST_INDIVIDUAL_URI, stmt.getIndividualURI() );
//        Assert.assertEquals(TEST_VALUE, stmt.getData() );
//        Assert.assertEquals(TEST_DATA_TYPE_URI, stmt.getDatatypeURI() );
//        Assert.assertNull(stmt.getLanguage());
//
//
//        stmt = RdfLiteralHash.getDataPropertyStmtByHash(ind, 111111);
//        Assert.assertNull(stmt);
//
//    }
    
//    @Test
//    public void testGetRdfsLabelStatementByHash(){
//
//        String n3 =
//            "@prefix ex: <http://example.com/> . \n" +
//            "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"+
//            "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n"+
//            " ex:bob rdfs:label \"Smith, Bob\"^^<"+XSD.xstring.getURI()+"> ." ;
//               
//        Model model = (ModelFactory.createDefaultModel()).read(new StringReader(n3), "", "N3");
//
//        Individual bob = new IndividualImpl();
//        bob.setURI("http://example.com/bob");
//
//        int hash = RdfLiteralHash.makeRdfsLabelLiteralHash(bob, "Smith, Bob", model);
//        DataPropertyStatement stmt = RdfLiteralHash.getRdfsLabelStatementByHash(bob.getURI(), model, hash);
//        
//        String data = stmt.getData();
//        String datatypeUri = stmt.getDatatypeURI();
//        String predicateUri = stmt.getDatapropURI();
//        String subjectUri = stmt.getIndividualURI();
//        
//        Assert.assertEquals("Smith, Bob", data);
//        Assert.assertEquals(XSD.xstring.getURI(), datatypeUri);
//        Assert.assertEquals(VitroVocabulary.LABEL, predicateUri);
//        Assert.assertEquals("http://example.com/bob", subjectUri);
//      
//    }
}
