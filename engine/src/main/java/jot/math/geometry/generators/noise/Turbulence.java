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

import static java.lang.Math.abs;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.geometry.generators.noise.Improved.lacunarity;
import static jot.math.geometry.generators.noise.Improved.noise;
import static jot.math.geometry.generators.noise.Improved.octaves;

/**
 * Class that implements the turbulence noise generation algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Turbulence extends AbstractNoiseGenerator {

    static final Logger log = getLogger("TurbulenceNoiseGenerator");

    static {
        log.setLevel(OFF);
    }

    /**
     * A simple implementation of turbulence. This implementation assumes an
     * integer number of octaves.
     *
     * @param x
     * @param y
     * @param z
     * @return turbulence.
     */
    public static double turbulence(double x, double y, double z) {
        int frequency = 1;
        double t = 0;
        for (int octave = 0; octave < octaves; octave++) {
            t += abs(noise(x, y, z)) / frequency;
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            frequency <<= 1;
        }
        return t;
    }

    @Override
    public double Noise(double x, double y, double z, double frequency) {
        return turbulence(x, 0, z);
    }
}
