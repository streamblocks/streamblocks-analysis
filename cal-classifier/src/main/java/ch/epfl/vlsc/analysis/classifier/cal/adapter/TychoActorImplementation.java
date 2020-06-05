package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.adapter.VanillaActorSchedule;
import ch.epfl.vlsc.analysis.core.adapter.VanillaPortSignature;
import ch.epfl.vlsc.analysis.core.air.*;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.entity.cal.InputPattern;
import se.lth.cs.tycho.ir.entity.cal.OutputExpression;
import se.lth.cs.tycho.ir.expr.ExprBinaryOp;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.expr.Expression;
import se.lth.cs.tycho.ir.network.Instance;
import se.lth.cs.tycho.transformation.cal2am.Schedule;

import java.util.*;

public class TychoActorImplementation extends TychoActorInstance implements ActorImplementation {

    private final List<StateVariable> stateVariables;
    private final List<Action> actions;
    private final Map<String, Action> actionMap;
    private final Map<se.lth.cs.tycho.ir.entity.cal.Action, Action> actionToTychoActionMap;
    private final TychoActionPriorityRelation priorityRelation;
    private final ActorSchedule fsm;
    private final CompilationTask compilationTask;

    public TychoActorImplementation(CompilationTask compilationTask, Instance instance, CalActor calActor) {
        super(instance, calActor);
        this.compilationTask = compilationTask;
        this.actionMap = new HashMap<>();
        this.actionToTychoActionMap = new HashMap<>();
        // -- Create state variables
        Map<String, StateVariable> stateVarMap = createStateVariables(calActor);
        stateVariables = new ArrayList<>(stateVarMap.values());

        // -- Create actions
        this.actions = new ArrayList<>();
        calActor.getActions().forEach(action -> {
            PortSignature portSignature = createPortSignature(action);
            Guard guard = new TychoGuard(compilationTask, action.getGuards(), stateVarMap, this);
            TychoAction tychoAction = new TychoAction(action, portSignature, guard);
            actions.add(tychoAction);
            actionMap.put(action.getTag().toString(), tychoAction);
            actionToTychoActionMap.put(action, tychoAction);
        });

        // -- Create priority relation between actions
        priorityRelation = new TychoActionPriorityRelation(compilationTask, calActor, actionMap);

        // -- Create Schedule/FSM
        fsm = createFsm(calActor, actionMap);

    }

    private Map<String, StateVariable> createStateVariables(CalActor calActor) {
        Map<String, StateVariable> variables = new HashMap<>();

        for (VarDecl decl : calActor.getVarDecls()) {
            StateVariable variable = new TychoStateVariable(decl);
            variables.put(variable.getName(), variable);
        }

        return variables;
    }

    private PortSignature createPortSignature(se.lth.cs.tycho.ir.entity.cal.Action action) {
        Map<PortInstance, Integer> portRates = new HashMap<>();
        for (InputPattern pattern : action.getInputPatterns()) {
            PortInstance portInstance = getPort(pattern.getPort().getName());
            int rate = pattern.getMatches().size();
            if (pattern.getRepeatExpr() != null) {
                rate = rate * sneakyConstantFolding(pattern.getRepeatExpr());
            }
            portRates.put(portInstance, rate);
        }

        for (OutputExpression outputExpression : action.getOutputExpressions()) {
            PortInstance portInstance = getPort(outputExpression.getPort().getName());
            int rate = outputExpression.getExpressions().size();
            if (outputExpression.getRepeatExpr() != null) {
                rate = rate * sneakyConstantFolding(outputExpression.getRepeatExpr());
            }
            portRates.put(portInstance, rate);
        }

        return new VanillaPortSignature(portRates);
    }


    private Integer sneakyConstantFolding(Expression expr) {
        if (expr instanceof ExprLiteral) {
            return ((ExprLiteral) expr).asInt().getAsInt();
        } else if (expr instanceof ExprBinaryOp) {
            ExprBinaryOp binaryOp = (ExprBinaryOp) expr;
            Integer left = sneakyConstantFolding(binaryOp.getOperands().get(0));
            Integer right = sneakyConstantFolding(binaryOp.getOperands().get(1));
            if (left != null && right != null && binaryOp.getOperations().equals("*")) {
                return left * right;
            }
        }
        return null;
    }

    private ActorSchedule createFsm(CalActor calActor, Map<String, Action> actionMap) {
        // -- Initialize maps
        Map<String, State> stateMap = new LinkedHashMap<>();
        Map<State, Set<Transition>> transitionMap = new HashMap<>();

        Schedule schedule = new Schedule(calActor);

        // -- Just create (initially empty) sets of transitions and priority relations to start with
        for (String state : schedule.getEligible().keySet()) {
            Set<Transition> transitions = new LinkedHashSet<>();
            PerStatePriorityRelation perStatePriorityRelation = new PerStatePriorityRelation(transitions, priorityRelation);
            State newState = new VanillaFsmState(state, transitions, perStatePriorityRelation);

            stateMap.put(state, newState);
            transitionMap.put(newState, transitions);
        }

        // -- Create the transitions
        for (String state : schedule.getEligible().keySet()) {
            List<se.lth.cs.tycho.ir.entity.cal.Action> actions = schedule.getEligible().get(state);
            State fromState = stateMap.get(state);
            Collection<Transition> transitions = transitionMap.get(fromState);
            actions.forEach(action -> {
                Set<String> target = schedule.targetState(Collections.singleton(state), action);
                State nextState = stateMap.get(target.iterator().next());
                Action tychoAction = actionToTychoActionMap.get(action);
                Transition t = new TychoTransition(fromState, nextState, tychoAction);
                transitions.add(t);
            });
        }
        State initialState = stateMap.get(schedule.getInitialState().iterator().next());
        assert (initialState != null);

        return new VanillaActorSchedule(stateMap.values(), initialState);
    }

    @Override
    public Collection<StateVariable> getStateVariables() {
        return stateVariables;
    }

    @Override
    public Collection<Action> getActions() {
        return actions;
    }

    @Override
    public ActorSchedule getSchedule() {
        return fsm;
    }

    @Override
    public boolean hasImplementation() {
        return true;
    }

    @Override
    public ActorImplementation getImplementation() {
        return this;
    }
}
