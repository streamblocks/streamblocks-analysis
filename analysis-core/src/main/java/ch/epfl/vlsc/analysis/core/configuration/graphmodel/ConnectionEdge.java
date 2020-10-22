package ch.epfl.vlsc.analysis.core.configuration.graphmodel;

import org.jgrapht.graph.DefaultEdge;

public class ConnectionEdge extends DefaultEdge {

    private final String source;
    private final String sourcePort;

    private final String target;
    private final String targetPort;

    public ConnectionEdge(String source, String sourcePort, String target, String targetPort){
        super();
        this.source = source;
        this.sourcePort = sourcePort;
        this.target = target;
        this.targetPort = targetPort;
    }

    @Override
    public String getSource() {
        return source;
    }

    public String getSourcePort() {
        return sourcePort;
    }

    @Override
    public String getTarget() {
        return target;
    }

    public String getTargetPort() {
        return targetPort;
    }
}

