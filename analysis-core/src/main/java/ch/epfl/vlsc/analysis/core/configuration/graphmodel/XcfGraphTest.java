package ch.epfl.vlsc.analysis.core.configuration.graphmodel;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.flow.EdmondsKarpMFImpl;
import org.jgrapht.alg.interfaces.MinimumSTCutAlgorithm;
import org.jgrapht.alg.interfaces.StrongConnectivityAlgorithm;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;

public class XcfGraphTest {


    public static void main(String[] args) {
        XcfGraphTest xcfGraphTest = new XcfGraphTest();
        xcfGraphTest.read(args);
    }

    private void printSynopsis() {
        System.err.println("Usage: XcfGraphTest configuration.xcf");
    }


    public void read(String[] args) {
        if (args.length < 1) {
            printSynopsis();
            return;
        }
        File input = new File(args[0]);

        try {
            XcfGraph xcfGraph = new XcfGraph(input);

            Graph<InstanceVertex, ConnectionEdge> graph = xcfGraph.getGraph();

            MinimumSTCutAlgorithm<InstanceVertex, ConnectionEdge> minimumSTCutAlgorithm = new EdmondsKarpMFImpl<InstanceVertex, ConnectionEdge>(graph);

            System.out.println(minimumSTCutAlgorithm.getCutCapacity());

            StrongConnectivityAlgorithm<InstanceVertex, ConnectionEdge> scAlg =
                    new KosarajuStrongConnectivityInspector<InstanceVertex, ConnectionEdge>(graph);
            List<Graph<InstanceVertex, ConnectionEdge>> stronglyConnectedSubgraphs =
                    scAlg.getStronglyConnectedComponents();
/*
            // prints the strongly connected components
            System.out.println("Strongly connected components:");
            for (int i = 0; i < stronglyConnectedSubgraphs.size(); i++) {
                System.out.println(stronglyConnectedSubgraphs.get(i));
            }
            System.out.println();
*/


        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
