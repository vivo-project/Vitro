/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.vitro.webapp.rdfservice.adapters;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.util.IteratorCollection;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final public class GraphUtils {

	private static final int CMP_GREATER = 1;
	private static final int CMP_EQUAL = 0;
	private static final int CMP_LESS = -1;
	private static int MIN_SRC_SIZE = 1000;
	// If source and destination are large, limit the search for the best way round
	// to "deleteFrom"
	private static int DST_SRC_RATIO = 2;

	public static Graph unwrapUnionGraphs(Graph graph) {
		if (graph != null && graph instanceof MultiUnion) {
		    return unwrapUnionGraphs(((MultiUnion)graph).getBaseGraph());
		}
		return graph;
	}

	public static void deleteFrom(BulkGraphMem bulkGraphMem, Graph srcGraph) {
		boolean events = bulkGraphMem.getEventManager().listening();
		if (bulkGraphMem == srcGraph && !events) {
			bulkGraphMem.clear();
			return;
		}
		boolean loopOnSrc = decideHowtoExecuteBySizeStep(bulkGraphMem, srcGraph);
		if (loopOnSrc) {
			deleteLoopSrc(bulkGraphMem, srcGraph);
			return;
		}
		deleteLoopDst(bulkGraphMem, srcGraph);
	}

	public static void addInto(BulkGraphMem bulkGraphMem, Graph srcGraph) {
		if (bulkGraphMem == srcGraph && !bulkGraphMem.getEventManager().listening()) {
			return;
		}
		bulkGraphMem.getPrefixMapping().setNsPrefixes(srcGraph.getPrefixMapping());
		addIteratorWorker(bulkGraphMem, findAll(srcGraph));
		bulkGraphMem.getEventManager().notifyAddGraph(bulkGraphMem, srcGraph);
	}

	private static ExtendedIterator<Triple> findAll(Graph g) {
		return g.find();
	}

	private static void addIteratorWorker(BulkGraphMem bulkGraphMem, Iterator<Triple> it) {
		List<Triple> s = IteratorCollection.iteratorToList(it);
		addIteratorWorkerDirect(bulkGraphMem, s.iterator());
	}

	private static void addIteratorWorkerDirect(BulkGraphMem bulkGraphMem, Iterator<Triple> it) {
		it.forEachRemaining(bulkGraphMem::addWithoutNotify);
	}

	private static void deleteLoopSrc(BulkGraphMem bulkGraphMem, Graph srcGraph) {
		deleteIteratorWorker(bulkGraphMem, findAll(srcGraph));
		bulkGraphMem.getEventManager().notifyDeleteGraph(bulkGraphMem, srcGraph);
	}

	private static void deleteLoopDst(BulkGraphMem bulkGraphMem, Graph srcGraph) {
		// Size the list to avoid reallocation on growth.
		int dstSize = bulkGraphMem.size();
		List<Triple> toBeDeleted = new ArrayList<>(dstSize);

		Iterator<Triple> iter = findAll(bulkGraphMem);
		for (; iter.hasNext();) {
			Triple t = iter.next();
			if (srcGraph.contains(t)) {
				toBeDeleted.add(t);
			}
		}
		deleteIteratorWorkerDirect(bulkGraphMem, toBeDeleted.iterator());
		bulkGraphMem.getEventManager().notifyDeleteGraph(bulkGraphMem, srcGraph);
	}

	private static void deleteIteratorWorker(BulkGraphMem bulkGraphMem, Iterator<Triple> it) {
		List<Triple> s = IteratorCollection.iteratorToList(it);
		deleteIteratorWorkerDirect(bulkGraphMem, s.iterator());
	}

	private static void deleteIteratorWorkerDirect(BulkGraphMem bulkGraphMem, Iterator<Triple> it) {
		it.forEachRemaining(bulkGraphMem::deleteWithoutNotify);
	}

	private static boolean decideHowtoExecuteBySizeStep(BulkGraphMem bulkGraphMem, Graph srcGraph) {
		int srcSize = srcGraph.size();
		if (srcSize <= MIN_SRC_SIZE)
			return true;
		boolean loopOnSrc = (srcSize <= MIN_SRC_SIZE
				|| compareSizeTo(bulkGraphMem, DST_SRC_RATIO * srcSize) == CMP_GREATER);
		return loopOnSrc;
	}

	private static int compareSizeTo(Graph graph, int size) {
		ExtendedIterator<Triple> it = graph.find();
		try {
			int stepsTake = Iter.step(it, size);
			if (stepsTake < size) {
				// Iterator ran out.
				return CMP_LESS;
			}
			if (!it.hasNext()) {
				// Finished at the same time.
				return CMP_EQUAL;
			}
			// Still more to go
			return CMP_GREATER;
		} finally {
			it.close();
		}
	}
}
