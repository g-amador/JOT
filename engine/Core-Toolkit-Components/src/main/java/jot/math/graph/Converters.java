/*
 * This file is part of the JOT game engine core toolkit component.
 * Copyright (C) 2014 Gonçalo Amador & Abel Gomes
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * E-mail Contacts: G. Amador (g.n.p.amador@gmail.com) & 
 *                  A. Gomes (agomes@it.ubi.pt)
 */
package jot.math.graph;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import java.util.ArrayList;
import static java.util.Collections.shuffle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements generic converters among graph data structures, e.g.,
 * array of pairs of nodes that form an edge 2 by 2 to a hash map with key pairs
 * of the type {@literal <}Node position, List of node neighbors{@literal >}.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Converters {

    static final Logger log = getLogger("GraphConverters");

    static {
        log.setLevel(OFF);
    }

    /**
     * Converts a graph array represented by pairs of connected nodes to a
     * hashMap.
     *
     * @param graph a graph array represented by pairs of connected nodes.
     * @return a hashMap conversion of graph array.
     */
    public static HashMap<Vector3D, ArrayList<Vector3D>> arrayGraph2HashMapGraph(Vector3D[] graph) {
        long start_time = nanoTime();

        log.info("Converting graph array to hash map.");
        HashMap<Vector3D, ArrayList<Vector3D>> hmGraph = new HashMap<>();

        //Create each key, one per each individual vertex.
        for (Vector3D key : graph) {
            if (!hmGraph.containsKey(key)) {
                hmGraph.put(new Vector3D(key.toArray()), new ArrayList<>());
            }
        }

        //for each key create an list with all its neighbors        
        hmGraph.keySet().stream().forEach(key -> {
            ArrayList<Vector3D> neighbors = new ArrayList<>();
            for (int i = 0; i < graph.length - 1; i += 2) {
                if (key.equals(graph[i])) {
                    neighbors.add(new Vector3D(graph[i + 1].toArray()));
                } else if (key.equals(graph[i + 1])) {
                    neighbors.add(new Vector3D(graph[i].toArray()));
                }

                /**
                 * We can stop for this key when the number of neighbors equals
                 * 8 since we have the maximum of possible connections for a
                 * squared grid.
                 */
                if (neighbors.size() == 8) {
                    break;
                }
            }
            hmGraph.put(key, neighbors);
        });

        long end_time = nanoTime() - start_time;
        log.info(format("Conversion took %.3f (milliseconds)",
                end_time / 1000000.0f));
        return hmGraph;
    }

    /**
     * Graph to minimum spanning tree (MST) converter using Prim's algorithm.
     *
     * @param Graph provided to determine the MST.
     * @return MST of a given Graph.
     */
    public static Vector3D[] Graph2MST(
            HashMap<Vector3D, ArrayList<Vector3D>> Graph) {
        HashSet<Vector3D> unvisited = new HashSet<>();
        PriorityQueue<Edge> edgesAvailable = new PriorityQueue<>();
        ArrayList<Vector3D> GraphMST = new ArrayList<>();

        Vector3D current = getRandomGraphNode(Graph);
        unvisited.addAll(Graph.keySet());
        unvisited.remove(current);

        while (!unvisited.isEmpty()) {
            // Populate all edges for the current vertex.
            for (Vector3D neighbor : Graph.get(current)) {
                // Don't add a duplicate edge.
                if (unvisited.contains(neighbor)) {
                    edgesAvailable.add(new Edge(
                            new Node(current), new Node(neighbor)));
                }
            }

            // Fetch the edge with least distance.
            Edge e;
            // If the target is already visited then move to next edge.
            do {
                e = edgesAvailable.poll();
            } while (null != e && !unvisited.contains(e.n2.position));

            if (null != e) {
                // Add the edge to the MST graph.
                GraphMST.add(e.n1.position);
                GraphMST.add(e.n2.position);

                // Get the next vertex.
                current = e.n2.position;
                unvisited.remove(current);
            } else {
                // Get the next vertex.
                current = unvisited.iterator().next();
                unvisited.remove(current);
            }
        }

        return GraphMST.toArray(new Vector3D[GraphMST.size()]);
    }

    /**
     * This method extracts a random Graph node.
     *
     * @param Graph
     * @return a random Graph node.
     */
    public static Vector3D getRandomGraphNode(
            HashMap<Vector3D, ArrayList<Vector3D>> Graph) {
        LinkedList<Vector3D> nodesPositions = new LinkedList<>();
        nodesPositions.addAll(Graph.keySet());
        shuffle(nodesPositions);

        return nodesPositions.getFirst();
    }

    /**
     * Default private constructor.
     */
    private Converters() {
    }
}
