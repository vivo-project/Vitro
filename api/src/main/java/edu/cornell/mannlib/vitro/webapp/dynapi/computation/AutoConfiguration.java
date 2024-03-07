package edu.cornell.mannlib.vitro.webapp.dynapi.computation;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameter;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Parameters;
import edu.cornell.mannlib.vitro.webapp.dynapi.components.Procedure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AutoConfiguration {

    private static final Log log = LogFactory.getLog(AutoConfiguration.class);

    public static void computeParams(Procedure procedure) {
        Parameters required = procedure.getInputParams();
        Parameters provided = procedure.getOutputParams();
        Parameters internal = procedure.getInternalParams();

        copyInternalProvidedParamsToInternal(provided, internal);
        ExecutionTree tree = new ExecutionTree(procedure);
        List<StepInfo> exits = tree.getLeafs();
        List<List<StepInfo>> paths = new LinkedList<List<StepInfo>>();
        for (StepInfo exit : exits) {
            LinkedList<StepInfo> path = new LinkedList<StepInfo>();
            path.add(exit);
            findAllPaths(tree, exit, path, paths);
        }
        for (List<StepInfo> path : paths) {
            Parameters computed = computeRequirements(path, provided);
            mergeParameters(required, internal, computed);
        }
        if (log.isDebugEnabled()) {
            Set<String> names = procedure.getInputParams().getNames();
            String toLog = String.join(", ", names);
            log.debug("Required params: " + toLog);
        }
    }

    private static void copyInternalProvidedParamsToInternal(Parameters provided, Parameters internal) {
        for (String name : provided.getNames()) {
            Parameter param = provided.get(name);
            if (param.isInternal()) {
                internal.add(param);
            }
        }
    }

    private static void mergeParameters(Parameters required, Parameters internal, Parameters computed) {
        // TODO: Support optional steps
        for (String name : computed.getNames()) {
            Parameter param = computed.get(name);
            if (param.isInternal()) {
                internal.add(param);
            } else {
                required.add(param);
            }
        }
    }

    private static Parameters computeRequirements(List<StepInfo> list, Parameters provided) {
        Parameters requirements = new Parameters();
        requirements.addAll(provided);
        int position = list.size();
        // Add required by last node
        position--;
        while (position > 0) {
            StepInfo step = list.get(position);
            Parameters stepRequired = step.getInputParams();
            Parameters stepProvided = step.getOutputParams();
            requirements.removeAll(stepProvided);
            requirements.addAll(stepRequired);
            position--;
        }
        return requirements;
    }

    private static void findAllPaths(ExecutionTree graph, StepInfo vertex, List<StepInfo> path,
            List<List<StepInfo>> resultPaths) {
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
