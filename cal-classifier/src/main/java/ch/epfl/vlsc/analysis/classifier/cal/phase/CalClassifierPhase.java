package ch.epfl.vlsc.analysis.classifier.cal.phase;

import ch.epfl.vlsc.analysis.classifier.cal.adapter.TychoNetwork;
import ch.epfl.vlsc.analysis.core.network.GenericNetworkAnalysis;
import ch.epfl.vlsc.analysis.core.network.NetworkClassifierOutput;
import ch.epfl.vlsc.analysis.core.network.SneakyNetworkAnalyzer;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.reporting.CompilationException;
import se.lth.cs.tycho.settings.Configuration;
import se.lth.cs.tycho.settings.EnumSetting;
import se.lth.cs.tycho.settings.ListSetting;
import se.lth.cs.tycho.settings.Setting;

import java.util.ArrayList;
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
        SneakyNetworkAnalyzer networkAnalyzer = new SneakyNetworkAnalyzer();
        NetworkClassifierOutput classifierOutput = new NetworkClassifierOutput();

        TychoNetwork tychoNetwork = new TychoNetwork(task);

        List<AnalysisType> kinds = context.getConfiguration().get(analysisKind);
        List<AnalysisType> kindsd = new ArrayList<>();
        for (AnalysisType kind : kinds) {
            switch (kind) {
                case SDF:
                    GenericNetworkAnalysis analysis = new GenericNetworkAnalysis(tychoNetwork, networkAnalyzer.analyze(tychoNetwork));
                    if (analysis.isSingleRateStaticDaflowGraph() || analysis.isMultiRateStaticDaflowGraph()) {
                        System.out.println("Is SDF");
                    }

                default:
                    break;
            }

        }

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
