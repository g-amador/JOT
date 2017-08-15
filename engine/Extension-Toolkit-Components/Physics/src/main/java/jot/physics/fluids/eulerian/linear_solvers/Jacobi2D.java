/*
 * This file is part of the JOT game engine physics extension toolkit component.
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
package jot.physics.fluids.eulerian.linear_solvers;

import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;

/**
 * Class that implements the Jacobi2D linear solver algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Jacobi2D extends AbstractLinearSolver2D {

    static final Logger log = getLogger("Jacobi");

    static {
        log.setLevel(OFF);
    }

    /**
     * SOR variable.
     */
    public float w = 1.1f;

    /**
     * Constructor.
     *
     * @param n
     */
    public Jacobi2D(int n) {
        super(n);
    }

    /**
     * Constructor
     *
     * @param n
     * @param w
     */
    public Jacobi2D(int n, float w) {
        super(n);
        this.w = w;
    }

    @Override
    public void linearSolver(
            int b, int iters,
            float[] x, float[] x0,
            float a, float c) {
        float[] temp = new float[x.length];
        float d = a == 1 ? 1.0f : a * 4.0f - 0.5f;

        for (int k = 0; k < iters; k++) {
            for (int i = 1; i < this.N - 1; i++) {
                for (int j = 1; j < this.N - 1; j++) {

                    if (extensionPhysicsOptions.get("useStableFluids")) {
                        temp[this.I(i, j)] = extensionPhysicsOptions.get("useMehrstellen")
                                ? (4.0f * x0[this.I(i, j)]
                                + 0.5f * (x0[this.I(i - 1, j)] + x0[this.I(i + 1, j)]
                                + x0[this.I(i, j - 1)] + x0[this.I(i, j + 1)])
                                + d * (x[this.I(i - 1, j)] + x[this.I(i + 1, j)]
                                + x[this.I(i, j - 1)] + x[this.I(i, j + 1)])
                                + a * (x[this.I(i - 1, j - 1)] + x[this.I(i + 1, j - 1)]
                                + x[this.I(i - 1, j + 1)] + x[this.I(i + 1, j + 1)])) / c
                                //a * (4.0f * (x[I(i - 1, j)] + x[I(i + 1, j)] + x[I(i, j - 1)] + x[I(i, j + 1)])
                                //x[I(i - 1, j - 1)] + x[I(i + 1, j - 1)] + x[I(i - 1, j + 1)] + x[I(i + 1, j + 1)]) =                                                       
                                //4 * x[I(i, j)] + 0.5f * (x[I(i - 1, j)] + x[I(i + 1, j)] + x[I(i, j - 1)] + x[I(i, j + 1)])             
                                //-4 * x0[I(i, j)] - 0.5f * (x0[I(i - 1, j)] + x0[I(i + 1, j)] + x0[I(i, j - 1)] + x0[I(i, j + 1)])                                                             
                                : (x0[this.I(i, j)]
                                + a * (x[this.I(i - 1, j)] + x[this.I(i + 1, j)]
                                + x[this.I(i, j - 1)] + x[this.I(i, j + 1)])) / c;
                    }

                    if (extensionPhysicsOptions.get("usePracticalFluids")) {
                        temp[this.I(i, j)] = x0[this.I(i, j)] + a
                                * (x0[this.I(i, j + 1)] + x0[this.I(i, j - 1)]
                                + x0[this.I(i + 1, j)] + x0[this.I(i - 1, j)]
                                - c * x0[this.I(i, j)]);
                    }
                }
            }

            for (int i = 1; i < this.N - 1; i++) {
                for (int j = 1; j < this.N - 1; j++) {
                    x[this.I(i, j)] = extensionPhysicsOptions.get("useSOR")
                            ? (1 - this.w) * x[this.I(i, j)] + this.w * temp[this.I(i, j)]
                            : temp[this.I(i, j)];
                }
            }

            this.setBoundary(b, x);
        }
    }
}
