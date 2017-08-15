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

import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.System.nanoTime;
import java.util.ArrayList;
import static java.util.Collections.reverse;
import java.util.HashMap;
import java.util.HashSet;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.graph.Node;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements Fringe search pathfinding algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class FringeSearch extends AbstractPathFinder {

    static final Logger log = getLogger("AbstractPathFinder");

    /**
     * Default constructor.
     */
    public FringeSearch() {
        if (extensionAIOptions.get("usePathFindersDebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * Fringe search algorithm [1].
     *
     * [1] Björnsson, Yngvi; Enzenberger, Markus; Holte, Robert C.; Schaeffer,
     * Johnathan. "Fringe Search: Beating A* at Pathfinding on Game Maps".
     * Proceedings of the 2005 IEEE Symposium on Computational Intelligence and
     * Games (CIG05). Essex University, Colchester, Essex, UK, 46 April, 2005.
     * IEEE 2005.
     * http://www.cs.ualberta.ca/~games/pathfind/publications/cig2005.pdf
     *
     * @param startPosition the Cartesian coordinates of the starting node.
     * @param goalPosition the Cartesian coordinates of the destination node.
     * @param timeout the amount of time at most a pathfinder can attempt to
     * find a path.
     * @return if a path is found returns, an array of Vector3D coordinates of
     * the points to go from an start to an target position. Otherwise returns
     * NULL.
     */
    @Override
    public Vector3D[] findPath(Vector3D startPosition, Vector3D goalPosition,
            long timeout) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.start_time = nanoTime();
            this.iterations = 0;
            this.maxTimePerIteration = 0;
            this.minTimePerIteration = MAX_VALUE;
            this.totalMemoryConsumption = 0;
            this.neighborsSize = 0;
            this.GraphNodesSize = 0;
            Node start = new Node(startPosition);
            Node goal = new Node(goalPosition);

            ArrayList<Node> open = new ArrayList<>();   //fringe F
            this.GraphNodes = new HashMap<>();               //Each individual node of the graph, i.e., each non repeated line vertex.        
            this.visited = new ArrayList<>();                //All the (current, neighbor) node pairs for each iteration.
            this.visited_hs = new HashSet<>();
            this.setupGraphNodes();

            start.parent = start;
            start.g = 0.0f;
            open.add(start);
            this.GraphNodes.put(start.position, start);

            if (extensionAIOptions.get("usePathFindersDebug")) {
                this.GraphNodesSize = this.GraphNodes.size();
            }

            float f_limit = this.getHeuristic(start, goal);

            while (!open.isEmpty()) {
                if (extensionAIOptions.get("usePathFindersDebug")) {
                    this.iterations++;
                    this.iterationStartTime = nanoTime();
                    long oldUsedMemoryConsumption = (open.size() * 2
                            + this.neighborsSize + this.GraphNodesSize)
                            * (3 * 8 + 3 * 4 + 8) // a node object approximate memory size.
                            + ( /*visited.size() * visited_hs.size()
                    +*/this.GraphNodesSize)
                            * (3 * 8);  // the key object memory approximate size.
                    this.totalMemoryConsumption = max(this.totalMemoryConsumption, oldUsedMemoryConsumption);
                }

                if (extensionAIOptions.get("usePathFindersTimeout") && (nanoTime() - this.start_time) > timeout) {
                    log.info("No path available by timeout.");
                    return null;
                }

                float f_min = POSITIVE_INFINITY;

                ArrayList<Node> aux = new ArrayList<>();
                aux.addAll(open);
                for (Node current : aux) {
                    float g_parent = this.GraphNodes.get(current.position).g;
                    float f = g_parent + this.getHeuristic(current, goal);

                    if (f > f_limit) {
                        f_min = min(f, f_min);
                        continue;
                    }

                    if (current.equals(goal)) {
                        start.parent = null;
                        this.GraphNodes.put(start.position, start);

                        Vector3D[] solution = this.reconstruct_path(current);
                        this.pathFoundLog("FringeS", solution);
                        return solution;
                    }

                    ArrayList<Node> neighbors = this.getNeighbors(current);
                    reverse(neighbors);
                    if (extensionAIOptions.get("usePathFindersDebug")) {
                        this.neighborsSize = neighbors.size();
                    }
                    neighbors.stream().forEach(neighbor -> {
                        this.visited.add(current.position);
                        this.visited.add(neighbor.position);
                        this.visited_hs.add(current.position);
                        this.visited_hs.add(neighbor.position);

                        float g_neighbor = g_parent + this.getCost(current, neighbor);

                        if (neighbor.parent != null) {
                            float g_chached = this.GraphNodes.get(neighbor.position).g;
                            if (g_neighbor < g_chached) {
                                if (open.contains(neighbor)) {
                                    open.remove(neighbor);
                                }
                                open.add(open.indexOf(current) + 1, neighbor);

                                neighbor.parent = new Node(current);
                                neighbor.g = g_neighbor;
                                this.GraphNodes.put(neighbor.position, neighbor);
                            }
                        } else {
                            if (open.contains(neighbor)) {
                                open.remove(neighbor);
                            }
                            open.add(open.indexOf(current) + 1, neighbor);

                            neighbor.parent = new Node(current);
                            neighbor.g = g_neighbor;
                            this.GraphNodes.put(neighbor.position, neighbor);
                        }
                    });
                    open.remove(current);
                }
                f_limit = f_min;

                if (extensionAIOptions.get("usePathFindersDebug")) {
                    long iterationTime = nanoTime() - this.iterationStartTime;
                    this.maxTimePerIteration = max(this.maxTimePerIteration, iterationTime);
                    this.minTimePerIteration = min(this.minTimePerIteration, iterationTime);
                }
            }
            this.pathNotFoundLog("FringeS");
        }
        return null;
    }
}
