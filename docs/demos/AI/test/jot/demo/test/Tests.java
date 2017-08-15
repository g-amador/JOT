package jot.demo.test;

/*
 * This file is part of the PathFinder program. This is a simple program that
 * serves as a testbed for steering behaviors, pathFinding, and maze generation
 * algorithms. The program features a JogAmp-based graphical component, to
 * visualize the graph to traverse, the found (if one exists) path and the
 * traversed nodes.
 *
 * The program also includes a loader for Collada 1.4 models and HOG2
 * Pathfinding Benchmarks, available at <http://www.movingai.com/benchmarks/>.
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
import java.io.File;
import static java.lang.Long.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.lang.String.format;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;
import java.util.HashSet;
import java.util.LinkedList;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.ai.influenceMaps.InfluenceMapGaussian;
import static jot.demo.util.GameConstants.setGameConstants;
import jot.io.data.format.HOG2Map.HOG2MapMesh;
import jot.io.data.format.UnzipUtility;
import static jot.manager.SceneManager.localAssetManager;
import jot.manager.ai.AbstractPathFindersManager.PathFinder;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.ALPHA_STAR;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.A_STAR;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.BEST_FIRST_SEARCH;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.BEST_NEIGHBOR_SEARCH;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.DIJKSTRA;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.FRINGE_SEARCH;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.OPTIMISTIC_SEARCH;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.REVISED_DYNAMIC_WEIGHTED_A_STAR;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.SKEPTICAL_SEARCH;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.STATIC_WEIGHTED_A_STAR;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.TRACE;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.TRIMMED_BEST_NEIGHBOR_SEARCH;
import jot.manager.ai.PathFindersManager;
import jot.math.geometry.TransformGroup;
import static jot.math.geometry.bounding.AbstractBoundingVolume.BoundingVolumeType.OBB;
import jot.math.geometry.generators.maze.Prim;
import static jot.math.graph.Converters.getRandomGraphNode;
import static jot.util.FrameworkOptions.frameworkOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a battery of Tests in the terminal.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Tests {

    static final Logger log = getLogger("TestsJUnit4");

    private final static float size = 100;
    private static PathFindersManager pfm;
    private static long maxAttractorCount = 0;
    private static long minAttractorCount = MAX_VALUE;
    private static long maxRepellerCount = 0;
    private static long minRepellerCount = MAX_VALUE;
    private static String maxAttractorCountMap;
    private static String minAttractorCountMap;
    private static String maxRepellerCountMap;
    private static String minRepellerCountMap;

    /**
     * Tests method.
     *
     * @param args
     */
    public static void main(String[] args) {
        setGameConstants();

        pfm = new PathFindersManager();
        //pfm = new PathFindersManager(60);

        log.setLevel(INFO);

        if (frameworkOptions.get("HOG2Maps")) {
            //Test batteries with HOG2Map files   
            UnzipUtility uu = new UnzipUtility();
            uu.unzip("dist/assets/HOG2/DragonAgeOrigins/map_tests.zip", "dist/assets/HOG2/DragonAgeOrigins/");
            File folder1 = new File("dist/assets/HOG2/DragonAgeOrigins/map_tests");
            for (final File fileEntry : folder1.listFiles()) {
                if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".map")) {
                    //&& fileEntry.getName().contains("arena2")) {
                    //log.info(format("----------Starting test batterry for %s----------", fileEntry.getName()));
                    Tests t = new Tests(fileEntry.getPath().replace("../", ""),
                            frameworkOptions.get("useInfluenceMaps") ? 12 : 25);
                }
            }
            uu.unzip("dist/assets/HOG2/WarcraftIII/map_tests.zip", "dist/assets/HOG2/WarcraftIII/");
            File folder2 = new File("dist/assets/HOG2/WarcraftIII/map_tests");
            for (final File fileEntry : folder2.listFiles()) {
                if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".map")) {
                    //&& fileEntry.getName().contains("divideandconquer")) {
                    //log.info(format("----------Starting test batterry for %s----------", fileEntry.getName()));
                    Tests t = new Tests(fileEntry.getPath().replace("../", ""),
                            frameworkOptions.get("useInfluenceMaps") ? 12 : 25);
                }
            }

            if (frameworkOptions.get("useInfluenceMaps")) {
                log.info(format("Max Attractor Count Map: %s", maxAttractorCountMap));
                log.info(format("Max Attractor Count: %d", maxAttractorCount));
                log.info(format("Min Attractor Count Map: %s", minAttractorCountMap));
                log.info(format("Min Attractor Count: %d", minAttractorCount));
                log.info(format("Max Repeller Count Map: %s", maxRepellerCountMap));
                log.info(format("Max Repeller Count: %d", maxRepellerCount));
                log.info(format("Min Repeller Count Map: %s", minRepellerCountMap));
                log.info(format("Min Repeller Count: %d", minRepellerCount));
            }
        }

        if (frameworkOptions.get("useMazeGenerators")
                && !frameworkOptions.get("useInfluenceMaps")) {
            //Test batteries with maze generation
            //log.info("----------Starting test batterry 1----------");
            Tests t1 = new Tests(25, 25, 25);
            //log.info("----------Starting test batterry 2----------");
            Tests t2 = new Tests(50, 50, 25);
            //log.info("----------Starting test batterry 3----------");
            Tests t3 = new Tests(100, 100, 25);
            //log.info("----------Starting test batterry 4----------");
            Tests t4 = new Tests(250, 250, 25);
//        log.info("----------Starting test batterry 5----------");
//        Tests t5 = new Tests(500, 500, 25);
//        log.info("----------Starting test batterry 6----------");
//        Tests t6 = new Tests(1000, 1000, 25);
//        log.info("----------Starting test batterry 7----------");
//        Tests t7 = new Tests(2500, 2500, 25);
//        log.info("----------Starting test batterry 8----------");
//        Tests t8 = new Tests(5000, 5000, 25);
//        log.info("----------Starting test batterry 9----------");
//        Tests t9 = new Tests(10000, 10000, 25);        
        }
    }

    private LinkedList<Vector3D> attractorsPositions;

    /**
     * HOG2 map test battery method.
     *
     * @param filePath the path to the HOG2 map.
     * @param iters the number of mazes to generate for each test in the
     * battery,
     */
    public Tests(String filePath, int iters) {
        //log.info("Loading map file.");
        //SceneManager sceneManager = new SceneManager(null);
        String fileName;
        if (filePath.contains("\\")) {
            fileName = filePath.split("\\\\")[filePath.split("\\\\").length - 1];
        } else {
            fileName = filePath.split("/")[filePath.split("/").length - 1];
        }
        //System.out.println(fileName);
        filePath = filePath.replace(fileName, "");
        TransformGroup floor;
        floor = localAssetManager.loadFormat(filePath, fileName, 1, OBB);
        pfm.setGridCoordsGraphNodes(
                //TODO: alter when implemented for a compound mesh.
                ((HOG2MapMesh) floor.getMeshes().get(0)).getGridCoordinatesGraphNodes());
        pfm.setGraphNodesGridCoords(
                //TODO: alter when implemented for a compound mesh.
                ((HOG2MapMesh) floor.getMeshes().get(0)).getGraphNodesGridCoordinates());
        pfm.setGraph(
                //TODO: alter when implemented for a compound mesh.
                ((HOG2MapMesh) floor.getMeshes().get(0)).getGraph());

        String map = fileName.replace(".map", "");
        int w = ((HOG2MapMesh) floor.getMeshes().get(0)).getHOG2MapWidth();
        int h = ((HOG2MapMesh) floor.getMeshes().get(0)).getHOG2MapHeight();

        //log.info("Done loading map file.");   
        //Set the start and goal nodes.
        Vector3D start, goal;

        //agent position
        GameObject agent = new GameObject("agent");
        //target position
        GameObject target = new GameObject("target");

        Vector3D leftmostBottommostNode = ((HOG2MapMesh) floor.getMeshes().get(0)).getLeftmostBottommostNode();
        Vector3D leftmostTopmostNode = ((HOG2MapMesh) floor.getMeshes().get(0)).getLeftmostTopmostNode();
        Vector3D rightmostBottommostNode = ((HOG2MapMesh) floor.getMeshes().get(0)).getRightmostBottommostNode();
        Vector3D rightmostTopmostNode = ((HOG2MapMesh) floor.getMeshes().get(0)).getRightmostTopmostNode();

        if (frameworkOptions.get("usePathFindersLowLevelGraph")) {
            //Ensure the start and goal graph nodes are random ones
            //start = getRandomGraphNode(pfm.getGraph());
            //do {
            //    goal = getRandomGraphNode(pfm.getGraph());
            //} while (goal.equals(start));
            //log.info(start.toString());
            //log.info(goal.toString());
            start = leftmostTopmostNode;
            goal = rightmostBottommostNode;
        } else {
            //TODO: alter when implemented for a compound mesh.
            start = floor.getMeshes().get(0).getMaxVertex();

            //TODO: alter when implemented for a compound mesh.
            goal = floor.getMeshes().get(0).getMinVertex();
        }
        agent.setPosition(start);
        pfm.setPrimaryAgent(agent);

        target.setPosition(goal);
        pfm.setPrimaryGoal(target);

        if (frameworkOptions.get("useInfluenceMaps")) {
            //log.info("\nSetup Influence Map.");

            float threshold = .1f;
            float influenceRepeller = (float) sqrt(10);
            float influenceAttractor = -(float) sqrt(10);

            InfluenceMapGaussian im = new InfluenceMapGaussian(
                    //TODO: alter when implemented for a compound mesh.
                    ((HOG2MapMesh) floor.getMeshes().get(0)).getHOG2MapWidth(),
                    ((HOG2MapMesh) floor.getMeshes().get(0)).getHOG2MapHeight(),
                    ((HOG2MapMesh) floor.getMeshes().get(0)).getNonDiagonalMaxDistance(),
                    ((HOG2MapMesh) floor.getMeshes().get(0)).getDiagonalMaxDistance(),
                    threshold, 1);
            im.setInfluenceValueRepeller(influenceRepeller);
            im.setInfluenceValueAttractor(influenceAttractor);

            for (int i = 0; i < iters; i++) {
                pfm.pathFindersDebugCSVLog();

                this.Test(floor, DIJKSTRA, fileName, filePath, w, h, im,
                        influenceAttractor, influenceRepeller);
                this.Test(floor, A_STAR, fileName, filePath, w, h, im,
                        influenceAttractor, influenceRepeller);
                //this.Test(floor, BEST_FIRST_SEARCH, fileName, filePath, w, h, im,
                //        influenceAttractor, influenceRepeller);

                /*
                //log.info("Not using influence maps.\n");
                frameworkOptions.put("useInfluenceMaps", false);
                //log.info("bound = 1.5");
                this.Test(STATIC_WEIGHTED_A_STAR, map, w, h, 1.5f);
                this.Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 1.5f);
                //this.Test(ALPHA_STAR, map, w, h, 1.5f);
                if (frameworkOptions.get("useInfluenceMapsNegativeValuedAttractors")) {
                    this.Test(SKEPTICAL_SEARCH, map, w, h, 1.5f);
                } else {
                    this.Test(OPTIMISTIC_SEARCH, map, w, h, 1.5f);
                }
                ////log.info("bound = 1.75");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 1.75f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 1.75f);
                //Test(ALPHA_STAR, map, w, h, 1.75f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 1.75f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 1.75f);
                ////log.info("bound = 2");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 2.0f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 2.0f);
                //Test(ALPHA_STAR, map, w, h, 2.0f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 2.0f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 2.0f);
                ////log.info("bound = 3");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 3.0f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 3.0f);
                //Test(ALPHA_STAR, map, w, h, 3.0f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 3.0f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 3.0f);
                ////log.info("bound = 5");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 5.0f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 5.0f);
                //Test(ALPHA_STAR, map, w, h, 5.0f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 5.0f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 5.0f);
                //log.info("Using influence maps.\n");
                frameworkOptions.put("useInfluenceMaps", true);
                 */
                //Set the next start and goal nodes.
                //log.info(format("agent.getPosition():" + agent.getPosition()));
                //log.info(format("target.getPosition():" + target.getPosition()));
                if (frameworkOptions.get("usePathFindersLowLevelGraph")) {
                    //Ensure the start and goal graph nodes are random ones
                    //start = getRandomGraphNode(pfm.getGraph());
                    //do {
                    //    goal = getRandomGraphNode(pfm.getGraph());
                    //} while (goal.equals(start));
                    ////log.info(start.toString());
                    ////log.info(goal.toString());
                    switch (iters) {
                        case 1:
                            start = leftmostTopmostNode;
                            goal = leftmostBottommostNode;
                            break;
                        case 2:
                            start = leftmostTopmostNode;
                            goal = rightmostTopmostNode;
                            break;
                        case 3:
                            start = leftmostBottommostNode;
                            goal = leftmostTopmostNode;
                            break;
                        case 4:
                            start = leftmostBottommostNode;
                            goal = rightmostTopmostNode;
                            break;
                        case 5:
                            start = leftmostBottommostNode;
                            goal = rightmostBottommostNode;
                            break;
                        case 6:
                            start = rightmostTopmostNode;
                            goal = leftmostTopmostNode;
                            break;
                        case 7:
                            start = rightmostTopmostNode;
                            goal = leftmostBottommostNode;
                            break;
                        case 8:
                            start = rightmostTopmostNode;
                            goal = rightmostBottommostNode;
                            break;
                        case 9:
                            start = rightmostBottommostNode;
                            goal = leftmostTopmostNode;
                            break;
                        case 10:
                            start = rightmostBottommostNode;
                            goal = leftmostBottommostNode;
                            break;
                        case 11:
                            start = rightmostBottommostNode;
                            goal = rightmostTopmostNode;
                            break;
                        default:
                            break;
                    }

                    //agent position
                    agent = new GameObject("agent");
                    agent.setPosition(start);
                    pfm.setPrimaryAgent(agent);

                    //target position
                    target = new GameObject("target");
                    target.setPosition(goal);
                    pfm.setPrimaryGoal(target);
                } else if (this.attractorsPositions.size() > 1) {
                    //Chose two random attractors as the new start and goal
                    //nodes, as long there is at least half of all attractors
                    //between them.
                    do {
                        float maxX = (float) max(
                                this.attractorsPositions.peekFirst().getX(),
                                this.attractorsPositions.peekLast().getX());
                        float minX = (float) min(
                                this.attractorsPositions.peekFirst().getX(),
                                this.attractorsPositions.peekLast().getX());
                        float maxY = (float) max(
                                this.attractorsPositions.peekFirst().getY(),
                                this.attractorsPositions.peekLast().getY());
                        float minY = (float) min(
                                this.attractorsPositions.peekFirst().getY(),
                                this.attractorsPositions.peekLast().getY());
                        float maxZ = (float) max(
                                this.attractorsPositions.peekFirst().getZ(),
                                this.attractorsPositions.peekLast().getZ());
                        float minZ = (float) min(
                                this.attractorsPositions.peekFirst().getZ(),
                                this.attractorsPositions.peekLast().getZ());

                        int count = 0;
                        count = this.attractorsPositions.stream()
                                .filter(ap
                                        -> !ap.equals(this.attractorsPositions.peekFirst())
                                && !ap.equals(this.attractorsPositions.peekLast())
                                && minX <= ap.getX() && ap.getX() <= maxX
                                && minY <= ap.getY() && ap.getY() <= maxY
                                && minZ <= ap.getZ() && ap.getZ() <= maxZ)
                                .map(ap -> 1).reduce(count, Integer::sum);

                        if (count > 3) {
                            //make sure these are not used in the next path search.
                            //attractorsPositions.removeFirst();
                            //attractorsPositions.removeLast();
                            //shuffle attractors by position.
                            shuffle(this.attractorsPositions);
                            break;
                        } else {
                            //shuffle attractors by position.
                            shuffle(this.attractorsPositions);
                        }
                    } while (true);

                    //agent position
                    agent = new GameObject("agent");
                    //TODO: alter when implemented for a compound mesh.
                    agent.setPosition(this.attractorsPositions.peekFirst());
                    pfm.setPrimaryAgent(agent);

                    //target position
                    target = new GameObject("target");
                    //TODO: alter when implemented for a compound mesh.                
                    target.setPosition(this.attractorsPositions.peekLast());
                    pfm.setPrimaryGoal(target);
                }
                log.info("");
            }
        } else {
            //log.info("Not using influence maps.\n");           
            frameworkOptions.put("useInfluenceMaps", false);
            pfm.pathFindersDebugCSVLog();

            for (int i = 0; i < iters; i++) {
                this.Test(DIJKSTRA, map, w, h);
                this.Test(A_STAR, map, w, h);
                this.Test(BEST_FIRST_SEARCH, map, w, h);
                this.Test(FRINGE_SEARCH, map, w, h);
                //Test(TRACE, map, w, h);
                this.Test(BEST_NEIGHBOR_SEARCH, map, w, h);
                this.Test(TRIMMED_BEST_NEIGHBOR_SEARCH, map, w, h);
                ////log.info("bound = 1.5");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 1.5f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 1.5f);
                //Test(ALPHA_STAR, map, w, h, 1.5f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 1.5f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 1.5f);
                ////log.info("bound = 1.75");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 1.75f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 1.75f);
                //Test(ALPHA_STAR, map, w, h, 1.75f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 1.75f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 1.75f);
                ////log.info("bound = 2");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 2.0f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 2.0f);
                //Test(ALPHA_STAR, map, w, h, 2.0f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 2.0f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 2.0f);
                ////log.info("bound = 3");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 3.0f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 3.0f);
                //Test(ALPHA_STAR, map, w, h, 3.0f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 3.0f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 3.0f);
                ////log.info("bound = 5");
                //Test(STATIC_WEIGHTED_A_STAR, map, w, h, 5.0f);
                //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, map, w, h, 5.0f);
                //Test(ALPHA_STAR, map, w, h, 5.0f);
                //Test(OPTIMISTIC_SEARCH, map, w, h, 5.0f);
                //Test(SKEPTICAL_SEARCH, map, w, h, 5.0f);

                //Ensure the start and goal graph nodes are random ones.
                //log.info(format("agent.getPosition():" + agent.getPosition()));
                //log.info(format("target.getPosition():" + target.getPosition()));                
                start = getRandomGraphNode(pfm.getGraph());
                do {
                    goal = getRandomGraphNode(pfm.getGraph());
                } while (goal.equals(start));
                //log.info(start.toString());
                //log.info(goal.toString());

                //set agent position
                agent = new GameObject("agent");
                //TODO: alter when implemented for a compound mesh.
                agent.setPosition(start);
                pfm.setPrimaryAgent(agent);

                //target position
                target = new GameObject("target");
                //TODO: alter when implemented for a compound mesh.                
                target.setPosition(goal);
                pfm.setPrimaryGoal(target);
                log.info("");
            }
        }
    }

    /**
     * Perform iters maze test batteries, testing each maze for all pathfinders.
     *
     * @param mazeWidth
     * @param mazeLength
     * @param iters the number of mazes to generate for each test in the
     * battery,
     */
    public Tests(int mazeWidth, int mazeLength, int iters) {
        for (int i = 0; i < iters; i++) {
            //generate maze        
            Prim mg = new Prim(mazeWidth, mazeLength, size);
            mg.generateGeometry();

            pfm.setGraph(mg.getMaze());
            pfm.setGraphNodesGridCoords(null);
            pfm.pathFindersDebugCSVLog();

            //set agent position
            GameObject agent = new GameObject("agent");
            agent.setPosition(mg.getMazeMax());
            pfm.setPrimaryAgent(agent);

            //set target position
            GameObject target = new GameObject("target");
            target.setPosition(mg.getMazeMin());
            pfm.setPrimaryGoal(target);

            this.Test(DIJKSTRA, "Maze", mazeWidth, mazeLength);
            this.Test(A_STAR, "Maze", mazeWidth, mazeLength);
            this.Test(BEST_FIRST_SEARCH, "Maze", mazeWidth, mazeLength);
            this.Test(FRINGE_SEARCH, "Maze", mazeWidth, mazeLength);
            //Test(TRACE, "Maze", mazeWidth, mazeLength);
            this.Test(BEST_NEIGHBOR_SEARCH, "Maze", mazeWidth, mazeLength);
            this.Test(TRIMMED_BEST_NEIGHBOR_SEARCH, "Maze", mazeWidth, mazeLength);
            ////log.info("bound = 1.5");
            //Test(STATIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 1.5f);
            //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 1.5f);
            //Test(ALPHA_STAR, "Maze", mazeWidth, mazeLength, 1.5f);
            //Test(OPTIMISTIC_SEARCH, "Maze", mazeWidth, mazeLength, 1.5f);
            //Test(SKEPTICAL_SEARCH, "Maze", mazeWidth, mazeLength, 1.5f);
            ////log.info("bound = 1.75");
            //Test(STATIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 1.75f);
            //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 1.75f);
            //Test(ALPHA_STAR, "Maze", mazeWidth, mazeLength, 1.75f);
            //Test(OPTIMISTIC_SEARCH, "Maze", mazeWidth, mazeLength, 1.75f);
            //Test(SKEPTICAL_SEARCH, "Maze", mazeWidth, mazeLength, 1.75f);
            ////log.info("bound = 2");
            //Test(STATIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 2);
            //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 2);
            //Test(ALPHA_STAR, "Maze", mazeWidth, mazeLength, 2);
            //Test(OPTIMISTIC_SEARCH, "Maze", mazeWidth, mazeLength, 2);
            //Test(SKEPTICAL_SEARCH, "Maze", mazeWidth, mazeLength, 2);
            ////log.info("bound = 3");
            //Test(STATIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 3);
            //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 3);
            //Test(ALPHA_STAR, "Maze", mazeWidth, mazeLength, 3);
            //Test(OPTIMISTIC_SEARCH, "Maze", mazeWidth, mazeLength, 3);
            //Test(SKEPTICAL_SEARCH, "Maze", mazeWidth, mazeLength, 3);
            ////log.info("bound = 5");
            //Test(STATIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 5);
            //Test(REVISED_DYNAMIC_WEIGHTED_A_STAR, "Maze", mazeWidth, mazeLength, 5);
            //Test(ALPHA_STAR, "Maze", mazeWidth, mazeLength, 5);
            //Test(OPTIMISTIC_SEARCH, "Maze", mazeWidth, mazeLength, 5);
            //Test(SKEPTICAL_SEARCH, "Maze", mazeWidth, mazeLength, 5);            
            log.info("");
        }
        //log.info("");
    }

    private void Test(TransformGroup floor, PathFinder logic,
            String fileName, String filePath, int w, int h,
            InfluenceMapGaussian im,
            float influenceAttractor, float influenceRepeller) {
        if (frameworkOptions.get("usePathFindersLowLevelGraph")) {
            this.setupInfluenceMap(floor, logic, fileName.replace(".map", ""),
                    w, h, im, influenceAttractor, influenceRepeller);
            this.setupHog2mapsFileLog(fileName, im);
            pfm.setInfluenceMap(im);
            //pfm.pathFindersDebugCSVLog();

            frameworkOptions.put("usePathFindersLowLevelGraph", false);
            this.Test(logic, fileName.replace(".map", ""), w, h);
            frameworkOptions.put("usePathFindersLowLevelGraph", true);
        } else {
            im.loadInfluenceMap(filePath, fileName.replace(".map", ".js"));
            im.updateInfluenceMap(0);
            this.setupHog2mapsFileLog(fileName, im);
            pfm.setInfluenceMap(im);
            //pfm.pathFindersDebugCSVLog();

            //get all attractors.
            this.attractorsPositions = new LinkedList<>();
            im.getPropagators().stream()
                    .filter(pg -> pg.getInfluenceSignum() < 0)
                    .forEach(pg -> {
                        this.attractorsPositions.add(pg.getPosition());
                    });

            //shuffle attractors by position.
            shuffle(this.attractorsPositions);

            this.Test(logic, fileName.replace(".map", ""), w, h);
        }
    }

    private Vector3D[] Test(PathFinder logic, String map, int w, int h) {
        //set pathfinder to use
        switch (logic) {
            case DIJKSTRA:
                pfm.setPathfinder2use(DIJKSTRA);
                this.setConstants(0.1f, 100.0f, 0.4f, 0.5f);
                break;
            case A_STAR:
                pfm.setPathfinder2use(A_STAR);
                this.setConstants(0.1f, 100.0f, 0.4f, 0.5f);
                break;
            case BEST_FIRST_SEARCH:
                pfm.setPathfinder2use(BEST_FIRST_SEARCH);
                break;
            case FRINGE_SEARCH:
                pfm.setPathfinder2use(FRINGE_SEARCH);
                break;
            case TRACE:
                pfm.setPathfinder2use(TRACE);
                break;
            case BEST_NEIGHBOR_SEARCH:
                pfm.setPathfinder2use(BEST_NEIGHBOR_SEARCH);
                break;
            case TRIMMED_BEST_NEIGHBOR_SEARCH:
                pfm.setPathfinder2use(TRIMMED_BEST_NEIGHBOR_SEARCH);
                break;
            default:
                throw new AssertionError(logic.name());
        }
        pfm.setupPathLog(map, w, h);

        //log.info("Not using influence maps.\n");
        boolean usingInfluenceMaps = frameworkOptions.get("useInfluenceMaps");
        boolean usingPathFindersAdaptivityTest
                = frameworkOptions.get("usePathFindersAdaptivityTest");
        boolean usingInfluenceMapsAdaptivityTest
                = frameworkOptions.get("useInfluenceMapsAdaptivityTest");
        frameworkOptions.put("useInfluenceMaps", false);
        frameworkOptions.put("usePathFindersAdaptivityTest", false);
        frameworkOptions.put("useInfluenceMapsAdaptivityTest", false);

        Vector3D[] path = pfm.findPath();

        if (usingInfluenceMaps) {
            //log.info("Using influence maps.\n");
            frameworkOptions.put("useInfluenceMaps", true);

            boolean usingInfluenceMapsConstraintAwareNavigation
                    = frameworkOptions.get("useInfluenceMapsConstraintAwareNavigation");
            frameworkOptions.put("useInfluenceMapsConstraintAwareNavigation", false);

            //find paths without altering the influence map or the graph.
            this.Test(logic);

            frameworkOptions.put("useInfluenceMapsAdaptivityTest", usingInfluenceMapsAdaptivityTest);
            frameworkOptions.put("usePathFindersAdaptivityTest", usingPathFindersAdaptivityTest);

            //find paths altering the influence map or the graph.
            if (frameworkOptions.get("useInfluenceMapsRiskAdversePathfinding")) {
                frameworkOptions.put("useInfluenceMapsRiskAdversePathfinding", false);
                this.Test();
                frameworkOptions.put("useInfluenceMapsRiskAdversePathfinding", true);
            } else {
                this.Test();
            }

            //make sure the influence map is back to its former self.
            if (frameworkOptions.get("useInfluenceMapsAdaptivityTest")) {
                pfm.undoTestModifyInfluenceMap();
            }

            if (usingInfluenceMapsConstraintAwareNavigation) {
                boolean usingInfluenceMapsNegativeValuedAttractors
                        = frameworkOptions.get("useInfluenceMapsNegativeValuedAttractors");
                boolean usingInfluenceMapsRiskAdversePathfinding
                        = frameworkOptions.get("useInfluenceMapsRiskAdversePathfinding");

                frameworkOptions.put("usePathFindersAdaptivityTest", false);
                frameworkOptions.put("useInfluenceMapsAdaptivityTest", false);
                frameworkOptions.put("useInfluenceMapsConstraintAwareNavigation", true);
                frameworkOptions.put("useInfluenceMapsNegativeValuedAttractors", true);
                frameworkOptions.put("useInfluenceMapsRiskAdversePathfinding", false);
                if (!logic.equals(BEST_FIRST_SEARCH)) {
                    pfm.findPath();
                }
                frameworkOptions.put("useInfluenceMapsRiskAdversePathfinding",
                        usingInfluenceMapsRiskAdversePathfinding);
                frameworkOptions.put("useInfluenceMapsNegativeValuedAttractors",
                        usingInfluenceMapsNegativeValuedAttractors);
                //frameworkOptions.put("useInfluenceMapsConstraintAwareNavigation",
                //        usingInfluenceMapsConstraintAwareNavigation);
                frameworkOptions.put("useInfluenceMapsAdaptivityTest",
                        usingInfluenceMapsAdaptivityTest);
                frameworkOptions.put("usePathFindersAdaptivityTest",
                        usingPathFindersAdaptivityTest);
            }
        }
        return path;
    }

    private void setConstants(float alpha1, float alpha2, float k1, float k2) {
        if (frameworkOptions.get("useInfluenceMapsRiskAdversePathfinding")) {
            pfm.setAlpha1(alpha1);
            pfm.setAlpha2(alpha2);
        }
        if (frameworkOptions.get("useInfluenceMapsConstraintAwareNavigation")) {
            pfm.setK1(k1);
            pfm.setK2(k2);
        }
    }

    private void Test() {
        if (frameworkOptions.get("useInfluenceMapsAdaptivityTest")
                && frameworkOptions.get("usePathFindersAdaptivityTest")) {
            //we do first the graph changes test in order to use the 
            //unaltered influence map.
            frameworkOptions.put("useInfluenceMapsAdaptivityTest", false);
            pfm.findPath();
            frameworkOptions.put("useInfluenceMapsAdaptivityTest", true);

            frameworkOptions.put("usePathFindersAdaptivityTest", false);
            pfm.findPath();
            frameworkOptions.put("usePathFindersAdaptivityTest", true);
        }

        if ((frameworkOptions.get("useInfluenceMapsAdaptivityTest")
                && !frameworkOptions.get("usePathFindersAdaptivityTest"))
                || (!frameworkOptions.get("useInfluenceMapsAdaptivityTest")
                && frameworkOptions.get("usePathFindersAdaptivityTest"))) {
            pfm.findPath();
        }
    }

    private void Test(PathFinder logic) {
        if (frameworkOptions.get("useInfluenceMapsRiskAdversePathfinding")) {
            frameworkOptions.put("useInfluenceMapsRiskAdversePathfinding", false);
            pfm.findPath();
            frameworkOptions.put("useInfluenceMapsRiskAdversePathfinding", true);
        }
        if (!logic.equals(BEST_FIRST_SEARCH)) {
            boolean usingInfluenceMapsNegativeValuedAttractors
                    = frameworkOptions.get("useInfluenceMapsNegativeValuedAttractors");
            if (frameworkOptions.get("useInfluenceMapsRiskAdversePathfinding")) {
                frameworkOptions.put("useInfluenceMapsNegativeValuedAttractors", true);
            }
            pfm.findPath();
            if (frameworkOptions.get("useInfluenceMapsRiskAdversePathfinding")) {
                frameworkOptions.put("useInfluenceMapsNegativeValuedAttractors",
                        usingInfluenceMapsNegativeValuedAttractors);
            }
        }
    }

    private void Test(PathFinder logic, String map, int w, int h, float bound) {
        //set pathfinder to use
        switch (logic) {
            case STATIC_WEIGHTED_A_STAR:
                pfm.setPathfinder2use(STATIC_WEIGHTED_A_STAR);
                pfm.setBound(bound);
                break;
            case REVISED_DYNAMIC_WEIGHTED_A_STAR:
                pfm.setPathfinder2use(REVISED_DYNAMIC_WEIGHTED_A_STAR);
                pfm.setBound(bound);
                break;
            case ALPHA_STAR:
                pfm.setPathfinder2use(ALPHA_STAR);
                pfm.setBound(bound);
                break;
            case OPTIMISTIC_SEARCH:
                pfm.setPathfinder2use(OPTIMISTIC_SEARCH);
                pfm.setBound(bound);
                break;
            case SKEPTICAL_SEARCH:
                pfm.setPathfinder2use(SKEPTICAL_SEARCH);
                pfm.setBound(bound);
                break;
            default:
                throw new AssertionError(logic.name());
        }

        pfm.setupPathLog(map, w, h);
        pfm.findPath();
    }

    private void setupInfluenceMap(TransformGroup floor, PathFinder logic,
            String map, int w, int h, InfluenceMapGaussian im,
            float influenceAttractor, float influenceRepeller) {
        boolean usingPathFindersAdaptivityTest
                = frameworkOptions.get("usePathFindersAdaptivityTest");
        boolean usingInfluenceMapsAdaptivityTest
                = frameworkOptions.get("useInfluenceMapsAdaptivityTest");
        frameworkOptions.put("usePathFindersAdaptivityTest", false);
        frameworkOptions.put("useInfluenceMapsAdaptivityTest", false);

        pfm.setLowLevelGraph(
                ((HOG2MapMesh) floor.getMeshes().get(0)).getGraphMST());

        ArrayList<HashSet<Vector3D>> GraphDoorNodesSets
                = ((HOG2MapMesh) floor.getMeshes().get(0))
                        .getGraphDoorNodesSets();

        //log.info("Searching for a path in the MST.");
        //pfm.pathFindersDebugCSVLog();
        frameworkOptions.put("useInfluenceMaps", false);
        frameworkOptions.put("usePathFindersDebug", false);
        Vector3D[] path = this.Test(logic, map, w, h);
        frameworkOptions.put("usePathFindersDebug", true);
        frameworkOptions.put("useInfluenceMaps", true);

        HashSet<Vector3D> pathNodes = new HashSet<>();
        pathNodes.addAll(asList(path));

        //Place one attractors at each 50th path node.
        for (int i = 0; i < path.length; i++) {
            if (i % 50 == 0) {
                im.addPropagator(new Vector3D(
                        pfm.getGraphNodeGridCoords(path[i]).getX(),
                        0,
                        pfm.getGraphNodeGridCoords(path[i]).getY()),
                        influenceAttractor);
            }
        }

        //Place 20 attractors at the start and goal nodes.
        //for (int i = 0; i < 20; i++) {
        //    im.addPropagator(new Vector3D(
        //            pfm.getGraphNodeGridCoords(
        //                    pfm.getPrimaryAgent().getPosition()).getX(),
        //            0,
        //            pfm.getGraphNodeGridCoords(
        //                    pfm.getPrimaryAgent().getPosition()).getY()),
        //            influenceAttractor);
        //    im.addPropagator(new Vector3D(
        //            pfm.getGraphNodeGridCoords(
        //                    pfm.getPrimaryGoal().getPosition()).getX(),
        //            0,
        //            pfm.getGraphNodeGridCoords(
        //                    pfm.getPrimaryGoal().getPosition()).getY()),
        //            influenceAttractor);
        //}
        //
        //Place a repeller at each door node region 
        //not traversed by the MST path.
        for (HashSet<Vector3D> GraphDoorNodesSet : GraphDoorNodesSets) {

            boolean placeRepellers = true;
            for (Vector3D GraphDoorNode : GraphDoorNodesSet) {
                if (pathNodes.contains(GraphDoorNode)) {
                    placeRepellers = false;
                    break;
                }
            }
            if (placeRepellers) {
                GraphDoorNodesSet.stream().forEach(GraphDoorNode -> {
                    im.addPropagator(new Vector3D(
                            pfm.getGraphNodeGridCoords(GraphDoorNode).getX(),
                            0,
                            pfm.getGraphNodeGridCoords(GraphDoorNode).getY()),
                            influenceRepeller);
                });
            }
        }
        im.updateInfluenceMap(0);

        frameworkOptions.put("useInfluenceMapsAdaptivityTest", usingInfluenceMapsAdaptivityTest);
        frameworkOptions.put("usePathFindersAdaptivityTest", usingPathFindersAdaptivityTest);

        pfm.restoreGraph();
    }

    private void setupHog2mapsFileLog(String fileName, InfluenceMapGaussian im) {
        if (maxAttractorCount < im.getAttractorCount()) {
            maxAttractorCount = im.getAttractorCount();
            maxAttractorCountMap = fileName.replace(".map", "");
        }
        if (minAttractorCount > im.getAttractorCount()) {
            minAttractorCount = im.getAttractorCount();
            minAttractorCountMap = fileName.replace(".map", "");
        }

        if (maxRepellerCount < im.getRepellerCount()) {
            maxRepellerCount = im.getRepellerCount();
            maxRepellerCountMap = fileName.replace(".map", "");
        }
        if (minRepellerCount > im.getRepellerCount()) {
            minRepellerCount = im.getRepellerCount();
            minRepellerCountMap = fileName.replace(".map", "");
        }
    }
}
