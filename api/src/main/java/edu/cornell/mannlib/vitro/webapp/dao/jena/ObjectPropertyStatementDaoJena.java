/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.rdfservice.ResultSetConsumer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.shared.Lock;
import org.apache.jena.util.iterator.ClosableIterator;

import edu.cornell.mannlib.vitro.webapp.beans.FauxProperty;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectProperty;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatementImpl;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.ObjectPropertyStatementDao;
import edu.cornell.mannlib.vitro.webapp.dao.VitroVocabulary;
import edu.cornell.mannlib.vitro.webapp.dao.jena.event.IndividualUpdateEvent;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFServiceException;

public class ObjectPropertyStatementDaoJena extends JenaBaseDao implements ObjectPropertyStatementDao {

    private static final Log log = LogFactory.getLog(ObjectPropertyStatementDaoJena.class);

    protected DatasetWrapperFactory dwf;
    protected RDFService rdfService;

    public ObjectPropertyStatementDaoJena(RDFService rdfService,
                                          DatasetWrapperFactory dwf,
                                          WebappDaoFactoryJena wadf) {
        super(wadf);
        this.rdfService = rdfService;
        this.dwf = dwf;
    }

    @Override
    public void deleteObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt) {
    	deleteObjectPropertyStatement(objPropertyStmt, getOntModelSelector().getABoxModel());
    }

    public void deleteObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,objPropertyStmt.getSubjectURI()));
        try {
            Resource s = ontModel.getResource(objPropertyStmt.getSubjectURI());
            org.apache.jena.rdf.model.Property p = ontModel.getProperty(objPropertyStmt.getPropertyURI());
            Resource o = ontModel.getResource(objPropertyStmt.getObjectURI());
            if ((s != null) && (p != null) && (o != null)) {
                ontModel.remove(s,p,o);
            }
            List<Statement> dependentResources = DependentResourceDeleteJena.getDependentResourceDeleteList(o, ontModel);
            ontModel.remove(dependentResources);
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,objPropertyStmt.getSubjectURI()));
            ontModel.leaveCriticalSection();
        }
    }

    @Override
    public Individual fillExistingObjectPropertyStatements(Individual entity) {
        if (entity.getURI() == null)
            return entity;
        else {
        	Map<String, ObjectProperty> uriToObjectProperty = new HashMap<String,ObjectProperty>();

        	ObjectPropertyDaoJena opDaoJena = (ObjectPropertyDaoJena) getWebappDaoFactory().getObjectPropertyDao();
        	//new ObjectPropertyDaoJena(rdfService, dwf, getWebappDaoFactory());

        	OntModel ontModel = getOntModelSelector().getABoxModel();
        	ontModel.enterCriticalSection(Lock.READ);
        	try {
	            Resource ind = ontModel.getResource(entity.getURI());
	            List<ObjectPropertyStatement> objPropertyStmtList = new ArrayList<ObjectPropertyStatement>();
	            ClosableIterator<Statement> propIt = ind.listProperties();
	            try {
	                while (propIt.hasNext()) {
	                    Statement st = propIt.next();

	                    if (st.getObject().isResource() && !(NONUSER_NAMESPACES.contains(st.getPredicate().getNameSpace()))) {
	                        try {
	                            ObjectPropertyStatement objPropertyStmt = new ObjectPropertyStatementImpl();
	                            objPropertyStmt.setSubjectURI(entity.getURI());
	                            objPropertyStmt.setSubject(entity);
	                            try {
	                                objPropertyStmt.setObjectURI(((Resource)st.getObject()).getURI());
	                            } catch (Throwable t) {
	                                log.error(t, t);
	                            }
	                            objPropertyStmt.setPropertyURI(st.getPredicate().getURI());
	                            try {
	                                Property prop = st.getPredicate();
	                                if( uriToObjectProperty.containsKey(prop.getURI())){
	                                	objPropertyStmt.setProperty(uriToObjectProperty.get(prop.getURI()));
	                                }else{
	                                	ObjectProperty p = opDaoJena.propertyFromOntProperty(getOntModel().getObjectProperty(prop.getURI()));
	                                	if( p != null ){
	                                		uriToObjectProperty.put(prop.getURI(), p);
	                                		objPropertyStmt.setProperty(uriToObjectProperty.get(prop.getURI()));
	                                	}else{
	                                		//if ObjectProperty not found in ontology, skip it
	                                		continue;
	                                	}
	                                }
	                            } catch (Throwable g) {
	                                //do not add statement to list
	                            	log.debug("exception while trying to get object property for statement list, statement skipped.", g);
	                            	continue;
	                            }
	                            if (objPropertyStmt.getObjectURI() != null) {
	                                Individual objInd = getWebappDaoFactory().getIndividualDao().getIndividualByURI(objPropertyStmt.getObjectURI());
	                                objPropertyStmt.setObject(objInd);
	                            }

	                            //add object property statement to list for Individual
	                            if ((objPropertyStmt.getSubjectURI() != null) && (objPropertyStmt.getPropertyURI() != null) && (objPropertyStmt.getObject() != null)){
	                                objPropertyStmtList.add(objPropertyStmt);
	                            }
	                        } catch (Throwable t) {
	                            log.error(t, t);
	                        }
	                    }
	                }
	            } finally {
	                propIt.close();
	            }
	            entity.setObjectPropertyStatements(objPropertyStmtList);
        	} finally {
        		ontModel.leaveCriticalSection();
        	}
            return entity;
        }
    }

    private int NO_LIMIT = -1;

    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements (ObjectProperty objectProperty) {
    	return getObjectPropertyStatements(objectProperty, NO_LIMIT, NO_LIMIT);
    }

    @Override
    public List<ObjectPropertyStatement> getObjectPropertyStatements (ObjectProperty objectProperty, int startIndex, int endIndex) {
    	getOntModel().enterCriticalSection(Lock.READ);
    	List<ObjectPropertyStatement> opss = new ArrayList<ObjectPropertyStatement>();
    	try {
    		Property prop = ResourceFactory.createProperty(objectProperty.getURI());
    		ClosableIterator<Statement> opsIt = getOntModel().listStatements(null,prop,(Resource)null);
    		try {
    			int count = 0;
    			while ( (opsIt.hasNext()) && ((endIndex<0) || (count<endIndex)) ) {
    				Statement stmt = opsIt.next();
    				if (stmt.getObject().isResource()) {
	    				++count;
	    				if (startIndex<0 || startIndex<=count) {
	    					Resource objRes = (Resource) stmt.getObject();
	    					if (!objRes.isAnon()) {
			    				ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
			    				ops.setSubjectURI(stmt.getSubject().getURI());
			    				ops.setPropertyURI(objectProperty.getURI());
			    				ops.setObjectURI(objRes.getURI());
			    				opss.add(ops);
	    					}
	    				}
    				}
    			}
    		} finally {
    			opsIt.close();
    		}
    	} finally {
    		getOntModel().leaveCriticalSection();
    	}
    	return opss;
    }

	@Override
	public List<ObjectPropertyStatement> getObjectPropertyStatements(
			ObjectPropertyStatement objPropertyStmt) {
		List<ObjectPropertyStatement> opss = new ArrayList<ObjectPropertyStatement>();

		getOntModel().enterCriticalSection(Lock.READ);
		try {
			String subjectUri = objPropertyStmt.getSubjectURI();
			String propertyUri = objPropertyStmt.getPropertyURI();
			String objectUri = objPropertyStmt.getObjectURI();

			Resource subject = (subjectUri == null) ? null : ResourceFactory.createResource(subjectUri);
			Property property = (propertyUri == null) ? null : ResourceFactory
					.createProperty(propertyUri);
			Resource object = (objectUri == null) ? null : ResourceFactory.createResource(objectUri);
			StmtIterator opsIt = getOntModel().listStatements(subject, property, object);
			try {
				while (opsIt.hasNext()) {
					Statement stmt = opsIt.next();
					if (stmt.getObject().isResource()) {
						Resource objRes = (Resource) stmt.getObject();
						if (!objRes.isAnon()) {
							ObjectPropertyStatement ops = new ObjectPropertyStatementImpl();
							ops.setSubjectURI(stmt.getSubject().getURI());
							ops.setPropertyURI(stmt.getPredicate().getURI());
							ops.setObjectURI(objRes.getURI());
							opss.add(ops);
						}
					}
				}
			} finally {
				opsIt.close();
			}
		} finally {
			getOntModel().leaveCriticalSection();
		}
		return opss;
	}

    @Override
	public int insertNewObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt) {
    	return insertNewObjectPropertyStatement(objPropertyStmt, getOntModelSelector().getABoxModel());
    }

    public int insertNewObjectPropertyStatement(ObjectPropertyStatement objPropertyStmt, OntModel ontModel) {
        ontModel.enterCriticalSection(Lock.WRITE);
        getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),true,objPropertyStmt.getSubjectURI()));
        try {
            Resource s = ontModel.getResource(objPropertyStmt.getSubjectURI());
            org.apache.jena.rdf.model.Property p = ontModel.getProperty(objPropertyStmt.getPropertyURI());
            Resource o = ontModel.getResource(objPropertyStmt.getObjectURI());
            if ((s != null) && (p != null) && (o != null)) {
                ontModel.add(s,p,o);
            }
        } finally {
        	getOntModel().getBaseModel().notifyEvent(new IndividualUpdateEvent(getWebappDaoFactory().getUserURI(),false,objPropertyStmt.getSubjectURI()));
            ontModel.leaveCriticalSection();
        }
        return 0;
    }

    /*
     * SPARQL-based method for getting values related to a single object property.
     * We cannot return a List<ObjectPropertyStatement> here, the way the corresponding method of
     * DataPropertyStatementDaoJena returns a List<DataPropertyStatement>. We need to accomodate
     * custom queries that could request any data in addition to just the object of the statement.
     * However, we do need to get the object of the statement so that we have it to create editing links.
     */

    @Override
    public List<Map<String, String>> getObjectPropertyStatementsForIndividualByProperty(
            String subjectUri,
            String propertyUri,
            final String objectKey, String domainUri, String rangeUri,
            String queryString,
            Set<String> constructQueryStrings,
            String sortDirection) {

        final List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        long start = System.currentTimeMillis();

        Model constructedModel = null;
        try {
            constructedModel = constructModelForSelectQueries(
                    subjectUri, propertyUri, rangeUri, constructQueryStrings);

            if (constructedModel != null) {
                if(log.isDebugEnabled()) {
                    log.debug("Constructed model has " + constructedModel.size() + " statements.");
                }
            }

            if("desc".equalsIgnoreCase( sortDirection ) ){
                queryString = queryString.replaceAll(" ASC\\(", " DESC(");
            }

            ResultSetConsumer consumer = new ResultSetConsumer() {
                @Override
                protected void processQuerySolution(QuerySolution qs) {
                    RDFNode node = qs.get(objectKey);
                    if (node != null && node.isURIResource()) {
                        list.add(QueryUtils.querySolutionToStringValueMap(qs));
                    }
                }
            };

            if (constructedModel == null) {
                selectFromRDFService(
                        queryString, subjectUri, propertyUri, domainUri, rangeUri, consumer);
            } else {
                selectFromConstructedModel(
                        queryString, subjectUri, propertyUri, domainUri, rangeUri, constructedModel, consumer);
            }

            if(log.isDebugEnabled()) {
                long duration = System.currentTimeMillis() - start;
                log.debug(duration + " to do list view for " +
                        propertyUri + " / " + domainUri + " / " + rangeUri);
            }
        } catch (Exception e) {
            log.error("Error getting object property values for subject " + subjectUri + " and property " + propertyUri, e);
            return Collections.emptyList();
        } finally {
            if (constructedModel != null) {
                constructedModel.close();
            }
        }
        return list;
    }

    private void selectFromRDFService(String queryString, String subjectUri,
                                      String propertyUri, String domainUri, String rangeUri, ResultSetConsumer consumer) {
        String[] part = queryString.split("[Ww][Hh][Ee][Rr][Ee]");
        part[1] = part[1].replace("?subject", "<" + subjectUri + ">");
        part[1] = part[1].replace("?property", "<" + propertyUri + ">");
        if (domainUri != null && !domainUri.startsWith(VitroVocabulary.PSEUDO_BNODE_NS)) {
            part[1] = part[1].replace("?subjectType", "<" + domainUri + ">");
        }
        if (rangeUri != null && !rangeUri.startsWith(VitroVocabulary.PSEUDO_BNODE_NS)) {
            part[1] = part[1].replace("?objectType", "<" + rangeUri + ">");
        }
        queryString = part[0] + "WHERE" + part[1];
        try {
            rdfService.sparqlSelectQuery(queryString, consumer);
        } catch (RDFServiceException e) {
            throw new RuntimeException(e);
        }
    }

    private void selectFromConstructedModel(String queryString,
                                                 String subjectUri, String propertyUri, String domainUri, String rangeUri,
                                                 Model constructedModel, ResultSetConsumer consumer) {
        Query query = null;
        try {
            query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        } catch(Throwable th){
            log.error("Could not create SPARQL query for query string. " + th.getMessage());
            log.error(queryString);
            throw new RuntimeException(th);
        }

        QuerySolutionMap initialBindings = new QuerySolutionMap();
        initialBindings.add("subject", ResourceFactory.createResource(subjectUri));
        initialBindings.add("property", ResourceFactory.createResource(propertyUri));
        if (domainUri != null && !domainUri.startsWith(VitroVocabulary.PSEUDO_BNODE_NS)) {
            initialBindings.add("subjectType", ResourceFactory.createResource(domainUri));
        }
        if (rangeUri != null && !rangeUri.startsWith(VitroVocabulary.PSEUDO_BNODE_NS)) {
            initialBindings.add("objectType", ResourceFactory.createResource(rangeUri));
        }

        if(log.isDebugEnabled()) {
            log.debug("Query string for object property " + propertyUri + ": " + queryString);
        }

        // Run the SPARQL query to get the properties

        QueryExecution qexec = null;
        try {
            qexec = QueryExecutionFactory.create(
                    query, constructedModel, initialBindings);
            consumer.processResultSet(qexec.execSelect());
        } finally {
            if (qexec != null) {
                qexec.close();
            }
        }
    }

    private Model constructModelForSelectQueries(String subjectUri,
                                                 String propertyUri,
                                                 String rangeUri,
                                                 Set<String> constructQueries) {

        if (constructQueries.size() == 0 || constructQueries == null) {
            return null;
        }

        Model constructedModel = ModelFactory.createDefaultModel();

        for (String queryString : constructQueries) {

            queryString = queryString.replace("?subject", "<" + subjectUri + ">");
            queryString = queryString.replace("?property", "<" + propertyUri + ">");
            if (rangeUri != null) {
                queryString = queryString.replace("?objectType", "<" + rangeUri + ">");
            }

            if (log.isDebugEnabled()) {
                log.debug("CONSTRUCT query string for object property " +
                        propertyUri + ": " + queryString);
            }

            try {
            	//If RDFService is null, will do what code used to do before,
                //otherwise employ rdfservice
            	if(rdfService == null) {
                    log.debug("RDF Service null, Using CONSTRUCT query string for object property " +
                            propertyUri + ": " + queryString);
                    Query query = null;
                    try {
                        query = QueryFactory.create(queryString, Syntax.syntaxARQ);
                    } catch(Throwable th){
                        log.error("Could not create CONSTRUCT SPARQL query for query " +
                                  "string. " + th.getMessage());
                        log.error(queryString);
                        return constructedModel;
                    }

                    DatasetWrapper w = dwf.getDatasetWrapper();
                    Dataset dataset = w.getDataset();
                    dataset.getLock().enterCriticalSection(Lock.READ);
                    QueryExecution qe = null;
                    try {
                        qe = QueryExecutionFactory.create(
                                query, dataset);
                        qe.execConstruct(constructedModel);
                    } catch (Exception e) {
                        log.error("Error getting constructed model for subject "
                            + subjectUri + " and property " + propertyUri);
                    } finally {
                        if (qe != null) {
                            qe.close();
                        }
                        dataset.getLock().leaveCriticalSection();
                        w.close();
                    }
            	} else {
            	    rdfService.sparqlConstructQuery(queryString, constructedModel);
            	}
            } catch (Exception e) {
                log.error("Error getting constructed model for subject "
                    + subjectUri + " and property " + propertyUri, e);
            }
        }
        return constructedModel;
    }

    protected static final String MOST_SPECIFIC_TYPE_QUERY = ""
        + "PREFIX rdfs: <" + VitroVocabulary.RDFS + "> \n"
        + "PREFIX vitro: <" + VitroVocabulary.vitroURI + "> \n"
        + "SELECT DISTINCT ?label ?type WHERE { \n"
        + "    ?subject vitro:mostSpecificType ?type . \n"
        + "    ?type rdfs:label ?label . \n"
        + "    ?type vitro:inClassGroup ?classGroup . \n"
        + "    ?classGroup a ?ClassGroup \n"
        + "} ORDER BY ?label ";

    @Override
    /**
     * Finds all mostSpecificTypes of an individual that are members of a classgroup.
     * Returns a list of type labels.
     *
     * Note that the Map returned is a LinkedHashMap, which means that an iterator
     * will return the entries in the order in which the keys were inserted in the map.
     * Since the SPARQL query included an "ORDER BY ?label" clause, and since an
     * iterator through that ResultSet was used to add entries to the map, an iterator
     * on the map will return entries that are sorted by label (value, not key).
     *
     * While this sorting order is not specified as a requirement (maybe it should be?),
     * it is certainly useful that the types are returned in some order that is
     * replicable, so an individual with multiple mostSpecificTypes, will always see
     * the same list in the same order. (See https://issues.library.cornell.edu/browse/NIHVIVO-568)
     * **/
    public Map<String, String> getMostSpecificTypesInClassgroupsForIndividual(String subjectUri) {

        String queryString = QueryUtils.subUriForQueryVar(MOST_SPECIFIC_TYPE_QUERY, "subject", subjectUri);

        log.debug("Query string for vitro:mostSpecificType : " + queryString);

        Query query = null;
        try {
            query = QueryFactory.create(queryString, Syntax.syntaxARQ);
        } catch(Throwable th){
            log.error("Could not create SPARQL query for query string. " + th.getMessage());
            log.error(queryString);
            return Collections.emptyMap();
        }

        Map<String, String> result = new LinkedHashMap<String, String>();
        Map<String, List<Literal>> types = new LinkedHashMap<String, List<Literal>>();
        DatasetWrapper w = dwf.getDatasetWrapper();
        Dataset dataset = w.getDataset();
        dataset.getLock().enterCriticalSection(Lock.READ);
        QueryExecution qexec = null;
        try {
            qexec = QueryExecutionFactory.create(query, dataset);
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();

                RDFNode typeNode = soln.get("type");
                String type = null;
                if (typeNode.isURIResource()) {
                     type = typeNode.asResource().getURI();
                }

                RDFNode labelNode = soln.get("label");
                if (StringUtils.isNotBlank(type) && labelNode.isLiteral()) {

                	List<Literal> langLabels = types.get(type);
                	if (null == langLabels) {
                		types.put(type, langLabels = new ArrayList<Literal>());
                	}
                	langLabels.add(labelNode.asLiteral());

                }
            }

            // choose labels corresponding to preferred languages
            Set<Entry<String, List<Literal>>> typeEntries = types.entrySet();
            for (Entry<String, List<Literal>> current : typeEntries) {
            	result.put(current.getKey(), tryLiteralForPreferredLanguages(current.getValue()).getLexicalForm());
            }

            return result;

        } catch (Exception e) {
            log.error("Error getting most specific types for subject " + subjectUri);
            return Collections.emptyMap();

        } finally {
            dataset.getLock().leaveCriticalSection();
            if (qexec != null) {
                qexec.close();
            }
            w.close();
        }

    }

	/**
	 * If this statement qualifies as a faux property, set the range and domain
	 * accordingly.
	 */
	@Override
	public void resolveAsFauxPropertyStatements(List<ObjectPropertyStatement> list) {
		if (list == null || list.size() == 0) {
			return;
		}

        Map<String, List<FauxProperty>> fauxPropMap = new HashMap<String, List<FauxProperty>>();
        Map<String, VClass> vclassTypeMap = new HashMap<String, VClass>();
        Map<String, String> indVClassURIMap = new HashMap<String, String>();
        Map<String, List<String>> superClassMap = new HashMap<String, List<String>>();

        for (ObjectPropertyStatement stmt : list) {
            ObjectProperty prop = obtainObjectPropertyFromStatement(stmt);
            if (prop != null) {
                FauxProperty useThisFaux = null;
                List<FauxProperty> fauxProps = fauxPropMap.get(prop.getURI());
                if (fauxProps == null) {
                    fauxProps = getWebappDaoFactory()
                            .getFauxPropertyDao()
                            .getFauxPropertiesForBaseUri(prop.getURI());

                    fauxPropMap.put(prop.getURI(), fauxProps);
                }

                if (fauxProps != null && !fauxProps.isEmpty()) {
                    String domainVClassURI = indVClassURIMap.get(stmt.getSubjectURI());
                    String rangeVClassURI  = indVClassURIMap.get(stmt.getObjectURI());

                    if (domainVClassURI == null) {
                        Individual subject = stmt.getSubject();
                        if (subject != null) {
                            domainVClassURI = subject.getVClassURI();
                            indVClassURIMap.put(subject.getURI(), domainVClassURI);
                        }
                    }

                    if (rangeVClassURI == null) {
                        Individual object  = stmt.getObject();
                        if (object != null) {
                            rangeVClassURI = object.getVClassURI();
                            indVClassURIMap.put(object.getURI(), rangeVClassURI);
                        }
                    }

                    if (domainVClassURI != null && rangeVClassURI != null) {
                        for (FauxProperty fauxProp : fauxProps) {
                            if (domainVClassURI.equals(fauxProp.getDomainVClassURI()) &&
                                    rangeVClassURI.equals(fauxProp.getRangeVClassURI())) {
                                useThisFaux = fauxProp;
                                break;
                            }
                        }
                    }

                    if (useThisFaux == null) {
                        List<String> domainSuperClasses = superClassMap.get(domainVClassURI);
                        List<String> rangeSuperClasses  = superClassMap.get(rangeVClassURI);

                        if (domainSuperClasses == null) {
                            domainSuperClasses = getWebappDaoFactory().getVClassDao().getAllSuperClassURIs(domainVClassURI);
                            if (!domainSuperClasses.contains(domainVClassURI)) {
                                domainSuperClasses.add(domainVClassURI);
                            }
                            superClassMap.put(domainVClassURI, domainSuperClasses);
                        }

                        if (rangeSuperClasses == null) {
                            rangeSuperClasses  = getWebappDaoFactory().getVClassDao().getAllSuperClassURIs(rangeVClassURI);
                            if (!rangeSuperClasses.contains(rangeVClassURI)) {
                                rangeSuperClasses.add(rangeVClassURI);
                            }
                            superClassMap.put(rangeVClassURI, rangeSuperClasses);
                        }

                        if (domainSuperClasses != null && domainSuperClasses.size() > 0) {
                            if (rangeSuperClasses != null && rangeSuperClasses.size() > 0) {
                                for (FauxProperty fauxProp : fauxProps) {
                                    if (domainSuperClasses.contains(fauxProp.getDomainVClassURI()) &&
                                            rangeSuperClasses.contains(fauxProp.getRangeVClassURI())) {
                                        useThisFaux = fauxProp;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (useThisFaux != null) {
                    String fauxDomainVClassURI = useThisFaux.getDomainVClassURI();
                    VClass fauxDomainVClass = vclassTypeMap.get(fauxDomainVClassURI);
                    if (fauxDomainVClass == null) {
                        fauxDomainVClass = getWebappDaoFactory().getVClassDao().getVClassByURI(fauxDomainVClassURI);
                        vclassTypeMap.put(fauxDomainVClassURI, fauxDomainVClass);
                    }

                    String fauxRangeVClassURI  = useThisFaux.getRangeVClassURI();
                    VClass fauxRangeVClass  = vclassTypeMap.get(fauxRangeVClassURI);
                    if (fauxRangeVClass == null) {
                        fauxRangeVClass  = getWebappDaoFactory().getVClassDao().getVClassByURI(fauxRangeVClassURI);
                        vclassTypeMap.put(fauxRangeVClassURI, fauxRangeVClass);
                    }

                    if (fauxDomainVClass != null && fauxDomainVClassURI != null) {
                        prop.setDomainVClass(fauxDomainVClass);
                        prop.setDomainVClassURI(fauxDomainVClassURI);
                    }

                    if (fauxRangeVClass != null && fauxRangeVClassURI != null) {
                        prop.setRangeVClass(fauxRangeVClass);
                        prop.setRangeVClassURI(fauxRangeVClassURI);
                    }
                }
            }
        }
	}

	private ObjectProperty obtainObjectPropertyFromStatement(
			ObjectPropertyStatement stmt) {
		ObjectProperty prop = stmt.getProperty();
		if (prop != null) {
			return prop;
		}
		String propertyURI = stmt.getPropertyURI();
		if (propertyURI == null) {
			return null;
		}
		return getWebappDaoFactory().getObjectPropertyDao()
				.getObjectPropertyByURI(propertyURI);
	}

	private Individual obtainSubjectFromStatement(ObjectPropertyStatement stmt) {
		Individual subject = stmt.getSubject();
		if (subject != null) {
			return subject;
		}
		String subjectURI = stmt.getSubjectURI();
		if (subjectURI == null) {
			return null;
		}
		return getWebappDaoFactory().getIndividualDao().getIndividualByURI(
				subjectURI);
	}

	private Individual obtainObjectFromStatement(ObjectPropertyStatement stmt) {
		Individual object = stmt.getObject();
		if (object != null) {
			return object;
		}
		String objectURI = stmt.getObjectURI();
		if (objectURI == null) {
			return null;
		}
		return getWebappDaoFactory().getIndividualDao().getIndividualByURI(
				objectURI);
	}

	private VClass selectType(List<VClass> types, String uri) {
		for (VClass type : types) {
			if (Objects.equals(type.getURI(), uri)) {
				return type;
			}
		}
		return null;
	}

}
