package ch.epfl.vlsc.analysis.classifier.cal.phase;

import ch.epfl.vlsc.analysis.classifier.cal.adapter.TychoNetwork;
import ch.epfl.vlsc.analysis.core.actor.GenericActorAnalysis;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Network;
import ch.epfl.vlsc.analysis.core.network.GenericNetworkAnalysis;
import ch.epfl.vlsc.analysis.core.network.McdfNetworkAnalysis;
import ch.epfl.vlsc.analysis.core.network.NetworkClassifierOutput;
import ch.epfl.vlsc.analysis.core.network.ScenarioAwareNetworkAnalysis;
import ch.epfl.vlsc.analysis.core.network.SneakyNetworkAnalyzer;
import ch.epfl.vlsc.analysis.core.visualization.view.NetworkView;
import ch.epfl.vlsc.analysis.core.visualization.controllers.GenericNetworkController;
import ch.epfl.vlsc.analysis.core.visualization.controllers.NetworkController;
import com.mxgraph.view.mxGraph;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Compiler;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.EnumSetting;
import se.lth.cs.tycho.settings.ListSetting;
import se.lth.cs.tycho.settings.Setting;

import java.awt.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CalClassifierPhase implements Phase {
    public static final Setting<List<AnalysisType>> analysisKind = new ListSetting<AnalysisType>(
            new EnumSetting<AnalysisType>(AnalysisType.class) {
                @Override
                public String getKey() {
                    return "classifier-kind";
                }

                @Override
                public String getDescription() {
                    return "Classifier analysis kind";
                }

                @Override
                public AnalysisType defaultValue(Configuration configuration) {
                    throw new AssertionError();
                }
            }, ",") {
        @Override
        public List<AnalysisType> defaultValue(Configuration configuration) {
            return Collections.singletonList(AnalysisType.SDF);
        }
    };

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public List<Setting<?>> getPhaseSettings() {
        return Arrays.asList(analysisKind);
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        Path targetPath = context.getConfiguration().get(Compiler.targetPath);


        SneakyNetworkAnalyzer networkAnalyzer = new SneakyNetworkAnalyzer();
        NetworkClassifierOutput classifierOutput = new NetworkClassifierOutput(targetPath.toAbsolutePath().toString());

        Network tychoNetwork = new TychoNetwork(task);
        GenericNetworkAnalysis analysis = new GenericNetworkAnalysis(tychoNetwork, networkAnalyzer.analyze(tychoNetwork));
        classifierOutput.print(analysis);


        for (ActorInstance instance : tychoNetwork.getActors()) {
            GenericActorAnalysis ga = analysis.getGenericActorAnalysis(instance);
            System.out.println(instance.getName());
            System.out.println("\t - " + ga.getActorInstanceType().getName());
        }

        List<AnalysisType> kinds = context.getConfiguration().get(analysisKind);
        for (AnalysisType kind : kinds) {
            switch (kind) {
                case SDF:
                    if (analysis.isSingleRateStaticDaflowGraph() || analysis.isMultiRateStaticDaflowGraph()) {
                        classifierOutput.printSDFXML(tychoNetwork, analysis);
                    }
                    break;
                case CSDF:
                    if (analysis.isCycloStaticDaflowGraph()) {
                        classifierOutput.printCSDFXML(tychoNetwork, analysis);
                    }
                    break;
                case MCDF:
                    McdfNetworkAnalysis mcdfAnalysis = new McdfNetworkAnalysis(tychoNetwork, networkAnalyzer.analyze(tychoNetwork));
                    if (mcdfAnalysis.isModeControlledDataflow()) {
                        classifierOutput.printMcdfXML(tychoNetwork, mcdfAnalysis);
                    }
                    break;
                case FSMSADF:
                    ScenarioAwareNetworkAnalysis saAnalysis = new ScenarioAwareNetworkAnalysis(tychoNetwork,
                            networkAnalyzer.analyze(tychoNetwork));
                    if (saAnalysis.isScenarioAwareDataflowGraph()) {
                        classifierOutput.printSaXML(tychoNetwork, saAnalysis);
                    }
                    break;
                default:
                    break;
            }
        }

        mxGraph graph = new mxGraph();
        NetworkView view = new NetworkView(graph);
        NetworkController ctrl = new GenericNetworkController(view);
        view.setPreferredSize(new Dimension(800, 600));
        ctrl.setNetwork(tychoNetwork);
        ctrl.createFrame();
        view.zoomToFit();


        return null;
    }

    public enum AnalysisType {
        SDF("SDF"), CSDF("CSDF"), MCDF("MCDF"), FSMSADF("FSMSADF");

        private final String name;

        AnalysisType(String n) {
            name = n;
        }

        public String getName() {
            return name;
        }
    }

}
