package edu.cornell.mannlib.vitro.webapp.servlet.setup;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;

public class RDFFilesLoaderTest extends AbstractTestClass {

    @org.junit.Test
    public void testFirsttimeUpdate() {
        
        // the current state of the firsttime file on the filesystem
        String fileModelRdf = "@prefix rdfs: <" + RDFS.getURI() + "> .\n"
                + "@prefix : <http://example.com/individual/> .\n"
                + ":n1 rdfs:label \"fish 'n' chips\"@en-GB . \n"
                + ":n1 rdfs:label \"fish and fries\"@en-US . \n"                
                + ":n2 rdfs:label \"tube\"@en-GB . \n"
                + ":n2 rdfs:label \"Subway!\"@en-US . \n"
                + ":n2 rdfs:label \"metrou\"@ro-RO . \n";        
        
        // the backup of the previous state of the firsttime file
        String backupModelRdf = "@prefix rdfs: <" + RDFS.getURI() + "> .\n"
                + "@prefix : <http://example.com/individual/> .\n"
                + ":n1 rdfs:label \"fish 'n' chips\"@en-GB . \n"          
                + ":n2 rdfs:label \"tube\"@en-GB . \n"
                + ":n2 rdfs:label \"nooo\"@no-NO . \n";
        
        // the current state of the user-editable model        
        String userModelRdf = "@prefix rdfs: <" + RDFS.getURI() + "> .\n"
                + "@prefix : <http://example.com/individual/> .\n"
                + ":n1 rdfs:label \"fish and chips\"@en-GB . \n"          
                + ":n2 rdfs:label \"tube\"@en-GB . \n"
                + ":n2 rdfs:label \"subway\"@en-US . \n"
                + ":n2 rdfs:label \"nooo\"@no-NO . \n";
        
        // the expected state of the user-editable model after firsttime
        // updates have been applied
        String userModelExpectedRdf = "@prefix rdfs: <" + RDFS.getURI() + "> .\n"
                + "@prefix : <http://example.com/individual/> .\n"
                + ":n1 rdfs:label \"fish and chips\"@en-GB . \n"
                + ":n1 rdfs:label \"fish and fries\"@en-US . \n"                
                + ":n2 rdfs:label \"tube\"@en-GB . \n"
                + ":n2 rdfs:label \"subway\"@en-US . \n"
                + ":n2 rdfs:label \"metrou\"@ro-RO . \n";
        
        Model fileModel = ModelFactory.createDefaultModel();
        fileModel.read(new StringReader(fileModelRdf), null, "N3");
        Model backupModel = ModelFactory.createDefaultModel();
        backupModel.read(new StringReader(backupModelRdf), null, "N3");
        Model userModel = ModelFactory.createDefaultModel();
        userModel.read(new StringReader(userModelRdf), null, "N3");
        Model userModelExpected = ModelFactory.createDefaultModel();
        userModelExpected.read(new StringReader(userModelExpectedRdf), null, "N3");
        
        Model additionsModel = fileModel.difference(backupModel);
        Model retractionsModel = backupModel.difference(fileModel);
        
        RDFFilesLoader.removeChangesThatConflictWithUIEdits(backupModel, userModel, additionsModel);
        RDFFilesLoader.removeChangesThatConflictWithUIEdits(backupModel, userModel, retractionsModel);
        
        userModel.remove(retractionsModel);
        userModel.add(additionsModel);
                
        // For any given triple (S, P, O) changed in the fileModel, it
        // should only be propagated to the user model if the user model
        // doesn't already have a conflicting triple (S, P, X) where O and X 
        // have the same language tag.
        assertTrue("expected: " + userModelExpected + " but was: " + userModel,
                userModelExpected.isIsomorphicWith(userModel));
    }
    
    @org.junit.Test
    public void testFirsttimeUpdateEmptyBackup() {
        
        // the current state of the firsttime file on the filesystem
        String fileModelRdf = "@prefix rdfs: <" + RDFS.getURI() + "> .\n"
                + "@prefix : <http://example.com/individual/> .\n"
                + ":n1 rdfs:label \"fish 'n' chips\"@en-GB . \n"
                + ":n1 rdfs:label \"fish and fries\"@en-US . \n"                
                + ":n2 rdfs:label \"tube\"@en-GB . \n"
                + ":n2 rdfs:label \"Subway!\"@en-US . \n"
                + ":n2 rdfs:label \"metrou\"@ro-RO . \n";       
        
        // the current state of the user-editable model        
        String userModelRdf = "@prefix rdfs: <" + RDFS.getURI() + "> .\n"
                + "@prefix : <http://example.com/individual/> .\n"
                + ":n1 rdfs:label \"fish and chips\"@en-GB . \n"          
                + ":n2 rdfs:label \"tube\"@en-GB . \n"
                + ":n2 rdfs:label \"subway\"@en-US . \n"
                + ":n2 rdfs:label \"nooo\"@no-NO . \n";
        
        // the expected state of the user-editable model after firsttime
        // updates have been applied
        String userModelExpectedRdf = "@prefix rdfs: <" + RDFS.getURI() + "> .\n"
                + "@prefix : <http://example.com/individual/> .\n"
                + ":n1 rdfs:label \"fish and chips\"@en-GB . \n"
                + ":n1 rdfs:label \"fish and fries\"@en-US . \n"                
                + ":n2 rdfs:label \"tube\"@en-GB . \n"
                + ":n2 rdfs:label \"subway\"@en-US . \n"
                + ":n2 rdfs:label \"metrou\"@ro-RO . \n"
                + ":n2 rdfs:label \"nooo\"@no-NO . \n";
        
        Model fileModel = ModelFactory.createDefaultModel();
        fileModel.read(new StringReader(fileModelRdf), null, "N3");
        Model backupModel = ModelFactory.createDefaultModel();
        Model userModel = ModelFactory.createDefaultModel();
        userModel.read(new StringReader(userModelRdf), null, "N3");
        Model userModelExpected = ModelFactory.createDefaultModel();
        userModelExpected.read(new StringReader(userModelExpectedRdf), null, "N3");
        
        Model additionsModel = fileModel.difference(backupModel);
        Model retractionsModel = backupModel.difference(fileModel);
        
        RDFFilesLoader.removeChangesThatConflictWithUIEdits(backupModel, userModel, additionsModel);
        RDFFilesLoader.removeChangesThatConflictWithUIEdits(backupModel, userModel, retractionsModel);
        
        userModel.remove(retractionsModel);
        userModel.add(additionsModel);
        
        // For any given triple (S, P, O) found in the fileModel, it
        // should only be propagated to the user model if the user model
        // doesn't already have a conflicting triple (S, P, X) where O and X 
        // have the same language tag.
        assertTrue("expected: " + userModelExpected + " but was: " + userModel,
                userModelExpected.isIsomorphicWith(userModel));        
    }

}
