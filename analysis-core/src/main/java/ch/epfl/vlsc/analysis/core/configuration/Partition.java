package ch.epfl.vlsc.analysis.core.configuration;

import ch.epfl.vlsc.analysis.core.adapter.VanillaConnection;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.air.PortInstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Partition {

    private final Collection<ActorInstance> actors;
    private final Set<Connection> connections;
    private final Set<Connection> inputConnections;
    private final Set<Connection> outputConnections;
    private final int id;

    public Partition(int id,
                     List<ActorInstance> actorInstances,
                     Set<VanillaConnection> configurationConnections) {

        actors = new ArrayList<>();
        connections = new HashSet<>();
        inputConnections = new HashSet<>();
        outputConnections = new HashSet<>();

        this.id = id;

        actors.addAll(actorInstances);

        for (VanillaConnection connection : configurationConnections) {
            PortInstance producerPort = connection.getProducerPort();
            PortInstance consumerPort = connection.getConsumerPort();

            ActorInstance producerInstance = producerPort.getActor();
            ActorInstance consumerInstance = consumerPort.getActor();

            if (isIncludedInTheNetwork(producerInstance) && isIncludedInTheNetwork(consumerInstance)) {
                connections.add(connection);
            } else if (isIncludedInTheNetwork(producerInstance) && !isIncludedInTheNetwork(consumerInstance)) {
                outputConnections.add(connection);
            } else if (!isIncludedInTheNetwork(producerInstance) && isIncludedInTheNetwork(consumerInstance)) {
                inputConnections.add(connection);
            }
        }
    }

    public Collection<Connection> getOutputConnections() {
        return outputConnections;
    }

    private boolean isIncludedInTheNetwork(ActorInstance instance) {
        for (ActorInstance actorInstance : actors) {
            if (actorInstance.getName().equals(instance.getName())) {
                return true;
            }
        }
        return false;
    }

    public int getId() {
        return id;
    }

    public int nbrConnections() {
        return connections.size();
    }

    public int nbrInputConnections() {
        return inputConnections.size();
    }

    public int nbrOutputConnections() {
        return outputConnections.size();
    }

    public int nbrInstances() {
        return actors.size();
    }

    public Collection<Connection> getInputConnections() {
        return inputConnections;
    }
}
