package ch.epfl.vlsc.analysis.core.configuration.graphmodel;

import ch.epfl.vlsc.configuration.Configuration;
import ch.epfl.vlsc.configuration.ConfigurationManager;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedPseudograph;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XcfGraph {
    private final String name;

    private final Graph<InstanceVertex, ConnectionEdge> graph;

    private final Map<String, InstanceVertex> instanceVertexes;

    private final Map<Integer, String> partitionColor;

    public XcfGraph(File xcfFile) throws JAXBException {
        ConfigurationManager manager = new ConfigurationManager(xcfFile);
        Configuration configuration = manager.getConfiguration();
        name = configuration.getNetwork().getId();

        graph = new DirectedPseudograph<InstanceVertex, ConnectionEdge>(ConnectionEdge.class);

        instanceVertexes = new HashMap<>();

        partitionColor = new HashMap<>();

        // -- Add all nodes instances
        int i =0;
        for (Configuration.Partitioning.Partition partition : configuration.getPartitioning().getPartition()) {
            for (Configuration.Partitioning.Partition.Instance instance : partition.getInstance()) {
                InstanceVertex instanceVertex = new InstanceVertex(String.valueOf(i), partition.getId());
                graph.addVertex(instanceVertex);
                instanceVertexes.put(instance.getId(), instanceVertex);
                i++;
            }
            partitionColor.put((int) partition.getId(), encodeColor(hashColor(partition)));
        }


        // -- Add all edges connection
        for (Configuration.Connections.Connection fifoConnection : configuration.getConnections().getConnection()) {
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

    public String getName() {
        return name;
    }

    public Map<Integer, String> getPartitionColor() {
        return partitionColor;
    }

    /**
     * Get an RGB color from object hash code
     *
     * @param value
     * @return
     */
    private Color hashColor(Object value) {
        if (value == null) {
            return Color.WHITE.darker();
        } else {
            int r = 0xff - (Math.abs(1 + value.hashCode()) % 0xce);
            int g = 0xff - (Math.abs(1 + value.hashCode()) % 0xdd);
            int b = 0xff - (Math.abs(1 + value.hashCode()) % 0xec);
            return new Color(r, g, b);
        }
    }

    /**
     * @return a hex Color string in the format #rrggbb.
     */
    public static String encodeColor(Color color) {
        return "#" + String.format("%06x", color.getRGB() & 0xffffff);

    }


}