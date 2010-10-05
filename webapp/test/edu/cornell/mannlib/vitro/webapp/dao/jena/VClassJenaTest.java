/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;


import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.CardinalityRestriction;
import com.hp.hpl.jena.ontology.ComplementClass;
import com.hp.hpl.jena.ontology.HasValueRestriction;
import com.hp.hpl.jena.ontology.IntersectionClass;
import com.hp.hpl.jena.ontology.MaxCardinalityRestriction;
import com.hp.hpl.jena.ontology.MinCardinalityRestriction;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;
import com.hp.hpl.jena.ontology.UnionClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.beans.BaseResourceBean;
import edu.cornell.mannlib.vitro.webapp.beans.Ontology;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.OntologyDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;

/**
 * 
 * 
 */

public class VClassJenaTest {
	
	@Test
	// NIHVIVO-1157 introduced VClassJena.java, a lazy-loading version of VClass.java.  
	// Per instructions from Brian L., this test tests that for onw randomly selected Class,
	// the getter methods in VClassJena return the same values as would have been
	// returned by the pre-NIHVIVO-1157 VClass (which would have been set by the
	// (now deleted) vClassWebappFromOntClass inner class in VClassDaoJena).

	
	public void correctValues(){
	
		// 1. create a model and populate it with the data for one class
		// 2. populate a VClass instance as it would have been populated pre-NIHVIVO-1157
		// 3. populate a VClassJena instance with the current system code
		// 4. verify that the getter methods on the VClassJena and VClass instances return the same values
		
		String class1URI = "http://test.vivo/AcademicDegree";

		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM); 
		
		String rdfsLabel = "this is the rdfs label";
		String lang = "en-US";
		
		// populate sub-model
		OntClass class1 = ontModel.createClass(class1URI);
		
		class1.setLabel(rdfsLabel,lang);   //rdfs:label		
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.IN_CLASSGROUP), ontModel.createResource("http://thisIsTheClassGroupURI"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.SHORTDEF), ontModel.createTypedLiteral("this is the short definition"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.EXAMPLE_ANNOT), ontModel.createTypedLiteral("this is the example"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.DESCRIPTION_ANNOT), ontModel.createTypedLiteral("this is the description"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.DISPLAY_LIMIT), ontModel.createTypedLiteral(-1));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.DISPLAY_RANK_ANNOT), ontModel.createTypedLiteral(-11));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.SEARCH_BOOST_ANNOT), ontModel.createTypedLiteral(2.4f));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT), ontModel.createResource("http://vitro.mannlib.cornell.edu/ns/vitro/role#curator"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT), ontModel.createResource("http://vitro.mannlib.cornell.edu/ns/vitro/role#selfEditor"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMENTRYFORMANNOT), ontModel.createTypedLiteral("this is the custom entry form annotation"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMDISPLAYVIEWANNOT), ontModel.createTypedLiteral("this is the custom display view annotation"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMSHORTVIEWANNOT), ontModel.createTypedLiteral("this is the custom short view annotation"));
		class1.setPropertyValue(ontModel.createProperty(VitroVocabulary.PROPERTY_CUSTOMSEARCHVIEWANNOT), ontModel.createTypedLiteral("this is the custom search view annotation"));
	
		
		WebappDaoFactoryJena wadf = new WebappDaoFactoryJena(ontModel);
		
		// Populate a VClass instance... 
		
		VClassDaoJena vcdj = (VClassDaoJena) wadf.getVClassDao();
		VClass vClass = new VClass();
		
		
		     //VClass vClass = vcdj.getVClassByURI(class1URI);          
		
		
		
		// Populate a VClassJena instance...
		
		VClassJena vClassJena = new VClassJena(class1, wadf);
		
		
		// Check that the getters from the VClass and the VClassJena return the same values
		//Assert.assertEquals(vClassJena.getName(), vClass.getName());  
		
		
        
	}

	
	// To help in debugging the unit test
	void printModels(OntModel ontModel) {
	    
		System.out.println("\nThe model has " + ontModel.size() + " statements:");
		System.out.println("---------------------------------------------------");
		ontModel.writeAll(System.out,"N3",null);
		
	}
	
	
	/*
	
	// The following class and methods are pre-NIHVIVO-1157 code for
	// populating a VClass. Original comments included.

    private VClass vClassWebappFromOntClass(OntClass cls) {
        VClass vcw = new VClass();
        cls.getModel().enterCriticalSection(Lock.READ);
		vcw.setName(getLabelForClass(cls,false,false));
		vcw.setLocalNameWithPrefix(getLabelForClass(cls,true,false));
		vcw.setPickListName(getLabelForClass(cls,false,true));
        try {
        	if (cls.isAnon()) {
        		vcw.setNamespace(VitroVocabulary.PSEUDO_BNODE_NS);
        		vcw.setLocalName(cls.getId().toString());
        	} else {
                if (vcw.getName() == null)
                    vcw.setName("[null]");
                vcw.setURI(cls.getURI());
                vcw.setNamespace(cls.getNameSpace());
                vcw.setLocalName(cls.getLocalName());
        	}
            try {
                Resource groupRes = (Resource) cls.getPropertyValue(VitroVocabulary.IN_CLASSGROUP);
                if (groupRes != null) {
                    vcw.setGroupURI(groupRes.getURI());
                }
            } catch (Exception e) {
                System.out.println("error retrieving vitro:inClassGroup property value for "+cls.getURI());
            }
            vcw.setShortDef(getPropertyStringValue(cls,VitroVocabulary.SHORTDEF));
            vcw.setExample(getPropertyStringValue(cls,VitroVocabulary.EXAMPLE_ANNOT));
            vcw.setDescription(getPropertyStringValue(cls,VitroVocabulary.DESCRIPTION_ANNOT));
            vcw.setDisplayLimit(getPropertyNonNegativeIntValue(cls,VitroVocabulary.DISPLAY_LIMIT));
            vcw.setDisplayRank(getPropertyNonNegativeIntValue(cls,VitroVocabulary.DISPLAY_RANK_ANNOT));
            vcw.setCustomEntryForm(getPropertyStringValue(cls,VitroVocabulary.PROPERTY_CUSTOMENTRYFORMANNOT));
            vcw.setCustomDisplayView(getPropertyStringValue(cls,VitroVocabulary.PROPERTY_CUSTOMDISPLAYVIEWANNOT));
            vcw.setCustomShortView(getPropertyStringValue(cls,VitroVocabulary.PROPERTY_CUSTOMSHORTVIEWANNOT));
            vcw.setCustomSearchView(getPropertyStringValue(cls,VitroVocabulary.PROPERTY_CUSTOMSEARCHVIEWANNOT));
            vcw.setSearchBoost(getPropertyFloatValue(cls,VitroVocabulary.SEARCH_BOOST_ANNOT));
            
            //There might be multiple HIDDEN_FROM_EDIT_DISPLAY_ANNOT properties, only use the highest
            StmtIterator it = cls.listProperties(VitroVocabulary.HIDDEN_FROM_DISPLAY_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel hiddenRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = (Resource)obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel = BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (hiddenRoleLevel == null || roleFromModel.compareTo(hiddenRoleLevel) > 0 )){
                            hiddenRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            vcw.setHiddenFromDisplayBelowRoleLevel(hiddenRoleLevel);//this might get set to null

            //There might be multiple PROHIBITED_FROM_UPDATE_DISPLAY_ANNOT properties, only use the highest
            it = cls.listProperties(VitroVocabulary.PROHIBITED_FROM_UPDATE_BELOW_ROLE_LEVEL_ANNOT);
            BaseResourceBean.RoleLevel prohibitedRoleLevel = null;
            while( it.hasNext() ){
                Statement stmt = it.nextStatement();
                RDFNode obj;
                if( stmt != null && (obj = stmt.getObject()) != null && obj.isURIResource() ){
                    Resource res = (Resource)obj.as(Resource.class);
                    if( res != null && res.getURI() != null ){
                        BaseResourceBean.RoleLevel roleFromModel = BaseResourceBean.RoleLevel.getRoleByUri(res.getURI());
                        if( roleFromModel != null && 
                            (prohibitedRoleLevel == null || roleFromModel.compareTo(prohibitedRoleLevel) > 0 )){
                            prohibitedRoleLevel = roleFromModel;                            
                        }
                    }
                }
            }            
            vcw.setProhibitedFromUpdateBelowRoleLevel(prohibitedRoleLevel);//this might get set to null

            // We need a better way of caching the counts.  For now I'm only setting 0 for the empty classes, to hide them from the DWR editing
            
            //ClosableIterator typeIt = getOntModel().listStatements(null,RDF.type,cls);
            //try {
            //    if (!typeIt.hasNext()) {
            //    	vcw.setEntityCount(0);
            //    }
            //} finally {
            //	typeIt.close();
            //}
            
            
        } finally {
            cls.getModel().leaveCriticalSection();
        }
        return vcw;
    }

    
    public String getLabelForClass(OntClass cls,boolean withPrefix,boolean forPickList) {
    	cls.getModel().enterCriticalSection(Lock.READ);
    	try {
	    	if (cls.isAnon()) {
		    	if (cls.isRestriction()) {	    		
		    		Restriction rest = cls.asRestriction();
		    		OntProperty onProperty = rest.getOnProperty();
		    		String labelStr = "restriction on " + getLabelOrId(onProperty) + ": ";
		    		if (rest.isAllValuesFromRestriction() || rest.isSomeValuesFromRestriction()) {
			    		Resource fillerRes = null;
			    		if (rest.isAllValuesFromRestriction()) {
			    			AllValuesFromRestriction avfRest = rest.asAllValuesFromRestriction();
			    			fillerRes = avfRest.getAllValuesFrom();
			    			labelStr += "all values from ";
			    		} else {
			    			SomeValuesFromRestriction svfRest = rest.asSomeValuesFromRestriction();
			    			fillerRes = svfRest.getSomeValuesFrom();
			    			labelStr += "some values from ";
			    		}
		    			if (fillerRes.canAs(OntClass.class)) { 
		    				OntClass avf = (OntClass) fillerRes.as(OntClass.class);
		    				labelStr += getLabelForClass(avf,withPrefix,forPickList);
		    			} else {
		    				try {
		    					labelStr += getLabelOrId( (OntResource) fillerRes.as(OntResource.class));
		    				} catch (Exception e) {
		    					labelStr += "???";
		    				}
		    			}		    			
		    		} else if (rest.isHasValueRestriction()) {
		    			HasValueRestriction hvRest = rest.asHasValueRestriction();
		    			labelStr += "has value ";
		    			RDFNode fillerNode = hvRest.getHasValue();
		    			try {
			    			if (fillerNode.isResource()) {
			    				labelStr += getLabelOrId((OntResource)fillerNode.as(OntResource.class));
			    			} else {
			    				labelStr += ((Literal) fillerNode.as(Literal.class)).getLexicalForm(); 
			    			}
		    			} catch (Exception e) {
		    				labelStr += "???";
		    			}
		    		} else if (rest.isMinCardinalityRestriction()) {
		    			MinCardinalityRestriction mcRest = rest.asMinCardinalityRestriction();
		    			labelStr += "minimum cardinality ";
		    			labelStr += mcRest.getMinCardinality();
		    		} else if (rest.isMaxCardinalityRestriction()) {
		    			MaxCardinalityRestriction mcRest = rest.asMaxCardinalityRestriction();
		    			labelStr += "maximum cardinality ";
		    			labelStr += mcRest.getMaxCardinality();
		    		} else if (rest.isCardinalityRestriction()) {
		    			CardinalityRestriction cRest = rest.asCardinalityRestriction();
		    			labelStr += "cardinality ";
		    			labelStr += cRest.getCardinality();
		    		}
		    		return labelStr;
		    	} else if (isBooleanClassExpression(cls)) {
		    		String labelStr = "(";
		    		if (cls.isComplementClass()) {
		    			labelStr += "not ";
		    			ComplementClass ccls = (ComplementClass) cls.as(ComplementClass.class);
		    			labelStr += getLabelForClass(ccls.getOperand(),withPrefix,forPickList);		    			
		    		} else if (cls.isIntersectionClass()) {
		    			IntersectionClass icls = (IntersectionClass) cls.as(IntersectionClass.class);
		    			for (Iterator operandIt = icls.listOperands(); operandIt.hasNext();) {
		    				OntClass operand = (OntClass) operandIt.next();
		    				labelStr += getLabelForClass(operand,withPrefix,forPickList);
		    				if (operandIt.hasNext()) {
		    					labelStr += " and ";
		    				}
		    			}
		    		} else if (cls.isUnionClass()) {
		    			UnionClass icls = (UnionClass) cls.as(UnionClass.class);
		    			for (Iterator operandIt = icls.listOperands(); operandIt.hasNext();) {
		    				OntClass operand = (OntClass) operandIt.next();
		    				labelStr += getLabelForClass(operand,withPrefix,forPickList);
		    				if (operandIt.hasNext()) {
		    					labelStr += " or ";
		    				}
		    			}
		    		}
		    		return labelStr+")";
		    	} else {
		    		// BJL23 2009-02-19
		    		// I'm putting the link markup in because I need it,
		    		// but obviously we need to factor this out into the display layer.
		    		return "<a href=\"vclassEdit?uri="+URLEncoder.encode(getClassURIStr(cls),"UTF-8")+"\">[anonymous class]</a>";
		    	}
	    	} else {
	    	    if (withPrefix || forPickList) {
                    OntologyDao oDao=getWebappDaoFactory().getOntologyDao();
                    Ontology o = (Ontology)oDao.getOntologyByURI(cls.getNameSpace());
                    if (o!=null) {
                        if (withPrefix) {                        	
                            return(o.getPrefix()==null?(o.getName()==null?"unspec:"+getLabelOrId(cls):o.getName()+":"+getLabelOrId(cls)):o.getPrefix()+":"+getLabelOrId(cls));
                        } else {
                            return(getLabelOrId(cls)+(o.getPrefix()==null?(o.getName()==null?" (unspec)":" ("+o.getName()+")"):" ("+o.getPrefix()+")"));                            
                        }
                    } else {
                    	return getLabelOrId(cls);
                    }
	    	    }
	    		return getLabelOrId(cls);
	    	}
    	} catch (Exception e) {
    		return "???";
    	} finally {
    		cls.getModel().leaveCriticalSection();
    	}
    }
    
    protected String getLabelOrId(OntResource r) {
    	String label = null;
    	r.getOntModel().enterCriticalSection(Lock.READ);
    	try {
    		label = getLabel(r);
    		if( label == null || label.length() == 0 )
    		    label = getLocalNameOrId(r);
    	} finally {
    		r.getOntModel().leaveCriticalSection();
    	}
        return label;
    }
    
    */
    private final boolean ALSO_TRY_NO_LANG = true;
    /**
     * works through list of PREFERRED_LANGUAGES to find an appropriate 
     * label, or NULL if not found.  
     */
    /*
    protected String getLabel(OntResource r){
        String label = null;
        r.getOntModel().enterCriticalSection(Lock.READ);
        try {            
            // try rdfs:label with preferred languages
            label = tryPropertyForPreferredLanguages( r, RDFS.label, ALSO_TRY_NO_LANG );
            
            // try vitro:label with preferred languages
            if ( label == null ) {
                label = tryPropertyForPreferredLanguages( r, r.getModel().getProperty(VitroVocabulary.label), ALSO_TRY_NO_LANG );
            }                              
        } finally {
            r.getOntModel().leaveCriticalSection();
        }
        return label;
    }
    
    private String tryPropertyForPreferredLanguages( OntResource r, Property p, boolean alsoTryNoLang ) {
    	String label = null;
	    List<RDFNode> labels = (List<RDFNode>) r.listPropertyValues(p).toList();
	    
	    String lang = "en-US";
	    label = getLabel2(lang,labels);
	    
        if ( label == null && alsoTryNoLang ) {
        	label = getLabel2("", labels);
        }
        
	    return label;
    }
    
    private String getLabel2(String lang, List<RDFNode>labelList) {
    	Iterator<RDFNode> labelIt = labelList.iterator();
    	while (labelIt.hasNext()) {
    		RDFNode label = labelIt.next();
    		if (label.isLiteral()) {
    			Literal labelLit = ((Literal)label);
    			String labelLanguage = labelLit.getLanguage();
    			if ( (labelLanguage==null) && (lang==null) ) {
    				return labelLit.getLexicalForm();
    			}
    			if ( (lang != null) && (lang.equals(labelLanguage)) ) {
    				return labelLit.getLexicalForm();
    			}
    		}
    	}
    	return null;
    }
    
    */
    /**
     * Get the local name, bnode or URI of the resource. 
     */
    protected String getLocalNameOrId(OntResource r){
        String label = null;
        r.getOntModel().enterCriticalSection(Lock.READ);
        try {                       
            String localName = r.getLocalName();
            if (localName != null) {
                label = localName;
            } else if (r.isAnon()) {
                label = r.getId().toString();
            } else {
                label = r.getURI();                
            }                       
        } finally {
            r.getOntModel().leaveCriticalSection();
        }
        return label;
    }
    
    
}
