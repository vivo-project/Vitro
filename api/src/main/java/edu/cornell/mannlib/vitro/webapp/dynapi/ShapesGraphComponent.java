package edu.cornell.mannlib.vitro.webapp.dynapi;

import org.topbraid.shacl.engine.ShapesGraph;

public class ShapesGraphComponent {

    ShapesGraph graph;
   
    public ShapesGraph getGraph() {
        return graph;
    }

    public ShapesGraphComponent(ShapesGraph shapesGraph) {
        this.graph = shapesGraph;
    }

    public ShapesGraph getShapesGraph() {
        return graph;
    }
    
}
