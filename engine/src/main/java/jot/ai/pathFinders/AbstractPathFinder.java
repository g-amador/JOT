/*
 * This file is part of the JOT game engine A.I. extension toolkit component.
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
package jot.ai.pathFinders;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.graph.GraphBackup;
import jot.math.graph.Node;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Abstract class that each pathfinder must implement. Alternatively one massive
 * class may be implemented instead.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractPathFinder {

    private static final Logger log = getLogger("AbstractPathFinder");

    static {
        log.setLevel(OFF);
    }

    protected HashMap<Vector3D, ArrayList<Vector3D>> Graph;
    protected HashMap<Vector3D, Vector2D> GraphNodesGridCoords;
    protected HashMap<Vector2D, Vector3D> GridCoordsGraphNodes;

    //Store every individual Vector3D that bellongs to the graph in this array list
    protected HashMap<Vector3D, Node> GraphNodes;
    //Store every Vector3D that ever gets visited in this array list
    protected ArrayList<Vector3D> visited = new ArrayList<>();
    protected HashSet<Vector3D> visited_hs = new HashSet<>();

    protected long start_time, end_time;
    protected long iterations;
    protected long totalMemoryConsumption;
    protected long minTimePerIteration;
    protected long maxTimePerIteration;
    protected long iterationStartTime;
    protected long neighborsSize;
    protected long GraphNodesSize;

    protected String map;
    protected int w, h;

    /**
     * This methods returns a path if found using either a default or a provided
     * pathFinder logic.
     *
     * @param startPosition the Cartesian coordinates of the starting node.
     * @param goalPosition the Cartesian coordinates of the destination node.
     * @param timeout the amount of time at most a pathfinder can attempt to
     * find a path.
     * @return if a path is found returns, an array of Vector3D coordinates of
     * the points to go from an start to an target position. Otherwise returns
     * NULL.
     */
    public abstract Vector3D[] findPath(Vector3D startPosition, Vector3D goalPosition, long timeout);

    /**
     * Get the visited nodes positions.
     *
     * @return the visited nodes positions.
     */
    public ArrayList<Vector3D> getVisited() {
        return extensionAIOptions.get("usePathFinders")
                ? this.visited : null;
    }

    /**
     * Get the cost of an edge connecting two nodes of the weighted search
     * graph.
     *
     * @param n1 graph node 1.
     * @param n2 graph node 2.
     * @return the cost of an edge formed by graph nodes n1 and n2.
     */
    protected float getCost(Node n1, Node n2) {
        return n1.getCost(n2);
    }

    /**
     * Get the heuristic of an edge connecting two nodes of the weighted search
     * graph.
     *
     * @param n1 graph node 1.
     * @param n2 graph node 2.
     * @return the heuristic between of an edge formed by graph nodes n1 and n2.
     */
    protected float getHeuristic(Node n1, Node n2) {
        return n1.getCost(n2);
    }

    /**
     * Obtain all the neighbors of a given node.
     *
     * @param n given graph node.
     * @return The neighbors of a given node n.
     */
    protected ArrayList<Node> getNeighbors(Node n) {
        ArrayList<Vector3D> neighborsPositions = new ArrayList<>();
        neighborsPositions.addAll(this.Graph.get(n.position));

        ArrayList<Node> neighbors = new ArrayList<>();
        neighborsPositions.stream().forEach(neighborPosition
                -> neighbors.add(this.GraphNodes.get(neighborPosition)));

        return neighbors;
    }

    /**
     * This method gets the graph.
     *
     * @return the graph, which corresponds to a hash map where each key is a
     * graph node coordinates that is associated with a list of neighbor nodes.
     */
    public HashMap<Vector3D, ArrayList<Vector3D>> getGraph() {
        return extensionAIOptions.get("usePathFinders")
                ? this.Graph : null;
    }

    /**
     * This method sets a new graph.
     *
     * @param Graph the new graph, which corresponds to a hash map where each
     * key is a graph node coordinates that is associated with a list of
     * neighbor nodes.
     */
    public void setGraph(
            HashMap<Vector3D, ArrayList<Vector3D>> Graph) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.Graph = Graph;
        }
    }

    /**
     * Set the hashMap that gets the 2D grid coordinates of a graph node.
     *
     * @param GraphNodesGridCoords the hashMap to set.
     */
    public void setGraphNodesGridCoords(
            HashMap<Vector3D, Vector2D> GraphNodesGridCoords) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.GraphNodesGridCoords = GraphNodesGridCoords;
        }
    }

    /**
     * Set the hashMap that from the 3D coordinate of a graph node gets its 2D
     * grid coordinates.
     *
     * @param GridCoordsGraphNodess the hashMap to set.
     */
    public void setGridCoordsGraphNodes(
            HashMap<Vector2D, Vector3D> GridCoordsGraphNodess) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.GridCoordsGraphNodes = GridCoordsGraphNodess;
        }
    }

    /**
     * Setup the graph nodes.
     */
    protected void setupGraphNodes() {
        this.Graph.keySet().stream()
                .forEach(v -> this.GraphNodes.put(v, new Node(v)));
    }

    /**
     * Setup the variables for the pathLog.
     *
     * @param map the designation of the map.
     * @param w the width of the grid graph.
     * @param h the height of the grid graph.
     */
    public void setupPathLog(String map, int w, int h) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.map = map;
            this.w = w;
            this.h = h;
        }
    }

    /**
     * Path log messages.
     *
     * @param logic the pathfinder designation.
     * @param end_time termination time of the total time the pathfinder took in
     * the search of a path.
     * @param solution the path found.
     */
    protected void pathLog(String logic, long end_time, Vector3D[] solution) {
        if (extensionAIOptions.get("usePathFindersDebug")) {
            if (solution != null) {
                log.info(format("%s; %d x %d; %d; %.2f%%; %s; %d; %d; "
                        + "%d; %.3f; %.3f; %.3f; %.3f; %.3f", this.map,
                        this.w, this.h, this.GraphNodes.size(),
                        this.GraphNodes.size() / (float) (this.w * this.h) * 100,
                        logic, solution.length, this.iterations, this.visited_hs.size(),
                        this.totalMemoryConsumption / 1_000.0f,
                        this.minTimePerIteration / 1_000_000.0f,
                        this.maxTimePerIteration / 1_000_000.0f,
                        end_time / this.iterations / 1_000_000.0f,
                        end_time / 1_000_000_000.0f));
            } else {
                log.info(format("%s; %d x %d; %d; %.2f%%; %s; NaN; %d; %d; "
                        + "%.3f; %.3f; %.3f; %.3f; %.3f", this.map,
                        this.w, this.h, this.GraphNodes.size(),
                        this.GraphNodes.size() / (float) (this.w * this.h) * 100,
                        logic, this.iterations, this.visited_hs.size(),
                        this.totalMemoryConsumption / 1_000.0f,
                        this.minTimePerIteration / 1_000_000.0f,
                        this.maxTimePerIteration / 1_000_000.0f,
                        end_time / this.iterations / 1_000_000.0f,
                        end_time / 1_000_000_000.0f));
            }
            this.visited_hs.clear();
            this.GraphNodes.clear();
        }
    }

    /**
     * Path found log messages.
     *
     * @param logic the pathfinder designation.
     * @param solution the path found.
     */
    protected void pathFoundLog(String logic, Vector3D[] solution) {
        if (extensionAIOptions.get("usePathFindersDebug")) {
            this.end_time = nanoTime() - this.start_time;
            this.pathLog(logic, this.end_time, solution);
        }
    }

    /**
     * Path not found log messages.
     *
     * @param logic the pathfinder designation.
     */
    protected void pathNotFoundLog(String logic) {
        if (extensionAIOptions.get("usePathFindersDebug")) {
            this.end_time = nanoTime() - this.start_time;
            this.pathLog(logic, this.end_time, null);
        }
    }

    /**
     * Backtrack a path from given node to start node.
     *
     * @param n the node to backtrack the path to the start node.
     * @return the path to travel from start to goal.
     */
    protected Vector3D[] reconstruct_path(Node n) {
        //ArrayList<Vector3D> solution = new ArrayList<>();
        LinkedList<Vector3D> solution = new LinkedList<>();
        while (n != null) {
            solution.addFirst(n.position);
            n = n.parent;
        }

        return solution.toArray(new Vector3D[solution.size()]);
    }

    /**
     * This method replaces the current graph by a provided backup.
     *
     * @param gb backup of the old graph nodes grid coordinates, graph hash map,
     * and graph array.
     */
    public void restoreGraph(GraphBackup gb) {
        this.setGraph(gb.Graph);
        this.setGraphNodesGridCoords(gb.GraphNodesGridCoords);
        this.setGridCoordsGraphNodes(gb.GridCoordsGraphNodess);
    }
}
