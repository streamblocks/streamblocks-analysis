package ch.epfl.vlsc.analysis.classifier.cal.adapter;

import se.lth.cs.tycho.ir.QID;
import se.lth.cs.tycho.ir.entity.cal.Action;
import se.lth.cs.tycho.ir.entity.cal.CalActor;
import se.lth.cs.tycho.ir.util.ImmutableList;

import java.io.PrintStream;
import java.util.*;

public class PriorityGraph {

    private final CalActor actor;
    private final Set<Vertex> vertices;
    private final Map<QID, PriorityGraph.Node> graph;
    private final Map<QID, Set<QID>> isPrefixOf;
    private final Map<QID, Action> qidByAction;

    public PriorityGraph(CalActor actor) {
        this.actor = actor;
        this.graph = new HashMap<>();
        this.vertices = new HashSet<>();
        this.isPrefixOf = new HashMap<>();
        this.qidByAction = new HashMap<>();
        initIsPrefixOf();
        initNodes();
        initEdges();
        constructPriorityGraph();
    }

    public PriorityGraph() {
        this.actor = null;
        this.graph = new HashMap<>();
        this.vertices = new HashSet<>();
        this.isPrefixOf = new HashMap<>();
        this.qidByAction = new HashMap<>();
    }

    private void constructPriorityGraph() {
        for (Node node : graph.values()) {
            Action sourceAction = qidByAction.get(node.getTag());
            Vertex sourceVertex = findVertex(sourceAction);
            if (sourceVertex == null) {
                sourceVertex = addVertex(sourceAction);
            }
            for (Node edge : node.getReachable()) {
                Action targetAction = qidByAction.get(edge.getTag());
                Vertex targetVertex = findVertex(targetAction);
                if (targetVertex == null) {
                    targetVertex = addVertex(targetAction);
                }
                new Edge(sourceVertex, targetVertex);
            }
        }
        // TODO: Finally add the actions that are used in the FSM but not in any priority relation
    }

    private void initIsPrefixOf() {
        for (ImmutableList<QID> prioritySeq : actor.getPriorities()) {
            for (QID priorityTag : prioritySeq) {
                isPrefixOf.putIfAbsent(priorityTag, new HashSet<>());
                for (Action action : actor.getActions()) {
                    QID actionTag = action.getTag();
                    if (actionTag != null && priorityTag.isPrefixOf(actionTag)) {
                        isPrefixOf.get(priorityTag).add(actionTag);
                    }
                }
            }
        }
    }

    private void initNodes() {
        for (Action action : actor.getActions()) {
            graph.putIfAbsent(action.getTag(), new Node(action.getTag()));
            qidByAction.put(action.getTag(), action);
        }
    }

    private void initEdges() {
        for (ImmutableList<QID> prioritySeq : actor.getPriorities()) {
            QID high = null;
            for (QID low : prioritySeq) {
                if (high != null) {
                    for (QID highTag : isPrefixOf.get(high)) {
                        for (QID lowTag : isPrefixOf.get(low)) {
                            graph.get(highTag).addEdge(graph.get(lowTag));
                        }
                    }
                }
                high = low;
            }
        }
    }

    public void addEdge(Action source, Action target) {
        Vertex sourceVertex = findVertex(source);
        Vertex targetVertex = findVertex(target);
        new Edge(sourceVertex, targetVertex);
    }

    public Set<Vertex> getVertices() {
        return this.vertices;
    }

    private Vertex findVertex(Action a) {
        for (Vertex v : vertices) {
            if (v.getAction() == a) {
                return v;
            }
        }
        return null;
    }

    public Vertex addVertex(Action a) {
        if (findVertex(a) != null) {
            return findVertex(a);
        }
        Vertex v = new Vertex(a);
        vertices.add(v);
        return v;
    }

    // public Set<Edge> getEdges() {
    // 	return this.edges;
    // }

    private void addVertex(Vertex v) {
        vertices.add(v);
    }

    /*
     *  Returns a projection of the graph containing only
     *  actions also found in the argument set 'a'.
     */
    public PriorityGraph getSubgraph(Set<Action> a) {
        PriorityGraph subgraph = new PriorityGraph();

        for (Vertex v : vertices) {
            Vertex v1 = subgraph.addVertex(v.getAction());
            if (!a.contains(v.getAction())) {
                v1.mark();
            }
        }

        for (Vertex v : vertices) {
            Vertex source = subgraph.findVertex(v.getAction());
            for (Edge e : v.getOutgoingEdges()) {
                Vertex target = subgraph.findVertex(e.getTarget().getAction());
                new Edge(source, target);
            }
        }

        for (Vertex v : subgraph.getVertices()) {
            // System.out.println("delete vertex: '" + v.getAction().getId() + "'");
            if (v.isMarked()) {
                for (Edge incoming : v.getIncomingEdges()) {
                    for (Edge outgoing : v.getOutgoingEdges()) {
                        Vertex source = incoming.getSource();
                        // System.out.println("  " + source.getAction().getId());
                        Vertex target = outgoing.getTarget();
                        // System.out.println("  " + target.getAction().getId());
                        new Edge(source, target);
                    }
                }
                for (Edge incoming : v.getIncomingEdges()) {
                    incoming.getSource().removeOutgoingEdge(incoming);
                }
                for (Edge outgoing : v.getOutgoingEdges()) {
                    outgoing.getTarget().removeIncomingEdge(outgoing);
                }
            }
        }

        PriorityGraph result = new PriorityGraph();
        for (Vertex v : subgraph.getVertices()) {
            if (!v.isMarked()) {
                result.addVertex(v);
            }
        }
        return result;
    }

    public List<Action> getActions() {
        Set<Vertex> vertices = this.getVertices();
        List<Action> actions = new ArrayList<Action>();
        for (Vertex v : vertices) {
            actions.add(v.getAction());
        }
        return actions;
    }

    public Set<Vertex> getRoots() {
        Set<Vertex> entryPoints = new HashSet<Vertex>();
        for (Vertex v : vertices) {
            if (v.getIncomingEdges().isEmpty()) {
                entryPoints.add(v);
            }
        }
        return entryPoints;
    }

    public List<Action> getOneTopologicalOrder() {
        List<Vertex> reverseOrder = new ArrayList<Vertex>();
        List<Action> result = new ArrayList<Action>();
        // Reverse post order sorting

        for (Vertex root : getRoots()) {
            DFS(root, reverseOrder);
        }
        for (int i = reverseOrder.size(); i > 0; i--) {
            result.add(reverseOrder.get(i - 1).getAction());
        }
        return result;
    }

    public void DFS(Vertex v, List<Vertex> order) {
        if (!order.contains(v)) {
            for (Edge e : v.getOutgoingEdges()) {
                if (!order.contains(e.getTarget())) {
                    DFS(e.getTarget(), order);
                }
            }
            order.add(v);
        }
    }

    public void print(PrintStream out) {
        out.println("diagraph G {");
        for (Vertex v : vertices) {
            out.println("  " + v.getAction().getTag().toString() + ";");
        }

        for (Vertex v : vertices) {
            for (Edge e : v.getOutgoingEdges()) {
                out.println("  " + v.getAction().getTag().toString() + " -> " + e.getTarget().getAction().getTag().toString() + ";");
            }
        }

        out.println("}");
    }

    private class Node {
        private final QID tag;
        private final Set<PriorityGraph.Node> edges;

        public Node(QID tag) {
            this.tag = tag;
            this.edges = new HashSet<>();
        }

        public void addEdge(PriorityGraph.Node node) {
            edges.add(node);
        }

        public Set<PriorityGraph.Node> getReachable() {
            Set<PriorityGraph.Node> result = new HashSet<>();
            Queue<PriorityGraph.Node> queue = new ArrayDeque<>();
            queue.addAll(edges);
            while (!queue.isEmpty()) {
                PriorityGraph.Node n = queue.remove();
                if (result.add(n)) {
                    queue.addAll(n.edges);
                }
            }
            return result;
        }

        public QID getTag() {
            return tag;
        }
    }

    public class Vertex {

        private final Action action;

        private final Set<Edge> incoming = new HashSet<>();

        private final Set<Edge> outgoing = new HashSet<Edge>();

        private boolean marked = false;

        private int nrOfTimesVisited = 0;

        public Vertex(Action action) {
            this.action = action;
        }

        public void addIncomingEdge(Edge edge) {
            incoming.add(edge);
        }

        public void removeIncomingEdge(Edge edge) {
            incoming.remove(edge);
        }

        public Set<Edge> getIncomingEdges() {
            return incoming;
        }

        public void addOutgoingEdge(Edge edge) {
            outgoing.add(edge);
        }

        public void removeOutgoingEdge(Edge edge) {
            outgoing.remove(edge);
        }

        public Set<Edge> getOutgoingEdges() {
            return outgoing;
        }

        public Action getAction() {
            return action;
        }

        public void mark() {
            marked = true;
        }

        public boolean isMarked() {
            return marked;
        }

        public void visit() {
            nrOfTimesVisited++;
        }

        public int getNrOfTimesVisited() {
            return nrOfTimesVisited;
        }

    }

    public class Edge {
        private final Vertex source;
        private final Vertex target;

        public Edge(Vertex source, Vertex target) {
            this.source = source;
            source.addOutgoingEdge(this);
            this.target = target;
            target.addIncomingEdge(this);
            //edges.add(this);
        }

        public Vertex getSource() {
            return source;
        }

        public Vertex getTarget() {
            return target;
        }

    }
}
