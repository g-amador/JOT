/*
 * This file is part of the JOT game engine core toolkit component.
 * Copyright (C) 2014 Gonçalo Amador & Abel Gomes
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
package jot.math;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements numerical interpolation algorithms.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Interpolation {

    static final Logger log = getLogger("Interpolation");

    static {
        log.setLevel(OFF);
    }

    /**
     * Single precision linear interpolate between a and b for x ranging from 0
     * to 1.
     *
     * @param a
     * @param b
     * @param x
     * @return single precision linear interpolation between a and b for x
     * ranging from 0 to 1.
     */
    public static float lerp(
            float a, float b,
            float x) {
        // Imprecise method which does not guarantee a  = b when x = 1,
        // due to floating-point arithmetic error.
        // return a + x * (b - a);

        // Precise method which guarantees a = b when x = 1.
        return (1 - x) * a + x * b;
    }

    /**
     * Double precision linear interpolate between a and b for x ranging from 0
     * to 1.
     *
     * @param a
     * @param b
     * @param x
     * @return double precision linear interpolation between a and b for x
     * ranging from 0 to 1.
     */
    public static double lerp(
            double a, double b,
            double x) {
        // Imprecise method which does not guarantee a  = b when x = 1,
        // due to floating-point arithmetic error.
        // return a + x * (b - a);

        // Precise method which guarantees a = b when x = 1.
        return (1 - x) * a + x * b;
    }

    /**
     * Double precision linear interpolate between each value of vectors a and b
     * for x ranging from 0 to 1.
     *
     * @param a
     * @param b
     * @param x
     * @return double precision linear interpolation between each value of
     * vectors a and b for x ranging from 0 to 1.
     */
    public static Vector3D lerp(
            Vector3D a, Vector3D b,
            double x) {

        return new Vector3D(
                (1 - x) * a.getX() + x * b.getX(),
                (1 - x) * a.getY() + x * b.getY(),
                (1 - x) * a.getZ() + x * b.getZ());
    }

    /**
     * Single precision cubic interpolate using samples a through d for x
     * ranging from 0 to 1. A Catmull-Rom spline is used. Over- and undershoots
     * are clamped to prevent blow-up.
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @param x
     * @return single precision cubic interpolation using samples a through d
     * for x ranging from 0 to 1. A Catmull-Rom spline is used. Over- and
     * undershoots are clamped to prevent blow-up.
     */
    public static float cerp(
            float a, float b,
            float c, float d,
            float x) {
        float xsq = x * x;
        float xcu = xsq * x;

        float minV = min(a, min(b, min(c, d)));
        float maxV = max(a, max(b, max(c, d)));

        float t
                = a * (0.0f - 0.5f * x + 1.0f * xsq - 0.5f * xcu)
                + b * (1.0f + 0.0f * x - 2.5f * xsq + 1.5f * xcu)
                + c * (0.0f + 0.5f * x + 2.0f * xsq - 1.5f * xcu)
                + d * (0.0f + 0.0f * x - 0.5f * xsq + 0.5f * xcu);

        return min(max(t, minV), maxV);
    }

    /**
     * Double precision cubic interpolate using samples a through d for x
     * ranging from 0 to 1. A Catmull-Rom spline is used. Over- and undershoots
     * are clamped to prevent blow-up.
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @param x
     * @return double precision cubic interpolation using samples a through d
     * for x ranging from 0 to 1. A Catmull-Rom spline is used. Over- and
     * undershoots are clamped to prevent blow-up.
     */
    public static double cerp(
            double a, double b,
            double c, double d,
            double x) {
        double xsq = x * x;
        double xcu = xsq * x;

        double minV = min(a, min(b, min(c, d)));
        double maxV = max(a, max(b, max(c, d)));

        double t
                = a * (0.0 - 0.5 * x + 1.0 * xsq - 0.5 * xcu)
                + b * (1.0 + 0.0 * x - 2.5 * xsq + 1.5 * xcu)
                + c * (0.0 + 0.5 * x + 2.0 * xsq - 1.5 * xcu)
                + d * (0.0 + 0.0 * x - 0.5 * xsq + 0.5 * xcu);

        return min(max(t, minV), maxV);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private Interpolation() {
    }
}
