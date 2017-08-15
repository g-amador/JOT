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

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import jot.physics.fluids.eulerian.linear_solvers.AbstractLinearSolver2D;

/**
 * Abstract class that each Eulerian Solver must extend.
 *
 * @author G. Amador {@literal &} A. Gomes
 *
 */
public abstract class AbstractEulerianFluidSolver2D {

    //Fluid simulatior variables
    /**
     * Grid length and width and total cells count.
     */
    public int n, size;

    /**
     * New and old velocity in u,
     */
    public float[] u, uOld;

    /**
     * New and old velocity in v,
     */
    public float[] v, vOld;

    /**
     * New and old density,
     */
    public float[] d, dOld;

    /**
     * Curl values,
     */
    public float[] curl;

    /**
     * Auxiliary swap array.
     */
    protected float[] tmp;

    //Linear solvers variables
    /**
     * Iterations to perform by the linear solver in the diffusion.
     */
    public int diffusion_iterations;

    /**
     * The pointer to the diffusion linear solver.
     */
    public AbstractLinearSolver2D linearSolverDiffusion;

    //Render variables
    /**
     * Cell dimensions.
     */
    public float h;

    protected int listDensity;
    protected int listVelocity;
    protected float dx, dy; //Cell position.

    /**
     * * Set the grid size, the time step, and the linear solver to use.
     *
     * @param n grid cells width and length.
     * @param linearSolverDiffusion the linear solver to use in diffusion.
     */
    public abstract void setup(int n, AbstractLinearSolver2D linearSolverDiffusion);

    /**
     * Reset the data structures. We use 1d arrays for speed.
     *
     */
    public abstract void reset();

    /**
     * Update the fluid with a time step dt.
     *
     * @param dt simulation time step.
     */
    public abstract void update(float dt);

    /**
     * Render the density field.
     *
     * @param gl
     */
    public void drawDensity(GL2 gl) {
        gl.glPushMatrix();
        gl.glDeleteLists(this.listDensity, 1);
        this.listDensity = gl.glGenLists(1);
        gl.glNewList(this.listDensity, GL_COMPILE);
        {
            gl.glRotatef(180, 0, 1, 0);
            for (int i = 1; i < this.n - 1; i++) {
                //x position of current cell
                this.dx = (i - 0.5f) * this.h;
                for (int j = 1; j < this.n - 1; j++) {
                    //y position of current cell
                    this.dy = (j - 0.5f) * this.h;

                    gl.glColor3f(1 - this.d[this.I(i, j)], 1 - this.d[this.I(i, j)], 1 - this.d[this.I(i, j)]);
                    gl.glRectf(this.dx, this.dy, this.dx + this.h, this.dy + this.h);
                }
            }
        }
        gl.glEndList();
        gl.glCallList(this.listDensity);
        gl.glPopMatrix();
    }

    /**
     * Render the velocity field.
     *
     * @param gl
     */
    public void drawVelocity(GL2 gl) {
        gl.glPushMatrix();
        gl.glDeleteLists(this.listVelocity, 1);
        this.listVelocity = gl.glGenLists(1);
        gl.glNewList(this.listVelocity, GL_COMPILE);
        {
            gl.glColor3f(1, 0, 0);
            gl.glPointSize(2);
            gl.glLineWidth(2);
            gl.glRotatef(180, 0, 1, 0);
            gl.glBegin(GL_LINES);
            {
                for (int i = 1; i < this.n - 1; i++) {
                    //x position of current cell
                    this.dx = (i - 0.5f) * this.h;
                    for (int j = 1; j < this.n - 1; j++) {
                        //y position of current cell
                        this.dy = (j - 0.5f) * this.h;

                        gl.glVertex3f(this.dx, this.dy, 0.1f);
                        gl.glVertex3f(this.dx + this.u[this.I(i, j)], this.dy + this.v[this.I(i, j)], 0.1f);
                    }
                }
            }
            gl.glEnd();
        }
        gl.glEndList();
        gl.glCallList(this.listVelocity);
        gl.glPopMatrix();
    }

    /**
     * Utility method for indexing 1d arrays.
     *
     * @param i index of a grid cell.
     * @param j index of a grid cell.
     * @return the i,j cell value.
     */
    public int I(int i, int j) {
        return i + this.n * j;
    }
}
