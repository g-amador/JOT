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
import jot.math.geometry.generators.smoother.AbstractCurve;
import static jot.math.geometry.generators.smoother.parametric.BSplinesCurve.KnotsType.NON_PERIODIC_UNIFORM;
import static jot.math.geometry.generators.smoother.parametric.BSplinesCurve.KnotsType.NON_UNIFORM;
import static jot.math.geometry.generators.smoother.parametric.BSplinesCurve.KnotsType.PERIODIC_UNIFORM;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Class that implements a BSplines curve generator.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class BSplinesCurve extends AbstractCurve {

    private static final Logger log = getLogger("BSplinesCurve");

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
     * The knots.
     */
    protected ArrayList<Float> knots;

    /**
     * The points that represent the BSplines curve.
     */
    protected final ArrayList<Vector2D> BSplinesCurve;

    /**
     * Constructor.
     */
    public BSplinesCurve() {
        this.knotsType = PERIODIC_UNIFORM;
        this.knots = new ArrayList<>();
        this.BSplinesCurve = new ArrayList<>();
    }

    /**
     * Change the degree of the curve.
     *
     * @param degree the degree of the curve.
     */
    protected void setDegree(int degree) {
        this.k = degree;
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
    public ArrayList<Vector2D> getCurve() {
        return this.BSplinesCurve;
    }

    @Override
    public void generateCurve(ArrayList<Vector2D> ControlPoints, int smoothFactor) {
        int PointsCount = ControlPoints.size() + smoothFactor;
        this.setDegree(smoothFactor + 1);

        this.BSplinesCurve.clear();
        this.knots.clear();

        this.knots = this.generateKnots(this.k - 1, ControlPoints.size());

        int n = ControlPoints.size() - 1;
        if (n < 2) {
            return;
        }
        log.info(format("Control points: %d\n", n + 1));

        if (this.k < 2 || this.k > (n + 1)) {
            return;
        }
        log.info(format("Degree: %d\n", this.k - 1));

        int startIndex = 0;
        int endIndex = 0;
        if (this.knotsType == PERIODIC_UNIFORM) {
            startIndex = this.k - 1;
            endIndex = n + 1;
        } else {
            for (int i = 0; i < this.knots.size(); i++) {
                float knot = this.knots.get(i);
                if (knot > 0 && startIndex == 0.0f) {
                    startIndex = i;
                }
                if (knot == 1 && endIndex == 0.0f) {
                    endIndex = i - 1;
                    break;
                }
            }
        }
        log.info(format("Start index: %d", startIndex));
        log.info(format("End index: %d", endIndex));

        float t_size = 0.5f * (1.0f / PointsCount);
        log.info(format("Step: %f", t_size));

        if (this.knotsType == NON_UNIFORM) {
            startIndex = this.k - startIndex;
            endIndex -= startIndex;
        }

        for (double t = this.knots.get(startIndex); t < this.knots.get(endIndex); t += t_size) {
            float x = 0, y = 0;
            for (int i = 0; i < (n + 1); i++) {
                float N = this.basisFunction(i, this.k - 1, (float) t, this.knots);
                //log.info(format("Basis Function %d: %f", i, N));
                x += ControlPoints.get(i).getX() * N;
                y += ControlPoints.get(i).getY() * N;
            }

            this.BSplinesCurve.add(new Vector2D(x, y));
            //log.info(format("BSplines(%f): (%f, %f)", t, x, y));
        }
        log.info(Integer.toString(this.BSplinesCurve.size()));
        log.info("");
    }

    /**
     * Generates an knots array.
     *
     * @param degree the degree of the curve.
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
     * @param degree the degree of the curve.
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
        //    log.info(Float.toString(f));
        //}
        log.info(Integer.toString(PUKnots.size()));
        log.info("");
        return PUKnots;
    }

    /**
     * Generates an Non Periodic Uniform Knots array.
     *
     * @param degree the degree of the curve.
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
        //    log.info(Float.toString(f));
        //}
        log.info(Integer.toString(NPUKnots.size()));
        log.info("");
        return NPUKnots;
    }

    /**
     * Generates an Non Uniform Knots array.
     *
     * @param degree the degree of the curve.
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
        //    log.info(Float.toString(f));
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
     * a new NURBS curve point.
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
