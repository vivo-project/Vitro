package edu.cornell.mannlib.vitro.webapp.visualization.coauthorship;

import java.util.Set;

import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Edge;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.Node;

public class VisVOContainer {
	
	private Set<Node> nodes;
	private Set<Edge> edges;
	private Node egoNode;
	
	public VisVOContainer(Node egoNode, Set<Node> nodes, Set<Edge> edges) {
		this.egoNode = egoNode;
		this.nodes = nodes;
		this.edges = edges;
	}
	
	public Set<Node> getNodes() {
		return nodes;
	}

	public Set<Edge> getEdges() {
		return edges;
	}	
	
	public Node getEgoNode() {
		return egoNode;
	}
	
}
