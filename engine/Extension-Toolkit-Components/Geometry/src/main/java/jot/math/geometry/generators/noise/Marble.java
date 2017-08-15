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

import static java.lang.Math.sin;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.geometry.generators.noise.Turbulence.turbulence;

/**
 * Class that implements the marble noise generation algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Marble extends AbstractNoiseGenerator {

    static final Logger log = getLogger("MarbleNoiseGenerator");

    static {
        log.setLevel(OFF);
    }

    /**
     *
     * Based on the classic (circa 1985) Perlin implementation for generating a
     * marble pattern.
     *
     * @param x
     * @param y
     * @param z
     * @return marble pattern.
     */
    public static double marble(double x, double y, double z) {
        double t = turbulence(x, y, z);
        double marble = sin(y + t);
        return marble;
    }

    @Override
    public double Noise(double x, double y, double z, double frequency) {
        return marble(x, y, z);
    }
}
