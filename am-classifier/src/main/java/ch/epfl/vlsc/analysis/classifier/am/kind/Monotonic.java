package ch.epfl.vlsc.analysis.classifier.am.kind;

import ch.epfl.vlsc.analysis.classifier.am.attributes.DecisionPath;
import ch.epfl.vlsc.analysis.classifier.am.attributes.Determinacy;
import ch.epfl.vlsc.analysis.classifier.am.attributes.Graph;
import ch.epfl.vlsc.analysis.classifier.am.attributes.Monotonicity;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;

public class Monotonic implements Classifier {

    private final ActorMachine actorMachine;
    private final Monotonicity monotonicity;

    public Monotonic(ActorMachine actorMachine) {
        this.actorMachine = actorMachine;
        Graph graph = MultiJ.from(Graph.class).bind("actorMachine").to(actorMachine).instance();
        DecisionPath decisionPath = MultiJ.from(DecisionPath.class).bind("graph").to(graph).instance();
        Determinacy determinacy = MultiJ.from(Determinacy.class).bind("decisionPath").to(decisionPath).instance();
        this.monotonicity = MultiJ.from(Monotonicity.class)
                .bind("decisionPath").to(decisionPath)
                .bind("determinacy").to(determinacy)
                .instance();
    }

    @Override
    public String kind() {
        return "monotonic";
    }

    @Override
    public boolean classify() {
        return monotonicity.isMonotonic(actorMachine);
    }
}

