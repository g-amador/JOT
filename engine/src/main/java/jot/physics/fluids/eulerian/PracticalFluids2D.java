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
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.System.arraycopy;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.fluids.eulerian.linear_solvers.AbstractLinearSolver2D;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;

/**
 * Port of Kaboom fluid simulator from intel
 * http://software.intel.com/en-us/articles/multi-threaded-fluid-simulation-for-games
 * based on Mick West 2006 style fluid solver. http://mickwest.com/
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class PracticalFluids2D extends AbstractEulerianFluidSolver2D {

    static final Logger log = getLogger("PracticalFluids2D");

    static {
        log.setLevel(OFF);
    }

    //Boundary ranges to test proximity of a number to zero
    float ZERO_MAX = .00001f;
    float ZERO_MIN = -.00001f;

    /**
     * New and Old pressure.
     */
    public float[] pressure, pressureOld;

    /**
     * New and Old heat.
     */
    public float[] heat, heatOld;

    //public float VelX;
    //public float VelY;
    /**
     * Velocity diffusion rate.
     */
    public float velocity_diffusion = 1.0f; //0.0f; //1.0f; //3.0f; //3.5f; //3.5f works nicely
    //With no pressure diffusion, waves of pressure keep moving
    //But only if pressure is advected ahead of velocity

    /**
     * Pressure diffusion rate.
     */
    public float pressure_diffusion = 10.0f; //0.0f; //3.5f; //6.0f; //10.0f; //too big a diffusion scale creates innacuracies, but it's fine around here

    /**
     * Heat diffusion rate.
     */
    public float heat_diffusion = 0.0f; //0.0f; //3.0f;

    /**
     * Density diffusion rate.
     */
    public float d_diffusion = 0.0f; //0.0f; //0.5f; //1.0f; //3.0f;

    /**
     * Pressure acceleration. Larger values ({@literal >}10) are more realistic
     * (like water) values that are too large lead to chaotic waves.
     */
    public float pressure_acc = 2.0f; //2.0 worked well for smoke-like stuff

    /**
     * Vorticity value.
     */
    public float vorticity = 0.0f; //0.03f;

    //heat follows d in advection, but is a seperate scalar value
    /**
     * Density force.
     */
    public float d_force = 0.0f; //0.0f; //0.1f; //1.5f;

    /**
     * Heat force.
     */
    public float heat_force = -0.1f; //0.0f; //0.1f; //-0.1f;

    //public float d_decay = 0.0f;
    /**
     * Heat decay rate.
     */
    public float heat_decay = 0.0f;

    /**
     * Velocity decay rate.
     */
    public float velocity_decay = 0.0f; //viscosity
    //high d advection allows fast moving nice swirlys

    /**
     * Density advection.
     */
    public float d_advection = 150.0f;

//seems nice if both velocity and pressure at same value, which makes sense
    /**
     * Velocity advection.
     */
    public float velocity_advection = 150.0f;

    /**
     * Pressure advection.
     */
    public float pressure_advection = 150.0f;  //130, lag behind, to allow vel to dissipate???

    /**
     * Heat advection.
     */
    public float heat_advection = 150.0f;

    @Override
    public void setup(int n, AbstractLinearSolver2D linearSolverDiffusion) {
        this.n = n;
        this.size = n * n;
        this.diffusion_iterations = 1;
        this.linearSolverDiffusion = linearSolverDiffusion;

        this.reset();
    }

    @Override
    public void reset() {
        this.u = new float[this.size];
        this.uOld = new float[this.size];
        this.v = new float[this.size];
        this.vOld = new float[this.size];
        this.pressure = new float[this.size];
        this.pressureOld = new float[this.size];
        this.d = new float[this.size];
        this.dOld = new float[this.size];
        this.heat = new float[this.size];
        this.heatOld = new float[this.size];
        this.curl = new float[this.size];

        //SetField(pressureOld, 1.0f); //should be 1.0f, lower is more like gas, higher less compressible)
        //SetField(heatOld, 1.0f); //should be 1.0f, lower is more like gas, higher less compressible)
    }

    @Override
    public void update(float dt) {
        this.updateDiffusion(dt);
        this.updateForces(dt);
        this.updateAdvection(dt);
    }

    /**
     * Apply diffusion across the grids.
     *
     * @param dt simulation time step.
     */
    private void updateDiffusion(float dt) {
        //Diffusion of Velocity
        if (this.velocity_diffusion != 0.0f) {
            for (int i = 0; i < this.diffusion_iterations; i++) {
                this.Diffusion(this.uOld, this.u, this.velocity_diffusion / this.diffusion_iterations, dt);
                this.swapU();
                this.Diffusion(this.vOld, this.v, this.velocity_diffusion / this.diffusion_iterations, dt);
                this.swapV();
            }
        }

        //Diffusion of Pressure        
        if (this.pressure_diffusion != 0.0f) {
            for (int i = 0; i < this.diffusion_iterations; i++) {
                this.Diffusion(this.pressureOld, this.pressure, this.pressure_diffusion / this.diffusion_iterations, dt);
                this.swapPressure();
            }
        }

        //Diffusion of Heat
        if (this.heat_diffusion != 0.0f) {
            for (int i = 0; i < this.diffusion_iterations; i++) {
                this.Diffusion(this.heatOld, this.heat, this.heat_diffusion / this.diffusion_iterations, dt);
                this.swapHeat();
            }
        }

        //Diffusion of Ink
        if (this.d_diffusion != 0.0f) {
            for (int i = 0; i < this.diffusion_iterations; i++) {
                this.Diffusion(this.dOld, this.d, this.d_diffusion / this.diffusion_iterations, dt);
                this.swapD();
            }
        }
    }

    /**
     * Apply forces across the grids.
     *
     * @param dt simulation time step.
     */
    private void updateForces(float dt) {
        //Apply upwards force on velocity from d rising under its own steam 
        if (this.d_force != 0.0f) {
            this.ForceFrom(this.dOld, this.vOld, this.d_force);
        }

        //Apply upwards force on velocity field from heat
        if (this.heat_force != 0.0f) {
            this.Heat(this.heat_force, dt);

            if (this.heat_decay != 0.0f) {
                this.ExponentialDecay(this.heatOld, this.heat_decay, dt);
            }
        }

        //Apply dampening force on velocity due to viscosity
        if (this.velocity_decay != 0.0f) {
            this.ExponentialDecay(this.uOld, this.velocity_decay, dt);
            this.ExponentialDecay(this.vOld, this.velocity_decay, dt);
        }

        //Apply equilibrium force on pressure for mass conservation
        if (this.pressure_acc != 0.0f) {
            this.PressureAcceleration(this.pressure_acc, dt);
        }

        //Apply curl force on vorticies to prevent artificial dampening
        if (this.vorticity != 0.0f) {
            this.VorticityConfinement(this.vorticity);
        }
    }

    /**
     * Apply advection across the grids.
     *
     * @param dt simulation time step.
     */
    private void updateAdvection(float dt) {
        float avg_dimension = (this.n + this.n) / 2.0f;

        //Change advection scale depending on grid size. Smaller grids means larger cells, so scale should be smaller.
        //Average dimension size of 100 equals an advection_scale of 1
        float advection_scale = avg_dimension / this.n; //100.0f;

        this.SetField(this.d, 1.0f);

        //Advect the d - d is one fluid suspended in another, like smoke in air
        this.ForwardAdvection(this.dOld, this.d, this.d_advection * advection_scale, dt);
        this.swapD();
        this.ReverseAdvection(this.dOld, this.d, this.d_advection * advection_scale, dt);
        this.swapD();

        //Only advect the heat if it is applying a force
        if (this.heat_force != 0.0f) {
            this.ForwardAdvection(this.heatOld, this.heat, this.heat_advection * advection_scale, dt);
            this.swapHeat();
            this.ReverseAdvection(this.heatOld, this.heat, this.heat_advection * advection_scale, dt);
            this.swapHeat();
        }

        //Advection order makes significant differences
        //Advecting pressure first leads to self-maintaining waves and ripple artifacts
        //Adecting velocity first naturally dissipates the waves
        //Advect Velocity
        this.ForwardAdvection(this.uOld, this.u, this.velocity_advection * advection_scale, dt);
        this.ForwardAdvection(this.vOld, this.v, this.velocity_advection * advection_scale, dt);
        this.ReverseSignedAdvection(advection_scale, dt); //We can use signed reverse advection as quantities can be negative.

        //Take care of boundries
        this.InvertVelocityEdges();

        //Advect Pressure. Represents compressible fluid (like the air)
        this.ForwardAdvection(this.pressureOld, this.pressure, this.pressure_advection * advection_scale, dt);
        this.swapPressure();
        this.ReverseAdvection(this.pressureOld, this.pressure, this.pressure_advection * advection_scale, dt);
        this.swapPressure();
    }

    /**
     * Cells adjacent to border cells have a different diffusion. They still
     * take in the four surrounding cells but since the border cells are not
     * updated, the adjacent cells should emit less. We could use a lookup table
     * for this or just calculate an adjaceny figure. ACTUALLY - NOW WE DO
     * UPDATE ALL THE CELLS, SO BORDER CONDITION NOW CHANGED
     *
     * @param p_in
     * @param p_out
     * @param scale
     * @param dt simulation time step.
     */
    private void Diffusion(float[] p_in, float[] p_out, float scale, float dt) {
        float force = dt * scale;

        //top and bot edges
        for (int x = 1; x < this.n - 1; x++) {
            p_out[this.I(x, 0)] = p_in[this.I(x, 0)] + force
                    * (p_in[this.I(x - 1, 0)] + p_in[this.I(x + 1, 0)]
                    + p_in[this.I(x, 1)] - 3.0f * p_in[this.I(x, 0)]);
            p_out[this.I(x, this.n - 1)] = p_in[this.I(x, this.n - 1)] + force
                    * (p_in[this.I(x - 1, this.n - 1)] + p_in[this.I(x + 1, this.n - 1)]
                    + p_in[this.I(x, this.n - 2)] - 3.0f * p_in[this.I(x, this.n - 1)]);
        }

        //left and right edges
        for (int y = 1; y < this.n - 1; y++) {
            p_out[this.I(0, y)] = p_in[this.I(0, y)] + force
                    * (p_in[this.I(0, y - 1)] + p_in[this.I(0, y + 1)]
                    + p_in[this.I(1, y)] - 3.0f * p_in[this.I(0, y)]);
            p_out[this.I(this.n - 1, y)] = p_in[this.I(this.n - 1, y)] + force
                    * (p_in[this.I(this.n - 1, y - 1)] + p_in[this.I(this.n - 1, y + 1)]
                    + p_in[this.I(this.n - 2, y)] - 3.0f * p_in[this.I(this.n - 1, y)]);
        }

        //corners
        p_out[this.I(0, 0)] = p_in[this.I(0, 0)] + force
                * (p_in[this.I(1, 0)] + p_in[this.I(0, 1)]
                - 2.0f * p_in[this.I(0, 0)]);
        p_out[this.I(this.n - 1, 0)] = p_in[this.I(this.n - 1, 0)] + force
                * (p_in[this.I(this.n - 2, 0)] + p_in[this.I(this.n - 1, 1)]
                - 2.0f * p_in[this.I(this.n - 1, 0)]);
        p_out[this.I(0, this.n - 1)] = p_in[this.I(0, this.n - 1)] + force
                * (p_in[this.I(1, this.n - 1)] + p_in[this.I(0, this.n - 2)]
                - 2.0f * p_in[this.I(0, this.n - 1)]);
        p_out[this.I(this.n - 1, this.n - 1)] = p_in[this.I(this.n - 1, this.n - 1)] + force
                * (p_in[this.I(this.n - 2, this.n - 1)] + p_in[this.I(this.n - 1, this.n - 2)]
                - 2.0f * p_in[this.I(this.n - 1, this.n - 1)]);

        //everything else
        this.linearSolverDiffusion.linearSolver(0, 1, p_out, p_in, force, 4);
    }

    /**
     * Given a field p_in, and a field p_out, then add f *p_in to p_our can be
     * used to apply a heat field to velocity.
     *
     * @param p_in
     * @param p_out
     * @param f
     */
    private void ForceFrom(float[] p_in, float[] p_out, float f) {
        for (int cell = 0; cell < this.size; cell++) {
            p_out[cell] += p_in[cell] * f;
        }
    }

    /**
     * Apply the effects of heat to the velocity grid.
     *
     * @param scale
     * @param dt simulation time step.
     */
    private void Heat(float scale, float dt) {
        float force = dt * scale;

        arraycopy(this.uOld, 0, this.u, 0, this.size);
        arraycopy(this.vOld, 0, this.v, 0, this.size);

        for (int x = 0; x < this.n - 1; x++) {
            for (int y = 0; y < this.n - 1; y++) {
                //Pressure differential between points to get an acceleration force.
                float force_x = this.heatOld[this.I(x, y)] - this.heatOld[this.I(x + 1, y)];
                float force_y = this.heatOld[this.I(x, y)] - this.heatOld[this.I(x, y + 1)];

                //Use the acceleration force to move the velocity field in the appropriate direction. 
                //Ex. If an area of high pressure exists the acceleration force will turn the velocity field
                //away from this area
                this.u[this.I(x, y)] += force * force_x;
                this.u[this.I(x + 1, y)] += force * force_x;

                this.v[this.I(x, y)] += force * force_y;
                this.v[this.I(x, y + 1)] += force * force_y;
            }
        }

        this.swapU();
        this.swapV();
    }

    /**
     * Apply a natural deceleration to forces applied to the grids.
     *
     * @param p_in
     * @param decay
     * @param dt simulation time step.
     */
    private void ExponentialDecay(float[] p_in, float decay, float dt) {
        for (int x = 0; x < this.n; x++) {
            for (int y = 0; y < this.n; y++) {
                p_in[this.I(x, y)] = (float) (p_in[this.I(x, y)] * pow(1 - decay, dt));
            }
        }
    }

    /**
     * Apply acceleration due to pressure.
     *
     * @param dt simulation time step.
     */
    private void PressureAcceleration(float scale, float dt) {
        float force = dt * scale;

        arraycopy(this.uOld, 0, this.u, 0, this.size);
        arraycopy(this.vOld, 0, this.v, 0, this.size);

        for (int x = 0; x < this.n - 1; x++) {
            for (int y = 0; y < this.n - 1; y++) {
                //Pressure differential between points to get an acceleration force.
                float force_x = this.pressureOld[this.I(x, y)] - this.pressureOld[this.I(x + 1, y)];
                float force_y = this.pressureOld[this.I(x, y)] - this.pressureOld[this.I(x, y + 1)];

                //Use the acceleration force to move the velocity field in the appropriate direction. 
                //Ex. If an area of high pressure exists the acceleration force will turn the velocity field
                //away from this area
                this.u[this.I(x, y)] += force * force_x;
                this.u[this.I(x + 1, y)] += force * force_x;

                this.v[this.I(x, y)] += force * force_y;
                this.v[this.I(x, y + 1)] += force * force_y;
            }
        }

        this.swapU();
        this.swapV();
    }

    /**
     * Apply vorticities to the simulation.
     *
     * @param scale
     */
    private void VorticityConfinement(float scale) {
        float lr_curl;   //curl in the left-right direction
        float ud_curl;   //curl in the up-down direction
        float length;
        float magnitude;

        this.SetField(this.u, 0.0f);
        this.SetField(this.v, 0.0f);

        this.SetField(this.curl, 0.0f);

        for (int i = 1; i < this.n - 1; i++) {
            for (int j = 1; j < this.n - 1; j++) {
                this.curl[this.I(i, j)] = abs(this.Curl(i, j));
            }
        }

        for (int x = 2; x < this.n - 1; x++) {
            for (int y = 2; y < this.n - 1; y++) {
                //get curl gradient across cells
                lr_curl = (this.curl[this.I(x + 1, y)] - this.curl[this.I(x - 1, y)]) * 0.5f;
                ud_curl = (this.curl[this.I(x, y + 1)] - this.curl[this.I(x, y - 1)]) * 0.5f;

                //Normalize the derivative curl vector
                length = (float) sqrt(lr_curl * lr_curl + ud_curl * ud_curl) + 0.000001f;
                lr_curl /= length;
                ud_curl /= length;

                magnitude = this.Curl(x, y);

                this.u[this.I(x, y)] = -ud_curl * magnitude;
                this.v[this.I(x, y)] = lr_curl * magnitude;

            }
        }

        this.ForceFrom(this.u, this.uOld, scale);
        this.ForceFrom(this.v, this.vOld, scale);
    }

    /**
     * Calculate the curl at position (i, j) in the fluid grid. Physically this
     * represents the vortex strength at the cell. Computed as follows: w = (del
     * x U) where U is the velocity vector at (i, j).
     *
     * @param x
     * @param y
     * @return The curl at position (i, j) in the fluid grid.
     */
    private float Curl(int x, int y) {
        //difference in XV of cells above and below
        //positive number is a counter-clockwise rotation
        float x_curl = (this.uOld[this.I(x, y + 1)] - this.uOld[this.I(x, y - 1)]) * 0.5f;

        //difference in YV of cells left and right
        //positive number is a counter-clockwise rotation
        float y_curl = (this.vOld[this.I(x + 1, y)] - this.vOld[this.I(x - 1, y)]) * 0.5f;

        return x_curl - y_curl;
    }

    /**
     *
     * Move scalar along the velocity field. Forward advection moves the value
     * at a point forward along the vector from the same point and dissipates it
     * between four points as needed.
     *
     * @param p_in
     * @param p_out
     * @param scale
     * @param dt simulation time step.
     */
    private void ForwardAdvection(float[] p_in, float[] p_out, float scale, float dt) {
        float force = dt * scale; //distance to advect
        float vx, vy; //velocity values of the current x and y location
        float x1, y1; //x and y location after advection
        int x1A, y1A; //x and y location of top-left grid point (A) after advection
        float fx1, fy1; //fractional remainders of x1, y1
        float source_value; //original source value
        float A, //top-left grid point value after advection
                B, //top-right grid point value after advection
                C, //bottom-left grid point value after advection
                D; //bottom-right grid point value after advection

        //Copy source to destination as forward advection results in adding/subtracting not moving
        arraycopy(p_in, 0, p_out, 0, this.size);

        if (force == 0.0f) {
            return;
        }
        for (int x = 0; x < this.n; x++) {
            for (int y = 0; y < this.n; y++) {
                vx = this.uOld[this.I(x, y)];
                vy = this.vOld[this.I(x, y)];
                if (!this.Equal2Zero(vx) || !this.Equal2Zero(vy)) {
                    //Find the floating point location of the forward advection
                    x1 = x + vx * force;
                    y1 = y + vy * force;
                    if (!extensionPhysicsOptions.get("useBounds")
                            && (x1 > this.n || x1 < 0 || y1 > this.n || y1 < 0)) {
                        p_out[this.I(x, y)] -= p_in[this.I(x, y)];
                        break;
                    }

                    float[] collide = this.Collide(x1, y1);
                    x1 = collide[0];
                    y1 = collide[1];

                    //Find the nearest top-left integer grid point of the advection 
                    x1A = (int) x1;
                    y1A = (int) y1;

                    //Store the fractional parts
                    fx1 = x1 - x1A;
                    fy1 = y1 - y1A;

                    //The floating point location after forward advection (x1,y1) will land within 4 points on the grid (A,B,C,D).
                    //Distribute the value of the source point among the destination grid points (A,B,C,D) using bilinear interpolation.
                    //Subtract the total value given to the destination grid points from the source point.
                    //
                    // A----B
                    // |    |
                    // |    |
                    // C----D
                    //
                    //(Should be square)
                    //Pull source value from the unmodified p_in
                    source_value = p_in[this.I(x, y)];

                    //Bilinear interpolation
                    A = (1.0f - fy1) * (1.0f - fx1) * source_value;
                    B = (1.0f - fy1) * (fx1) * source_value;
                    C = (fy1) * (1.0f - fx1) * source_value;
                    D = (fy1) * (fx1) * source_value;

                    //Add A,B,C,D to the four destination cells
                    p_out[this.I(x1A, y1A)] += A;
                    p_out[this.I(x1A + 1, y1A)] += B;
                    p_out[this.I(x1A, y1A + 1)] += C;
                    p_out[this.I(x1A + 1, y1A + 1)] += D;

                    //Subtract A,B,C,D from source (x,y) for mass conservation
                    p_out[this.I(x, y)] -= A + B + C + D;
                }
            }
        }
    }

    /**
     * Move a scalar along the velocity field.
     *
     * @param p_in
     * @param p_out
     * @param scale
     * @param dt simulation time step.
     */
    private void ReverseAdvection(float[] p_in, float[] p_out, float scale, float dt) {
        float force = -dt * scale; //negate force, since it's reverse advection
        float vx, vy; //velocity values of the current x and y location
        float x1, y1; //x and y location after advection
        int x1A, y1A; //x and y location of top-left grid point (A) after advection
        float fx1, fy1; //fractional remainders of x1, y1
        float A, //top-left grid point value after advection
                B, //top-right grid point value after advection
                C, //bottom-left grid point value after advection
                D; //bottom-right grid point value after advection
        float A_Total, B_Total, C_Total, D_Total; //Total fraction being requested by the 4 grid points after entire grid has been advected

        //Copy source to destination as reverse advection results in adding/subtracting not moving
        arraycopy(p_in, 0, p_out, 0, this.size);

        //we need to zero out the fractions 
        int[] FromSource_xA = new int[this.size];
        this.SetField(FromSource_xA, -1); //The new X coordinate after advection stored in x,y where x,y is the original source point
        int[] FromSource_yA = new int[this.size];
        this.SetField(FromSource_yA, -1); //The new Y coordinate after advection stored in x,y where x,y is the original source point
        float[] FromSource_A = new float[this.size]; //The value of A after advection stored in x,y where x,y is the original source point
        float[] FromSource_B = new float[this.size]; //The value of B after advection stored in x,y where x,y is the original source point
        float[] FromSource_C = new float[this.size]; //The value of C after advection stored in x,y where x,y is the original source point
        float[] FromSource_D = new float[this.size]; //The value of D after advection stored in x,y where x,y is the original source point
        float[] TotalDestValue = new float[this.size]; //The total accumulated value after advection stored in x,y where x,y is the destination point

        for (int y = 0; y < this.n; y++) {
            for (int x = 0; x < this.n; x++) {
                vx = this.uOld[this.I(x, y)];
                vy = this.vOld[this.I(x, y)];
                if (!this.Equal2Zero(vx) || !this.Equal2Zero(vy)) {
                    //Find the floating point location of the advection
                    x1 = x + vx * force;
                    y1 = y + vy * force;
                    if (!extensionPhysicsOptions.get("useBounds")
                            && (x1 > this.n || x1 < 0 || y1 > this.n || y1 < 0)) {
                        p_out[this.I(x, y)] -= p_in[this.I(x, y)];
                        break;
                    }
                    float[] collide = this.Collide(x1, y1);
                    x1 = collide[0];
                    y1 = collide[1];

                    //Find the nearest top-left integer grid point of the advection 
                    x1A = (int) x1;
                    y1A = (int) y1;

                    //Store the fractional parts
                    fx1 = x1 - x1A;
                    fy1 = y1 - y1A;

                    //The floating point location after forward advection (x1,y1) will land within 4 points on the grid (A,B,C,D).
                    //Distribute the value of the source point among the destination grid points (A,B,C,D) using bilinear interpolation.
                    //Subtract the total value given to the destination grid points from the source point.
                    //
                    // A----B
                    // |    |
                    // |    |
                    // C----D
                    //
                    //(Should be square)
                    /*

                     By adding the source value into the destination, we handle the problem of multiple destinations
                     but by subtracting it from the source we gloss ove the problem of multiple sources.
                     Suppose multiple destinations have the same (partial) source cells, then what happens is the first dest that 
                     is processed will get all of that source cell (or all of the fraction it needs).  Subsequent dest
                     cells will get a reduced fraction.  In extreme cases this will lead to holes forming based on 
                     the update order.

                     Solution:  Maintain an array for dest cells, and source cells.
                     For dest cells, store the four source cells and the four fractions
                     For source cells, store the number of dest cells that source from here, and the total fraction
                     E.G.  Dest cells A, B, C all source from cell D (and explicit others XYZ, which we don't need to store)
                     So, dest cells store A->D(0.1)XYZ..., B->D(0.5)XYZ.... C->D(0.7)XYZ...
                     Source Cell D is updated with A, B then C
                     Update A:   Dests = 1, Tot = 0.1
                     Update B:   Dests = 2, Tot = 0.6
                     Update C:   Dests = 3, Tot = 1.3

                     How much should go to each of A, B and C? They are asking for a total of 1.3, so should they get it all, or 
                     should they just get 0.4333 in total?
                     Ad Hoc answer:  
                     if total <=1 then they get what they ask for
                     if total >1 then is is divided between them proportionally.
                     If there were two at 1.0, they would get 0.5 each
                     If there were two at 0.5, they would get 0.5 each
                     If there were two at 0.1, they would get 0.1 each
                     If there were one at 0.6 and one at 0.8, they would get 0.6/1.4 and 0.8/1.4  (0.429 and 0.571) each

                     So in our example, total is 1.3, 
                     A gets 0.1/1.3, B gets 0.6/1.3 C gets 0.7/1.3, all totaling 1.0

                     */
                    //Bilinear interpolation
                    A = (1.0f - fy1) * (1.0f - fx1);
                    B = (1.0f - fy1) * (fx1);
                    C = (fy1) * (1.0f - fx1);
                    D = (fy1) * (fx1);

                    //Store the coordinates of destination point A for this source point (x,y)
                    FromSource_xA[this.I(x, y)] = x1A;
                    FromSource_yA[this.I(x, y)] = y1A;

                    //Store the values of A,B,C,D for this source point
                    FromSource_A[this.I(x, y)] = A;
                    FromSource_B[this.I(x, y)] = B;
                    FromSource_C[this.I(x, y)] = C;
                    FromSource_D[this.I(x, y)] = D;

                    //Accumulating the total value for the four destinations
                    TotalDestValue[this.I(x1A, y1A)] += A;
                    TotalDestValue[this.I(x1A + 1, y1A)] += B;
                    TotalDestValue[this.I(x1A, y1A + 1)] += C;
                    TotalDestValue[this.I(x1A + 1, y1A + 1)] += D;
                }
            }
        }

        for (int y = 0; y < this.n; y++) {
            for (int x = 0; x < this.n; x++) {
                if (FromSource_xA[this.I(x, y)] != -1) {
                    //Get the coordinates of A
                    x1A = FromSource_xA[this.I(x, y)];
                    y1A = FromSource_yA[this.I(x, y)];

                    //Get the four fractional amounts we earlier interpolated
                    A = FromSource_A[this.I(x, y)];
                    B = FromSource_B[this.I(x, y)];
                    C = FromSource_C[this.I(x, y)];
                    D = FromSource_D[this.I(x, y)];

                    //Get the TOTAL fraction requested from each source cell
                    A_Total = TotalDestValue[this.I(x1A, y1A)];
                    B_Total = TotalDestValue[this.I(x1A + 1, y1A)];
                    C_Total = TotalDestValue[this.I(x1A, y1A + 1)];
                    D_Total = TotalDestValue[this.I(x1A + 1, y1A + 1)];

                    //If less then 1.0 in total then no scaling is neccessary
                    if (A_Total < 1.0f) {
                        A_Total = 1.0f;
                    }
                    if (B_Total < 1.0f) {
                        B_Total = 1.0f;
                    }
                    if (C_Total < 1.0f) {
                        C_Total = 1.0f;
                    }
                    if (D_Total < 1.0f) {
                        D_Total = 1.0f;
                    }

                    //Scale the amount we are transferring
                    A /= A_Total;
                    B /= B_Total;
                    C /= C_Total;
                    D /= D_Total;

                    //Give the fraction of the original source, do not alter the original
                    //So we are taking fractions from p_in, but not altering those values as they are used again by later cells
                    //if the field were mass conserving, then we could simply move the value but if we try that we lose mass
                    p_out[this.I(x, y)] += A * p_in[this.I(x1A, y1A)]
                            + B * p_in[this.I(x1A + 1, y1A)]
                            + C * p_in[this.I(x1A, y1A + 1)]
                            + D * p_in[this.I(x1A + 1, y1A + 1)];

                    //Subtract the values added to the destination from the source for mass conservation
                    p_out[this.I(x1A, y1A)] -= A * p_in[this.I(x1A, y1A)];
                    p_out[this.I(x1A + 1, y1A)] -= B * p_in[this.I(x1A + 1, y1A)];
                    p_out[this.I(x1A, y1A + 1)] -= C * p_in[this.I(x1A, y1A + 1)];
                    p_out[this.I(x1A + 1, y1A + 1)] -= D * p_in[this.I(x1A + 1, y1A + 1)];
                }
            }
        }
    }

    /**
     * Signed advection is mass conserving, but allows signed quantities so
     * could be used for velocity, since it's faster.
     *
     * @param advectScale
     * @param dt simulation time step.
     */
    private void ReverseSignedAdvection(float advectScale, float dt) {
        float scale = this.velocity_advection * advectScale;
        float force = -dt * scale; //negate advection scale, since it's reverse advection
        float vx, vy; //velocity values of the current x and y location
        float x1, y1; //x and y location after advection
        int x1A, y1A; //x and y location of top-left grid point (A) after advection
        float fx1, fy1; //fractional remainders of x1, y1
        float A_X, A_Y, //top-left grid point value after advection
                B_X, B_Y, //top-right grid point value after advection
                C_X, C_Y, //bottom-left grid point value after advection
                D_X, D_Y; //bottom-right grid point value after advection

        //First copy the scalar values over, since we are adding/subtracting in values, not moving things
        float[] velOutX = new float[this.size];
        float[] velOutY = new float[this.size];
        arraycopy(this.u, 0, velOutX, 0, this.size);
        arraycopy(this.v, 0, velOutY, 0, this.size);

        for (int x = 0; x < this.n; x++) {
            for (int y = 0; y < this.n; y++) {
                vx = this.uOld[this.I(x, y)];
                vy = this.vOld[this.I(x, y)];
                if (!this.Equal2Zero(vx) || !this.Equal2Zero(vy)) {
                    x1 = x + vx * force;
                    y1 = y + vy * force;

                    float[] collide = this.Collide(x1, y1);
                    x1 = collide[0];
                    y1 = collide[1];

                    x1A = (int) x1;
                    y1A = (int) y1;

                    //get fractional parts
                    fx1 = x1 - x1A;
                    fy1 = y1 - y1A;

                    //Get amounts from (in) source cells for X velocity
                    A_X = (1.0f - fy1) * (1.0f - fx1) * this.u[this.I(x1A, y1A)];
                    B_X = (1.0f - fy1) * (fx1) * this.u[this.I(x1A + 1, y1A)];
                    C_X = (fy1) * (1.0f - fx1) * this.u[this.I(x1A, y1A + 1)];
                    D_X = (fy1) * (fx1) * this.u[this.I(x1A + 1, y1A + 1)];

                    //Get amounts from (in) source cells for Y velocity
                    A_Y = (1.0f - fy1) * (1.0f - fx1) * this.v[this.I(x1A, y1A)];
                    B_Y = (1.0f - fy1) * (fx1) * this.v[this.I(x1A + 1, y1A)];
                    C_Y = (fy1) * (1.0f - fx1) * this.v[this.I(x1A, y1A + 1)];
                    D_Y = (fy1) * (fx1) * this.v[this.I(x1A + 1, y1A + 1)];

                    //X Velocity
                    //add to (out) source cell
                    velOutX[this.I(x, y)] += A_X + B_X + C_X + D_X;
                    //and subtract from (out) dest cells
                    velOutX[this.I(x1A, y1A)] -= A_X;
                    velOutX[this.I(x1A + 1, y1A)] -= B_X;
                    velOutX[this.I(x1A, y1A + 1)] -= C_X;
                    velOutX[this.I(x1A + 1, y1A + 1)] -= D_X;

                    //Y Velocity
                    //add to (out) source cell
                    velOutY[this.I(x, y)] += A_Y + B_Y + C_Y + D_Y;
                    //and subtract from (out) dest cells
                    velOutY[this.I(x1A, y1A)] -= A_Y;
                    velOutY[this.I(x1A + 1, y1A)] -= B_Y;
                    velOutY[this.I(x1A, y1A + 1)] -= C_Y;
                    velOutY[this.I(x1A + 1, y1A + 1)] -= D_Y;
                }
            }
        }

        arraycopy(velOutX, 0, this.uOld, 0, this.size);
        arraycopy(velOutY, 0, this.vOld, 0, this.size);
    }

    /**
     * Checks if destination point during advection is out of bounds and pulls
     * point in if needed.
     *
     * @param x1
     * @param y1
     * @return
     */
    private float[] Collide(float x1, float y1) {
        float right_bound = this.n - 1.0001f;
        float bot_bound = this.n - 1.0001f;

        while (x1 < 0 || x1 > right_bound) {
            if (x1 < 0) {
                x1 = -x1;
            } else if (x1 > right_bound) {
                x1 = right_bound - x1 - right_bound;
            }
        }

        while (y1 < 0 || y1 > bot_bound) {
            if (y1 < 0) {
                y1 = -y1;
            } else if (y1 > bot_bound) {
                y1 = bot_bound - y1 - bot_bound;
            }
        }

        float[] solution = {x1, y1};
        return solution;
    }

    /**
     * Invert velocities that are facing outwards at boundaries.
     */
    private void InvertVelocityEdges() {
        for (int y = 0; y < this.n; y++) {
            if (this.uOld[this.I(0, y)] < 0.0f) {
                this.uOld[this.I(0, y)] = -this.uOld[this.I(0, y)];
            }
            if (this.uOld[this.I(this.n - 1, y)] > 0.0f) {
                this.uOld[this.I(this.n - 1, y)] = -this.uOld[this.I(this.n - 1, y)];
            }
        }

        for (int x = 0; x < this.n; x++) {
            if (this.vOld[this.I(x, 0)] < 0.0f) {
                this.vOld[this.I(x, 0)] = -this.vOld[this.I(x, 0)];
            }
            if (this.vOld[this.I(x, this.n - 1)] > 0.0f) {
                this.vOld[this.I(x, this.n - 1)] = -this.vOld[this.I(x, this.n - 1)];
            }
        }
    }

    /**
     * float zero approximation.
     *
     * @param in
     * @return TRUE if in between ZERO_MAX and ZERO_MIN, FALSE otherwise.
     */
    private boolean Equal2Zero(float in) {
        return in < this.ZERO_MAX && in > this.ZERO_MIN;
    }

    /**
     *
     * @param p_field
     * @param i
     */
    private void SetField(int[] p_field, int i) {
        for (int x = 0; x < this.size; x++) {
            p_field[x] = i;
        }
    }

    /**
     *
     * @param p_field
     * @param f
     */
    public void SetField(float[] p_field, float f) {
        for (int x = 0; x < this.size; x++) {
            p_field[x] = f;
        }
    }

    /**
     * Utility method to swap old and new velocities in x first two arrays.
     */
    private void swapU() {
        this.tmp = this.uOld;
        this.uOld = this.u;
        this.u = this.tmp;
    }

    /**
     * Utility method to swap old and new velocities in y first two arrays.
     */
    private void swapV() {
        this.tmp = this.vOld;
        this.vOld = this.v;
        this.v = this.tmp;
    }

    /**
     * Utility method to swap old and new pressure arrays.
     */
    private void swapPressure() {
        this.tmp = this.pressureOld;
        this.pressureOld = this.pressure;
        this.pressure = this.tmp;
    }

    /**
     * Utility method to swap old and new heat arrays.
     */
    private void swapHeat() {
        this.tmp = this.heatOld;
        this.heatOld = this.heat;
        this.heat = this.tmp;
    }

    /**
     * Utility method to swap old and new d arrays.
     */
    private void swapD() {
        this.tmp = this.dOld;
        this.dOld = this.d;
        this.d = this.tmp;
    }

    /**
     * Distribute a value to the 4 grid points surrounding the floating point
     * coordinates.
     *
     * @param p
     * @param x
     * @param y
     * @param value
     */
    public void DistributeFloatingPoint(float[] p, float x, float y, float value) {
        //Coordinate (ix, iy) is the top-left point on cell
        int ix = (int) floor(x);
        int iy = (int) floor(y);

        //Get fractional parts of coordinates 
        float fx = x - ix;
        float fy = y - iy;

        //Add appropriate fraction of value into each of the 4 cell points
        p[this.I(ix, iy)] += (1.0f - fy) * (1.0f - fx) * value;
        p[this.I(ix + 1, iy)] += (1.0f - fy) * (fx) * value;
        p[this.I(ix, iy + 1)] += (fy) * (1.0f - fx) * value;
        p[this.I(ix + 1, iy + 1)] += (fy) * (fx) * value;
    }
}
