package ch.epfl.vlsc.analysis.classifier.cal.platforms;

import ch.epfl.vlsc.analysis.classifier.cal.phase.CalClassifierPhase;
import se.lth.cs.tycho.compiler.Compiler;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.AddSchedulePhase;
import se.lth.cs.tycho.phase.LiftProcessVarDeclsPhase;
import se.lth.cs.tycho.phase.MergeManyGuardsPhase;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.phase.ProcessToCalPhase;
import se.lth.cs.tycho.phase.RenameActorVariablesPhase;
import se.lth.cs.tycho.phase.ScheduleInitializersPhase;
import se.lth.cs.tycho.phase.ScheduleUntaggedPhase;
import se.lth.cs.tycho.platform.Platform;

import java.util.List;

public class CalClassifierPlatform implements Platform {
    private static final List<Phase> phases = ImmutableList.<Phase>builder()
            .addAll(Compiler.frontendPhases())
            .addAll(Compiler.templatePhases())
            .addAll(Compiler.networkElaborationPhases())
            .addAll(Compiler.nameAndTypeAnalysis())
            .addAll(prepareActorPhases())
            .add(new CalClassifierPhase())
            .build();

    public static List<Phase> prepareActorPhases() {
        return ImmutableList.of(
                new RenameActorVariablesPhase(),
                new LiftProcessVarDeclsPhase(),
                new ProcessToCalPhase(),
                new AddSchedulePhase(),
                new ScheduleUntaggedPhase(),
                new ScheduleInitializersPhase(),
                new MergeManyGuardsPhase()
        );
    }

    @Override
    public String name() {
        return "StreamBlocks classifier";
    }

    @Override
    public String description() {
        return "Classifies every actor in a Dataflow Network";
    }

    @Override
    public List<Phase> phases() {
        return phases;
    }
}
