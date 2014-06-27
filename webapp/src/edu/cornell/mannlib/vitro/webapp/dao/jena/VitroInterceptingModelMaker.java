/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.dao.jena;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.hp.hpl.jena.graph.GraphMaker;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.ModelReader;
import com.hp.hpl.jena.shared.AlreadyExistsException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

/**
 * A decorator on top of a model maker. It looks for requests on particular
 * URIs, and shunts them to the pre-made models on ModelAccess.
 * 
 * The result is two-fold:
 * 
 * We have effective URIs for models that only exist as combinations of other
 * named models (UNION_FULL, BASE_FULL, INFERRED_FULL).
 * 
 * For models with in-memory mapping, a request will return a reference to that
 * mapping.
 */
public class VitroInterceptingModelMaker implements ModelMaker {
	private final ModelMaker innerMM;
	private final Map<String, Model> specialMap;

	public VitroInterceptingModelMaker(ModelMaker innerMM, Map<String, Model> specialMap) {
		this.innerMM = innerMM;
		this.specialMap = Collections.unmodifiableMap(new HashMap<>(specialMap));
	}

	@Override
	public Model getModel(String url) {
		return isSpecial(url) ? getSpecial(url) : innerMM.getModel(url);
	}

	@Override
	public Model getModel(String url, ModelReader loadIfAbsent) {
		return isSpecial(url) ? getSpecial(url) : innerMM.getModel(url,
				loadIfAbsent);
	}

	@Override
	public Model createDefaultModel() {
		return innerMM.createDefaultModel();
	}

	@Override
	public Model createFreshModel() {
		return innerMM.createFreshModel();
	}

	@Override
	public Model openModel(String name) {
		return isSpecial(name) ? getSpecial(name) : innerMM.openModel(name);
	}

	@Override
	public Model openModelIfPresent(String name) {
		return isSpecial(name) ? getSpecial(name) : innerMM
				.openModelIfPresent(name);
	}

	@Override
	public void close() {
		innerMM.close();
	}

	@Override
	public Model createModel(String name) {
		return isSpecial(name) ? getSpecial(name) : innerMM.createModel(name);
	}

	@Override
	public Model createModel(String name, boolean strict) {
		if (isSpecial(name)) {
			if (strict) {
				throw new AlreadyExistsException(name);
			} else {
				return getSpecial(name);
			}
		} else {
			return innerMM.createModel(name, strict);
		}
	}

	/**
	 * TODO this should actually return an intercepting graph maker.
	 */
	@Override
	public GraphMaker getGraphMaker() {
		return innerMM.getGraphMaker();
	}

	@Override
	public boolean hasModel(String name) {
		return isSpecial(name) || innerMM.hasModel(name);
	}

	@Override
	public ExtendedIterator<String> listModels() {
		return new SetsExtendedIterator(getSpecialNames(), innerMM.listModels()
				.toSet());
	}

	@Override
	public Model openModel(String name, boolean strict) {
		return isSpecial(name) ? getSpecial(name) : innerMM.openModel(name,
				strict);
	}

	/**
	 * We don't lete anyone remove the special models.
	 */
	@Override
	public void removeModel(String name) {
		if (!isSpecial(name)) {
			innerMM.removeModel(name);
		}
	}

	// ----------------------------------------------------------------------
	// Intercepting mechanism
	// ----------------------------------------------------------------------

	private Collection<String> getSpecialNames() {
		return specialMap.keySet();
	}

	private boolean isSpecial(String url) {
		return specialMap.containsKey(url);
	}

	private Model getSpecial(String url) {
		return specialMap.get(url);
	}

	private static class SetsExtendedIterator extends NiceIterator<String> {
		private final Iterator<String> iter;

		@SafeVarargs
		public SetsExtendedIterator(Collection<String>... collections) {
			Set<String> set = new TreeSet<>();
			for (Collection<String> c : collections) {
				set.addAll(c);
			}
			this.iter = set.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public String next() {
			return iter.next();
		}

	}
}
