package ch.epfl.vlsc.analysis.core.configuration.graphmodel;

import ch.epfl.vlsc.configuration.Configuration;
import ch.epfl.vlsc.configuration.ConfigurationManager;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XcfGraph {
    private final String name;

    private final Graph<InstanceVertex, ConnectionEdge> graph;

    private final Map<String, InstanceVertex> instanceVertexes;

    public XcfGraph(File xcfFile) throws JAXBException {
        ConfigurationManager manager = new ConfigurationManager(xcfFile);
        Configuration configuration = manager.getConfiguration();
        name = configuration.getNetwork().getId();

        graph = new DirectedPseudograph<InstanceVertex, ConnectionEdge>(ConnectionEdge.class);

        instanceVertexes = new HashMap<>();

        // -- Add all nodes instances
        for (Configuration.Partitioning.Partition partition : configuration.getPartitioning().getPartition()) {
            for (Configuration.Partitioning.Partition.Instance instance : partition.getInstance()) {
                InstanceVertex instanceVertex = new InstanceVertex(instance.getId(), partition.getId());
                graph.addVertex(instanceVertex);
                instanceVertexes.put(instance.getId(), instanceVertex);
            }
        }

        // -- Add all edges connection
        for (Configuration.Connections.FifoConnection fifoConnection : configuration.getConnections().getFifoConnection()) {
            String source = fifoConnection.getSource();
            String target = fifoConnection.getTarget();
            String sourcePort = fifoConnection.getSourcePort();
            String targetPort = fifoConnection.getTargetPort();

            graph.addEdge(instanceVertexes.get(source), instanceVertexes.get(target), new ConnectionEdge(source, sourcePort, target, targetPort));
        }
    }

    public Graph<InstanceVertex, ConnectionEdge> getGraph() {
        return graph;
    }

    public String getName(){
        return name;
    }

}