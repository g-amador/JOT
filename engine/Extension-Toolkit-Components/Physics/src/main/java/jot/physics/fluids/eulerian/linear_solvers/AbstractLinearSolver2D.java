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

import static java.lang.String.format;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;

/**
 * Abstract class that each linear solver must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractLinearSolver2D {

    private static final Logger log = getLogger("AbstractLinearSolver");

    static {
        log.setLevel(OFF);
    }

    //#define _ND (0x03ffffff) //not Dirichlet cell
    //#define _A (0x00000000) //air cell (i.e., Dirichlet cell)
    //#define _MB (0x80000000) //moving bound cell (i.e., Neumann cell)
    //#define _SB (0x40000000) //static bound cell (i.e., Neumann cell)
    //#define _SL1 (0x20000000) //surface of liquid 1 cell (i.e., Interior cell)
    //#define _SL2 (0x10000000) //surface of liquid 2 cell (i.e., Interior cell)
    //#define _LL1 (0x08000000) //liquid 1 cell (i.e., Interior cell)
    //#define _LL2 (0x04000000) //liquid 2 cell (i.e., Interior cell)
    protected int n;
    protected int N;

    /**
     * Constructor.
     *
     * @param n the width and length of the 2d matrix-
     */
    public AbstractLinearSolver2D(int n) {
        this.n = n;
        this.N = n;
    }

    /**
     * Specifies boundary conditions.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param x the velocity or density array to enforce boundaries
     */
    public void setBoundary(int b, float[] x) {
        if (extensionPhysicsOptions.get("useBounds")
                && extensionPhysicsOptions.get("useStableFluids")) {
            //simulation external boundaries
            for (int i = 1; i < this.N - 1; i++) {
                x[this.I(0, i)] = b == 1 ? -x[this.I(1, i)] : x[this.I(1, i)];
                x[this.I(this.N - 1, i)] = b == 1 ? -x[this.I(this.N - 2, i)] : x[this.I(this.N - 2, i)];
                x[this.I(i, 0)] = b == 2 ? -x[this.I(i, 1)] : x[this.I(i, 1)];
                x[this.I(i, this.N - 1)] = b == 2 ? -x[this.I(i, this.N - 2)] : x[this.I(i, this.N - 2)];
            }

            //simulation corner boundaries
            x[this.I(0, 0)] = 0.5f * (x[this.I(1, 0)] + x[this.I(0, 1)]);
            x[this.I(0, this.N - 1)] = 0.5f * (x[this.I(1, this.N - 1)] + x[this.I(0, this.N - 2)]);
            x[this.I(this.N - 1, 0)] = 0.5f * (x[this.I(this.N - 2, 0)] + x[this.I(this.N - 1, 1)]);
            x[this.I(this.N - 1, this.N - 1)] = 0.5f * (x[this.I(this.N - 2, this.N - 1)] + x[this.I(this.N - 1, this.N - 2)]);
        }

        //simulation internal and moving boundaries
//        for (int i = 1; i <= N; i++) {
//            for (int j = 1; j <= N; j++) {
//                if (((g -> cell_type[_IX(i, j, k)]) & _SB) == _SB) {
//                    x[_IX(i, j, k)] = 0;
//                }
//
//                if (((g -> cell_type[_IX(i, j, k)]) & _MB) == _MB) {
//                    x[_IX(i, j, k)] = (b == 1 ? (s_obj -> vel.x) : x[_IX(i, j, k)]);
//                    x[_IX(i, j, k)] = (b == 2 ? (s_obj -> vel.y) : x[_IX(i, j, k)]);
//                    x[_IX(i, j, k)] = (b == 3 ? (s_obj -> vel.z) : x[_IX(i, j, k)]);
//                    x[_IX(i, j, k)] = (b == 0 ? 0 : x[_IX(i, j, k)]);
//                }
//            }
//        }
    }

    /**
     * Utility method for indexing 1d arrays.
     *
     * @param i index of a grid cell.
     * @param j index of a grid cell.
     * @return the i,j cell value.
     */
    public int I(int i, int j) {
        return i + this.N * j;
        //return i + n * j;
    }

    /**
     * Iterative linear system solver to use, e.g., Jacobi, Gauss-Seidel, SOR,
     * Conjugate Gradient, Multigrid, etc.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param iters maximum of iterations to perform.
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     * @param a
     * @param c
     */
    public abstract void linearSolver(
            int b, int iters,
            float[] x, float[] x0,
            float a, float c);

    /**
     * Print to terminal all values of array x in 2d matrix form.
     *
     * @param x
     */
    public void show(float[] x) {
        for (int i = 0; i < this.N; i++) {
            for (int j = 0; j < this.N; j++) {
                log.info(format("%f ", x[this.I(i, j)]));
            }
            log.info("");
        }
        log.info("");
    }
}
