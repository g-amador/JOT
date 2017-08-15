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
 * Class that implements Prims's maze generation algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Prim extends AbstractMazeGenerator {

    static final Logger log = getLogger("PrimMazeGenerator");

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
    public Prim(int rows, int columns, float size) {
        super(rows, columns, size);
        if (extensionGeometryOptions.get("useMazeGeneratorsDebug")) {
            log.setLevel(INFO);
        }
        this.Id = "PrimMaze";
    }

    /**
     * Maze generator using Prim's randomized algorithm.
     */
    @Override
    public void generateMaze() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            this.start_time = nanoTime();

            ArrayList<Vector3D> maze = new ArrayList<>(); //a graph containing each connection between two maze cells.
            ArrayList<Vector3D> inCells = new ArrayList<>();
            ArrayList<Vector3D> frontierCells = new ArrayList<>();

            Vector3D startCell = new Vector3D((int) (random() * (this.mazeWidth - 1)), 0, (int) (random() * (this.mazeLength - 1)));
            Vector3D currentFrontierCell;
            Vector3D currentInteriorCell;
            inCells.add(startCell);
            frontierCells.addAll(this.getFrontiersAround(inCells, startCell));
            while (!frontierCells.isEmpty()) {
                currentFrontierCell = frontierCells.get((int) (random() * (frontierCells.size() - 1)));
                currentInteriorCell = this.getRandomInCellAround(inCells, currentFrontierCell);
                inCells.add(currentFrontierCell);
                frontierCells.addAll(this.getFrontiersAround(inCells, currentFrontierCell));
                frontierCells.remove(currentFrontierCell);
                maze.add(new Vector3D(currentInteriorCell.getX() * this.cellLength - this.size / 2, 0, currentInteriorCell.getZ() * this.cellWidth - this.size / 2));
                maze.add(new Vector3D(currentFrontierCell.getX() * this.cellLength - this.size / 2, 0, currentFrontierCell.getZ() * this.cellWidth - this.size / 2));
            }

            this.end_time = nanoTime() - this.start_time;

            log.info(format("Maze generation using Prim's algorithm took " + this.end_time + " (nanoseconds)"));
            this.graph = maze.toArray(new Vector3D[maze.size()]);
        } else {
            this.graph = null;
        }
    }

    /**
     * This method returns for a given cell all its neighbors that are frontier
     * cells and not part of the interior of the maze.
     *
     * @param inCells cells of the interior of the maze, i.e., cells where its
     * possible to travel.
     * @param cell a given cell whose frontier neighbors (if any) will be
     * returned.
     * @return all the given cell neighbors that are frontier cells and not part
     * of the interior of the maze.
     */
    private ArrayList<Vector3D> getFrontiersAround(ArrayList<Vector3D> inCells, Vector3D cell) {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            ArrayList<Vector3D> frontiers = new ArrayList<>();

            if (cell.getX() != 0 && !inCells.contains(new Vector3D(cell.getX() - 1, 0, cell.getZ()))) {
                //log.info("frontier 1");
                frontiers.add(new Vector3D(cell.getX() - 1, 0, cell.getZ()));
            }
            if (cell.getZ() != 0 && !inCells.contains(new Vector3D(cell.getX(), 0, cell.getZ() - 1))) {
                //log.info("frontier 2");
                frontiers.add(new Vector3D(cell.getX(), 0, cell.getZ() - 1));
            }
            if (cell.getX() != (this.mazeLength - 1) && !inCells.contains(new Vector3D(cell.getX() + 1, 0, cell.getZ()))) {
                //log.info("frontier 3");
                frontiers.add(new Vector3D(cell.getX() + 1, 0, cell.getZ()));
            }
            if (cell.getZ() != (this.mazeWidth - 1) && !inCells.contains(new Vector3D(cell.getX(), 0, cell.getZ() + 1))) {
                //log.info("frontier 4");
                frontiers.add(new Vector3D(cell.getX(), 0, cell.getZ() + 1));
            }
            return frontiers;
        }
        return null;
    }

    /**
     * This method returns for a given cell one of its neighbors that is
     * interior cell chosen at random.
     *
     * @param inCells cells of the interior of the maze, i.e., cells where its
     * possible to travel.
     * @param cell a given cell whose one of it neighbor interior cells (if any)
     * will be returned.
     * @return one of the given cell neighbors that is interior cell.
     */
    private Vector3D getRandomInCellAround(ArrayList<Vector3D> inCells, Vector3D cell) {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            ArrayList<Vector3D> inCellsAround = new ArrayList<>();

            if (cell.getX() != 0 && inCells.contains(new Vector3D(cell.getX() - 1, 0, cell.getZ()))) {
                inCellsAround.add(new Vector3D(cell.getX() - 1, 0, cell.getZ()));
            }
            if (cell.getZ() != 0 && inCells.contains(new Vector3D(cell.getX(), 0, cell.getZ() - 1))) {
                inCellsAround.add(new Vector3D(cell.getX(), 0, cell.getZ() - 1));
            }
            if (cell.getX() != (this.mazeLength - 1) && inCells.contains(new Vector3D(cell.getX() + 1, 0, cell.getZ()))) {
                inCellsAround.add(new Vector3D(cell.getX() + 1, 0, cell.getZ()));
            }
            if (cell.getZ() != (this.mazeWidth - 1) && inCells.contains(new Vector3D(cell.getX(), 0, cell.getZ() + 1))) {
                inCellsAround.add(new Vector3D(cell.getX(), 0, cell.getZ() + 1));
            }
            return inCellsAround.get((int) (random() * (inCellsAround.size() - 1)));
        }
        return null;
    }
}
