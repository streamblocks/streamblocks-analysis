package ch.epfl.vlsc.analysis.classifier.attributes;

import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.Port;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.entity.am.ActorMachine;
import se.lth.cs.tycho.ir.entity.am.PortCondition;
import se.lth.cs.tycho.ir.entity.am.Transition;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.util.HashMap;
import java.util.Map;

@Module
public interface PortDeclaration {

    @Binding(BindingKind.INJECTED)
    ActorMachine actorMachine();

    default PortDecl lookupPort(PortCondition cond, Port port) {
        if (cond.isInputCondition()) {
            return lookupInPortList(actorMachine().getInputPorts(), port);
        } else {
            return lookupInPortList(actorMachine().getOutputPorts(), port);
        }
    }

    default Map<PortDecl, Integer> inputRates(Transition t) {
        return tokenRates(actorMachine().getInputPorts(), t.getInputRates());
    }

    default Map<PortDecl, Integer> outputRates(Transition t) {
        return tokenRates(actorMachine().getOutputPorts(), t.getOutputRates());
    }

    default Map<PortDecl, Integer> tokenRates(ImmutableList<PortDecl> portDecls, Map<Port, Integer> rates) {
        Map<PortDecl, Integer> result = new HashMap<>();
        for (Map.Entry<Port, Integer> entry : rates.entrySet()) {
            PortDecl decl = lookupInPortList(portDecls, entry.getKey());
            result.put(decl, entry.getValue());
        }
        return result;
    }

    default PortDecl lookupInPortList(ImmutableList<PortDecl> portList, Port port) {
        for (PortDecl decl : portList) {
            if (decl.getName().equals(port.getName())) {
                return decl;
            }
        }
        return null;
    }
}
