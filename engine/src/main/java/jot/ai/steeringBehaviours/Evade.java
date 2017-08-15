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
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds evade steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Evade extends Flee {

    protected Vector3D oldPosition;
    protected Agent secondaryGoal;

    /**
     * Default constructor.
     */
    public Evade() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }

        this.oldPosition = ZERO;
        this.secondaryGoal = new Agent("evadeSecondaryGoal", 0, 0, ZERO, ZERO, ZERO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.evade(this.primaryGoal);
    }

    /**
     * Update the position of the A.I. controlled agent, using the evade logic.
     * Evade differs from flee in the way it's an attempt to predict the target
     * future position.
     *
     * @param pursuer the agent to evade.
     * @return the steering velocity.
     */
    protected Vector3D evade(Agent pursuer) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            /* Not necessary to include the check for facing direction this time. */
            Vector3D ToPursuer = pursuer.getPosition().subtract(this.primaryAgent.getPosition());
            //System.out.println(ToPursuer);

            //the look-ahead time is proportional to the distance between the pursuer
            //and the evader; and is inversely proportional to the sum of the
            //agents' velocities.    
            double LookAheadTime = ToPursuer.getNorm()
                    / (this.primaryAgent.getMaxSpeed() + pursuer.getVelocity().getNorm());
            //System.out.println(LookAheadTime);

            //now flee away from predicted future position of the pursuer.                        
            Vector3D predictedFuturePosition = pursuer.getPosition().add(
                    pursuer.getVelocity().scalarMultiply(LookAheadTime));
            //System.out.println(secondaryGoal.getPosition());
            this.secondaryGoal.setPosition(predictedFuturePosition);
            //System.out.println(predictedFuturePosition);
            return this.flee(predictedFuturePosition);
        }
        return ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void renderDirection(GL2 gl) {
        super.renderDirection(gl);
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            gl.glDisable(GL_LIGHTING);
            {
                gl.glBegin(GL_TRIANGLES);
                {
                    /* secondary target predicted position */
                    gl.glColor3f(0.0f, 0.0f, 1.0f);

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
                }
                gl.glEnd();
            }
            gl.glEnable(GL_LIGHTING);
        }
    }
}
