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
package jot.math.geometry.generators.terrain;

import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.GL2GL3.GL_FILL;
import static com.jogamp.opengl.GL2GL3.GL_LINE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.lang.Float.NaN;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import java.util.ArrayList;
import jot.math.geometry.generators.AbstractGeometryGenerator;
import jot.math.geometry.generators.smoother.AbstractSurface;
import jot.physics.Material;
import static jot.util.CoreOptions.coreOptions;
import static jot.util.ExtensionGeometryOptions.extensionGeometryOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that each terrain generator must extend.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractTerrainGenerator extends AbstractGeometryGenerator {

    /**
     * The dimensions of single cell width and height.
     */
    protected float cellSideSize;

    protected int dimension;
    protected float roughness;
    protected float terrainMin, terrainMax;

    /**
     * The geometry.
     */
    protected ArrayList<ArrayList<Vector3D>> Geometry;

    /**
     * Constructor, sets the application Window, and initializes the terrain
     * height map.
     *
     * @param lod the level of detail of the heightmap to generate. lod
     * corresponds to the integer variable k in the formula (2 ^ k) + 1, where k
     * = 1, 2, 3 ... is the value of both the length and width of the heightmap,
     * stored in the GeometryGen class "terrain" variable. The value of (2 ^ k)
     * + 1 gives the number of points per line of the grid. The higher k is the
     * more detailed is the generated heightmap, i.e., more points are
     * generated. Consequently, more details means more time to calculate the
     * heightmap values, and to render the generated terrain.
     * @param roughness the value that will increase or decrease the random
     * interval added to each new generated height.
     * @param size (considering a squared game world) of one of the game world
     * sides.
     */
    public AbstractTerrainGenerator(int lod, float roughness, float size) {
        this.dimension = (int) pow(2, lod) + 1;
        this.roughness = roughness;
        this.cellSideSize = size / this.dimension;
        this.Id = "Terrain";
        this.materials = new ArrayList<>();
        this.materials.add(new Material(this.Id));
        this.materials.get(0).setRenderable(true);
        extensionGeometryOptions.put("useTerrainGenerators", true);
    }

    /**
     * This method generates a terrain.
     *
     */
    public abstract void generateTerrain();

    @Override
    public void generateGeometry() {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            this.genList = true;
            this.resetGeometry();
            this.generateTerrain();
            this.wrappingGeometry();
        }
    }

    /**
     * Smooths the terrain to make it less irregular. Some terrain generators
     * require smoothing to clean some visual artifacts, i.e., spikes
     *
     * @param smoothFactor relates to the amount of extra points required to
     * generate for the surface smoothing.
     * @param smoother the specific smoother algorithm to use.
     */
    public void smoothTerrain(int smoothFactor, AbstractSurface smoother) {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            smoother.generateSurface(this.Geometry, smoothFactor);
            this.Geometry = smoother.getSurface();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGeometryLength() {
        return this.Geometry.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGeometryWidth() {
        return this.Geometry.get(0).size();
    }

    /**
     * Get an specific altitude stored in the height map with coordinates i and
     * j.
     *
     * @param i the coordinate that correspond to the x axis in OpenGL
     * coordinates.
     * @param j the coordinate that correspond to the z axis in OpenGL
     * coordinates.
     * @return the value of the altitude in coordinates i and j.
     */
    public float getAltitude(int i, int j) {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            return i < this.Geometry.size() && j < this.Geometry.get(i).size()
                    ? (float) this.Geometry.get(i).get(j).getY() : NaN;
        }
        return NaN;
    }

    /**
     * Get an specific smoothed/normalized altitude stored in the height map
     * with coordinates i and j.
     *
     * @param i the coordinate that correspond to the x axis in OpenGL
     * coordinates.
     * @param j the coordinate that correspond to the z axis in OpenGL
     * coordinates.
     * @return the smoothed value of the altitude in coordinates i and j.
     */
    public float getSmoothAltitude(int i, int j) {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            return i < this.Geometry.size() && j < this.Geometry.get(i).size()
                    ? (float) ((this.Geometry.get(i).get(j).getY() - this.terrainMin) / (this.terrainMax - this.terrainMin)) : NaN;
        }
        return NaN;
    }

    /**
     * Get an specific mean altitude stored in the height map with coordinates
     * between (i,j) and (i+1,j).
     *
     * @param i the coordinate that correspond to the x axis in OpenGL
     * coordinates.
     * @param j the coordinate that correspond to the z axis in OpenGL
     * coordinates.
     * @return the mean altitude stored in the height map with coordinates
     * between (i,j) and (i+1,j).
     */
    public float getMeanAltitude(float i, float j) {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            int iround = round(i);
            int jround = round(j);

            return iround < this.Geometry.size()
                    && jround < this.Geometry.get(iround).size()
                    && (int) i < (this.Geometry.size() - 1)
                    && (int) j < (this.Geometry.get((int) i).size() - 1)
                    ? (this.getAltitude((int) i, (int) j)
                    + this.getAltitude(iround, (int) j)
                    + this.getAltitude(iround, jround)) / 3
                    : NaN;
        }
        return NaN;
    }

    /**
     * Get the color of an specific smoothed altitude stored in the height map
     * with coordinates i and j.
     *
     * @param i the coordinate that correspond to the x axis in OpenGL
     * coordinates.
     * @param j the coordinate that correspond to the z axis in OpenGL
     * coordinates.
     * @return get the color of the smoothed value of the altitude in
     * coordinates i and j.
     */
    public Vector3D getColor(int i, int j) {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            Vector3D blue = new Vector3D(0, 0, 1);
            Vector3D green = new Vector3D(0, 1, 0);
            Vector3D white = new Vector3D(1, 1, 1);
            float a = this.getSmoothAltitude(i, j);
            if (extensionGeometryOptions.get("useTerrainGeneratorsWithMiddleMinimumHeight")) {
                white = white.subtract(green).scalarMultiply(a);
                green = green.add(white);
                return green;
            } else if (a < .5) {
                green = green.subtract(blue).scalarMultiply((a - 0.0f) / 0.5f);
                blue = blue.add(green);
                return blue;
            } else {
                white = white.subtract(green).scalarMultiply((a - 0.5f) / 0.5f);
                green = green.add(white);
                return green;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        if (extensionGeometryOptions.get("useTerrainGenerators")
                && this.materials.get(0).isRenderable()) {
            gl.glPushMatrix();
            {
                this.materials.get(0).applyMaterialProperties(gl);

                gl.glPolygonMode(GL_FRONT_AND_BACK,
                        coreOptions.get("showWireframe") ? GL_LINE : GL_FILL);

                if (coreOptions.get("showTextures")
                        && this.materials.get(0).getTexture() != null) {
                    this.materials.get(0).getTexture().bind(gl);
                    this.materials.get(0).getTexture().enable(gl);
                } else {
                    gl.glDisable(GL_LIGHTING);
                }

                if (this.genList) {
                    Vector3D color;
                    gl.glDeleteLists(this.listMesh, 1);
                    this.listMesh = gl.glGenLists(1);
                    gl.glNewList(this.listMesh, GL_COMPILE);
                    {
                        //TODO: alter to use multiple textures depending on the medium height of the 3 vertices.
                        gl.glBegin(GL_TRIANGLES);
                        {
                            for (int i = 0; i < (this.Geometry.size() - 1); i++) {
                                for (int j = 0; j < (this.Geometry.get(i).size() - 1); j++) {
                                    //Triangle 1
                                    if (coreOptions.get("showTextures")
                                            && this.materials.get(0).getTexture() != null) {
                                        gl.glTexCoord2d(
                                                (double) i / (this.Geometry.size() - 1),
                                                (double) j / (this.Geometry.get(i).size() - 1));
                                    } else {
                                        color = this.getColor(i, j);
                                        gl.glColor3d(color.getX(), color.getY(), color.getZ());
                                    }
                                    gl.glVertex3d(this.Geometry.get(i).get(j).getX(),
                                            this.Geometry.get(i).get(j).getY(),
                                            this.Geometry.get(i).get(j).getZ());

                                    if (coreOptions.get("showTextures")
                                            && this.materials.get(0).getTexture() != null) {
                                        gl.glTexCoord2d(
                                                (double) i / (this.Geometry.size() - 1),
                                                (double) (j + 1) / (this.Geometry.get(i).size() - 1));
                                    } else {
                                        color = this.getColor(i, j + 1);
                                        gl.glColor3d(color.getX(), color.getY(), color.getZ());
                                    }
                                    gl.glVertex3d(this.Geometry.get(i).get(j + 1).getX(),
                                            this.Geometry.get(i).get(j + 1).getY(),
                                            this.Geometry.get(i).get(j + 1).getZ());

                                    if (coreOptions.get("showTextures")
                                            && this.materials.get(0).getTexture() != null) {
                                        gl.glTexCoord2d(
                                                (double) (i + 1) / (this.Geometry.size() - 1),
                                                (double) (j + 1) / (this.Geometry.get(i).size() - 1));
                                    } else {
                                        color = this.getColor(i + 1, j + 1);
                                        gl.glColor3d(color.getX(), color.getY(), color.getZ());
                                    }
                                    gl.glVertex3d(this.Geometry.get(i + 1).get(j + 1).getX(),
                                            this.Geometry.get(i + 1).get(j + 1).getY(),
                                            this.Geometry.get(i + 1).get(j + 1).getZ());

                                    //Triangle 2
                                    if (coreOptions.get("showTextures")
                                            && this.materials.get(0).getTexture() != null) {
                                        gl.glTexCoord2d(
                                                (double) i / (this.Geometry.size() - 1),
                                                (double) j / (this.Geometry.get(i).size() - 1));
                                    } else {
                                        color = this.getColor(i, j);
                                        gl.glColor3d(color.getX(), color.getY(), color.getZ());
                                    }
                                    gl.glVertex3d(this.Geometry.get(i).get(j).getX(),
                                            this.Geometry.get(i).get(j).getY(),
                                            this.Geometry.get(i).get(j).getZ());

                                    if (coreOptions.get("showTextures")
                                            && this.materials.get(0).getTexture() != null) {
                                        gl.glTexCoord2d(
                                                (double) (i + 1) / (this.Geometry.size() - 1),
                                                (double) (j + 1) / (this.Geometry.get(i).size() - 1));
                                    } else {
                                        color = this.getColor(i + 1, j + 1);
                                        gl.glColor3d(color.getX(), color.getY(), color.getZ());
                                    }
                                    gl.glVertex3d(this.Geometry.get(i + 1).get(j + 1).getX(),
                                            this.Geometry.get(i + 1).get(j + 1).getY(),
                                            this.Geometry.get(i + 1).get(j + 1).getZ());

                                    if (coreOptions.get("showTextures")
                                            && this.materials.get(0).getTexture() != null) {
                                        gl.glTexCoord2d(
                                                (double) (i + 1) / (this.Geometry.size() - 1),
                                                (double) (j) / (this.Geometry.get(i).size() - 1));
                                    } else {
                                        color = this.getColor(i + 1, j);
                                        gl.glColor3d(color.getX(), color.getY(), color.getZ());
                                    }
                                    gl.glVertex3d(this.Geometry.get(i + 1).get(j).getX(),
                                            this.Geometry.get(i + 1).get(j).getY(),
                                            this.Geometry.get(i + 1).get(j).getZ());
                                }
                            }
                        }
                        gl.glEnd();
                    }
                    gl.glEndList();
                    this.genList = false;
                }

                gl.glCallList(this.listMesh);

                if (coreOptions.get("showTextures")
                        && this.materials.get(0).getTexture() != null) {
                    this.materials.get(0).getTexture().disable(gl);
                } else {
                    gl.glEnable(GL_LIGHTING);
                }
            }
            gl.glPopMatrix();
        }
    }

    /**
     * Set all values of the height map to zero.
     */
    protected void resetGeometry() {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            this.Geometry = new ArrayList<>();
            float x_pos = 0;
            for (int x = 0; x < this.dimension; x++, x_pos += this.cellSideSize) {
                ArrayList<Vector3D> line = new ArrayList<>(this.dimension);
                float z_pos = 0;
                for (int z = 0; z < this.dimension; z++, z_pos += this.cellSideSize) {
                    Vector3D v = new Vector3D(x_pos, 0, z_pos);
                    line.add(v);
                }
                this.Geometry.add(line);
            }
        }
    }

    /**
     * Recalculate max and min heights in order to further evaluate and
     * determine the color of each for each point depending on his height. use
     * Terrain Generators With Zero Minimum Height
     */
    protected void wrappingGeometry() {
        this.terrainMin = this.roughness / 2;
        this.terrainMax = -this.roughness / 2;

        //Get the minimum and maximum height.
        for (int i = 0; i < this.dimension; i++) {
            for (int j = 0; j < this.dimension; j++) {
                double height = this.Geometry.get(i).get(j).getY();
                if (height < this.terrainMin) {
                    this.terrainMin = (float) height;
                }
                if (height > this.terrainMax) {
                    this.terrainMax = (float) height;
                }
            }
        }
        this.terrainMin = (this.terrainMax + this.terrainMin) / 2;

        //Set to middle heights in the interval [terrainMin;Middle[.
        for (int i = 0; i < this.dimension; i++) {
            for (int j = 0; j < this.dimension; j++) {
                Vector3D vertex = this.Geometry.get(i).get(j);
                double height = vertex.getY();
                if (extensionGeometryOptions.get("useTerrainGeneratorsWithMiddleMinimumHeight")
                        && height < this.terrainMin) {
                    this.Geometry.get(i).set(j, new Vector3D(
                            vertex.getX(), this.terrainMin, vertex.getZ()));
                }
            }
        }

        //Add to every eight the minimum in the interval [terrainMin;Middle[.
        for (int i = 0; i < this.dimension; i++) {
            for (int j = 0; j < this.dimension; j++) {
                Vector3D vertex = this.Geometry.get(i).get(j);
                if (extensionGeometryOptions.get("useTerrainGeneratorsWithMiddleMinimumHeight")) {
                    this.Geometry.get(i).set(j, new Vector3D(
                            vertex.getX(), vertex.getY() - this.terrainMin, vertex.getZ()));
                }
            }
        }
    }
}
