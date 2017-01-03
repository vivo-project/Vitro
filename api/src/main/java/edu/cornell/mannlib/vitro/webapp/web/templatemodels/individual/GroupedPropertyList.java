/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.web.templatemodels.individual;

import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.LanguageOption.LANGUAGE_NEUTRAL;
import static edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess.PolicyOption.POLICY_NEUTRAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Property;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyGroup;
import edu.cornell.mannlib.vitro.webapp.beans.PropertyInstance;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.dao.DataPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.FauxPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyDao.FullPropertyKey;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyGroupDao;
import edu.cornell.mannlib.vitro.webapp.dao.PropertyInstanceDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.WebappDaoFactory;
import edu.cornell.mannlib.vitro.webapp.modelaccess.ModelAccess;
import edu.cornell.mannlib.vitro.webapp.utils.ApplicationConfigurationOntologyUtils;
import edu.cornell.mannlib.vitro.webapp.web.templatemodels.BaseTemplateModel;

/*
 public class GroupedPropertyList extends ArrayList<PropertyGroupTemplateModel> {
 If this class extends a List type, Freemarker does not let the templates call methods
 on it. Since the class must then contain a list rather than be a list, the template
 syntax is less idiomatic: e.g., groups.all rather than simply groups. An alternative
 is to make the get methods (getProperty and getPropertyAndRemoveFromList) methods
 of the IndividualTemplateModel. Then this class doesn't need methods, and can extend
 a List type.
 */
public class GroupedPropertyList extends BaseTemplateModel {

    private static final Log log = LogFactory.getLog(GroupedPropertyList.class);
    private static final int MAX_GROUP_DISPLAY_RANK = 99;

    @SuppressWarnings("serial")
    protected static final List<String> VITRO_PROPS_TO_ADD_TO_LIST = new ArrayList<String>() {
        {
            add(VitroVocabulary.IND_MAIN_IMAGE);
        }
    };

    private final Individual subject;
    private final VitroRequest vreq;
    private final WebappDaoFactory wdf;

    private List<PropertyGroupTemplateModel> groups;

    GroupedPropertyList(Individual subject, VitroRequest vreq,
            boolean editing) {
        this.vreq = vreq;
        this.subject = subject;
        this.wdf = vreq.getWebappDaoFactory();

        // Create the property list for the subject. The properties will be put
        // into groups later.
        List<Property> propertyList = new ArrayList<Property>();

        // First get all the object properties that occur in statements in the
        // db with this subject as subject.
        // This may include properties that are not defined as
        // "possible properties" for a subject of this class,
        // so we cannot just rely on getting that list.
        List<ObjectProperty> populatedObjectPropertyList = subject
                .getPopulatedObjectPropertyList();
         
        Map<String, List<String>> populatedObjTypes = makePopulatedObjTypeMap(
                populatedObjectPropertyList);
        
        // save applicable ranges before deduping to filter later 
        populatedObjectPropertyList = dedupe(populatedObjectPropertyList);
                
        Collection<ObjectProperty> additions = ApplicationConfigurationOntologyUtils
                .getAdditionalFauxSubpropertiesForList(
                        populatedObjectPropertyList, subject, vreq);
        
        additions = filterAdditions(additions, populatedObjTypes);
         
        if (log.isDebugEnabled()) {
            for (ObjectProperty t : additions) {
                log.debug("addition: " + t);
            }
            log.debug("Added " + additions.size() + 
                    " properties due to application configuration ontology");
        }
        
        populatedObjectPropertyList.addAll(additions);
        
        propertyList.addAll(populatedObjectPropertyList);
        
        // If editing this page, merge in object properties applicable to the individual that are currently
        // unpopulated, so the properties are displayed to allow statements to be added to these properties.
        // RY In future, we should limit this to properties that the user has permission to add properties to.
        if (editing) {
            propertyList = mergeAllPossibleObjectProperties(populatedObjectPropertyList, propertyList);
        }
        
        // Now do much the same with data properties: get the list of populated data properties, then add in placeholders for missing ones 
        // rjy7 Currently we are getting the list of properties in one sparql query, then doing a separate query
        // to get values for each property. This could be optimized by doing a single query to get a map of properties to 
        // DataPropertyStatements. Note that this does not apply to object properties, because the queries
        // can be customized and thus differ from property to property. So it's easier for now to keep the
        // two working in parallel.
        List<DataProperty> populatedDataPropertyList = subject
                .getPopulatedDataPropertyList();
        propertyList.addAll(populatedDataPropertyList);
        
        if (editing) {
            mergeAllPossibleDataProperties(propertyList);
        }
        sort(propertyList);

        // Put the list into groups
        List<PropertyGroup> propertyGroupList = addPropertiesToGroups(propertyList);
        
        // Build the template data model from the groupList
        groups = new ArrayList<PropertyGroupTemplateModel>(
                propertyGroupList.size());
        for (PropertyGroup propertyGroup : propertyGroupList) {
            groups.add(new PropertyGroupTemplateModel(vreq, propertyGroup,
                    subject, editing, populatedDataPropertyList,
                    populatedObjectPropertyList));
        }
        
        if (!editing) {
            pruneEmptyProperties();
        }

    }
    
    private Map<String, List<String>> makePopulatedObjTypeMap(List<ObjectProperty> props) {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        for (ObjectProperty prop : props) {
            if(prop.getRangeVClassURI() != null) {
                List<String> typeList = map.get(prop.getURI());
                if(typeList == null) {
                    typeList = new ArrayList<String>();
                    map.put(prop.getURI(), typeList);
                } 
                typeList.add(prop.getRangeVClassURI());                
            }
        }
        return map;
    }
    
    private List<ObjectProperty> filterAdditions(Collection<ObjectProperty> additions, 
            Map<String, List<String>> populatedObjTypes) {
        List<ObjectProperty> filteredAdditions = new ArrayList<ObjectProperty>();
        for (ObjectProperty prop : additions) {
            List<String> allowedTypes = populatedObjTypes.get(prop.getURI());
            if(allowedTypes != null && (allowedTypes.contains(prop.getRangeVClassURI()) 
                    || allowedTypes.contains(prop.getRangeEntityURI()) ) ) {
                filteredAdditions.add(prop);
            }
        }
        return filteredAdditions;
    }
    
    // It's possible that an object property retrieved in the call to getPopulatedObjectPropertyList()
    // is now empty of statements, because if not editing, some statements without a linked individual
    // are not retrieved by the query. (See <linked-individual-required> elements in queries.)
    // Remove these properties, and also remove any groups with no remaining properties. 
    private void pruneEmptyProperties() {
        Iterator<PropertyGroupTemplateModel> iGroups = groups.iterator();
        while (iGroups.hasNext()) {
            PropertyGroupTemplateModel pgtm = iGroups.next();
            Iterator<PropertyTemplateModel> iProperties = pgtm.getProperties()
                    .iterator();
            while (iProperties.hasNext()) {
                PropertyTemplateModel property = iProperties.next();
                if (property instanceof ObjectPropertyTemplateModel) {
                    // It's not necessary to do comparable pruning of the subclass list
                    // of a CollatedObjectPropertyTemplateModel, because the collated subclass
                    // list is compiled on the basis of existing statements. There will not
                    // be any empty subclasses.
                    if (((ObjectPropertyTemplateModel) property).isEmpty()) {
                        iProperties.remove();
                    }
                }
            }
            if (pgtm.isEmpty()) {
                iGroups.remove();
            }
        }
    }
    
    //assumes sorted list 
    protected List<ObjectProperty> dedupe(List<ObjectProperty> propList) {
        List<ObjectProperty> dedupedList = new ArrayList<ObjectProperty>();
        String uriRegister = "";
        for (ObjectProperty prop : propList) {
            if(!uriRegister.equals(prop.getURI())) {
                uriRegister = prop.getURI();
                dedupedList.add(prop);
            }
        }
        return dedupedList;
    }

    protected void sort(List<Property> propertyList) {
        try {
            Collections.sort(propertyList, new PropertyRanker(vreq));
        } catch (Exception ex) {
            log.error("Exception sorting merged property list: "
                    + ex.getMessage());
        }
    }

    private List<Property> mergeAllPossibleObjectProperties(
            List<ObjectProperty> populatedObjectPropertyList,
            List<Property> propertyList) {

        // There is no ObjectPropertyDao.getAllPossibleObjectPropertiesForIndividual() parallel to 
        // DataPropertyDao.getAllPossibleDatapropsForIndividual(). The comparable method for object properties
        // is defined using PropertyInstance rather than ObjectProperty.
        
        // Getting Language-neutral WebappDaoFactory because the language-filtering 
    	// breaks blank node structures in the restrictions that determine applicable properties.
        WebappDaoFactory wadf = vreq.getLanguageNeutralWebappDaoFactory();
        PropertyInstanceDao piDao = wadf.getPropertyInstanceDao();
        
        Collection<PropertyInstance> allPossiblePI = piDao
                .getAllPossiblePropInstForIndividual(subject.getURI());
        if (allPossiblePI != null) {
            for (PropertyInstance possiblePI : allPossiblePI) {
                if (possiblePI != null) {
                    // use the language-aware wdf because redundancy check
                    // for display will depend on public label match
                    ObjectProperty possibleOP = assembleObjectProperty(possiblePI);
                    if (possibleOP == null) {
                        continue;
                    }
                    boolean addToList = true;
                    for(ObjectProperty populatedOP : populatedObjectPropertyList) {
                    	if (redundant(populatedOP, possibleOP)) {
                    		addToList = false;
                    	}
                    }
                    if(addToList) {
                        propertyList.add(possibleOP);         
                    }
                } else {
                    log.error("a property instance in the Collection created by PropertyInstanceDao.getAllPossiblePropInstForIndividual() is unexpectedly null");
                }
            }
        } else {
            log.error("a null Collection is returned from PropertyInstanceDao.getAllPossiblePropInstForIndividual()");
        }

        // These properties are outside the ontologies (in vitro and vitro public) but need to be added to the list.
        // In future, vitro ns props will be phased out. Vitro public properties should be changed so they do not
        // constitute a special case (i.e., included in piDao.getAllPossiblePropInstForIndividual()).
        for (String propertyUri : VITRO_PROPS_TO_ADD_TO_LIST) {
            if (!alreadyOnPropertyList(propertyList, propertyUri)) {
                addObjectPropertyToPropertyList(propertyUri, null, null, propertyList);
            }
        }
        
        return propertyList;
    }

	private ObjectProperty assembleObjectProperty(PropertyInstance pi) {
		WebappDaoFactory rawWadf = ModelAccess.on(vreq).getWebappDaoFactory(
				LANGUAGE_NEUTRAL, POLICY_NEUTRAL);
		ObjectPropertyDao opDao = rawWadf.getObjectPropertyDao();
		FauxPropertyDao fpDao = rawWadf.getFauxPropertyDao();
		
		String base = pi.getPropertyURI();
		String domain = pi.getDomainClassURI();
		String range = pi.getRangeClassURI();

		ObjectProperty op = opDao.getObjectPropertyByURIs(base, domain, range);
		try {
			FauxProperty fp = fpDao.getFauxPropertyByUris(domain, base, range);
			if (fp != null) {
				return new FauxObjectPropertyWrapper(op, fp);
			}
		} catch (Exception e) {
			log.warn("Couldn't look up the faux property", e);
		}
		return op;
	}
    
	/**
	* Don't know what the real problem is with VIVO-976, but somehow we have the same property 
	* showing up once with a blank node as a domain, and once with null or OWL:Thing as a domain.
	* 
	* Similarly, don't know the real problem with VIVO-989, except that the ranges are both
	* blank nodes - probably the same blank node but on two different reads.
	* 
	* For VIVO-1015, if op2 (the unpopulated property) is a Faux property, it will appear to 
	* be not redundant because of the range difference, and that's what we want. But if op2
	* (the unpopulated property) has a different range than op1 because of a restriction, 
	* then we want to ignore that difference, so it appears to be redundant.
	*/
	private boolean redundant(ObjectProperty populatedOP, ObjectProperty possibleOP) {
		if (new FullPropertyKey((Property)populatedOP).equals(
			new FullPropertyKey((Property)possibleOP))) {
			return true;
		} else if (
			new FullPropertyKey(fudgeBlankNodeInDomain(populatedOP.getDomainVClassURI()), 
								populatedOP.getURI(),
								populatedOP.getRangeVClassURI()).equals(
			new FullPropertyKey(fudgeBlankNodeInDomain(possibleOP.getDomainVClassURI()), 
								possibleOP.getURI(),
								possibleOP.getRangeVClassURI()))) {
			return true;
		} else if (
			new FullPropertyKey(populatedOP.getDomainVClassURI(), 
								populatedOP.getURI(),
								fudgeBlankNodeInRange(populatedOP.getRangeVClassURI())).equals(
			new FullPropertyKey(possibleOP.getDomainVClassURI(), 
								possibleOP.getURI(),
								fudgeBlankNodeInRange(possibleOP.getRangeVClassURI())))) {
			return true;
		} else if (!(possibleOP instanceof FauxObjectPropertyWrapper) && 
			         populatedOP.getURI().equals(possibleOP.getURI())) { // If not faux property, ignore range difference
			return true;
		} else {
			return false;
		}
    }
	
	private String fudgeBlankNodeInDomain(String rawDomainUri) {
		if (rawDomainUri == null) {
			return null;
		} else if (rawDomainUri.contains("http://vitro.mannlib.cornell.edu/ns/bnode#")) {
			return OWL.Thing.getURI();
		} else {
			return rawDomainUri;
		}
	}

	private String fudgeBlankNodeInRange(String rawRangeUri) {
		if (rawRangeUri == null) {
			return null;
		} else if (rawRangeUri.contains("http://vitro.mannlib.cornell.edu/ns/bnode#")) {
			return "http://vitro.mannlib.cornell.edu/ns/bnode#-deadbeef";
		} else {
			return rawRangeUri;
		}
	}
	
    private void addObjectPropertyToPropertyList(String propertyUri, String domainUri, String rangeUri,
            List<Property> propertyList) {
        ObjectPropertyDao opDao = wdf.getObjectPropertyDao();
        ObjectProperty op = opDao.getObjectPropertyByURIs(propertyUri, domainUri, rangeUri);
        if (op == null) {
            log.error("ObjectProperty op returned null from opDao.getObjectPropertyByURI(" + propertyUri + ")");
        } else if (op.getURI() == null) {
            log.error("ObjectProperty op returned with null propertyURI from opDao.getObjectPropertyByURI()");
        } else {
            propertyList.add(op);
        }
    }

    protected void mergeAllPossibleDataProperties(List<Property> propertyList) {
        // see comments in mergeAllPossibleObjectProperties() for the reason
        // that we need a neutral WebappDaoFactory here.
        DataPropertyDao dpDao = vreq.getLanguageNeutralWebappDaoFactory().getDataPropertyDao();
        Collection<DataProperty> allDatapropColl = dpDao
                .getAllPossibleDatapropsForIndividual(subject.getURI());
        if (allDatapropColl != null) {
            for (DataProperty dp : allDatapropColl) {
                if (dp != null) {
                    if (dp.getURI() == null) {
                        log.error("DataProperty dp returned with null propertyURI from dpDao.getAllPossibleDatapropsForIndividual()");
                    } else if (!alreadyOnPropertyList(propertyList, dp)) {
                        propertyList.add(dp);
                    }
                } else {
                    log.error("a data property in the Collection created in DataPropertyDao.getAllPossibleDatapropsForIndividual() is unexpectedly null)");
                }
            }
        } else {
            log.error("a null Collection is returned from DataPropertyDao.getAllPossibleDatapropsForIndividual())");
        }
    }

    private boolean alreadyOnPropertyList(List<Property> propertyList,
            Property p) {
        if (p.getURI() == null) {
            log.error("Property p has no propertyURI in alreadyOnPropertyList()");
            return true; // don't add to list
        }
        return (alreadyOnPropertyList(propertyList, p.getURI()));
    }

    private boolean alreadyOnPropertyList(List<Property> propertyList,
            String propertyUri) {
        for (Property p : propertyList) {
            String uri = p.getURI();
            if (uri != null && uri.equals(propertyUri)) {
                return true;
            }
        }
        return false;
    }

    private List<PropertyGroup> addPropertiesToGroups(
            List<Property> propertyList) {

        // Get the property groups
        PropertyGroupDao pgDao = wdf.getPropertyGroupDao();
        long start = System.currentTimeMillis();
        List<PropertyGroup> groupList = pgDao.getPublicGroups(false); // may be returned empty but not null
        // To test no property groups defined, use:
        // List<PropertyGroup> groupList = new ArrayList<PropertyGroup>();

        start = System.currentTimeMillis();
        int groupCount = groupList.size();

        /*
         * If no property groups are defined, create a dummy group with a null
         * name to signal to the template that it's not a real group. This
         * allows the looping structure in the template to be the same whether
         * there are groups or not.
         */
        if (groupCount == 0) {
            log.warn("groupList has no groups on entering addPropertiesToGroups(); creating a dummy group");
            PropertyGroup dummyGroup = pgDao.createDummyPropertyGroup(null, 1);
            dummyGroup.getPropertyList().addAll(propertyList);
            groupList.add(dummyGroup);
            return groupList;
        }

        /*
         * This group will hold properties that are not assigned to any groups.
         * In case no real property groups are populated, we end up with the
         * dummy group case above, and we will change the name to null to signal
         * to the template that it shouldn't be treated like a group.
         */
        PropertyGroup groupForUnassignedProperties = pgDao
                .createDummyPropertyGroup("", MAX_GROUP_DISPLAY_RANK);
                
        if (groupCount > 1) {
            try {
                Collections.sort(groupList);
            } catch (Exception ex) {
                log.error("Exception on sorting groupList in addPropertiesToGroups()");
            }
        }

        populateGroupListWithProperties(groupList,
                groupForUnassignedProperties, propertyList);

        // Remove unpopulated groups
        try {
            int removedCount = pgDao.removeUnpopulatedGroups(groupList);
            if (removedCount == 0) {
                log.warn("Of " + groupCount
                        + " groups, none removed by removeUnpopulatedGroups");
            }
            groupCount -= removedCount;
        } catch (Exception ex) {
            log.error("Exception on trying to prune unpopulated groups from group list: "
                    + ex.getMessage());
        }
        log.debug(System.currentTimeMillis() - start + " to remove unpopulated groups");

        // If the group for unassigned properties is populated, add it to the
        // group list.
        if (groupForUnassignedProperties.getPropertyList().size() > 0) {
            groupList.add(groupForUnassignedProperties);
            // If no real property groups are populated, the groupForUnassignedProperties 
            // moves from case 2 to case 1 above, so change the name to null
            // to signal to the templates that there are no real groups.
            if (groupCount == 0) {
                groupForUnassignedProperties.setName(null);
            }
        }

        return groupList;
    }

    private void populateGroupListWithProperties(List<PropertyGroup> groupList,
            PropertyGroup groupForUnassignedProperties,
            List<Property> propertyList) {

        // Clear out the property lists on the groups
        for (PropertyGroup pg : groupList) {
        	if (pg.getPropertyList() == null) {
        		pg.setPropertyList(new ArrayList<Property>());
        	} else if (pg.getPropertyList().size() > 0) {
                pg.getPropertyList().clear();
            }
        }

        // Assign the properties to the groups
        for (Property p : propertyList) {
            if (p.getURI() == null) {
                log.error("Property p has null URI in populateGroupListWithProperties()");
                // If the property is not assigned to any group, add it to the
                // group for unassigned properties
            } else {
                String groupUriForProperty = p.getGroupURI();
                boolean assignedToGroup = false;
                if (groupUriForProperty != null) {
                    for (PropertyGroup pg : groupList) {
                        String groupUri = pg.getURI();
                        if (groupUriForProperty.equals(groupUri)) {
                            pg.getPropertyList().add(p);
                            assignedToGroup = true;
                            break;
                        }     
                    }
                }

                // Either the property is not assigned to a group, or it is assigned to a group
                // not in the list (i.e., a non-existent group).
                if (!assignedToGroup) {
                    if (groupForUnassignedProperties != null) {
                        groupForUnassignedProperties.getPropertyList().add(p);
                        log.debug("adding property " + getLabel(p)
                                + " to group for unassigned properties");
                    }
                }
            }
        }
    }

    private class PropertyRanker implements Comparator<Property> {

        WebappDaoFactory wdf;
        PropertyGroupDao pgDao;

        private PropertyRanker(VitroRequest vreq) {
            this.wdf = vreq.getWebappDaoFactory();
            this.pgDao = wdf.getPropertyGroupDao();
        }

        public int compare(Property p1, Property p2) {

            int diff = determineDisplayRank(p1) - determineDisplayRank(p2);
            if (diff == 0) {
                return getLabel(p1).compareTo(getLabel(p2));
            } else {
                return diff;
            }
        }

        private int determineDisplayRank(Property p) {
            if (p instanceof DataProperty) {
                DataProperty dp = (DataProperty) p;
                return dp.getDisplayTier();
            } else if (p instanceof ObjectProperty) {
                ObjectProperty op = (ObjectProperty) p;
                return op.getDomainDisplayTier();
            } else {
                log.error("Property is of unknown class in PropertyRanker()");
            }
            return 0;
        }
    }

    // Since we're now including some vitro properties in the property list, 
    // which don't have labels, use their local name instead.
    private String getLabel(Property property) {
        String label = property.getLabel();
        if (label == null) {
            label = property.getLocalName();
        }
        return label;
    }

    /* Template properties */

    public List<PropertyGroupTemplateModel> getAll() {
        return groups;
    }
    
    
    /* Template methods */

    public PropertyTemplateModel getProperty(String propertyUri) {

        for (PropertyGroupTemplateModel pgtm : groups) {
            List<PropertyTemplateModel> properties = pgtm.getProperties();
            for (PropertyTemplateModel ptm : properties) {
                if (propertyUri.equals(ptm.getUri())) {
                    return ptm;
                }
            }
        }
        return null;
    }

    public PropertyTemplateModel getProperty(String propertyUri, String rangeUri) {

        for (PropertyGroupTemplateModel pgtm : groups) {
            List<PropertyTemplateModel> properties = pgtm.getProperties();
            for (PropertyTemplateModel ptm : properties) {
                if (propertyUri.equals(ptm.getUri()) && rangeUri.equals(ptm.getRangeUri())) {
                    return ptm;
                }
            }
        }
        return null;
    }

    public PropertyTemplateModel pullProperty(String propertyUri) {
        return pullProperty(propertyUri, null);
    }
    
    public PropertyTemplateModel pullProperty(String propertyUri, String rangeUri) {

        for (PropertyGroupTemplateModel pgtm : groups) {
            List<PropertyTemplateModel> properties = pgtm.getProperties();
            for (PropertyTemplateModel ptm : properties) {
                if (propertyUri.equals(ptm.getUri()) && 
                        (rangeUri == null || rangeUri.equals(ptm.getRangeUri()))) {
                    // Remove the property from the group.
                    // NB Works with a for-each loop instead of an iterator,
                    // since iteration doesn't continue after the remove.
                    properties.remove(ptm);
                    // If this is the only property in the group, remove the group as well.
                    // NB Works with a for-each loop instead of an iterator, since
                    // iteration doesn't continue after the remove.
                    if (properties.size() == 0) {
                        groups.remove(pgtm);
                    }
                    return ptm;
                }
            }
        }
        return null;
    }
    
    public PropertyGroupTemplateModel pullPropertyGroup(String groupName) {
        Iterator<PropertyGroupTemplateModel> groupIt = groups.iterator();
        while (groupIt.hasNext()) {
            PropertyGroupTemplateModel group = groupIt.next();
            if (groupName.equals(group.getName())) {
                groups.remove(group);
                return group;
            }
        }
        return null;
    }
}
