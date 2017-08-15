/*
 * This file is part of the JOT game engine A.I. extension toolkit component.
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
package jot.ai;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that each A.I. agent must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractAgent {

    /**
     * Get the A.I. agent Id.
     *
     * @return the A.I. agent Id.
     */
    public abstract String getId();

    /**
     * Set the A.I. agent Id.
     *
     * @param agentId the A.I. agent Id to set.
     */
    public abstract void setId(String agentId);

    /**
     * Get the bounding radius of interaction of this agent with other agents.
     *
     * @return the bounding radius of interaction of this agent with other
     * agents.
     */
    public abstract float getBoundingRadius();

    /**
     * Set the bounding radius of interaction of this agent with other agents.
     *
     * @param radius of interaction of this agent with other agents.
     */
    public abstract void setBoundingRadius(float radius);

    /**
     * Get the maximum speed at which this agent can go.
     *
     * @return the maximum speed at which this agent can go.
     */
    public abstract float getMaxSpeed();

    /**
     * Set the maximum speed at which this agent can go.
     *
     * @param maxSpeed at which this agent can go.
     */
    public abstract void setMaxSpeed(float maxSpeed);

    /**
     * Get the A.I. agent position.
     *
     * @return the A.I. agent position.
     */
    public abstract Vector3D getPosition();

    /**
     * Set the A.I. agent position.
     *
     * @param position the A.I. agent position to set.
     */
    public abstract void setPosition(Vector3D position);

    /**
     * Get the A.I. agent rotation.
     *
     * @return the A.I. agent rotation.
     */
    public abstract Vector3D getRotation();

    /**
     * Set the A.I. agent rotation.
     *
     * @param rotation the A.I. agent rotation to set.
     */
    public abstract void setRotation(Vector3D rotation);

    /**
     * Get the A.I. agent velocity.
     *
     * @return the A.I. agent velocity.
     */
    public abstract Vector3D getVelocity();

    /**
     * Set the A.I. agent velocity.
     *
     * @param velocity the A.I. agent velocity to set.
     */
    public abstract void setVelocity(Vector3D velocity);
}
