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
package jot.physics.particle_system;

import com.jogamp.opengl.GL2;

/**
 * Interface that specifies the core methods to implement for a particle system.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface ParticleSystem {

    /**
     * If no more particles alive the particle system is dead.
     *
     * @return True if no more alive particles, false otherwise.
     */
    boolean isDead();

    /**
     * Render all particles in the system
     *
     * @param gl
     */
    void render(GL2 gl);

    /**
     * Update particle system.
     *
     * @param dt simulation time step.
     */
    void update(float dt);
}
