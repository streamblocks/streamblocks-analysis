package ch.epfl.vlsc.analysis.core.partitioning;

import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.configuration.BiPartitionXcfWriter;
import ch.epfl.vlsc.analysis.core.configuration.XcfConfiguration;
import ch.epfl.vlsc.analysis.core.partitioning.algorithms.KernighanLin;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KernighanLinPartitioner {


    public static void main(String[] args) {
        KernighanLinPartitioner partitioner = new KernighanLinPartitioner();
        partitioner.run(args);
    }


    private void printSynopsis() {
        System.err.println("Usage: KernighanLinPartitioner configuration.xcf");
    }

    public void run(String[] args) {
        if (args.length < 1) {
            printSynopsis();
            return;
        }
        File input = new File(args[0]);
        try {
            XcfConfiguration configuration = new XcfConfiguration(input);

            KernighanLin kLin = new KernighanLin(configuration.getNetwork(), configuration.getConnectionBandwidth());
            List<KernighanLin.KlPartition> partitions = kLin.compute();

            List<ActorInstance> partitionInZero = new ArrayList<>(partitions.get(0).getActorInstances());
            List<ActorInstance> partitionInOne = new ArrayList<>(partitions.get(1).getActorInstances());
            Set<Connection> connections = (Set<Connection>) configuration.getNetwork().getConnections();

            BiPartitionXcfWriter writer = new BiPartitionXcfWriter(configuration.getNetwork().getName(), partitionInZero, partitionInOne, connections);
            File xcfFile = new File("./" + configuration.getNetwork().getName() + "_KLin.xcf");
            writer.write(xcfFile);


        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
