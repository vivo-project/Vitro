/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.controller.edit.listing.jena;

import java.util.ArrayList;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.cornell.mannlib.vitro.webapp.utils.JSPPageHandler;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.CardinalityRestriction;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.MaxCardinalityRestriction;
import org.apache.jena.ontology.MinCardinalityRestriction;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vedit.controller.BaseEditController;
import edu.cornell.mannlib.vitro.webapp.auth.permissions.SimplePermission;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.controller.Controllers;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;

public class RestrictionsListingController extends BaseEditController {

	private static String LAMBDA = "";
	
	private EditProcessObject epo = null;
	
    public void doGet(HttpServletRequest request, HttpServletResponse response) {    	
        if (!isAuthorizedToDisplayPage(request, response, SimplePermission.EDIT_ONTOLOGY.ACTION)) {
        	return;
        }

        VitroRequest vrequest = new VitroRequest(request);

        epo = super.createEpo(request);
        
		OntModel ontModel = ModelAccess.on(getServletContext()).getOntModel();

        ObjectPropertyDao opDao = vrequest.getUnfilteredWebappDaoFactory().getObjectPropertyDao();
        VClassDao vcDao = vrequest.getUnfilteredWebappDaoFactory().getVClassDao();
        IndividualDao iDao = vrequest.getUnfilteredWebappDaoFactory().getIndividualDao();
        
        ArrayList results = new ArrayList();
        request.setAttribute("results",results);
        results.add("XX");
        results.add("property");
        results.add("restriction");
        results.add("filler(s)");
        results.add(LAMBDA);
        
        String vClassURI = request.getParameter("VClassURI");

        if (vClassURI != null) {
        	ontModel.enterCriticalSection(Lock.READ);
        	try {
	        	OntClass ontClass = ontModel.getOntClass(vClassURI);
	        	if (ontClass != null) {
	        		ClosableIterator superClassIt = ontClass.listSuperClasses();
	        		try {
		        		for (Iterator i = superClassIt; i.hasNext(); ) {
		        			OntClass superClass = (OntClass) i.next();
		        			tryRestriction(superClass, vcDao, opDao, iDao, results, vClassURI);
		        		}
	        		} finally {
	        			superClassIt.close();
	        		}
	        		ClosableIterator equivClassIt = ontClass.listEquivalentClasses();
	        		try {
		        		for (Iterator i = equivClassIt; i.hasNext(); ) {
		        			OntClass superClass = (OntClass) i.next();
		        			tryRestriction(superClass, vcDao, opDao, iDao, results, vClassURI);
		        		}
	        		} finally {
	        			equivClassIt.close();
	        		}
	        	} else {
	        		doClassNotFound(results);
	        	}
            } finally {
            	ontModel.leaveCriticalSection();
            }
        } else {
        	doClassNotFound(results);
        }


        

        request.setAttribute("columncount",new Integer(5));
        request.setAttribute("suppressquery","true");
        request.setAttribute("title","Restrictions");
        try {
			JSPPageHandler.renderBasicPage(request, response, Controllers.HORIZONTAL_JSP);
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
    
    private void tryRestriction(OntClass theClass, VClassDao vcDao, ObjectPropertyDao opDao, IndividualDao iDao, ArrayList results, String vClassURI) {
		if (theClass.isRestriction()) {
			Restriction rest = (Restriction) theClass.as(Restriction.class);
			try {
				results.add("XX");
				Property onProperty = rest.getOnProperty();
				ObjectProperty op = opDao.getObjectPropertyByURI(onProperty.getURI());
				results.add(op.getPickListName());
				if (rest.isAllValuesFromRestriction()) {
					results.add("all values from");
					AllValuesFromRestriction avfrest = (AllValuesFromRestriction) rest.as(AllValuesFromRestriction.class);
					Resource allValuesFrom = avfrest.getAllValuesFrom();
					results.add(printAsClass(vcDao, allValuesFrom));	        					
				} else if (rest.isSomeValuesFromRestriction()) {
					results.add("some values from");
					SomeValuesFromRestriction svfrest = (SomeValuesFromRestriction) rest.as(SomeValuesFromRestriction.class);
					Resource someValuesFrom = svfrest.getSomeValuesFrom();
					results.add(printAsClass(vcDao, someValuesFrom));
				} else if (rest.isHasValueRestriction()) {
					results.add("has value");
					HasValueRestriction hvrest = (HasValueRestriction) rest.as(HasValueRestriction.class);
					RDFNode hasValue = hvrest.getHasValue();
					if (hasValue.isResource()) {
						Resource hasValueRes = (Resource) hasValue.as(Resource.class);
						try {
							if (hasValueRes.getURI() != null) {
								Individual ind = iDao.getIndividualByURI(hasValueRes.getURI());
								if (ind.getName() != null) {
									results.add(ind.getName());
								}
							}
						} catch (Exception e) {
							results.add("???");
						}
					}
					
				} else if (rest.isMinCardinalityRestriction()) {
					MinCardinalityRestriction crest = (MinCardinalityRestriction) rest.as(MinCardinalityRestriction.class);
					results.add("at least "+crest.getMinCardinality());
					results.add(LAMBDA);
				} else if (rest.isMaxCardinalityRestriction()) {
					MaxCardinalityRestriction crest = (MaxCardinalityRestriction) rest.as(MaxCardinalityRestriction.class);
					results.add("at most "+crest.getMaxCardinality());
					results.add(LAMBDA);
				} else if (rest.isCardinalityRestriction()) {
					CardinalityRestriction crest = (CardinalityRestriction) rest.as(CardinalityRestriction.class);
					results.add("exactly "+crest.getCardinality());
					results.add(LAMBDA);
				}
				
				results.add("<form action=\"addRestriction\" method=\"post\">" +
						        "<input type=\"hidden\" name=\"_action\" value=\"delete\"/>" +
						        "<input type=\"submit\" value=\"Delete\"/>" + 
						        "<input type=\"hidden\" name=\"_epoKey\" value=\""+epo.getKey()+"\"/>" +
						        "<input type=\"hidden\" name=\"classUri\" value=\""+vClassURI+"\"/>" +
						        "<input type=\"hidden\" name=\"restrictionId\" value=\""+( (rest.getId() != null) ? rest.getId() : rest.getURI() )+"\"/>" +
						    "</form>");
				
			} catch (Exception e) {
				e.printStackTrace(); // results.add("unknown property");
			}
					
		}	
    }
    
    private String printAsClass(VClassDao vcDao, Resource res) {
    	String UNKNOWN = "???";
    	try {
    		VClass vClass = vcDao.getVClassByURI(res.getURI());
    		return (vClass.getName() != null) ? vClass.getName() : UNKNOWN ;
    	} catch (Exception e) {
    		return UNKNOWN;
    	}
    }

    private void doClassNotFound(ArrayList results) {
    	results.add("XX");
    	results.add("Class not found");
    	results.add(LAMBDA);
    	results.add(LAMBDA);
    	results.add(LAMBDA);
    }
    
	
}
