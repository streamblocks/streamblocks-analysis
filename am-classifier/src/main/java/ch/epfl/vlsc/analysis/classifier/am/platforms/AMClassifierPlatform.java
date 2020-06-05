package ch.epfl.vlsc.analysis.classifier.am.platforms;

import ch.epfl.vlsc.analysis.classifier.am.phase.AMClassifierPhase;
import se.lth.cs.tycho.compiler.Compiler;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.*;
import se.lth.cs.tycho.platform.Platform;

import java.util.List;

public class AMClassifierPlatform implements Platform {

    private static final List<Phase> phases = ImmutableList.<Phase>builder()
            .addAll(Compiler.frontendPhases())
            .addAll(Compiler.networkElaborationPhases())
            .addAll(Compiler.nameAndTypeAnalysis())
            .addAll(actorMachinePhases())
            .add(new AMClassifierPhase())
            .build();

    @Override
    public String description() {
        return "Classifies every actor in a Dataflow Network";
    }

    public static List<Phase> actorMachinePhases() {
        return ImmutableList.of(
                new RenameActorVariablesPhase(),
                new LiftProcessVarDeclsPhase(),
                new ProcessToCalPhase(),
                new AddSchedulePhase(),
                new ScheduleUntaggedPhase(),
                new ScheduleInitializersPhase(),
                new MergeManyGuardsPhase(),
                new CalToAmPhase(),
                new RemoveEmptyTransitionsPhase()
        );
    }

    @Override
    public String name() {
        return "StreamBlocks Actor Machine based classifier";
    }

    @Override
    public List<Phase> phases() {
        return phases;
    }
}