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

import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.lang.String.format;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.geometry.generators.smoother.parametric.BSplinesCurve.KnotsType.NON_UNIFORM;
import static jot.math.geometry.generators.smoother.parametric.BSplinesCurve.KnotsType.PERIODIC_UNIFORM;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Class that implements a NURNS curve generator.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class NURBSCurve extends BSplinesCurve {

    static final Logger log = getLogger("NURBSCurve");

    static {
        log.setLevel(OFF);
    }

    /**
     * The points that represent the NURBs curve.
     */
    final ArrayList<Vector2D> NURBSCurve;

    private ArrayList<Float> weightsCurve;

    /**
     * Constructor.
     */
    public NURBSCurve() {
        this.knotsType = PERIODIC_UNIFORM;
        this.knots = new ArrayList<>();
        this.weightsCurve = new ArrayList<>();
        this.NURBSCurve = new ArrayList<>();
    }

    @Override
    public ArrayList<Vector2D> getCurve() {
        return this.NURBSCurve;
    }

    @Override
    public void generateCurve(ArrayList<Vector2D> ControlPoints, int smoothFactor) {
        int PointsCount = ControlPoints.size() + smoothFactor;
        this.setDegree(smoothFactor + 1);

        this.NURBSCurve.clear();
        this.knots.clear();
        this.weightsCurve.clear();

        this.knots = this.generateKnots(this.k - 1, ControlPoints.size());
        this.weightsCurve = this.generateWeights(ControlPoints.size());

        int n = ControlPoints.size() - 1;
        if (n < 2) {
            return;
        }
        log.info(format("Control points: %d", n + 1));

        if (this.k < 2 || this.k > (n + 1)) {
            return;
        }
        log.info(format("Degree: %d", this.k - 1));

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
            float x = 0, y = 0, sum = 0;
            for (int i = 0; i < (n + 1); i++) {
                float N = this.basisFunction(i, this.k - 1, (float) t, this.knots) * this.weightsCurve.get(i);
                //log.info(format("Basis Function %d: %f", i, N));
                sum += N;
                x += ControlPoints.get(i).getX() * N;
                y += ControlPoints.get(i).getY() * N;
            }
            //log.info(format("Sum %f", sum));
            x /= sum;
            y /= sum;
            //NURBSCurve.add(new Vector2D(x, y));
            //log.info(format("NURBS(%f): (%f, %f)", t, x, y));
        }
        log.info(Integer.toString(this.NURBSCurve.size()));
        log.info("");
    }

    /**
     * Generates an weights array. Each eights is in the interval [0.5; 1].
     *
     * @param n the number of control points in the OpenGL coordinates x axis.
     * @return An Weights array.
     */
    private ArrayList<Float> generateWeights(int n) {
        ArrayList<Float> weights = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            weights.add((float) round(random() + 0.5f)); //Interval [0.5; 1]
        }
        log.info(Integer.toString(weights.size()));
        log.info("");
        return weights;
    }
}
