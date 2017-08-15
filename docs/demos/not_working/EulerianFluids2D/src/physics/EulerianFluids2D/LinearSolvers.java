package physics.EulerianFluids2D;

/**
 * Class that implements linear solvers algorithms.
 *
 * @author G. Amador & A. Gomes
 */
public class LinearSolvers implements LinearSolver {

    public enum LinearSolver {

        JACOBI, GAUSS_SEIDEL, SOR, CONJUGATE_GRADIENT, MULTIGRID;
    }
    private LinearSolver linearSolverLogic;
    private int n;

    public LinearSolvers(int n) {
        this.n = n;
        linearSolverLogic = LinearSolver.GAUSS_SEIDEL;
    }

    @Override
    public void setLinearSolver(String logic) {
        if (logic.toLowerCase().contains("jacobi")) {
            System.out.println("Using Jacobi linear solver!");
            linearSolverLogic = LinearSolver.JACOBI;
        } else if (logic.toLowerCase().contains("gauss_seidel")) {
            System.out.println("Using Gauss-Seidel linear solver!");
            linearSolverLogic = LinearSolver.GAUSS_SEIDEL;
        } else if (logic.toLowerCase().contains("sor")) {
            System.out.println("Using Successive Over Relaxation linear solver!");
            linearSolverLogic = LinearSolver.SOR;
        } else if (logic.toLowerCase().contains("conjugate_gradient")) {
            System.out.println("Using Conjugate Gradient linear solver!");
            linearSolverLogic = LinearSolver.CONJUGATE_GRADIENT;
        } else if (logic.toLowerCase().contains("multigrid")) {
            System.out.println("Using Multigrid linear solver!");
            linearSolverLogic = LinearSolver.MULTIGRID;
        } else {
            System.out.println("No valid linear solver selected, using default!");
        }
    }

    @Override
    public void linearSolver(int b, float[] x, float[] x0, float a, float c) {
        switch (linearSolverLogic) {
            case JACOBI:
                jacobi(b, x, x0, a, c);
                break;
            case GAUSS_SEIDEL:
                gauss_seidel(b, x, x0, a, c);
                break;
            case SOR:
                sor(b, x, x0, a, c);
                break;
            case CONJUGATE_GRADIENT:
                conjugate_gradient(b, x, x0, a, c);
                break;
            case MULTIGRID:
                multigrid(b, x, x0, a, c);
                break;
            default:
                throw new AssertionError(linearSolverLogic.name());
        }
    }

    /**
     * Jacobi linear solver.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     * @param a
     * @param c
     */
    private void jacobi(int b, float[] x, float[] x0, float a, float c) {
        //TODO: (Sparse Linear System Solver) implement Jacobi linear solver, as explained at the link:
        //http://ocw.mit.edu/courses/mechanical-engineering/2-29-numerical-fluid-mechanics-fall-2011/lecture-notes/MIT2_29F11_lect_10.pdf       
    }

    /**
     * Gauss-Seidel linear solver.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     * @param a
     * @param c
     */
    private void gauss_seidel(int b, float[] x, float[] x0, float a, float c) {
        //TODO: (Sparse Linear System Solver) Gauss-Sidel linear solver, as explained at the link:
        //http://www.dgp.toronto.edu/people/stam/reality/Research/pdf/GDC03.pdf
        for (int k = 0; k < 20; k++) {
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= n; j++) {
                    x[I(i, j)] = (a * (x[I(i - 1, j)] + x[I(i + 1, j)]
                            + x[I(i, j - 1)] + x[I(i, j + 1)])
                            + x0[I(i, j)]) / c;
                }
            }
            setBoundry(b, x);
        }
    }

    /**
     * Successive Over Relaxation (SOR) linear solver.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     * @param a
     * @param c
     */
    private void sor(int b, float[] x, float[] x0, float a, float c) {
        //TODO: (Sparse Linear System Solver) implement Successive Over Relaxation (SOR) linear solver, as explained at the link:
        //http://ocw.mit.edu/courses/mechanical-engineering/2-29-numerical-fluid-mechanics-fall-2011/lecture-notes/MIT2_29F11_lect_10.pdf       
    }

    /**
     * Conjugate Gradient linear solver.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     * @param a
     * @param c
     */
    private void conjugate_gradient(int b, float[] x, float[] x0, float a, float c) {
        //TODO: (Project) implement Conjugate Gradient linear solver, as explained at the link:
        //https://www.cs.cmu.edu/~quake-papers/painless-conjugate-gradient.pdf
    }

    /**
     * Multigrid linear solver.
     *
     * @param b orientation of the velocity or density component i.e., (x,y).
     * @param x a vector of velocities or densities.
     * @param x0 the old value of velocities and densities per grid cell.
     * @param a
     * @param c
     */
    private void multigrid(int b, float[] x, float[] x0, float a, float c) {
        //TODO: (Project) implement multigrid linear solver.
    }

    @Override
    public void setBoundry(int b, float[] x) {
        for (int i = 1; i <= n; i++) {
            x[I(0, i)] = b == 1 ? -x[I(1, i)] : x[I(1, i)];
            x[I(n + 1, i)] = b == 1 ? -x[I(n, i)] : x[I(n, i)];
            x[I(i, 0)] = b == 2 ? -x[I(i, 1)] : x[I(i, 1)];
            x[I(i, n + 1)] = b == 2 ? -x[I(i, n)] : x[I(i, n)];
        }

        x[I(0, 0)] = 0.5f * (x[I(1, 0)] + x[I(0, 1)]);
        x[I(0, n + 1)] = 0.5f * (x[I(1, n + 1)] + x[I(0, n)]);
        x[I(n + 1, 0)] = 0.5f * (x[I(n, 0)] + x[I(n + 1, 1)]);
        x[I(n + 1, n + 1)] = 0.5f * (x[I(n, n + 1)] + x[I(n + 1, n)]);
    }

    /**
     * Util method for indexing 1d arrays.
     *
     * @param i index of a grid cell.
     * @param j index of a grid cell.
     * @return the i,j cell value.
     */
    private int I(int i, int j) {
        return i + (n + 2) * j;
    }
}
