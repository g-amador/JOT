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

import static java.lang.Math.random;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.ExtensionGeometryOptions.extensionGeometryOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements the diamond square terrain generating algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class DiamondSquare extends AbstractTerrainGenerator {

    static final Logger log = getLogger("DiamondSquareTerrainGenerator");

    static {
        log.setLevel(OFF);
    }

    /**
     * {@inheritDoc}
     */
    public DiamondSquare(int lod, float roughness, float size) {
        super(lod, roughness, size);
        this.Id = "DiamondSquareTerrain";
    }

    /**
     * Geometry generation using the diamond square algorithm.
     */
    @Override
    public void generateTerrain() {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            //TODO: align with center of the scene.
            float rn = this.roughness;
            int size = this.dimension;

            for (int length = this.dimension - 1; length >= 2; length /= 2) {
                int half = length / 2;
                rn /= 2;

                // generate the new square values
                for (int x = 0; x < this.dimension - 1; x += length) {
                    for (int z = 0; z < this.dimension - 1; z += length) {
                        float average = (float) (this.Geometry.get(x).get(z).getY()
                                + // top left
                                this.Geometry.get(x + length).get(z).getY()
                                + // top right
                                this.Geometry.get(x).get(z + length).getY()
                                + // lower left
                                this.Geometry.get(x + length).get(z + length).getY()); // lower right
                        average /= 4;
                        //average += (rn * random());
                        average += 2 * rn * random() - rn;
                        Vector3D v = this.Geometry.get(x + half).get(z + half);
                        this.Geometry.get(x + half).set(z + half, new Vector3D(
                                v.getX(), average, v.getZ()));
                    }
                }

                // generate the new diamond values
                for (int x = 0; x < this.dimension - 1; x += half) {
                    for (int z = (x + half) % length; z < this.dimension - 1; z += length) {
                        float average = (float) (this.Geometry.get((x - half + size) % size).get(z).getY()
                                + // middle left
                                this.Geometry.get((x + half) % size).get(z).getY()
                                + // middle right
                                this.Geometry.get(x).get((z + half) % size).getY()
                                + // middle top
                                this.Geometry.get(x).get((z - half + size) % size).getY()); // middle bottom

                        average /= 4;
                        //average += (rn * random());
                        average += 2 * rn * random() - rn;
                        Vector3D v = this.Geometry.get(x).get(z);
                        this.Geometry.get(x).set(z, new Vector3D(
                                v.getX(), average, v.getZ()));

                        // values on the top and right edges
                        if (x == 0) {
                            v = this.Geometry.get(this.dimension - 1).get(z);
                            this.Geometry.get(this.dimension - 1).set(z, new Vector3D(
                                    v.getX(), average, v.getZ()));
                        }
                        if (z == 0) {
                            v = this.Geometry.get(x).get(this.dimension - 1);
                            this.Geometry.get(x).set(this.dimension - 1, new Vector3D(
                                    v.getX(), average, v.getZ()));
                        }
                    }
                }
            }
        }
    }
}
