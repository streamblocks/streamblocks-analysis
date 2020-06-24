package ch.epfl.vlsc.analysis.core.weights;

import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.trace.ArtNetworkDescription;
import ch.epfl.vlsc.analysis.core.trace.ArtNetworkDescriptionReader;
import ch.epfl.vlsc.analysis.core.trace.ArtTraceEvent;
import ch.epfl.vlsc.analysis.core.util.io.ErrorConsole;
import ch.epfl.vlsc.analysis.core.util.io.StdErrorConsole;
import ch.epfl.vlsc.analysis.core.util.io.XmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActionWeightReader extends XmlReader {

    private final ErrorConsole errorConsole;
    private final List<ActionWeight> actionWeights;
    private final ArtTraceEvent mLastEvent;
    private int mCpuIndex;

    public ActionWeightReader() {
        mLastEvent = null;
        errorConsole = new StdErrorConsole();
        actionWeights = new ArrayList<>();
    }

    public static void main(String[] args) {
        ActionWeightReader reader = new ActionWeightReader();
        reader.read(args);

    }

    private void printSynopsis() {
        System.err.println("Usage: ActionWeightReader net_trace.xml <trace-file>...");
    }

    private ArtNetworkDescription readNetwork(String fileName) {
        ArtNetworkDescriptionReader reader = new ArtNetworkDescriptionReader(errorConsole);
        File input = new File(fileName);
        return reader.readNetworkDescription(input);
    }

    public void readWeights(String[] args, int first) {
        for (int i = first + 1; i < args.length; ++i) {
            File input = new File(args[i]);
            Document doc = readDocument(input);
            System.out.println("Document loaded");
            for (Node node = doc.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (isTag(node, "execution-trace")) {
                    mCpuIndex = i;
                    readExecutionTrace((Element) node);
                }
            }
        }
    }

    private void readExecutionTrace(Element element) {
        for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            ArtTraceEvent event = null;

            if (isTag(node, "trace")) {
                readTraceEvent((Element) node);
            } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                readOther((Element) node);
            }

        }
    }

    private void readTraceEvent(Element element) {
        Long timestamp = getRequiredLongAttribute(element, "timestamp");

        if (timestamp != null) {
            Integer step = getIntegerAttribute(element, "step");
            Integer action = getIntegerAttribute(element, "action");
            Integer execTime = getIntegerAttribute(element, "exectime");
            if (step != null && action != null) {
                actionWeights.get(action).update(execTime);
            }
        }
    }

    private ArtTraceEvent readOther(Element element) {
        return null;
    }

    public void read(String[] args) {
        if (args.length < 2) {
            printSynopsis();
            return;
        }

        ArtNetworkDescription network = readNetwork(args[0]);

        for (Integer key : network.getActionMap().keySet()) {
            actionWeights.add(key, new ActionWeight(key));
        }

        readWeights(args, 0);
        System.out.println("Weights have been read");

        actionWeights.forEach(w -> w.finalize(true));

        System.out.println("<?xml version=\"1.0\" ?>");
        String n = String.format("<network name=\"%s\">", network.getName());
        System.out.println(n);
        for (ActorInstance instance : network.getActors()) {
            System.out.println("\t<actor id=\"" + instance.getInstanceName() + "\">");
            for (ActionWeight weight : actionWeights) {
                String qidActionName = network.getAction(weight.getActionId()).getName();
                if (qidActionName.startsWith(instance.getInstanceName() + ".")) {
                    String actionName = qidActionName.replace(instance.getInstanceName() + ".", "");
                    String w = String.format("\t\t<action id=\"%s\" clockcycles=\"%.1f\" clockcycles-min=\"%.1f\" clockcycles-max=\"%.1f\"/>",
                            actionName,
                            weight.getFirings() > 0 ? weight.getAverage() : 0,
                            (float) weight.getMin(),
                            (float) weight.getMax()
                    );
                    String w2 = String.format("\t\t<action id=\"%s\" clockcycles=\"%.1f\" clockcycles-min=\"%.1f\" clockcycles-max=\"%.1f\" variance=\"%.1f\" firings=\"%d\" dropped-firings=\"%d\"/>",
                            actionName,
                            weight.getFirings() > 0 ? weight.getAverage() : 0,
                            (float) weight.getMin(),
                            (float) weight.getMax(),
                            weight.getVariance(),
                            weight.getFirings(),
                            weight.getFirings() - weight.getFilteredFirings()
                    );

                    System.out.println(w);
                }
            }
            System.out.println("\t</actor>");
        }
        System.out.println("</network>");

        int viz = 20;

        String name = network.getAction(actionWeights.get(viz).getActionId()).getName();

        WeightsVisualization.visulize(name + " filtered", actionWeights.get(viz).getPlot(true));

        WeightsVisualization.visulize(name, actionWeights.get(viz).getPlot(false));

    }
}
