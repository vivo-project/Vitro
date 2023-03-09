/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.IND_MAIN_IMAGE;
import static edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary.PSEUDO_BNODE_NS;

import java.sql.Timestamp;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;
import org.apache.jena.vocabulary.OWL;

import edu.cornell.mannlib.vitro.webapp.beans.DataProperty;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.IndividualImpl;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.VClassDao;
import edu.cornell.mannlib.vitro.webapp.filestorage.model.ImageInfo;

public class IndividualJena extends IndividualImpl implements Individual {

    private static final Log LOG = LogFactory.getLog(IndividualJena.class.getName());

    private boolean retrievedNullRdfsLabel = false;

    private OntResource ind = null;
    private WebappDaoFactoryJena wadf = null;

    public IndividualJena(OntResource ind, WebappDaoFactoryJena wadf) {
        this.ind = ind;
        this.wadf = wadf;

        setupURIParts(this.ind);
    }

    @Override
    public String getName() {
        if (this.name != null) {
            return name;
        }
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            this.name = wadf.getJenaBaseDao().getLabelOrId(ind);
            if (this.name == null) {
                this.name = "[null]";
            }
            return this.name;
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
    }

    @Override
    public String getLabel() {
        return getRdfsLabel();
    }

    @Override
    public String getRdfsLabel() {
        if (this.rdfsLabel != null) {
            return rdfsLabel;
        } 
        if (this.rdfsLabel == null && retrievedNullRdfsLabel) {
            return null;
        }
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            this.rdfsLabel = wadf.getJenaBaseDao().getLabel(ind);
            retrievedNullRdfsLabel = this.rdfsLabel == null;
            return this.rdfsLabel;
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
    }

    @Override
    public String getVClassURI() {
        if (this.vClassURI != null) {
            return vClassURI;
        }
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator<Resource> typeIt = ind.listRDFTypes(true);
            try {
                while (typeIt.hasNext()) {
                    Resource type = typeIt.next();
                    if (type.getNameSpace()!=null && (!wadf.getJenaBaseDao().NONUSER_NAMESPACES
                            .contains(type.getNameSpace()) || type.getURI()
                            .equals(OWL.Thing.getURI())) ) {

                        this.vClassURI=type.getURI();
                        break;
                    }
                }
            } finally {
                typeIt.close();
            }
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
        return this.vClassURI;
    }

    @Override
    public VClass getVClass() {
        if (this.vClass != null) {
            return this.vClass;
        }
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            ClosableIterator<Resource> typeIt = ind.listRDFTypes(true);
            try {
               while (typeIt.hasNext()) {
                   Resource type = (Resource) typeIt.next();
                   if (type.getNameSpace()!=null && (!wadf.getJenaBaseDao().NONUSER_NAMESPACES
                           .contains(type.getNameSpace()) || type.getURI()
                           .equals(OWL.Thing.getURI()))) {

                       this.vClassURI=type.getURI();
                       this.vClass = wadf.getVClassDao().getVClassByURI(this.vClassURI);
                       break;
                   }
               }
            } finally {
                typeIt.close();
            }
            return this.vClass;
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
    }

    @Override
    public Timestamp getModTime() {
        if (modTime != null) {
            return modTime;
        }
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            Date modDate = wadf.getJenaBaseDao().getPropertyDateTimeValue(ind,
                wadf.getJenaBaseDao().MODTIME);
            if (modDate != null) {
                modTime = new Timestamp(modDate.getTime());
            }
            return modTime;
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
    }

    @Override
    public Float getSearchBoost() {
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            searchBoost = ((Literal) ind
                .getPropertyValue(wadf.getJenaBaseDao().SEARCH_BOOST_ANNOT)).getFloat();
        } catch (Exception e) {
            LOG.debug(e);
            searchBoost = null;
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
        return searchBoost;
    }

    @Override
    public String getMainImageUri() {
        if (this.mainImageUri != NOT_INITIALIZED) {
            return mainImageUri;
        }
        for (ObjectPropertyStatement stmt : getObjectPropertyStatements()) {
            if (stmt.getPropertyURI().equals(IND_MAIN_IMAGE)) {
                // arbitrarily return the first value in the list.
                mainImageUri = stmt.getObjectURI();
                return mainImageUri;
            }
        }
        return null;
    }

    @Override
    public String getImageUrl() {
        if (this.imageInfo == null) {
            this.imageInfo = ImageInfo.instanceFromEntityUri(wadf, this);
            LOG.trace("figured imageInfo for " + getURI() + ": '" + this.imageInfo + "'");
        }
        if (this.imageInfo == null) {
            this.imageInfo = ImageInfo.EMPTY_IMAGE_INFO;
            LOG.trace("imageInfo for " + getURI() + " is empty.");
        }
        return this.imageInfo.getMainImage().getBytestreamAliasUrl();
    }

    @Override
    public String getThumbUrl() {
        if (this.imageInfo == null) {
            this.imageInfo = ImageInfo.instanceFromEntityUri(wadf, this);
            LOG.trace("figured imageInfo for " + getURI() + ": '" + this.imageInfo + "'");
        }
        if (this.imageInfo == null) {
            this.imageInfo = ImageInfo.EMPTY_IMAGE_INFO;
            LOG.trace("imageInfo for " + getURI() + " is empty.");
        }
        return this.imageInfo.getThumbnail().getBytestreamAliasUrl();
    }

    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements() {
        if (this.objectPropertyStatements != null) {
            return this.objectPropertyStatements;
        }
        try {
            wadf.getObjectPropertyStatementDao().fillExistingObjectPropertyStatements(this);
        } catch (Exception e) {
            LOG.error(this.getClass().getName()
                + " could not fill existing ObjectPropertyStatements for " + this.getURI(), e);
        }
        return this.objectPropertyStatements;
    }

    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements(String propertyURI) {
        if (propertyURI == null) {
            return null;
        }
        List<ObjectPropertyStatement> objectPropertyStatements = new ArrayList<>();
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            StmtIterator sit = ind.listProperties(ind.getModel().getProperty(propertyURI));
            while (sit.hasNext()) {
                Statement s = sit.nextStatement();
                if (!s.getSubject().canAs(OntResource.class) || !s.getObject().canAs(OntResource.class)) {
                    continue;
                }
                Individual subj = new IndividualJena(s.getSubject().as(OntResource.class), wadf);
                Individual obj = new IndividualJena(s.getObject().as(OntResource.class), wadf);
                ObjectProperty op = wadf.getObjectPropertyDao().getObjectPropertyByURI(s.getPredicate().getURI());
                if (subj != null && obj != null && op != null) {
                    ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
                    ops.setSubject(subj);
                    ops.setSubjectURI(subj.getURI());
                    ops.setObject(obj);
                    ops.setObjectURI(obj.getURI());
                    ops.setProperty(op);
                    ops.setPropertyURI(op.getURI());
                    objectPropertyStatements.add(ops);
                }
            }
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
        return objectPropertyStatements;
    }

    @Override
    public List<Individual> getRelatedIndividuals(String propertyURI) {
        if (propertyURI == null) {
            return null;
        }
        List<Individual> relatedIndividuals = new ArrayList<Individual>();
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            NodeIterator values = ind.listPropertyValues(ind.getModel().getProperty(propertyURI));
            while (values.hasNext()) {
                RDFNode value = values.nextNode();
                if (value.canAs(OntResource.class)) {
                    relatedIndividuals.add(new IndividualJena(value.as(OntResource.class), wadf));
                }
            }
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
        return relatedIndividuals;
    }

    @Override
    public Individual getRelatedIndividual(String propertyURI) {
        if (propertyURI == null) {
            return null;
        }
        ind.getOntModel().enterCriticalSection(Lock.READ);
        try {
            RDFNode value = ind.getPropertyValue(ind.getModel().getProperty(propertyURI));
            if (value != null && value.canAs(OntResource.class)) {
                return new IndividualJena(value.as(OntResource.class), wadf);
            }
            return null;
        } finally {
            ind.getOntModel().leaveCriticalSection();
        }
    }

    @Override
    public List<ObjectProperty> getObjectPropertyList() {
        if (this.propertyList != null) {
            return this.propertyList;
        }
        try {
            wadf.getObjectPropertyDao().fillObjectPropertiesForIndividual(this);
        } catch (Exception e) {
            LOG.error(this.getClass().getName() + " could not fillEntityProperties for "
                + this.getURI());
        }
        return this.propertyList;
    }

    @Override
    public List<ObjectProperty> getPopulatedObjectPropertyList() {
        if (populatedObjectPropertyList == null) {
            populatedObjectPropertyList = wadf.getObjectPropertyDao().getObjectPropertyList(this);
        }
        return populatedObjectPropertyList;
    }

    @Override
    public Map<String, ObjectProperty> getObjectPropertyMap() {
        if (this.objectPropertyMap != null) {
            return objectPropertyMap;
        }
        Map<String, ObjectProperty> map = new HashMap<>();
        if (this.propertyList == null) {
            getObjectPropertyList();
        }
        for (ObjectProperty op : this.propertyList) {
            if (op.getURI() != null) {
                map.put(op.getURI(), op);
            }
        }
        this.objectPropertyMap = map;
        return map;
    }

    @Override
    public List<DataPropertyStatement> getDataPropertyStatements() {
        if (this.dataPropertyStatements != null) {
            return this.dataPropertyStatements;
        }
        try {
            wadf.getDataPropertyStatementDao().fillExistingDataPropertyStatementsForIndividual(this/*,false*/);
        } catch (Exception e) {
            LOG.error(this.getClass().getName()
                + " could not fill existing DataPropertyStatements for " + this.getURI());
        }
        return this.dataPropertyStatements;
    }

    @Override
    public List getDataPropertyList() {
        if (this.datatypePropertyList != null) {
            return this.datatypePropertyList;
        }
        try {
            wadf.getDataPropertyDao().fillDataPropertiesForIndividual(this);
        } catch (Exception e) {
            LOG.error(this.getClass().getName() + " could not fill data properties for "
                + this.getURI());
        }
        return this.datatypePropertyList;
    }

    @Override
    public List<DataProperty> getPopulatedDataPropertyList() {
        if (populatedDataPropertyList == null) {
            populatedDataPropertyList = wadf.getDataPropertyDao().getDataPropertyList(this);
        }
        return populatedDataPropertyList;
    }

    @Override
    public Map<String, DataProperty> getDataPropertyMap() {
        if (this.dataPropertyMap != null) {
            return dataPropertyMap;
        }
        Map<String, DataProperty> map = new HashMap<>();
        if (this.datatypePropertyList == null) {
            getDataPropertyList();
        }
        for (DataProperty dp : this.datatypePropertyList) {
            if (dp.getURI() != null) {
                map.put(dp.getURI(), dp);
            }
        }
        this.dataPropertyMap = map;
        return map;
    }

    @Override
    public List<DataPropertyStatement> getExternalIds() {
        // BJL 2007-11-11: need to decide whether we want to use Collections or Lists in our interfaces - we seem to be leaning toward Lists
        if (this.externalIds != null) {
            return this.externalIds;
        }
        try {
            List<DataPropertyStatement> dpsList = new ArrayList<>();
            dpsList.addAll(wadf.getIndividualDao().getExternalIds(this.getURI(), null));
            this.externalIds = dpsList;
        } catch (Exception e) {
            LOG.error(this.getClass().getName() + " could not fill external IDs for "
                + this.getURI());
        }
        return this.externalIds;
    }

    @Override
    public List<VClass> getVClasses() {
    	return getVClasses(false);
    }

    @Override
    public List<VClass> getVClasses(boolean direct) {
        if (direct) {
            if (directVClasses != null) {
                return directVClasses;
            }
            directVClasses = getMyVClasses(true);
            return directVClasses;
        }
        if (allVClasses != null) {
            return allVClasses;
        }
        allVClasses = getMyVClasses(false);
        return allVClasses;
    }

    private List<VClass> getMyVClasses(boolean direct) {
        List<VClass> vClassList = new ArrayList<>();
        OntModel ontModel = ind.getOntModel();
        ontModel.enterCriticalSection(Lock.READ);
        try {
            ClosableIterator<Resource> typeIt = ind.listRDFTypes(direct);
            try {
                for (Iterator<Resource> it = typeIt; it.hasNext();) {
                    Resource type = typeIt.next();
                    String typeURI = (!type.isAnon())
                        ? type.getURI()
                        : PSEUDO_BNODE_NS + type.getId().toString();
                    if (type.getNameSpace() == null ||
                            (!wadf.getNonuserNamespaces().contains(type.getNameSpace()))) {
                        VClass vc = wadf.getVClassDao().getVClassByURI(typeURI);
                        if (vc != null) {
                            vClassList.add(vc);
                        }
                    }
                }
            } finally {
                typeIt.close();
            }
        } finally {
            ontModel.leaveCriticalSection();
        }
        try {
            Collections.sort(vClassList);
        } catch (Exception e) {}
        return vClassList;
    }

	/**
	 * The base method in {@link IndividualImpl} is adequate if the reasoner is
	 * up to date.
	 *
	 * If the base method returns false, check directly to see if
	 * any of the super classes of the direct classes will satisfy this request.
	 */
	@Override
	public boolean isVClass(String uri) {
    	if (uri == null) {
    		return false;
    	}

		if (super.isVClass(uri)) {
			return true;
		}

        VClassDao vclassDao = wadf.getVClassDao();
		for (VClass vClass : getVClasses(true)) {
			for (String superClassUri: vclassDao.getAllSuperClassURIs(vClass.getURI())) {
				if (uri.equals(superClassUri)) {
					return true;
				}
			}
		}
		return false;
	}

    /**
     * Overriding the base method so that we can do the sorting by arbitrary property here.  An
     * IndividualJena has a reference back to the model; everything else is just a dumb bean (for now).
     */
    @Override
    protected void sortEnts2EntsForDisplay(){
        if( getObjectPropertyList() == null ) return;

		for (ObjectProperty prop : getObjectPropertyList()) {
			/*  if (prop.getObjectIndividualSortPropertyURI()==null) {
            	prop.sortObjectPropertyStatementsForDisplay(prop,prop.getObjectPropertyStatements());
            } else {*/
			prop.sortObjectPropertyStatementsForDisplay(prop, prop.getObjectPropertyStatements());
        /*  }*/
		}
    }

    private Collator collator = Collator.getInstance();

    private void sortObjectPropertyStatementsForDisplay(ObjectProperty prop) {
        try {
            LOG.info("Doing special sort for "+prop.getDomainPublic());
            final String sortPropertyURI = prop.getObjectIndividualSortPropertyURI();
            String tmpDir;
            boolean tmpAsc;

            tmpDir = prop.getDomainEntitySortDirection();

            //valid values are "desc" and "asc", anything else will default to ascending
            tmpAsc = !"desc".equalsIgnoreCase(tmpDir);

            final boolean dir = tmpAsc;
            Comparator comp = new Comparator(){
                final boolean cAsc = dir;

                public final int compare(Object o1, Object o2){
                    ObjectPropertyStatement e2e1= (ObjectPropertyStatement)o1, e2e2=(ObjectPropertyStatement)o2;
                    Individual e1 , e2;
                    e1 = e2e1 != null ? e2e1.getObject():null;
                    e2 = e2e2 != null ? e2e2.getObject():null;

                    Object val1 = null, val2 = null;
                    if( e1 != null ){
                        try {
                        	DataProperty dp = e1.getDataPropertyMap().get(sortPropertyURI);
                        	if (dp.getDataPropertyStatements() != null && dp.getDataPropertyStatements().size()>0) {
                        		val1 = dp.getDataPropertyStatements().get(0).getData();
                        	}
                        }
                        catch (Exception e) {
                        	val1 = "";
                        }
                    } else {
                        LOG.warn( "IndividualJena.sortObjectPropertiesForDisplay passed object property statement with no range entity.");
                    }

                    if( e2 != null ){
                        try {
                        	DataProperty dp = e2.getDataPropertyMap().get(sortPropertyURI);
                        	if (dp.getDataPropertyStatements() != null && dp.getDataPropertyStatements().size()>0) {
                        		val2 = dp.getDataPropertyStatements().get(0).getData();
                        	}
                        }
                        catch (Exception e) {
                        	val2 = "";
                        }
                    } else {
                        LOG.warn( "IndividualJena.sortObjectPropertyStatementsForDisplay() was passed an object property statement with no range entity.");
                    }

                    int rv = 0;
                    try {
                        if( val1 instanceof String )
                        	rv = collator.compare(val1 , val2);
                            //rv = ((String)val1).compareTo((String)val2);
                        else if( val1 instanceof Date ) {
                            DateTime dt1 = new DateTime(val1);
                            DateTime dt2 = new DateTime(val2);
                            rv = dt1.compareTo(dt2);
                        }
                        else
                            rv = 0;
                    } catch (NullPointerException e) {
                        LOG.error(e, e);
                    }

                    if( cAsc )
                        return rv;
                    else
                        return rv * -1;
                }
            };
            try {
                getObjectPropertyStatements().sort(comp);
            } catch (Exception e) {
                LOG.error("Exception sorting object property statements for object property "+this.getURI());
            }
    	} catch (Exception e) {
            LOG.error(e, e);
    	}
    }

	@Override
	public void resolveAsFauxPropertyStatements(List<ObjectPropertyStatement> list) {
        wadf.getObjectPropertyStatementDao().resolveAsFauxPropertyStatements(list);
	}

    /**
     * Setup the URI parts from the individual ontology.
     *
     * @param ind The individual ontology resource.
     */
    protected void setupURIParts(OntResource ind) {
        if (ind != null) {
            if (ind.isAnon()) {
                this.setNamespace(PSEUDO_BNODE_NS);
                this.setLocalName(ind.getId().toString());
            } else {
                this.URI = ind.getURI();
                this.namespace = ind.getNameSpace();
                this.localName = ind.getLocalName();
            }
        }
    }

    /**
     * Get web application DAO factory.
     *
     * @return The web application DAO factory.
     */
    protected WebappDaoFactoryJena getWebappDaoFactory() {
        return wadf;
    }

    /**
     * Get the individual ontology resource.
     *
     * @return The ontology resource.
     */
    protected OntResource getInd() {
        return ind;
    }

    /**
     * Set the individual ontology resource.
     *
     * @param The ontology resource.
     */
    protected void setInd(OntResource ind) {
        this.ind = ind;
    }

}
