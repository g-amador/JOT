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
import jot.math.geometry.generators.smoother.AbstractSurface;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a Bezier surface generator.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class BezierSurface extends AbstractSurface {

    static final Logger log = getLogger("BezierSurface");

    static {
        log.setLevel(OFF);
    }

    ArrayList<ArrayList<Vector3D>> BezierSurface;

    /**
     * Constructor.
     */
    public BezierSurface() {
        this.BezierSurface = new ArrayList<>();
    }

    @Override
    public ArrayList<ArrayList<Vector3D>> getSurface() {
        return this.BezierSurface;
    }

    @Override
    public void generateSurface(ArrayList<ArrayList<Vector3D>> Heightfield, int smoothFactor) {
        int PointsCountU = Heightfield.size() + smoothFactor;
        int PointsCountV = Heightfield.get(0).size() + smoothFactor;

        this.BezierSurface.clear();
        int n = Heightfield.size() - 1;
        int m = Heightfield.get(0).size() - 1;
        if (n < 1 || m < 1) {
            this.BezierSurface = Heightfield;
            return;
        }
        log.info(format("Control points in u: %d\nControl points in v: {%d", n + 1, m + 1));

        float u_size = 1 / (float) PointsCountU;
        float v_size = 1 / (float) PointsCountV;
        log.info(format("Step in u: %f\nStep in v: %f", u_size, v_size));

        for (double u = 0; u <= 1; u += u_size) {
            ArrayList<Vector3D> line = new ArrayList<>();
            for (double v = 0; v <= 1; v += v_size) {
                float x = 0, y = 0, z = 0;
                for (int i = 0; i <= n; i++) {
                    float bernsteinPolynomial_i = this.BernsteinPolynomial(n, i, (float) u);
                    for (int j = 0; j <= m; j++) {
                        float bernsteinPolynomial_j = this.BernsteinPolynomial(m, j, (float) v);
                        //log.info(format("P(" + i + ", " + j + "): " + Heightfield.get(i).get(j)));
                        x += Heightfield.get(i).get(j).getX() * bernsteinPolynomial_i * bernsteinPolynomial_j;
                        y += Heightfield.get(i).get(j).getY() * bernsteinPolynomial_i * bernsteinPolynomial_j;
                        z += Heightfield.get(i).get(j).getZ() * bernsteinPolynomial_i * bernsteinPolynomial_j;
                    }
                }
                line.add(new Vector3D(x, y, z));
                //log.info(format("S(%f, %f): (%f, %f, %f)", u, v, x, y, z));
            }
            this.BezierSurface.add(line);
        }
        log.info(format("%d %d", this.BezierSurface.size(), this.BezierSurface.get(0).size()));
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
