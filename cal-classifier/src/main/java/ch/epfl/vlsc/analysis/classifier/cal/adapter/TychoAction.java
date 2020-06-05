package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.air.Action;
import ch.epfl.vlsc.analysis.core.air.Guard;
import ch.epfl.vlsc.analysis.core.air.PortSignature;
import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.AnnotationParameter;
import se.lth.cs.tycho.ir.expr.ExprLiteral;

import java.util.HashMap;
import java.util.Map;

public class TychoAction implements Action {

    private final se.lth.cs.tycho.ir.entity.cal.Action action;
    private final PortSignature portSignature;
    private final Guard guard;
    private final Map<String, Map<String, String>> annotations;

    public TychoAction(se.lth.cs.tycho.ir.entity.cal.Action action, PortSignature portSignature, Guard guard) {
        this.action = action;
        this.portSignature = portSignature;
        this.guard = guard;
        this.annotations = new HashMap<>();

        for (Annotation annotation : action.getAnnotations()) {
            Map<String, String> annotationParameter = new HashMap<String, String>();
            for (AnnotationParameter parameter : annotation.getParameters()) {
                String value = "";
                if (parameter.getExpression() instanceof ExprLiteral) {
                    value = ((ExprLiteral) parameter.getExpression()).getText();
                }
                annotationParameter.put(parameter.getName(), value);
            }
            annotations.put(annotation.getName(), annotationParameter);
        }
    }

    @Override
    public String getName() {
        return action.getTag() != null ? action.getTag().toString() : "untagged";
    }

    @Override
    public PortSignature getPortSignature() {
        return portSignature;
    }

    @Override
    public boolean hasGuard() {
        return guard != null;
    }

    @Override
    public Guard getGuard() {
        return guard;
    }

    @Override
    public boolean hasAnnotation(String annotation) {
        return annotations.containsKey(annotation);
    }

    @Override
    public Map<String, String> getAnnotationArguments(String annotation) {
        return annotations.get(annotation);
    }

    @Override
    public String getAnnotationArgumentValue(String annotation, String argId) {
        Map<String, String> annotationArguments = getAnnotationArguments(annotation);
        if (annotationArguments != null)
            return annotationArguments.get(argId);
        return null;
    }
}
