package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.adapter.IncidentConnectionSet;
import ch.epfl.vlsc.analysis.core.adapter.VanillaConnection;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.air.Network;
import ch.epfl.vlsc.analysis.core.air.PortInstance;
import se.lth.cs.tycho.attribute.GlobalNames;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.ir.decl.GlobalEntityDecl;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.network.Instance;

import java.util.*;

public class TychoNetwork implements Network {

    private final CompilationTask compilationTask;
    private final se.lth.cs.tycho.ir.network.Network network;
    private final Map<Instance, ActorInstance> actorInstanceMapping;
    private final Set<Connection> connections;
    private final String name;

    public TychoNetwork(CompilationTask compilationTask) {
        actorInstanceMapping = new HashMap<>();
        connections = new HashSet<>();
        this.compilationTask = compilationTask;
        this.name = compilationTask.getIdentifier().toString();
        this.network = compilationTask.getNetwork();

        // -- Create Actor Instances
        for (Instance instance : network.getInstances()) {
            GlobalNames globalNames = compilationTask.getModule(GlobalNames.key);
            GlobalEntityDecl entityDecl = globalNames.entityDecl(instance.getEntityName(), true);
            assert entityDecl.getEntity() instanceof CalActor;
            CalActor calActor = (CalActor) entityDecl.getEntity();
            ActorInstance actorInstance = new TychoActorImplementation(compilationTask, instance, calActor);
            actorInstanceMapping.put(instance, actorInstance);
        }

        // -- Create Connections
        for (se.lth.cs.tycho.ir.network.Connection connection : network.getConnections()) {
            createConnection(connection);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Collection<ActorInstance> getActors() {
        return actorInstanceMapping.values();
    }

    public ActorInstance getActor(Instance instance) {
        return actorInstanceMapping.get(instance);
    }

    @Override
    public Collection<? extends Connection> getConnections() {
        return connections;
    }

    @Override
    public Collection<? extends Connection> getIncidentConnections(PortInstance port) {
        return new IncidentConnectionSet(connections, port);
    }


    private void createConnection(se.lth.cs.tycho.ir.network.Connection connection) {
        PortInstance source = getPortInstance(connection.getSource());
        PortInstance target = getPortInstance(connection.getTarget());
        connections.add(new VanillaConnection(source, target));
    }

    private PortInstance getPortInstance(se.lth.cs.tycho.ir.network.Connection.End end) {
        Optional<String> instanceName = end.getInstance();
        if (instanceName.isPresent()) {
            Optional<Instance> instance = network.getInstances().stream().filter(i -> i.getInstanceName().equals(instanceName.get())).findFirst();
            if (instance.isPresent()) {
                ActorInstance actorInstance = getActor(instance.get());
                PortInstance port = actorInstance.getPort(end.getPort());
                return port;
            }
        }
        return null;
    }

    private void createActorInstance() {

    }
}
