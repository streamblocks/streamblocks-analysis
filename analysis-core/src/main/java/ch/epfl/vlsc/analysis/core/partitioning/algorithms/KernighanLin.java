package ch.epfl.vlsc.analysis.core.partitioning.algorithms;

import ch.epfl.vlsc.analysis.core.air.ActorInstance;
import ch.epfl.vlsc.analysis.core.air.Connection;
import ch.epfl.vlsc.analysis.core.air.Network;
import ch.epfl.vlsc.analysis.core.air.PortInstance;
import ch.epfl.vlsc.analysis.core.util.collections.Pair;
import ch.epfl.vlsc.platformutils.utils.MathUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class KernighanLin {
    private final Map<Connection, Long> buffersCost;
    private final Collection<ActorInstance> actorInstances;
    private final Network network;

    public KernighanLin(Network network, Map<Connection, Long> buffersCost) {
        this.network = network;
        this.buffersCost = buffersCost;
        this.actorInstances = (Collection<ActorInstance>) network.getActors();
    }

    public List<KlPartition> compute() {
        List<KlPartition> partitions = new ArrayList<>();
        if (actorInstances.isEmpty()) {
            //Logger.debug("Empty partition");
            return partitions;
        } else if (actorInstances.size() == 1) {
            //Logger.debug("1:1 partition");
            partitions.add(new KlPartition(network, actorInstances));
            return partitions;
        }

        // compute initial set
        List<ActorInstance> setA = new ArrayList<>();
        List<ActorInstance> setB = new ArrayList<>();

        for (ActorInstance ActorInstance : actorInstances) {
            if (setA.size() <= setB.size()) {
                setA.add(ActorInstance);
            } else {
                setB.add(ActorInstance);
            }
        }

        long g = Long.MIN_VALUE;
        Swapper swapper = new Swapper(setA, setB);
        while (swapper.g > g) {
            g = swapper.g;
            for (Pair<ActorInstance, ActorInstance> swap : swapper.swaps) {
                ActorInstance a = swap.getFirst();
                ActorInstance b = swap.getSecond();

                setA.remove(a);
                setB.remove(b);

                setA.add(b);
                setB.add(a);
            }

            swapper = new Swapper(setA, setB);
        }

        partitions.add(new KlPartition(network, setA));
        partitions.add(new KlPartition(network, setB));
        return partitions;
    }

    private long getCost(Connection buffer) {
        return buffersCost.containsKey(buffer) ? buffersCost.get(buffer) : 0;
    }

    public class KlPartition {

        private final Collection<ActorInstance> actorInstances = new HashSet<>();
        private final Map<ActorInstance, Long> internalCostMap = new HashMap<>();
        private final Map<ActorInstance, Long> externalCostMap = new HashMap<>();

        private KlPartition(Network network, Collection<ActorInstance> A) {
            actorInstances.addAll(A);

            // evaluate costs
            for (ActorInstance a : actorInstances) {
                long totInternal = 0;
                long totExternal = 0;

                for (PortInstance port : a.getInputPorts()) {
                    for (Connection buffer : network.getIncidentConnections(port)) {
                        ActorInstance source = buffer.getConsumerPort().getActor();
                        if (A.contains(source)) {
                            totInternal += getCost(buffer);
                        } else {
                            totExternal += getCost(buffer);
                        }
                    }
                }

                for (PortInstance port : a.getOutputPorts()) {
                    for (Connection buffer : network.getIncidentConnections(port)) {
                        ActorInstance target = buffer.getProducerPort().getActor();
                        if (A.contains(target)) {
                            totInternal += getCost(buffer);
                        } else {
                            totExternal += getCost(buffer);
                        }
                    }
                }

                internalCostMap.put(a, totInternal);
                externalCostMap.put(a, totExternal);
            }
        }

        public Collection<ActorInstance> getActorInstances() {
            return actorInstances;
        }

        public Map<ActorInstance, Long> getInternalCostMap() {
            return internalCostMap;
        }

        public Map<ActorInstance, Long> getExternalCostMap() {
            return externalCostMap;
        }

        public long getInternalCost() {
            return MathUtils.sumLong(internalCostMap);
        }

        public long getExternalCost() {
            return MathUtils.sumLong(externalCostMap);
        }

    }

    private class Swapper {

        long g = 0;
        List<Pair<ActorInstance, ActorInstance>> swaps = new ArrayList<>();

        Map<ActorInstance, Long> internalCost = new HashMap<>();
        Map<ActorInstance, Long> externalCost = new HashMap<>();
        Map<ActorInstance, Long> dCost = new HashMap<>();

        Swapper(Collection<ActorInstance> A, Collection<ActorInstance> B) {

            Collection<ActorInstance> sA = new ArrayList<>(A);
            Collection<ActorInstance> sB = new ArrayList<>(B);

            Collection<ActorInstance> unswappedA = new ArrayList<>(A);
            Collection<ActorInstance> unswappedB = new ArrayList<>(B);

            // compute initial costs
            for (ActorInstance a : A) {
                computeCost(a, A, B);
            }
            for (ActorInstance b : B) {
                computeCost(b, B, A);
            }

            Map<Pair<ActorInstance, ActorInstance>, Long> swapsCost = new HashMap<>();
            List<Pair<ActorInstance, ActorInstance>> swaps = new ArrayList<>();

            while (!unswappedA.isEmpty() && !unswappedB.isEmpty()) {
                long gMax = Long.MIN_VALUE;
                Pair<ActorInstance, ActorInstance> bestSwap = null;
                for (ActorInstance a : unswappedA) {
                    for (ActorInstance b : unswappedB) {
                        long dA = dCost.get(a);
                        long dB = dCost.get(b);
                        long cab = computeCa(a, b);
                        long g = dA + dB - 2 * cab;
                        if (g > gMax) {
                            gMax = g;
                            bestSwap = Pair.create(a, b);
                        }
                    }
                }

                swapsCost.put(bestSwap, gMax);
                swaps.add(bestSwap);

                ActorInstance bsa = bestSwap.getFirst();
                ActorInstance bsb = bestSwap.getSecond();

                unswappedA.remove(bsa);
                unswappedB.remove(bsb);

                sA.remove(bsa);
                sB.remove(bsb);
                sA.add(bsb);
                sB.add(bsa);

                // recompute costs
                for (ActorInstance a : unswappedA) {
                    computeCost(a, sA, sB);
                }
                for (ActorInstance b : unswappedB) {
                    computeCost(b, sB, sA);
                }
            }

            int k = -1;
            long gMax = Long.MIN_VALUE;
            for (int i = 0; i < swapsCost.size(); i++) {
                Pair<ActorInstance, ActorInstance> swap = swaps.get(i);
                long g = swapsCost.get(swap);
                if (g > gMax) {
                    g = gMax;
                    k = i;
                } else {
                    break;
                }
            }

            if (k >= 0) {
                for (int i = 0; i <= k; i++) {
                    Pair<ActorInstance, ActorInstance> swap = swaps.get(i);
                    this.swaps.add(swap);
                    this.g = swapsCost.get(swap);
                }
            }
        }

        long computeCa(ActorInstance a, ActorInstance b) {
            long cost = 0;
            for (PortInstance port : a.getInputPorts()) {
                for (Connection buffer : network.getIncidentConnections(port)) {
                    ActorInstance source = buffer.getConsumerPort().getActor();
                    if (source == b) {
                        cost += getCost(buffer);
                    }
                }
            }


            for (PortInstance port : b.getInputPorts()) {
                for (Connection buffer : network.getIncidentConnections(port)) {
                    ActorInstance source = buffer.getConsumerPort().getActor();
                    if (source == a) {
                        cost += getCost(buffer);
                    }
                }
            }
            return cost;
        }

        void computeCost(ActorInstance a, Collection<ActorInstance> A, Collection<ActorInstance> B) {
            if (!A.contains(a)) {
                throw new RuntimeException("ActorInstance " + a + " not contained in " + A);
            }

            long totInternal = 0;
            long totExternal = 0;


            for (PortInstance port : a.getInputPorts()) {
                for (Connection buffer : network.getIncidentConnections(port)) {
                    ActorInstance source = buffer.getConsumerPort().getActor();
                    if (A.contains(source)) {
                        totInternal += getCost(buffer);
                    } else if (B.contains(source)) {
                        totExternal += getCost(buffer);
                    }
                }
            }

            for (PortInstance port : a.getOutputPorts()) {
                for (Connection buffer : network.getIncidentConnections(port)) {
                    ActorInstance target = buffer.getProducerPort().getActor();
                    if (A.contains(target)) {
                        totInternal += getCost(buffer);
                    } else if (B.contains(target)) {
                        totExternal += getCost(buffer);
                    }
                }
            }


            long d = totExternal - totInternal;
            internalCost.put(a, totInternal);
            externalCost.put(a, totExternal);
            dCost.put(a, d);
        }

    }
}
