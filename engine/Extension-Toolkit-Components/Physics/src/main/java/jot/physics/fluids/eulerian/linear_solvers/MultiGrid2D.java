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

import static java.lang.Math.pow;
import static java.lang.String.format;
import java.util.ArrayList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;

/**
 * Class that implements the multigrid linear solver algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class MultiGrid2D extends AbstractLinearSolver2D {

    static final Logger log = getLogger("MultiGrid");

    static {
        log.setLevel(OFF);
    }

    private final AbstractLinearSolver2D linearSolver;

    /**
     * Multigrid levels.
     */
    public int levels = 3;

    /**
     * Constructor.
     *
     * @param n
     * @param linearSolver
     */
    public MultiGrid2D(int n, AbstractLinearSolver2D linearSolver) {
        super(n);
        if (extensionPhysicsOptions.get("useMGdebug")) {
            log.setLevel(INFO);
        }
        this.linearSolver = linearSolver;
    }

    /**
     * Constructor.
     *
     * @param n
     * @param levels
     * @param linearSolver
     */
    public MultiGrid2D(int n, int levels, AbstractLinearSolver2D linearSolver) {
        super(n);
        if (extensionPhysicsOptions.get("useMGdebug")) {
            log.setLevel(INFO);
        }
        this.levels = levels;
        this.linearSolver = linearSolver;
    }

    /**
     *
     * @param i
     * @param j
     * @param x
     * @param a
     * @param c
     * @return
     */
    private float laplacian(int i, int j, float[] x, float a, float c) {
        return ((x[this.I(i, j)] * c) - a * (x[this.I(i - 1, j)] + x[this.I(i + 1, j)]
                + x[this.I(i, j - 1)] + x[this.I(i, j + 1)]));
    }

    /**
     *
     * @param b
     * @param iters
     * @param x
     * @param x0
     * @param a
     * @param c
     */
    private void smoth(int b, int iters, float[] x, float[] x0, float a, float c) {
        this.linearSolver.linearSolver(b, iters, x, x0, a, c);
    }

    /**
     *
     * @param x
     * @param x0
     * @param r
     * @param a
     * @param c
     */
    private void residual(float[] r, float[] x, float[] x0, float a, float c) {
        for (int i = 1; i < this.N - 1; i++) {
            for (int j = 1; j < this.N - 1; j++) {
                r[this.I(i, j)] = x0[this.I(i, j)] - this.laplacian(i, j, x, a, c);
            }
        }
    }

    /**
     *
     * @param r
     * @param x
     * @param x0
     */
    private void restrict(float[] r, float[] x0) {
        int i, j, o, p;

        for (o = 1, i = 1; i < this.N - 1; i += 2, o++) {
            for (p = 1, j = 1; j < this.N - 1; j += 2, p++) {
                x0[o + ((this.N / 2) + 2) * p] = !extensionPhysicsOptions.get("useFullWeightedMG")
                        //5 points stencil half weighted
                        //   |0 1 0|
                        //1/8|1 4 1|
                        //   |0 1 0|
                        ? 0.5f * r[this.I(i, j)] + 0.125f
                        * (r[this.I(i - 1, j)] + r[this.I(i + 1, j)]
                        + r[this.I(i, j - 1)] + r[this.I(i, j + 1)])
                        //9 points stencil full weighted
                        //    |1 2 1|
                        //1/16|2 4 2|
                        //    |1 2 1|	
                        : 0.25f * r[this.I(i, j)] + 0.125f
                        * (r[this.I(i - 1, j)] + r[this.I(i + 1, j)]
                        + r[this.I(i, j - 1)] + r[this.I(i, j + 1)]) + 0.0625f
                        * (r[this.I(i - 1, j - 1)] + r[this.I(i + 1, j - 1)]
                        + r[this.I(i - 1, j + 1)] + r[this.I(i + 1, j + 1)]);
            }
        }

//        if (useLinearSolversBounds) {
//            //if all cell neighbours are moving bounds cell the coarser grid cell is marked as moving bound
//            if ((((g -> cell_type[_IX(i, j, k)]) & _MB) == _MB)
//                    && (((g -> cell_type[_IX(i - 1, j, k)]) & _MB) == _MB)
//                    && (((g -> cell_type[_IX(i, j - 1, k)]) & _MB) == _MB)
//                    && (((g -> cell_type[_IX(i, j, k - 1)]) & _MB) == _MB)
//                    && (((g -> cell_type[_IX(i - 1, j - 1, k)]) & _MB) == _MB)
//                    && (((g -> cell_type[_IX(i - 1, j, k - 1)]) & _MB) == _MB)
//                    && (((g -> cell_type[_IX(i, j - 1, k - 1)]) & _MB) == _MB)
//                    && (((g -> cell_type[_IX(i - 1, j - 1, k - 1)]) & _MB) == _MB)) {
//                (g -> cell_type[_IX(l, m, n)]) = _MB;
//            }
//
//            //if all cell neighbours are static bounds cells the coarser grid cell is marked as static bound
//            if ((((g -> cell_type[_IX(i, j, k)]) & _SB) == _SB)
//                    && (((g -> cell_type[_IX(i - 1, j, k)]) & _SB) == _SB)
//                    && (((g -> cell_type[_IX(i, j - 1, k)]) & _SB) == _SB)
//                    && (((g -> cell_type[_IX(i, j, k - 1)]) & _SB) == _SB)
//                    && (((g -> cell_type[_IX(i - 1, j - 1, k)]) & _SB) == _SB)
//                    && (((g -> cell_type[_IX(i - 1, j, k - 1)]) & _SB) == _SB)
//                    && (((g -> cell_type[_IX(i, j - 1, k - 1)]) & _SB) == _SB)
//                    && (((g -> cell_type[_IX(i - 1, j - 1, k - 1)]) & _SB) == _SB)) {
//                (g -> cell_type[_IX(l, m, n)]) = _SB;
//            }
//
//            //if at least one of the cell's neighbours is air the coarser grid cell is air, otherwise its interior cell
//            if ((((g -> cell_type[_IX(l, m, n)]) & _MB) == _MB)
//                    && (((g -> cell_type[_IX(l, m, n)]) & _SB) == _SB)
//                    && ((g -> cell_type[_IX(l, m, n)]) & _ND) != _ND) {
//                (g -> cell_type[_IX(l, m, n)]) = _A;
//            } else {
//                (g -> cell_type[_IX(l, m, n)]) |= _A;
//            }
//        }
    }

    /**
     *
     * @param x
     */
    private void prolong(float[] x, float[] x0) {
        int i, j, o, p;

        for (o = this.N - 2, i = this.N - 2; i > 0; i -= 2, o--) {
            for (p = this.N - 2, j = this.N - 2; j > 0; j -= 2, p--) {
                //stencil
                //|1/4 1/2 1/4|
                //|1/2  1  1/2|
                //|1/4 1/2 1/4|
                x[this.I(i, j)] += x0[o + ((this.N / 2) + 2) * p];

                x[this.I(i - 1, j)] += (0.5f * x0[o + ((this.N / 2) + 2) * p]);
                x[this.I(i + 1, j)] += (0.5f * x0[o + ((this.N / 2) + 2) * p]);
                x[this.I(i, j - 1)] += (0.5f * x0[o + ((this.N / 2) + 2) * p]);
                x[this.I(i, j + 1)] += (0.5f * x0[o + ((this.N / 2) + 2) * p]);

                x[this.I(i - 1, j - 1)] += (0.25f * x0[o + ((this.N / 2) + 2) * p]);
                x[this.I(i - 1, j + 1)] += (0.25f * x0[o + ((this.N / 2) + 2) * p]);
                x[this.I(i + 1, j - 1)] += (0.25f * x0[o + ((this.N / 2) + 2) * p]);
                x[this.I(i + 1, j + 1)] += (0.25f * x0[o + ((this.N / 2) + 2) * p]);
            }
        }

//        if (useLinearSolversBounds) {
//            //if a coarser grid cell is marked as moving bound the finer grid neighbours cell's are moving bounds 
//            if (((g -> cell_type[_IX(l, m, n)]) & _MB) == _MB) {
//                (g -> cell_type[_IX(i, j, k)]) = _MB;
//                (g -> cell_type[_IX(i - 1, j, k)]) = _MB;
//                (g -> cell_type[_IX(i, j - 1, k)]) = _MB;
//                (g -> cell_type[_IX(i, j, k - 1)]) = _MB;
//                (g -> cell_type[_IX(i - 1, j - 1, k)]) = _MB;
//                (g -> cell_type[_IX(i - 1, j, k - 1)]) = _MB;
//                (g -> cell_type[_IX(i, j - 1, k - 1)]) = _MB;
//                (g -> cell_type[_IX(i - 1, j - 1, k - 1)]) = _MB;
//            }
//
//            //if a coarser grid cell is marked as static bound all the finer grid neighbours cell's are static bounds 
//            if (((g -> cell_type[_IX(l, m, n)]) & _SB) == _SB) {
//                (g -> cell_type[_IX(i, j, k)]) = _SB;
//                (g -> cell_type[_IX(i - 1, j, k)]) = _SB;
//                (g -> cell_type[_IX(i, j - 1, k)]) = _SB;
//                (g -> cell_type[_IX(i, j, k - 1)]) = _SB;
//                (g -> cell_type[_IX(i - 1, j - 1, k)]) = _SB;
//                (g -> cell_type[_IX(i - 1, j, k - 1)]) = _SB;
//                (g -> cell_type[_IX(i, j - 1, k - 1)]) = _SB;
//                (g -> cell_type[_IX(i - 1, j - 1, k - 1)]) = _SB;
//            }
//
//            //if a coarser grid cell is air all the finer grid cell's neighbours are air, otherwise its their interior
//            if (((g -> cell_type[_IX(l, m, n)]) & _A) == _A) {
//                (g -> cell_type[_IX(i, j, k)]) = _A;
//                (g -> cell_type[_IX(i - 1, j, k)]) = _A;
//                (g -> cell_type[_IX(i, j - 1, k)]) = _A;
//                (g -> cell_type[_IX(i, j, k - 1)]) = _A;
//                (g -> cell_type[_IX(i - 1, j - 1, k)]) = _A;
//                (g -> cell_type[_IX(i - 1, j, k - 1)]) = _A;
//                (g -> cell_type[_IX(i, j - 1, k - 1)]) = _A;
//                (g -> cell_type[_IX(i - 1, j - 1, k - 1)]) = _A;
//            } else {
//                (g -> cell_type[_IX(i, j, k)]) |= _A;
//                (g -> cell_type[_IX(i - 1, j, k)]) |= _A;
//                (g -> cell_type[_IX(i, j - 1, k)]) |= _A;
//                (g -> cell_type[_IX(i, j, k - 1)]) |= _A;
//                (g -> cell_type[_IX(i - 1, j - 1, k)]) |= _A;
//                (g -> cell_type[_IX(i - 1, j, k - 1)]) |= _A;
//                (g -> cell_type[_IX(i, j - 1, k - 1)]) |= _A;
//                (g -> cell_type[_IX(i - 1, j - 1, k - 1)]) |= _A;
//            }
//        }
    }

    @Override
    public void linearSolver(
            int b, int iters,
            float[] x, float[] x0,
            float a, float c) {
        float[] r;
        ArrayList<float[]> x_old = new ArrayList<>();
        ArrayList<float[]> x0_old = new ArrayList<>();
        x_old.add(x);
        x0_old.add(x0);

        if (extensionPhysicsOptions.get("useMGdebug")) {
            log.info(format("for level 0 to level %d", (this.levels - 2)));
            log.info("\tA^(h)v^(h)=f^(h)");
            log.info("\tr^(h)=f^(h)-A^(h)v^(h)");
            log.info("\tr^(2h)=R(r^(h))");
            log.info("\tX^(2h)=0\n");
        }

        for (int level = 0; level < (this.levels - 1); level++) {
            this.N = this.n / (int) pow(2.0f, level);

            //A^(h)v^(h)=f^(h)
            this.smoth(b, iters, x_old.get(level), x0_old.get(level), a, c);
            if (extensionPhysicsOptions.get("useMGdebug")) {
                log.info(format("N: %d", this.N));
                log.info(format("level %d", level));
                log.info("x");
                this.show(x_old.get(level));
                log.info("x0");
                this.show(x0_old.get(level));
            }

            //r^(h)=f^(h)-A^(h)v^(h)
            r = new float[this.N * this.N];
            this.residual(r, x_old.get(level), x0_old.get(level), a, c);
            if (extensionPhysicsOptions.get("useMGdebug")) {
                log.info("r");
                this.show(r);
            }

            //r^(2h)=R(r^(h))  
            int next_level_n = this.n / (int) pow(2.0f, level + 1);
            int size = (next_level_n + 2) * (next_level_n + 2);
            x0_old.add(new float[size]);
            this.restrict(r, x0_old.get(level + 1));
            if (extensionPhysicsOptions.get("useMGdebug")) {
                this.N /= 2;
                log.info("x0");
                this.show(x0_old.get(level + 1));
                this.N *= 2;
            }

            //X^(2h)=0
            x_old.add(new float[size]);
        }

        if (extensionPhysicsOptions.get("useMGdebug")) {
            log.info(format("\nSolve for the coarser level %d\n", (this.levels - 1)));
        }

        this.N = this.n / (int) pow(2.0f, this.levels - 1);

        //A^(2h)e^(2h)=r^(2h)
        this.smoth(b, iters, x_old.get(this.levels - 1), x0_old.get(this.levels - 1), a, c);
        if (extensionPhysicsOptions.get("useMGdebug")) {
            log.info(format("N: %d", this.N));
            log.info(format("level %d", (this.levels - 1)));
            log.info("x");
            this.show(x_old.get(this.levels - 1));
            log.info("x0");
            this.show(x0_old.get(this.levels - 1));

            log.info(format("\nfor level %d to level 0 do", (this.levels - 2)));
            log.info("\tv^(h)=v^(h)+P(e^(2h))");
            log.info("\tA^(h)v^(h)=f^(h)\n");
        }

        for (int level = (this.levels - 2); level >= 0; level--) {
            this.N = this.n / (int) pow(2.0f, level);

            if (extensionPhysicsOptions.get("useMGdebug")) {
                log.info(format("N: %d", this.N));
                log.info(format("level %d", level));
                log.info("x");
                this.show(x_old.get(level));
                log.info("x0");
                this.show(x0_old.get(level));
            }

            //v^(h)=v^(h)+P(e^(2h))
            this.prolong(x_old.get(level), x0_old.get(level));

            //A^(h)v^(h)=f^(h)
            if (level == 0) {
                this.smoth(b, iters, x, x0, a, c);
            } else {
                this.smoth(b, iters, x_old.get(level), x0_old.get(level), a, c);
            }
        }
    }

}
