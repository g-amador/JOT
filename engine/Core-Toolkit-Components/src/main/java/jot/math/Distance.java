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

import static jot.math.Distance.DistanceType.L2_NORM;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.distance;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.distance1;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.distanceInf;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.distanceSq;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import static org.apache.commons.math3.geometry.euclidean.twod.Vector2D.distance;
import static org.apache.commons.math3.geometry.euclidean.twod.Vector2D.distanceInf;
import static org.apache.commons.math3.geometry.euclidean.twod.Vector2D.distanceSq;

/**
 * Abstract class that specifies the distance function to use.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class Distance {

    private static DistanceType distanceType = L2_NORM;

    /**
     * Get the Euclidean distance from given vertex (v1) position to given
     * vertex (v2) position.
     *
     * @param v1 position in 2D Cartesian coordinates of given vertex.
     * @param v2 position in 2D Cartesian coordinates of given vertex.
     * @return Euclidean distance from v1 to v2.
     */
    public static double getDistance(Vector2D v1, Vector2D v2) {
        switch (distanceType) {
            case L2_NORM:
                return (float) distance(v2, v1);
            case LINFINITY_NORM:
                return (float) distanceInf(v2, v1);
            case SQUARE:
                return (float) distanceSq(v2, v1);
            default:
                //throw new AssertionError(distanceType.name());
                return 0.0;
        }
    }

    /**
     * Get the Euclidean distance from given vertex (v1) position to given
     * vertex (v2) position.
     *
     * @param v1 position in 3D Cartesian coordinates of given vertex.
     * @param v2 position in 3D Cartesian coordinates of given vertex.
     * @return Euclidean distance from v1 to v2.
     */
    public static double getDistance(Vector3D v1, Vector3D v2) {
        switch (distanceType) {
            case L1_NORM:
                return (float) distance1(v2, v1);
            case L2_NORM:
                return (float) distance(v2, v1);
            case LINFINITY_NORM:
                return (float) distanceInf(v2, v1);
            case SQUARE:
                return (float) distanceSq(v2, v1);
            default:
                //throw new AssertionError(distanceType.name());
                return 0.0;
        }
    }

    /**
     * Set the distance type to use, by default L2 norm distance is used.
     *
     * @param distType the distance type to use.
     */
    public static void setDistanceType(DistanceType distType) {
        distanceType = distType;
    }

    /**
     * Default private constructor
     */
    private Distance() {
    }

    /**
     * Available distance formulas: L1_NORM, L2_NORM, LINFINITY_NORM, and
     * SQUARE.
     */
    public enum DistanceType {

        L1_NORM, L2_NORM, LINFINITY_NORM, SQUARE;
    }
}
