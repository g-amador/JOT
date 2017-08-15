/*
 * This file is part of the JOT game engine geometry extension toolkit 
 * component.
 * Copyright(C) 2014 Gonçalo Amador & Abel Gomes
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
package jot.math.geometry.generators.maze;

import static java.lang.String.format;
import static java.lang.System.nanoTime;
import java.util.ArrayList;
import static java.util.Collections.shuffle;
import java.util.Stack;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.ExtensionGeometryOptions.extensionGeometryOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements Kruskal's maze generation algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Kruskal extends AbstractMazeGenerator {

    static final Logger log = getLogger("KruskalMazeGenerator");

    static {
        log.setLevel(OFF);
    }

    /**
     * Constructor.
     *
     * @param rows of the maze number of cells in the z axis (OpenGL
     * coordinates) direction.
     * @param columns of the maze number of cells in the x axis (OpenGL
     * coordinates) direction.
     * @param size (considering a squared game world) of one of the game world
     * sides.
     */
    public Kruskal(int rows, int columns, float size) {
        super(rows, columns, size);
        if (extensionGeometryOptions.get("useMazeGeneratorsDebug")) {
            log.setLevel(INFO);
        }
        this.Id = "KruskalMaze";
    }

    /**
     * Maze generator using Kruskal's randomized algorithm.
     */
    @Override
    public void generateMaze() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            this.start_time = nanoTime();

            ArrayList<Vector3D> maze = new ArrayList<>(); //a graph containing each connection between two maze cells.

            //1. Create a list of all walls, and create a set for each cell,
            //   each containing just that one cell.
            Stack<Vector3D> walls = new Stack<>();
            ArrayList<ArrayList<Vector3D>> cellsSets = new ArrayList<>();
            for (int i = 0; i < this.mazeLength; i++) {
                for (int j = 0; j < this.mazeWidth; j++) {
                    //north and east walls of cell i;
                    if (i > 0) {
                        walls.add(new Vector3D(i, 'N', j));
                    }
                    if (j > 0) {
                        walls.add(new Vector3D(i, 'E', j));
                    }
                    ArrayList<Vector3D> cellSet = new ArrayList<>();
                    cellSet.add(new Vector3D(i, 0, j));
                    cellsSets.add(cellSet);
                }
            }
            shuffle(walls); //shuffle walls

            //2. For each wall, in some random order:   
            for (Vector3D wall : walls) {
                int set1Index = -1;
                int set2Index = -1;
                ArrayList<Vector3D> wallCells = this.wall2Edge(wall);

                for (int i = 0; i < cellsSets.size(); i++) {
                    ArrayList<Vector3D> cellSet = cellsSets.get(i);

                    if (cellSet.containsAll(wallCells)) {
                        break;
                    }

                    if (cellSet.contains(wallCells.get(0))) {
                        set1Index = i;
                    }

                    if (cellSet.contains(wallCells.get(1))) {
                        set2Index = i;
                    }

                    //2.1. If the cells divided by this 
                    //     wall belong to distinct sets: 
                    if (set1Index > -1 && set2Index > -1) {
                        //2.1.1. Remove the current wall.
                        //walls.remove(wall);
                        maze.add(new Vector3D(
                                wallCells.get(0).getX() * this.cellLength - this.size / 2,
                                0,
                                wallCells.get(0).getZ() * this.cellWidth - this.size / 2));
                        maze.add(new Vector3D(
                                wallCells.get(1).getX() * this.cellLength - this.size / 2,
                                0,
                                wallCells.get(1).getZ() * this.cellWidth - this.size / 2));

                        //2.1.2. Join the sets of the formerly divided cells.
                        cellSet = cellsSets.get(set1Index);
                        cellSet.addAll(cellsSets.get(set2Index));
                        cellsSets.set(set1Index, cellSet);
                        cellsSets.remove(set2Index);
                        break;
                    }
                }
            }

            this.end_time = nanoTime() - this.start_time;
            log.info(format("Maze generation using Kruskal's algorithm took " + this.end_time + " (nanoseconds)"));
            this.graph = maze.toArray(new Vector3D[maze.size()]);
        } else {
            this.graph = null;
        }
    }

    //Convert a wall into an edge containing the two cells divided by this wall.
    private ArrayList<Vector3D> wall2Edge(Vector3D wall) {
        ArrayList<Vector3D> cells = new ArrayList<>();
        if (wall.getY() == 'N') {
            //North
            cells.add(new Vector3D(wall.getX(), 0, wall.getZ()));
            cells.add(new Vector3D(wall.getX() - 1, 0, wall.getZ()));
        } else {
            //East
            cells.add(new Vector3D(wall.getX(), 0, wall.getZ()));
            cells.add(new Vector3D(wall.getX(), 0, wall.getZ() - 1));
        }
        return cells;
    }
}
