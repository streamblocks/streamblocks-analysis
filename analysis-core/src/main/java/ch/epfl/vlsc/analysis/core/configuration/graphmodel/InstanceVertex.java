package ch.epfl.vlsc.analysis.core.configuration.graphmodel;

public class InstanceVertex {
    private final String name;
    private final int partition;

    public InstanceVertex(String name, int partition) {
        this.name = name;
        this.partition = partition;
    }

    public String getName() {
        return name;
    }

    public int getPartition() {
        return partition;
    }

    @Override
    public String toString() {
        return name;
    }
}
