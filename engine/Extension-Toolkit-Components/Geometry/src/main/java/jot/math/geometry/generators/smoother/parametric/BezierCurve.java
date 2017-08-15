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

import static java.lang.Math.pow;
import static java.lang.String.format;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.geometry.generators.smoother.AbstractCurve;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

/**
 * Class that implements a Bezier curve generator.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class BezierCurve extends AbstractCurve {

    static final Logger log = getLogger("BezierCurve");

    static {
        log.setLevel(OFF);
    }

    final ArrayList<Vector2D> BezierCurve;

    /**
     * Constructor.
     */
    public BezierCurve() {
        this.BezierCurve = new ArrayList<>();
    }

    @Override
    public ArrayList<Vector2D> getCurve() {
        return this.BezierCurve;
    }

    @Override
    public void generateCurve(ArrayList<Vector2D> ControlPoints, int smoothFactor) {
        int PointsCount = ControlPoints.size() + smoothFactor;

        this.BezierCurve.clear();
        int n = ControlPoints.size() - 1;
        if (n < 2) {
            return;
        }
        log.info(format("Control points: %d", n + 1));

        float t_size = 1 / (float) PointsCount;
        log.info(format("Step: %f", t_size));

        //for (float t = 0; t < (1 + t_size); t += t_size) {
        for (double t = 0; t <= 1; t += t_size) {
            float x = 0, y = 0;
            for (int i = 0; i <= n; i++) {
                float bernsteinPolynomial = this.BernsteinPolynomial(n, i, (float) t);
                //log.info(format("ControlPoints(" + i + "): " + ControlPoints.get(i)));
                x += ControlPoints.get(i).getX() * bernsteinPolynomial;
                y += ControlPoints.get(i).getY() * bernsteinPolynomial;
            }
            this.BezierCurve.add(new Vector2D(x, y));
            //log.info(format("B(%f): ({%f, %f)", t, x, y));
        }
        log.info(Integer.toString(this.BezierCurve.size()));
        log.info("");
    }

    /**
     * Calculates the Bernstein for given n, i and t values.
     *
     * @param n number of elements.
     * @param i elements per group.
     * @param t number of Bezier points to generate.
     * @return Bernstein polynomial value for given n, i and t values.
     */
    private float BernsteinPolynomial(int n, int i, float t) {
        return (float) (this.BinomialCoefficient(n, i) * pow(t, i) * pow(1.0 - t, n - i));
    }

    /**
     * Calculate the binomial coefficient of n elements in i elements groups.
     *
     * @param n number of elements.
     * @param i elements per group.
     * @return the binomial coefficient of n by i.
     */
    private float BinomialCoefficient(int n, int i) {
        return this.fact(n) / (this.fact(i) * this.fact(n - i));
    }

    /**
     * Calculate the factorial of a given natural number n.
     *
     * @param n a given natural number.
     * @return the factorial of a given natural number n.
     */
    private float fact(int n) {
        if (n == 0 || n == 1) {
            return 1; //!0 = 1 just because!
        }

        float mult = n;
        for (int i = n - 1; i > 0; i--) {
            mult *= i;
        }
        return mult;
    }
}
