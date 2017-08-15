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
package jot.math.geometry.generators.smoother.parametric;

import static java.lang.Double.isInfinite;
import static java.lang.Math.random;
import static java.lang.String.format;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.geometry.generators.smoother.AbstractSurface;
import static jot.math.geometry.generators.smoother.parametric.BSplinesSurface.KnotsType.NON_PERIODIC_UNIFORM;
import static jot.math.geometry.generators.smoother.parametric.BSplinesSurface.KnotsType.NON_UNIFORM;
import static jot.math.geometry.generators.smoother.parametric.BSplinesSurface.KnotsType.PERIODIC_UNIFORM;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a BSplines surface generator.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class BSplinesSurface extends AbstractSurface {

    private static final Logger log = getLogger("BSplinesSurface");

    static {
        log.setLevel(OFF);
    }

    /**
     * The type of knots of this surface.
     */
    protected KnotsType knotsType;

    /**
     * The k degree of the surface.
     */
    protected int k = 2;

    /**
     * The l degree of the surface.
     */
    protected int l = 2;

    /**
     * The knots in u.
     */
    protected ArrayList<Float> knotsU;

    /**
     * The knots in v.
     */
    protected ArrayList<Float> knotsV;

    /**
     * The heightmap that contains a NURBs surface.
     */
    protected final ArrayList<ArrayList<Vector3D>> BSplinesSurface;

    /**
     * Constructor.
     */
    public BSplinesSurface() {
        this.knotsType = PERIODIC_UNIFORM;
        this.knotsU = new ArrayList<>();
        this.knotsV = new ArrayList<>();
        this.BSplinesSurface = new ArrayList<>();
    }

    /**
     * Change the degree of the surface for the u points.
     *
     * @param degree the degree k of the surface, in the u direction.
     */
    protected void setDegreeK(int degree) {
        this.k = degree;
    }

    /**
     * Change the degree of the surface for the v points.
     *
     * @param degree the degree l of the surface, in the v direction.
     */
    protected void setDegreeL(int degree) {
        this.l = degree;
    }

    /**
     * Change the knotsType default value;
     *
     * @param knotsType the knotsType to use.
     */
    public void setKnotsType(KnotsType knotsType) {
        this.knotsType = knotsType;

    }

    @Override
    public ArrayList<ArrayList<Vector3D>> getSurface() {
        return this.BSplinesSurface;
    }

    @Override
    public void generateSurface(ArrayList<ArrayList<Vector3D>> Heightfield, int smoothFactor) {
        int PointsCountU = Heightfield.size() + smoothFactor;
        int PointsCountV = Heightfield.get(0).size() + smoothFactor;
        this.setDegreeK(smoothFactor + 1);
        this.setDegreeL(smoothFactor + 1);

        this.BSplinesSurface.clear();
        this.knotsU.clear();
        this.knotsV.clear();

        this.knotsU = this.generateKnots(this.k - 1, Heightfield.size());
        this.knotsV = this.generateKnots(this.l - 1, Heightfield.get(0).size());

        int n = Heightfield.size() - 1;
        int m = Heightfield.get(0).size() - 1;
        if (n < 2 || m < 2) {
            return;
        }
        log.info(format("Control points in the X axis: %d\nControl points in the Y axis: %d", n + 1, m + 1));

        if (this.k < 2 || this.k > (n + 1) || this.l < 2 || this.l > (m + 1)) {
            return;
        }
        log.info(format("Degree in the X axis: %d\nDegree in the Y axis: %d", this.k - 1, this.l - 1));

        int startIndexU = 0;
        int startIndexV = 0;
        int endIndexU = 0;
        int endIndexV = 0;
        if (this.knotsType == PERIODIC_UNIFORM) {
            startIndexU = this.k - 1;
            startIndexV = this.l - 1;
            endIndexU = n + 1;
            endIndexV = m + 1;
        } else {
            for (int i = 0; i < this.knotsU.size(); i++) {
                float knot = this.knotsU.get(i);
                if (knot > 0 && startIndexU == 0.0f) {
                    startIndexU = i;
                }
                if (knot == 1 && endIndexU == 0.0f) {
                    endIndexU = i - 1;
                    break;
                }
            }
            for (int i = 0; i < this.knotsV.size(); i++) {
                float knot = this.knotsV.get(i);
                if (knot > 0 && startIndexV == 0.0f) {
                    startIndexV = i;
                }
                if (knot == 1 && endIndexV == 0.0f) {
                    endIndexV = i - 1;
                    break;
                }
            }
        }
        log.info(format("Start index in U: %d", startIndexU));
        log.info(format("End index in U: %d", endIndexU));
        log.info(format("Start index in V: %d", startIndexV));
        log.info(format("End index in V: %d", endIndexV));

        float u_size = 0.5f * (1.0f / PointsCountU);
        log.info(format("Step in U: %f", u_size));

        float v_size = 0.5f * (1.0f / PointsCountV);
        log.info(format("Step in V: %f", v_size));

        if (this.knotsType == NON_UNIFORM) {
            startIndexU = this.k - startIndexU;
            endIndexU -= startIndexU;
            startIndexV = this.l - startIndexV;
            endIndexV -= startIndexV;
        }

        for (double u = this.knotsU.get(startIndexU); u < this.knotsU.get(endIndexU); u += u_size) {
            ArrayList<Vector3D> line = new ArrayList<>();
            for (double v = this.knotsV.get(startIndexV); v < this.knotsV.get(endIndexV); v += v_size) {
                float x = 0, y = 0, z = 0;
                for (int i = 0; i < (n + 1); i++) {
                    float N_i = this.basisFunction(i, this.k - 1, (float) u, this.knotsU);
                    //og.info(format("Basis Function %d: %f", i, N_i));
                    for (int j = 0; j < (m + 1); j++) {
                        float N_j = this.basisFunction(j, this.l - 1, (float) v, this.knotsV);
                        //log.info(format("Basis Function %d: %f", i, N_j));
                        //log.info(format("P(" + i + ", " + j + "):" + Heightfield.get(i).get(j)));
                        x += Heightfield.get(i).get(j).getX() * N_i * N_j;
                        y += Heightfield.get(i).get(j).getY() * N_i * N_j;
                        z += Heightfield.get(i).get(j).getZ() * N_i * N_j;
                    }
                }
                line.add(new Vector3D(x, y, z));
                //log.info(format("S(%f, %f): (%f, %f, %f)", u, v, x, y, z));
            }
            this.BSplinesSurface.add(line);
        }
        log.info(format("%d %d", this.BSplinesSurface.size(), this.BSplinesSurface.get(0).size()));
        log.info("");
    }

    /**
     * Generates an knots array.
     *
     * @param degree the degree of the surface.
     * @param n the number of control points.
     * @return an Knots array.
     */
    public ArrayList<Float> generateKnots(int degree, int n) {
        switch (this.knotsType) {
            case PERIODIC_UNIFORM:
                return this.PeriodicUniformKnots(degree, n);
            case NON_PERIODIC_UNIFORM:
                return this.NonPeriodicUniformKnots(degree, n);
            case NON_UNIFORM:
                return this.NonUniformKnots(degree, n);
            default:
                throw new AssertionError(this.knotsType.name());
        }
    }

    /**
     * Generates an Periodic Uniform Knots array.
     *
     * @param degree the degree of the surface.
     * @param n the number of control points.
     * @return an Periodic Uniform Knots array.
     */
    protected ArrayList<Float> PeriodicUniformKnots(int degree, int n) {
        ArrayList<Float> PUKnots = new ArrayList<>();
        for (double j = 0, sum = 0; j < (degree + n); j++) {
            PUKnots.add((float) sum);
            sum += 1.0f / (degree + n);
        }
        PUKnots.add(1.0f);
        //for (Float f : PUKnots) {
        //    log.info(f);
        //}
        log.info(Integer.toString(PUKnots.size()));
        log.info("");
        return PUKnots;
    }

    /**
     * Generates an Non Periodic Uniform Knots array.
     *
     * @param degree the degree of the surface.
     * @param n the number of control points.
     * @return an Non Periodic Uniform Knots array.
     */
    protected ArrayList<Float> NonPeriodicUniformKnots(int degree, int n) {
        ArrayList<Float> NPUKnots = new ArrayList<>();
        for (double j = 0, sum = 0; j < (degree + n + 1); j++) {
            if (j <= degree) {
                NPUKnots.add(0.0f);
            } else if (j < n) {
                sum += 1.0f / (degree + n - degree * 2);
                NPUKnots.add((float) sum);
            } else {
                NPUKnots.add(1.0f);
            }
        }
        //for (Float f : NPUKnots) {
        //    log.info(f);
        //}
        log.info(Integer.toString(NPUKnots.size()));
        log.info("");
        return NPUKnots;
    }

    /**
     * Generates an Non Uniform Knots array.
     *
     * @param degree the degree of the surface.
     * @param n the number of control points.
     * @return an Non Uniform Knots array.
     */
    protected ArrayList<Float> NonUniformKnots(int degree, int n) {
        ArrayList<Float> NUKnots = new ArrayList<>();
        NUKnots.add(0.0f);
        for (int j = 1; j < (degree + n); j++) {
            float knot = (float) random() / degree * 5 + NUKnots.get(j - 1);
            NUKnots.add(knot >= 1.0f ? 1.0f : knot);
        }
        NUKnots.add(1.0f);
        //for (Float f : NUKnots) {
        //    log.info(f);
        //}
        log.info(Integer.toString(NUKnots.size()));
        log.info("");
        return NUKnots;
    }

    /**
     * The (recursive) basis function.
     *
     * @param i
     * @param k
     * @param t
     * @param knots
     * @return the value of all contributing knots to calculate a given value of
     * a new NURBS surface point.
     */
    protected float basisFunction(int i, int k, float t, ArrayList<Float> knots) {
        if (k == 0) {
            return knots.get(i) <= t && t < knots.get(i + 1) ? 1 : 0;
        }

        //THESE FOLLOWING 2 IF STATEMENTS ARE ONLY FOR NON UNIFORM KNOTS!!!!!!!
        if (knots.get(i + k) - knots.get(i) == 0) {
            return 0;
        }

        if (knots.get(i + k + 1) - knots.get(i + 1) == 0) {
            return 0;
        }

        if (isInfinite((t - knots.get(i)) / (knots.get(i + k) - knots.get(i)))) {
            log.info(Float.toString(t - knots.get(i)));
            log.info(Float.toString(knots.get(i + k) - knots.get(i)));
            log.info(Float.toString((t - knots.get(i)) / (knots.get(i + k) - knots.get(i))));
        }

        if (isInfinite((knots.get(i + k + 1) - t) / (knots.get(i + k + 1) - knots.get(i + 1)))) {
            log.info(Float.toString(knots.get(i + k + 1) - t));
            log.info(Float.toString(knots.get(i + k + 1) - knots.get(i + 1)));
            log.info(Float.toString((knots.get(i + k + 1) - t) / (knots.get(i + k + 1) - knots.get(i + 1))));
        }

        return ((t - knots.get(i)) / (knots.get(i + k) - knots.get(i)))
                * this.basisFunction(i, k - 1, t, knots)
                + ((knots.get(i + k + 1) - t) / (knots.get(i + k + 1) - knots.get(i + 1)))
                * this.basisFunction(i + 1, k - 1, t, knots);

        //return 0;
    }

    /**
     * Types of knots.
     */
    public enum KnotsType {

        /**
         * Periodic uniform.
         */
        PERIODIC_UNIFORM,
        /**
         * Non-periodic uniform
         */
        NON_PERIODIC_UNIFORM,
        /**
         * Non-uniform
         */
        NON_UNIFORM,
    }
}
