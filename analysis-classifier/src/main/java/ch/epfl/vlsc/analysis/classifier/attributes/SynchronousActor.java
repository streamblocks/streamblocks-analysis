package ch.epfl.vlsc.analysis.classifier.attributes;

import ch.epfl.vlsc.analysis.classifier.kind.CycloStatic;
import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.Transition;

import java.util.Map;

@Module
public interface SynchronousActor {
    @Binding(BindingKind.INJECTED)
    CycloStaticActor cycloStaticActor();

    @Binding(BindingKind.INJECTED)
    PortDeclaration portDeclaration();

    default boolean isSynchronous(ActorMachine actorMachine) {
        boolean cycloStatic = cycloStaticActor().isCycloStatic(actorMachine);
        if (!cycloStatic) {
            return false;
        }
        Map<PortDecl, Integer> inputRates = null;
        Map<PortDecl, Integer> outputRates = null;
        boolean first = true;
        for (Transition t : actorMachine.getTransitions()) {
            if (first) {
                inputRates = portDeclaration().inputRates(t);
                outputRates = portDeclaration().outputRates(t);
                first = false;
            } else {
                if (!inputRates.equals(portDeclaration().inputRates(t))) {
                    return false;
                }
                if (!outputRates.equals(portDeclaration().outputRates(t))) {
                    return false;
                }
            }
        }
        return true;
    }

}
