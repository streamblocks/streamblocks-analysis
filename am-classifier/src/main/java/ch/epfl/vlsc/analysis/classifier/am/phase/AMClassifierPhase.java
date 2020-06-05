package ch.epfl.vlsc.analysis.classifier.am.phase;

import ch.epfl.vlsc.analysis.classifier.am.kind.*;
import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.compiler.Context;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.ir.util.ImmutableList;
import se.lth.cs.tycho.phase.Phase;
import se.lth.cs.tycho.reporting.CompilationException;

import java.util.List;


public class AMClassifierPhase implements Phase {

    private List<Classifier> classifiers(ActorMachine actorMachine) {
        return ImmutableList.of(
                new Deterministic(actorMachine),
                new Monotonic(actorMachine),
                new Kahn(actorMachine),
                new CycloStatic(actorMachine),
                new Synchronous(actorMachine)
        );
    }

    @Override
    public String getDescription() {
        return "StreamBlocks Classier analysis for tycho compiler";
    }

    @Override
    public CompilationTask execute(CompilationTask task, Context context) throws CompilationException {
        GlobalNames globalNames = task.getModule(GlobalNames.key);
        for (Instance instance : task.getNetwork().getInstances()) {
            GlobalEntityDecl entityDecl = globalNames.entityDecl(instance.getEntityName(), true);
            if (!entityDecl.getExternal()) {
                ActorMachine actorMachine = (ActorMachine) entityDecl.getEntity();
                System.out.println(entityDecl.getName() + " :");
                for (Classifier classifier : classifiers(actorMachine)) {
                    System.out.println(String.format("\t%s : %b", classifier.kind(), classifier.classify()));
                }
            }
        }
        return null;
    }
}
