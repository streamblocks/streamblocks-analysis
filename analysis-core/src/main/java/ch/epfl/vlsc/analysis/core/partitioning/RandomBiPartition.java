package ch.epfl.vlsc.analysis.core.partitioning;

import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.configuration.BiPartitionXcfWriter;
import ch.epfl.vlsc.analysis.core.configuration.Partition;
import ch.epfl.vlsc.analysis.core.configuration.XcfInitialBiConfiguration;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomBiPartition {


    public static void main(String[] args) {
        RandomBiPartition randomBiPartition = new RandomBiPartition();
        randomBiPartition.read(args);
    }

    public static void printBits(String prompt, BitSet b) {
        System.out.print(prompt + " ");
        for (int i = 0; i < b.size(); i++) {
            System.out.print(b.get(i) ? "1" : "0");
        }
        System.out.println();
    }

    public static BitSet getBits(Random sr, int size) {
        byte[] ar = new byte[(int) Math.ceil(size / 8F)];
        sr.nextBytes(ar);
        return BitSet.valueOf(ar).get(0, size);
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

            List<ActorInstance> actorInstances = configuration.getInstances();

            Set<Connection> connections = configuration.getConnections();

            BitSet initial = configuration.getPartitionBitSet();
            System.out.println(initial.size());

            //printBits("Initial Partitioning:", initial);
            int found = 0;
            for (int times = 0; times < 500000; times++) {
                Random r = new Random();
                BitSet random = getBits(r, initial.size());
                System.out.println(random.size());
                // -- Force instances on partition 0 to be in the random too
                for (int i = 0; i < initial.size(); i++) {
                    if (!initial.get(i)) {
                        random.set(i, false);
                    }
                }

                // -- Get Instances on partition 0
                List<ActorInstance> instancesInZero = new ArrayList<>();
                List<ActorInstance> instancesInOne = new ArrayList<>();
                for (int i = 0; i < actorInstances.size(); i++) {
                    if (random.get(i)) {
                        instancesInOne.add(actorInstances.get(i));
                    } else {
                        instancesInZero.add(actorInstances.get(i));
                    }
                }

                // -- Get Instances on partition 1
                Partition partitionZero = new Partition(0, instancesInZero, connections);
                //System.out.println("Nbr input :" + partitionZero.nbrInputConnections());
                //System.out.println("Nbr output :" + partitionZero.nbrOutputConnections());
                if (partitionZero.nbrInputConnections() + partitionZero.nbrOutputConnections() <= 30) {
                    printBits(found + ": Random Partitioning: ", random);
                    BiPartitionXcfWriter writer = new BiPartitionXcfWriter(configuration.getName(), instancesInZero, instancesInOne, connections);
                    File xcfFile = new File("./" + configuration.getName() + "_random_" + found + ".xcf");
                    writer.write(xcfFile);

                    found++;
                }

            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
