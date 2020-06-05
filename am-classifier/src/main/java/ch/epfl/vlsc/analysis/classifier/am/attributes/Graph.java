package ch.epfl.vlsc.analysis.classifier.am.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.am.ctrl.*;

@Module
public interface Graph {

    @Binding(BindingKind.INJECTED)
    ActorMachine actorMachine();

    default State lookupState(int index) {
        return actorMachine().controller().getStateList().get(index);
    }

    default Condition lookupCondition(int index) {
        return actorMachine().getCondition(index);
    }

    default Transition lookupTransition(int index) {
        return actorMachine().getTransitions().get(index);
    }

    State destination(Instruction instruction);

    default State destination(Exec e) {
        return e.target();
    }

    default State destination(Wait w) {
        return w.target();
    }

    default State destinationTrue(Test t) {
        return t.targetTrue();
    }

    default State destinationFalse(Test t) {
        return t.targetFalse();
    }

    default Condition condition(Test t) {
        return actorMachine().getCondition(t.condition());
    }

    default Transition transition(Exec e) {
        return actorMachine().getTransitions().get(e.transition());
    }
}
