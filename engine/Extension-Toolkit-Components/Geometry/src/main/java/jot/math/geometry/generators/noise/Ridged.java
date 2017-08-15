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
import static jot.math.geometry.generators.noise.Improved.gain;
import static jot.math.geometry.generators.noise.Improved.lacunarity;
import static jot.math.geometry.generators.noise.Improved.noise;
import static jot.math.geometry.generators.noise.Improved.octaves;

/**
 * Class that implements the ridged noise generation algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Ridged extends AbstractNoiseGenerator {

    static final Logger log = getLogger("RidgedNoiseGenerator");

    static {
        log.setLevel(OFF);
    }

    /**
     * A simple implementation of a ridged multifractal noise function. This
     * implementation assumes an integer number of octaves and an H factor of
     * 1.0. This simplification is done for speed.
     *
     * @param x
     * @param y
     * @param z
     * @param offset
     * @return ridged multifractal noise.
     */
    public static double ridged(double x, double y, double z, double offset) {
        double answer = 0;
        double signal;
        double weight = 1.0;
        int frequency = 1;

        for (int octave = 0; octave < octaves; octave++) {
            signal = offset - abs(noise(x, y, z));
            // Square the signal to sharpen the ridges
            signal *= signal * weight;
            answer += signal / frequency;
            // weight successive contributions by previous signal
            weight = signal * gain;
            // Clamp the weight 0..1
            if (weight > 1.0) {
                weight = 1.0;
            }
            if (weight < 0.0) {
                weight = 0.0;
            }
            x *= lacunarity;
            y *= lacunarity;
            z *= lacunarity;
            frequency <<= 1;
        }

        return answer;
    }

    @Override
    public double Noise(double x, double y, double z, double frequency) {
        return ridged(x, y, z, 1);
    }
}
