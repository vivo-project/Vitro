package edu.cornell.mannlib.vitro.webapp.dynapi.computation;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.steps.NullStep;

public class ExecutionTree {
    private Map<StepInfo, List<StepInfo>> forwardMap;
    private Map<StepInfo, List<StepInfo>> backwardMap;

    private ExecutionTree() {
        // map from previous to list of next
        forwardMap = new HashMap<StepInfo, List<StepInfo>>();
        // map from next to list of previous
        backwardMap = new HashMap<StepInfo, List<StepInfo>>();
    }

    public ExecutionTree(StepInfo rootNode) {
        this();
        addNodesToMaps(rootNode);
    }

    private void addNodesToMaps(StepInfo node) {
        Set<StepInfo> nextNodes = node.getNextNodes();
        for (StepInfo next : nextNodes) {
            if (!containsPrev(next)) {
                addArc(node, next);
                addNodesToMaps(next);
            }
        }
    }

    public List<StepInfo> getLeafs() {
        return getPrevOf(NullStep.getInstance());
    }

    public List<StepInfo> getPrevOf(StepInfo node) {
        if (!backwardMap.containsKey(node)) {
            return Collections.emptyList();
        }
        return backwardMap.get(node);
    }

    public List<StepInfo> getNextOf(StepInfo node) {
        if (!forwardMap.containsKey(node)) {
            return Collections.emptyList();
        }
        return forwardMap.get(node);
    }

    private void addArc(StepInfo prev, StepInfo next) {
        addToForwardMap(prev, next);
        addToBackwardMap(prev, next);
    }

    private boolean containsNext(StepInfo node) {
        return backwardMap.containsKey(node);
    }

    private boolean containsPrev(StepInfo node) {
        return forwardMap.containsKey(node);
    }

    private void addToBackwardMap(StepInfo prev, StepInfo next) {
        if (backwardMap.containsKey(next)) {
            backwardMap.get(next).add(prev);
        } else {
            LinkedList<StepInfo> prevs = new LinkedList<StepInfo>();
            prevs.add(prev);
            backwardMap.put(next, prevs);
        }
    }

    private void addToForwardMap(StepInfo prev, StepInfo next) {
        if (forwardMap.containsKey(prev)) {
            forwardMap.get(prev).add(next);
        } else {
            LinkedList<StepInfo> nextNodes = new LinkedList<StepInfo>();
            nextNodes.add(next);
            forwardMap.put(prev, nextNodes);
        }
    }

}
