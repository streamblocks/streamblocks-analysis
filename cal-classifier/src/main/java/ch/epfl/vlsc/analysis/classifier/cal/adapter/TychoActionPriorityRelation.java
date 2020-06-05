package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import ch.epfl.vlsc.analysis.core.air.AbstractPriorityRelation;
import ch.epfl.vlsc.analysis.core.air.Action;
import se.lth.cs.tycho.compiler.CompilationTask;
import se.lth.cs.tycho.ir.entity.cal.CalActor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TychoActionPriorityRelation extends AbstractPriorityRelation<Action> {

    private final Map<Action, Entry> action2Entry;

    public TychoActionPriorityRelation(CompilationTask compilationTask, CalActor calActor, Map<String, Action> actionMap) {
        action2Entry = new HashMap<>();
        PriorityGraph priorityGraph = new PriorityGraph(calActor);

        // Determine descendants by traversing them "Postorder"
        // (so that the descendants of a vertex have been processed
        // before processing the vertex).
        for (PriorityGraph.Vertex vertex : priorityGraph.getRoots()) {
            String id = vertex.getAction().getTag().toString();
            Action action = actionMap.get(id);
            assert (action != null);
            computeDescendants(vertex, action, actionMap);
        }

        // Count the number of ancestors
        countAncestors();

    }

    @Override
    public Set<Action> getDomain() {
        return action2Entry.keySet();
    }

    @Override
    protected Set<Action> descendants(Object x) {
        Entry entry = action2Entry.get(x);
        return (entry != null) ? entry.descendants : null;
    }

    @Override
    protected int getNumAncestors(Action x) {
        Entry entry = action2Entry.get(x);
        return (entry != null) ? entry.numAncestors : 0;
    }

    /**
     * Determines the number of ancestors of each action by counting
     * the number of times it is mentioned as a descendant
     */
    private void countAncestors() {
        for (Entry entry : action2Entry.values()) {
            for (Action action : entry.descendants) {
                Entry e = action2Entry.get(action);
                ++e.numAncestors;
            }

        }
    }

    /**
     * @param vertex    a vertex of the PriorityGraph
     * @param action    the corresponding action
     * @param id2Action mapping from "action ids" to actions
     * @return the set of descendants (actions with lower priority) of the given vertex/action
     */
    Set<Action> computeDescendants(PriorityGraph.Vertex vertex,
                                   Action action,
                                   Map<String, ? extends Action> id2Action) {
        Set<Action> descendants;

        if (action2Entry.containsKey(action)) {
            // Action already visited
            Entry entry = action2Entry.get(action);
            if (entry == null)
                throw new IllegalArgumentException("priority graph is cyclic");
            else
                descendants = entry.descendants;
        } else {
            // Associate a null entry with action (to detect a possible cycle)
            action2Entry.put(action, null);

            // Now visit all successors and compute the set of reachable actions (descendants)
            descendants = new LinkedHashSet<>();
            for (PriorityGraph.Edge edge : vertex.getOutgoingEdges()) {
                PriorityGraph.Vertex succ = edge.getTarget();
                String id = succ.getAction().getTag().toString();
                Action succAction = id2Action.get(id);
                descendants.add(succAction);
                descendants.addAll(computeDescendants(succ, succAction, id2Action));
            }

            // Now publish the actual entry (in Postorder)
            action2Entry.put(action, new Entry(descendants));
        }

        return descendants;
    }

    private class Entry {
        Set<Action> descendants;
        int numAncestors;

        public Entry(Set<Action> descendants) {
            this.descendants = descendants;
        }
    }
}
