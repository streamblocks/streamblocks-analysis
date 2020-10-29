package ch.epfl.vlsc.analysis.core.configuration;

import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XcfBiPartitionInformation {

    public static void main(String[] args) {
        XcfBiPartitionInformation xcfInformation = new XcfBiPartitionInformation();
        xcfInformation.read(args);
    }

    private void printSynopsis() {
        System.err.println("Usage: XcfInformation configuration.xcf");
    }

    public void read(String[] args) {
        if (args.length < 1) {
            printSynopsis();
            return;
        }
        File input = new File(args[0]);
        try {
            XcfInitialBiConfiguration configuration = new XcfInitialBiConfiguration(input);

            System.out.println("Number of Partitions : " + configuration.nbrPartitions());
            System.out.println("Number of Connections : " + configuration.nbrConnections());
            System.out.println();

            List<ActorInstance> actorInstances = configuration.getInstances();

            Set<Connection> connections = configuration.getConnections();

            Map<Integer, List<ActorInstance>> partitions =  configuration.getPartitions();


            Partition partitionZero = new Partition(0, partitions.get(0), connections);
            Partition partitionOne = new Partition(1, partitions.get(1), connections);

            System.out.println("Partition : " + 0);
            System.out.println("\tInstances : " + partitionZero.getActors().size());
            System.out.println("\tConnections : " + partitionZero.nbrConnections());
            System.out.println("\tInput Connections : " + partitionZero.nbrInputConnections());
            System.out.println("\tOutput Connections : " + partitionZero.nbrOutputConnections());

            System.out.println("Partition : " + 1);
            System.out.println("\tInstances : " + partitionOne.getActors().size());
            System.out.println("\tConnections : " + partitionOne.nbrConnections());
            System.out.println("\tInput Connections : " + partitionOne.nbrInputConnections());
            System.out.println("\tOutput Connections : " + partitionOne.nbrOutputConnections());

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


}
