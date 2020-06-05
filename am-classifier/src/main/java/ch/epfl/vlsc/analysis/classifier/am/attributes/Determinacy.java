package ch.epfl.vlsc.analysis.classifier.am.attributes;

import ch.epfl.vlsc.analysis.classifier.am.util.DecisionPathKnowledge;
import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.ctrl.Exec;
import se.lth.cs.tycho.ir.entity.am.ctrl.Instruction;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;

import java.util.HashSet;
import java.util.Set;

@Module
public interface Determinacy {

    @Binding(BindingKind.INJECTED)
    DecisionPath decisionPath();

    @Binding(BindingKind.LAZY)
    default Set<State> builder() {
        return new HashSet<>();
    }

    default boolean isDeterministic(ActorMachine actorMachine) {
        builder().add(actorMachine.controller().getInitialState());
        accept(actorMachine);
        for (State s : builder()) {
            if (!isDeterministic(s)) {
                return false;
            }
        }
        return true;
    }

    default boolean isDeterministic(State s) {
        Set<DecisionPathKnowledge> paths = decisionPath().decisionPaths(s);
        for (DecisionPathKnowledge pathA : paths) {
            for (DecisionPathKnowledge pathB : paths) {
                if (pathA.getDestination().target() != pathB.getDestination().target()) {
                    boolean trueInAFalseInB = pathA.getTrueConditions().intersects(pathB.getFalseConditions());
                    boolean falseInATrueInB = pathA.getFalseConditions().intersects(pathB.getTrueConditions());
                    boolean differentResult = trueInAFalseInB || falseInATrueInB;
                    if (!differentResult) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    default void accept(ActorMachine actorMachine) {
        for (State state : actorMachine.controller().getStateList()) {
            for (Instruction instruction : state.getInstructions()) {
                if (instruction instanceof Exec) {
                    builder().add(((Exec) instruction).target());
                }
            }
        }
    }
}