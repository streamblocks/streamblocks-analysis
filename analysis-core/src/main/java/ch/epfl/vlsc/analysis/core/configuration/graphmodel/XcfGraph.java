package ch.epfl.vlsc.analysis.core.configuration.graphmodel;

import ch.epfl.vlsc.configuration.Configuration;
import ch.epfl.vlsc.configuration.ConfigurationManager;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;

import javax.xml.bind.JAXBException;
import java.io.File;

public class XcfGraph {
    private final String name;

    private final Graph<String, ConnectionEdge> graph;

    public XcfGraph(File xcfFile) throws JAXBException {
        ConfigurationManager manager = new ConfigurationManager(xcfFile);
        Configuration configuration = manager.getConfiguration();
        name = configuration.getNetwork().getId();

        graph = new DirectedPseudograph<String, ConnectionEdge>(ConnectionEdge.class);

        // -- Add all nodes instances
        for (Configuration.Partitioning.Partition partition : configuration.getPartitioning().getPartition()) {
            for (Configuration.Partitioning.Partition.Instance instance : partition.getInstance()) {
                graph.addVertex(instance.getId());
            }
        }

        // -- Add all edges connection
        for (Configuration.Connections.FifoConnection fifoConnection : configuration.getConnections().getFifoConnection()) {
            String source = fifoConnection.getSource();
            String target = fifoConnection.getTarget();
            String sourcePort = fifoConnection.getSourcePort();
            String targetPort = fifoConnection.getTargetPort();

            graph.addEdge(source, target, new ConnectionEdge(source, sourcePort, target, targetPort));
        }
    }

    public Graph<String, ConnectionEdge> getGraph() {
        return graph;
    }

}