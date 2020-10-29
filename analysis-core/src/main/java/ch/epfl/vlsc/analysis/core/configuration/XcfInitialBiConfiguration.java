package ch.epfl.vlsc.analysis.core.configuration;

import ch.epfl.vlsc.analysis.core.adapter.VanillaActorInstance;
import ch.epfl.vlsc.analysis.core.adapter.VanillaConnection;
import ch.epfl.vlsc.analysis.core.adapter.VanillaPortInstance;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.air.PortInstance;
import ch.epfl.vlsc.configuration.Configuration;
import ch.epfl.vlsc.configuration.ConfigurationManager;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XcfInitialBiConfiguration {
    private final String name;
    private final Map<Integer, List<ActorInstance>> partitions;
    private final Set<Connection> connections;
    private final List<ActorInstance> instances;
    private final Map<String, ActorInstance> mNameActorInstance;

    private final Map<ActorInstance, Integer> mInstancePartition;

    private final BitSet partitionBitSet;

    private final List<Integer> partitionIds;

    public XcfInitialBiConfiguration(File xcfFile) throws JAXBException {
        ConfigurationManager manager = new ConfigurationManager(xcfFile);
        Configuration configuration = manager.getConfiguration();
        name = configuration.getNetwork().getId();

        partitions = new HashMap<>();

        instances = new ArrayList<>();

        mNameActorInstance = new HashMap<>();

        connections = new HashSet<>();

        mInstancePartition = new HashMap<>();

        partitionIds = new ArrayList<>();

        // -- Add all instances of partitions
        for (Configuration.Partitioning.Partition partition : configuration.getPartitioning().getPartition()) {
            for (Configuration.Partitioning.Partition.Instance instance : partition.getInstance()) {
                VanillaActorInstance actorInstance = new VanillaActorInstance(instance.getId());
                instances.add(actorInstance);
                mNameActorInstance.put(instance.getId(), actorInstance);
                mInstancePartition.put(actorInstance, (int) partition.getId());
                Integer partId = (int) partition.getId();
                if (partitions.containsKey(partId)) {
                    partitions.get(partId).add(actorInstance);
                } else {
                    List<ActorInstance> instances = new ArrayList<>();
                    instances.add(actorInstance);
                    partitions.put(partId, instances);
                }
            }
        }

        partitionBitSet = new BitSet(instances.size());

        for (ActorInstance actorInstance : instances) {
            partitionBitSet.set(instances.indexOf(actorInstance), mInstancePartition.get(actorInstance) == 1);
            partitionIds.add(mInstancePartition.get(actorInstance) == 1 ? 1 : 0);
        }

        // -- Create all connections
        for (Configuration.Connections.FifoConnection fifoConnection : configuration.getConnections().getFifoConnection()) {
            String source = fifoConnection.getSource();
            String target = fifoConnection.getTarget();
            String sourcePort = fifoConnection.getSourcePort();
            String targetPort = fifoConnection.getTargetPort();

            ActorInstance sourceInstance = mNameActorInstance.get(source);
            ActorInstance targetInstance = mNameActorInstance.get(target);

            VanillaPortInstance sourcePortProducer = new VanillaPortInstance(sourceInstance, sourcePort, PortInstance.Direction.OUT);
            VanillaPortInstance targetPortConsumer = new VanillaPortInstance(targetInstance, targetPort, PortInstance.Direction.IN);

            if (sourceInstance.getPort(source) == null) {
                sourceInstance.getOutputPorts().add(sourcePortProducer);
            }

            if (targetInstance.getPort(target) == null) {
                targetInstance.getInputPorts().add(targetPortConsumer);
            }

            mNameActorInstance.put(source, sourceInstance);
            mNameActorInstance.put(target, targetInstance);

            VanillaConnection connection = new VanillaConnection(sourcePortProducer, targetPortConsumer);

            connections.add(connection);
        }

    }

    public int nbrPartitions() {
        return partitions.size();
    }

    public int nbrConnections() {
        return connections.size();
    }

    public List<ActorInstance> getInstances() {
        return instances;
    }

    public Set<Connection> getConnections() {
        return connections;
    }

    public BitSet getPartitionBitSet() {
        return partitionBitSet;
    }

    public String getName() {
        return name;
    }

    public Map<Integer, List<ActorInstance>> getPartitions() {
        return partitions;
    }

    public List<Integer> getPartitionIds(){
        return partitionIds;
    }
}
