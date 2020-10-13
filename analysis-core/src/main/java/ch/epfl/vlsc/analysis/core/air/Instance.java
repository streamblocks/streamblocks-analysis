package ch.epfl.vlsc.analysis.core.air;

import java.util.Collection;

public interface Instance {

    /**
     * @return a descriptive name of the actor instance,
     *         which is unique within the actor network
     */
    String getName();

}
