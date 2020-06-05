package ch.epfl.vlsc.analysis.classifier.am.kind;

import ch.epfl.vlsc.analysis.classifier.am.attributes.*;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;

public class Synchronous implements Classifier {

    private final ActorMachine actorMachine;
    private final SynchronousActor synchronousActor;

    public Synchronous(ActorMachine actorMachine) {
        this.actorMachine = actorMachine;

        Graph graph = MultiJ.from(Graph.class).bind("actorMachine").to(actorMachine).instance();

        DecisionPath decisionPath = MultiJ.from(DecisionPath.class).bind("graph").to(graph).instance();

        Determinacy determinacy = MultiJ.from(Determinacy.class).bind("decisionPath").to(decisionPath).instance();

        Monotonicity monotonicity = MultiJ.from(Monotonicity.class)
                .bind("decisionPath").to(decisionPath)
                .bind("determinacy").to(determinacy)
                .instance();

        PortDeclaration portDeclaration = MultiJ.from(PortDeclaration.class).bind("actorMachine").to(actorMachine).instance();

        KahnProcess kahnProcess = MultiJ.from(KahnProcess.class)
                .bind("decisionPath").to(decisionPath)
                .bind("monotonicity").to(monotonicity)
                .bind("graph").to(graph)
                .bind("portDeclaration").to(portDeclaration)
                .instance();

        CycloStaticActor cycloStaticActor = MultiJ.from(CycloStaticActor.class).bind("kahnProcess").to(kahnProcess).instance();

        this.synchronousActor = MultiJ.from(SynchronousActor.class)
                .bind("portDeclaration").to(portDeclaration)
                .bind("cycloStaticActor").to(cycloStaticActor)
                .instance();
    }


    @Override
    public String kind() {
        return "synchronous";
    }

    @Override
    public boolean classify() {
        return synchronousActor.isSynchronous(actorMachine);
    }
}
