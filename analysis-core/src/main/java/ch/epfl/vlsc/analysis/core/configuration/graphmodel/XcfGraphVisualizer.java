package ch.epfl.vlsc.analysis.core.configuration.graphmodel;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.nio.dot.DOTExporter;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

public class XcfGraphVisualizer extends
        JApplet {

    private static final long serialVersionUID = 2202072534703043194L;

    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

    private JGraphXAdapter<InstanceVertex, ConnectionEdge> jgxAdapter;

    private Graph<InstanceVertex, ConnectionEdge> graph;

    private String fileName;

    private void printSynopsis() {
        System.err.println("Usage: XcfGraphTest configuration.xcf");
    }

    private Map<Integer, String> partitionColor;

    /**
     * An alternative starting point for this demo, to also allow running this applet as an
     * application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        XcfGraphVisualizer applet = new XcfGraphVisualizer();
        applet.read(args);
        applet.init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle(applet.fileName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }


    public void read(String[] args) {
        if (args.length < 1) {
            printSynopsis();
            return;
        }

        File input = new File(args[0]);

        try {
            XcfGraph xcfGraph = new XcfGraph(input);
            partitionColor = xcfGraph.getPartitionColor();
            fileName = input.getName();
            graph = xcfGraph.getGraph();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void init() {
        // create a JGraphT graph
        ListenableGraph<InstanceVertex, ConnectionEdge> g =
                new DefaultListenableGraph<>(graph);

        // create a visualization using JGraph, via an adapter

        jgxAdapter = new JGraphXAdapter<>(g);
        DOTExporter<InstanceVertex, ConnectionEdge> exporter = new DOTExporter<>();
        exporter.exportGraph(graph, new BufferedWriter(new PrintWriter(System.out)));


        jgxAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_NOLABEL, "1");
        jgxAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.STYLE_ROUNDED, "1");
        jgxAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.EDGESTYLE_ORTHOGONAL, "1");
        jgxAdapter.getStylesheet().getDefaultEdgeStyle().put(mxConstants.EDGESTYLE_LOOP, "1");

        jgxAdapter.getStylesheet().getDefaultVertexStyle().put(mxConstants.STYLE_ROUNDED, "1");


        mxGraphComponent component = new mxGraphComponent(jgxAdapter);
        component.setConnectable(false);
        component.getGraph().setAllowDanglingEdges(false);
        getContentPane().add(component);

        // positioning via jgraphx layouts
        mxHierarchicalLayout layout = new mxHierarchicalLayout(jgxAdapter);
        layout.setOrientation(SwingConstants.WEST);

        Map<InstanceVertex, mxICell> instanceToCell = jgxAdapter.getVertexToCellMap();
        for (InstanceVertex instance : instanceToCell.keySet()) {
            mxICell cell = instanceToCell.get(instance);
            Object[] cells = {cell};
            if (partitionColor.containsKey(instance.getPartition())) {
                jgxAdapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, partitionColor.get(instance.getPartition()), cells);
                jgxAdapter.setCellStyles(mxConstants.STYLE_FONTCOLOR, XcfGraph.encodeColor(Color.WHITE), cells);
            }

        }


        layout.execute(jgxAdapter.getDefaultParent());
    }

}
