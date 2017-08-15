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

import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class that implements a Heightmap from a texture file terrain generating
 * algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Heightmap {

    //TODO: Implement me!!!!!!
    static final Logger log = getLogger("Heightmap");

    static {
        log.setLevel(OFF);
    }
//
//    /**
//     * Constructor, sets the application Window, and initializes the terrain
//     * height map. Used for diamond square.
//     *
//     * @param lod the level of detail of the heightmap to generate. lod
//     * corresponds to the integer variable k in the formula (2 ^ k) + 1, where k
//     * = 1, 2, 3 ... is the value of both the length and width of the heightmap,
//     * stored in the GeometryGen class "terrain" variable. The value of (2 ^ k)
//     * + 1 gives the number of points per line of the grid. The higher k is the
//     * more detailed is the generated heightmap, i.e., more points are
//     * generated. Consequently, more details means more time to calculate the
//     * heightmap values, and to render the generated terrain.
//     * @param roughness the value that will increase or decrease the random
//     * interval added to each new generated height.
//     * @param size (considering a squared game world) of one of the game world
//     * sides.
//     */
//    public DiamondSquare(int lod, float roughness, float size) {
//    Id = "DiamondSquareTerrain";
//        this.dimension = (int) pow(2, lod) + 1;
//        this.roughness = roughness;
//        cellSideSize = size / dimension;
//        materials = new ArrayList<>();
//        materials.add(new Material());
//        materials.get(0).setRenderable(true);
//        extensionGeometryOptions.put("useTerrainGenerators", true);
//    }
//
//    /**
//     * Geometry generation using the diamond square algorithm.
//     */
//    @Override
//    public void generateTerrain() {
//        if (extensionGeometryOptions.get("useTerrainGenerators")) {
//            //TODO: align with center of the scene.
//            float rn = roughness;
//            int size = dimension;
//
//            for (int length = dimension - 1; length >= 2; length /= 2) {
//                int half = length / 2;
//                rn /= 2;
//
//                // generate the new square values
//                for (int x = 0; x < dimension - 1; x += length) {
//                    for (int z = 0; z < dimension - 1; z += length) {
//                        float average = (float) (Geometry.get(x).get(z).getY() + // top left
//                                Geometry.get(x + length).get(z).getY() + // top right
//                                Geometry.get(x).get(z + length).getY() + // lower left
//                                Geometry.get(x + length).get(z + length).getY()); // lower right
//                        average /= 4;
//                        //average += (rn * random());
//                        average += 2 * rn * random() - rn;
//                        Vector3D v = Geometry.get(x + half).get(z + half);
//                        Geometry.get(x + half).set(z + half, new Vector3D(
//                                v.getX(), average, v.getZ()));
//                    }
//                }
//
//                // generate the new diamond values
//                for (int x = 0; x < dimension - 1; x += half) {
//                    for (int z = (x + half) % length; z < dimension - 1; z += length) {
//                        float average = (float) (Geometry.get((x - half + size) % size).get(z).getY() + // middle left
//                                Geometry.get((x + half) % size).get(z).getY() + // middle right
//                                Geometry.get(x).get((z + half) % size).getY() + // middle top
//                                Geometry.get(x).get((z - half + size) % size).getY()); // middle bottom
//
//                        average /= 4;
//                        //average += (rn * random());
//                        average += 2 * rn * random() - rn;
//                        Vector3D v = Geometry.get(x).get(z);
//                        Geometry.get(x).set(z, new Vector3D(
//                                v.getX(), average, v.getZ()));
//
//                        // values on the top and right edges
//                        if (x == 0) {
//                            v = Geometry.get(dimension - 1).get(z);
//                            Geometry.get(dimension - 1).set(z, new Vector3D(
//                                    v.getX(), average, v.getZ()));
//                        }
//                        if (z == 0) {
//                            v = Geometry.get(x).get(dimension - 1);
//                            Geometry.get(x).set(dimension - 1, new Vector3D(
//                                    v.getX(), average, v.getZ()));
//                        }
//                    }
//                }
//            }
//        }
//    }
}
