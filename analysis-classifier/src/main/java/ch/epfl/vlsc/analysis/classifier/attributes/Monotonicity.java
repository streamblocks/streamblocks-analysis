package ch.epfl.vlsc.analysis.classifier.attributes;

import ch.epfl.vlsc.analysis.classifier.util.DecisionPathKnowledge;
import ch.epfl.vlsc.analysis.classifier.util.ImmutableBitSet;
import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;

import java.util.BitSet;
import java.util.Set;

@Module
public interface Monotonicity {

    @Binding(BindingKind.INJECTED)
    DecisionPath decisionPath();

    @Binding(BindingKind.INJECTED)
    Determinacy determinacy();

    default boolean isMonotonic(ActorMachine actorMachine) {
        if (determinacy().isDeterministic(actorMachine)) {
            ImmutableBitSet portConds = portConditions(actorMachine);
            for (State s : actorMachine.controller().getStateList()) {
                Set<DecisionPathKnowledge> paths = decisionPath().decisionPaths(s);
                for (DecisionPathKnowledge path : paths) {
                    if (path.getFalseConditions().intersects(portConds)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    default ImmutableBitSet portConditions(ActorMachine actorMachine) {
        BitSet portConds = new BitSet();
        int i = 0;
        for (Condition c : actorMachine.getConditions()) {
            if (c instanceof PortCondition) {
                portConds.set(i);
            }
            i += 1;
        }
        return ImmutableBitSet.fromBitSet(portConds);
    }

    default ImmutableBitSet predicateConditions(ActorMachine actorMachine) {
        BitSet predConds = new BitSet();
        int i = 0;
        for (Condition c : actorMachine.getConditions()) {
            if (c instanceof PredicateCondition) {
                predConds.set(i);
            }
            i += 1;
        }
        return ImmutableBitSet.fromBitSet(predConds);
    }

}