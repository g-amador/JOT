package physics.EulerianFluids2D;

/**
 * Interface that each linear solver must implement. Alternatively one massive
 * class may be implemented instead.
 *
 * @author G. Amador & A. Gomes
 */
public interface LinearSolver {

    /**
     * Chose/change the linear solver logic to use.
     *
     * @param logic the linear solver logic to use.
     */
    public void setLinearSolver(String logic);

    /**
     * Iterative linear system solver, e.g., Jacobi, Gauss-Seidel, SOR, Conjugate
     * Gradient, Multigrid, etc.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     * @param a
     * @param c
     */
    public void linearSolver(int b, float[] x, float[] x0, float a, float c);

    /**
     * Specifies boundary conditions.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param x the velocity or density array to enforce boundaries
     */
    public void setBoundry(int b, float[] x);
}
