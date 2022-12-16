/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxDataPropertyWrapper;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxObjectPropertyWrapper;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual.FauxPropertyWrapper;

public class ApplicationConfigurationOntologyUtils {

    private static final Log log = LogFactory.getLog(ApplicationConfigurationOntologyUtils.class);
    
    private static final String getPossibleFauxQuery(boolean isData) {
    	return
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "PREFIX config: <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" +
                "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" +
                "SELECT DISTINCT ?domain ?property ?context ?isData WHERE { \n" +
                "    ?context config:configContextFor ?property . \n" +
                (isData ? 
                     "?property a <http://www.w3.org/2002/07/owl#DatatypeProperty> . \n" 
                        : 
                     "?property a <http://www.w3.org/2002/07/owl#ObjectProperty> . \n" ) +
                "    OPTIONAL {  ?context config:qualifiedBy ?range . } \n " +  
                "    ?context config:hasConfiguration ?configuration . \n" +
                "    ?configuration a config:ObjectPropertyDisplayConfig . \n" +
                "    OPTIONAL { ?context config:qualifiedByDomain ?domain } . \n" +
                "}";
    }
    
    private static String getFauxPropQuery(String baseUri, boolean optionalRange) {
		return
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
	            "PREFIX config: <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" +
	            "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" +
	            "SELECT DISTINCT ?domain ?context WHERE { \n" +
	            "    ?context config:configContextFor <" + baseUri + "> . \n" +
	            (optionalRange ? " OPTIONAL { " : "") +  
	            "    ?context config:qualifiedBy ?range . \n" +
	            (optionalRange ? " } " : "") +  
	            "    ?context config:hasConfiguration ?configuration . \n" +
	            "    ?configuration a config:ObjectPropertyDisplayConfig . \n" +
	            "    OPTIONAL { ?context config:qualifiedByDomain ?domain } \n" +
	            "}";
	} 		

    public static List<FauxObjectPropertyWrapper> getPopulatedFauxOPs(List<ObjectProperty> populatedObjectProperties, Individual subject, VitroRequest vreq) {
		List<FauxObjectPropertyWrapper> fauxProperties = new ArrayList<FauxObjectPropertyWrapper>();
		for (ObjectProperty op : populatedObjectProperties) {
		    fauxProperties.addAll(getPopulatedFauxObjectProperties(op, subject, vreq));
		}
		return fauxProperties;
    }
    
    public static List<FauxDataPropertyWrapper> getPopulatedFauxDPs(List<DataProperty> populatedDataProperties, Individual subject, VitroRequest vreq) {
		List<FauxDataPropertyWrapper> fauxProperties = new ArrayList<FauxDataPropertyWrapper>();
		for (DataProperty dp : populatedDataProperties) {
		    fauxProperties.addAll(getPopulatedFauxDataProperties(dp, subject, vreq));
		}
		return fauxProperties;
    }

    private static List<FauxObjectPropertyWrapper> getPopulatedFauxObjectProperties(ObjectProperty op, Individual subject, VitroRequest vreq) {
        Model displayModel = vreq.getDisplayModel();
        Model tboxModel = vreq.getOntModelSelector().getTBoxModel();
        Model union = ModelFactory.createUnion(displayModel, tboxModel);
        WebappDaoFactory wadf = new WebappDaoFactoryJena(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, union));
        FauxPropertyDao fpDao = wadf.getFauxPropertyDao();

        Query q = createQuery(op, false);
        QueryExecution qe = QueryExecutionFactory.create(q, union);
        List<FauxObjectPropertyWrapper> fauxObjectProps = new ArrayList<FauxObjectPropertyWrapper>();
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qsoln = rs.nextSolution();
                log.debug(qsoln);
                Resource domainRes = qsoln.getResource("domain");
                String domainURI = (domainRes != null) ? domainRes.getURI() : null;
                String contextURI = qsoln.getResource("context").getURI();
                if (isDomainMatchSubject(domainURI, subject)) {
                    try {
                        FauxProperty fp = fpDao.getFauxPropertyFromContextUri(contextURI);
                        if (fp != null) {
                            fauxObjectProps.add(new FauxObjectPropertyWrapper(op.clone(), fp));
                        }
                    } catch (Exception e) {
                        log.warn("Couldn't look up the faux property", e);
                    }
                }
            }
        } finally {
            qe.close();
        }
        return fauxObjectProps;
    }
	
    private static List<FauxDataPropertyWrapper> getPopulatedFauxDataProperties(DataProperty dp, Individual subject, VitroRequest vreq) {
        Model displayModel = vreq.getDisplayModel();
        Model tboxModel = vreq.getOntModelSelector().getTBoxModel();
        Model union = ModelFactory.createUnion(displayModel, tboxModel);
        WebappDaoFactory wadf = new WebappDaoFactoryJena(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, union));
        FauxPropertyDao fpDao = wadf.getFauxPropertyDao();

        Query q = createQuery(dp, true);
        QueryExecution qe = QueryExecutionFactory.create(q, union);
        List<FauxDataPropertyWrapper> fauxDataProps = new ArrayList<FauxDataPropertyWrapper>();
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qsoln = rs.nextSolution();
                log.debug(qsoln);
                Resource domainRes = qsoln.getResource("domain");
                String domainURI = (domainRes != null) ? domainRes.getURI() : null;
                String contextURI = qsoln.getResource("context").getURI();
                if (isDomainMatchSubject(domainURI, subject)) {
                    try {
                        FauxProperty fp = fpDao.getFauxPropertyFromContextUri(contextURI);
                        if (fp != null) {
                            fauxDataProps.add(new FauxDataPropertyWrapper(dp, fp));
                        }
                    } catch (Exception e) {
                        log.warn("Couldn't look up the faux property", e);
                    }
                }
            }
        } finally {
            qe.close();
        }
        return fauxDataProps;
    }

    private static Query createQuery(Property op, boolean optionalRange) {
        String queryStr = getFauxPropQuery(op.getURI(), optionalRange);
        log.debug(queryStr);
        Query q = QueryFactory.create(queryStr);
        return q;
    }

    private static boolean isDomainMatchSubject(String domainUri, Individual subject) {
        if (subject == null || domainUri == null) {
            return true;
        }
        Set<String> vClassUris = subject.getVClasses().stream().map(vclass -> vclass.getURI())
                .collect(Collectors.toSet());
        for (String vClassUri : vClassUris) {
            if (vClassUri != null && vClassUri.equals(domainUri)) {
                return true;
            }
        }
        return false;
    }

    public static List<Property> getPossibleFauxProps(List<? extends FauxPropertyWrapper> curProps, Individual subject, VitroRequest vreq, boolean isData) {
        Model displayModel = vreq.getDisplayModel();
        Model tboxModel = vreq.getOntModelSelector().getTBoxModel();
        Model union = ModelFactory.createUnion(displayModel, tboxModel);
        Map<String, FauxProperty> curPropsMap = curProps.stream()
                .collect(Collectors.toMap(FauxPropertyWrapper::getContextUri, FauxPropertyWrapper::getFauxProperty));
        WebappDaoFactory wadf = new WebappDaoFactoryJena(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, union));
        FauxPropertyDao fpDao = wadf.getFauxPropertyDao();
        ObjectPropertyDao opDao = wadf.getObjectPropertyDao();
        DataPropertyDao dpDao = wadf.getDataPropertyDao();

        Query q = QueryFactory.create(getPossibleFauxQuery(isData));
        QueryExecution qe = QueryExecutionFactory.create(q, union);
        List<Property> fauxDataProps = new ArrayList<Property>();
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qsoln = rs.nextSolution();
                Resource domainRes = qsoln.getResource("domain");
                String domainURI = (domainRes != null) ? domainRes.getURI() : null;
                String basePropertyUri = qsoln.getResource("property").getURI();
                String contextURI = qsoln.getResource("context").getURI();
                if (isDomainMatchSubject(domainURI, subject) && !curPropsMap.containsKey(contextURI)) {
                    if (isData) {
                        addDataProperty(fauxDataProps, basePropertyUri, contextURI, fpDao, dpDao);
                    } else {
                        addObjectProperty(fauxDataProps, basePropertyUri, contextURI, fpDao, opDao);
                    }
                }
            }
        } finally {
            qe.close();
        }
        return fauxDataProps;
    }

    private static void addObjectProperty(List<Property> fauxProps, String basePropertyUri, String contextURI,
            FauxPropertyDao fpDao, ObjectPropertyDao opDao) {
        try {
            FauxProperty fp = fpDao.getFauxPropertyFromContextUri(contextURI);
            ObjectProperty op = opDao.getObjectPropertyByURI(basePropertyUri);
            if (fp != null && op != null) {
                fauxProps.add(new FauxObjectPropertyWrapper(op, fp));
            }
        } catch (Exception e) {
            log.warn("Couldn't look up the faux object property, contextUri " + contextURI, e);
        }
    }

    private static void addDataProperty(List<Property> fauxProps, String basePropertyUri, String contextURI,
            FauxPropertyDao fpDao, DataPropertyDao dpDao) {
        try {
            FauxProperty fp = fpDao.getFauxPropertyFromContextUri(contextURI);
            DataProperty dp = dpDao.getDataPropertyByURI(basePropertyUri);
            if (fp != null && dp != null) {
                fauxProps.add(new FauxDataPropertyWrapper(dp, fp));
            }
        } catch (Exception e) {
            log.warn("Couldn't look up the faux data property, contextUri " + contextURI, e);
        }

    }
}
