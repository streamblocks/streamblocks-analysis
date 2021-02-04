package ch.epfl.vlsc.analysis.core.configuration;

import ch.epfl.vlsc.analysis.core.adapter.VanillaConnection;
import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.configuration.Configuration;
import ch.epfl.vlsc.configuration.ConfigurationManager;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;
import java.util.Set;

public class BiPartitionXcfWriter {
    private final String networkName;
    private final List<ActorInstance> instancesInZero;
    private final List<ActorInstance> instancesInOne;
    private final Set<Connection> connections;


    public BiPartitionXcfWriter(String networkName, List<ActorInstance> instancesInZero, List<ActorInstance> instancesInOne, Set<Connection> connections) {
        this.networkName = networkName;
        this.instancesInZero = instancesInZero;
        this.instancesInOne = instancesInOne;
        this.connections = connections;
    }

    public void write(File xcfFile) {
        Configuration xcf = new Configuration();

        // -- Set Configuration Network
        Configuration.Network xcfNetwork = new Configuration.Network();
        xcfNetwork.setId(networkName);
        xcf.setNetwork(xcfNetwork);

        // -- Create Partition Zero
        Configuration.Partitioning partitioning = new Configuration.Partitioning();
        Configuration.Partitioning.Partition partitionZero = new Configuration.Partitioning.Partition();

        partitionZero.setId((short) 0);
        partitionZero.setPe("x86_64");
        partitionZero.setCodeGenerator("sw");
        partitionZero.setHost(true);

        for (ActorInstance actorInstance : instancesInZero) {
            Configuration.Partitioning.Partition.Instance xcfInstance = new Configuration.Partitioning.Partition.Instance();
            xcfInstance.setId(actorInstance.getName());
            partitionZero.getInstance().add(xcfInstance);
        }

        partitioning.getPartition().add(partitionZero);
        xcf.setPartitioning(partitioning);

        // -- Create Partition One
        Configuration.Partitioning.Partition partitionOne = new Configuration.Partitioning.Partition();

        partitionOne.setId((short) 1);
        partitionOne.setPe("FPGA");
        partitionOne.setCodeGenerator("hw");

        for (ActorInstance actorInstance : instancesInOne) {
            Configuration.Partitioning.Partition.Instance xcfInstance = new Configuration.Partitioning.Partition.Instance();
            xcfInstance.setId(actorInstance.getName());
            partitionOne.getInstance().add(xcfInstance);
        }

        partitioning.getPartition().add(partitionOne);
        xcf.setPartitioning(partitioning);

        // -- Code generator
        Configuration.CodeGenerators xcfCodeGenerators = new Configuration.CodeGenerators();

        // -- SW Code generator
        Configuration.CodeGenerators.CodeGenerator xcfSwCodeGenerator = new Configuration.CodeGenerators.CodeGenerator();
        xcfSwCodeGenerator.setId("sw");
        xcfSwCodeGenerator.setPlatform("multicore");
        xcfCodeGenerators.getCodeGenerator().add(xcfSwCodeGenerator);

        // -- HW Code generator
        Configuration.CodeGenerators.CodeGenerator xcfHwCodeGenerator = new Configuration.CodeGenerators.CodeGenerator();
        xcfHwCodeGenerator.setId("hw");
        xcfHwCodeGenerator.setPlatform("vivado-hls");
        xcfCodeGenerators.getCodeGenerator().add(xcfHwCodeGenerator);

        xcf.setCodeGenerators(xcfCodeGenerators);

        // -- Create connections

        Configuration.Connections xcfConnections = new Configuration.Connections();
        for (Connection vanillaConnection : connections) {
            Configuration.Connections.Connection fifoConnection = new Configuration.Connections.Connection();
            fifoConnection.setSize((long)4096);
            fifoConnection.setSource(((VanillaConnection) vanillaConnection).getFirst().getName());
            fifoConnection.setSourcePort(vanillaConnection.getConsumerPort().getName());
            fifoConnection.setTarget(((VanillaConnection) vanillaConnection).getSecond().getName());
            fifoConnection.setTargetPort(vanillaConnection.getProducerPort().getName());
            xcfConnections.getConnection().add(fifoConnection);
        }

        xcf.setConnections(xcfConnections);
        try {
            ConfigurationManager.write(xcfFile, xcf);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
