package ch.epfl.vlsc.analysis.classifier.kind;

import ch.epfl.vlsc.analysis.classifier.attributes.*;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;

public class Kahn implements Classifier {

    private ActorMachine actorMachine;

    private KahnProcess kahnProcess;

    public Kahn(ActorMachine actorMachine) {
        this.actorMachine = actorMachine;
        Graph graph = MultiJ.from(Graph.class).bind("actorMachine").to(actorMachine).instance();
        DecisionPath decisionPath = MultiJ.from(DecisionPath.class).bind("graph").to(graph).instance();
        Determinacy determinacy = MultiJ.from(Determinacy.class).bind("decisionPath").to(decisionPath).instance();
        Monotonicity monotonicity = MultiJ.from(Monotonicity.class)
                .bind("decisionPath").to(decisionPath)
                .bind("determinacy").to(determinacy)
                .instance();
        PortDeclaration portDeclaration = MultiJ.from(PortDeclaration.class).bind("actorMachine").to(actorMachine).instance();
        this.kahnProcess = MultiJ.from(KahnProcess.class)
                .bind("decisionPath").to(decisionPath)
                .bind("monotonicity").to(monotonicity)
                .bind("graph").to(graph)
                .bind("portDeclaration").to(portDeclaration)
                .instance();
    }


    @Override
    public String kind() {
        return "kahn";
    }

    @Override
    public boolean classify() {
        return kahnProcess.isKahnProcess(actorMachine);
    }
}
