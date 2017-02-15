/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.searchindex.tasks;

import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.ALLTEXT;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.ALLTEXTUNSTEMMED;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.CLASSGROUP_URI;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.DOCID;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.INDEXEDTIME;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.MOST_SPECIFIC_TYPE_URIS;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_LOWERCASE_SINGLE_VALUED;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.NAME_RAW;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.RDFTYPE;
import static edu.cornell.mannlib.vitro.webapp.search.VitroSearchTermNames.URI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.beans.DataPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.Individual;
import edu.cornell.mannlib.vitro.webapp.beans.ObjectPropertyStatement;
import edu.cornell.mannlib.vitro.webapp.beans.VClass;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineNotRespondingException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchInputDocument;
import edu.cornell.mannlib.vitro.webapp.modules.searchIndexer.SearchIndexerUtils;
import edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.DocumentModifier;
import edu.cornell.mannlib.vitro.webapp.searchindex.documentBuilding.DocumentModifierList;

public class UpdateDocumentWorkUnit implements Runnable {
	private static final Log log = LogFactory
			.getLog(UpdateDocumentWorkUnit.class);

	private static final String URI_OWL_THING = OWL.Thing.getURI();
	private static final String URI_DIFFERENT_FROM = OWL.differentFrom.getURI();
	private static final String URI_RDFS_LABEL = RDFS.label.getURI();

	private final Individual ind;
	private final DocumentModifierList modifiers;
	private final SearchEngine searchEngine;

	public UpdateDocumentWorkUnit(Individual ind, DocumentModifierList modifiers) {
		this.ind = ind;
		this.modifiers = modifiers;
		this.searchEngine = ApplicationUtils.instance().getSearchEngine();
	}

	public Individual getInd() {
		return ind;
	}

	@Override
	public void run() {
		try {
			SearchInputDocument doc = searchEngine.createInputDocument();
			modifiers.modifyDocument(ind, doc);
			addIndexedTime(doc);
			searchEngine.add(doc);
		} catch (SearchEngineNotRespondingException e) {
			log.warn("Failed to add '" + ind + "' to the search index: "
					+ "the search engine is not responding.");
		} catch (Exception e) {
			log.warn("Failed to add '" + ind + "' to the search index.", e);
		}
	}

	private void addIndexedTime(SearchInputDocument doc) {
		doc.addField(INDEXEDTIME, (Object) new DateTime().getMillis());
	}

	// ----------------------------------------------------------------------
	// Helper classes
	// ----------------------------------------------------------------------

	/**
	 * These will be hardcoded at the beginning of the list of
	 * DocumentModifiers.
	 */
	public static class MinimalDocumentModifiers {
		private final List<DocumentModifier> list;

		public MinimalDocumentModifiers() {
			this.list = Arrays.asList(new DocumentModifier[] {
					new IdUriLabel(), new AddClasses(),
					new AddMostSpecificTypes(), new AddObjectPropertyText(),
					new AddDataPropertyText(), new AddEntityBoost() });
		}

		public List<DocumentModifier> getList() {
			return list;
		}
	}

	private abstract static class BaseDocumentModifier implements
			DocumentModifier {
		protected void addToAlltext(SearchInputDocument doc, String raw) {
			if (StringUtils.isBlank(raw)) {
				return;
			}
			String clean = Jsoup.parse(raw).text();
			if (StringUtils.isBlank(clean)) {
				return;
			}
			doc.addField(ALLTEXT, clean);
			doc.addField(ALLTEXTUNSTEMMED, clean);
		}

		@Override
		public void shutdown() {
			// Nothing to do.
		}
	}

	private static class IdUriLabel extends BaseDocumentModifier {
		@Override
		public void modifyDocument(Individual ind, SearchInputDocument doc) {
			addIdAndUri(ind, doc);
			addLabel(ind, doc);
		}

		private void addIdAndUri(Individual ind, SearchInputDocument doc) {
			doc.addField(DOCID, SearchIndexerUtils.getIdForUri(ind.getURI()));
			doc.addField(URI, ind.getURI());
		}

		private void addLabel(Individual ind, SearchInputDocument doc) {
			String name = ind.getRdfsLabel();
			if (name == null) {
				name = ind.getLocalName();
			}

			doc.addField(NAME_RAW, name);
			doc.addField(NAME_LOWERCASE_SINGLE_VALUED, name);
		}

		@Override
		public String toString() {
			return "Internal: IdUriLabel";
		}
	}

	/**
	 * For each class that the individual belongs to, record the class URI, the
	 * class group URI, the class Name, and the class boost.
	 */
	private static class AddClasses extends BaseDocumentModifier {
		@Override
		public void modifyDocument(Individual ind, SearchInputDocument doc) {
			List<VClass> vclasses = ind.getVClasses(false);
			if (vclasses == null) {
				return;
			}

			Set<String> classGroupUris = new HashSet<>();
			for (VClass clz : vclasses) {
				String classUri = clz.getURI();
				if (classUri == null || URI_OWL_THING.equals(classUri)) {
					continue;
				}
				doc.addField(RDFTYPE, classUri);

				String classGroupUri = clz.getGroupURI();
				if (classGroupUri != null) {
					classGroupUris.add(classGroupUri);
				}

				addToAlltext(doc, clz.getName());

				Float boost = clz.getSearchBoost();
				if (boost != null) {
					doc.setDocumentBoost(doc.getDocumentBoost() + boost);
				}
			}
			if (!classGroupUris.isEmpty()) {
				doc.addField(CLASSGROUP_URI, classGroupUris);
			}
		}

		@Override
		public String toString() {
			return "Internal: AddClasses";
		}
	}

	private static class AddMostSpecificTypes extends BaseDocumentModifier {
		@Override
		public void modifyDocument(Individual ind, SearchInputDocument doc) {
			List<String> mstURIs = ind.getMostSpecificTypeURIs();
			if (mstURIs != null) {
				for (String typeURI : mstURIs) {
					if (StringUtils.isNotBlank(typeURI)) {
						doc.addField(MOST_SPECIFIC_TYPE_URIS, typeURI);
					}
				}
			}
		}

		@Override
		public String toString() {
			return "Internal: AddMostSpecificTypes";
		}
	}

	private static class AddObjectPropertyText extends BaseDocumentModifier {
		@Override
		public void modifyDocument(Individual ind, SearchInputDocument doc) {
			List<ObjectPropertyStatement> stmts = ind
					.getObjectPropertyStatements();
			if (stmts != null) {
				for (ObjectPropertyStatement stmt : stmts) {
					if (!URI_DIFFERENT_FROM.equals(stmt.getPropertyURI())) {
						addToAlltext(doc, stmt.getObject().getRdfsLabel());
					}
				}
			}
		}

		@Override
		public String toString() {
			return "Internal: AddObjectPropertyText";
		}
	}

	private static class AddDataPropertyText extends BaseDocumentModifier {
		@Override
		public void modifyDocument(Individual ind, SearchInputDocument doc) {
			List<DataPropertyStatement> stmts = ind.getDataPropertyStatements();
			if (stmts != null) {
				for (DataPropertyStatement stmt : stmts) {
					if (!stmt.getDatapropURI().equals(URI_RDFS_LABEL)) {
						addToAlltext(doc, stmt.getData());
					}
				}
			}
		}

		@Override
		public String toString() {
			return "Internal: AddDataPropertyText";
		}
	}

	private static class AddEntityBoost extends BaseDocumentModifier {
		@Override
		public void modifyDocument(Individual ind, SearchInputDocument doc) {
			Float boost = ind.getSearchBoost();
			if (boost != null && !boost.equals(0.0F)) {
				doc.setDocumentBoost(boost);
			}
		}

		@Override
		public String toString() {
			return "Internal: AddEntityBoost";
		}
	}

}
