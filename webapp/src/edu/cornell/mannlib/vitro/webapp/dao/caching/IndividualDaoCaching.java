/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.caching;

import static edu.cornell.mannlib.vitro.webapp.dao.jena.WebappDaoFactorySDB.SDBDatasetMode.ASSERTIONS_AND_INFERENCES;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.dao.IndividualDao;
import edu.cornell.mannlib.vitro.webapp.dao.InsertException;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualDaoSDB;
import edu.cornell.mannlib.vitro.webapp.dao.jena.IndividualSDB;
import edu.cornell.mannlib.vitro.webapp.edit.EditLiteral;
import edu.cornell.mannlib.vitro.webapp.utils.sparql.ConstructQueryRunner;

/**
 * Keep a cache of the Individuals that have been accessed by this request.
 * Remove an individual from the cache if it is updated.
 * 
 * NOTE: This should not be used in a request-independent context.
 * 
 * The cache has a limited size of 64K individuals.
 */
public class IndividualDaoCaching implements IndividualDao {
	private static final Log log = LogFactory
			.getLog(IndividualDaoCaching.class);

	/**
	 * A limited-size cache that keeps the most-recently-accessed items.
	 */
	private final static class CacheMap extends
			LinkedHashMap<String, Individual> {
		static Map<String, Individual> create() {
			return Collections.synchronizedMap(new CacheMap());
		}

		private CacheMap() {
			super(64, 0.75F, true);
		}

		@Override
		protected boolean removeEldestEntry(Map.Entry<String, Individual> eldest) {
			return size() >= 65536;
		}
	}

	private abstract static class IndividualGetter {
		abstract Individual get(String uri);
	}

	private final IndividualDaoSDB inner;
	private final IndividualCachingWebappDaoFactorySDB wadf;
	private final Map<String, Individual> cache = CacheMap.create();

	public IndividualDaoCaching(IndividualCachingWebappDaoFactorySDB wadf) {
		this.wadf = wadf;
		this.inner = (IndividualDaoSDB) wadf.getIndividualDao();
	}

	private void cache(Individual ind) {
		String uri = ind.getURI();
		if (uri != null) {
			cache.put(uri, ind);
		}
	}

	private void cacheAll(Collection<Individual> c) {
		if (c != null) {
			for (Individual ind : c) {
				if (ind != null) {
					cache(ind);
				}
			}
		}
	}

	private Individual getWithCaching(String uri, IndividualGetter getter) {
		if (cache.containsKey(uri)) {
			log.debug("Cache hit:  " + uri);
		} else {
			log.debug("Cache miss: " + uri);
			cache(getter.get(uri));
		}
		return cache.get(uri);
	}

	private void invalidate(String uri) {
		if (uri != null) {
			cache.remove(uri);
		}
	}

	private void invalidate(Individual ind) {
		if (ind != null) {
			invalidate(ind.getURI());
		}
	}

	// ----------------------------------------------------------------------
	// delegated methods
	// ----------------------------------------------------------------------

	@Override
	public Collection<DataPropertyStatement> getExternalIds(String individualURI) {
		return inner.getExternalIds(individualURI);
	}

	@Override
	public Collection<DataPropertyStatement> getExternalIds(
			String individualURI, String dataPropertyURI) {
		return inner.getExternalIds(individualURI, dataPropertyURI);
	}

	@Override
	public void addVClass(String individualURI, String vclassURI) {
		invalidate(individualURI);
		inner.addVClass(individualURI, vclassURI);
	}

	@Override
	public void removeVClass(String individualURI, String vclassURI) {
		invalidate(individualURI);
		inner.removeVClass(individualURI, vclassURI);
	}

	@Override
	public List<Individual> getIndividualsByVClass(VClass vclass) {
		List<Individual> list = inner.getIndividualsByVClass(vclass);
		cacheAll(list);
		return list;
	}

	@Override
	public List<Individual> getIndividualsByVClassURI(String vclassURI) {
		List<Individual> list = inner.getIndividualsByVClassURI(vclassURI);
		cacheAll(list);
		return list;
	}

	@Override
	public List<Individual> getIndividualsByVClassURI(String vclassURI,
			int offset, int quantity) {
		@SuppressWarnings("unchecked")
		List<Individual> list = inner.getIndividualsByVClassURI(vclassURI,
				offset, quantity);
		cacheAll(list);
		return list;
	}

	@Override
	public String insertNewIndividual(Individual individual)
			throws InsertException {
		invalidate(individual);
		return inner.insertNewIndividual(individual);
	}

	@Override
	public int updateIndividual(Individual individual) {
		invalidate(individual);
		return inner.updateIndividual(individual);
	}

	@Override
	public int deleteIndividual(String individualURI) {
		invalidate(individualURI);
		return inner.deleteIndividual(individualURI);
	}

	@Override
	public int deleteIndividual(Individual individual) {
		invalidate(individual);
		return inner.deleteIndividual(individual);
	}

	@Override
	public void markModified(Individual individual) {
		invalidate(individual);
		inner.markModified(individual);
	}

	@Override
	public Individual getIndividualByURI(final String individualURI) {
		return getWithCaching(individualURI, new IndividualGetter() {
			@Override
			Individual get(String uri) {
				return inner.getIndividualByURI(individualURI);
			}
		});
	}

	@Override
	public Collection<String> getAllIndividualUris() {
		return inner.getAllIndividualUris();
	}

	@Override
	public Iterator<String> getUpdatedSinceIterator(long updatedSince) {
		return inner.getUpdatedSinceIterator(updatedSince);
	}

	@Override
	public boolean isIndividualOfClass(String vclassURI, String indURI) {
		return inner.isIndividualOfClass(vclassURI, indURI);
	}

	@Override
	public List<Individual> getIndividualsByDataProperty(
			String dataPropertyUri, String value) {
		return inner.getIndividualsByDataProperty(dataPropertyUri, value);
	}

	@Override
	public List<Individual> getIndividualsByDataProperty(
			String dataPropertyUri, String value, String datatypeUri,
			String lang) {
		return inner.getIndividualsByDataProperty(dataPropertyUri, value,
				datatypeUri, lang);
	}

	@Override
	public void fillVClassForIndividual(Individual individual) {
		inner.fillVClassForIndividual(individual);
	}

	@Override
	public String getUnusedURI(Individual individual) throws InsertException {
		return inner.getUnusedURI(individual);
	}

	@Override
	public EditLiteral getLabelEditLiteral(String individualUri) {
		return inner.getLabelEditLiteral(individualUri);
	}

	// ----------------------------------------------------------------------
	// Seeding the cache
	// ----------------------------------------------------------------------

	private final String SEEDING_QUERY = "" //
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
			+ "CONSTRUCT { \n" //
			+ "  ?o rdfs:label ?label . \n" //
			+ "  ?o a ?type . \n" //
			+ "} \n" //
			+ "WHERE { \n" //
			+ "  { \n" //
			+ "    ?uri ?p ?o . \n" //
			+ "    ?o rdfs:label ?label . \n" //
			+ "  } UNION { " //
			+ "    ?uri ?p ?o . \n" //
			+ "    ?o a ?type . \n" //
			+ "  } \n" //
			+ "} ";

	/**
	 * Create instances of IndividualSDB for the object of each object property
	 * of this individual. Add them to the cache. Creating them in bulk like
	 * this is faster than doing it individually.
	 */
	public void createObjectIndividualsFor(String individualUri) {
		Model tempModel = ConstructQueryRunner
				.createQueryContext(wadf.getRDFService(), SEEDING_QUERY)
				.bindVariableToUri("uri", individualUri).execute();

		for (Resource seed : tempModel.listSubjectsWithProperty(RDF.type)
				.toList()) {
			if (seed.isURIResource()) {
				String seedUri = seed.getURI();
				try {
					cache(new IndividualSDB(seedUri,
							wadf.getDatasetWrapperFactory(),
							ASSERTIONS_AND_INFERENCES, wadf, tempModel));
					log.debug("Seeded the cache with '" + seedUri + "'");
				} catch (Exception e) {
					log.error("Failed to see the cache with '" + seedUri + "'",
							e);
				}
			}
		}
	}
}
