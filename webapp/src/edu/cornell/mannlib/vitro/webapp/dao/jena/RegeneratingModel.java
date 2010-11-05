/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.BindingQueryPlan;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.shared.Command;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

public class RegeneratingModel implements OntModel {

    private ModelGenerator generator;
    private OntModel model = null;
    private Model plainVanillaModel;

    public RegeneratingModel(ModelGenerator modelGenerator) {
        this.generator = modelGenerator;
        regenerate();
        this.plainVanillaModel = ModelFactory.createDefaultModel();
    }

    private void regenerate() {
        model = generator.generateModel();
    }


    public void addLoadedImport(String arg0) {
        try {
            model.addLoadedImport(arg0);
        } catch (Exception e) {
            regenerate();
            model.addLoadedImport(arg0);
        }
    }

    public void addSubModel(Model arg0) {
        try {
            model.addSubModel(arg0);
        } catch (Exception e) {
            regenerate();
            model.addSubModel(arg0);
        }
    }

    public void addSubModel(Model arg0, boolean arg1) {
        try {
            model.addSubModel(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            model.addSubModel(arg0, arg1);
        }
    }

    public int countSubModels() {
        try {
            return model.countSubModels();
        } catch (Exception e) {
            regenerate();
            return model.countSubModels();
        }
    }

    public AllDifferent createAllDifferent() {
        try {
            return model.createAllDifferent();
        } catch (Exception e) {
            regenerate();
            return model.createAllDifferent();
        }
    }

    public AllDifferent createAllDifferent(RDFList arg0) {
        try {
            return model.createAllDifferent(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createAllDifferent(arg0);
        }
    }

    public AllValuesFromRestriction createAllValuesFromRestriction(String arg0,
            Property arg1, Resource arg2) {
        try {
            return model.createAllValuesFromRestriction(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.createAllValuesFromRestriction(arg0, arg1, arg2);
        }
    }

    public AnnotationProperty createAnnotationProperty(String arg0) {
        try {
            return model.createAnnotationProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createAnnotationProperty(arg0);
        }
    }

    public CardinalityQRestriction createCardinalityQRestriction(String arg0,
            Property arg1, int arg2, OntClass arg3) {
        try {
            return model.createCardinalityQRestriction(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.createCardinalityQRestriction(arg0, arg1, arg2, arg3);
        }
    }

    public CardinalityRestriction createCardinalityRestriction(String arg0,
            Property arg1, int arg2) {
        try {
            return model.createCardinalityRestriction(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.createCardinalityRestriction(arg0, arg1, arg2);
        }
    }

    public OntClass createClass() {
        try {
            return model.createClass();
        } catch (Exception e) {
            regenerate();
            return model.createClass();
        }
    }

    public OntClass createClass(String arg0) {
        try {
            return model.createClass(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createClass(arg0);
        }
    }

    public ComplementClass createComplementClass(String arg0, Resource arg1) {
        try {
            return model.createComplementClass(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createComplementClass(arg0, arg1);
        }
    }

    public DataRange createDataRange(RDFList arg0) {
        try {
            return model.createDataRange(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createDataRange(arg0);
        }
    }

    public DatatypeProperty createDatatypeProperty(String arg0) {
        try {
            return model.createDatatypeProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createDatatypeProperty(arg0);
        }
    }

    public DatatypeProperty createDatatypeProperty(String arg0, boolean arg1) {
        try {
            return model.createDatatypeProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createDatatypeProperty(arg0, arg1);
        }
    }

    public EnumeratedClass createEnumeratedClass(String arg0, RDFList arg1) {
        try {
            return model.createEnumeratedClass(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createEnumeratedClass(arg0, arg1);
        }
    }

    public HasValueRestriction createHasValueRestriction(String arg0,
            Property arg1, RDFNode arg2) {
        try {
            return model.createHasValueRestriction(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.createHasValueRestriction(arg0, arg1, arg2);
        }
    }

    public Individual createIndividual(Resource arg0) {
        try {
            return model.createIndividual(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createIndividual(arg0);
        }
    }

    public Individual createIndividual(String arg0, Resource arg1) {
        try {
            return model.createIndividual(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createIndividual(arg0, arg1);
        }
    }

    public IntersectionClass createIntersectionClass(String arg0, RDFList arg1) {
        try {
            return model.createIntersectionClass(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createIntersectionClass(arg0, arg1);
        }
    }

    public InverseFunctionalProperty createInverseFunctionalProperty(String arg0) {
        try {
            return model.createInverseFunctionalProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createInverseFunctionalProperty(arg0);
        }
    }

    public InverseFunctionalProperty createInverseFunctionalProperty(
            String arg0, boolean arg1) {
        try {
            return model.createInverseFunctionalProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createInverseFunctionalProperty(arg0, arg1);
        }
    }
    
    public Statement createLiteralStatement(Resource res, Property prop, boolean bool) {
    	try {
    		return model.createLiteralStatement(res, prop, bool);
    	} catch (Exception e) {
    		regenerate();
    		return model.createLiteralStatement(res, prop, bool);
    	}
    }
    
    public Statement createLiteralStatement(Resource res, Property prop, float f) {
    	try {
    		return model.createLiteralStatement(res, prop, f);
    	} catch (Exception e) {
    		regenerate();
    		return model.createLiteralStatement(res, prop, f);
    	}
    }
    
    public Statement createLiteralStatement(Resource res, Property prop, double d) {
    	try {
    		return model.createLiteralStatement(res, prop, d);
    	} catch (Exception e) {
    		regenerate();
    		return model.createLiteralStatement(res, prop, d);
    	}
    }
    
    public Statement createLiteralStatement(Resource res, Property prop, long l) {
    	try {
    		return model.createLiteralStatement(res, prop, l);
    	} catch (Exception e) {
    		regenerate();
    		return model.createLiteralStatement(res, prop, l);
    	}
    }

    public Statement createLiteralStatement(Resource res, Property prop, int i) {
    	try {
    		return model.createLiteralStatement(res, prop, i);
    	} catch (Exception e) {
    		regenerate();
    		return model.createLiteralStatement(res, prop, i);
    	}
    }
    
    public Statement createLiteralStatement(Resource res, Property prop, char c) {
    	try {
    		return model.createLiteralStatement(res, prop, c);
    	} catch (Exception e) {
    		regenerate();
    		return model.createLiteralStatement(res, prop, c);
    	}
    }
    
    public Statement createLiteralStatement(Resource res, Property prop, Object o) {
    	try {
    		return model.createLiteralStatement(res, prop, o);
    	} catch (Exception e) {
    		regenerate();
    		return model.createLiteralStatement(res, prop, o);
    	}
    }

    public StmtIterator listLiteralStatements(Resource res, Property prop, boolean bool) {
    	try {
    		return model.listLiteralStatements(res, prop, bool);
    	} catch (Exception e) {
    		regenerate();
    		return model.listLiteralStatements(res, prop, bool);
    	}
    }
    
    public StmtIterator listLiteralStatements(Resource res, Property prop, double d) {
    	try {
    		return model.listLiteralStatements(res, prop, d);
    	} catch (Exception e) {
    		regenerate();
    		return model.listLiteralStatements(res, prop, d);
    	}
    }
    
    public StmtIterator listLiteralStatements(Resource res, Property prop, long l) {
    	try {
    		return model.listLiteralStatements(res, prop, l);
    	} catch (Exception e) {
    		regenerate();
    		return model.listLiteralStatements(res, prop, l);
    	}
    }

    public StmtIterator listLiteralStatements(Resource res, Property prop, char c) {
    	try {
    		return model.listLiteralStatements(res, prop, c);
    	} catch (Exception e) {
    		regenerate();
    		return model.listLiteralStatements(res, prop, c);
    	}
    }
    
    public MaxCardinalityQRestriction createMaxCardinalityQRestriction(
            String arg0, Property arg1, int arg2, OntClass arg3) {
        try {
            return model.createMaxCardinalityQRestriction(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.createMaxCardinalityQRestriction(arg0, arg1, arg2, arg3);
        }
    }

    public MaxCardinalityRestriction createMaxCardinalityRestriction(
            String arg0, Property arg1, int arg2) {
        try {
            return model.createMaxCardinalityRestriction(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.createMaxCardinalityRestriction(arg0, arg1, arg2);
        }
    }

    public MinCardinalityQRestriction createMinCardinalityQRestriction(
            String arg0, Property arg1, int arg2, OntClass arg3) {
        try {
            return model.createMinCardinalityQRestriction(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.createMinCardinalityQRestriction(arg0, arg1, arg2, arg3);
        }
    }

    public MinCardinalityRestriction createMinCardinalityRestriction(
            String arg0, Property arg1, int arg2) {
        try {
            return model.createMinCardinalityRestriction(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.createMinCardinalityRestriction(arg0, arg1, arg2);
        }
    }

    public ObjectProperty createObjectProperty(String arg0) {
        try {
            return model.createObjectProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createObjectProperty(arg0);
        }
    }

    public ObjectProperty createObjectProperty(String arg0, boolean arg1) {
        try {
            return model.createObjectProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createObjectProperty(arg0, arg1);
        }
    }

    public OntProperty createOntProperty(String arg0) {
        try {
            return model.createOntProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createOntProperty(arg0);
        }
    }

    public OntResource createOntResource(String arg0) {
        try {
            return model.createOntResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createOntResource(arg0);
        }
    }

    public Ontology createOntology(String arg0) {
        try {
            return model.createOntology(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createOntology(arg0);
        }
    }

    public Restriction createRestriction(Property arg0) {
        try {
            return model.createRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createRestriction(arg0);
        }
    }

    public Restriction createRestriction(String arg0, Property arg1) {
        try {
            return model.createRestriction(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createRestriction(arg0, arg1);
        }
    }

    public SomeValuesFromRestriction createSomeValuesFromRestriction(
            String arg0, Property arg1, Resource arg2) {
        try {
            return model.createSomeValuesFromRestriction(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.createSomeValuesFromRestriction(arg0, arg1, arg2);
        }
    }

    public SymmetricProperty createSymmetricProperty(String arg0) {
        try {
            return model.createSymmetricProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createSymmetricProperty(arg0);
        }
    }

    public SymmetricProperty createSymmetricProperty(String arg0, boolean arg1) {
        try {
            return model.createSymmetricProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createSymmetricProperty(arg0, arg1);
        }
    }

    public TransitiveProperty createTransitiveProperty(String arg0) {
        try {
            return model.createTransitiveProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTransitiveProperty(arg0);
        }
    }

    public TransitiveProperty createTransitiveProperty(String arg0, boolean arg1) {
        try {
            return model.createTransitiveProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createTransitiveProperty(arg0, arg1);
        }
    }

    public UnionClass createUnionClass(String arg0, RDFList arg1) {
        try {
            return model.createUnionClass(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createUnionClass(arg0, arg1);
        }
    }

    public AllValuesFromRestriction getAllValuesFromRestriction(String arg0) {
        try {
            return model.getAllValuesFromRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getAllValuesFromRestriction(arg0);
        }
    }

    public AnnotationProperty getAnnotationProperty(String arg0) {
        try {
            return model.getAnnotationProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getAnnotationProperty(arg0);
        }
    }

    public Model getBaseModel() {
        try {
            return model.getBaseModel();
        } catch (Exception e) {
            regenerate();
            return model.getBaseModel();
        }
    }

    public CardinalityQRestriction getCardinalityQRestriction(String arg0) {
        try {
            return model.getCardinalityQRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getCardinalityQRestriction(arg0);
        }
    }

    public CardinalityRestriction getCardinalityRestriction(String arg0) {
        try {
            return model.getCardinalityRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getCardinalityRestriction(arg0);
        }
    }

    public ComplementClass getComplementClass(String arg0) {
        try {
            return model.getComplementClass(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getComplementClass(arg0);
        }
    }

    public DatatypeProperty getDatatypeProperty(String arg0) {
        try {
            return model.getDatatypeProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getDatatypeProperty(arg0);
        }
    }

    public OntDocumentManager getDocumentManager() {
        try {
            return model.getDocumentManager();
        } catch (Exception e) {
            regenerate();
            return model.getDocumentManager();
        }
    }

    public boolean getDynamicImports() {
        try {
            return model.getDynamicImports();
        } catch (Exception e) {
            regenerate();
            return model.getDynamicImports();
        }
    }

    public EnumeratedClass getEnumeratedClass(String arg0) {
        try {
            return model.getEnumeratedClass(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getEnumeratedClass(arg0);
        }
    }

    public HasValueRestriction getHasValueRestriction(String arg0) {
        try {
            return model.getHasValueRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getHasValueRestriction(arg0);
        }
    }

    public ModelMaker getImportModelMaker() {
        try {
            return model.getImportModelMaker();
        } catch (Exception e) {
            regenerate();
            return model.getImportModelMaker();
        }
    }

    public OntModel getImportedModel(String arg0) {
        try {
            return model.getImportedModel(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getImportedModel(arg0);
        }
    }

    public Individual getIndividual(String arg0) {
        try {
            return model.getIndividual(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getIndividual(arg0);
        }
    }

    public IntersectionClass getIntersectionClass(String arg0) {
        try {
            return model.getIntersectionClass(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getIntersectionClass(arg0);
        }
    }

    public InverseFunctionalProperty getInverseFunctionalProperty(String arg0) {
        try {
            return model.getInverseFunctionalProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getInverseFunctionalProperty(arg0);
        }
    }

    public MaxCardinalityQRestriction getMaxCardinalityQRestriction(String arg0) {
        try {
            return model.getMaxCardinalityQRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getMaxCardinalityQRestriction(arg0);
        }
    }

    public MaxCardinalityRestriction getMaxCardinalityRestriction(String arg0) {
        try {
            return model.getMaxCardinalityRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getMaxCardinalityRestriction(arg0);
        }
    }

    public MinCardinalityQRestriction getMinCardinalityQRestriction(String arg0) {
        try {
            return model.getMinCardinalityQRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getMinCardinalityQRestriction(arg0);
        }
    }

    public MinCardinalityRestriction getMinCardinalityRestriction(String arg0) {
        try {
            return model.getMinCardinalityRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getMinCardinalityRestriction(arg0);
        }
    }

    /**
     * @deprecated
     */
    public ModelMaker getModelMaker() {
        try {
            return model.getModelMaker();
        } catch (Exception e) {
            regenerate();
            return model.getModelMaker();
        }
    }

    public ObjectProperty getObjectProperty(String arg0) {
        try {
            return model.getObjectProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getObjectProperty(arg0);
        }
    }

    public OntClass getOntClass(String arg0) {
        try {
            return model.getOntClass(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getOntClass(arg0);
        }
    }

    public OntProperty getOntProperty(String arg0) {
        try {
            return model.getOntProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getOntProperty(arg0);
        }
    }

    public OntResource getOntResource(String arg0) {
        try {
            return model.getOntResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getOntResource(arg0);
        }
    }

    public OntResource getOntResource(Resource arg0) {
        try {
            return model.getOntResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getOntResource(arg0);
        }
    }

    public Ontology getOntology(String arg0) {
        try {
            return model.getOntology(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getOntology(arg0);
        }
    }

    public Profile getProfile() {
        try {
            return model.getProfile();
        } catch (Exception e) {
            regenerate();
            return model.getProfile();
        }
    }

    public QualifiedRestriction getQualifiedRestriction(String arg0) {
        try {
            return model.getQualifiedRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getQualifiedRestriction(arg0);
        }
    }

    public Restriction getRestriction(String arg0) {
        try {
            return model.getRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getRestriction(arg0);
        }
    }

    public SomeValuesFromRestriction getSomeValuesFromRestriction(String arg0) {
        try {
            return model.getSomeValuesFromRestriction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getSomeValuesFromRestriction(arg0);
        }
    }

    public OntModelSpec getSpecification() {
        try {
            return model.getSpecification();
        } catch (Exception e) {
            regenerate();
            return model.getSpecification();
        }
    }

    public List getSubGraphs() {
        try {
            return model.getSubGraphs();
        } catch (Exception e) {
            regenerate();
            return model.getSubGraphs();
        }
    }

    public SymmetricProperty getSymmetricProperty(String arg0) {
        try {
            return model.getSymmetricProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getSymmetricProperty(arg0);
        }
    }

    public TransitiveProperty getTransitiveProperty(String arg0) {
        try {
            return model.getTransitiveProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getTransitiveProperty(arg0);
        }
    }

    public UnionClass getUnionClass(String arg0) {
        try {
            return model.getUnionClass(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getUnionClass(arg0);
        }
    }

    public boolean hasLoadedImport(String arg0) {
        try {
            return model.hasLoadedImport(arg0);
        } catch (Exception e) {
            regenerate();
            return model.hasLoadedImport(arg0);
        }
    }

    public boolean isInBaseModel(RDFNode arg0) {
        try {
            return model.isInBaseModel(arg0);
        } catch (Exception e) {
            regenerate();
            return model.isInBaseModel(arg0);
        }
    }

    public boolean isInBaseModel(Statement arg0) {
        try {
            return model.isInBaseModel(arg0);
        } catch (Exception e) {
            regenerate();
            return model.isInBaseModel(arg0);
        }
    }

    public ExtendedIterator listAllDifferent() {
        try {
            return model.listAllDifferent();
        } catch (Exception e) {
            regenerate();
            return model.listAllDifferent();
        }
    }

    public ExtendedIterator listAnnotationProperties() {
        try {
            return model.listAnnotationProperties();
        } catch (Exception e) {
            regenerate();
            return model.listAnnotationProperties();
        }
    }

    public ExtendedIterator listClasses() {
        try {
            return model.listClasses();
        } catch (Exception e) {
            regenerate();
            return model.listClasses();
        }
    }

    public ExtendedIterator listComplementClasses() {
        try {
            return model.listComplementClasses();
        } catch (Exception e) {
            regenerate();
            return model.listComplementClasses();
        }
    }

    public ExtendedIterator listDataRanges() {
        try {
            return model.listDataRanges();
        } catch (Exception e) {
            regenerate();
            return model.listDataRanges();
        }
    }

    public ExtendedIterator listDatatypeProperties() {
        try {
            return model.listDatatypeProperties();
        } catch (Exception e) {
            regenerate();
            return model.listDatatypeProperties();
        }
    }

    public ExtendedIterator listEnumeratedClasses() {
        try {
            return model.listEnumeratedClasses();
        } catch (Exception e) {
            regenerate();
            return model.listEnumeratedClasses();
        }
    }

    public ExtendedIterator listFunctionalProperties() {
        try {
            return model.listFunctionalProperties();
        } catch (Exception e) {
            regenerate();
            return model.listFunctionalProperties();
        }
    }

    public ExtendedIterator listHierarchyRootClasses() {
        try {
            return model.listHierarchyRootClasses();
        } catch (Exception e) {
            regenerate();
            return model.listHierarchyRootClasses();
        }
    }

    /**
     * @deprecated
     */
    public ExtendedIterator listImportedModels() {
        try {
            return model.listImportedModels();
        } catch (Exception e) {
            regenerate();
            return model.listImportedModels();
        }
    }

    public Set listImportedOntologyURIs() {
        try {
            return model.listImportedOntologyURIs();
        } catch (Exception e) {
            regenerate();
            return model.listImportedOntologyURIs();
        }
    }

    public Set listImportedOntologyURIs(boolean arg0) {
        try {
            return model.listImportedOntologyURIs(arg0);
        } catch (Exception e) {
            regenerate();
            return model.listImportedOntologyURIs(arg0);
        }
    }

    public ExtendedIterator listIndividuals() {
        try {
            return model.listIndividuals();
        } catch (Exception e) {
            regenerate();
            return model.listIndividuals();
        }
    }

    public ExtendedIterator listIndividuals(Resource arg0) {
        try {
            return model.listIndividuals();
        } catch (Exception e) {
            regenerate();
            return model.listIndividuals();
        }
    }

    public ExtendedIterator listIntersectionClasses() {
        try {
            return model.listIntersectionClasses();
        } catch (Exception e) {
            regenerate();
            return model.listIntersectionClasses();
        }
    }

    public ExtendedIterator listInverseFunctionalProperties() {
        try {
            return model.listInverseFunctionalProperties();
        } catch (Exception e) {
            regenerate();
            return model.listInverseFunctionalProperties();
        }
    }

    public ExtendedIterator listNamedClasses() {
        try {
            return model.listNamedClasses();
        } catch (Exception e) {
            regenerate();
            return model.listNamedClasses();
        }
    }

    public ExtendedIterator listObjectProperties() {
        try {
            return model.listObjectProperties();
        } catch (Exception e) {
            regenerate();
            return model.listObjectProperties();
        }
    }

    public ExtendedIterator listOntProperties() {
        try {
            return model.listOntProperties();
        } catch (Exception e) {
            regenerate();
            return model.listOntProperties();
        }
    }

    public ExtendedIterator listOntologies() {
        try {
            return model.listOntologies();
        } catch (Exception e) {
            regenerate();
            return model.listOntologies();
        }
    }

    public ExtendedIterator listRestrictions() {
        try {
            return model.listRestrictions();
        } catch (Exception e) {
            regenerate();
            return model.listRestrictions();
        }
    }

    public ExtendedIterator listSubModels() {
        try {
            return model.listSubModels();
        } catch (Exception e) {
            regenerate();
            return model.listSubModels();
        }
    }

    public ExtendedIterator listSubModels(boolean arg0) {
        try {
            return model.listSubModels(arg0);
        } catch (Exception e) {
            regenerate();
            return model.listSubModels(arg0);
        }
    }

    public ExtendedIterator listSymmetricProperties() {
        try {
            return model.listSymmetricProperties();
        } catch (Exception e) {
            regenerate();
            return model.listSymmetricProperties();
        }
    }

    public ExtendedIterator listTransitiveProperties() {
        try {
            return model.listTransitiveProperties();
        } catch (Exception e) {
            regenerate();
            return model.listTransitiveProperties();
        }
    }

    public ExtendedIterator listUnionClasses() {
        try {
            return model.listUnionClasses();
        } catch (Exception e) {
            regenerate();
            return model.listUnionClasses();
        }
    }

    public void loadImports() {
        try {
            model.loadImports();
        } catch (Exception e) {
            regenerate();
            model.loadImports();
        }
    }

    public void removeLoadedImport(String arg0) {
        try {
            model.removeLoadedImport(arg0);
        } catch (Exception e) {
            regenerate();
            model.removeLoadedImport(arg0);
        }
    }

    public void removeSubModel(Model arg0) {
        try {
            model.removeSubModel(arg0);
        } catch (Exception e) {
            regenerate();
            model.removeSubModel(arg0);
        }
    }

    public void removeSubModel(Model arg0, boolean arg1) {
        try {
            model.removeSubModel(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            model.removeSubModel(arg0, arg1);
        }
    }

    public void setDynamicImports(boolean arg0) {
        try {
            model.setDynamicImports(arg0);
        } catch (Exception e) {
            regenerate();
            model.setDynamicImports(arg0);
        }
    }

    public void setStrictMode(boolean arg0) {
        try {
            model.setStrictMode(arg0);
        } catch (Exception e) {
            regenerate();
            model.setStrictMode(arg0);
        }
    }

    public boolean strictMode() {
        try {
            return model.strictMode();
        } catch (Exception e) {
            regenerate();
            return model.strictMode();
        }
    }

    public Model writeAll(Writer arg0, String arg1, String arg2) {
        try {
            return model.writeAll(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.writeAll(arg0, arg1, arg2);
        }
    }

    public Model writeAll(OutputStream arg0, String arg1, String arg2) {
        try {
            return model.writeAll(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.writeAll(arg0, arg1, arg2);
        }
    }

    public Model getDeductionsModel() {
        try {
            return model.getDeductionsModel();
        } catch (Exception e) {
            regenerate();
            return model.getDeductionsModel();
        }
    }

    public Iterator getDerivation(Statement arg0) {
        try {
            return model.getDerivation(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getDerivation(arg0);
        }
    }

    public Model getRawModel() {
        try {
            return model.getRawModel();
        } catch (Exception e) {
            regenerate();
            return model.getRawModel();
        }
    }

    public Reasoner getReasoner() {
        try {
            return model.getReasoner();
        } catch (Exception e) {
            regenerate();
            return model.getReasoner();
        }
    }

    public StmtIterator listStatements(Resource arg0, Property arg1,
            RDFNode arg2, Model arg3) {
        try {
            return model.listStatements(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.listStatements(arg0, arg1, arg2, arg3);
        }   }

    public void prepare() {
        try {
            model.prepare();
        } catch (Exception e) {
            regenerate();
            model.prepare();
        }
    }

    public void rebind() {
        try {
            model.rebind();
        } catch (Exception e) {
            regenerate();
            model.rebind();
        }
    }

    public void reset() {
        try {
            model.reset();
        } catch (Exception e) {
            regenerate();
            model.reset();
        }
    }

    public void setDerivationLogging(boolean arg0) {
        try {
            model.setDerivationLogging(arg0);
        } catch (Exception e) {
            regenerate();
            model.setDerivationLogging(arg0);
        }
    }

    public ValidityReport validate() {
        try {
            return model.validate();
        } catch (Exception e) {
            regenerate();
            return model.validate();
        }
    }

    public Model abort() {
        try {
            return model.abort();
        } catch (Exception e) {
            regenerate();
            return model.abort();
        }
    }

    public Model add(Statement arg0) {
        try {
            return model.add(arg0);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0);
        }
    }

    public Model add(Statement[] arg0) {
        try {
            return model.add(arg0);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0);
        }
    }

    public Model add(StmtIterator arg0) {
        try {
            return model.add(arg0);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0);
        }
    }

    public Model add(Model arg0) {
        try {
            return model.add(arg0);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0);
        }
    }

    public Model add(Model arg0, boolean arg1) {
        try {
            return model.add(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0, arg1);
        }
    }

    public Model begin() {
        try {
            return model.begin();
        } catch (Exception e) {
            regenerate();
            return model.begin();
        }
    }

    public void close() {
        try {
            model.close();
        } catch (Exception e) {
            regenerate();
            model.close();
        }
    }

    public Model commit() {
        try {
            return model.commit();
        } catch (Exception e) {
            regenerate();
            return model.commit();
        }
    }

    public boolean contains(Statement arg0) {
        try {
            return model.contains(arg0);
        } catch (Exception e) {
            regenerate();
            return model.contains(arg0);
        }
    }

    public boolean contains(Resource arg0, Property arg1) {
        try {
            return model.contains(arg0,arg1);
        } catch (Exception e) {
            regenerate();
            return model.contains(arg0,arg1);
        }
    }

    public boolean contains(Resource arg0, Property arg1, RDFNode arg2) {
        try {
            return model.contains(arg0,arg1);
        } catch (Exception e) {
            regenerate();
            return model.contains(arg0,arg1);
        }
    }

    public boolean containsAll(StmtIterator arg0) {
        try {
            return model.containsAll(arg0);
        } catch (Exception e) {
            regenerate();
            return model.containsAll(arg0);
        }   }

    public boolean containsAll(Model arg0) {
        try {
            return model.containsAll(arg0);
        } catch (Exception e) {
            regenerate();
            return model.containsAll(arg0);
        }
    }

    public boolean containsAny(StmtIterator arg0) {
        try {
            return model.containsAny(arg0);
        } catch (Exception e) {
            regenerate();
            return model.containsAny(arg0);
        }
    }

    public boolean containsAny(Model arg0) {
        try {
            return model.containsAny(arg0);
        } catch (Exception e) {
            regenerate();
            return model.containsAny(arg0);
        }
    }

    public boolean containsResource(RDFNode arg0) {
        try {
            return model.containsResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.containsResource(arg0);
        }
    }

    public RDFList createList() {
        try {
            return model.createList();
        } catch (Exception e) {
            regenerate();
            return model.createList();
        }
    }

    public RDFList createList(RDFNode[] arg0) {
        try {
            return model.createList(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createList(arg0);
        }
    }

    public Literal createLiteral(String arg0, String arg1) {
        try {
            return model.createLiteral(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createLiteral(arg0, arg1);
        }
    }

    public Literal createLiteral(String arg0, boolean arg1) {
        try {
            return model.createLiteral(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createLiteral(arg0, arg1);
        }
    }

    public Property createProperty(String arg0, String arg1) {
        try {
            return model.createProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createProperty(arg0, arg1);
        }
    }

    public ReifiedStatement createReifiedStatement(Statement arg0) {
        try {
            return model.createReifiedStatement(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createReifiedStatement(arg0);
        }
    }

    public ReifiedStatement createReifiedStatement(String arg0, Statement arg1) {
        try {
            return model.createReifiedStatement(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createReifiedStatement(arg0, arg1);
        }
    }

    public Resource createResource() {
        try {
            return model.createResource();
        } catch (Exception e) {
            regenerate();
            return model.createResource();
        }
    }

    public Resource createResource(AnonId arg0) {
        try {
            return model.createResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createResource(arg0);
        }
    }

    public Resource createResource(String arg0) {
        try {
            return model.createResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createResource(arg0);
        }
    }

    public Statement createStatement(Resource arg0, Property arg1, RDFNode arg2) {
        try {
            return model.createStatement(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.createStatement(arg0, arg1, arg2);
        }
    }

    public Literal createTypedLiteral(Object arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(String arg0, RDFDatatype arg1) {
        try {
            return model.createTypedLiteral(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0, arg1);
        }
    }

    public Literal createTypedLiteral(Object arg0, RDFDatatype arg1) {
        try {
            return model.createTypedLiteral(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0, arg1);
        }
    }

    public Model difference(Model arg0) {
        try {
            return model.difference(arg0);
        } catch (Exception e) {
            regenerate();
            return model.difference(arg0);
        }
    }

    public Object executeInTransaction(Command arg0) {
        try {
            return model.executeInTransaction(arg0);
        } catch (Exception e) {
            regenerate();
            return model.executeInTransaction(arg0);
        }
    }

    public Resource getAnyReifiedStatement(Statement arg0) {
        try {
            return model.getAnyReifiedStatement(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getAnyReifiedStatement(arg0);
        }
    }

    public Lock getLock() {
        return plainVanillaModel.getLock();
    }

    public Property getProperty(String arg0, String arg1) {
        try {
            return model.getProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getProperty(arg0);
        }
    }

    public Statement getProperty(Resource arg0, Property arg1) {
        try {
            return model.getProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.getProperty(arg0, arg1);
        }
    }

    public ReificationStyle getReificationStyle() {
        try {
            return model.getReificationStyle();
        } catch (Exception e) {
            regenerate();
            return model.getReificationStyle();
        }
    }

    public Statement getRequiredProperty(Resource arg0, Property arg1) {
        try {
            return model.getRequiredProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.getRequiredProperty(arg0, arg1);
        }
    }

    public Resource getResource(String arg0) {
        try {
            return model.getResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getResource(arg0);
        }
    }

    public boolean independent() {
        try {
            return model.independent();
        } catch (Exception e) {
            regenerate();
            return model.independent();
        }
    }

    public Model intersection(Model arg0) {
        try {
            return model.intersection(arg0);
        } catch (Exception e) {
            regenerate();
            return model.intersection(arg0);
        }
    }

    public boolean isClosed() {
        try {
            return model.isClosed();
        } catch (Exception e) {
            regenerate();
            return model.isClosed();
        }
    }

    public boolean isEmpty() {
        try {
            return model.isEmpty();
        } catch (Exception e) {
            regenerate();
            return model.isEmpty();
        }
    }

    public boolean isIsomorphicWith(Model arg0) {
        try {
            return model.isIsomorphicWith(arg0);
        } catch (Exception e) {
            regenerate();
            return model.isIsomorphicWith(arg0);
        }
    }

    public boolean isReified(Statement arg0) {
        try {
            return model.isReified(arg0);
        } catch (Exception e) {
            regenerate();
            return model.isReified(arg0);
        }
    }

    public NsIterator listNameSpaces() {
        try {
            return model.listNameSpaces();
        } catch (Exception e) {
            regenerate();
            return model.listNameSpaces();
        }
    }

    public NodeIterator listObjects() {
        try {
            return model.listObjects();
        } catch (Exception e) {
            regenerate();
            return model.listObjects();
        }
    }

    public NodeIterator listObjectsOfProperty(Property arg0) {
        try {
            return model.listObjectsOfProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.listObjectsOfProperty(arg0);
        }
    }

    public NodeIterator listObjectsOfProperty(Resource arg0, Property arg1) {
        try {
            return model.listObjectsOfProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.listObjectsOfProperty(arg0, arg1);
        }
    }

    public RSIterator listReifiedStatements() {
        try {
            return model.listReifiedStatements();
        } catch (Exception e) {
            regenerate();
            return model.listReifiedStatements();
        }
    }

    public RSIterator listReifiedStatements(Statement arg0) {
        try {
            return model.listReifiedStatements(arg0);
        } catch (Exception e) {
            regenerate();
            return model.listReifiedStatements(arg0);
        }
    }

    public StmtIterator listStatements() {
        try {
            return model.listStatements();
        } catch (Exception e) {
            regenerate();
            return model.listStatements();
        }
    }

    public StmtIterator listStatements(Selector arg0) {
        try {
            return model.listStatements(arg0);
        } catch (Exception e) {
            regenerate();
            return model.listStatements(arg0);
        }
    }

    public StmtIterator listStatements(Resource arg0, Property arg1,
            RDFNode arg2) {
        try {
            return model.listStatements(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.listStatements(arg0, arg1, arg2);
        }
    }

    public ResIterator listSubjects() {
        try {
            return model.listSubjects();
        } catch (Exception e) {
            regenerate();
            return model.listSubjects();
        }
    }

    public ResIterator listSubjectsWithProperty(Property arg0) {
        try {
            return model.listSubjectsWithProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.listSubjectsWithProperty(arg0);
        }
    }

    public ResIterator listSubjectsWithProperty(Property arg0, RDFNode arg1) {
        try {
            return model.listSubjectsWithProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.listSubjectsWithProperty(arg0, arg1);
        }
    }

    public Model notifyEvent(Object arg0) {
        try {
            return model.notifyEvent(arg0);
        } catch (Exception e) {
            regenerate();
            return model.notifyEvent(arg0);
        }
    }

    public Model query(Selector arg0) {
        try {
            return model.query(arg0);
        } catch (Exception e) {
            regenerate();
            return model.query(arg0);
        }
    }

    public Model read(String arg0) {
        try {
            return model.read(arg0);
        } catch (Exception e) {
            regenerate();
            return model.read(arg0);
        }
    }

    public Model read(InputStream arg0, String arg1) {
        try {
            return model.read(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.read(arg0, arg1);
        }
    }

    public Model read(Reader arg0, String arg1) {
        try {
            return model.read(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.read(arg0, arg1);
        }
    }

    public Model read(String arg0, String arg1) {
        try {
            return model.read(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.read(arg0, arg1);
        }
    }

    public Model read(InputStream arg0, String arg1, String arg2) {
        try {
            return model.read(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.read(arg0, arg1, arg2);
        }
    }

    public Model read(Reader arg0, String arg1, String arg2) {
        try {
            return model.read(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.read(arg0, arg1, arg2);
        }
    }

    public Model read(String arg0, String arg1, String arg2) {
        try {
            return model.read(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.read(arg0, arg1, arg2);
        }
    }

    public Model register(ModelChangedListener arg0) {
        try {
            return model.register(arg0);
        } catch (Exception e) {
            regenerate();
            return model.register(arg0);
        }
    }

    public Model remove(Statement[] arg0) {
        try {
            return model.remove(arg0);
        } catch (Exception e) {
            regenerate();
            return model.remove(arg0);
        }
    }

    public Model remove(Statement arg0) {
        try {
            return model.remove(arg0);
        } catch (Exception e) {
            regenerate();
            return model.remove(arg0);
        }
    }

    public Model removeAll() {
        try {
            return model.removeAll();
        } catch (Exception e) {
            regenerate();
            return model.removeAll();
        }
    }

    public Model removeAll(Resource arg0, Property arg1, RDFNode arg2) {
        try {
            return model.removeAll(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.removeAll(arg0, arg1, arg2);
        }
    }

    public void removeAllReifications(Statement arg0) {
        try {
            model.removeAllReifications(arg0);
        } catch (Exception e) {
            regenerate();
            model.removeAllReifications(arg0);
        }

    }

    public void removeReification(ReifiedStatement arg0) {
        try {
            model.removeReification(arg0);
        } catch (Exception e) {
            regenerate();
            model.removeReification(arg0);
        }
    }

    public long size() {
        try {
            return model.size();
        } catch (Exception e) {
            regenerate();
            return model.size();
        }
    }

    public boolean supportsSetOperations() {
        try {
            return model.supportsSetOperations();
        } catch (Exception e) {
            regenerate();
            return model.supportsSetOperations();
        }
    }

    public boolean supportsTransactions() {
        try {
            return model.supportsTransactions();
        } catch (Exception e) {
            regenerate();
            return model.supportsTransactions();
        }
    }

    public Model union(Model arg0) {
        try {
            return model.union(arg0);
        } catch (Exception e) {
            regenerate();
            return model.union(arg0);
        }
    }

    public Model unregister(ModelChangedListener arg0) {
        try {
            return model.unregister(arg0);
        } catch (Exception e) {
            regenerate();
            return model.unregister(arg0);
        }
    }

    public Model write(Writer arg0) {
        try {
            return model.write(arg0);
        } catch (Exception e) {
            regenerate();
            return model.write(arg0);
        }
    }

    public Model write(OutputStream arg0) {
        try {
            return model.write(arg0);
        } catch (Exception e) {
            regenerate();
            return model.write(arg0);
        }
    }

    public Model write(Writer arg0, String arg1) {
        try {
            return model.write(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.write(arg0, arg1);
        }
    }

    public Model write(OutputStream arg0, String arg1) {
        try {
            return model.write(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.write(arg0, arg1);
        }
    }

    public Model write(Writer arg0, String arg1, String arg2) {
        try {
            return model.write(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.write(arg0, arg1, arg2);
        }
    }

    public Model write(OutputStream arg0, String arg1, String arg2) {
        try {
            return model.write(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.write(arg0, arg1, arg2);
        }
    }

    public Model add(Resource arg0, Property arg1, RDFNode arg2) {
        try {
            return model.add(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0, arg1, arg2);
        }
    }

    public Model add(Resource arg0, Property arg1, String arg2) {
        try {
            return model.add(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0, arg1, arg2);
        }
    }

    public Model add(Resource arg0, Property arg1, String arg2, RDFDatatype arg3) {
        try {
            return model.add(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0, arg1, arg2, arg3);
        }
    }

    public Model add(Resource arg0, Property arg1, String arg2, boolean arg3) {
        try {
            return model.add(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0, arg1, arg2, arg3);
        }
    }

    public Model add(Resource arg0, Property arg1, String arg2, String arg3) {
        try {
            return model.add(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.add(arg0, arg1, arg2, arg3);
        }
    }

    public boolean contains(Resource arg0, Property arg1, String arg2) {
        try {
            return model.contains(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.contains(arg0, arg1, arg2);
        }
    }

    public boolean contains(Resource arg0, Property arg1, String arg2,
            String arg3) {
        try {
            return model.contains(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.contains(arg0, arg1, arg2, arg3);
        }
    }

    public Alt createAlt() {
        try {
            return model.createAlt();
        } catch (Exception e) {
            regenerate();
            return model.createAlt();
        }
    }

    public Alt createAlt(String arg0) {
        try {
            return model.createAlt(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createAlt(arg0);
        }
    }

    public Bag createBag() {
        try {
            return model.createBag();
        } catch (Exception e) {
            regenerate();
            return model.createBag();
        }
    }

    public Bag createBag(String arg0) {
        try {
            return model.createBag();
        } catch (Exception e) {
            regenerate();
            return model.createBag();
        }
    }

    /**
     * @deprecated
     */
    public Literal createLiteral(String arg0) {
        try {
            return model.createLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createLiteral(arg0);
        }
    }

    public Property createProperty(String arg0) {
        try {
            return model.createProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createProperty(arg0);
        }
    }

    public Resource createResource(Resource arg0) {
        try {
            return model.createResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createResource(arg0);
        }
    }

    @Deprecated
    public Resource createResource(ResourceF arg0) {
        try {
            return model.createResource(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createResource(arg0);
        }
    }

    public Resource createResource(String arg0, Resource arg1) {
        try {
            return model.createResource(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createResource(arg0, arg1);
        }
    }

    @Deprecated
    public Resource createResource(String arg0, ResourceF arg1) {
        try {
            return model.createResource(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.createResource(arg0, arg1);
        }
    }

    public Seq createSeq() {
        try {
            return model.createSeq();
        } catch (Exception e) {
            regenerate();
            return model.createSeq();
        }
    }

    public Seq createSeq(String arg0) {
        try {
            return model.createSeq(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createSeq(arg0);
        }
    }

    public Statement createStatement(Resource arg0, Property arg1, String arg2) {
        try {
            return model.createStatement(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.createStatement(arg0, arg1, arg2);
        }
    }

    public Statement createStatement(Resource arg0, Property arg1, String arg2,
            String arg3) {
        try {
            return model.createStatement(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.createStatement(arg0, arg1, arg2, arg3);
        }
    }

    public Statement createStatement(Resource arg0, Property arg1, String arg2,
            boolean arg3) {
        try {
            return model.createStatement(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.createStatement(arg0, arg1, arg2, arg3);
        }
    }

    public Statement createStatement(Resource arg0, Property arg1, String arg2,
            String arg3, boolean arg4) {
        try {
            return model.createStatement(arg0, arg1, arg2, arg3, arg4);
        } catch (Exception e) {
            regenerate();
            return model.createStatement(arg0, arg1, arg2, arg3, arg4);
        }
    }

    public Literal createTypedLiteral(boolean arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(int arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(long arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(Calendar arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(char arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(float arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(double arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(String arg0) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(String arg0, String arg1) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Literal createTypedLiteral(Object arg0, String arg1) {
        try {
            return model.createTypedLiteral(arg0);
        } catch (Exception e) {
            regenerate();
            return model.createTypedLiteral(arg0);
        }
    }

    public Alt getAlt(String arg0) {
        try {
            return model.getAlt(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getAlt(arg0);
        }
    }

    public Alt getAlt(Resource arg0) {
        try {
            return model.getAlt(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getAlt(arg0);
        }
    }

    public Bag getBag(String arg0) {
        try {
            return model.getBag(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getBag(arg0);
        }
    }

    public Bag getBag(Resource arg0) {
        try {
            return model.getBag(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getBag(arg0);
        }
    }

    public Property getProperty(String arg0) {
        try {
            return model.getProperty(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getProperty(arg0);
        }
    }

    public RDFNode getRDFNode(Node arg0) {
        try {
            return model.getRDFNode(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getRDFNode(arg0);
        }
    }

    @Deprecated
    public Resource getResource(String arg0, ResourceF arg1) {
        try {
            return model.getResource(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.getResource(arg0, arg1);
        }
    }

    public Seq getSeq(String arg0) {
        try {
            return model.getSeq(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getSeq(arg0);
        }
    }

    public Seq getSeq(Resource arg0) {
        try {
            return model.getSeq(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getSeq(arg0);
        }
    }

    public StmtIterator listStatements(Resource arg0, Property arg1, String arg2) {
        try {
            return model.listStatements(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.listStatements(arg0, arg1, arg2);
        }
    }

    public StmtIterator listStatements(Resource arg0, Property arg1,
            String arg2, String arg3) {
        try {
            return model.listStatements(arg0, arg1, arg2, arg3);
        } catch (Exception e) {
            regenerate();
            return model.listStatements(arg0, arg1, arg2, arg3);
        }
    }

    public ResIterator listSubjectsWithProperty(Property arg0, String arg1) {
        try {
            return model.listSubjectsWithProperty(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.listSubjectsWithProperty(arg0, arg1);
        }
    }

    public ResIterator listSubjectsWithProperty(Property arg0, String arg1,
            String arg2) {
        try {
            return model.listSubjectsWithProperty(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.listSubjectsWithProperty(arg0, arg1, arg2);
        }
    }

    public Model remove(StmtIterator arg0) {
        try {
            return model.remove(arg0);
        } catch (Exception e) {
            regenerate();
            return model.remove(arg0);
        }
    }

    public Model remove(Model arg0) {
        try {
            return model.remove(arg0);
        } catch (Exception e) {
            regenerate();
            return model.remove(arg0);
        }
    }

    public Model remove(Model arg0, boolean arg1) {
        try {
            return model.remove(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.remove(arg0, arg1);
        }
    }

    public Model remove(Resource arg0, Property arg1, RDFNode arg2) {
        try {
            return model.remove(arg0, arg1, arg2);
        } catch (Exception e) {
            regenerate();
            return model.remove(arg0, arg1, arg2);
        }
    }

    public RDFNode asRDFNode(Node arg0) {
        try {
            return model.asRDFNode(arg0);
        } catch (Exception e) {
            regenerate();
            return model.asRDFNode(arg0);
        }
    }

    public Statement asStatement(Triple arg0) {
        try {
            return model.asStatement(arg0);
        } catch (Exception e) {
            regenerate();
            return model.asStatement(arg0);
        }
    }

    public Graph getGraph() {
        try {
            return model.getGraph();
        } catch (Exception e) {
            regenerate();
            return model.getGraph();
        }
    }

    public QueryHandler queryHandler() {
        try {
            return model.queryHandler();
        } catch (Exception e) {
            regenerate();
            return model.queryHandler();
        }
    }

    public RDFReader getReader() {
        try {
            return model.getReader();
        } catch (Exception e) {
            regenerate();
            return model.getReader();
        }
    }

    public RDFReader getReader(String arg0) {
        try {
            return model.getReader(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getReader(arg0);
        }
    }

    public String setReaderClassName(String arg0, String arg1) {
        try {
            return model.setReaderClassName(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.setReaderClassName(arg0, arg1);
        }
    }

    public RDFWriter getWriter() {
        try {
            return model.getWriter();
        } catch (Exception e) {
            regenerate();
            return model.getWriter();
        }
    }

    public RDFWriter getWriter(String arg0) {
        try {
            return model.getWriter(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getWriter(arg0);
        }
    }

    public String setWriterClassName(String arg0, String arg1) {
        try {
            return model.setWriterClassName(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.setWriterClassName(arg0, arg1);
        }
    }

    public String expandPrefix(String arg0) {
        try {
            return model.expandPrefix(arg0);
        } catch (Exception e) {
            regenerate();
            return model.expandPrefix(arg0);
        }
    }

    public Map getNsPrefixMap() {
        try {
            return model.getNsPrefixMap();
        } catch (Exception e) {
            regenerate();
            return model.getNsPrefixMap();
        }
    }

    public String getNsPrefixURI(String arg0) {
        try {
            return model.getNsPrefixURI(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getNsPrefixURI(arg0);
        }
    }

    public String getNsURIPrefix(String arg0) {
        try {
            return model.getNsURIPrefix(arg0);
        } catch (Exception e) {
            regenerate();
            return model.getNsURIPrefix(arg0);
        }
    }

    public PrefixMapping lock() {
        try {
            return model.lock();
        } catch (Exception e) {
            regenerate();
            return model.lock();
        }
    }

    public String qnameFor(String arg0) {
        try {
            return model.qnameFor(arg0);
        } catch (Exception e) {
            regenerate();
            return model.qnameFor(arg0);
        }
    }

    public PrefixMapping removeNsPrefix(String arg0) {
        try {
            return model.removeNsPrefix(arg0);
        } catch (Exception e) {
            regenerate();
            return model.removeNsPrefix(arg0);
        }
    }

    public boolean samePrefixMappingAs(PrefixMapping arg0) {
        try {
            return model.samePrefixMappingAs(arg0);
        } catch (Exception e) {
            regenerate();
            return model.samePrefixMappingAs(arg0);
        }
    }

    public PrefixMapping setNsPrefix(String arg0, String arg1) {
        try {
            return model.setNsPrefix(arg0, arg1);
        } catch (Exception e) {
            regenerate();
            return model.setNsPrefix(arg0, arg1);
        }
    }

    public PrefixMapping setNsPrefixes(PrefixMapping arg0) {
        try {
            return model.setNsPrefixes(arg0);
        } catch (Exception e) {
            regenerate();
            return model.setNsPrefixes(arg0);
        }
    }

    public String shortForm(String arg0) {
        try {
            return model.shortForm(arg0);
        } catch (Exception e) {
            regenerate();
            return model.shortForm(arg0);
        }
    }

    public PrefixMapping withDefaultMappings(PrefixMapping arg0) {
        try {
            return model.withDefaultMappings(arg0);
        } catch (Exception e) {
            regenerate();
            return model.withDefaultMappings(arg0);
        }
    }

    public void enterCriticalSection(boolean arg0) {
        plainVanillaModel.enterCriticalSection(arg0);
    }

    public void leaveCriticalSection() {
        plainVanillaModel.leaveCriticalSection();
    }

	public ExtendedIterator listAllOntProperties() {
		try {
			return model.listAllOntProperties();
		} catch (Exception e) {
			regenerate();
			return model.listAllOntProperties();
		}
	}

	public ResIterator listResourcesWithProperty(Property arg0) {
		try {
			return model.listResourcesWithProperty(arg0);
		} catch (Exception e) {
			regenerate();
			return model.listResourcesWithProperty(arg0);
		}
	}

	public ResIterator listResourcesWithProperty(Property arg0, RDFNode arg1) {
		try {
			return model.listResourcesWithProperty(arg0, arg1);
		} catch (Exception e) {
			regenerate();
			return model.listResourcesWithProperty(arg0, arg1);
		}
	}

	public Model addLiteral(Resource arg0, Property arg1, boolean arg2) {
		try {
			return model.addLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.addLiteral(arg0, arg1, arg2);
		}
	}

	public Model addLiteral(Resource arg0, Property arg1, long arg2) {
		try {
			return model.addLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.addLiteral(arg0, arg1, arg2);
		}
	}

	public Model addLiteral(Resource arg0, Property arg1, int arg2) {
		try {
			return model.addLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.addLiteral(arg0, arg1, arg2);
		}
	}

	public Model addLiteral(Resource arg0, Property arg1, char arg2) {
		try {
			return model.addLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.addLiteral(arg0, arg1, arg2);
		}
	}

	public Model addLiteral(Resource arg0, Property arg1, float arg2) {
		try {
			return model.addLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.addLiteral(arg0, arg1, arg2);
		}
	}

	public Model addLiteral(Resource arg0, Property arg1, double arg2) {
		try {
			return model.addLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.addLiteral(arg0, arg1, arg2);
		}
	}

	@Deprecated
	public Model addLiteral(Resource arg0, Property arg1, Object arg2) {
		try {
			return model.addLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.addLiteral(arg0, arg1, arg2);
		}
	}

	public boolean containsLiteral(Resource arg0, Property arg1, boolean arg2) {
		try {
			return model.containsLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.containsLiteral(arg0, arg1, arg2);
		}
	}

	public boolean containsLiteral(Resource arg0, Property arg1, long arg2) {
		try {
			return model.containsLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.containsLiteral(arg0, arg1, arg2);
		}
	}

	public boolean containsLiteral(Resource arg0, Property arg1, int arg2) {
		try {
			return model.containsLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.containsLiteral(arg0, arg1, arg2);
		}
	}

	public boolean containsLiteral(Resource arg0, Property arg1, char arg2) {
		try {
			return model.containsLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.containsLiteral(arg0, arg1, arg2);
		}
	}

	public boolean containsLiteral(Resource arg0, Property arg1, float arg2) {
		try {
			return model.containsLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.containsLiteral(arg0, arg1, arg2);
		}
	}

	public boolean containsLiteral(Resource arg0, Property arg1, double arg2) {
		try {
			return model.containsLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.containsLiteral(arg0, arg1, arg2);
		}
	}

	public boolean containsLiteral(Resource arg0, Property arg1, Object arg2) {
		try {
			return model.containsLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.containsLiteral(arg0, arg1, arg2);
		}
	}

	public ResIterator listResourcesWithProperty(Property arg0, boolean arg1) {
		try {
			return model.listResourcesWithProperty(arg0, arg1);
		} catch (Exception e) {
			regenerate();
			return model.listResourcesWithProperty(arg0, arg1);
		}
	}

	public ResIterator listResourcesWithProperty(Property arg0, long arg1) {
		try {
			return model.listResourcesWithProperty(arg0, arg1);
		} catch (Exception e) {
			regenerate();
			return model.listResourcesWithProperty(arg0, arg1);
		}
	}

	public ResIterator listResourcesWithProperty(Property arg0, char arg1) {
		try {
			return model.listResourcesWithProperty(arg0, arg1);
		} catch (Exception e) {
			regenerate();
			return model.listResourcesWithProperty(arg0, arg1);
		}
	}

	public ResIterator listResourcesWithProperty(Property arg0, float arg1) {
		try {
			return model.listResourcesWithProperty(arg0, arg1);
		} catch (Exception e) {
			regenerate();
			return model.listResourcesWithProperty(arg0, arg1);
		}
	}

	public ResIterator listResourcesWithProperty(Property arg0, double arg1) {
		try {
			return model.listResourcesWithProperty(arg0, arg1);
		} catch (Exception e) {
			regenerate();
			return model.listResourcesWithProperty(arg0, arg1);
		}
	}

	public ResIterator listResourcesWithProperty(Property arg0, Object arg1) {
		try {
			return model.listResourcesWithProperty(arg0, arg1);
		} catch (Exception e) {
			regenerate();
			return model.listResourcesWithProperty(arg0, arg1);
		}
	}

	public <T extends OntResource> T createOntResource(Class<T> arg0,
			Resource arg1, String arg2) {
		try {
			return model.createOntResource(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.createOntResource(arg0, arg1, arg2);
		}
	}

	public <T extends RDFNode> ExtendedIterator<T> queryFor(
			BindingQueryPlan arg0, List<BindingQueryPlan> arg1, Class<T> arg2) {
		try {
			return model.queryFor(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.queryFor(arg0, arg1, arg2);
		}
		
	}

	public Model add(List<Statement> arg0) {
		try {
			return model.add(arg0);
		} catch (Exception e) {
			regenerate();
			return model.add(arg0);
		}
	}

	public RDFList createList(Iterator<? extends RDFNode> arg0) {
		try {
			return model.createList(arg0);
		} catch (Exception e) {
			regenerate();
			return model.createList(arg0);
		}
	}

	public Model remove(List<Statement> arg0) {
		try {
			return model.remove(arg0);
		} catch (Exception e) {
			regenerate();
			return model.remove(arg0);
		}
	}

	public StmtIterator listLiteralStatements(Resource arg0, Property arg1,
			float arg2) {
		try {
			return model.listLiteralStatements(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.listLiteralStatements(arg0, arg1, arg2);
		}
	}

	public PrefixMapping setNsPrefixes(Map<String, String> arg0) {
		try {
			return model.setNsPrefixes(arg0);
		} catch (Exception e) {
			regenerate();
			return model.setNsPrefixes(arg0);
		}
	}

	@Deprecated
	public Model addLiteral(Resource arg0, Property arg1, Literal arg2) {
		try {
			return model.addLiteral(arg0, arg1, arg2);
		} catch (Exception e) {
			regenerate();
			return model.addLiteral(arg0, arg1, arg2);
		}		
	}

	public Resource wrapAsResource(Node arg0) {
		try {
			return model.wrapAsResource(arg0);
		} catch (Exception e) {
			regenerate();
			return model.wrapAsResource(arg0);
		}
	}

}
