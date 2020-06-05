package ch.epfl.vlsc.analysis.classifier.am.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PredicateCondition;

@Module
public interface CycloStaticActor {

    @Binding(BindingKind.INJECTED)
    KahnProcess kahnProcess();

    default boolean isCycloStatic(ActorMachine actorMachine) {
        boolean kahn = kahnProcess().isKahnProcess(actorMachine);
        if (!kahn) {
            return false;
        }
        for (Condition cond : actorMachine.getConditions()) {
            if (cond instanceof PredicateCondition) {
                return false;
            }
        }
        return true;
    }

}
