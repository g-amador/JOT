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
import static jot.math.geometry.generators.smoother.parametric.BSplinesSurface.KnotsType.NON_UNIFORM;
import static jot.math.geometry.generators.smoother.parametric.BSplinesSurface.KnotsType.PERIODIC_UNIFORM;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a NURNS surface generator.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class NURBSSurface extends BSplinesSurface {

    static final Logger log = getLogger("NURBSSurface");

    static {
        log.setLevel(OFF);
    }

    /**
     * The heightmap that contains a NURBs surface.
     */
    final ArrayList<ArrayList<Vector3D>> NURBSSurface;

    private ArrayList<ArrayList<Float>> weightsSurface;

    /**
     * Constructor.
     */
    public NURBSSurface() {
        this.knotsType = PERIODIC_UNIFORM;
        this.knotsU = new ArrayList<>();
        this.knotsV = new ArrayList<>();
        this.weightsSurface = new ArrayList<>();
        this.NURBSSurface = new ArrayList<>();
    }

    @Override
    public ArrayList<ArrayList<Vector3D>> getSurface() {
        return this.NURBSSurface;
    }

    @Override
    public void generateSurface(ArrayList<ArrayList<Vector3D>> Heightfield, int smoothFactor) {
        int PointsCountU = Heightfield.size() + smoothFactor;
        int PointsCountV = Heightfield.get(0).size() + smoothFactor;
        this.setDegreeK(smoothFactor + 1);
        this.setDegreeL(smoothFactor + 1);

        this.NURBSSurface.clear();
        this.knotsU.clear();
        this.knotsV.clear();
        this.weightsSurface.clear();

        this.knotsU = this.generateKnots(this.k - 1, Heightfield.size());
        this.knotsV = this.generateKnots(this.l - 1, Heightfield.get(0).size());
        this.weightsSurface = this.generateWeights(Heightfield.size(), Heightfield.get(0).size());

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
                float x = 0, y = 0, z = 0, sum = 0;
                for (int i = 0; i < (n + 1); i++) {
                    float N_i = this.basisFunction(i, this.k - 1, (float) u, this.knotsU);
                    //log.info(format("Basis Function %d: %f", i, N_i));
                    for (int j = 0; j < (m + 1); j++) {
                        float N_j = this.basisFunction(j, this.l - 1, (float) v, this.knotsV);
                        sum += N_i * N_j * this.weightsSurface.get(i).get(j);
                        //log.info(format("Basis Function %d: %f", i, N_j));
                        //log.info(format("P(" + i + ", " + j + "): " + Heightfield.get(i).get(j)));
                        x += Heightfield.get(i).get(j).getX() * N_i * N_j * this.weightsSurface.get(i).get(j);
                        y += Heightfield.get(i).get(j).getY() * N_i * N_j * this.weightsSurface.get(i).get(j);
                        z += Heightfield.get(i).get(j).getZ() * N_i * N_j * this.weightsSurface.get(i).get(j);
                    }
                }
                //log.info(format("Sum %f", sum));
                x /= sum;
                y /= sum;
                z /= sum;
                line.add(new Vector3D(x, y, z));
                //log.info(format("S(%f, %f): (%f, %f, %f)", u, v, x, y, z));
            }
            this.NURBSSurface.add(line);
        }
        log.info(format("%d %d", this.NURBSSurface.size(), this.NURBSSurface.get(0).size()));
        log.info("");
    }

    /**
     * Generates an weights array. Each eights is in the interval [0.5; 1].
     *
     * @param n the number of control points in the heightfield in the OpenGL
     * coordinates x axis.
     * @param m the number of control points in the heightfield in the OpenGL
     * coordinates z axis.
     * @return An Weights array.
     */
    private ArrayList<ArrayList<Float>> generateWeights(int n, int m) {
        ArrayList<ArrayList<Float>> weights = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ArrayList<Float> line = new ArrayList<>();
            for (int j = 0; j < m; j++) {
                //line.add((float) Math.round((Math.random() * 2.0f) - 1.0f)); //Interval [-1; 1]
                //line.add((float) Math.round(Math.random() * - 1.0f)); //Interval [-1; 0]
                //line.add((float) Math.round(Math.random())); //Interval [0; 1]
                line.add((float) round(random() + 0.5f)); //Interval [0.5; 1]
            }

            //for (Float f : line) {
            //    log.info(Float.toString(f));
            //}
            weights.add(line);
        }
        log.info(format("%d %d", weights.size(), weights.get(0).size()));
        log.info("");
        return weights;
    }
}
