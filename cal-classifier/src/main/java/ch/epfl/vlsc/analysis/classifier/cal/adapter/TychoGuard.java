package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.adapter.VanillaInputLookAhead;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Guard;
import ch.epfl.vlsc.analysis.core.air.InputLookAhead;
import ch.epfl.vlsc.analysis.core.air.StateVariable;
import ch.epfl.vlsc.analysis.core.util.collections.UnionOfDisjointIntervals;
import org.multij.Binding;
import org.multij.BindingKind;
import org.multij.Module;
import org.multij.MultiJ;
import se.lth.cs.tycho.attribute.ConstantEvaluator;
import se.lth.cs.tycho.attribute.Ports;
import se.lth.cs.tycho.attribute.VariableDeclarations;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.ir.decl.PatternVarDecl;
import se.lth.cs.tycho.ir.decl.VarDecl;
import se.lth.cs.tycho.ir.entity.PortDecl;
import se.lth.cs.tycho.ir.expr.*;

import java.util.*;

public class TychoGuard implements Guard {

    private final List<Expression> guards;
    private final Set<StateVariable> stateVariables;
    private final CompilationTask compilationTask;
    private final Set<InputLookAhead> lookAheads;
    private final ActorInstance actorInstance;

    public TychoGuard(CompilationTask compilationTask, List<Expression> guards, Map<String, StateVariable> stateVariableMap, ActorInstance actorInstance) {
        this.compilationTask = compilationTask;
        this.guards = guards;
        this.actorInstance = actorInstance;
        this.lookAheads = new HashSet<>();
        this.stateVariables = new HashSet<>();

        if (!guards.isEmpty()) {
            InputAndStateVariableFinder stateVariableFinder = MultiJ.from(InputAndStateVariableFinder.class)
                    .bind("actorInstance").to(actorInstance)
                    .bind("stateVariables").to(stateVariables)
                    .bind("lookaheads").to(lookAheads)
                    .bind("stateVariableMap").to(stateVariableMap)
                    .bind("declarations").to(compilationTask.getModule(VariableDeclarations.key))
                    .bind("ports").to(compilationTask.getModule(Ports.key))
                    .bind("evalutor").to(compilationTask.getModule(ConstantEvaluator.key))
                    .instance();

            // -- Find State Variables and Lookaheads used in the guard
            for (Expression expression : guards) {
                stateVariableFinder.visit(expression);
            }
        }
    }

    @Override
    public boolean dependsOnInput() {
        return !lookAheads.isEmpty();
    }

    @Override
    public Collection<InputLookAhead> getInputLookAheads() {
        return lookAheads;
    }

    @Override
    public boolean dependsOnState() {
        return !stateVariables.isEmpty();
    }

    @Override
    public Collection<StateVariable> getStateVariables() {
        return stateVariables;
    }

    @Override
    public UnionOfDisjointIntervals matchModeControlGuard() {
        if (lookAheads.size() == 1 && !dependsOnState()) {
            UnionOfDisjointIntervals result = matchModeControlGuard(guards.get(0));
            if (result == null || result.asSet().isEmpty()) {
                return null;
            }

            for (int i = 1; i < guards.size(); i++) {
                UnionOfDisjointIntervals s = matchModeControlGuard(guards.get(i));
                if (s == null) {
                    return null;
                }
                result = result.intersection(s);
                if (result.asSet().isEmpty()) {
                    return null;
                }
            }

            return result;
        } else {
            return null;
        }
    }

    @Override
    public UnionOfDisjointIntervals getScenarioAwareGuardIntervals(InputLookAhead ila) {
        UnionOfDisjointIntervals result = null;

        // Match each sub-guard in a possible conjunction of several guards
        boolean matches;
        PortPeekFinder finder = MultiJ.from(PortPeekFinder.class)
                .bind("declarations").to(compilationTask.getModule(VariableDeclarations.key))
                .bind("ports").to(compilationTask.getModule(Ports.key))
                .instance();
        for (Expression guard : guards) {
            matches = false;
            // Check if this sub-guard peeks from the given InputLookAhead 'ila'
            for (PortDecl pp : finder.visit(guard)) {
                if (pp.getName().equals(ila.getPort().getName())) {
                    matches = true;
                    break;
                }
            }
            if (matches) {
                UnionOfDisjointIntervals s = matchModeControlGuard(guard);
                if (s == null)
                    return null; // doesn't match pattern
                if (result == null)
                    result = s;
                else
                    result = result.intersection(s);
                if (result.asSet().isEmpty()) {
                    return null;
                }
            }
        }

        return result;
    }

    @Override
    public boolean isEmpty() {
        return guards.isEmpty();
    }

    private UnionOfDisjointIntervals matchModeControlGuard(Expression expression) {
        TychoPatternMatcher.matchExprBinaryOp matchExprBinaryOp = MultiJ.from(TychoPatternMatcher.matchExprBinaryOp.class).instance();
        TychoPatternMatcher.matchExprVariableOrIndex matchExprVariableOrIndex = MultiJ.from(TychoPatternMatcher.matchExprVariableOrIndex.class).instance();
        TychoPatternMatcher.matchExprLiteral matchExprLiteral = MultiJ.from(TychoPatternMatcher.matchExprLiteral.class).instance();
        ConstantEvaluator evaluator = compilationTask.getModule(ConstantEvaluator.key);

        ExprBinaryOp binExpr = matchExprBinaryOp.match(expression);

        if (binExpr != null) {
            TychoPatternMatcher.Relop relop = TychoPatternMatcher.matchRelop(binExpr.getOperations().get(0));
            if (relop != null) {
                Expression var = matchExprVariableOrIndex.match(binExpr.getOperands().get(0));
                ExprLiteral literal;

                if (var != null) {
                    literal = matchExprLiteral.match(binExpr.getOperands().get(1));
                } else {
                    var = matchExprVariableOrIndex.match(binExpr.getOperands().get(1));
                    literal = matchExprLiteral.match(binExpr.getOperands().get(0));
                    relop = relop.reverse();
                }

                boolean check = false;
                if (var != null) {
                    if (var instanceof ExprVariable) {
                        check = true;
                    } else {
                        ExprIndexer indexer = (ExprIndexer) var;
                        OptionalLong indexValue = evaluator.intValue(indexer.getIndex());
                        if (indexValue.isPresent()) {
                            check = indexValue.getAsLong() == 0L;
                        } else {
                            check = false;
                        }
                    }
                }

                if (var != null && check && literal != null) {
                    long x;

                    if (literal.getKind().equals(ExprLiteral.Kind.Integer)) {
                        x = literal.asInt().getAsInt();
                    } else {
                        return null;
                    }
                    UnionOfDisjointIntervals result = new UnionOfDisjointIntervals();
                    switch (relop) {
                        case Equal:
                            result.add(x, x);
                            break;
                        case NotEqual:
                            // This relop needs TWO disjoint intervals
                            if (x > Long.MIN_VALUE) {
                                result.add(Long.MIN_VALUE, x - 1);
                            }
                            if (x < Long.MAX_VALUE) {
                                result.add(x + 1, Long.MAX_VALUE);
                            }
                            break;
                        case LessThan:
                            if (x > Long.MIN_VALUE) {
                                result.add(Long.MIN_VALUE, x - 1);
                            }
                            break;
                        case LessThanEqual:
                            result.add(Long.MIN_VALUE, x);
                            break;
                        case GreaterThan:
                            if (x < Long.MAX_VALUE) {
                                result.add(x + 1, Long.MAX_VALUE);
                            }
                            break;
                        case GreaterThanEqual:
                            result.add(x, Long.MAX_VALUE);
                            break;
                        default:
                            assert (false);
                            return null;
                    }
                    return result;


                }

            } else if (binExpr.getOperations().get(0).equals("and") || binExpr.getOperations().get(0).equals("or")) {
                UnionOfDisjointIntervals result1 = matchModeControlGuard(binExpr.getOperands().get(0));

                if (result1 != null) {
                    UnionOfDisjointIntervals result2 = matchModeControlGuard(binExpr.getOperands().get(1));

                    if (result2 != null) {
                        return (binExpr.getOperations().get(0).equals("and")) ? result1.intersection(result2)
                                : result1.union(result2);
                    }
                }
            }
        }
        // TODO: also match unary operator "not"

        // Otherwise: not a match
        return null;
    }

    @Override
    public Map<InputLookAhead, UnionOfDisjointIntervals> matchScenarioAwareGuard() {
        if (dependsOnState() || lookAheads.size() == 0)
            return null;

        Map<InputLookAhead, UnionOfDisjointIntervals> result = new HashMap<InputLookAhead, UnionOfDisjointIntervals>();
        // For each input lookahead this guard depends on, find the interval
        for (InputLookAhead ila : lookAheads) {
            UnionOfDisjointIntervals s = getScenarioAwareGuardIntervals(ila);
            if (s == null)
                return null;
            else
                result.put(ila, s);
        }
        return result;
    }

    @Module
    interface InputAndStateVariableFinder {

        @Binding(BindingKind.INJECTED)
        ActorInstance actorInstance();

        @Binding(BindingKind.INJECTED)
        Set stateVariables();

        @Binding(BindingKind.INJECTED)
        Set lookaheads();

        @Binding(BindingKind.INJECTED)
        Map stateVariableMap();

        @Binding(BindingKind.INJECTED)
        VariableDeclarations declarations();

        @Binding(BindingKind.INJECTED)
        Ports ports();

        @Binding(BindingKind.INJECTED)
        ConstantEvaluator evalutor();


        default void visit(Expression expression) {
        }

        default void visit(ExprUnaryOp exprUnaryOp) {
            visit(exprUnaryOp.getOperand());
        }

        default void visit(ExprBinaryOp exprBinaryOp) {
            visit(exprBinaryOp.getOperands().get(0));
            visit(exprBinaryOp.getOperands().get(1));
        }

        default void visit(ExprApplication application) {
            for (Expression expr : application.getArgs()) {
                visit(expr);
            }
        }

        default void visit(ExprIndexer indexer) {
            if (indexer.getStructure() instanceof ExprVariable) {
                VarDecl decl = declarations().declaration((ExprVariable) indexer.getStructure());
                if (decl != null) {
                    if (decl instanceof PatternVarDecl) {
                        PortDecl port = ports().declaration((PatternVarDecl) decl);
                        OptionalLong index = evalutor().intValue(indexer.getIndex());
                        if (index.isPresent()) {
                            InputLookAhead lookAhead = new VanillaInputLookAhead(actorInstance().getPort(port.getName()), (int) index.getAsLong());
                            lookaheads().add(lookAhead);
                            return;
                        }
                    }
                }
            }
            visit(indexer.getStructure());
            visit(indexer.getIndex());
        }

        default void visit(ExprVariable exprVariable) {
            VarDecl decl = declarations().declaration(exprVariable);
            if (decl instanceof PatternVarDecl) {
                PortDecl port = ports().declaration((PatternVarDecl) decl);
                InputLookAhead lookAhead = new VanillaInputLookAhead(actorInstance().getPort(port.getName()), 0);
                lookaheads().add(lookAhead);
                return;
            }

            Map<String, StateVariable> stateVariableMap = stateVariableMap();
            StateVariable var = stateVariableMap.get(exprVariable.getVariable().getName());
            if (var != null) {
                stateVariables().add(var);
            }
        }
    }

    @Module
    interface PortPeekFinder {

        @Binding(BindingKind.INJECTED)
        VariableDeclarations declarations();

        @Binding(BindingKind.INJECTED)
        Ports ports();

        default List<PortDecl> visit(Expression expression) {
            return new ArrayList<>();
        }

        default List<PortDecl> visit(ExprUnaryOp exprUnaryOp) {
            return new ArrayList<>(visit(exprUnaryOp.getOperand()));
        }

        default List<PortDecl> visit(ExprBinaryOp exprBinaryOp) {
            List<PortDecl> portDecls = new ArrayList<>();
            portDecls.addAll(visit(exprBinaryOp.getOperands().get(0)));
            portDecls.addAll(visit(exprBinaryOp.getOperands().get(1)));
            return portDecls;
        }

        default List<PortDecl> visit(ExprApplication application) {
            List<PortDecl> portDecls = new ArrayList<>();
            for (Expression expr : application.getArgs()) {
                portDecls.addAll(visit(expr));
            }
            return portDecls;
        }


        default List<PortDecl> visit(ExprIndexer indexer) {
            List<PortDecl> portDecls = new ArrayList<>();
            if (indexer.getStructure() instanceof ExprVariable) {
                VarDecl decl = declarations().declaration((ExprVariable) indexer.getStructure());
                if (decl != null) {
                    if (decl instanceof PatternVarDecl) {
                        PortDecl port = ports().declaration((PatternVarDecl) decl);
                        portDecls.add(port);

                        portDecls.addAll(visit(indexer.getIndex()));
                    }
                }
            }
            portDecls.addAll(visit(indexer.getStructure()));
            portDecls.addAll(visit(indexer.getIndex()));
            return portDecls;
        }

        default List<PortDecl> visit(ExprVariable exprVariable) {
            List<PortDecl> portDecls = new ArrayList<>();
            VarDecl decl = declarations().declaration(exprVariable);
            if (decl instanceof PatternVarDecl) {
                PortDecl port = ports().declaration((PatternVarDecl) decl);
                portDecls.add(port);
            }

            return portDecls;
        }
    }


}
