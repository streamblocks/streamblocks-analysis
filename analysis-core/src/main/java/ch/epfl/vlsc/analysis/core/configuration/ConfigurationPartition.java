package ch.epfl.vlsc.analysis.core.configuration;

import ch.epfl.vlsc.analysis.core.adapter.IncidentConnectionSet;
import ch.epfl.vlsc.analysis.core.adapter.VanillaActorInstance;
import ch.epfl.vlsc.analysis.core.adapter.VanillaConnection;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.air.Network;
import ch.epfl.vlsc.analysis.core.air.PortInstance;
import ch.epfl.vlsc.configuration.Configuration;

import java.util.*;

public class ConfigurationPartition implements Network {

    private final Collection<ActorInstance> actors;
    private final Set<Connection> connections;
    private final Set<Connection> inputConnections;
    private final Set<Connection> outputConnections;
    private final int id;


    @Override
    public String getName() {
        return null;
    }

    public Collection<ActorInstance> getActors(){
        return actors;
    }

    @Override
    public Collection<? extends Connection> getConnections() {
        return connections;
    }

    public ConfigurationPartition(Map<String, ActorInstance> mNameActorInstance, Set<VanillaConnection> configurationConnections, Configuration.Partitioning.Partition partition) {
        actors = new ArrayList<>();
        connections = new HashSet<>();
        inputConnections = new HashSet<>();
        outputConnections = new HashSet<>();

        id = partition.getId();

        for (Configuration.Partitioning.Partition.Instance instance : partition.getInstance()) {
            actors.add(mNameActorInstance.get(instance.getId()));
        }

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

    public Collection<Connection> getInputConnections() {
        return inputConnections;
    }

    @Override
    public Collection<? extends Connection> getIncidentConnections(PortInstance port) {
        return new IncidentConnectionSet(connections, port);
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



    public int nbrConnections(){
        return connections.size();
    }

    public int nbrInputConnections(){
        return inputConnections.size();
    }

    public int nbrOutputConnections(){
        return outputConnections.size();
    }

    public int nbrInstances(){
        return actors.size();
    }


}
