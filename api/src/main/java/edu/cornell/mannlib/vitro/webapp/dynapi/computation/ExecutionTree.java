package edu.cornell.mannlib.vitro.webapp.dynapi.computation;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExecutionTree {
    private Map<StepInfo, List<StepInfo>> forwardMap;
    private Map<StepInfo, List<StepInfo>> backwardMap;
    
    public ExecutionTree() {
        //map from previous to list of next
        forwardMap = new HashMap<StepInfo, List<StepInfo>>();
        //map from next to list of previous
        backwardMap = new HashMap<StepInfo, List<StepInfo>>();
    }
    
    public ExecutionTree(StepInfo rootNode) {
        this();
        initialize(rootNode);
    }

    private void initialize(StepInfo prev) {
        if (prev == null) {
            return;
        }
        Set<StepInfo> nextNodes = prev.getNextNodes();
        for (StepInfo next : nextNodes) {
            if (!containsPrev(next)) {
                addArc(prev, next);
                initialize(next);
            }
        }
    }
    
    public List<StepInfo> getLeafs(){
        return getPrevOf(null);
    }

    public List<StepInfo>getPrevOf(StepInfo node){
        List<StepInfo> prevs = backwardMap.get(node);
        if (prevs == null) {
            return Collections.emptyList();
        }
        return prevs;
    }
    
    public List<StepInfo>getNextOf(StepInfo node){
        List<StepInfo> next = forwardMap.get(node);
        if (next == null) {
            return Collections.emptyList();
        }
        return next;
    }
    
    private void addArc(StepInfo prev, StepInfo next) {
        addToForwardMap(prev, next);
        addToBackwardMap(prev, next);
    }
    
    private boolean containsNext(StepInfo node) {
        if (backwardMap.containsKey(node)) {
            return true;
        }
        return false;
    }
    
    private boolean containsPrev(StepInfo node) {
        if (forwardMap.containsKey(node)) {
            return true;
        }
        return false;
    }

    private void addToBackwardMap(StepInfo prev, StepInfo next) {
        if (backwardMap.containsKey(next)){
            backwardMap.get(next).add(prev);
        } else {
            LinkedList<StepInfo> prevs = new LinkedList<StepInfo>();
            prevs.add(prev);
            backwardMap.put(next, prevs);
        }
    }
    
    private void addToForwardMap(StepInfo prev, StepInfo next) {
        if (forwardMap.containsKey(prev)){
            forwardMap.get(prev).add(next);
        } else {
            LinkedList<StepInfo> nextNodes = new LinkedList<StepInfo>();
            nextNodes.add(next);
            forwardMap.put(prev, nextNodes);
        }
    }

} 
