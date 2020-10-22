package ch.epfl.vlsc.analysis.core.configuration.graphmodel;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultListenableGraph;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;

public class XcfGraphVisualizer extends
        JApplet {

    private static final long serialVersionUID = 2202072534703043194L;

    private static final Dimension DEFAULT_SIZE = new Dimension(530, 320);

    private JGraphXAdapter<String, ConnectionEdge> jgxAdapter;

    private Graph<String, ConnectionEdge> graph;

    private void printSynopsis() {
        System.err.println("Usage: XcfGraphTest configuration.xcf");
    }

    /**
     * An alternative starting point for this demo, to also allow running this applet as an
     * application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args)
    {
        XcfGraphVisualizer applet = new XcfGraphVisualizer();
        applet.read(args);
        applet.init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle("JGraphT Adapter to JGraphX Demo");
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
            graph = xcfGraph.getGraph();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void init()
    {
        // create a JGraphT graph
        ListenableGraph<String, ConnectionEdge> g =
                new DefaultListenableGraph<>(graph);

        // create a visualization using JGraph, via an adapter
        jgxAdapter = new JGraphXAdapter<>(g);
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
        //layout.setUseBoundingBox(false);
        //layout.setEdgeRouting(true);
        //layout.setLevelDistance(60);
        //layout.setNodeDistance(60);

        layout.execute(jgxAdapter.getDefaultParent());
        // that's all there is to it!...
    }

}
