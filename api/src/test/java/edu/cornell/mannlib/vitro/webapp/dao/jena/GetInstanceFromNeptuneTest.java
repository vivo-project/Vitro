/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.ReasoningOption.ASSERTIONS_AND_INFERENCES;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import edu.cornell.mannlib.vitro.testing.AbstractTestClass;
import org.junit.Assert;
import org.junit.Before;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.XSD;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationImpl;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modules.Application;
import edu.cornell.mannlib.vitro.webapp.startup.StartupManager;
import edu.cornell.mannlib.vitro.webapp.startup.StartupStatus;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.ObjectPropertyTemplateModel;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDaoStub;
import stubs.edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccessFactoryStub;
import stubs.edu.cornell.mannlib.vitro.webapp.modules.ApplicationStub;
import stubs.freemarker.cache.TemplateLoaderStub;
import stubs.javax.servlet.ServletContextStub;
import stubs.javax.servlet.http.HttpServletRequestStub;
import stubs.javax.servlet.http.HttpSessionStub;


public class GetInstanceFromNeptuneTest extends AbstractTestClass {
    private static final Log log = LogFactory.getLog(GetInstanceFromNeptuneTest.class);
    private ServletContextEvent sce;

    private StartupManager sm;
    private StartupStatus ss;
    private static File configDir;
    private ObjectPropertyTemplateModel optm;
    private ObjectProperty op;
    private ObjectPropertyDaoStub opDao;
    private FauxPropertyDaoStub fpDao;
    private ServletContextStub ctx;
    private HttpSessionStub session;
    private HttpServletRequestStub hreq;
    private VitroRequest vreq;
    private IndividualImpl subject;
    private TemplateLoaderStub tl;
    private StringWriter logMessages;
    private ModelAccessFactoryStub mafs;


	String isDependentRelation =
		" <"+VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT+"> \"true\"^^xsd:boolean .\n" ;

	String nosePropIsDependentRel =
	"<"+VitroVocabulary.PROPERTY_STUBOBJECTPROPERTYANNOT+"> rdf:type owl:AnnotationProperty .\n" +
    " ex:hasNose " + isDependentRelation;

    String prefixesN3 =
        "@prefix vitro: <" + VitroVocabulary.vitroURI + "> . \n" +
        "@prefix xsd: <" + XSD.getURI() + "> . \n " +
        "@prefix ex: <http://example.com/> . \n" +
        "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"+
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"+
        "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n";

    private WebappDaoFactoryStub wadf;

//  	@Before
//  	public void setUpWebappDaoFactoryJena() {
//  		super.setUp();
//  		wadf = new WebappDaoFactoryJena(new SimpleOntModelSelector());
//  		ServletContextStub ctx = new ServletContextStub();
//  		new ModelAccessFactoryStub().get(ctx).setWebappDaoFactory(wadf);
//  	}
  	
    void printModels(Model expected, Model result){
    	System.out.println("Expected:");
    	expected.write(System.out);
    	System.out.println("Result:");
    	result.write(System.out);
    }

    void wipeOutModTime(Model model){
        model.removeAll(null, model.createProperty(VitroVocabulary.MODTIME), null);
    }
    @org.junit.Test
    public void testNeptuneDumpTripleStore() {
        logMessages = new StringWriter();

        opDao = new ObjectPropertyDaoStub();
        fpDao = new FauxPropertyDaoStub();
        wadf = new WebappDaoFactoryStub();
        wadf.setObjectPropertyDao(opDao);
        wadf.setFauxPropertyDao(fpDao);

        ctx = new ServletContextStub();
        // create paths for all of the files in the temporary config directory.
        ctx.setRealPaths("/config/", configDir);
        // add a path to match the hard-coded default path.
        ctx.setRealPath("/config/listViewConfig-default.xml",
                ctx.getRealPath("/config/testConfig-default.xml"));

        session = new HttpSessionStub();
        session.setServletContext(ctx);
        hreq = new HttpServletRequestStub();
        hreq.setSession(session);

        vreq = new VitroRequest(hreq);

        mafs = new ModelAccessFactoryStub();
        mafs.get(vreq).setWebappDaoFactory(wadf, ASSERTIONS_AND_INFERENCES);
        mafs.get(vreq).setWebappDaoFactory(wadf, POLICY_NEUTRAL);

        subject = new IndividualImpl();

        // We need a stub TemplateLoader because PropertyListConfig will check
        // to see whether the template name is recognized. How can we get around
        // that? This will do for now.
        tl = new TemplateLoaderStub();
        tl.createTemplate("propStatement-default.ftl", "");    }
    @org.junit.Test
    public void testGetAllPossiblePropInstForIndividual() {
//        String n3 = prefixesN3 +
//            "ex:hasMold a owl:ObjectProperty . \n" +
//            "ex:hasSpore a owl:ObjectProperty . \n" +
//            "ex:hasFungus a owl:ObjectProperty . \n" +
//            "ex:redHerring a owl:ObjectProperty . \n" +
//            "ex:Person a owl:Class . \n" +
//            "ex:Agent a owl:Class . \n" +
//            "ex:Mold a owl:Class . \n" +
//            "ex:Spore a owl:Class . \n" +
//            "ex:Fungus a owl:Class . \n" +
//            "ex:Organism a owl:Class . \n" +
//            "ex:Mold rdfs:subClassOf ex:Organism . \n" +
//            "ex:Spore rdfs:subClassOf ex:Organism . \n" +
//            "ex:Fungus rdfs:subClassOf ex:Organism . \n" +
//            "ex:Person rdfs:subClassOf ex:Agent . \n" +
//            "ex:hasFungus rdfs:range ex:Fungus . \n" +
//            "ex:hasFungus rdfs:domain ex:Agent . \n" +
//            "ex:Agent rdfs:subClassOf [ a owl:Restriction ; \n" +
//                                       "owl:onProperty ex:hasMold ; \n" +
//                                       "owl:allValuesFrom ex:Organism ] . \n" +
//            "ex:Person rdfs:subClassOf [ a owl:Restriction ; \n" +
//                                       "owl:onProperty ex:hasMold ; \n" +
//                                       "owl:allValuesFrom ex:Mold ] . \n" +
//            "ex:Agent rdfs:subClassOf [ a owl:Restriction ; \n" +
//                                       "owl:onProperty ex:hasSpore ; \n" +
//                                       "owl:allValuesFrom ex:Organism ] . \n" +
//            "ex:Person rdfs:subClassOf [ a owl:Restriction ; \n" +
//                                       "owl:onProperty ex:hasSpore ; \n" +
//                                       "owl:someValuesFrom ex:Spore ] . \n" +
//            "ex:bob a ex:Person ; a ex:Agent . \n";
//
//        // The applicable properties for bob should be:
//        // 1. hasMold (values from Mold)
//        // 2. hasSpore (values from Organism)
//        // 3. hasFungus (values from Fungus)
//
//        OntModel ontModel = (ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM));
//        ontModel.read(new StringReader(n3), null, "N3");
//
//        WebappDaoFactory wadf = new WebappDaoFactoryJena(ontModel);
//        log.info(wadf.getObjectPropertyDao().getAllObjectProperties().size());
////      log.info( wadf.getVClassDao().getAllVclasses().size());
//      log.info(wadf.getIndividualDao().getIndividualByURI("http://example.com/bob"));
//
//       Collection<PropertyInstance> pinsts = wadf.getPropertyInstanceDao()
//               .getAllPossiblePropInstForIndividual("http://example.com/bob");
//
//       log.info( pinsts.size());
//
//       Map<String, String> propToRange = new HashMap<String,String>();
//       for (PropertyInstance pi : pinsts) {
//           propToRange.put(pi.getPropertyURI(), pi.getRangeClassURI());
//       }
//
//       log.info( propToRange.get("http://example.com/hasMold"));
//       log.info( propToRange.get("http://example.com/hasSpore"));
//       log.info( propToRange.get("http://example.com/hasFungus"));
//       RDFDataMgr.write(System.out, ontModel, Lang.TURTLE);

    }
}
