package ch.epfl.vlsc.analysis.core.configuration;

import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.configuration.graphmodel.ConnectionEdge;
import ch.epfl.vlsc.analysis.core.configuration.graphmodel.InstanceVertex;
import ch.epfl.vlsc.configuration.Configuration;
import ch.epfl.vlsc.configuration.ConfigurationManager;
import ch.epfl.vlsc.platformutils.Emitter;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XcfToDot {

    public static void main(String[] args) {
        XcfToDot xcfInformation = new XcfToDot();
        xcfInformation.read(args);
    }

    private void printSynopsis() {
        System.err.println("Usage: XcfToDot original.xcf partitioned.xcf file.dot");
    }

    public void read(String[] args) {
        if (args.length < 3) {
            printSynopsis();
            return;
        }
        File o = new File(args[0]);
        File p = new File(args[1]);
        File w = new File(args[2]);

        ArrayList<String> colors = new ArrayList<>();
        colors.add("purple");
        colors.add("darkgreen");
        colors.add("blue");
        colors.add("red");
        colors.add("sienna1");
        colors.add("black");

        try {
            ConfigurationManager om = new ConfigurationManager(o);
            Configuration original = om.getConfiguration();
            ArrayList<String> instances = new ArrayList<>();
            for (Configuration.Partitioning.Partition partition : original.getPartitioning().getPartition()) {
                for (Configuration.Partitioning.Partition.Instance instance : partition.getInstance()) {
                    instances.add(instance.getId());
                }
            }

            ConfigurationManager pm = new ConfigurationManager(p);
            Configuration partitioned = pm.getConfiguration();
            Map<String, String> partitionColor = new HashMap<>();
            for (Configuration.Partitioning.Partition partition : partitioned.getPartitioning().getPartition()) {
                for (Configuration.Partitioning.Partition.Instance instance : partition.getInstance()) {
                    partitionColor.put(instance.getId(), colors.get((int)partition.getId()));
                }
            }


            Emitter emitter = new Emitter();
            emitter.open(w.toPath());

            emitter.emit("digraph G {");
            emitter.increaseIndentation();

            emitter.emit("node [fontsize = 100];");
            emitter.emit("graph [ranksep=\"3\"];");
            emitter.emit("rankdir=LR;");

            for (String name : instances) {
                emitter.emit("%s [shape=circle, fontcolor=white, style=filled, fillcolor=\"%s\"];", instances.indexOf(name), partitionColor.get(name));
            }

            // -- Add all edges connection
            for (Configuration.Connections.FifoConnection fifoConnection : original.getConnections().getFifoConnection()) {
                String source = fifoConnection.getSource();
                String target = fifoConnection.getTarget();

                emitter.emit("%s -> %s [arrowsize=3, weight=1, penwidth=3];", instances.indexOf(source), instances.indexOf(target));
            }


            emitter.decreaseIndentation();
            emitter.emit("}");

            emitter.close();

            for(String instance : instances){
                System.out.println(instances.indexOf(instance) + " : " + instance);
            }


        } catch (JAXBException jaxbException) {
            jaxbException.printStackTrace();
        }

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
