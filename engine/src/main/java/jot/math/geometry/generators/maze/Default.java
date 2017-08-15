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

import static java.lang.Math.floor;
import static java.lang.Math.random;
import static java.lang.String.format;
import static java.lang.System.nanoTime;
import java.util.ArrayList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.ExtensionGeometryOptions.extensionGeometryOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements the default recursive maze generation algorithm.
 * Adapted from the source available at
 * https://sites.google.com/a/temple.edu/cis-4350-spring-2014/home
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Default extends AbstractMazeGenerator {

    static final Logger log = getLogger("DefaultMazeGenerator");

    static {
        log.setLevel(OFF);
    }

    private byte[][] theMaze; // the maze, containing chambers
    private long count;

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
    public Default(int rows, int columns, float size) {
        super(rows, columns, size);
        if (extensionGeometryOptions.get("useMazeGeneratorsDebug")) {
            log.setLevel(INFO);
        }
        this.Id = "DefaultMaze";
    }

    /**
     * Maze generator using the default recursive maze generation algorithm.
     */
    @Override
    public void generateMaze() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            this.start_time = nanoTime();

            ArrayList<Vector3D> maze = new ArrayList<>(); //a graph containing each connection between two maze cells.

            // initialize: all walls up, path=false => 0x0f
            this.theMaze = new byte[this.mazeLength][this.mazeWidth];
            for (int r = 0; r < this.mazeLength; r++) {
                for (int c = 0; c < this.mazeWidth; c++) {
                    this.theMaze[r][c] = 0x0f;
                }
            }

            // pick a start point
            int startR = (int) floor(random() * this.mazeLength);
            int startC = (int) floor(random() * this.mazeLength);
            this.count = 0;

            // and generate
            this.generateRec(startR, startC, 0);
            // set exit point to 0,0
            this.theMaze[0][0] |= 0x20;      // 0x20: exit
            this.theMaze[0][0] &= 0xFE;      // remove North Wall   

            //Convert to desired format
            int noRows = this.theMaze.length;
            int noColumns = this.theMaze[0].length;
            for (int r = 0; r < noRows; r++) {
                for (int c = 0; c < noColumns; c++) {
                    int w = this.theMaze[r][c];
                    float posX = c * this.cellLength - this.size / 2;
                    float posY = r * this.cellWidth - this.size / 2;

                    // the walls
                    if ((w & 1) == 1) {
                        maze.add(new Vector3D(posX, 0, posY));
                        maze.add(new Vector3D(posX + this.cellLength, 0, posY));
                    }
                    if ((w & 2) == 2) {
                        maze.add(new Vector3D(posX, 0, posY));
                        maze.add(new Vector3D(posX, 0, posY + this.cellWidth));
                    }
                    if ((w & 4) == 4) {
                        maze.add(new Vector3D(posX, 0, posY + this.cellWidth));
                        maze.add(new Vector3D(posX + this.cellLength, 0, posY + this.cellWidth));
                    }
                    if ((w & 8) == 8) {
                        maze.add(new Vector3D(posX + this.cellLength, 0, posY));
                        maze.add(new Vector3D(posX + this.cellLength, 0, posY + this.cellWidth));
                    }
                }
            }

            this.end_time = nanoTime() - this.start_time;
            log.info(format("Maze generation using default recursive algorithm took " + this.end_time + " (nanoseconds)"));
            this.graph = maze.toArray(new Vector3D[maze.size()]);
        } else {
            this.graph = null;
        }
    }

    // -------------------------
    // Recursive Maze Generation
    // -------------------------
    private void generateRec(int r, int c, int direction) {
        // tear down wall towards source direction
        switch (direction) {
            case 1:
                this.theMaze[r][c] -= 4;
                break;
            case 2:
                this.theMaze[r][c] -= 8;
                break;
            case 4:
                this.theMaze[r][c] -= 1;
                break;
            case 8:
                this.theMaze[r][c] -= 2;
                break;
        }
        this.count++; // another chamber processed.

        // where to go now ?
        int noRows = this.theMaze.length;
        int noColumns = this.theMaze[0].length;

        // base case 1: all chambers finished
        if (this.count == noRows * noColumns) {
            return;
        }

        // recursive case: while there are walkable directions: walk
        while (true) {
            // find walkable directions
            boolean dir1, dir2, dir4, dir8;
            dir1 = dir2 = dir4 = dir8 = false;
            if (r > 0 && (this.theMaze[r - 1][c] == 0x0f)) {
                dir1 = true;
            }
            if (c > 0 && (this.theMaze[r][c - 1] == 0x0f)) {
                dir2 = true;
            }
            if (r < noRows - 1 && (this.theMaze[r + 1][c] == 0x0f)) {
                dir4 = true;
            }
            if (c < noColumns - 1 && (this.theMaze[r][c + 1] == 0x0f)) {
                dir8 = true;
            }

            // base case 2: no walkable directions left
            if ((dir1 | dir2 | dir4 | dir8) == false) {
                break;
            }

            boolean picked = false;
            do {
                int d = (int) floor(random() * 4); // direction 0-3
                switch (d) {
                    case 0:
                        if (dir1) {
                            picked = true;
                            this.theMaze[r][c] -= 1;
                            this.generateRec(r - 1, c, 1);
                            dir1 = false;
                            break;
                        }
                    case 1:
                        if (dir2) {
                            picked = true;
                            this.theMaze[r][c] -= 2;
                            this.generateRec(r, c - 1, 2);
                            dir2 = false;
                            break;
                        }
                    case 2:
                        if (dir4) {
                            picked = true;
                            this.theMaze[r][c] -= 4;
                            this.generateRec(r + 1, c, 4);
                            dir4 = false;
                            break;
                        }
                    case 3:
                        if (dir8) {
                            picked = true;
                            this.theMaze[r][c] -= 8;
                            this.generateRec(r, c + 1, 8);
                            dir8 = false;
                            break;
                        }
                }
            } while (!picked);
        }
        // base case2n cont'd: no more walkable directions left
        // return;
    }
}
