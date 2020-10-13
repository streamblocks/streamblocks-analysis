package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.adapter.VanillaPortInstance;
import ch.epfl.vlsc.analysis.core.air.ActorImplementation;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.PortInstance;
import ch.epfl.vlsc.analysis.core.util.collections.UnionCollection;
import se.lth.cs.tycho.ir.Annotation;
import se.lth.cs.tycho.ir.AnnotationParameter;
import se.lth.cs.tycho.ir.entity.Entity;
import se.lth.cs.tycho.ir.expr.ExprLiteral;
import se.lth.cs.tycho.ir.network.Instance;

import java.util.*;

public class TychoActorInstance implements ActorInstance {

    private final Instance instance;
    private final List<PortInstance> inputPorts;
    private final List<PortInstance> outputPorts;

    private final Map<String, Map<String, String>> annotations;

    public TychoActorInstance(Instance instance, Entity entity) {
        this.instance = instance;
        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();
        annotations = new HashMap<>();
        entity.getInputPorts().forEach(portDecl -> inputPorts.add(new VanillaPortInstance(this, portDecl.getName(), PortInstance.Direction.IN)));
        entity.getOutputPorts().forEach(portDecl -> outputPorts.add(new VanillaPortInstance(this, portDecl.getName(), PortInstance.Direction.OUT)));

        for (Annotation annotation : entity.getAnnotations()) {
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
        return instance.getInstanceName();
    }

    @Override
    public Collection<PortInstance> getPorts() {
        return new UnionCollection<>(inputPorts, outputPorts);
    }

    @Override
    public Collection<PortInstance> getInputPorts() {
        return inputPorts;
    }

    @Override
    public Collection<PortInstance> getOutputPorts() {
        return outputPorts;
    }

    @Override
    public PortInstance getPort(String name) {
        Optional<PortInstance> portInstance = getPorts().stream().filter(p -> p.getName().equals(name)).findAny();
        if (portInstance.isPresent()) {
            return portInstance.get();
        }

        return null;
    }

    @Override
    public boolean hasImplementation() {
        return false;
    }

    @Override
    public ActorImplementation getImplementation() {
        return null;
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

    @Override
    public String toString() {
        return getName();
    }
}
