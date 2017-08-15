/*
 * This file is part of the JOT game engine managers framework toolkit
 * component. 
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
package jot.manager.ai;

import com.jogamp.opengl.GL2;
import static java.lang.String.format;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.ai.Agent;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that specifies the methods that each A.I. class should
 * implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractAI {

    private static final Logger log = getLogger("AbstractAI");

    static {
        log.setLevel(OFF);
    }

    protected Agent primaryAgent;
    protected Agent primaryGoal;
    protected Agent secondaryGoal;

    /**
     * Get the player/agent to be controlled by an AI.
     *
     * @return the agent assigned game object.
     */
    public Agent getPrimaryAgent() {
        return this.primaryAgent;
    }

    /**
     * Set the player/agent to be controlled by an AI.
     *
     * @param primaryAgent the agent assigned game object.
     */
    public void setPrimaryAgent(GameObject primaryAgent) {
        this.primaryAgent = new Agent(
                primaryAgent.getId(),
                primaryAgent.getMaxSpeed(),
                primaryAgent.getBoundingRadius(),
                new Vector3D(primaryAgent.getPosition().toArray()),
                new Vector3D(primaryAgent.getRotation().toArray()),
                new Vector3D(primaryAgent.getVelocity().toArray()));
        this.setAgentLog(this.primaryAgent);
    }

    /**
     * Get the primary goal. This can be either a target position/rotation, an
     * identifier for a goal stored in a transform group, etc., not necessarily
     * a player.
     *
     * @return the primary goal.
     */
    public Agent getPrimaryGoal() {
        return this.primaryGoal;
    }

    /**
     * Set the primary goal. This can be either a target position/rotation, an
     * identifier for a goal stored in a transform group, etc., not necessarily
     * a player.
     *
     * @param primaryGoal either a target position/rotation, an identifier for a
     * goal stored in a transform group.
     */
    public void setPrimaryGoal(GameObject primaryGoal) {
        this.primaryGoal = new Agent(
                primaryGoal.getId(),
                primaryGoal.getMaxSpeed(),
                primaryGoal.getBoundingRadius(),
                new Vector3D(primaryGoal.getPosition().toArray()),
                new Vector3D(primaryGoal.getRotation().toArray()),
                new Vector3D(primaryGoal.getVelocity().toArray()));
        this.setAgentLog(this.primaryGoal);
    }

    /**
     * Log info for an agent set.
     *
     * @param agent
     */
    protected void setAgentLog(Agent agent) {
        log.info(format("Agent Id:" + agent.getId()
                + "\nPosition:" + agent.getPosition()
                + "\nRotation:" + agent.getRotation()
                + "\nVelocity:" + agent.getVelocity()));
    }

    /**
     * Render information regarding the used AI, e.g., in pathFollow the path to
     * follow.
     *
     * @param gl
     */
    public abstract void renderAI(GL2 gl);

    /**
     * Dispose all used display lists.
     *
     * @param gl
     */
    public abstract void dispose(GL2 gl);
}
