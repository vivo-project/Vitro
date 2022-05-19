package edu.cornell.mannlib.vitro.webapp.dynapi.computation;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Action;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;

public class AutoConfiguration {

    private static final Log log = LogFactory.getLog(AutoConfiguration.class);

    public static void computeParams(Action action) {
        //TODO: Support optional steps
        ExecutionTree tree = new ExecutionTree(action);
        List<StepInfo> exits = tree.getLeafs();
        List<List<StepInfo>> paths = new LinkedList<List<StepInfo>>();
        for (StepInfo exit : exits) {
            LinkedList<StepInfo> path = new LinkedList<StepInfo>();
            path.add(exit);
            findAllPaths(tree, exit, path, paths);
        }
        //TODO: Support multiple execution paths
        Parameters computed = computeActionRequirements(paths.get(0));
        action.getRequiredParams().addAll(computed);
        if( log.isDebugEnabled()) {
            Set<String> names = action.getRequiredParams().getNames();
            String toLog = String.join(", ", names);
            log.debug("Required params: " + toLog);    
        }
    }

    private static Parameters computeActionRequirements(List<StepInfo> list) {
        Parameters requirements = new Parameters();
        int position = list.size() - 1;
        StepInfo last = list.get(position);
        Parameters required = last.getRequiredParams();
        //Add required by last node
        requirements.addAll(required);
        position--;
        while (position > 0) {
            StepInfo step =  list.get(position);
            Parameters stepRequired = step.getRequiredParams();
            Parameters stepProvided = step.getProvidedParams();
            requirements.removeAll(stepProvided);
            requirements.addAll(stepRequired);
            position--;
        }
        return requirements;
    }

    private static void findAllPaths(ExecutionTree graph, StepInfo vertex, List<StepInfo> path, List<List<StepInfo>> resultPaths) {
        List<StepInfo> nodes = graph.getPrevOf(vertex);
        for (StepInfo node : nodes) {
            LinkedList<StepInfo> newPath = new LinkedList<StepInfo>(path);
            newPath.addFirst(node);
            if (node.isRoot()) {
                resultPaths.add(newPath);
            } else {
                findAllPaths(graph, node, newPath, resultPaths);
            }
        }
    }

}
