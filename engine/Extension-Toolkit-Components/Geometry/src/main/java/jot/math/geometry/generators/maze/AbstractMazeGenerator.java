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

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import java.util.ArrayList;
import java.util.HashMap;
import jot.math.geometry.generators.AbstractGeometryGenerator;
import static jot.math.graph.Converters.arrayGraph2HashMapGraph;
import jot.physics.Material;
import static jot.util.ExtensionGeometryOptions.extensionGeometryOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Abstract class that each maze generator must extend.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractMazeGenerator extends AbstractGeometryGenerator {

    protected float size;
    protected int mazeWidth;
    protected int mazeLength;
    protected Vector3D mazeColor;
    protected float cellWidth;
    protected float cellLength;
    protected long start_time, end_time;
    protected Vector3D[] graph;

    /**
     * Constructor.
     *
     * @param rows the maze number of cells in the z axis (OpenGL coordinates)
     * direction.
     * @param columns the maze number of cells in the x axis (OpenGL
     * coordinates) direction.
     * @param size (considering a squared game world) of one of the game world
     * sides.
     */
    public AbstractMazeGenerator(int rows, int columns, float size) {
        this.size = size;
        this.mazeLength = rows;
        this.mazeWidth = columns;
        this.mazeColor = ZERO;
        this.cellLength = size / rows;
        this.cellWidth = size / columns;
        this.Id = "Maze";
        this.materials = new ArrayList<>();
        this.materials.add(new Material(this.Id));
        this.materials.get(0).setRenderable(true);
        extensionGeometryOptions.put("useMazeGenerators", true);
    }

    @Override
    public void generateGeometry() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            this.genList = true;
            this.generateMaze();
        }
    }

    /**
     * This method generates a maze.
     */
    public abstract void generateMaze();

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGeometryLength() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            return this.mazeLength;
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGeometryWidth() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            return this.mazeWidth;
        }
        return 0;
    }

    /**
     * This method returns the geometry generated with either a default or a
     * provided geometry generator logic.
     *
     * @return the last geometry generated with a specific generator logic.
     */
    public HashMap<Vector3D, ArrayList<Vector3D>> getMaze() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            return arrayGraph2HashMapGraph(this.graph);
        }
        return null;
    }

    /**
     * This method returns the maze both top most left most cell.
     *
     * @return the maze both top most left most cell.
     */
    public Vector3D getMazeMin() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            Vector3D mazeMin = new Vector3D(this.graph[0].toArray());
            for (Vector3D v : this.graph) {
                if (mazeMin.getX() > v.getX() || mazeMin.getY() > v.getY() || mazeMin.getZ() > v.getZ()) {
                    mazeMin = new Vector3D(v.toArray());
                }
            }
            return mazeMin;
        }
        return null;
    }

    /**
     * This method returns the maze both bottom most right most cell.
     *
     * @return the maze both bottom most right most cell.
     */
    public Vector3D getMazeMax() {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            Vector3D mazeMax = new Vector3D(this.graph[0].toArray());
            for (Vector3D v : this.graph) {
                if (mazeMax.getX() < v.getX() || mazeMax.getY() < v.getY() || mazeMax.getZ() < v.getZ()) {
                    mazeMax = new Vector3D(v.toArray());
                }
            }
            return mazeMax;
        }
        return null;
    }

    /**
     * This method changes the color of the maze, by default the maze color is
     * black.
     *
     * @param color of the maze.
     */
    public void setMazeColor(Vector3D color) {
        if (extensionGeometryOptions.get("useMazeGenerators")) {
            this.mazeColor = color;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        //TODO: render in 3D
        if (extensionGeometryOptions.get("useMazeGenerators")
                && this.materials.get(0).isRenderable()) {
            gl.glPushMatrix();
            {
                this.materials.get(0).applyMaterialProperties(gl);

                if (this.genList) {
                    gl.glDeleteLists(this.listMesh, 1);
                    this.listMesh = gl.glGenLists(1);
                    gl.glNewList(this.listMesh, GL_COMPILE);
                    {
                        gl.glBegin(GL_TRIANGLES);
                        {
                            for (int i = 0; i < this.graph.length; i += 2) {
                                gl.glColor3d(this.mazeColor.getX(), this.mazeColor.getY(), this.mazeColor.getZ());
                                //Triangle 1
                                gl.glVertex3d(this.graph[i].getX() - this.cellLength / 4, 0.0d, this.graph[i].getZ() - this.cellWidth / 4);
                                gl.glVertex3d(this.graph[i + 1].getX() + this.cellLength / 4, 0.0d, this.graph[i].getZ() - this.cellWidth / 4);
                                gl.glVertex3d(this.graph[i + 1].getX() + this.cellLength / 4, 0.0d, this.graph[i + 1].getZ() + this.cellWidth / 4);

                                //Triangle 2
                                gl.glVertex3d(this.graph[i].getX() - this.cellLength / 4, 0.0d, this.graph[i].getZ() - this.cellWidth / 4);
                                gl.glVertex3d(this.graph[i + 1].getX() + this.cellLength / 4, 0.0d, this.graph[i + 1].getZ() + this.cellWidth / 4);
                                gl.glVertex3d(this.graph[i].getX() - this.cellLength / 4, 0.0d, this.graph[i + 1].getZ() + this.cellWidth / 4);

                                //Triangle 3
                                gl.glVertex3d(this.graph[i + 1].getX() - this.cellLength / 4, 0.0d, this.graph[i + 1].getZ() - this.cellWidth / 4);
                                gl.glVertex3d(this.graph[i + 1].getX() - this.cellLength / 4, 0.0d, this.graph[i].getZ() + this.cellWidth / 4);
                                gl.glVertex3d(this.graph[i].getX() + this.cellLength / 4, 0.0d, this.graph[i].getZ() + this.cellWidth / 4);

                                //Triangle 4
                                gl.glVertex3d(this.graph[i + 1].getX() - this.cellLength / 4, 0.0d, this.graph[i + 1].getZ() - this.cellWidth / 4);
                                gl.glVertex3d(this.graph[i].getX() + this.cellLength / 4, 0.0d, this.graph[i].getZ() + this.cellWidth / 4);
                                gl.glVertex3d(this.graph[i].getX() + this.cellLength / 4, 0.0d, this.graph[i + 1].getZ() - this.cellWidth / 4);
                            }
                        }
                        gl.glEnd();
                    }
                    gl.glEndList();
                    this.genList = false;
                }

                gl.glDisable(GL_LIGHTING);
                gl.glCallList(this.listMesh);
                gl.glEnable(GL_LIGHTING);
            }
            gl.glPopMatrix();
        }
    }
}
