package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import org.multij.Module;
import se.lth.cs.tycho.ir.expr.*;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a box in which we put an assortment of rather messy pattern matching functions
 * <p>
 * Each of the methods return a  Tycho ir object on match and null otherwise.
 */
public class TychoPatternMatcher {

    private static final Map<String, Relop> sRelops = createRelops();

    private static Map<String, Relop> createRelops() {
        Map<String, Relop> result = new HashMap<>();
        result.put("=", Relop.Equal);
        result.put("!=", Relop.NotEqual);
        result.put("<", Relop.LessThan);
        result.put("<=", Relop.LessThanEqual);
        result.put(">", Relop.GreaterThan);
        result.put(">=", Relop.GreaterThanEqual);
        return result;
    }

    public static Relop matchRelop(String operatorName) {
        return sRelops.get(operatorName);
    }

    public enum Relop {
        Equal,
        NotEqual,
        LessThan,
        LessThanEqual,
        GreaterThan,
        GreaterThanEqual;

        public Relop complement() {
            switch (this) {
                case Equal:
                    return NotEqual;
                case NotEqual:
                    return Equal;
                case LessThan:
                    return GreaterThanEqual;
                case LessThanEqual:
                    return GreaterThan;
                case GreaterThan:
                    return LessThanEqual;
                default:
                case GreaterThanEqual:
                    return LessThan;
            }
        }

        public Relop reverse() {
            switch (this) {
                case Equal:
                    return Equal;
                case NotEqual:
                    return NotEqual;
                case LessThan:
                    return GreaterThan;
                case LessThanEqual:
                    return GreaterThanEqual;
                case GreaterThan:
                    return LessThan;
                default:
                case GreaterThanEqual:
                    return LessThanEqual;
            }
        }
    }

    @Module
    interface matchExprBinaryOp {
        default ExprBinaryOp match(Expression expression) {
            return null;
        }

        default ExprBinaryOp match(ExprBinaryOp exprBinaryOp) {
            return exprBinaryOp;
        }
    }

    @Module
    interface matchExprVariableOrIndex {
        default Expression match(Expression expression) {
            return null;
        }

        default Expression match(ExprVariable exprVariable) {
            return exprVariable;
        }

        default Expression match(ExprIndexer exprIndexer) {
            if (exprIndexer.getStructure() instanceof ExprVariable) {
                return exprIndexer;
            }
            return null;
        }
    }

    @Module
    interface matchExprLiteral {
        default ExprLiteral match(Expression expression) {
            return null;
        }

        default ExprLiteral match(ExprLiteral exprLiteral) {
            if (exprLiteral.getKind().equals(ExprLiteral.Kind.String)) {
                return null;
            }
            return exprLiteral;
        }
    }
}
