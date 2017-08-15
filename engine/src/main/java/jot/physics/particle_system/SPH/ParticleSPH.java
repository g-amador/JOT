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

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import com.jogamp.opengl.GL2;
import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.particle_system.Particle;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.CONTAINER_HEIGHT;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.CONTAINER_WIDTH;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.DAMPING;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.D_KERNEL_FACTOR;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.D_KERNEL_FACTOR_GRADIENT;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.GRAVITY;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.GridHeight;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.GridWidth;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.HEAT_RATIO;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.INV_REPULSIVE_DISTANCE;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.INV_REST_DENSITY;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.INV_SMOOTHING_LENGTH;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.INV_UNIT_SCALE;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.PARTICLE_AREA;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.P_B;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.P_KERNEL_FACTOR;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.REPULSIVE_D;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.REPULSIVE_DISTANCE;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.REPULSIVE_DIST_SQ;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.REST_DENSITY;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.SMOOTHING_LENGTH;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.SMOOTHING_LENGTH_SQ;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.TIMESTEP;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.V_ALPHA;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.V_BETA;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.WORLD_SCALE;
import static jot.physics.particle_system.SPH.ParticleSystemSPH.XSPH_EPSILON;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements a generic simplistic particle.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class ParticleSPH implements Particle {

    static final Logger log = getLogger("ParticleSPH");

    /**
     * The amount of alive particles in the system.
     */
    public static int particlesCount = 0;

    static {
        log.setLevel(OFF);
    }

    /**
     * The particle successor particle.
     */
    public ParticleSPH Succ;

    /**
     * The density of the particle.
     */
    public float Density;

    private float DeltaDensity;
    private float InvDensity;
    private float InvDensitySq;
    private float Mass;
    private float Pressure;
    private float SoundSpeed;
    private Vector3D Position = ZERO;
    private Vector3D OldPosition = ZERO;
    private Vector3D Screen = ZERO;
    private Vector3D Velocity = ZERO; //Velocity of the particle
    private Vector3D DeltaVelocity = ZERO;
    private Vector3D Force = ZERO;

    private final float Id;
    private final int[] Dirx = {-1, -1, 1, 1, -1, 1, 0, 0, 0};
    private final int[] Diry = {-1, 1, -1, 1, 0, 0, -1, 1, 0};

    /**
     * Constructor.
     *
     * @param position coordinates (x,y) in OpenGL 2D coordinates.
     */
    public ParticleSPH(Vector3D position) {
        this.Position = new Vector3D(position.toArray());
        this.OldPosition = new Vector3D(position.toArray());
        this.Screen = new Vector3D(
                position.getX() * INV_UNIT_SCALE * WORLD_SCALE,
                position.getY() * INV_UNIT_SCALE * WORLD_SCALE,
                0);
        this.Density = REST_DENSITY; //Initialize the density and mass
        this.Mass = PARTICLE_AREA * REST_DENSITY;
        this.Id = particlesCount;
        particlesCount++;
    }

    /**
     * Fills the particles in a spatial grid whose cells contain linked lists of
     * particles.
     *
     * @param grid a generic grid to swap values from.
     */
    public void redistributeGrid(ParticleSPH[][] grid) {
        int Gridx = round((float) floor(this.Position.getX() * INV_SMOOTHING_LENGTH));
        int Gridy = round((float) floor(this.Position.getY() * INV_SMOOTHING_LENGTH));
        ParticleSPH Successor = grid[Gridx][Gridy];
        grid[Gridx][Gridy] = this;
        this.Succ = Successor;
    }

    /**
     * Calculates the new density, pressure, mass and sound speed values.
     *
     * @param grid a generic grid to swap values from.
     */
    public void computeDensities1(ParticleSPH[][] grid) {
        int intx = (int) (this.Position.getX() * INV_SMOOTHING_LENGTH);
        int inty = (int) (this.Position.getY() * INV_SMOOTHING_LENGTH);

        for (int j = 0; j <= 8; j++) {
            int Gridx = intx + this.Dirx[j];
            int Gridy = inty + this.Diry[j];

            if (Gridx < 0 || Gridy < 0 || Gridx >= GridWidth || Gridy >= GridHeight) {
                continue;
            }

            ParticleSPH p = grid[Gridx][Gridy];

            //Iterate through the linked list
            while (p != null) {
                //Do each pair just once
                if (this.Id > p.Id) {
                    float Dx = (float) (this.Position.getX() - p.Position.getX());
                    float Dy = (float) (this.Position.getY() - p.Position.getY());

                    float DSQ = Dx * Dx + Dy * Dy;

                    if (DSQ < SMOOTHING_LENGTH_SQ) {
                        float D = (float) sqrt(DSQ);

                        float R = SMOOTHING_LENGTH_SQ - DSQ;

                        float DensityFactor = (float) (((this.Velocity.getX() - p.Velocity.getX()) * Dx + (this.Velocity.getY() - p.Velocity.getY()) * Dy) * R * R / D);

                        this.DeltaDensity += p.Mass * DensityFactor;
                        p.DeltaDensity += this.Mass * DensityFactor;
                    }
                }

                p = p.Succ;
            }
        }
    }

    /**
     * Calculates the new density, pressure, mass and sound speed values.
     */
    public void computeDensities2() {
        this.Density += this.DeltaDensity * D_KERNEL_FACTOR_GRADIENT;

        if (this.Density < 0.3f || this.Density > 0.4f) {
            this.Density = REST_DENSITY * 1.1f;
        }

        this.InvDensity = 1.0f / this.Density;
        this.InvDensitySq = this.InvDensity * this.InvDensity;

        this.Mass = this.Density * PARTICLE_AREA;

        this.Pressure = P_B * this.Density * ((float) pow(this.Density * INV_REST_DENSITY, 7.0) - 1.0f);

        this.SoundSpeed = (float) sqrt(abs(HEAT_RATIO * this.Pressure * this.InvDensity));
    }

    /**
     * Reset the second order derivatives of the position and the first
     * derivative of the density.
     */
    public void computeStressTensors1() {
        this.Force = GRAVITY.scalarMultiply(this.Density);
        this.DeltaVelocity = ZERO;
        this.DeltaDensity = 0.0f;
    }

    /**
     * Finish recalculating the new stress tensor per particle and enforce
     * boundary conditions.
     *
     * @param FluidGrid the grid of fluids particles.
     * @param BoundaryGrid the grid of boundary particles.
     */
    public void computeStressTensors2(ParticleSPH[][] FluidGrid, ParticleSPH[][] BoundaryGrid) {
        //See the UpdateDensity method for more information about the grid iteration
        int Intx = (int) (this.Position.getX() * INV_SMOOTHING_LENGTH);
        int Inty = (int) (this.Position.getY() * INV_SMOOTHING_LENGTH);

        for (int j = 0; j <= 8; j++) {
            int Gridx = Intx + this.Dirx[j];
            int Gridy = Inty + this.Diry[j];

            if (Gridx < 0 || Gridy < 0 || Gridx >= GridWidth || Gridy >= GridHeight) {
                continue;
            }
            ParticleSPH p = FluidGrid[Gridx][Gridy];

            while (p != null) {
                if (this.Id > p.Id) {
                    float Dx = (float) (this.Position.getX() - p.Position.getX());
                    float Dy = (float) (this.Position.getY() - p.Position.getY());

                    float DSQ = Dx * Dx + Dy * Dy;

                    if (DSQ < SMOOTHING_LENGTH_SQ) {
                        float D = (float) sqrt(DSQ);

                        float R = SMOOTHING_LENGTH - D;

                        float VDx = (float) (p.Velocity.getX() - this.Velocity.getX());
                        float VDy = (float) (p.Velocity.getY() - this.Velocity.getY());

                        float Pressureforce = this.Pressure * this.InvDensitySq + p.Pressure * p.InvDensitySq; //Pressure force

                        float InvDensityAverage = 2.0f / (this.Density + p.Density);

                        float DotP = -Dx * VDx - Dy * VDy;

                        if (DotP < 0.0) { //Shear-bulk-viscosity
                            float M = SMOOTHING_LENGTH * DotP / (DSQ + 20.0f);

                            Pressureforce += (-V_ALPHA * M * (this.SoundSpeed + p.SoundSpeed) * 0.5f + V_BETA * M * M) * InvDensityAverage;
                        }

                        Pressureforce *= -p.Mass * P_KERNEL_FACTOR * R * R / D;

                        Dx *= Pressureforce;
                        Dy *= Pressureforce;

                        this.Force = new Vector3D(
                                this.Force.getX() + Dx,
                                this.Force.getY() + Dy,
                                0);
                        p.Force = new Vector3D(
                                p.Force.getX() - Dx,
                                p.Force.getY() - Dy,
                                0);

                        float R2 = SMOOTHING_LENGTH_SQ - DSQ;

                        //The xSPH variant introduced by Monaghan
                        float KernelFactor = XSPH_EPSILON * p.Mass * R2 * R2 * R2 * D_KERNEL_FACTOR * InvDensityAverage;

                        VDx *= KernelFactor;
                        VDy *= KernelFactor;

                        this.DeltaVelocity = new Vector3D(
                                this.DeltaVelocity.getX() + VDx,
                                this.DeltaVelocity.getY() + VDy,
                                0);
                        p.DeltaVelocity = new Vector3D(
                                p.DeltaVelocity.getX() - VDx,
                                p.DeltaVelocity.getY() - VDy,
                                0);
                    }
                }

                p = p.Succ;
            }

            p = BoundaryGrid[Gridx][Gridy];

            //Basic repulsive force calculation for boundary/fluid particle collision
            while (p != null) {
                float Dx = (float) (this.Position.getX() - p.Position.getX());
                float Dy = (float) (this.Position.getY() - p.Position.getY());

                float DSQ = Dx * Dx + Dy * Dy;

                if (DSQ < REPULSIVE_DIST_SQ) {
                    float InvDSQ = 1.0f / DSQ;

                    float RSQ = REPULSIVE_DIST_SQ * InvDSQ;

                    float RepulsiveFactor = REPULSIVE_D * (RSQ * RSQ - RSQ) * InvDSQ;

                    this.Force = new Vector3D(
                            this.Force.getX() + Dx * RepulsiveFactor,
                            this.Force.getY() + Dy * RepulsiveFactor,
                            0);
                }

                p = p.Succ;
            }
        }

        float Dist1 = (float) max(CONTAINER_WIDTH - this.Position.getX(), 0.0); //Repulsive force for the screen boundaries
        float Dist2 = (float) max(CONTAINER_HEIGHT - this.Position.getY(), 0.0);
        float Dist3 = (float) max(this.Position.getX(), 0.0);
        float Dist4 = (float) max(this.Position.getY(), 0.0);

        float x = (float) this.Force.getX();
        float y = (float) this.Force.getY();

        if (Dist1 < REPULSIVE_DISTANCE) {
            x -= 1.0f * (1.0f - (float) sqrt(Dist1 * INV_REPULSIVE_DISTANCE)) * this.Density;
        }
        if (Dist2 < REPULSIVE_DISTANCE) {
            y -= 1.0f * (1.0f - (float) sqrt(Dist2 * INV_REPULSIVE_DISTANCE)) * this.Density;
        }
        if (Dist3 < REPULSIVE_DISTANCE) {
            x += 1.0f * (1.0f - (float) sqrt(Dist3 * INV_REPULSIVE_DISTANCE)) * this.Density;
        }
        if (Dist4 < REPULSIVE_DISTANCE) {
            y += 1.0f * (1.0f - (float) sqrt(Dist4 * INV_REPULSIVE_DISTANCE)) * this.Density;
        }
        this.Force = new Vector3D(x, y, 0);
    }

    /**
     * Consider the influence from surrounding accelerations, e.g., additional
     * accelerations such as gravity, and update particles position and
     * velocity.
     */
    public void addAccelerations() {
        //Basic position verlet for timestepping
        float Oldx = (float) this.Position.getX();
        float Oldy = (float) this.Position.getY();

        float x = (float) this.Position.getX();
        float y = (float) this.Position.getY();

        x += (1.0f - DAMPING) * (x - this.OldPosition.getX())
                + this.DeltaVelocity.getX() * TIMESTEP + (TIMESTEP * TIMESTEP) * this.Force.getX();
        y += (1.0f - DAMPING) * (y - this.OldPosition.getY())
                + this.DeltaVelocity.getY() * TIMESTEP + (TIMESTEP * TIMESTEP) * this.Force.getY();

        x = (float) max(min(x, CONTAINER_WIDTH), 0.0);
        y = (float) max(min(y, CONTAINER_HEIGHT), 0.0);

        this.Position = new Vector3D(x, y, this.Position.getZ());

        this.OldPosition = new Vector3D(Oldx, Oldy, this.OldPosition.getZ());

        this.Velocity = new Vector3D(
                (this.Position.getX() - Oldx) * (1.0f / TIMESTEP),
                (this.Position.getY() - Oldy) * (1.0f / TIMESTEP),
                0);

        float DSQ = (float) (this.Velocity.getX() * this.Velocity.getX() + this.Velocity.getY() * this.Velocity.getY());

        //Limit the velocity to restrain the cancer in spreading. Otherwise, the simulation goes *woof* and *boom*
        if (DSQ > 4.0) {
            float Factor = (float) sqrt(4.0 / DSQ);

            this.Position = new Vector3D(Oldx, Oldy, this.Position.getZ());

            this.Velocity = new Vector3D(
                    this.Velocity.getX() * Factor,
                    this.Velocity.getY() * Factor,
                    0);

            this.Position = new Vector3D(
                    this.Position.getX() + this.Velocity.getX() * TIMESTEP,
                    this.Position.getY() + this.Velocity.getY() * TIMESTEP,
                    this.Position.getZ());

        }

        //Calculate the position in pixel space for rendering
        this.Screen = new Vector3D(
                this.Position.getX() * INV_UNIT_SCALE * WORLD_SCALE,
                this.Position.getY() * INV_UNIT_SCALE * WORLD_SCALE,
                0);
    }

    @Override
    public boolean isDead() {
        return false;
    }

    /**
     * See if particle is bye bye.
     *
     * @param Startx
     * @param Starty
     * @param Endx
     * @param Endy
     *
     * @return TRUE, if particle dead, FALSE otherwise.
     */
    public boolean isDead(float Startx, float Starty, float Endx, float Endy) {
        return this.Screen.getX() >= Startx && this.Screen.getX() <= Endx && this.Screen.getY() >= Starty && this.Screen.getY() <= Endy;
        //return false;        
    }

    @Override
    public void render(GL2 gl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Draw a particle.
     *
     * @param gl
     * @param color color of the particle.
     * @param min the minimum x and y coordinates offset.
     * @param max the maximum x and y coordinates offset.
     */
    public void render(GL2 gl, float[] color, Vector3D min, Vector3D max) {
        gl.glColor4f(color[0], color[1], color[2], color[3]);
        //TODO: remove when down code properly tested
//        gl.glBegin(GL_QUADS);
//        gl.glVertex3d(Screen.getX() + min.getX(), Screen.getY() + min.getY(), Screen.getZ() + max.getZ());
//        gl.glVertex3d(Screen.getX() + max.getX(), Screen.getY() + min.getY(), Screen.getZ() + max.getZ());
//        gl.glVertex3d(Screen.getX() + max.getX(), Screen.getY() + max.getY(), Screen.getZ() + max.getZ());
//        gl.glVertex3d(Screen.getX() + min.getX(), Screen.getY() + max.getY(), Screen.getZ() + max.getZ());
//        gl.glEnd();
        gl.glBegin(GL_TRIANGLES);
        {
            //Triangle 1
            gl.glVertex3d(this.Screen.getX() + min.getX(), this.Screen.getY() + min.getY(), this.Screen.getZ() + max.getZ());
            gl.glVertex3d(this.Screen.getX() + max.getX(), this.Screen.getY() + min.getY(), this.Screen.getZ() + max.getZ());
            gl.glVertex3d(this.Screen.getX() + max.getX(), this.Screen.getY() + max.getY(), this.Screen.getZ() + max.getZ());

            //Triangle 2
            gl.glVertex3d(this.Screen.getX() + min.getX(), this.Screen.getY() + min.getY(), this.Screen.getZ() + max.getZ());
            gl.glVertex3d(this.Screen.getX() + max.getX(), this.Screen.getY() + max.getY(), this.Screen.getZ() + max.getZ());
            gl.glVertex3d(this.Screen.getX() + min.getX(), this.Screen.getY() + max.getY(), this.Screen.getZ() + max.getZ());
        }
        gl.glEnd();
    }

    @Override
    public void update(float dt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
