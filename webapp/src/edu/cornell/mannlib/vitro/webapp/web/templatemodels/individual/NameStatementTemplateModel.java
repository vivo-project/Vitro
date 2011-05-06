/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.hp.hpl.jena.rdf.model.Literal;

import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.UrlBuilder;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

public class NameStatementTemplateModel extends
        DataPropertyStatementTemplateModel {
    
    private static final Log log = LogFactory.getLog(NameStatementTemplateModel.class); 

    private String curie;
    
    /*
     * This method handles the special case where we are creating a DataPropertyStatementTemplateModel outside the GroupedPropertyList.
     * Specifically, it allows rdfs:label to be treated like a data property statement and thus have editing links. It is not possible
     * to handle rdfs:label like vitro links and vitroPublic image, because it is not possible to construct a DataProperty from
     * rdfs:label. 
     */
    NameStatementTemplateModel(String subjectUri, VitroRequest vreq, EditingPolicyHelper policyHelper) {
        super(subjectUri, VitroVocabulary.LABEL, policyHelper);
        
        String propertyUri = VitroVocabulary.LABEL;
        DataPropertyStatementDao dpsDao = vreq.getWebappDaoFactory().getDataPropertyStatementDao();
        List<Literal> literals = dpsDao.getDataPropertyValuesForIndividualByProperty(subjectUri, propertyUri);
        
        // Make sure the subject has a value for this property 
        if (literals.size() > 0) {
            Literal literal = literals.get(0);
            value = literal.getLexicalForm();
            setEditAccess(literal, policyHelper);
            curie = UrlBuilder.getCurie(propertyUri, vreq);
        } else {
            // If the individual has no rdfs:label, use the local name. It will not be editable (this replicates previous behavior;
            // perhaps we would want to allow a label to be added. But such individuals do not usually have their profiles viewed or
            // edited directly.
            URI uri = new URIImpl(subjectUri);
            value = uri.getLocalName();       
            curie = null;
        }
    }
    
    
    /* Access methods for templates */

    public String getCurie() {
        return curie;
    }
}
