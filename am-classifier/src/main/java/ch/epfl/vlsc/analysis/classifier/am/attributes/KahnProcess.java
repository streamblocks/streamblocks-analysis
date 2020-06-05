package ch.epfl.vlsc.analysis.classifier.am.attributes;

import ch.epfl.vlsc.analysis.classifier.am.util.DecisionPathKnowledge;
import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Condition;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.entity.am.ctrl.State;

import java.util.Map;
import java.util.Set;

@Module
public interface KahnProcess {
    @Binding(BindingKind.INJECTED)
    DecisionPath decisionPath();

    @Binding(BindingKind.INJECTED)
    Monotonicity monotonicity();

    @Binding(BindingKind.INJECTED)
    Graph graph();

    @Binding(BindingKind.INJECTED)
    PortDeclaration portDeclaration();

    default boolean isKahnProcess(ActorMachine actorMachine) {
        if (monotonicity().isMonotonic(actorMachine)) {
            for (State s : actorMachine.controller().getStateList()) {
                boolean kahn = isKahnProcess(s);
                if (!kahn) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    default boolean isKahnProcess(State s) {
        Set<DecisionPathKnowledge> paths = decisionPath().decisionPaths(s);
        for (DecisionPathKnowledge path : paths) {
            Transition t = graph().transition(path.getDestination());
            Map<PortDecl, Integer> inputRates = portDeclaration().inputRates(t);
            for (int c : path.getTrueConditions()) {
                Condition cond = graph().lookupCondition(c);
                if (cond instanceof PortCondition) {
                    PortCondition p = (PortCondition) cond;
                    if (p.isInputCondition()) {
                        PortDecl port = portDeclaration().lookupPort(p, p.getPortName());
                        int transitionInputRate = portDeclaration().inputRates(t).containsKey(port) ? inputRates.get(port) : 0;
                        if (transitionInputRate < p.N()) {
                            return false;
                        }
                    }
                }
            }

        }
        return true;
    }

}
