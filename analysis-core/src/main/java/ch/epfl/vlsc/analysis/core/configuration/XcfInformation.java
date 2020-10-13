package ch.epfl.vlsc.analysis.core.configuration;

import javax.xml.bind.JAXBException;
import java.io.File;

public class XcfInformation {

    public static void main(String[] args) {
        XcfInformation xcfInformation = new XcfInformation();
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
            XcfConfiguration configuration = new XcfConfiguration(input);

            System.out.println("Number of Partitions : " + configuration.nbrPartitions());
            System.out.println("Number of Connections : " + configuration.nbrConnections());
            System.out.println();
            for (ConfigurationPartition partition : configuration.getPartitions()){
                System.out.println("Partition : " + configuration.getPartitions().indexOf(partition));
                System.out.println("\tInstances : " + partition.getActors().size());
                System.out.println("\tConnections : " + partition.nbrConnections());
                System.out.println("\tInput Connections : " + partition.nbrInputConnections());
                System.out.println("\tOutput Connections : " + partition.nbrOutputConnections());
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


}
