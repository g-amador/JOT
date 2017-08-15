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
package jot.math.graph;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Class that implements a graph backup for usage in path finders.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GraphBackup {

    public HashMap<Vector3D, Vector2D> GraphNodesGridCoords = new HashMap<>();
    public HashMap<Vector2D, Vector3D> GridCoordsGraphNodess = new HashMap<>();
    public HashMap<Vector3D, ArrayList<Vector3D>> Graph = new HashMap<>();

    /**
     * Constructor.
     *
     * @param Graph the graph in a hash map with key pairs of the type
     * {@literal <}Node position, List of node neighbors{@literal >}.
     * @param GraphNodesGridCoords the 2D grid coordinates to map between a grid
     * and a node position.
     * @param GridCoordsGraphNodess the 3D coordinates to map between a node
     * position and a 2D a grid.
     */
    public GraphBackup(
            HashMap<Vector3D, ArrayList<Vector3D>> Graph,
            HashMap<Vector3D, Vector2D> GraphNodesGridCoords,
            HashMap<Vector2D, Vector3D> GridCoordsGraphNodess) {
        if (Graph != null) {
            this.Graph.putAll(Graph);
        }
        if (GraphNodesGridCoords != null) {
            this.GraphNodesGridCoords.putAll(GraphNodesGridCoords);
        }
        if (GridCoordsGraphNodess != null) {
            this.GridCoordsGraphNodess.putAll(GridCoordsGraphNodess);
        }
    }
}
