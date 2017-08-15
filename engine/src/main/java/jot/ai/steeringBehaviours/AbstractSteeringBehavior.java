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
package jot.ai.steeringBehaviours;

import static com.jogamp.opengl.GL.GL_TRIANGLES;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.lang.Float.NaN;
import static java.lang.Math.acos;
import static java.lang.Math.toDegrees;
import static java.lang.String.format;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.ai.Agent;
import static jot.math.Distance.getDistance;
import static jot.physics.Kinematics.translatePolar;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Abstract class that each steering behavior class should implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractSteeringBehavior {

    static final Logger log = getLogger("SteeringBehavior");

    static {
        log.setLevel(OFF);
    }

    protected Agent primaryAgent;
    protected Agent primaryGoal;

    /**
     * Toggle moving primary agent on/off
     */
    protected boolean isMoving = false;

    /**
     * Threshold distance between the primary agent and target position.
     */
    protected float distance2TargetThreshold;

    /**
     * Get the distance of A.I. controlled agent to a given target.
     *
     * @param targetPosition position in Cartesian coordinates of given target.
     * @return distance of A.I. controlled agent to a given target.
     */
    public float getDistance2Target(Vector3D targetPosition) {
        return extensionAIOptions.get("useSteeringBehaviors")
                ? this.getDistance2Target(targetPosition, this.primaryAgent.getPosition())
                : NaN;
    }

    /**
     * Get the distance from given agent to given target position.
     *
     * @param targetPosition position in Cartesian coordinates of given target.
     * @param agentPosition position in Cartesian coordinates of given agent.
     * @return distance from pPos to tPos.
     */
    public float getDistance2Target(Vector3D targetPosition, Vector3D agentPosition) {
        return extensionAIOptions.get("useSteeringBehaviors")
                ? (float) getDistance(targetPosition, agentPosition) : NaN;
    }

    /**
     * Get the direction (i.e., rotation) where given target is relatively to AI
     * controlled agent.
     *
     * @param targetPosition position in Cartesian coordinates of given target.
     * @return rotation in order for A.I. controlled agent to look at the given
     * target.
     */
    public float getDirection2Target(Vector3D targetPosition) {
        return extensionAIOptions.get("useSteeringBehaviors")
                ? this.getDirection2Target(targetPosition, this.primaryAgent.getPosition())
                : NaN;
    }

    /**
     * Get the direction (i.e., rotation) where given target is relatively to
     * given agent.
     *
     * @param targetPosition position in Cartesian coordinates of given target.
     * @param agentPosition position in Cartesian coordinates of given agent.
     * @return rotation in order for agent in position pPos to look at given
     * target with position tPos.
     */
    public float getDirection2Target(
            Vector3D targetPosition, Vector3D agentPosition) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            Vector3D direction = new Vector3D(
                    targetPosition.getX() - agentPosition.getX(),
                    0.0f,
                    targetPosition.getZ() - agentPosition.getZ());

            float theta = 0.0f;

            //Calculation of the angle between vector direction and (0,1) agenth centered in the origin  
            //float dotProduct = (0.0f * direction.x) + (1.0f * direction.z);
            //float length_a = getDistance2Target(tPos, new Vector3D (0.0f, 0.0f, 0.0f));
            //float length_b = getDistance2Target(pPos, new Vector3D (0.0f, 0.0f, 0.0f));
            //if ((length_a != 0) && (length_b != 0)) {
            //theta = (float) acos(dotProduct / (length_a * length_b));
            //}
            //In order to do less computation the former operations can be simplified to solely performing
            float orientation_normal = this.getDistance2Target(direction, ZERO);

            if (orientation_normal != 0.0f) {
                theta = (float) acos(direction.getZ() / orientation_normal);
            }

            return direction.getX() < 0
                    ? (float) (toDegrees(-theta)) : (float) (toDegrees(theta));
        }
        return NaN;
    }

    /**
     * Set the threshold distance after which a steering behavior stops, i.e.,
     * goal position achieved.
     *
     * @param threshold the distance after which a steering behavior stops,
     * i.e., goal position achieved.
     */
    public void setDistance2TargetThreshold(float threshold) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.distance2TargetThreshold = threshold;
        }
    }

    /**
     * Get the player/agent to be controlled by an AI.
     *
     * @return the agent assigned player ID.
     */
    public Agent getPrimaryAgent() {
        return extensionAIOptions.get("useSteeringBehaviors")
                ? this.primaryAgent : null;
    }

    /**
     * Set the player/agent to be controlled by an AI.
     *
     * @param primaryAgent the agent assigned player ID.
     */
    public void setPrimaryAgent(Agent primaryAgent) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.primaryAgent = primaryAgent;
            this.setAgentLog(this.primaryAgent);
        }
    }

    /**
     * Get the primary goal. This can be either another agent a position
     * position, etc..
     *
     * @return the primary goal.
     */
    public Agent getPrimaryGoal() {
        return extensionAIOptions.get("useSteeringBehaviors")
                ? this.primaryGoal : null;
    }

    /**
     * Set the primary goal. This can be either another agent a position
     * position, etc..
     *
     * @param primaryGoal this can be either another agent a position position,
     * etc..
     */
    public void setPrimaryGoal(Agent primaryGoal) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.primaryGoal = primaryGoal;
            this.setAgentLog(this.primaryGoal);
        }
    }

    /**
     * Log info for an agent.
     *
     * @param agent whose information will be logged.
     */
    protected void setAgentLog(Agent agent) {
        log.info(format("Agent Id:" + agent.getId()
                + "\nPosition:" + agent.getPosition()
                + "\nRotation:" + agent.getRotation()
                + "\nVelocity:" + agent.getVelocity()));
    }

    /**
     * Test if distance to target lower than threshold.
     *
     * @param targetPosition in Cartesian coordinates.
     * @return TRUE if distance to target lower than threshold, FALSE otherwise.
     */
    protected boolean isTargetCloseEnough(Vector3D targetPosition) {
        return extensionAIOptions.get("useSteeringBehaviors")
                ? this.getDistance2Target(targetPosition) <= this.distance2TargetThreshold
                : false;
    }

    /**
     * Test if distance to target larger than threshold.
     *
     * @param targetPosition in Cartesian coordinates.
     * @return TRUE if distance to target larger than threshold, FALSE
     * otherwise.
     */
    protected boolean isTargetFarEnough(Vector3D targetPosition) {
        return extensionAIOptions.get("useSteeringBehaviors")
                ? this.getDistance2Target(targetPosition) >= this.distance2TargetThreshold
                : false;
    }

    /**
     * Steer using one of Craig Reynolds steering behaviors.
     *
     * @param gl
     */
    public abstract void render(GL2 gl);

    /**
     * Render an arrow pointing to the direction the agent is moving.
     *
     * @param gl
     */
    protected void renderDirection(GL2 gl) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            gl.glDisable(GL_LIGHTING);
            {
                gl.glBegin(GL_TRIANGLES);
                {
                    /* primary agent direction arrow */
                    Vector3D posRight = translatePolar(
                            this.primaryAgent.getPosition(), 2.5f,
                            (float) this.primaryAgent.getRotation().getY(), -90, 1);
                    Vector3D posLeft = translatePolar(
                            this.primaryAgent.getPosition(), 2.5f,
                            (float) this.primaryAgent.getRotation().getY(), 90, 1);
                    Vector3D posFuture = translatePolar(
                            this.primaryAgent.getPosition(), 5,
                            (float) this.primaryAgent.getRotation().getY(), 0, 1);

                    gl.glColor3f(1.0f, 0.0f, 0.0f);
                    gl.glVertex3d(posRight.getX(), posRight.getY() + .01d, posRight.getZ());
                    gl.glVertex3d(posLeft.getX(), posLeft.getY() + .01d, posLeft.getZ());
                    gl.glVertex3d(posFuture.getX(), posFuture.getY() + .01d, posFuture.getZ());

                    /* primary agent position or predicted position */
                    gl.glColor3f(0.0f, 1.0f, 0.0f);

                    //triangle 1
                    gl.glVertex3d(
                            this.primaryGoal.getPosition().getX() - .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.primaryGoal.getPosition().getZ() - .75d);

                    gl.glVertex3d(
                            this.primaryGoal.getPosition().getX() - .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.primaryGoal.getPosition().getZ() + .75d);

                    gl.glVertex3d(
                            this.primaryGoal.getPosition().getX() + .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.primaryGoal.getPosition().getZ() + .75d);

                    //triangle 2
                    gl.glVertex3d(
                            this.primaryGoal.getPosition().getX() - .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.primaryGoal.getPosition().getZ() - .75d);

                    gl.glVertex3d(
                            this.primaryGoal.getPosition().getX() + .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.primaryGoal.getPosition().getZ() + .75d);

                    gl.glVertex3d(
                            this.primaryGoal.getPosition().getX() + .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.primaryGoal.getPosition().getZ() - .75d);
                }
                gl.glEnd();
            }
            gl.glEnable(GL_LIGHTING);
        }
    }

    /**
     * Dispose all used display lists.
     *
     * @param gl
     */
    public abstract void dispose(GL2 gl);

    /**
     * Steer using one of Craig Reynolds steering behaviors.
     *
     * @return the steering velocity.
     */
    public abstract Vector3D steer();
}
