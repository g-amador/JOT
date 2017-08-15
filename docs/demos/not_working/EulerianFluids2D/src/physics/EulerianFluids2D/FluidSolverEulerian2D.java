package physics.EulerianFluids2D;

/*
 * FluidSolverEulerian2D.java
 * Alexander McKenzie
 * 12 March, 2004
 *
 */
/**
 * Jos Stam style fluid solver with vorticity confinement and buoyancy force.
 *
 * @author Alexander McKenzie
 * @version 1.0
 *
 */
public class FluidSolverEulerian2D {

    public int n, size;
    public float dt;
    public float visc = 0.0f;
    public float diff = 0.0f;
    public float[] tmp;
    public float[] d, dOld;
    public float[] u, uOld;
    public float[] v, vOld;
    public float[] curl;
    public LinearSolvers linearSolver;

    /**
     * Set the grid size and timestep.
     *
     * @param n grid cells width and length.
     * @param dt time step.
     */
    public void setup(int n, float dt) {
        this.n = n;
        this.dt = dt;
        size = (n + 2) * (n + 2);
        linearSolver = new LinearSolvers(n);

        reset();
    }

    /**
     * Reset the data structures. We use 1d arrays for speed.
     *
     */
    public void reset() {
        d = new float[size];
        dOld = new float[size];
        u = new float[size];
        uOld = new float[size];
        v = new float[size];
        vOld = new float[size];
        curl = new float[size];

        for (int i = 0; i < size; i++) {
            u[i] = uOld[i] = v[i] = vOld[i] = 0.0f;
            d[i] = dOld[i] = curl[i] = 0.0f;
        }

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
    public void buoyancy(float[] Fbuoy) {
        float Tamb = 0;
        float a = 0.000625f;
        float b = 0.025f;

        // sum all temperatures
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                Tamb += d[I(i, j)];
            }
        }

        // get average temperature
        Tamb /= (n * n);

        // for each cell compute buoyancy force
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                Fbuoy[I(i, j)] = a * d[I(i, j)] + -b * (d[I(i, j)] - Tamb);
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
    public float curl(int i, int j) {
        float du_dy = (u[I(i, j + 1)] - u[I(i, j - 1)]) * 0.5f;
        float dv_dx = (v[I(i + 1, j)] - v[I(i - 1, j)]) * 0.5f;

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
    public void vorticityConfinement(float[] Fvc_x, float[] Fvc_y) {
        float dw_dx, dw_dy;
        float length;
        float vorticity;

        // Calculate magnitude of curl(u,v) for each cell. (|w|)
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                curl[I(i, j)] = Math.abs(curl(i, j));
            }
        }

        for (int i = 2; i < n; i++) {
            for (int j = 2; j < n; j++) {

                // Find derivative of the magnitude (n = del |w|)
                dw_dx = (curl[I(i + 1, j)] - curl[I(i - 1, j)]) * 0.5f;
                dw_dy = (curl[I(i, j + 1)] - curl[I(i, j - 1)]) * 0.5f;

                // Calculate vector length. (|n|)
                // Add small factor to prevent divide by zeros.
                length = (float) Math.sqrt(dw_dx * dw_dx + dw_dy * dw_dy) + 0.000001f;

                // N = ( n/|n| )
                dw_dx /= length;
                dw_dy /= length;

                vorticity = curl(i, j);

                // N x w
                Fvc_x[I(i, j)] = dw_dy * -vorticity;
                Fvc_y[I(i, j)] = dw_dx * vorticity;
            }
        }
    }

    /**
     * The basic velocity solving routine as described by Stam.
     *
     */
    public void velocitySolver() {

        // add velocity that was input by mouse
        addSource(u, uOld);
        addSource(v, vOld);

        //TODO: (Eulerian 2D Stable Fluids Solver Extas) Comment vorticity confinement only for liquids.
        // add in vorticity confinement force        
        vorticityConfinement(uOld, vOld);
        addSource(u, uOld);
        addSource(v, vOld);

        //TODO: (Eulerian 2D Stable Fluids Solver Extas) Comment vorticity confinement only for liquids.
        // add in buoyancy force
        buoyancy(vOld);
        addSource(v, vOld);

        // swapping arrays for economical mem use
        // and calculating diffusion in velocity.
        swapU();
        diffuse(0, u, uOld, visc);

        swapV();
        diffuse(0, v, vOld, visc);

        // we create an incompressible field
        // for more effective advection.
        project(u, v, uOld, vOld);

        swapU();
        swapV();

        // self advect velocities
        advect(1, u, uOld, uOld, vOld);
        advect(2, v, vOld, uOld, vOld);

        // make an incompressible field
        project(u, v, uOld, vOld);

        // clear all input velocities for next frame
        for (int i = 0; i < size; i++) {
            uOld[i] = 0;
            vOld[i] = 0;
        }
    }

    /**
     * The basic density solving routine.
     *
     */
    public void densitySolver() {
        // add density inputted by mouse
        addSource(d, dOld);
        swapD();

        diffuse(0, d, dOld, diff);
        swapD();

        advect(0, d, dOld, u, v);

        // clear input density array for next frame
        for (int i = 0; i < size; i++) {
            dOld[i] = 0;
        }
    }

    /**
     * Add external sources.
     *
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     */
    private void addSource(float[] x, float[] x0) {
        //TODO: (Eulerian 2D Stable Fluids Solver) add sources, as explained at the link:
        //http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/GDC03.pdf
        for (int i = 0; i < size; i++) {
            x[i] += dt * x0[i];
        }
    }

    /**
     * Calculate the input array after advection. We start with an input array
     * from the previous timestep and an and output array. For all grid cells we
     * need to calculate for the next timestep, we trace the cell's center
     * position backwards through the velocity field. Then we interpolate from
     * the grid of the previous timestep and assign this value to the current
     * grid cell.
     *
     * @param b Flag specifying how to handle boundaries.
     * @param d Array to store the advected field.
     * @param d0 The array to advect.
     * @param du The x component of the velocity field.
     * @param dv The y component of the velocity field.
     *
     */
    private void advect(int b, float[] d, float[] d0, float[] du, float[] dv) {
        int i0, j0, i1, j1;
        float x, y, s0, t0, s1, t1, dt0;

        dt0 = dt * n;

        //TODO: (Eulerian 2D Stable Fluids Solver) Semi-Lagrangain Advection, as explained at the link:
        //http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/GDC03.pdf
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                // go backwards through velocity field
                x = i - dt0 * du[I(i, j)];
                y = j - dt0 * dv[I(i, j)];

                // interpolate results
                if (x > n + 0.5) {
                    x = n + 0.5f;
                }
                if (x < 0.5) {
                    x = 0.5f;
                }

                i0 = (int) x;
                i1 = i0 + 1;

                if (y > n + 0.5) {
                    y = n + 0.5f;
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

                d[I(i, j)] = s0 * (t0 * d0[I(i0, j0)] + t1 * d0[I(i0, j1)])
                        + s1 * (t0 * d0[I(i1, j0)] + t1 * d0[I(i1, j1)]);

            }
        }
        linearSolver.setBoundry(b, d);
    }

    /**
     * Recalculate the input array with diffusion effects. Here we consider a
     * stable method of diffusion by finding the densities, which when diffused
     * backward in time yield the same densities we started with. This is
     * achieved through use of a linear solver to solve the sparse matrix built
     * from this linear system.
     *
     * @param b Flag to specify how boundaries should be handled.
     * @param c The array to store the results of the diffusion computation.
     * @param c0 The input array on which we should compute diffusion.
     * @param diff The factor of diffusion.
     *
     */
    private void diffuse(int b, float[] c, float[] c0, float diff) {
        float a = dt * diff * n * n;
        linearSolver.linearSolver(b, c, c0, a, 1 + 4 * a);
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
    void project(float[] x, float[] y, float[] p, float[] div) {
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                div[I(i, j)] = (x[I(i + 1, j)] - x[I(i - 1, j)]
                        + y[I(i, j + 1)] - y[I(i, j - 1)])
                        * -0.5f / n;
                p[I(i, j)] = 0;
            }
        }

        linearSolver.setBoundry(0, div);
        linearSolver.setBoundry(0, p);

        linearSolver.linearSolver(0, p, div, 1, 4);

        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                x[I(i, j)] -= 0.5f * n * (p[I(i + 1, j)] - p[I(i - 1, j)]);
                y[I(i, j)] -= 0.5f * n * (p[I(i, j + 1)] - p[I(i, j - 1)]);
            }
        }

        linearSolver.setBoundry(1, x);
        linearSolver.setBoundry(2, y);
    }

    /**
     * Util method for swaping old and new velocity u components arrays.
     */
    private void swapU() {
        tmp = u;
        u = uOld;
        uOld = tmp;
    }

    /**
     * Util method for swaping old and new velocity v components arrays.
     */
    private void swapV() {
        tmp = v;
        v = vOld;
        vOld = tmp;
    }

    /**
     * Util method for swaping old and new density arrays.
     */
    private void swapD() {
        tmp = d;
        d = dOld;
        dOld = tmp;
    }

    /**
     * Util method for indexing 1d arrays.
     *
     * @param i index of a grid cell.
     * @param j index of a grid cell.
     * @return the i,j cell value.
     */
    public int I(int i, int j) {
        return i + (n + 2) * j;
    }
}