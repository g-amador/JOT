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
package jot.math.geometry.generators.noise;

import static java.lang.Math.floor;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.Interpolation.lerp;

/**
 * Class that implements the improved noise generation algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Improved extends AbstractNoiseGenerator {

    static final Logger log = getLogger("ImprovedNoiseGenerator");

    // JAVA REFERENCE IMPLEMENTATION OF IMPROVED NOISE-COPYRIGHT 2002 KEN PERLIN.
    /**
     * Octaves are how many layers you are putting together. If you start with
     * big features, the number of octaves determines how detailed the map will
     * look.
     */
    public static int octaves = 16;

    /**
     * Gain Gain/persistence, is what makes the amplitude shrink (or not
     * shrink). Each octave the amplitude is multiplied by the gain. If it is
     * higher than 0.65 then the amplitude will barely shrink, and maps get
     * crazy. Too low and the details become miniscule, and the map looks washed
     * out. However, most use 1/lacunarity. Since the standard for lacunarity is
     * 2.0, the standard for the gain is 0.5. Noise that has a gain of 0.5 and a
     * lacunarity of 2.0 is referred to as 1/f noise, and is the industry
     * standard.
     */
    public static float gain = 0.75f;

    /**
     * Lacunarity is what makes the frequency grow. Each octave the frequency is
     * multiplied by the lacunarity. I use a lacunarity of 2.0, however values
     * of 1.8715 or 2.1042 can help to reduce artifacts in some algorithms. A
     * lacunarity of 2.0 means that the frequency doubles each octave, so if the
     * first octave had 3 points the second would have 6, then 12, then 24, etc.
     * This is used almost exclusively, partly because octaves in music double
     * in frequency. Other values are perfectly acceptable, but the results will
     * vary.
     */
    public static float lacunarity = 8.0f;

    static {
        log.setLevel(OFF);
    }

    static {
        for (int i = 0; i < 256; i++) {
            p[256 + i] = p[i] = permutation[i];
        }
    }

    /**
     * Improved noise COPYRIGHT 2002 KEN PERLIN.
     *
     * @param x
     * @param y
     * @param z
     * @return Improved noise.
     */
    public static double noise(double x, double y, double z) {
        int X = (int) floor(x) & 255, // FIND UNIT CUBE THAT
                Y = (int) floor(y) & 255, // CONTAINS POINT.
                Z = (int) floor(z) & 255;
        x -= floor(x); // FIND RELATIVE X,Y,Z
        y -= floor(y); // OF POINT IN CUBE.
        z -= floor(z);
        double u = fade(x), // COMPUTE FADE CURVES
                v = fade(y), // FOR EACH OF X,Y,Z.
                w = fade(z);
        int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z, // HASH COORDINATES
                // OF
                B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z; // THE 8 CUBE
        // CORNERS,

        return lerp(lerp(lerp(u, grad(p[AA], x, y, z), // AND ADD
                grad(p[BA], x - 1, y, z)), // BLENDED
                lerp(grad(p[AB], x, y - 1, z), // RESULTS
                        grad(p[BB], x - 1, y - 1, z), u), v),// FROM 8
                lerp(lerp(grad(p[AA + 1], x, y, z - 1), // CORNERS
                                grad(p[BA + 1], x - 1, y, z - 1), u), // OF CUBE
                        lerp(grad(p[AB + 1], x, y - 1, z - 1), grad(
                                        p[BB + 1], x - 1, y - 1, z - 1), u), v), w);
    }

    static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    static double grad(int hash, double x, double y, double z) {
        int h = hash & 15; // CONVERT LO 4 BITS OF HASH CODE
        double u = h < 8 ? x : y, // INTO 12 GRADIENT DIRECTIONS.
                v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    // END REFERENCE IMPLEMENTATION OF IMPROVED NOISE-COPYRIGHT 2002 KEN PERLIN.
    // -------------------------------------------------------------------------
    // BEGIN ADDED IMPLEMENTATION 
    @Override
    public double Noise(double x, double y, double z, double frequency) {
        return this.fBm(x, 0, z, frequency);
    }

    /**
     * A implementation of fractional Brownian motion. This implementation
     * assumes an integer number of octaves.
     *
     * @param x
     * @param y
     * @param z
     *
     * @param frequency
     * @return fractional Brownian motion.
     */
    private double fBm(double x, double y, double z, double frequency) {
        double answer = 0;
        double amplitude = gain;
        for (int i = 0; i < octaves; i++) {
            answer += amplitude * noise(x * frequency, y * frequency, z * frequency);

            frequency *= lacunarity;
            amplitude *= gain;

            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
        }
        return answer;
    }
}
