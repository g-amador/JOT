/*
 * This file is part of the JOT game engine managers framework toolkit
 * component. 
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
package jot.manager.ai;

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import com.jogamp.opengl.util.gl2.GLUT;
import static com.jogamp.opengl.util.gl2.GLUT.BITMAP_TIMES_ROMAN_10;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.round;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.HashMap;
import java.util.LinkedList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.ai.Agent;
import jot.ai.pathFinders.AbstractPathFinder;
import jot.ai.pathFinders.FringeSearch;
import jot.ai.pathFinders.influenceMapBased.AStar;
import jot.ai.pathFinders.influenceMapBased.BestFirstSearch;
import jot.ai.pathFinders.influenceMapBased.Dijkstra;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.A_STAR;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.BEST_FIRST_SEARCH;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.DIJKSTRA;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.FRINGE_SEARCH;
import static jot.math.Distance.getDistance;
import jot.math.graph.GraphBackup;
import static jot.util.CoreOptions.coreOptions;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import static jot.util.ExtensionGeometryOptions.extensionGeometryOptions;
import static jot.util.FrameworkOptions.frameworkOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Class that implements Pathfinder algorithms.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
//TODO: do not extend steering behaviours
public class PathFindersManager extends AbstractPathFindersManager {

    static final Logger log = getLogger("PathFinders");

    static {
        log.setLevel(OFF);
    }

    /**
     * The path to follow.
     */
//    protected Vector3D[] path = {new Vector3D(-87, 0, 32),
//        new Vector3D(-56, 0, 45),
//        new Vector3D(-40, 0, 75),
//        new Vector3D(-15, 0, 12),
//        new Vector3D(0, 0, 35),
//        new Vector3D(55, 0, 55),
//        new Vector3D(70, 0, 30),
//        new Vector3D(50, 0, 10),
//        new Vector3D(50, 0, -50)
//    };
    protected Vector3D[] path = {new Vector3D(-25, 0, 40),
        new Vector3D(30, 0, 40),
        new Vector3D(30, 0, -40),
        new Vector3D(-35, 0, -40),
        new Vector3D(-35, 0, -35),
        new Vector3D(-25, 0, -35),
        new Vector3D(-25, 0, 0),
        new Vector3D(-35, 0, 20),
        new Vector3D(-25, 0, 40),}; //The default path to use
//    protected Vector3D[] path = {new Vector3D(-50, 0, -50),
//        new Vector3D(50, 0, 50),}; //The default path to use
//    protected Vector3D[] path = {new Vector3D(0, 0, -50),
//        new Vector3D(0, 0, 50),}; //The default path to use

    protected HashMap<Vector3D, ArrayList<Vector3D>> Graph;
    protected HashMap<Vector3D, ArrayList<Vector3D>> lowLevelGraph;
    protected HashMap<Vector3D, Vector2D> GraphNodesGridCoords;
    protected HashMap<Vector2D, Vector3D> GridCoordsGraphNodes;

    private final Vector3D[] arrayGraph = {
        new Vector3D(-44, 0, 16),
        new Vector3D(-44, 0, 0), //LINE 1
        new Vector3D(-44, 0, 0),
        new Vector3D(-44, 0, -15), //LINE 2
        new Vector3D(-44, 0, -15),
        new Vector3D(-23, 0, -15), //LINE 3
        new Vector3D(-23, 0, -15),
        new Vector3D(-23, 0, 0), //LINE 4
        new Vector3D(-23, 0, 0),
        new Vector3D(-44, 0, 0), //LINE 5
        new Vector3D(-44, 0, 0),
        new Vector3D(-28, 0, 23), //LINE 6
        new Vector3D(-28, 0, 23),
        new Vector3D(-44, 0, 16), //LINE 7
        //new Vector3D(-28, 0, 23),
        //new Vector3D(-20, 0, 38), //LINE 8
        new Vector3D(-20, 0, 38),
        new Vector3D(-23, 0, 0), //LINE 9
        //new Vector3D(-23, 0, 0),
        //new Vector3D(-8, 0, 6), //LINE 10
        new Vector3D(-8, 0, 6),
        new Vector3D(-20, 0, 38), //LINE 11
        new Vector3D(-8, 0, 6),
        new Vector3D(-3, 0, 38), //LINE 12
        new Vector3D(-8, 0, 6),
        new Vector3D(0, 0, 18), //LINE 13
        new Vector3D(-3, 0, 38),
        new Vector3D(30, 0, 48), //LINE 14
        new Vector3D(30, 0, 48),
        new Vector3D(39, 0, 38), //LINE 15
        new Vector3D(39, 0, 38),
        new Vector3D(28, 0, 28), //LINE 16
        new Vector3D(28, 0, 28),
        new Vector3D(-3, 0, 38), //LINE 17
        new Vector3D(-3, 0, 38),
        new Vector3D(0, 0, 18), //LINE 18
        new Vector3D(0, 0, 18),
        new Vector3D(28, 0, 28), //LINE 19
        new Vector3D(28, 0, 28),
        new Vector3D(35, 0, 15), //LINE 20
        new Vector3D(35, 0, 15),
        new Vector3D(25, 0, 5), //LINE 21
        //new Vector3D(25, 0, 5),
        //new Vector3D(0, 0, 18), //LINE 22
        new Vector3D(0, 0, 18),
        new Vector3D(8, 0, 3), //LINE 23
        new Vector3D(8, 0, 3),
        new Vector3D(25, 0, -25), //LINE 24
    //new Vector3D(25, 0, -25),
    //new Vector3D(-23, 0, -15), //LINE 25
    //new Vector3D(-23, 0, -15),
    //new Vector3D(8, 0, 3), //LINE 26
    //new Vector3D(25, 0, 5),
    //new Vector3D(25, 0, -25) //LINE 27
    };

    private AbstractPathFinder apf;
    private PathFinder pathFinder; //default pathFinder to use
    private long start_time, end_time;
    private final GLUT glut = new GLUT();
    private int listGraph;
    private int listVisited;
    private boolean generateListGraph;
    //private boolean generateListVisited;
    private Vector3D GraphColor;
    private long timeout;

    /**
     * Default constructor.
     */
    public PathFindersManager() {
        //The default setup to use
        extensionAIOptions.put("usePathFinders", true);
        frameworkOptions.put("useAI", true);

        this.pathFinder = A_STAR; //default pathFinder to A*
        this.GraphColor = new Vector3D(0.0f, 0.0f, 0.8f);
        this.Graph = this.arrayGraph2HashMapGraph(this.arrayGraph);
        //generateListGraph = generateListVisited = true;

        if (extensionAIOptions.get("usePathFindersTimeout")) {
            this.timeout = 60_000_000_000L; // 60 seconds in nano seconds.
        }

        if (extensionAIOptions.get("usePathFindersDebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * Default constructor.
     *
     * @param timeout the amount of time in seconds at most a pathfinder can
     * attempt to find a path.
     */
    public PathFindersManager(double timeout) {
        //The default setup to use
        extensionAIOptions.put("usePathFinders", true);
        frameworkOptions.put("useAI", true);

        this.pathFinder = A_STAR; //default pathFinder is A*     
        this.GraphColor = new Vector3D(0.0f, 0.0f, 0.8f);
        this.Graph = this.arrayGraph2HashMapGraph(this.arrayGraph);
        //generateListGraph = generateListVisited = true;

        this.timeout = round(timeout * 1_000_000_000L);
        extensionAIOptions.put("usePathFindersTimeout", true);

        if (extensionAIOptions.get("usePathFindersDebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<Vector3D, ArrayList<Vector3D>> getGraph() {
        return extensionAIOptions.get("usePathFinders")
                ? this.Graph : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGraph(Vector3D[] Graph) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.generateListGraph = true;
            this.Graph = this.arrayGraph2HashMapGraph(Graph);
            if (this.apf != null) {
                this.apf.setGraph(this.Graph);
            }

            //TODO: build low level Graph
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGraph(HashMap<Vector3D, ArrayList<Vector3D>> Graph) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.generateListGraph = true;
            this.Graph = Graph;
            if (this.apf != null) {
                this.apf.setGraph(Graph);
            }
            //TODO: build low level Graph
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLowLevelGraph(Vector3D[] lowLevelGraph) {
        if (extensionAIOptions.get("usePathFinders")
                && extensionAIOptions.get("usePathFindersLowLevelGraph")) {
            this.generateListGraph = true;
            this.lowLevelGraph = this.arrayGraph2HashMapGraph(lowLevelGraph);
            if (this.apf != null) {
                this.apf.setGraph(this.lowLevelGraph);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLowLevelGraph(
            HashMap<Vector3D, ArrayList<Vector3D>> lowLevelGraph) {
        if (extensionAIOptions.get("usePathFinders")
                && extensionAIOptions.get("usePathFindersLowLevelGraph")) {
            this.lowLevelGraph = lowLevelGraph;
            if (this.apf != null) {
                this.apf.setGraph(lowLevelGraph);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2D getGraphNodeGridCoords(Vector3D key) {
        return extensionAIOptions.get("usePathFinders")
                ? this.GraphNodesGridCoords.get(key)
                : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGraphNodesGridCoords(
            HashMap<Vector3D, Vector2D> GraphNodesGridCoords) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.GraphNodesGridCoords = GraphNodesGridCoords;
            if (this.apf != null) {
                this.apf.setGraphNodesGridCoords(GraphNodesGridCoords);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector2D getGridCoordsGraphNode(Vector3D key) {
        return extensionAIOptions.get("usePathFinders")
                ? this.GraphNodesGridCoords.get(key)
                : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGridCoordsGraphNodes(
            HashMap<Vector2D, Vector3D> GridCoordsGraphNodes) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.GridCoordsGraphNodes = GridCoordsGraphNodes;
            if (this.apf != null) {
                this.apf.setGridCoordsGraphNodes(GridCoordsGraphNodes);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setGraphColor(Vector3D color) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.GraphColor = color;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPathfinder2use(PathFinder logic) {
        if (extensionAIOptions.get("usePathFinders")) {
            switch (logic) {
                case DIJKSTRA:
                    this.pathFinder = DIJKSTRA;
                    this.apf = new Dijkstra();
                    this.setupGraph();
                    break;
                case A_STAR:
                    this.pathFinder = A_STAR;
                    this.apf = new AStar();
                    this.setupGraph();
                    break;
                case BEST_FIRST_SEARCH:
                    this.pathFinder = BEST_FIRST_SEARCH;
                    this.apf = new BestFirstSearch();
                    this.setupGraph();
                    break;
                case FRINGE_SEARCH:
                    this.pathFinder = FRINGE_SEARCH;
                    this.apf = new FringeSearch();
                    this.setupGraph();
                    break;
                default:
                    log.info("No valid pathFinder selected, using default path!");
            }
        }
    }

    private void setupGraph() {
        this.apf.setGraph(this.Graph);
        this.apf.setGraphNodesGridCoords(this.GraphNodesGridCoords);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setupPathLog(String map, int w, int h) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.apf.setupPathLog(map, w, h);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pathFindersDebugCSVLog() {
        if (extensionAIOptions.get("usePathFinders")
                && extensionAIOptions.get("usePathFindersDebug")) {
            log.info("Map; w.h; k; d; Algorithm; np; nc;"
                    + " no; Memory Consumption (KB);"
                    + " Min Time Per Iteration (ms);"
                    + " Max Time Per Iteration (ms);"
                    + " Mean Time Per Iteration (ms); Time Total (s)");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Agent getPrimaryAgent() {
        if (extensionAIOptions.get("usePathFinders")) {
            return super.getPrimaryAgent();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrimaryAgent(GameObject primaryAgent) {
        if (extensionAIOptions.get("usePathFinders")) {
            super.setPrimaryAgent(primaryAgent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Agent getPrimaryGoal() {
        if (extensionAIOptions.get("usePathFinders")) {
            return super.getPrimaryGoal();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPrimaryGoal(GameObject primaryGoal) {
        if (extensionAIOptions.get("usePathFinders")) {
            super.setPrimaryGoal(primaryGoal);
        }
    }

    /**
     * Converts a Graph array represented by pairs of connected nodes to a
     * hashMap.
     *
     * @param Graph a Graph array represented by pairs of connected nodes.
     * @return a hashMap conversion of Graph array.
     */
    private HashMap<Vector3D, ArrayList<Vector3D>> arrayGraph2HashMapGraph(Vector3D[] Graph) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.start_time = nanoTime();

            if (extensionAIOptions.get("usePathFindersDebug")) {
                log.info("Converting Graph array to hash map.");
            }
            HashMap<Vector3D, ArrayList<Vector3D>> hmGraph = new HashMap<>();
            if (this.GraphNodesGridCoords != null
                    && !this.GraphNodesGridCoords.isEmpty()) {
                if (extensionAIOptions.get("usePathFindersDebug")) {
                    log.info("Using 2D grid coords.");
                }
                //Create each key, one per each individual vertex.
                this.GraphNodesGridCoords.keySet().stream()
                        .filter(key -> !hmGraph.containsKey(key))
                        .forEach(
                                key -> hmGraph.put(new Vector3D(key.toArray()), new ArrayList<>()));

                //for each key create an list with all its neighbors
                hmGraph.keySet().stream().forEach(key -> {
                    Vector2D keyGridCoords = this.GraphNodesGridCoords.get(key);
                    float neighboursCount = 8;
                    if (keyGridCoords.getX() == 0) {
                        neighboursCount -= 3;
                    }

                    if (keyGridCoords.getY() == 0) {
                        neighboursCount -= 3;
                    }

                    if (keyGridCoords.getX() == 0
                            && keyGridCoords.getY() == 0) {
                        neighboursCount++;
                    }

                    ArrayList<Vector3D> neighbors = new ArrayList<>();
                    for (int i = 0; i < Graph.length - 1; i += 2) {
                        if (key.equals(Graph[i])) {
                            neighbors.add(new Vector3D(Graph[i + 1].toArray()));
                        } else if (key.equals(Graph[i + 1])) {
                            neighbors.add(new Vector3D(Graph[i].toArray()));
                        }

                        /**
                         * We can stop for this key when the number of neighbors
                         * equals 8 since we have the maximum of possible
                         * connections for a squared grid.
                         */
                        if (neighbors.size() == neighboursCount) {
                            break;
                        }
                    }
                    hmGraph.put(key, neighbors);
                });
            } else {
                if (extensionAIOptions.get("usePathFindersDebug")) {
                    log.info("Using Graph array");
                }
                //Create each key, one per each individual vertex.
                for (Vector3D key : Graph) {
                    if (!hmGraph.containsKey(key)) {
                        hmGraph.put(new Vector3D(key.toArray()), new ArrayList<>());
                    }
                }

                //for each key create an list with all its neighbors
                hmGraph.keySet().stream().forEach(key -> {
                    ArrayList<Vector3D> neighbors = new ArrayList<>();
                    for (int i = 0; i < Graph.length - 1; i += 2) {
                        if (key.equals(Graph[i])) {
                            neighbors.add(new Vector3D(Graph[i + 1].toArray()));
                        } else if (key.equals(Graph[i + 1])) {
                            neighbors.add(new Vector3D(Graph[i].toArray()));
                        }

                        /**
                         * We can stop for this key when the number of neighbors
                         * equals 8 since we have the maximum of possible
                         * connections for a squared grid.
                         */
                        if (neighbors.size() == 8) {
                            break;
                        }
                    }
                    hmGraph.put(key, neighbors);
                });
            }
            if (extensionAIOptions.get("usePathFindersDebug")) {
                this.end_time = nanoTime() - this.start_time;
                log.info(format("Conversion took %.3f (milliseconds)",
                        this.end_time / 1000000.0f));
            }
            return hmGraph;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D[] findPath() {
        if (extensionAIOptions.get("usePathFinders")) {
            //generateListVisited = true;

            //If the primary agent position not a Graph node look for the closest one.
            Vector3D start = this.getClosestNode(new Vector3D(this.primaryAgent.getPosition().toArray()));
            //If the primary goal position not a Graph node look for the closest one.
            Vector3D goal = this.getClosestNode(new Vector3D(this.primaryGoal.getPosition().toArray()));

            //find path
            LinkedList<Vector3D> solution = new LinkedList<>();
            Vector3D[] solutionArray = this.apf.findPath(start, goal, this.timeout);
            if (solutionArray != null) {
                if (!frameworkOptions.get("usePathFindersAdaptivityTest")) {
                    solution.addAll(asList(solutionArray));
                } else {
                    LinkedList<Vector3D> temp_solution = new LinkedList<>();
                    temp_solution.addAll(asList(solutionArray));

                    Vector3D key = null;
                    ArrayList<Vector3D> neighbors = new ArrayList<>();
                    GraphBackup gb = null;
                    if (frameworkOptions.get("usePathFindersAdaptivityTest")) {
                        //remove node and neighbors from the Graph.
                        key = temp_solution.get(temp_solution.size() / 2);
                        if (frameworkOptions.get("usePathFindersRemoveNeighborsAdaptivityTest")) {
                            neighbors.addAll(this.Graph.get(key));
                        }
                        gb = this.removeNode(key);
                        if (frameworkOptions.get("usePathFindersRemoveNeighborsAdaptivityTest")) {
                            neighbors.stream().forEach(neighbor -> {
                                this.removeNode(neighbor);
                            });
                        }
                    }

                    for (int i = 0; i < temp_solution.size(); i++) {
                        Vector3D pn_pos = temp_solution.get(i);

                        if (frameworkOptions.get("usePathFindersAdaptivityTest")) {
                            if (key != null && pn_pos != null && !pn_pos.equals(key)
                                    && !neighbors.contains(pn_pos)) {
                                solution.add(pn_pos);
                            } else {
                                Vector3D localStart;
                                Vector3D localGoal;

                                int j = 1;
                                if (frameworkOptions.get("usePathFindersAdaptivityTest")
                                        && frameworkOptions.get("usePathFindersRemoveNeighborsAdaptivityTest")) {
                                    j = 3;
                                }

                                if (i < j) {
                                    localStart = new Vector3D(start.toArray());
                                } else {
                                    localStart = temp_solution.get(i - j);
                                }

                                if (i > temp_solution.size() - (j + 1)) {
                                    localGoal = new Vector3D(goal.toArray());
                                } else {
                                    localGoal = temp_solution.get(i + j);
                                }

                                solution.addAll(asList(this.apf.findPath(
                                        localStart, localGoal, this.timeout)));

                                i += j;
                            }

                        }
                    }

                    //remove nodes between two repeated nodes in the found solution, 
                    //i.e., remove unnecessary loops
                    for (int i = 0; i < solution.size(); i++) {
                        Vector3D pos = solution.get(i);
                        for (int j = i + 1; j < solution.size(); j++) {
                            if (pos.equals(solution.get(j))) {
                                //System.out.println(i + " " + j);
                                for (int k = i; k < j; k++) {
                                    //System.out.println(k);
                                    solution.remove(i);
                                }
                                break;
                            }
                        }
                    }

                    //int count = 0;
                    //for (int i = 0; i < solution.size(); i++) {
                    //    Vector3D pos = solution.get(i);
                    //    for (int j = i + 1; j < solution.size(); j++) {
                    //        if (pos.equals(solution.get(j))) {
                    //            count++;
                    //        }
                    //    }
                    //    if (count > 1) {
                    //        System.out.println("repetidos " + pos + " " + count);
                    //        count = 0;
                    //    }
                    //}                                                   
                    if (frameworkOptions.get("usePathFindersAdaptivityTest")
                            && gb != null) {
                        //set the Graph as it was.
                        this.restoreGraph(gb);
                    }
                }

                if ((frameworkOptions.get("usePathFindersAdaptivityTest"))
                        && frameworkOptions.get("usePathFindersReconstructPathDebug")
                        && (solution.isEmpty() || !solution.getFirst().equals(start))) {
                    log.info("Path not found.");
                    //log.info(format("Path not found between " + start + " and " + goal));
                }
                return solution.toArray(new Vector3D[solution.size()]);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getClosestNode(Vector3D position) {
        if (extensionAIOptions.get("usePathFinders")) {
            //System.out.println(Graph_hs.contains(position));
            //System.out.println(position);
            if (!this.lowLevelGraph.containsKey(position)) {
                double minDistance = POSITIVE_INFINITY;
                for (Vector3D nodePosition : this.lowLevelGraph.keySet()) {
                    double distance = getDistance(nodePosition,
                            this.primaryAgent.getPosition());
                    if (minDistance > distance) {
                        minDistance = distance;
                        position = new Vector3D(nodePosition.toArray());
                    }
                }
            }
            //if (!this.Graph.containsKey(position)) {
            //    double minDistance = POSITIVE_INFINITY;
            //    for (Vector3D nodePosition : this.Graph.keySet()) {
            //        double distance = getDistance(nodePosition,
            //                this.primaryAgent.getPosition());
            //        if (minDistance > distance) {
            //            minDistance = distance;
            //            position = new Vector3D(nodePosition.toArray());
            //        }
            //    }
            //}
            return position;
            //System.out.println(Graph_hs.contains(position));
            //System.out.println(position);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreGraph() {
        if (extensionAIOptions.get("usePathFinders")) {
            this.setGraph(this.Graph);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void restoreGraph(GraphBackup gb) {
        if (extensionAIOptions.get("usePathFinders")) {
            this.setGraph(gb.Graph);
            this.setGraphNodesGridCoords(gb.GraphNodesGridCoords);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GraphBackup removeNode(Vector3D key) {
        if (extensionAIOptions.get("usePathFinders")) {
            Vector3D closest = new Vector3D(key.toArray());
            GraphBackup gb = new GraphBackup(this.Graph,
                    this.GraphNodesGridCoords, this.GridCoordsGraphNodes);

            //If the node key not position not a Graph node look for the closest one.
            //System.out.println(closest);
            //System.out.println(this.Graph.containsKey(closest));
            if (!this.Graph.containsKey(closest)) {
                double minDistance = POSITIVE_INFINITY;
                for (Vector3D nodePosition : this.Graph.keySet()) {
                    double distance = getDistance(nodePosition,
                            this.primaryAgent.getPosition());
                    if (minDistance > distance) {
                        minDistance = distance;
                        closest = new Vector3D(nodePosition.toArray());
                    }
                }
            }
            //System.out.println(closest);
            //System.out.println(this.Graph.containsKey(closest));

            this.GridCoordsGraphNodes.remove(this.GraphNodesGridCoords.get(closest));
            this.setGridCoordsGraphNodes(this.GridCoordsGraphNodes);
            this.GraphNodesGridCoords.remove(closest);
            this.setGraphNodesGridCoords(this.GraphNodesGridCoords);

            //Remove conections from and to the Graph node to remove.   
            for (Vector3D neighbor_position : this.Graph.get(closest)) {
                ArrayList<Vector3D> neighbors = new ArrayList<>();
                neighbors.addAll(this.Graph.get(neighbor_position));
                neighbors.remove(closest);
                this.Graph.put(neighbor_position, neighbors);
            }
            //Remove from the Graph node to remove.
            this.Graph.remove(closest);
            this.setGraph(this.Graph);

            return gb;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderAI(GL2 gl) {
        this.renderGraph(gl);
        this.renderVisited(gl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderGraph(GL2 gl) {
        if (extensionAIOptions.get("usePathFinders")
                && frameworkOptions.get("showGraph")
                && gl != null) {
            gl.glPushMatrix();
            {
                if (this.generateListGraph) {
                    gl.glDeleteLists(this.listGraph, 1);
                    this.listGraph = gl.glGenLists(1);
                    gl.glNewList(this.listGraph, GL_COMPILE);
                    {
                        this.Graph = this.apf.getGraph();
                        this.Graph.keySet().stream().forEach(key -> {
                            ArrayList<Vector3D> neighbors = this.Graph.get(key);
                            neighbors.stream().forEach(neighbor -> {
                                gl.glLineWidth(1.5f);
                                //gl.glLineWidth(2.5f);
                                gl.glBegin(GL_LINES);
                                {
                                    gl.glColor3d(
                                            this.GraphColor.getX(),
                                            this.GraphColor.getY(),
                                            this.GraphColor.getZ());
                                    gl.glVertex3d(
                                            key.getX(),
                                            coreOptions.get("HOG2Maps")
                                            ? .01 : key.getY() + .01,
                                            key.getZ());
                                    gl.glVertex3d(
                                            neighbor.getX(),
                                            coreOptions.get("HOG2Maps")
                                            ? .01 : neighbor.getY() + .01,
                                            neighbor.getZ());
                                }
                                gl.glEnd();

                                if (extensionAIOptions.get("usePathFindersDebug")
                                        && !extensionGeometryOptions.get("useMazeGenerators")
                                        && !coreOptions.get("HOG2Maps")) {
                                    gl.glColor3f(.0f, .0f, .0f);
                                    gl.glRasterPos3d(
                                            key.getX(),
                                            key.getY() + .04,
                                            key.getZ());
                                    this.glut.glutBitmapString(BITMAP_TIMES_ROMAN_10,
                                            key.toString());

                                    gl.glRasterPos3d(
                                            neighbor.getX(),
                                            neighbor.getY() + .04,
                                            neighbor.getZ());
                                    this.glut.glutBitmapString(BITMAP_TIMES_ROMAN_10,
                                            neighbor.toString());
                                }
                            });
                        });
                    }
                    gl.glEndList();
                    this.generateListGraph = false;
                }

                gl.glDisable(GL_LIGHTING);
                gl.glCallList(this.listGraph);
                gl.glEnable(GL_LIGHTING);
            }
            gl.glPopMatrix();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void renderVisited(GL2 gl) {
        if (extensionAIOptions.get("usePathFinders")
                && frameworkOptions.get("showVisited")
                && gl != null) {
            ArrayList<Vector3D> visited = this.apf.getVisited();
            if (visited != null && !visited.isEmpty()) {
                gl.glPushMatrix();
                {
                    //if (generateListVisited) {
                    gl.glDeleteLists(this.listVisited, 1);
                    this.listVisited = gl.glGenLists(1);
                    gl.glNewList(this.listVisited, GL_COMPILE);
                    {
                        gl.glLineWidth(1.5f);
                        //gl.glLineWidth(2.5f);
                        gl.glBegin(GL_LINES);
                        {
                            for (int i = 0; i < visited.size() - 1; i += 2) {
                                gl.glColor3d(
                                        (1.0f - this.GraphColor.getX())
                                        / this.GraphColor.getX(),
                                        (1.0f - this.GraphColor.getY())
                                        / this.GraphColor.getY(),
                                        (1.0f - this.GraphColor.getZ())
                                        / this.GraphColor.getZ());

                                gl.glVertex3d(
                                        visited.get(i).getX(),
                                        coreOptions.get("HOG2Maps")
                                        ? .02
                                        : visited.get(i).getY() + .02,
                                        visited.get(i).getZ());

                                gl.glVertex3d(
                                        visited.get(i + 1).getX(),
                                        coreOptions.get("HOG2Maps")
                                        ? .02
                                        : visited.get(i + 1).getY() + .02,
                                        visited.get(i + 1).getZ());
                            }
                        }
                        gl.glEnd();
                    }
                    gl.glEndList();
                    //generateListVisited = false;
                    //}

                    gl.glDisable(GL_LIGHTING);
                    gl.glCallList(this.listVisited);
                    gl.glEnable(GL_LIGHTING);
                }
                gl.glPopMatrix();
            }
        }
    }

    /**
     * Dispose the listPath, listGraph, and listVisited lists.
     *
     * @param gl
     */
    @Override
    public void dispose(GL2 gl) {
        if (extensionAIOptions.get("usePathFinders") && gl != null) {
            log.info("Dispose listGraph.");
            gl.glDeleteLists(this.listGraph, 1);
            log.info("Dispose listVisited.");
            gl.glDeleteLists(this.listVisited, 1);
        }
    }
}
