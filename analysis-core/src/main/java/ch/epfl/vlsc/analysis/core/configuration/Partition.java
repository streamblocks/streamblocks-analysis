package ch.epfl.vlsc.analysis.core.configuration;

import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.air.PortInstance;

import java.util.*;

public class Partition {

    private final Collection<ActorInstance> actors;
    private final Set<Connection> connections;
    private final Set<Connection> inputConnections;
    private final Set<Connection> outputConnections;


    private final Map<String, List<Connection>> inputPortConnections;
    private final Map<String, List<Connection>> outputPortConnections;

    private final int id;

    public Partition(int id,
                     List<ActorInstance> actorInstances,
                     Set<Connection> configurationConnections) {

        actors = new ArrayList<>();
        connections = new HashSet<>();
        inputConnections = new HashSet<>();
        outputConnections = new HashSet<>();
        inputPortConnections = new HashMap<>();
        outputPortConnections = new HashMap<>();

        this.id = id;

        actors.addAll(actorInstances);

        for (Connection connection : configurationConnections) {
            PortInstance producerPort = connection.getProducerPort();
            PortInstance consumerPort = connection.getConsumerPort();

            ActorInstance producerInstance = producerPort.getActor();
            ActorInstance consumerInstance = consumerPort.getActor();

            if (isIncludedInTheNetwork(producerInstance) && isIncludedInTheNetwork(consumerInstance)) {
                connections.add(connection);
            } else if (isIncludedInTheNetwork(producerInstance) && !isIncludedInTheNetwork(consumerInstance)) {
                outputConnections.add(connection);


                String instancePort = producerInstance.getName() + ":" + producerPort.getName();
                if (outputPortConnections.containsKey(instancePort)) {
                    outputPortConnections.get(instancePort).add(connection);
                } else {
                    List<Connection> cons = new ArrayList<>();
                    cons.add(connection);
                    outputPortConnections.put(instancePort, cons);
                }

            } else if (!isIncludedInTheNetwork(producerInstance) && isIncludedInTheNetwork(consumerInstance)) {
                inputConnections.add(connection);
                String instancePort = producerInstance.getName() + ":" + producerPort.getName();
                if (inputPortConnections.containsKey(instancePort)) {
                    inputPortConnections.get(instancePort).add(connection);
                } else {
                    List<Connection> cons = new ArrayList<>();
                    cons.add(connection);
                    inputPortConnections.put(instancePort, cons);
                }
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
        return inputPortConnections.keySet().size();
    }

    public int nbrOutputConnections() {
        return outputPortConnections.keySet().size();
    }

    public int nbrInstances() {
        return actors.size();
    }

    public Collection<Connection> getInputConnections() {
        return inputConnections;
    }

    public Collection<ActorInstance> getActors(){
        return actors;
    }
}
