package ch.epfl.vlsc.analysis.classifier.am.kind;

import ch.epfl.vlsc.analysis.classifier.am.attributes.DecisionPath;
import ch.epfl.vlsc.analysis.classifier.am.attributes.Determinacy;
import ch.epfl.vlsc.analysis.classifier.am.attributes.Graph;
import org.multij.MultiJ;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;

public class Deterministic implements Classifier {

    private final ActorMachine actorMachine;
    private final Determinacy determinacy;

    public Deterministic(ActorMachine actorMachine) {
        this.actorMachine = actorMachine;
        Graph graph = MultiJ.from(Graph.class).bind("actorMachine").to(actorMachine).instance();
        DecisionPath decisionPath = MultiJ.from(DecisionPath.class).bind("graph").to(graph).instance();
        this.determinacy = MultiJ.from(Determinacy.class).bind("decisionPath").to(decisionPath).instance();
    }


    @Override
    public String kind() {
        return "deterministic";
    }

    @Override
    public boolean classify() {
        return determinacy.isDeterministic(actorMachine);
    }
}
