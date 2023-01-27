package FeatureExtraction;

// Java program to implement Graph
// with the help of Generics
// Source: https://www.geeksforgeeks.org/implementing-generic-graph-in-java/

import java.util.*;

class Graph<T> {

    // We use Hashmap to store the edges in the graph
    private Map<T, List<T>> map = new HashMap<>();

    // This function adds a new vertex to the graph
    public void addVertex(T s)
    {
        map.put(s, new LinkedList<T>());
    }

    // This function adds the edge
    // between source to destination
    public void addEdge(T source,
                        T destination)
    {

        if (!map.containsKey(source))
            addVertex(source);

        if (!map.containsKey(destination))
            addVertex(destination);

        map.get(source).add(destination);
        map.get(destination).add(source);
    }

    // This function gives the count of vertices
    public int getVertexCount()
    {
      return map.keySet().size();
    }

    // This function gives the count of edges
    public int getEdgesCount()
    {
        int count = 0;
        for (T v : map.keySet()) {
            count += map.get(v).size();
        }
        // Because the edges are bidirectional, so we have the cut the count in half
        count = count / 2;
        return count;
    }

    // This function gives whether
    // a vertex is present or not.
    public boolean hasVertex(T s)
    {
        return map.containsKey(s);
    }

    // This function gives whether an edge is present or not.
    public boolean hasEdge(T s, T d)
    {
        return map.get(s).contains(d);
    }

    public List<T> getVertex(T s) {
        return map.get(s);
    }

    public Map<T, List<T>> getMap() {
        return map;
    }

    // Prints the adjancency list of each vertex.
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (T v : map.keySet()) {
            builder.append(v.toString() + ": ");
            for (T w : map.get(v)) {
                builder.append(w.toString() + " ");
            }
            builder.append("\n");
        }

        return (builder.toString());
    }
}


