/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.utils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.dao.ModelAccess.ModelID;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactoryJena;

public class ApplicationConfigurationOntologyUtils {

    private static final Log log = LogFactory.getLog(ApplicationConfigurationOntologyUtils.class);
    
    public static List<ObjectProperty> getAdditionalFauxSubpropertiesForList(List<ObjectProperty> propList, Individual subject, VitroRequest vreq) {
        ServletContext ctx = vreq.getSession().getServletContext();
		Model displayModel = ModelAccess.on(ctx).getDisplayModel();
        Model tboxModel = ModelAccess.on(ctx).getOntModel(ModelID.UNION_TBOX);
        return getAdditionalFauxSubpropertiesForList(propList, subject, displayModel, tboxModel);
    }
    
    public static List<ObjectProperty> getAdditionalFauxSubproperties(ObjectProperty prop, 
                                                                         Individual subject,
                                                                         Model tboxModel,
                                                                         Model union) {

        List<ObjectProperty> additionalProps = new ArrayList<ObjectProperty>();
        String queryStr = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                "PREFIX config: <http://vitro.mannlib.cornell.edu/ns/vitro/ApplicationConfiguration#> \n" +
                "PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#> \n" +
                "SELECT DISTINCT ?range ?domain ?label ?group ?customForm ?displayLevel ?updateLevel ?property WHERE { \n" +
//                "    ?p rdfs:subPropertyOf ?property . \n" +
                "    ?context config:configContextFor ?property . \n" +
                "    ?context config:qualifiedBy ?range . \n" +
                "    ?context config:hasConfiguration ?configuration . \n" +
                "    OPTIONAL { ?context config:qualifiedByDomain ?domain } \n" +
                "    OPTIONAL { ?configuration config:propertyGroup ?group } \n" +
                "    OPTIONAL { ?configuration config:displayName ?label } \n" +
                "    OPTIONAL { ?configuration vitro:customEntryFormAnnot ?customForm } \n" +
                "    OPTIONAL { ?configuration vitro:hiddenFromDisplayBelowRoleLevelAnnot ?displayLevel } \n" +
                "    OPTIONAL { ?configuration vitro:prohibitedFromUpdateBelowRoleLevelAnnot ?updateLevel } \n" +
                "}"; 
      
        if(prop != null) {
            log.debug("Checking " + prop.getURI() + " for additional properties");
            queryStr = queryStr.replaceAll("\\?property", "<" + prop.getURI() + ">");
        }
        log.debug(queryStr);
        Query q = QueryFactory.create(queryStr);
        QueryExecution qe = QueryExecutionFactory.create(q, union);
        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                QuerySolution qsoln = rs.nextSolution();
                log.debug(qsoln);
                ObjectProperty op = null;
                if (prop == null) {
                    String opURI = qsoln.getResource("property").getURI();
                    OntModel tboxOntModel = ModelFactory.createOntologyModel(
                            OntModelSpec.OWL_MEM, tboxModel);
                    WebappDaoFactory wadf = new WebappDaoFactoryJena(tboxOntModel); 
                    op = wadf.getObjectPropertyDao().getObjectPropertyByURI(opURI);
                } else {
                    op = prop;
                }
                ObjectProperty newProp = new ObjectProperty();
                newProp.setURI(op.getURI());
                Resource domainRes = qsoln.getResource("domain");
                if(domainRes != null) {
                    if(!appropriateDomain(
                        domainRes, subject, tboxModel)) {
                        continue;
                    } else {
                        newProp.setDomainVClassURI(domainRes.getURI());
                    }
                } 
                Resource rangeRes = qsoln.getResource("range");
                if (rangeRes != null) {
                    newProp.setRangeVClassURI(rangeRes.getURI());
                } else {
                    newProp.setRangeVClassURI(op.getRangeVClassURI());
                }
                Resource groupRes = qsoln.getResource("group");
                if (groupRes != null) {
                    newProp.setGroupURI(groupRes.getURI());
                } else {
                    newProp.setGroupURI(op.getURI());
                }
                Literal labelLit = qsoln.getLiteral("label");
                if (labelLit != null) {
                    newProp.setDomainPublic(labelLit.getLexicalForm());
                } else {
                    newProp.setDomainPublic(op.getDomainPublic());
                }
                Literal customFormLit = qsoln.getLiteral("customForm");
                if (customFormLit != null) {
                    newProp.setCustomEntryForm(customFormLit.getLexicalForm());
                } else {
                    newProp.setCustomEntryForm(op.getCustomEntryForm());
                }
                Resource displayLevelRes = qsoln.getResource("displayLevel");
                if (displayLevelRes != null) {
                    newProp.setHiddenFromDisplayBelowRoleLevel(
                            BaseResourceBean.RoleLevel.getRoleByUri(
                                    displayLevelRes.getURI()));
                }
                Resource updateLevelRes = qsoln.getResource("updateLevel");
                if (updateLevelRes != null) {
                    newProp.setProhibitedFromUpdateBelowRoleLevel(
                            BaseResourceBean.RoleLevel.getRoleByUri(
                                    updateLevelRes.getURI()));
                }
                additionalProps.add(newProp);
            }  
        } finally {
            qe.close();
        }
        return additionalProps;
    }   
    
    
    
    public static List<ObjectProperty> getAdditionalFauxSubpropertiesForList(List<ObjectProperty> propList, 
                                                                         Individual subject, 
                                                                         Model displayModel, 
                                                                         Model tboxModel) {
        
        List<ObjectProperty> additionalProps = new ArrayList<ObjectProperty>();
        Model union = ModelFactory.createUnion(displayModel, tboxModel);
        
        for (ObjectProperty op : propList) {
            propList.addAll(getAdditionalFauxSubproperties(op, subject, tboxModel, union));
        }    

        return additionalProps;
    }
    
    private static boolean appropriateDomain(Resource domainRes, Individual subject, Model tboxModel) {
        if (subject == null) {
            return true;
        }
        for (VClass vclass : subject.getVClasses()) {
            if ((vclass.getURI() != null) &&
                    ((vclass.getURI().equals(domainRes.getURI()) ||
                    (tboxModel.contains(
                            ResourceFactory.createResource(
                                    vclass.getURI()), RDFS.subClassOf, domainRes))))) {
                return true;
            }
        }
        return false;
    }
    
}
