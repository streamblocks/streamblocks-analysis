package ch.epfl.vlsc.analysis.classifier.attributes;

import ch.epfl.vlsc.analysis.classifier.util.DecisionPathKnowledge;
import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import se.lth.cs.tycho.ir.entity.am.ctrl.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Module
public interface DecisionPath {

    @Binding(BindingKind.INJECTED)
    Graph graph();

    default Set<DecisionPathKnowledge> decisionPaths(State s) {
        Set<DecisionPathKnowledge> result = new HashSet<>();
        for (Instruction i : s.getInstructions()) {
            Set<DecisionPathKnowledge> paths = decisionPathsIn(i);
            result.addAll(paths);
        }
        return result;
    }

    Set<DecisionPathKnowledge> decisionPathsIn(Instruction i);

    default Set<DecisionPathKnowledge> decisionPathsIn(Exec c) {
        return Collections.singleton(new DecisionPathKnowledge(c));
    }

    default Set<DecisionPathKnowledge> decisionPathsIn(Test t) {
        Set<DecisionPathKnowledge> paths = new HashSet<>();
        State stateTrue = graph().destinationTrue(t);
        State stateFalse = graph().destinationFalse(t);
        Set<DecisionPathKnowledge> pathsTrue = decisionPaths(stateTrue);
        Set<DecisionPathKnowledge> pathsFalse = decisionPaths(stateFalse);
        int conditionIndex = t.condition();
        for (DecisionPathKnowledge p : pathsTrue) {
            paths.add(p.prepend(conditionIndex, true));
        }
        for (DecisionPathKnowledge p : pathsFalse) {
            paths.add(p.prepend(conditionIndex, false));
        }
        return paths;
    }

    default Set<DecisionPathKnowledge> decisionPathsIn(Wait w) {
        return Collections.EMPTY_SET;
        //return decisionPaths(w.target());
    }
}

