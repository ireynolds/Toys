/**
 * Defines a mutable directed labeled multigraph. 
 * 
 * A graph contains nodes and edges that connect two nodes. This graph contains
 * unique nodes as defined by their label, and unique edges as defined by the 
 * nodes they link and their label. It also does not allow null edges or nodes.
 *
 * The type parameter N is the type of the node data. The type parameter E is 
 * the type of the edge data.
 *
 * Note that each method specified by DirectedGraph throws a 
 * NullPointerException if any of its parameters is null.
 * 
 * @specfiedl node  : N             // A piece of data representing a node in 
 *                                     the graph.
 * @specfield nodes : Set<node>     // A collection of all vertices in the 
 *                                     graph, each containing some data.
 * @specfield edge  : Edge<N, E>    // A link between two nodes containing some 
 *                                     data.
 * @specfield edges : Set<edge>     // A collection of all edges in the graph.
 * 
 * 
 * @author Isaac Reynolds
 * @date 25 April 2012
 */
public class AdjacencyListGraph<N, E> {
    
    public static boolean IS_DEBUGGING = false;
    
    // Abstraction Function:
    //
    // AF(this) = a directed labeled multigraph g such that
    //      graph.keySet() = nodes
    //      graph.get(A).keySet() = A's children
    //      graph.get(A).get(B) = Set of all edges from A to B
    
    
    // Representation Invariant:
    //
    // graph != null
    // !graph.keySet().contains(null)
    // !graph.valueSet().contains(null)
    // !graph.get(A).keySet().contains(null)
    // !graph.get(A).valueSet().contains(null)
    // !graph.get(A).get(B).contains(null)
    //
    // -OR, IN ENGLISH-
    //
    // There are no nulls in graph or any sub-collection in graph.
    // 
    
    
    // Represents a graph structure
    private final Map<N, Map<N, Set<E>>> graph;
    
    /**
     * Creates a new AdjacencyListGraph with no nodes or edges.
     */
    public AdjacencyListGraph() {
        graph = new HashMap<N, Map<N, Set<E>>>();
        checkRep();
    }
    
    /**
     * If edges does not contain e, adds e (ensures that there are no 
     * duplicates). Returns true if edges was changed as a result of this call. 
     *
     * @param e     the Edge to add
     * @return      true if edges changed as a result of this call
     * @modifies    edges
     * @effects     edges - adds e
     */
    public boolean addEdge(Edge<? extends N, ? extends E> e) {
        N source = e.getSource();
        N destination = e.getDestination();
        if (!containsNode(source) || !containsNode(destination))
            return false;
        checkRep();
        
        boolean changed = false;
        N src = source;
        N dest = destination;
        
        // if dest doesn't exist as a child of src, add it, then add edge
        if (!graph.get(src).containsKey(dest))
            graph.get(src).put(dest, new HashSet<E>());
        changed = graph.get(src).get(dest).add(e.getLabel());
        
        checkRep();
        return changed;
    }
    
    /**
     * If nodes does not contain n, adds n (ensuring that there are no
     * duplicates). Returns true if nodes was changed as a result of this call.
     *
     * @param n     the Node to add
     * @return      true if nodes changed as a result of this call
     * @modifies    nodes
     * @effects     nodes - adds n
     */
    public boolean addNode(N n) {
        if (n == null)
            throw new NullPointerException();
        checkRep();
        
        boolean needsChange  = !containsNode(n);
        if (needsChange) {
            graph.put(n, new HashMap<N, Set<E>>());
        }
        
        checkRep();
        return needsChange;
    }
    
    /**
     * Returns the Set of n's children. That is, returns all nodes y such that 
     * there is an edge from n to y. Returns null if n is not in nodes.
     * 
     * @param n     the node whose children to get
     * @return      a Set of all Nodes y such that there exists an Edge from n 
     *              to y, or null if n is not in nodes.
     */
    public Set<N> children(N n) {
        if (n == null)
            throw new NullPointerException();
        if (!containsNode(n))
            return null;
        
        Set<N> children = graph.get(n).keySet();
        children = Collections.unmodifiableSet(children);
        
        return children;
    }
    
    /**
     * Removes all nodes from nodes, and all edges from edges.
     * 
     * @modifies    nodes
     *              edges
     * @effects     nodes - makes nodes empty
     *              edges - makes edges empty
     */
    public void clear() {
        graph.clear();
    }
    
    /** 
     * Returns true if edges contains e.
     * 
     * @param n     Edge whose presence in edges is to be tested.
     * @return      true if e is in edges
     */
    public boolean containsEdge(Edge<? extends N, ? extends E> e) {
        N source = e.getSource();
        N destination = e.getDestination();
        if (!containsNode(source) || !containsNode(destination))
            return false;
        if (!edgeExists(source, destination))
            return false;
        
        return getEdges(source, destination).contains(e.getLabel());
    }
    
    /** 
     * Returns true if nodes contains n.
     * 
     * @param n     Node whose presence in nodes is to be tested.
     * @return      true if n is in nodes
     */
    public boolean containsNode(N n) {
        if (n == null)
            throw new NullPointerException();
        
        return graph.containsKey(n);
    }
    
    /**
     * Returns true if there exists an edge from source to destination, or false
     * otherwise.
     * 
     * @param source        the Node from which the edge is directed
     * @param destination   the Node toward which the edge is directed
     * @return              true if there exists an edge from source to
     *                      destination. False if no such edge exists, or if 
     *                      source or destination is not in nodes.
     */
    public boolean edgeExists(N source, N destination) {
        if (source == null || destination == null)
            throw new NullPointerException();
        if (!containsNode(source) || !containsNode(destination))
            return false;
        
        if (graph.get(source).containsKey(destination))
            // if the last edge were ever deleted, it would leave the empty
            // set behind.
            return !graph.get(source).get(destination).isEmpty();
        else
            return false;
    }
    
    /**
     * Returns a Set of the data of all edges e directed from source to 
     * destination. Returns null if one or both of source and destination isn't 
     * in nodes.
     * 
     * @param source        the Node from which e is directed
     * @param destination   the Node to which e is directed
     * @return              a Set of Edges from source to destination, or null
     *                      if one or both of source and destination isn't in
     *                      nodes.
     */
    public Set<E> getEdges(N source, N destination) {
        if (source == null || destination == null)
            throw new NullPointerException();
        if (!containsNode(source) || !containsNode(destination))
            return null;
        
        Set<E> edges;
        if (edgeExists(source, destination))
            edges = graph.get(source).get(destination);
        else
            edges = new HashSet<E>();
        edges = Collections.unmodifiableSet(edges);
        
        return edges;
    }
    
    /** 
     * Returns true if nodes is empty.
     *
     * @return true if size() == 0.
     */
    public boolean isEmpty() {
        return size() == 0;
    }
    
    /**
     * Returns nodes.
     * 
     * @return a Set of all nodes in nodes
     */
    public Set<N> nodeSet() {
        Set<N> nodes = this.graph.keySet();
        nodes = Collections.unmodifiableSet(nodes);
        
        return nodes;
    }
    
    /** 
     * Returns n's parents. That is, returns a Set of nodes y such that there 
     * exists an Edge from y to n. Returns null if n is not in nodes.
     * 
     * @param n     the node whose parents to get
     * @return      a Set of all Nodes y such that there exists an Edge from y 
     *              to n, or null if n is not in nodes.
     */
    public Set<N> parents(N n) {
        if (n == null)
            throw new NullPointerException();
        if (!containsNode(n))
            return null;
        
        Set<N> parents = new HashSet<N>();
        for (N p : graph.keySet()) {
            if (graph.get(p).containsKey(n))
                parents.add(p);
        }
        
        return parents;
    }
    
    /**
     * If e is in edges, removes e.
     * 
     * @param n     Edge to remove
     * @return      true if edges changed as a result of this call
     * @modifies    edges
     * @effects     edges - removes e
     */
    public boolean removeEdge(Edge<? extends N, ? extends E> e) {
        N source = e.getSource();
        N destination = e.getDestination();
        if (!containsEdge(e))
            return false;
        checkRep();
        
        boolean changed = graph.get(source).get(destination).remove(e.getLabel());
        
        checkRep();
        return changed;
    }
    
    /**
     * If n is in nodes, removes n and all Edges to and from n.
     * 
     * @param n     Node to remove
     * @return      true if nodes changed as a result of this call
     * @modifies    nodes
     * @effects     nodes - removes n
     */
    public boolean removeNode(N n) {
        if (n == null)
            throw new NullPointerException();
        if (!containsNode(n))
            return false;
        checkRep();
        
        for (N p : parents(n)) {
            graph.get(p).remove(n);
        }
        graph.remove(n);
        
        checkRep();
        return true;
    }
    
    /**
     * If e is in edges, sets e's data to the parameter data and returns a new 
     * Edge reflecting the changes.
     * 
     * @param oldEdge   Edge whose data to set
     * @param newLabel  Represents the new data
     * @return      a new Edge reflecting the changes
     * @modifies    edges
     * @effects     edges - !edges.contains(e) & edges.contains(e_post)
     * @throws      IllegalStateException - if edges does not contain e
     * @throws      IllegalStateException - if edges contains e_post
     */
    public <T extends N, S extends E, P extends S> void replaceEdge(Edge<T, S> oldEdge, P newLabel) {
        T source = oldEdge.getSource();
        T destination = oldEdge.getDestination();
        S oldLabel = oldEdge.getLabel();
        if (oldLabel == null || newLabel == null)
            throw new NullPointerException();
        if (!containsNode(source) || !containsNode(destination))
            throw new IllegalStateException();
        if (!containsEdge(oldEdge))
            throw new IllegalStateException();
        checkRep();
        
        removeEdge(oldEdge);
        Edge<T, P> newEdge = new Edge<T, P>(source, destination, newLabel);
        if (containsEdge(newEdge))
            throw new IllegalStateException();
        addEdge(newEdge);
        
        checkRep();
    }
    
    /**
     * If n is in nodes, sets n's data to the parameter data and returns a new 
     * Node reflecting the changes.
     * 
     * @param oldLabel  Node whose data to set
     * @param newLabel  String representing the new data
     * @return      a new Node reflecting the changes
     * @modifies    nodes
     * @effects     nodes - !nodes.contains(n_pre) & nodes.contains(n_post)
     * @throws      IllegalStateException - if nodes does not contain n
     * @throws      IllegalStateException - if nodes contains n_post
     */
    public void replaceNode(N oldLabel, N newLabel) {
        if (oldLabel == null || newLabel == null)
            throw new NullPointerException();
        if (!containsNode(oldLabel))
            throw new IllegalStateException();
        checkRep();
        
        if (containsNode(newLabel))
            throw new IllegalStateException();
        
        addNode(newLabel);
        // for every edge <p, n> create a new edge <p, n_post>
        for (N parent : parents(oldLabel)) {
            for (E edgeLabel : getEdges(parent, oldLabel)) {
                addEdge(new Edge<N, E>(parent, newLabel, edgeLabel));
            }
        }
        // for every edge <n, c> create a new edge <n_post, c>
        for (N child : children(oldLabel)) {
            for (E edgeLabel : getEdges(oldLabel, child)) {
                addEdge(new Edge<N, E>(newLabel, child, edgeLabel));
            }
        };
        removeNode(oldLabel);
        
        checkRep();
    }
    
    /**
     * Returns the number of Nodes in nodes.
     * 
     * @return the size of nodes.
     */
    public int size() {
        return graph.size();
    }
    
    /**
     * Returns the number of Edges in edges.
     *
     * @return the size of edges
     */
    public int sizeEdges() {
        int size = 0;
        for (N src : graph.keySet()) {
            for (N dest : graph.get(src).keySet()) {
                size += graph.get(src).get(dest).size();
            }
        }
        return size;
    }
    
    private void checkRep() {
        if (!IS_DEBUGGING)
            return;
        
        // nodes != null
        assert graph != null;
        // ! nodes.containsKey(null)
        assert !graph.containsKey(null);
        // ! nodes.containsValue(null)
        assert !graph.containsValue(null);
        for (N src : graph.keySet()) {
            Map<N, Set<E>> children = graph.get(src);
            // ! nodes.get(A).containsKey(null) if A is in nodes
            assert !children.containsKey(null);
            // ! nodes.get(A).containsValue(null) if A is in nodes
            assert !children.containsValue(null);
            for (N dest : children.keySet()) {
                // ! nodes.get(A).get(B).contains(null) if B is a child of A
                assert !children.get(dest).contains(null);
            }
        }
    }    
}

/**
 * Edge represents an immutable directed edge  in a graph structure. This Edge 
 * class is meant to be used with a class that implements the DirectedGraph 
 * interface.
 * 
 * @specfield Label       : String  // A piece of data stored with this edge 
 *                                     which identifies it uniquely among the 
 *                                     edges in the Graph.
 * @specfield Source      : Node    // The Node away from which this edge 
 *                                     points.
 * @specfield Destination : Node    // The Node toward which this edge points.
 * 
 * @author Isaac
 * @date 25 April 2012
 * @see DirectedGraph
 */
public final class Edge<N, E> {
    
    /**
     * Stores this edge's label.
     */
    private final E label;
    
    /**
     * Stores this edge's source.
     */
    private final N source;
    
    /**
     * Stores this edge's destination.
     */
    private final N destination;
    
    /**
     * Creates a new Edge.
     * 
     * @require             source != null
     * @require             destination != null
     * @param source        this edge's source
     * @param destination   this edge's destination
     * @param label         this Node's label
     */
    public Edge(N source, N destination, E label) {
        if (source == null || destination == null || label == null)
            throw new NullPointerException();
        
        this.label = label;
        this.source = source;
        this.destination = destination;
    }
    
    public static <N, E> Edge<N, E> makeEdge(N source, N destination, E label) {
        return new Edge<N, E>(source, destination, label);
    }
    
    /**
     * Creates a new Edge e2 such that e2.equals(e).
     * 
     * @param e     the edge to copy.
     */
    public Edge(Edge<N, E> e) {
        this(e.source, e.destination, e.label);
    }
    
    /**
     * Returns label.
     * 
     * @return this edge's label
     */
    public E getLabel() {
        return label;
    }
    
    /**
     * Returns source.
     * 
     * @return this edge's source
     */
    public N getSource() {
        return source;
    }
    
    /**
     * Returns destination.
     * 
     * @return this edge's destination
     */
    public N getDestination() {
        return destination;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = 17;
        int c = 0;
        
        c = label.hashCode();
        result = 31 * result + c;
        c = source.hashCode();
        result = 31 * result + c;
        c = destination.hashCode();
        result = 31 * result + c;
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Edge<?, ?>) {
            
            Edge<?, ?> e = (Edge<?, ?>) o;
            return source.equals(e.source) && destination.equals(e.destination)
                    && label.equals(e.label);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return getLabel() + ": " + source + " -> "
                + destination;
    }
}

public class AdjacencyListGraphTest {
    
    // Custom Strings
    private final String n1  = "lorem";
    private final String n2  = "ipsum";
    private final String n3  = "dolor";
    private final String n4  = "sit";
    
    // Custom Edge<String, String>s
    private final Edge<String, String> e1  = new Edge<String, String>(n1, n2, "lorem");
    private final Edge<String, String> e2  = new Edge<String, String>(n2, n3, "ipsum");
    private final Edge<String, String> e3  = new Edge<String, String>(n3, n4, "dolor");
    private final Edge<String, String> e4  = new Edge<String, String>(n1, n4, "sit");
    
    // convenient way to get a new graph with m Strings.
    private static AdjacencyListGraph<String, String> graph(String... args) {
        AdjacencyListGraph<String, String> g = new AdjacencyListGraph<String, String>();
        for (String arg : args) 
            g.addNode(arg);
        return g;
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // addNode(String) Test
    
    @Test (expected=NullPointerException.class)
    public void testAddStringNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.addNode(null);
    }
    
    @Test
    public void testAddString() {
        AdjacencyListGraph<String, String> g = graph();
        assertTrue(g.addNode(n1));
    }
    
    @Test
    public void testAddStringMultiple() {
        AdjacencyListGraph<String, String> g = graph();
        assertTrue(g.addNode(n1));
        assertTrue(g.addNode(n2));
    }
    
    @Test
    public void testAddStringDuplicate() {
        AdjacencyListGraph<String, String> g = graph();
        assertTrue(g.addNode(n1));
        assertFalse(g.addNode(n1));
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // containsString(String) Test
    
    @Test (expected=NullPointerException.class)
    public void testContainsStringNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.containsNode(null);
    }
    
    @Test
    public void testNotContainsString() {
        AdjacencyListGraph<String, String> g = graph(n1);
        assertFalse(g.containsNode(n2));
    }
    
    @Test
    public void testContainsOneString() {
        AdjacencyListGraph<String, String> g = graph(n1);
        assertTrue(g.containsNode(n1));
    }
    
    @Test
    public void testContainsStringMultiple() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        assertTrue(g.containsNode(n2));
        assertTrue(g.containsNode(n1));
    }
    
    @Test
    public void testContainsStringDuplicate() {
        AdjacencyListGraph<String, String> g = graph(n1, n1);
        assertTrue(g.containsNode(n1));
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // addEdge(Edge<String, String>) Test
    
    @Test (expected=NullPointerException.class)
    public void testAddEdgeNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.addEdge(null);
    }
    
    @Test
    public void testAddEdge() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e = new Edge<String, String>(n1, n2, "label");
        assertTrue(g.addEdge(e));
    }
    
    @Test
    public void testAddBadEdge() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e = new Edge<String, String>(n1, n3, "label");
        assertFalse(g.addEdge(e));
    }
    
    @Test
    public void testAddDuplicateEdge() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e = new Edge<String, String>(n1, n2, "label");
        assertTrue(g.addEdge(e));
        assertFalse(g.addEdge(e));
    }
    
    @Test
    public void testAddMultigraph() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e1 = new Edge<String, String>(n1, n2, "label");
        Edge<String, String> e2 = new Edge<String, String>(n2, n1, "label");
        Edge<String, String> e3 = new Edge<String, String>(n1, n2, "label2");
        Edge<String, String> e4 = new Edge<String, String>(n2, n1, "label2");
        assertTrue(g.addEdge(e1));
        assertTrue(g.addEdge(e2));
        assertTrue(g.addEdge(e3));
        assertTrue(g.addEdge(e4));
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // containsEdge(Edge<String, String>) Test
    
    @Test (expected=NullPointerException.class)
    public void testContainsEdgeNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.containsEdge(null);
    }
    
    @Test
    public void testContainsSingleEdge() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e = new Edge<String, String>(n1, n2, "label");
        g.addEdge(e);
        assertTrue(g.containsEdge(e));
    }
    
    @Test
    public void testContainsNoEdge() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e = new Edge<String, String>(n1, n3, "label");
        assertFalse(g.containsEdge(e));
    }
    
    @Test
    public void testContainsDuplicateEdge() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e = new Edge<String, String>(n1, n2, "label");
        g.addEdge(e);
        g.addEdge(e);
        assertTrue(g.containsEdge(e));
    }
    
    @Test
    public void testContainsMultipleEdge() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        g.addEdge(e1); g.addEdge(e2); g.addEdge(e3); g.addEdge(e4);
        assertTrue(g.containsEdge(e1));
        assertTrue(g.containsEdge(e2));
        assertTrue(g.containsEdge(e3));
        assertTrue(g.containsEdge(e4));
    }
    
    @Test
    public void testContainsMultigraphEdge() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e1 = new Edge<String, String>(n1, n2, "label");
        Edge<String, String> e2 = new Edge<String, String>(n2, n1, "label");
        Edge<String, String> e3 = new Edge<String, String>(n1, n2, "label2");
        Edge<String, String> e4 = new Edge<String, String>(n2, n1, "label2");
        g.addEdge(e1); g.addEdge(e2); g.addEdge(e3); g.addEdge(e4);
        assertTrue(g.containsEdge(e1));
        assertTrue(g.containsEdge(e2));
        assertTrue(g.containsEdge(e3));
        assertTrue(g.containsEdge(e4));
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // children Test
    
    @Test (expected=NullPointerException.class)
    public void testChildrenNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.children(null);
    }
    
    @Test
    public void testChildrenWithEmptyGraph() {
        AdjacencyListGraph<String, String> g = graph();
        assertNull(g.children(n1));
    }
    
    @Test
    public void testChildrenWithChildlessParent() {
        AdjacencyListGraph<String, String> g = graph(n1);
        assertTrue(g.children(n1).isEmpty());
    }
    
    @Test
    public void testChildrenSimple() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        g.addEdge(e1); g.addEdge(e4);
        
        Set<String> children1 = g.children(n1);
        assertTrue(children1.contains(n2));
        assertTrue(children1.contains(n4));
        assertEquals(2, children1.size());
    }
    
    @Test
    public void testChildrenMultiGraph() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        Edge<String, String> e5 = new Edge<String, String>(n2, n1, "lorem");
        g.addEdge(e1); g.addEdge(e2); g.addEdge(e3); g.addEdge(e4); g.addEdge(e5);
        
        Set<String> children1 = g.children(n1);
        assertTrue(children1.contains(n2));
        assertTrue(children1.contains(n4));
        assertEquals(2, children1.size());
        
        Set<String> children2 = g.children(n2);
        assertTrue(children2.contains(n3));
        assertTrue(children2.contains(n1));
        assertEquals(2, children2.size());
        
        Set<String> children3 = g.children(n3);
        assertTrue(children3.contains(n4));
        assertEquals(1, children3.size());
        
        Set<String> children4 = g.children(n4);
        assertEquals(0, children4.size());
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // parents Test
    
    @Test (expected=NullPointerException.class)
    public void testParentsNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.parents(null);
    }
    
    @Test
    public void testParentWithEmptyGraph() {
        AdjacencyListGraph<String, String> g = graph();
        assertNull(g.parents(n1));
    }
    
    @Test
    public void testParentWithParentlessChild() {
        AdjacencyListGraph<String, String> g = graph(n1);
        assertTrue(g.children(n1).isEmpty());
    }
    
    @Test
    public void testParentSimple() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        g.addEdge(e1); g.addEdge(e2); g.addEdge(e3); g.addEdge(e4);
        
        Set<String> parents4 = g.parents(n4);
        assertTrue(parents4.contains(n1));
        assertTrue(parents4.contains(n3));
        assertEquals(2, parents4.size());
    }
    
    @Test
    public void testParentsMultiGraph() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        Edge<String, String> e5 = new Edge<String, String>(n2, n1, "lorem");
        g.addEdge(e1); g.addEdge(e2); g.addEdge(e3); g.addEdge(e4); g.addEdge(e5);
        
        Set<String> parents1 = g.parents(n1);
        assertTrue(parents1.contains(n2));
        assertEquals(1, parents1.size());
        
        Set<String> parents2 = g.parents(n2);
        assertTrue(parents2.contains(n1));
        assertEquals(1, parents2.size());
        
        Set<String> parents3 = g.parents(n3);
        assertTrue(parents3.contains(n2));
        assertEquals(1, parents3.size());
        
        Set<String> parents4 = g.parents(n4);
        assertTrue(parents4.contains(n1));
        assertTrue(parents4.contains(n3));
        assertEquals(2, parents4.size());
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // getEdges Test
    
    @Test (expected=NullPointerException.class)
    public void testGetEdgesOneNull() {
        AdjacencyListGraph<String, String> g = graph(n1);
        g.getEdges(null, n1);
    }
    
    @Test (expected=NullPointerException.class)
    public void testGetEdgesBothNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.getEdges(null, null);
    }
    
    @Test
    public void testGetEdgesWithUncontainedStrings() {
        AdjacencyListGraph<String, String> g = graph(n1);
        assertNull(g.getEdges(n1, n2));
        assertNull(g.getEdges(n2, n1));
        assertNull(g.getEdges(n3, n4));
    }
    
    @Test
    public void testGetEdgesSimple() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        g.addEdge(e1);
        
        Set<String> s1 = g.getEdges(n1, n2);
        assertTrue(s1.contains(e1.getLabel()));
        assertEquals(1, s1.size());
        
        Set<String> s2 = g.getEdges(n2, n1);
        assertFalse(s2.contains(e1.getLabel()));
        assertEquals(0, s2.size());
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // Edge<String, String>Exists Test
    
    @Test (expected=NullPointerException.class)
    public void testEdgeExistsNull() {
        AdjacencyListGraph<String, String> g = graph(n1);
        g.edgeExists(null, n1);
    }
    
    @Test (expected=NullPointerException.class)
    public void testEdgeExistsBothNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.edgeExists(null, null);
    }
    
    @Test
    public void testEdgeExistsWithUncontainedStrings() {
        AdjacencyListGraph<String, String> g = graph(n1);
        assertFalse(g.edgeExists(n1, n2));
        assertFalse(g.edgeExists(n2, n1));
        assertFalse(g.edgeExists(n3, n4));
    }
    
    @Test
    public void testEdgeExistsSimple() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        g.addEdge(e1);
        
        assertTrue(g.edgeExists(n1, n2));
        assertFalse(g.edgeExists(n2, n1));
    }
    
    @Test
    public void testgetEdgesMultiGraph() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        Edge<String, String> e5 = new Edge<String, String>(n2, n1, "lorem");
        g.addEdge(e1); g.addEdge(e2); g.addEdge(e3); g.addEdge(e4); g.addEdge(e5);
        
        assertTrue(g.edgeExists(n1, n2));
        assertTrue(g.edgeExists(n2, n3));
        assertTrue(g.edgeExists(n3, n4));
        assertTrue(g.edgeExists(n1, n4));
        assertTrue(g.edgeExists(n2, n1));
        
        assertFalse(g.edgeExists(n4, n1));
        assertFalse(g.edgeExists(n4, n3));
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // removeEdge(Edge<String, String>) Test
   
    @Test (expected=NullPointerException.class)
    public void testRemoveEdgeNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.removeEdge(null);
    }
    
    @Test
    public void testRemoveEdgeNotExists() {
        AdjacencyListGraph<String, String> g = graph();
        assertFalse(g.removeEdge(e1));
    }
    
    @Test
    public void testRemoveEdgeSimple() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        g.addEdge(e1);
        assertTrue(g.containsEdge(e1));
        assertTrue(g.removeEdge(e1));
        assertFalse(g.containsEdge(e1));
        assertFalse(g.edgeExists(n1, n2));
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // removeNode(String) Test
    
    @Test (expected=NullPointerException.class)
    public void testRemoveStringNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.removeEdge(null);
    }
    
    @Test
    public void testRemoveStringNotExists() {
        AdjacencyListGraph<String, String> g = graph();
        assertFalse(g.removeNode(n1));
    }
    
    @Test
    public void testRemoveStringNoParents() {
        AdjacencyListGraph<String, String> g = graph(n1);
        assertTrue(g.removeNode(n1));
        assertFalse(g.containsNode(n1));
    }
    
    @Test
    public void testRemoveStringWithChildren() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        g.addEdge(e1);
        
        assertTrue(g.removeNode(n1));
        assertFalse(g.containsNode(n1));
        assertFalse(g.edgeExists(n1, n2));
        assertNull(g.getEdges(n1, n2));
    }
    
    @Test
    public void testRemoveStringWithParents() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        g.addEdge(e1);
        
        assertTrue(g.removeNode(n2));
        assertFalse(g.containsNode(n2));
        assertFalse(g.edgeExists(n1, n2));
        assertNull(g.getEdges(n1, n2));
        assertFalse(g.containsEdge(e1));
    }
    
    @Test
    public void testRemoveStringWithParentsAndAddAgain() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        g.addEdge(e1);
        
        g.removeNode(n2);
        g.addNode(n2);
        assertTrue(g.containsNode(n2));
        assertFalse(g.containsEdge(e1));
        g.addEdge(e1);
        assertTrue(g.containsEdge(e1));
    }
    
    
    // /////////////////////////////////////////////////////////////////////////
    // // setEdge<String, String>Data(Edge<String, String>) Test
    
    @Test (expected=NullPointerException.class)
    public void testSetEdgeDataNull() {
        AdjacencyListGraph<Object, String> g = new AdjacencyListGraph<Object, String>();
        g.addNode(n1);
        g.addNode(n2);
        g.addEdge(e1);
        g.replaceEdge(null, "");
    }
    
    @Test (expected=NullPointerException.class)
    public void testSetEdgeDataBothNull() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        g.addEdge(e1);
        g.replaceEdge(null, null);
    }
    
    @Test
    public void testSetEdgeDataNoDuplicate() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        g.addEdge(e1);
        
        g.replaceEdge(e1, "new");
        assertFalse(g.containsEdge(e1));
        assertTrue(g.containsEdge(new Edge<String, String>(e1.getSource(), e1.getDestination(), "new")));
    }
    
    @Test (expected=IllegalStateException.class)
    public void testSetEdgeDataDuplicate() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        Edge<String, String> e1 = new Edge<String, String>(n1, n2, "one");
        Edge<String, String> e2 = new Edge<String, String>(n1, n2, "two");
        g.addEdge(e1); g.addEdge(e2);
        g.replaceEdge(e1, "two");
    }
    
    @Test (expected=IllegalStateException.class)
    public void testSetEdgeDataNoEdge() {
        AdjacencyListGraph<String, String> g = graph();
        g.replaceEdge(e1, "new_data");
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // replaceNode(String) Test
    
    @Test (expected=NullPointerException.class)
    public void testReplaceNodeNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.replaceNode(null, "new_label");
    }
    
    @Test (expected=NullPointerException.class)
    public void testReplaceNodeBothNull() {
        AdjacencyListGraph<String, String> g = graph();
        g.replaceNode(null, null);
    }
    
    @Test (expected=IllegalStateException.class)
    public void testReplaceNodeNoString() {
        AdjacencyListGraph<String, String> g = graph();
        g.replaceNode(n1, "new_label");
    }
    
    @Test (expected=IllegalStateException.class)
    public void testReplaceNodeDuplicate() {
        AdjacencyListGraph<String, String> g = graph();
        g.replaceNode(n1, "new_label");
    }
    
    @Test
    public void testReplaceNodeNoDuplicate() {
        AdjacencyListGraph<String, String> g = graph(n1);
        
        g.replaceNode(n1, "new_label");
        assertTrue(g.containsNode("new_label"));
        assertFalse(g.containsNode(n1));
    }
    
    @Test
    public void testReplaceNodeWithChildren() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e1 = new Edge<String, String>(n1, n2, "one");
        g.addEdge(e1);
        
        g.replaceNode(n1, "new_label");
        assertFalse(g.containsNode(n1));
        assertTrue(g.containsNode("new_label"));
        
        assertFalse(g.edgeExists(n1, n2));
        assertTrue(g.edgeExists("new_label", n2));
    }
    
    @Test
    public void testReplaceNodeSelfAsChild() {
        AdjacencyListGraph<String, String> g = graph(n1);
        g.addEdge(new Edge<String, String>(n1, n1, "label"));
        
        g.replaceNode(n1, "new_label");
        assertFalse(g.containsNode(n1));
        assertTrue(g.containsNode("new_label"));
        assertTrue(g.edgeExists("new_label", "new_label"));
    }
    
    @Test
    public void testReplaceNodeWithParents() {
        AdjacencyListGraph<String, String> g = graph(n1, n2);
        Edge<String, String> e1 = new Edge<String, String>(n2, n1, "one");
        g.addEdge(e1);
        assertTrue(g.edgeExists(n2, n1));
        
        String n2_post = "new_label";
        g.replaceNode(n2, n2_post);
        assertFalse(g.containsNode(n2));
        assertTrue(g.containsNode(n2_post));
        
        assertFalse(g.edgeExists(n2, n1));
        assertTrue(g.edgeExists(n2_post, n1));
        assertEquals(1, g.getEdges(n2_post, n1).size());
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // StringSet Test
    
    @Test
    public void testNodeSetEmptyStrings() {
        AdjacencyListGraph<String, String> g = graph();
        assertEquals(0, g.nodeSet().size());
    }
    
    @Test
    public void testNodeSetMultipleStrings() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        assertTrue(g.nodeSet().contains(n1));
        assertTrue(g.nodeSet().contains(n2));
        assertTrue(g.nodeSet().contains(n3));
        assertTrue(g.nodeSet().contains(n4));
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // size Test
    
    @Test
    public void testSizeZeroStrings() {
        AdjacencyListGraph<String, String> g = graph();
        assertEquals(0, g.size());
    }
    
    @Test
    public void testSizeMultipleStrings() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3);
        assertEquals(3, g.size());
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // sizeEdge<String, String>s Test
    
    @Test
    public void testSizeEdgesZeroEdges() {
        AdjacencyListGraph<String, String> g = graph();
        assertEquals(0, g.sizeEdges());
    }
    
    @Test
    public void testSizeEdgesMultigraphEdges() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        g.addEdge(e1); g.addEdge(e2); g.addEdge(e3); g.addEdge(e4);
        
        assertEquals(4, g.sizeEdges());
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // isEmpty Test
    
    @Test
    public void testIsEmptyZeroStrings() {
        AdjacencyListGraph<String, String> g = graph();
        assertTrue(g.isEmpty());
    }
    
    @Test
    public void testIsEmptyOneString() {
        AdjacencyListGraph<String, String> g = graph(n1);
        assertFalse(g.isEmpty());
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // clear Test
    
    @Test
    public void testClear() {
        AdjacencyListGraph<String, String> g = graph(n1, n2, n3, n4);
        g.addEdge(e1); g.addEdge(e2); g.addEdge(e3); g.addEdge(e4);
        
        g.clear();
        assertTrue(g.isEmpty());
    }
}

public class EdgeTest {
    
    private final String n1  = "lorem";
    private final String n2  = "ipsum";
    private final String n3  = "dolor";

    
    private final Edge<String, String> e1  = new Edge<String, String>(n1, n2, "lorem");
    private final Edge<String, String> e2  = new Edge<String, String>(n2, n3, "ipsum");
    private final Edge<String, String> e8  = new Edge<String, String>(n1, n2, "lorem");   
    private final Edge<String, String> e15 = new Edge<String, String>(n1, n2, "lorem");
    
    // /////////////////////////////////////////////////////////////////////////
    // // hashCode Test
    
    @Test
    public void testHashCodeConsistentEdge() {
        assertEquals(e1.hashCode(), e1.hashCode());
    }
    
    @Test
    public void testHashCodeEqualsConsistencyEdge() {
        assertTrue(e1.equals(e8));
        assertTrue(e1.hashCode() == e8.hashCode());
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // // equals Test
    
    @Test 
    public void testReflexiveEdge() {
        assertTrue(e1.equals(e1));
    }
    
    @Test
    public void testSymmetricEdge() {
        assertEquals(e1.equals(e8), e8.equals(e1));
        assertEquals(e1.equals(e2), e2.equals(e1));
    }
    
    @Test
    public void testEqualsTransitiveEdge() {
        assertTrue(e1.equals(e8));
        assertTrue(e8.equals(e15));
        assertTrue(e1.equals(e15));
    }
    
    @Test
    public void testEqualsConsistentEdge() {
        assertFalse(e1.equals(e2) || e1.equals(e2));
        assertTrue(e1.equals(e8) && e1.equals(e8));
    }
    
    @Test
    public void testEqualsNullEdge() {
        assertFalse(e1.equals(null));
    }
    
    @Test
    public void testSrcEquality() {
        Edge<String, String> e1 = new Edge<String, String>(n1, n2, "label");
        Edge<String, String> e2 = new Edge<String, String>(n3, n2, "label");
        assertFalse(e1.equals(e2));
    }
    
    @Test
    public void testDestEquality() {
        Edge<String, String> e1 = new Edge<String, String>(n1, n2, "label");
        Edge<String, String> e2 = new Edge<String, String>(n1, n3, "label");
        assertFalse(e1.equals(e2));
    }
    
    @Test
    public void testLabelEquality() {
        Edge<String, String> e1 = new Edge<String, String>(n1, n2, "label");
        Edge<String, String> e2 = new Edge<String, String>(n1, n2, "label2");
        assertFalse(e1.equals(e2));
    }
}
