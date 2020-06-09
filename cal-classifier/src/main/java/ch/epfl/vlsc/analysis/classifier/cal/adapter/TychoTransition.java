package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.air.Action;
import ch.epfl.vlsc.analysis.core.air.Guard;
import ch.epfl.vlsc.analysis.core.air.State;
import ch.epfl.vlsc.analysis.core.air.Transition;

public class TychoTransition implements Transition {

    private final State sourceState;
    private final State targetState;
    private final Action action;

    public TychoTransition(State sourceState, State targetState, Action action) {
        this.sourceState = sourceState;
        this.targetState = targetState;
        this.action = action;
    }

    @Override
    public State getSourceState() {
        return sourceState;
    }

    @Override
    public State getTargetState() {
        return targetState;
    }

    @Override
    public boolean hasGuard() {
        return !action.getGuard().isEmpty();
    }

    @Override
    public Guard getGuard() {
        return action.getGuard();
    }

    @Override
    public Action getAction() {
        return action;
    }
}
