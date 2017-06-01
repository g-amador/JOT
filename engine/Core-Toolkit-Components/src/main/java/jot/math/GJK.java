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

import static java.lang.Math.signum;
import static java.lang.String.format;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.crossProduct;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.dotProduct;

/**
 * Class that implements the Gilbert-Johnson-Keerthi algorithm for collision
 * detection in 3D, as described in the video lecture at
 * http://mollyrocket.com/849
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class GJK {

    static final Logger log = getLogger("GJK");

    private static Vector3D Direction;

    //To prevent infinite loops - if an intersection is not found in 20 rounds, consider there is no intersection.
    private static final int MaxIterations = 20;

    static {
        log.setLevel(OFF);
    }

    /**
     * Given the vertices (in any order) of two convex 3D bodies, calculates
     * whether they intersect.
     *
     * @param shape1 convex 3D body.
     * @param shape2 convex 3D body.
     * @return TRUE if two convex 3D bodies intersect, FALSE otherwise
     */
    public static boolean BodiesIntersect(ArrayList<Vector3D> shape1, ArrayList<Vector3D> shape2) {
        //For initial point, just take the difference between any two vertices (in this case - the first ones)
        Vector3D initialPoint = new Vector3D(shape1.get(0).toArray()).subtract(shape2.get(0));

        //Choose a search direction
        Vector3D S = MaxPointInMinkDiffAlongDir(shape1, shape2, initialPoint);
        Direction = S.negate();

        //Get the first Minkowski Difference point
        ArrayList<Vector3D> simplex = new ArrayList<>();
        simplex.add(S);

        for (int i = 0; i < MaxIterations; i++) {
            //Get our next simplex point toward the origin.
            Vector3D A = MaxPointInMinkDiffAlongDir(shape1, shape2, Direction);

            //If we move toward the origin and didn't pass it then we never will and there's no intersection.
            if (dotProduct(A, Direction) < 0) {
                return false;
            }

            //Otherwise we add the new point to the simplex and process it.
            simplex.add(A);

            //Here we either find a collision or we find the closest feature of
            //the simplex to the origin, make that the new simplex and update the direction
            //to move toward the origin from that feature.            
            log.info(format("GJK: %s", Direction.toString()));
            if (UpdateSimplexAndDirection(simplex)) {
                return true;
            }
            log.info(format("GJK: %s", Direction.toString()));
            log.info("");
        }

        return false;
    }

    /**
     * Updates the current simplex and the direction in which to look for the
     * origin. Called DoSimplex in the video lecture.
     *
     * @param simplex A list of points in the current simplex. The last point in
     * the list must be the last point added to the simplex.
     * @param direction
     * @return TRUE if an intersection found on this iteration, FALSE otherwise.
     */
    //@SuppressWarnings({"unchecked"})
    //@SuppressWarnings({"Unused Assignment"})
    private static boolean UpdateSimplexAndDirection(ArrayList<Vector3D> simplex) {
        switch (simplex.size()) {
            case 2: //if the simplex is a line.
            {
                //A is the point added last to the simplex
                Vector3D A = simplex.get(1);
                Vector3D B = simplex.get(0);
                Vector3D AB = B.subtract(A);
                Vector3D AO = A.negate();

                log.info(format("simplex2: %s", Direction.toString()));
                if (dotProduct(AB, AO) > 0) {
                    Direction = crossProduct(crossProduct(AB, AO), AB);
                } else {
                    Direction = AO;
                }
                log.info(format("simplex2: %s", Direction.toString()));
            }
            break;
            case 3: //if the simplex is a triangle.
            {
                //A is the point added last to the simplex
                Vector3D A = simplex.get(2);
                Vector3D B = simplex.get(1);
                Vector3D C = simplex.get(0);
                Vector3D AO = A.negate();
                Vector3D AB = B.subtract(A);
                Vector3D AC = C.subtract(A);
                Vector3D ABC = crossProduct(AB, AC);

                log.info(format("simplex3: %s", Direction.toString()));
                if (dotProduct(crossProduct(ABC, AC), AO) > 0) {
                    if (dotProduct(AC, AO) > 0) {
                        simplex.clear();
                        simplex.add(C);
                        simplex.add(A);
                        Direction = crossProduct(crossProduct(AC, AO), AC);
                    } else if (dotProduct(AB, AO) > 0) {
                        simplex.clear();
                        simplex.add(B);
                        simplex.add(A);
                        Direction = crossProduct(crossProduct(AB, AO), AB);
                    } else {
                        simplex.clear();
                        simplex.add(A);
                        Direction = AO;
                    }
                } else if (dotProduct(crossProduct(AB, ABC), AO) > 0) {
                    if (dotProduct(AB, AO) > 0) {
                        simplex.clear();
                        simplex.add(B);
                        simplex.add(A);
                        Direction = crossProduct(crossProduct(AB, AO), AB);
                    } else {
                        simplex.clear();
                        simplex.add(A);
                        Direction = AO;
                    }
                } else if (dotProduct(ABC, AO) > 0) {
                    //the simplex stays A, B, C
                    Direction = ABC;
                } else {
                    simplex.clear();
                    simplex.add(B);
                    simplex.add(C);
                    simplex.add(A);

                    Direction = ABC.negate();
                }
                log.info(format("simplex3: %s", Direction.toString()));
            }
            break;
            default: //if the simplex is a tetrahedron
            {
                //A is the point added last to the simplex
                Vector3D A = simplex.get(3);
                Vector3D B = simplex.get(2);
                Vector3D C = simplex.get(1);
                Vector3D D = simplex.get(0);

                Vector3D AO = A.negate();
                Vector3D AB = B.subtract(A);
                Vector3D AC = C.subtract(A);
                Vector3D AD = D.subtract(A);
                Vector3D ABC = crossProduct(AB, AC);
                Vector3D ACD = crossProduct(AC, AD);
                Vector3D ADB = crossProduct(AD, AB);

                //the side (positive or negative) of B, C and D relative to the planes of ACD, ADB and ABC respectively
                int BsideOnACD = (int) signum(dotProduct(ACD, AB));
                int CsideOnADB = (int) signum(dotProduct(ADB, AC));
                int DsideOnABC = (int) signum(dotProduct(ABC, AD));

                //whether the origin is on the same side of ACD/ADB/ABC as B, C and D respectively
                boolean ABsameAsOrigin = signum(dotProduct(ACD, AO)) == BsideOnACD;
                boolean ACsameAsOrigin = signum(dotProduct(ADB, AO)) == CsideOnADB;
                boolean ADsameAsOrigin = signum(dotProduct(ABC, AO)) == DsideOnABC;

                log.info(format("simplex4: %s", Direction.toString()));
                //if the origin is on the same side as all B, C and D, the origin is inside the tetrahedron and thus there is a collision
                if (ABsameAsOrigin && ACsameAsOrigin && ADsameAsOrigin) {
                    return true;
                } //if the origin is not on the side of B relative to ACD
                else if (!ABsameAsOrigin) {
                    //B is farthest from the origin among all of the tetrahedron's points, so remove it from the list and go on with the triangle case
                    simplex.remove(B);
                    //the new direction is on the other side of ACD, relative to B
                    Direction = ACD.scalarMultiply(-BsideOnACD);

                } //if the origin is not on the side of C relative to ADB
                else if (!ACsameAsOrigin) {
                    //C is farthest from the origin among all of the tetrahedron's points, so remove it from the list and go on with the triangle case
                    simplex.remove(C);
                    //the new direction is on the other side of ADB, relative to C                
                    Direction = ADB.scalarMultiply(-CsideOnADB);
                } //if the origin is not on the side of D relative to ABC
                else //if (!ADsameAsOrigin)
                {
                    //D is farthest from the origin among all of the tetrahedron's points, so remove it from the list and go on with the triangle case
                    simplex.remove(D);
                    //the new direction is on the other side of ABC, relative to D                
                    Direction = ABC.scalarMultiply(-DsideOnABC);
                }
                log.info(format("simplex4: %s", Direction.toString()));

                //go on with the triangle case
                //TODO: maybe we should restrict the depth of the recursion, just like we restricted the number of iterations in BodiesIntersect?
                return UpdateSimplexAndDirection(simplex);
            }
        }

        //no intersection found on this iteration
        return false;
    }

    /**
     * Finds the farthest point along a given direction of the Minkowski
     * difference of two convex polyhedra. Called Support in the video lecture:
     * max(D.Ai) - max(-D.Bj)
     *
     * @param shape1
     * @param shape2
     * @param direction
     * @return the farthest point along a given direction of the Minkowski
     * difference of two convex polyhedra.
     */
    private static Vector3D MaxPointInMinkDiffAlongDir(
            ArrayList<Vector3D> shape1, ArrayList<Vector3D> shape2, Vector3D direction) {
        Vector3D max = MaxPointAlongDirection(shape1, direction).
                subtract(MaxPointAlongDirection(shape2, direction.negate()));

        return max;
    }

    /**
     * Finds the farthest point along a given direction of a convex polyhedron.
     *
     * @param shape
     * @param direction
     * @return the farthest point along a given direction of a convex
     * polyhedron.
     */
    private static Vector3D MaxPointAlongDirection(ArrayList<Vector3D> shape, Vector3D direction) {
        Vector3D max = new Vector3D(shape.get(0).toArray());

        for (Vector3D v : shape) {
            if (dotProduct(max, direction) < dotProduct(v, direction)) {
                max = new Vector3D(v.toArray());
            }
        }

        return max;
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private GJK() {
    }
}
