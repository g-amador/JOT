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

import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a A.I. agent.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Agent extends AbstractAgent {

    static final Logger log = getLogger("Agent");

    static {
        log.setLevel(OFF);
    }

    protected String Id;

    protected Vector3D position;
    protected Vector3D rotation;
    protected Vector3D velocity;
    protected float maxSpeed;
    protected float boundingRadius;

    /**
     * Default constructor.
     *
     * @param Id of the A.I. agent.
     * @param maxSpeed at which the A.I. agent can go.
     * @param boundingRadius of the A.I. agent.
     * @param position of the A.I. agent.
     * @param rotation of the A.I. agent.
     * @param velocity of the A.I. agent.
     */
    public Agent(String Id, float maxSpeed, float boundingRadius,
            Vector3D position, Vector3D rotation, Vector3D velocity) {
        this.Id = Id;
        this.maxSpeed = maxSpeed;
        this.boundingRadius = boundingRadius;
        this.position = position;
        this.rotation = rotation;
        this.velocity = velocity;

    }

    /**
     * Default constructor.
     *
     * @param go game object.
     */
    public Agent(GameObject go) {
        this.Id = go.getId();
        this.maxSpeed = go.getMaxSpeed();
        this.boundingRadius = go.getBoundingRadius();
        this.position = go.getPosition();
        this.rotation = go.getRotation();
        this.velocity = go.getVelocity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return this.Id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(String agentId) {
        this.Id = agentId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getBoundingRadius() {
        return this.boundingRadius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoundingRadius(float radius) {
        this.boundingRadius = radius;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getMaxSpeed() {
        return this.maxSpeed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getPosition() {
        return this.position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPosition(Vector3D position) {
        this.position = position;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getRotation() {
        return this.rotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRotation(Vector3D rotation) {
        this.rotation = rotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getVelocity() {
        return this.velocity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVelocity(Vector3D velocity) {
        this.velocity = velocity;
    }
}
