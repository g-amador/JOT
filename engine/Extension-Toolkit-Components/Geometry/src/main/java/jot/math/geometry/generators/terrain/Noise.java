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
import jot.math.geometry.generators.noise.AbstractNoiseGenerator;
import static jot.util.ExtensionGeometryOptions.extensionGeometryOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements noise based terrain generating algorithms.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Noise extends AbstractTerrainGenerator {

    static final Logger log = getLogger("NoiseTerrainGenerator");

    static {
        log.setLevel(OFF);
    }

    /**
     * The noise generator to use.
     */
    private final AbstractNoiseGenerator noiseGenerator;

    /**
     * Constructor, sets the application Window, and initializes the terrain
     * height map. Used by noise generators.
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
     * @param noiseGenerator to use.
     */
    public Noise(int lod, float roughness, float size, AbstractNoiseGenerator noiseGenerator) {
        super(lod, roughness, size);
        this.noiseGenerator = noiseGenerator;
        this.Id = "NoiseTerrain";
    }

    @Override
    public void generateGeometry() {
        super.generateGeometry();
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            this.noiseGenerator.reinitialize();
        }
    }

    /**
     * Geometry generation using a noise algorithms.
     *
     */
    @Override
    public void generateTerrain() {
        if (extensionGeometryOptions.get("useTerrainGenerators")) {
            float frequency = 1.0f / this.dimension;

            float x_pos = 0;
            for (int x = 0; x < this.dimension; x++, x_pos += this.cellSideSize) {
                float z_pos = 0;
                for (int z = 0; z < this.dimension; z++, z_pos += this.cellSideSize) {
                    float noise = (float) this.noiseGenerator.Noise(x_pos, 0, z_pos, frequency);
                    this.Geometry.get(x).set(z, new Vector3D(x_pos, noise * this.noiseGenerator.factor, z_pos));
                }
            }
        }
    }
}
