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
package jot.demo.test;

import java.io.File;
import static java.lang.Long.MAX_VALUE;
import static java.lang.String.format;
import java.util.LinkedList;
import static java.util.logging.Level.INFO;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.demo.util.GameConstants.setGameConstants;
import jot.io.data.format.HOG2Map.HOG2MapMesh;
import jot.io.data.format.UnzipUtility;
import static jot.manager.SceneManager.localAssetManager;
import jot.manager.ai.AbstractPathFindersManager.PathFinder;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.A_STAR;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.BEST_FIRST_SEARCH;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.DIJKSTRA;
import static jot.manager.ai.AbstractPathFindersManager.PathFinder.FRINGE_SEARCH;
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
                    Tests t = new Tests(fileEntry.getPath().replace("../", ""), 25);
                }
            }
            uu.unzip("dist/assets/HOG2/WarcraftIII/map_tests.zip", "dist/assets/HOG2/WarcraftIII/");
            File folder2 = new File("dist/assets/HOG2/WarcraftIII/map_tests");
            for (final File fileEntry : folder2.listFiles()) {
                if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".map")) {
                    //&& fileEntry.getName().contains("divideandconquer")) {
                    //log.info(format("----------Starting test batterry for %s----------", fileEntry.getName()));
                    Tests t = new Tests(fileEntry.getPath().replace("../", ""), 25);
                }
            }
        }

        if (frameworkOptions.get("useMazeGenerators")) {
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

        Vector3D leftmostTopmostNode = ((HOG2MapMesh) floor.getMeshes().get(0)).getLeftmostTopmostNode();
        Vector3D rightmostBottommostNode = ((HOG2MapMesh) floor.getMeshes().get(0)).getRightmostBottommostNode();

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

        pfm.pathFindersDebugCSVLog();

        for (int i = 0; i < iters; i++) {
            this.Test(DIJKSTRA, map, w, h);
            this.Test(A_STAR, map, w, h);
            this.Test(BEST_FIRST_SEARCH, map, w, h);
            this.Test(FRINGE_SEARCH, map, w, h);

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
            log.info("");
        }
        //log.info("");
    }

    private Vector3D[] Test(PathFinder logic, String map, int w, int h) {
        //set pathfinder to use
        switch (logic) {
            case DIJKSTRA:
                pfm.setPathfinder2use(DIJKSTRA);
                break;
            case A_STAR:
                pfm.setPathfinder2use(A_STAR);
                break;
            case BEST_FIRST_SEARCH:
                pfm.setPathfinder2use(BEST_FIRST_SEARCH);
                break;
            case FRINGE_SEARCH:
                pfm.setPathfinder2use(FRINGE_SEARCH);
                break;
            default:
                throw new AssertionError(logic.name());
        }
        pfm.setupPathLog(map, w, h);

        Vector3D[] path = pfm.findPath();

        return path;
    }
}
