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
package jot.physics.particle_system.SPH;

import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.lang.Math.PI;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.round;
import java.util.ArrayList;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.particle_system.ParticleSystem;
import static jot.physics.particle_system.SPH.ParticleSPH.particlesCount;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements a generic simplistic particle system.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ParticleSystemSPH implements ParticleSystem {

    static final Logger log = getLogger("ParticleSystemSPH");

    /**
     * The display Window width.
     */
    public static final int GWIDTH = 800;
    //public static final int GWIDTH = 1920;

    /**
     * The display Window height.
     */
    public static final int GHEIGHT = 600;
    //public static final int GHEIGHT = 1080;

    /**
     * Damping used in the integration step. Feels a bit hacky, but looks nicer.
     */
    public static float DAMPING = 0.0015f;

    /**
     * The ratio of heat transmission.
     */
    public static float HEAT_RATIO = 1.0f;

    /**
     * One of the two constants used for the shear-and-bulk viscosity. Should
     * not be altered at the moment, since this might induce cancer.
     */
    public static float V_ALPHA = 5.0f;

    /**
     * One of the two constants used for the shear-and-bulk viscosity. Should
     * not be altered at the moment, since this might induce cancer.
     */
    public static float V_BETA = 10.0f;

    /**
     * Time stepping constant. The simulation will run faster if the time step
     * is set higher, but the accuracy will decrease.
     */
    public static float TIMESTEP = 0.6f;

    /**
     * The unit length used in the equations. you might choose this to be
     * smaller, but don't forget to adjust the rest density.
     */
    public static final float UNIT_SCALE = 0.07f;

    /**
     * Inverse of the unit length used in the equations. you might choose this
     * to be smaller, but don't forget to adjust the rest density.
     */
    public static float INV_UNIT_SCALE = 1.0f / UNIT_SCALE;

    /**
     * Scale constant used to map between pixel space and world space. You can
     * set this smaller if you want to fit more water on the screen.
     */
    public static final float WORLD_SCALE = 0.5f * 0.35f;

    /**
     * Inverse of the scale constant used to map between pixel space and world
     * space. You can set this smaller if you want to fit more water on the
     * screen.
     */
    public static final float INV_WORLD_SCALE = 1.0f / WORLD_SCALE;

    /**
     * Width of the container (=window) in world space.
     */
    public static float CONTAINER_WIDTH = GWIDTH * UNIT_SCALE * INV_WORLD_SCALE;

    /**
     * Height of the container (=window) in world space.
     */
    public static final float CONTAINER_HEIGHT = GHEIGHT * UNIT_SCALE * INV_WORLD_SCALE;

    /**
     * If a fluid particle approaches a boundary particle, this distance is used
     * to determine, whether it will feel repulsive force.
     */
    public static final float REPULSIVE_DISTANCE = 30.0f * UNIT_SCALE;

    /**
     * Inverse of the if a fluid particle approaches a boundary particle, this
     * distance is used to determine, whether it will feel repulsive force.
     */
    public static float INV_REPULSIVE_DISTANCE = 1.0f / REPULSIVE_DISTANCE;

    /**
     * Power of 2 of the if a fluid particle approaches a boundary particle,
     * this distance is used to determine, whether it will feel repulsive force.
     */
    public static float REPULSIVE_DIST_SQ = REPULSIVE_DISTANCE * REPULSIVE_DISTANCE;

    /**
     * Force scale for the repulsive force between fluid and boundary particles.
     */
    public static float REPULSIVE_D = 0.5f;

    /**
     * The radius in which particles will interact with each other.
     */
    public static final float SMOOTHING_LENGTH = 55 * UNIT_SCALE;

    /**
     * Power of 2 of the radius in which particles will interact with each
     * other.
     */
    public static float SMOOTHING_LENGTH_SQ = SMOOTHING_LENGTH * SMOOTHING_LENGTH;

    /**
     * Different kernel normalization factors and their derivatives used for
     * smoothing attributes between particles.
     */
    public static float INV_SMOOTHING_LENGTH = 1.0f / SMOOTHING_LENGTH;

    /**
     * Poly6.
     */
    public static final float D_KERNEL_FACTOR = 315.0f / (64.0f * (float) PI * (float) pow(SMOOTHING_LENGTH, 9));

    /**
     * Spiky (gradient).
     */
    public static float P_KERNEL_FACTOR = -45.0f / ((float) PI * (float) pow(SMOOTHING_LENGTH, 6));

    /**
     * Poly6 derivative (I//m not sure if this one is right, since I had to
     * calculate it on my own).
     */
    public static float D_KERNEL_FACTOR_GRADIENT = -6.0f * D_KERNEL_FACTOR;

    /**
     * Volume of a particle used to calculate the particle mass.
     */
    public static float PARTICLE_AREA = (float) pow(SMOOTHING_LENGTH * 0.3f, 2) * (float) PI;

    /**
     * The epsilon factor used in the xSPH variant to provide more orderly
     * high-speed flows (makes the simulation more stable too).
     */
    public static float XSPH_EPSILON = 0.5f;

    /**
     * Gravity.
     */
    public static final Vector3D GRAVITY = new Vector3D(0.0f * UNIT_SCALE, 0.5f * UNIT_SCALE, 0);

    /**
     * The density the fluid would have if it was at rest, used to initialize
     * particles and calculate the pressure.
     */
    public static final float REST_DENSITY = 0.35f;

    /**
     * Inverse of the density the fluid would have if it was at rest, used to
     * initialize particles and calculate the pressure.
     */
    public static float INV_REST_DENSITY = 1.0f / REST_DENSITY;

    /**
     * Scaling constant used for the pressure and speed-of-sound calculation.
     */
    public static float P_B = (float) GRAVITY.getY() * CONTAINER_HEIGHT / 7.0f * 3;

    /**
     * Minimal distance that water particles should have.
     */
    public static float PARTICLE_SPACING = 30 * WORLD_SCALE;

    /**
     * Same for boundary particles.
     */
    public static float BOUNDARY_SPACING = 30 * WORLD_SCALE;

    /**
     * The simulation grid width.
     */
    public static int GridWidth;

    /**
     * The simulation grid height.
     */
    public static int GridHeight;

    static {
        log.setLevel(OFF);
    }

    private int listParticles;
    private final ParticleSPH[][] FluidGrid;
    private final ParticleSPH[][] BoundaryGrid;
    private final ArrayList<ParticleSPH> Particles = new ArrayList<>();
    private final ArrayList<ParticleSPH> BoundaryParticles = new ArrayList<>();

    /**
     * Constructor.
     */
    public ParticleSystemSPH() {
        GridWidth = round((float) ceil(CONTAINER_WIDTH * INV_SMOOTHING_LENGTH));
        GridHeight = round((float) ceil(CONTAINER_HEIGHT * INV_SMOOTHING_LENGTH));

        this.FluidGrid = new ParticleSPH[GridWidth][GridHeight];
        this.BoundaryGrid = new ParticleSPH[GridWidth][GridHeight];
    }

    /**
     * Reset the particle system, to an initial configuration.
     */
    public void reset() {
        this.Particles.clear();
        this.BoundaryParticles.clear();

        if (GWIDTH == 1_920 && GHEIGHT == 1_080) {
            for (double y = 100; y <= 500; y += PARTICLE_SPACING) {
                for (double x = 400; x <= 600; x += PARTICLE_SPACING) {
                    this.AddParticle(new Vector3D(x + random() - 0.5, y + random() - 0.5, -1));
                }
            }

            for (double y = 1_060; y <= 1_079; y += PARTICLE_SPACING) {
                for (double x = 1; x <= 1_000; x += PARTICLE_SPACING) {
                    this.AddParticle(new Vector3D(x + random() - 0.5, y + random() - 0.5, -1));
                }
            }

            for (double y = 800; y <= 815; y += BOUNDARY_SPACING) {
                for (double x = 50; x <= 550; x += BOUNDARY_SPACING) {
                    this.AddBoundaryParticle(new Vector3D(x, y, -1));
                }
            }

            for (double y = 1_000; y <= 1_079; y += BOUNDARY_SPACING) {
                for (double x = 1_000; x <= 1_020; x += BOUNDARY_SPACING) {
                    this.AddBoundaryParticle(new Vector3D(x + 80, y, -1));
                    //AddBoundaryParticle(new Vector3D(x + 270, y, -1));
                }
            }
        }

        if (GWIDTH == 800 && GHEIGHT == 600) {
            for (double y = 50; y <= 400; y += PARTICLE_SPACING) {
                for (double x = 100; x <= 260; x += PARTICLE_SPACING) {
                    this.AddParticle(new Vector3D(x + random() - 0.5, y + random() - 0.5, -1));
                }
            }

            for (double y = 580; y <= 599; y += PARTICLE_SPACING) {
                for (double x = 1; x <= 799; x += PARTICLE_SPACING) {
                    this.AddParticle(new Vector3D(x + random() - 0.5, y + random() - 0.5, -1));
                }
            }

            for (double y = 500; y <= 515; y += BOUNDARY_SPACING) {
                for (double x = 50; x <= 550; x += BOUNDARY_SPACING) {
                    this.AddBoundaryParticle(new Vector3D(x, y, -1));
                }
            }

            for (double y = 50; y <= 450; y += BOUNDARY_SPACING) {
                for (double x = 0; x <= 15; x += BOUNDARY_SPACING) {
                    this.AddBoundaryParticle(new Vector3D(x + 80, y, -1));
                    this.AddBoundaryParticle(new Vector3D(x + 270, y, -1));
                }
            }
        }
    }

    /**
     * Fills the particles in a spatial grid whose cells contain linked lists of
     * particles.
     */
    private void redistributeGrid() {
        for (int x = 0; x < GridWidth; x++) {
            for (int y = 0; y < GridHeight; y++) {
                this.FluidGrid[x][y] = null;
                this.BoundaryGrid[x][y] = null;
            }
        }

        this.Particles.stream().forEach(p -> p.redistributeGrid(this.FluidGrid));
        this.BoundaryParticles.stream().forEach(p -> p.redistributeGrid(this.BoundaryGrid));
    }

    /**
     * Calculates the new density, pressure, mass and sound speed values.
     */
    private void computeDensities() {
        this.Particles.stream().forEach(p -> p.computeDensities1(this.FluidGrid));
        this.Particles.stream().forEach(p -> p.computeDensities2());
    }

    /**
     * Compute the influence of surrounding accelerations.
     */
    private void computeStressTensors() {
        this.Particles.stream().forEach(p -> p.computeStressTensors1());
        this.Particles.stream().forEach(p -> p.computeStressTensors2(this.FluidGrid, this.BoundaryGrid));
    }

    /**
     * Consider the influence from surrounding accelerations, e.g., additional
     * accelerations such as gravity, and update particles position and
     * velocity.
     */
    private void addAccelerations() {
        this.Particles.stream().forEach(p -> p.addAccelerations());
    }

    /**
     * Removes all fluid and boundary particles that are inside the specified
     * rectangle.
     *
     * @param Startx the min x value in OpenGL coordinates of the rectangle.
     * @param Starty the min y value in OpenGL coordinates of the rectangle.
     * @param Endx the max x value in OpenGL coordinates of the rectangle.
     * @param Endy the max x value in OpenGL coordinates of the rectangle.
     */
    public void freeRect(float Startx, float Starty, float Endx, float Endy) {
        int ArraySize = this.Particles.size();

        for (int i = 0; i < this.Particles.size(); i++) {
            if (i >= ArraySize) {
                break;
            }

            ParticleSPH p = this.Particles.get(i);

            if (p.isDead(Startx, Starty, Endx, Endy)) {
                this.Particles.remove(p);
                particlesCount -= 1;
                ArraySize -= 1;
                i -= 1;
            }
        }

        ArraySize = this.BoundaryParticles.size();

        for (int i = 0; i < this.BoundaryParticles.size(); i++) {
            if (i >= ArraySize) {
                break;
            }

            ParticleSPH p = this.BoundaryParticles.get(i);

            if (p.isDead(Startx, Starty, Endx, Endy)) {
                this.BoundaryParticles.remove(p);
                particlesCount -= 1;
                ArraySize -= 1;
                i -= 1;
            }
        }
    }

    /**
     * Method to create an new fluid particle.
     *
     * @param position coordinates (x,y) in OpenGL 2D coordinates.
     */
    public void AddParticle(Vector3D position) {
        this.Particles.add(new ParticleSPH(new Vector3D(
                position.getX() * UNIT_SCALE * INV_WORLD_SCALE,
                position.getY() * UNIT_SCALE * INV_WORLD_SCALE,
                position.getZ())));
    }

    /**
     * Method to create an new boundary particle.
     *
     * @param position coordinates (x,y) in OpenGL 2D coordinates.
     */
    public void AddBoundaryParticle(Vector3D position) {
        this.BoundaryParticles.add(new ParticleSPH(new Vector3D(
                position.getX() * UNIT_SCALE * INV_WORLD_SCALE,
                position.getY() * UNIT_SCALE * INV_WORLD_SCALE,
                position.getZ())));
    }

    @Override
    public boolean isDead() {
        return this.Particles.isEmpty() && this.BoundaryParticles.isEmpty();
    }

    @Override
    public void render(GL2 gl) {
        gl.glPushMatrix();
        {
            gl.glDeleteLists(this.listParticles, 1);
            this.listParticles = gl.glGenLists(1);
            gl.glNewList(this.listParticles, GL_COMPILE);
            {
                //Draw fluid particles
                this.Particles.stream().forEach(p -> {
                    float Ratio = min(max((p.Density + REST_DENSITY * 0.0f) / (2.0f * REST_DENSITY), 0.0f), 1.0f);
                    float[] color = {1.0f - Ratio, 1.0f - Ratio, 1.0f, 1.0f};
                    p.render(gl, color, ZERO, new Vector3D(2, 2, 0.5));
                });

                //Draw boundaries
                this.BoundaryParticles.stream().forEach(p -> {
                    float[] color = {1.0f, 0.5f, 0.5f, 1.0f};
                    p.render(gl, color, new Vector3D(-3, -3, 0), new Vector3D(3, 3, 0.5));
                });
            }
            gl.glEndList();

            gl.glDisable(GL_LIGHTING);
            gl.glCallList(this.listParticles);
            gl.glEnable(GL_LIGHTING);
        }
        gl.glPopMatrix();
    }

    @Override
    public void update(float dt) {
        this.redistributeGrid();
        this.computeDensities();
        this.computeStressTensors();
        this.addAccelerations();
    }
}
