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
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;

/**
 * Class that implements the conjugate gradient linear solver algorithm.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ConjugateGradient2D extends AbstractLinearSolver2D {

    static final Logger log = getLogger("ConjugateGradient");

    static {
        log.setLevel(OFF);
    }

    /**
     * Tolerance
     */
    public double tol = 1e-3;

    /**
     * Constructor.
     *
     * @param n
     */
    public ConjugateGradient2D(int n) {
        super(n);
        if (extensionPhysicsOptions.get("useCGdebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * Constructor.
     *
     * @param n
     * @param tol
     */
    public ConjugateGradient2D(int n, double tol) {
        super(n);
        if (extensionPhysicsOptions.get("useCGdebug")) {
            log.setLevel(INFO);
        }
        this.tol = tol;
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
     * @param r
     * @param p
     * @param x
     * @param x0
     * @param a
     * @param c
     */
    private void set_r_p(float[] r, float[] p, float[] x, float[] x0, float a, float c) {
        for (int i = 1; i < this.N - 1; i++) {
            for (int j = 1; j < this.N - 1; j++) {
                r[this.I(i, j)] = x0[this.I(i, j)] - this.laplacian(i, j, x, a, c);
                p[this.I(i, j)] = r[this.I(i, j)];
            }
        }
    }

    /**
     *
     * @param v1
     * @param v2
     * @return
     */
    private float dotProd(float[] v1, float[] v2) {
        float sum = 0.0f;

        for (int i = 1; i < this.N - 1; i++) {
            for (int j = 1; j < this.N - 1; j++) {
                sum += v1[this.I(i, j)] * v2[this.I(i, j)];
            }
        }

        return sum;
    }

    /**
     *
     * @param q
     * @param p
     * @param a
     * @param c
     */
    private void update_q(float[] q, float[] p, float a, float c) {
        for (int i = 1; i < this.N - 1; i++) {
            for (int j = 1; j < this.N - 1; j++) {
                q[this.I(i, j)] = this.laplacian(i, j, p, a, c);
            }
        }
    }

    /**
     *
     * @param x
     * @param r
     * @param p
     * @param q
     * @param alpha
     */
    private void update_x_r(float[] x, float[] r, float[] p, float[] q, float alpha) {
        for (int i = 1; i < this.N - 1; i++) {
            for (int j = 1; j < this.N - 1; j++) {
                x[this.I(i, j)] += alpha * p[this.I(i, j)];
                r[this.I(i, j)] -= alpha * q[this.I(i, j)];
            }
        }
    }

    /**
     *
     * @param p
     * @param r
     * @param beta
     */
    private void update_p(float[] p, float[] r, float beta) {
        for (int i = 1; i < this.N - 1; i++) {
            for (int j = 1; j < this.N - 1; j++) {
                p[this.I(i, j)] = r[this.I(i, j)] + beta * p[this.I(i, j)];
            }
        }
    }

    @Override
    public void linearSolver(
            int b, int iters,
            float[] x, float[] x0,
            float a, float c) {
        int it;
        float rho, rho_old, rho0, alpha, beta;

        float[] r = new float[x.length];
        float[] p = new float[x.length];
        float[] q = new float[x.length];

        rho_old = alpha = beta = 0.0f;

        //r=b-Ax
        //p=r
        if (extensionPhysicsOptions.get("useCGdebug")) {
            log.info("r=b-Ax");
            log.info("p=r");
        }
        this.set_r_p(r, p, x, x0, a, c);

        if (extensionPhysicsOptions.get("useCGdebug")) {
            log.info("rho=r^T.r");
            log.info("rho0=rho\n");
        }
        rho = this.dotProd(r, p);    //rho=r^T.r
        rho0 = rho;             //rho0=rho

        if (extensionPhysicsOptions.get("useCGdebug")) {
            log.info(format("alpha :%f", alpha));
            log.info(format("beta :%f", beta));
            log.info(format("rho :%f", rho));
            log.info(format("rho0 :%f", rho0));
            log.info(format("rho_old :%f", rho_old));
            log.info(format("tol * tol * rho0 :%f\n", this.tol * this.tol * rho0));

            log.info(format("for %d iters do", iters));
            log.info("\tif (rho>(Tol*Tol*rho0))");
            log.info("\tq=Ap");
            log.info("\t(p^T.q)");
            log.info("\talpha=rho/(p^T.q)");
            log.info("\tx=x+alpha*p");
            log.info("\tr=r-alpha*q");
            log.info("\trho_old=rho");
            log.info("\trho=r^T.r");
            log.info("\tbeta=rho/rho_old");
            log.info("\tp=r+beta*p\n");
        }

        for (it = 0; it < iters; it++) {
            if ((rho != 0.0f) && (rho > (this.tol * this.tol * rho0))) {
                //q=Ap
                this.update_q(q, p, a, c);

                //(p^T.q)
                alpha = this.dotProd(p, q);

                //alpha=rho/(p^T.q)
                if (alpha != 0.0f) {
                    alpha = rho / alpha;
                }

                //x=x+alpha*p
                //r=r-alpha*q
                this.update_x_r(x, r, p, q, alpha);

                //rho_old=rho
                //rho=r^T.r
                rho_old = rho;
                rho = this.dotProd(r, r);

                //beta=rho/rho_old
                beta = rho_old == 0.0f ? 0.0f : rho / rho_old;

                //p=r+beta*p
                this.update_p(p, r, beta);

                this.setBoundary(b, x);
            } else {
                break;
            }
        }

        if (extensionPhysicsOptions.get("useCGdebug")) {
            log.info(format("alpha: %f", alpha));
            log.info(format("beta: %f", beta));
            log.info(format("rho: %f", rho));
            log.info(format("rho0: %f", rho0));
            log.info(format("rho_old: %f", rho_old));
            log.info(format("iters donne %d\n", it++));
        }
    }
}
