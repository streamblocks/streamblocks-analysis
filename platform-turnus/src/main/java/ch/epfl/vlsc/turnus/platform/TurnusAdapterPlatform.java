package ch.epfl.vlsc.turnus.platform;

import ch.epfl.vlsc.turnus.phase.TurnusAdapterPhase;
import se.lth.cs.tycho.compiler.Compiler;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.*;
import se.lth.cs.tycho.platform.Platform;

import java.util.List;

public class TurnusAdapterPlatform implements Platform {
    private static final List<Phase> phases = ImmutableList.<Phase>builder()
            .addAll(Compiler.frontendPhases())
            .addAll(Compiler.networkElaborationPhases())
            .addAll(Compiler.nameAndTypeAnalysis())
            .addAll(prepareActorPhases())
            .add(new TurnusAdapterPhase())
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
        return "StreamBlocks Turnus Adapter";
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
