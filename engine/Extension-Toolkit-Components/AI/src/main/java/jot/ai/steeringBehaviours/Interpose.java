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
import static java.util.logging.Level.INFO;
import jot.ai.Agent;
import static jot.ai.steeringBehaviours.AbstractSteeringBehavior.log;
import static jot.physics.Kinematics.translatePolar;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds interpose steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Interpose extends Arrive {

    protected Vector3D MidPoint = ZERO;

    protected Agent secondaryGoal;

    /**
     * Default constructor.
     */
    public Interpose() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * Set the secondary goal.
     *
     * @param secondaryGoal either a secondary target position/rotation, an
     * identifier for a goal stored in a transform group, etc., not necessarily
     * a player.
     */
    public void setSecondaryGoal(Agent secondaryGoal) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.secondaryGoal = secondaryGoal;
            this.setAgentLog(this.secondaryGoal);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.interpose(this.primaryGoal, this.secondaryGoal);
    }

    /**
     * The Update the position of the A.I. controlled agent, using the interpose
     * logic.
     *
     * @param A agent.
     * @param B agent.
     * @return the steering velocity.
     */
    protected Vector3D interpose(Agent A, Agent B) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //first we need to figure out where the two agents are going to be at
            //time T in the future. This is approximated by determining the time
            //taken to reach the midway point at the current time at max speed.
            this.MidPoint = new Vector3D(
                    A.getPosition().getX() + B.getPosition().getX(),
                    A.getPosition().getY() + B.getPosition().getY(),
                    A.getPosition().getZ() + B.getPosition().getZ())
                    .scalarMultiply(0.5);
            //System.out.println(A.getPosition());
            //System.out.println(B.getPosition());
            //System.out.println(MidPoint);

            double TimeToReachMidPoint
                    = this.primaryAgent.getPosition().distance(this.MidPoint)
                    / this.primaryAgent.getMaxSpeed();
            //System.out.println(TimeToReachMidPoint);

            //now we have T, we assume that agent A and agent B will continue on a
            //straight trajectory and extrapolate to get their future positions
            //Vector3D APos = A.getPosition().add(A.getVelocity().scalarMultiply(TimeToReachMidPoint));
            //Vector3D BPos = B.getPosition().add(B.getVelocity().scalarMultiply(TimeToReachMidPoint));
            Vector3D APos = translatePolar(
                    A.getPosition(),
                    A.getVelocity(),
                    (float) A.getRotation().getY(),
                    0,
                    (float) TimeToReachMidPoint);
            Vector3D BPos = translatePolar(
                    B.getPosition(),
                    B.getVelocity(),
                    (float) B.getRotation().getY(),
                    0,
                    (float) TimeToReachMidPoint);
            //System.out.println(APos + " " + BPos);

            //calculate the midpoint of these predicted positions
            this.MidPoint = APos.add(BPos).scalarMultiply(0.5);
            //System.out.println(MidPoint);

            //then steer to arrive at it
            return this.arrive(this.MidPoint, this.deceleration);
        }
        return ZERO;
    }

    /**
     * Render an arrow pointing to the direction the agent is moving.
     *
     * @param gl
     */
    @Override
    protected void renderDirection(GL2 gl) {
        super.renderDirection(gl);
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            gl.glDisable(GL_LIGHTING);
            {
                gl.glBegin(GL_TRIANGLES);
                {
                    /* secondary target position */
                    gl.glColor3f(0.0f, 1.0f, 0.0f);

                    //triangle 1
                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() - .75d,
                            this.secondaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() - .75d);

                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() - .75d,
                            this.secondaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() + .75d);

                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() + .75d,
                            this.secondaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() + .75d);

                    //triangle 2
                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() - .75d,
                            this.secondaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() - .75d);

                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() + .75d,
                            this.secondaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() + .75d);

                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() + .75d,
                            this.secondaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() - .75d);

                    /* midpoint target predicted position */
                    gl.glColor3f(0.0f, 0.0f, 1.0f);

                    //triangle 1
                    gl.glVertex3d(
                            this.MidPoint.getX() - .75d,
                            this.MidPoint.getY() + .01d,
                            this.MidPoint.getZ() - .75d);

                    gl.glVertex3d(
                            this.MidPoint.getX() - .75d,
                            this.MidPoint.getY() + .01d,
                            this.MidPoint.getZ() + .75d);

                    gl.glVertex3d(
                            this.MidPoint.getX() + .75d,
                            this.MidPoint.getY() + .01d,
                            this.MidPoint.getZ() + .75d);

                    //triangle 2
                    gl.glVertex3d(
                            this.MidPoint.getX() - .75d,
                            this.MidPoint.getY() + .01d,
                            this.MidPoint.getZ() - .75d);

                    gl.glVertex3d(
                            this.MidPoint.getX() + .75d,
                            this.MidPoint.getY() + .01d,
                            this.MidPoint.getZ() + .75d);

                    gl.glVertex3d(
                            this.MidPoint.getX() + .75d,
                            this.MidPoint.getY() + .01d,
                            this.MidPoint.getZ() - .75d);
                }
                gl.glEnd();
            }
            gl.glEnable(GL_LIGHTING);
        }
    }
}
