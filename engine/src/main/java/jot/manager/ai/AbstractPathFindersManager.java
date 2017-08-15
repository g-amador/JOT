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

import com.jogamp.opengl.GL2;
import java.util.ArrayList;
import java.util.HashMap;
import jot.math.graph.GraphBackup;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Abstract class that each path finder must implement. Alternatively one
 * massive class may be implemented instead.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractPathFindersManager extends AbstractAI {

    /**
     * This method gets a new hash map Graph.
     *
     * @return Graph, which corresponds to a hash map where each key is a Graph
     * node coordinates that is associated with a list of neighbor nodes.
     */
    public abstract HashMap<Vector3D, ArrayList<Vector3D>> getGraph();

    /**
     * This method sets a new array Graph.
     *
     * @param Graph, which corresponds to all lines formed by each pair of
     * nodes.
     */
    public abstract void setGraph(Vector3D[] Graph);

    /**
     * This method sets a new hash map Graph.
     *
     * @param Graph, which corresponds to a hash map where each key is a Graph
     * node coordinates that is associated with a list of neighbor nodes.
     */
    public abstract void setGraph(HashMap<Vector3D, ArrayList<Vector3D>> Graph);

    /**
     * This method sets a new low level array Graph.
     *
     * @param lowLevelGraph, which corresponds to all lines formed by each pair
     * of nodes.
     */
    public abstract void setLowLevelGraph(Vector3D[] lowLevelGraph);

    /**
     * This method sets a new low level hash map Graph.
     *
     * @param lowLevelGraph, which corresponds to a hash map where each key is a
     * Graph node coordinates that is associated with a list of neighbor nodes.
     */
    public abstract void setLowLevelGraph(
            HashMap<Vector3D, ArrayList<Vector3D>> lowLevelGraph);

    /**
     * Get the for a given 3D Graph node key its 2D grid coordinates.
     *
     * @param key a given 3D Graph node.
     *
     * @return for a given 3D Graph node key its 2D grid coordinates.
     */
    public abstract Vector2D getGraphNodeGridCoords(Vector3D key);

    /**
     * Set the hashMap that gets the 2D grid coordinates of a Graph node.
     *
     * @param GraphNodeGridCoords a hash map of Graph node 3D coordinates key,
     * 2D grid coordinates pairs.
     */
    public abstract void setGraphNodesGridCoords(
            HashMap<Vector3D, Vector2D> GraphNodeGridCoords);

    /**
     * Get the for a given Graph node 2D grid coordinates key its 3D
     * coordinates.
     *
     * @param key a given 2D grid coordinates Graph node.
     *
     * @return for a given Graph node 2D grid coordinates key its 3D
     * coordinates.
     */
    public abstract Vector2D getGridCoordsGraphNode(Vector3D key);

    /**
     * Set the hashMap that gets the 3D coordinates of a Graph node.
     *
     * @param GridCoordsGraphNodes a hash map of Graph node 2D grid coordinates
     * key, 3D coordinates pairs.
     */
    public abstract void setGridCoordsGraphNodes(
            HashMap<Vector2D, Vector3D> GridCoordsGraphNodes);

    /**
     * Get the closest graph node to a given position.
     *
     * @param position provided to identify the closest node.
     * @return the closest graph node to a given position.
     */
    public abstract Vector3D getClosestNode(Vector3D position);

    /**
     * This method sets the Graph render color.
     *
     * @param color the color to render the Graph lines formed by each pair of
     * nodes.
     */
    public abstract void setGraphColor(Vector3D color);

    /**
     * Chose/change the pathfinder logic to use.
     *
     * @param logic the pathfinder logic to use.
     */
    public abstract void setPathfinder2use(PathFinder logic);

    /**
     * This method returns a path if found using either a default or a provided
     * pathFinder logic.
     *
     * @return if a path is found returns, an array of Vector3D coordinates of
     * the points to go from an start to an target position. Otherwise returns
     * null.
     */
    public abstract Vector3D[] findPath();

    /**
     * This method outputs a CSV file first line.
     */
    public abstract void pathFindersDebugCSVLog();

    /**
     * This method setups the output for the CSV file.
     *
     * @param map the designation of the map.
     * @param w the width of the grid Graph.
     * @param h the height of the grid Graph.
     */
    public abstract void setupPathLog(String map, int w, int h);

    /**
     * This method removes a node and all its connections from the Graph.
     *
     * @param key a given 3D Graph node.
     * @return backup of the old Graph nodes grid coordinates, Graph hash map,
     * and Graph array.
     */
    public abstract GraphBackup removeNode(Vector3D key);

    /**
     * Draw the Graph that represent all the node connections. Note that a grid
     * is a particular type of Graph.
     *
     * @param gl
     */
    public abstract void renderGraph(GL2 gl);

    /**
     * Draw all the Graph nodes that were visited while using a pathfinder.
     *
     * @param gl
     */
    public abstract void renderVisited(GL2 gl);

    /**
     * This method resets the Graph to the default Graph, e.h., if the current
     * Graph is the low level Graph.
     */
    public abstract void restoreGraph();

    /**
     * This method replaces the current Graph by a provided backup.
     *
     * @param gb backup of the old Graph nodes grid coordinates, Graph hash map,
     * and Graph array.
     */
    public abstract void restoreGraph(GraphBackup gb);

    /**
     * Available pathfinding algorithms: DIJKSTRA, A_STAR, BEST_FIRST_SEARCH,
     * FRINGE_SEARCH.
     */
    public enum PathFinder {

        DIJKSTRA,
        A_STAR,
        BEST_FIRST_SEARCH,
        FRINGE_SEARCH,
    }
}
