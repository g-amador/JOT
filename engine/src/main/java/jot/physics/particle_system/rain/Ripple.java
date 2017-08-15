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
package jot.physics.particle_system.rain;

import static com.jogamp.opengl.GL.GL_POINTS;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.particle_system.Particle;
import static jot.util.ExtensionPhysicsOptions.extensionPhysicsOptions;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import static org.apache.commons.math3.geometry.euclidean.twod.Vector2D.distance;

/**
 * Class that implement a Ripple effect.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Ripple implements Particle {

    static final Logger log = getLogger("Ripple");

    static {
        log.setLevel(OFF);
    }

    //Solver variables
    private final float size;
    private final int n; //cell count in x an y    

    private float[] d, dOld, temp;

    private float r = 1;
    private int xstart = -1;
    private int ystart = -1;

    private int ttl;
    private boolean iterate = false;

    private int listRipple;

    /**
     *
     * Constructor.
     *
     * @param n cell count in x an y.
     * @param size absolute size value of one of the sides of the square that
     * defines the floor, i.e., floor as size*size area.
     * @param ttl ripple time to live.
     */
    public Ripple(int n, int size, int ttl) {
        this.n = n;
        this.size = size;
        this.ttl = ttl;

        this.d = new float[n * n];
        this.dOld = new float[n * n];
        this.temp = new float[n * n];
    }

    /**
     * Create a perturbation in the heightmap position dx, dy.
     *
     * @param dx
     * @param dy
     */
    public void disturb(int dx, int dy) {
        this.xstart = dx;
        this.ystart = dy;
        this.d[this.I(this.xstart, this.ystart)] = -0.5f;
        this.splash(this.xstart, this.ystart, 5);
        this.iterate = true;
    }

    @Override
    public boolean isDead() {
        return this.ttl < 0;
    }

    @Override
    public void render(GL2 gl) {
        //Swap
        this.temp = this.dOld;
        this.dOld = this.d;
        this.d = this.temp;

        gl.glPushMatrix();
        {
            gl.glDeleteLists(this.listRipple, 1);
            this.listRipple = gl.glGenLists(1);
            gl.glNewList(this.listRipple, GL_COMPILE);
            {
                gl.glBegin(GL_POINTS);
                for (int i = 0; i < this.n; i++) {
                    for (int j = 0; j < this.n; j++) {
                        if (this.dOld[this.I(i, j)] != 0) {
                            gl.glColor3f(1, 1, 1);
                            gl.glVertex3f(i * -this.size / this.n, this.dOld[this.I(i, j)], j * -this.size / this.n); // for each vertex
                        }
                    }
                }
                gl.glEnd();
            }
            gl.glEndList();
        }
        gl.glPopMatrix();

        gl.glDisable(GL_LIGHTING);
        gl.glCallList(this.listRipple);
        gl.glEnable(GL_LIGHTING);
    }

    @Override
    public void update(float dt) {
        if (this.ystart != -1 && this.xstart != -1) {
            for (int y = this.ystart - (int) this.r; y < this.ystart + (int) this.r; y++) {
                for (int x = this.xstart - (int) this.r; x < this.xstart + (int) this.r; x++) {
                    float dist = (float) distance(new Vector2D(x, y), new Vector2D(this.xstart, this.ystart));
                    if (dist < this.r // if within circle
                    
                            && this.xstart - (int) this.r > 1 && this.xstart + (int) this.r < this.n - 2
                            && this.ystart - (int) this.r > 1 && this.ystart + (int) this.r < this.n - 2) {
                        this.d[this.I(x, y)] = (this.dOld[this.I(x - 1, y)]
                                + this.dOld[this.I(x + 1, y)]
                                + this.dOld[this.I(x, y - 1)]
                                + this.dOld[this.I(x, y + 1)]) / 2
                                - this.d[this.I(x, y)];

                        this.d[this.I(x, y)] /= dt;
                        //d[I(x, y)] *= dt;
                        //d[I(x, y)] -= d[I(x, y)] * dt;
                    }
                }
            }
        }

        if (this.iterate) {
            if (this.r < (this.n - 2) / 2 && this.ttl >= 0
                    && this.xstart > 1 && this.xstart < this.n - 2
                    && this.ystart > 1 && this.ystart < this.n - 2) {
                this.r += 0.2f;
                this.ttl--;
            } else {
                this.r = 1;
                this.ttl = -1;
                this.iterate = false;
            }
        }
    }

    private void splash(int cx, int cy, int radius) {
        if (extensionPhysicsOptions.get("useRipple")) {
            for (int y = cy - radius; y < cy + radius; y++) {
                for (int x = cx - radius; x < cx + radius; x++) {
                    float dist = (float) distance(new Vector2D(x, y), new Vector2D(cx, cy));
                    if (dist < radius // if within splash circle
                            && cx - radius > 1 && cx + radius < this.n - 2
                            && cy - radius > 1 && cy + radius < this.n - 2) {
                        this.d[this.I(x, y)] = 127 + (256 * 1 - dist / radius);
                    }
                }
            }
        }
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
