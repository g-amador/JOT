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
package jot.physics.fluids.eulerian;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.fluids.eulerian.linear_solvers.AbstractLinearSolver2D;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;

/**
 * Jos Stam style fluid solver with vorticity confinement and buoyancy force.
 *
 * @author Alexander McKenzie
 * @version 1.0
 * @since 12 March, 2004
 */
public class StableFluids2D extends AbstractEulerianFluidSolver2D {

    static final Logger log = getLogger("StableFluids2D");

    static {
        log.setLevel(OFF);
    }

    /**
     * Fluid simulation viscosity.
     */
    public float visc = 0.0f;

    /**
     * Fluid simulation diffusion rate.
     */
    public float diff = 0.0f;

    /**
     * Iterations to perform by the linear solver used in projection.
     */
    public int projection_iterations;

    /**
     * The pointer to the projection linear solver.
     */
    public AbstractLinearSolver2D linearSolverProjection;

    @Override
    public void setup(int n, AbstractLinearSolver2D linearSolverDiffusion) {
        this.n = n;
        this.size = n * n;
        this.diffusion_iterations = 20;
        this.projection_iterations = 20;
        this.linearSolverDiffusion = linearSolverDiffusion;
        this.linearSolverProjection = linearSolverDiffusion;

        this.reset();
    }

    @Override
    public void reset() {
        this.d = new float[this.size];
        this.dOld = new float[this.size];
        this.u = new float[this.size];
        this.uOld = new float[this.size];
        this.v = new float[this.size];
        this.vOld = new float[this.size];

        if (extensionPhysicsOptions.get("useVorticityConfinement")) {
            this.curl = new float[this.size];
        }
    }

    @Override
    public void update(float dt) {
        this.updateVelocity(dt);
        this.updateDensity(dt);
    }

    /**
     * Method to update the velocity field.
     *
     * @param dt simulation time step.
     */
    private void updateVelocity(float dt) {
        //add velocity that was input by mouse
        this.addSource(this.u, this.uOld, dt);
        this.addSource(this.v, this.vOld, dt);

        //Vorticity confinement only for liquids.
        if (extensionPhysicsOptions.get("useVorticityConfinement")) {
            //add in vorticity confinement force        
            this.vorticityConfinement(this.uOld, this.vOld);
            this.addSource(this.u, this.uOld, dt);
            this.addSource(this.v, this.vOld, dt);

            //add in buoyancy force
            this.buoyancy(this.vOld);
            this.addSource(this.v, this.vOld, dt);
        }

        //swapping arrays for economical mem use
        //and calculating diffusion in velocity.
        this.swapU();
        this.diffuse(0, this.u, this.uOld, this.visc, dt);

        this.swapV();
        this.diffuse(0, this.v, this.vOld, this.visc, dt);

        //we create an incompressible field
        //for more effective advection.
        this.project(this.u, this.v, this.uOld, this.vOld);

        this.swapU();
        this.swapV();

        //self advect velocities
        this.advect(1, this.u, this.uOld, this.uOld, this.vOld, dt);
        this.advect(2, this.v, this.vOld, this.uOld, this.vOld, dt);

        //make an incompressible field
        this.project(this.u, this.v, this.uOld, this.vOld);

        //clear all input velocities for next frame
        this.uOld = new float[this.size];
        this.vOld = new float[this.size];
    }

    /**
     * Method to update the all densities.
     *
     * @param dt simulation time step.
     */
    private void updateDensity(float dt) {
        //add density inputted by mouse
        this.addSource(this.d, this.dOld, dt);
        this.swapD();

        this.diffuse(0, this.d, this.dOld, this.diff, dt);
        this.swapD();

        this.advect(0, this.d, this.dOld, this.u, this.v, dt);

        //clear input density array for next frame
        this.dOld = new float[this.size];
    }

    /**
     * Calculate the buoyancy force as part of the velocity solver. Fbuoy =
     * -a*d*Y + b*(T-Tamb)*Y where Y = (0,1). The constants a and b are positive
     * with appropriate (physically meaningful) units. T is the temperature at
     * the current cell, Tamb is the average temperature of the fluid grid. The
     * density d provides a mass that counteracts the buoyancy force.
     *
     * In this simplified implementation, we say that the temperature is
     * synonymous with density (since smoke is *hot*) and because there are no
     * other heat sources we can just use the density field instead of a new,
     * separate temperature field.
     *
     * @param Fbuoy Array to store buoyancy force for each cell.
     *
     */
    private void buoyancy(float[] Fbuoy) {
        float Tamb = 0;
        float a = 0.000625f;
        float b = 0.025f;

        //sum all temperatures
        for (int i = 1; i < this.n - 1; i++) {
            for (int j = 1; j < this.n - 1; j++) {
                Tamb += this.d[this.I(i, j)];
            }
        }

        //get average temperature
        Tamb /= (this.n - 2) * (this.n - 2);

        //for each cell compute buoyancy force
        for (int i = 1; i < this.n - 1; i++) {
            for (int j = 1; j < this.n - 1; j++) {
                Fbuoy[this.I(i, j)] = a * this.d[this.I(i, j)] + -b * (this.d[this.I(i, j)] - Tamb);
            }
        }
    }

    /**
     * Calculate the curl at position (i, j) in the fluid grid. Physically this
     * represents the vortex strength at the cell. Computed as follows: w = (del
     * x U) where U is the velocity vector at (i, j).
     *
     * @param i The x index of the cell.
     * @param j The y index of the cell.
     *
     */
    private float curl(int i, int j) {
        float du_dy = (this.u[this.I(i, j + 1)] - this.u[this.I(i, j - 1)]) * 0.5f;
        float dv_dx = (this.v[this.I(i + 1, j)] - this.v[this.I(i - 1, j)]) * 0.5f;

        return du_dy - dv_dx;
    }

    /**
     * Calculate the vorticity confinement force for each cell in the fluid
     * grid. At a point (i,j), Fvc = N x w where w is the curl at (i,j) and N =
     * del |w| / |del |w||. N is the vector pointing to the vortex center, hence
     * we add force perpendicular to N.
     *
     * @param Fvc_x The array to store the x component of the vorticity
     * confinement force for each cell.
     * @param Fvc_y The array to store the y component of the vorticity
     * confinement force for each cell.
     *
     */
    private void vorticityConfinement(float[] Fvc_x, float[] Fvc_y) {
        float dw_dx, dw_dy;
        float length;
        float vorticity;

        //Calculate magnitude of curl(u,v) for each cell. (|w|)
        for (int i = 1; i < this.n - 1; i++) {
            for (int j = 1; j < this.n - 1; j++) {
                this.curl[this.I(i, j)] = abs(this.curl(i, j));
            }
        }

        for (int i = 2; i < this.n - 2; i++) {
            for (int j = 2; j < this.n - 2; j++) {

                //Find derivative of the magnitude (n = del |w|)
                dw_dx = (this.curl[this.I(i + 1, j)] - this.curl[this.I(i - 1, j)]) * 0.5f;
                dw_dy = (this.curl[this.I(i, j + 1)] - this.curl[this.I(i, j - 1)]) * 0.5f;

                //Calculate vector length. (|n|)
                //Add small factor to prevent divide by zeros.
                length = (float) sqrt(dw_dx * dw_dx + dw_dy * dw_dy) + 0.000001f;

                //N = ( n/|n| )
                dw_dx /= length;
                dw_dy /= length;

                vorticity = this.curl(i, j);

                //N x w
                Fvc_x[this.I(i, j)] = dw_dy * -vorticity;
                Fvc_y[this.I(i, j)] = dw_dx * vorticity;
            }
        }
    }

    /**
     * Add external sources.
     *
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     * @param dt simulation time step.
     */
    private void addSource(float[] x, float[] x0, float dt) {
        for (int i = 0; i < this.size; i++) {
            x[i] += dt * x0[i];
        }
    }

    /**
     * Calculate the input array after advection. We start with an input array
     * from the previous time step and an and output array. For all grid cells
     * we need to calculate for the next time step, we trace the cell's center
     * position backwards through the velocity field. Then we interpolate from
     * the grid of the previous time step and assign this value to the current
     * grid cell.
     *
     * @param b Flag specifying how to handle boundaries.
     * @param d Array to store the advected field.
     * @param d0 The array to advect.
     * @param du The x component of the velocity field.
     * @param dv The y component of the velocity field.
     * @param dt simulation time step.
     */
    private void advect(int b, float[] d, float[] d0, float[] du, float[] dv, float dt) {
        int i0, j0, i1, j1;
        float x, y, s0, t0, s1, t1, dt0;

        dt0 = dt * (this.n - 2);

        for (int i = 1; i < this.n - 1; i++) {
            for (int j = 1; j < this.n - 1; j++) {
                //go backwards through velocity field
                x = i - dt0 * du[this.I(i, j)];
                y = j - dt0 * dv[this.I(i, j)];

                //interpolate results
                if (x > this.n - 1.5) {
                    x = this.n - 1.5f;
                }
                if (x < 0.5) {
                    x = 0.5f;
                }

                i0 = (int) x;
                i1 = i0 + 1;

                if (y > this.n - 1.5) {
                    y = this.n - 1.5f;
                }
                if (y < 0.5) {
                    y = 0.5f;
                }

                j0 = (int) y;
                j1 = j0 + 1;

                s1 = x - i0;
                s0 = 1 - s1;
                t1 = y - j0;
                t0 = 1 - t1;

                d[this.I(i, j)] = s0 * (t0 * d0[this.I(i0, j0)] + t1 * d0[this.I(i0, j1)])
                        + s1 * (t0 * d0[this.I(i1, j0)] + t1 * d0[this.I(i1, j1)]);

            }
        }
        this.linearSolverDiffusion.setBoundary(b, d);
    }

    /**
     * Recalculate the input array with diffusion effects. Here we consider a
     * stable method of diffusion by finding the densities, which when diffused
     * backward in time yield the same densities we started with. This is
     * achieved through use of a linear solver to solve the sparse matrix built
     * from this linear system.
     *
     * @param b Flag to specify how boundaries should be handled.
     * @param x The array to store the results of the diffusion computation.
     * @param x0 The input array on which we should compute diffusion.
     * @param diff The factor of diffusion.
     * @param dt simulation time step.
     */
    private void diffuse(int b, float[] x, float[] x0, float diff, float dt) {
        float a = dt * diff * (this.n - 2) * (this.n - 2);

        this.linearSolverDiffusion.linearSolver(b, this.diffusion_iterations, x, x0, a,
                extensionPhysicsOptions.get("useMehrstellen")
                        ? 4 + 20 * a : 1 + 4 * a);
    }

    /**
     * Use project() to make the velocity a mass conserving, incompressible
     * field. Achieved through a Hodge decomposition. First we calculate the
     * divergence field of our velocity using the mean finite difference
     * approach, and apply the linear solver to compute the Poisson equation and
     * obtain a "height" field. Now we subtract the gradient of this field to
     * obtain our mass conserving velocity field.
     *
     * @param x The array in which the x component of our final velocity field
     * is stored.
     * @param y The array in which the y component of our final velocity field
     * is stored.
     * @param p A temporary array we can use in the computation.
     * @param div Another temporary array we use to hold the velocity divergence
     * field.
     *
     */
    private void project(float[] x, float[] y, float[] p, float[] div) {
        for (int i = 1; i < this.n - 1; i++) {
            for (int j = 1; j < this.n - 1; j++) {
                div[this.I(i, j)] = (x[this.I(i + 1, j)] - x[this.I(i - 1, j)]
                        + y[this.I(i, j + 1)] - y[this.I(i, j - 1)])
                        * -0.5f / (this.n - 2);
                p[this.I(i, j)] = 0;
            }
        }

        this.linearSolverProjection.setBoundary(0, div);
        this.linearSolverProjection.setBoundary(0, p);

        this.linearSolverProjection.linearSolver(0, this.projection_iterations, p, div, 1,
                extensionPhysicsOptions.get("useMehrstellen") ? 20 : 4);

        for (int i = 1; i < this.n - 1; i++) {
            for (int j = 1; j < this.n - 1; j++) {
                x[this.I(i, j)] -= 0.5f * (this.n - 2) * (p[this.I(i + 1, j)] - p[this.I(i - 1, j)]);
                y[this.I(i, j)] -= 0.5f * (this.n - 2) * (p[this.I(i, j + 1)] - p[this.I(i, j - 1)]);
            }
        }

        this.linearSolverProjection.setBoundary(1, x);
        this.linearSolverProjection.setBoundary(2, y);
    }

    /**
     * Utility method to swap old and new velocity u components arrays.
     */
    private void swapU() {
        this.tmp = this.u;
        this.u = this.uOld;
        this.uOld = this.tmp;
    }

    /**
     * Utility method to swap old and new velocity v components arrays.
     */
    private void swapV() {
        this.tmp = this.v;
        this.v = this.vOld;
        this.vOld = this.tmp;
    }

    /**
     * Utility method to swap old and new density arrays.
     */
    private void swapD() {
        this.tmp = this.d;
        this.d = this.dOld;
        this.dOld = this.tmp;
    }
}
