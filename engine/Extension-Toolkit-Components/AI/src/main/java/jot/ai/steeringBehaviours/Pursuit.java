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
import static java.lang.Math.signum;
import static java.util.logging.Level.INFO;
import jot.ai.Agent;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds pursuit steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Pursuit extends Seek {

    protected Agent secondaryGoal;

    /**
     * Default constructor.
     */
    public Pursuit() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }

        this.secondaryGoal = new Agent("pursuitSecondaryGoal", 0, 0, ZERO, ZERO, ZERO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.pursuit(this.primaryGoal);
    }

    /**
     * Update the position of the A.I. controlled agent, using the pursuit
     * logic. Pursuit differs from seek in the way it's an attempt to predict
     * the target future position.
     *
     * @param evader agent to pursuit.
     * @return the steering velocity.
     */
    protected Vector3D pursuit(Agent evader) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //if the evader is ahead and facing the agent then we can just seek
            //for the evader's current position.
            Vector3D ToEvader = evader.getPosition().subtract(this.primaryAgent.getPosition());
            //System.out.println(ToEvader);

            Vector3D pbHeading = new Vector3D(
                    signum(this.primaryAgent.getVelocity().getX()),
                    signum(this.primaryAgent.getVelocity().getY()),
                    signum(this.primaryAgent.getVelocity().getZ()));
            Vector3D eHeading = new Vector3D(
                    signum(evader.getVelocity().getX()),
                    signum(evader.getVelocity().getY()),
                    signum(evader.getVelocity().getZ()));

            double RelativeHeading = pbHeading.dotProduct(eHeading);
            //System.out.println(RelativeHeading);

            //System.out.println(ToEvader.dotProduct(pbVelNorm));
            if (ToEvader.dotProduct(pbHeading) > 0
                    && RelativeHeading < -0.95) { //acos(0.95)=18 degs
                //System.out.println("the evader is ahead and facing the agent");
                return this.seek(evader.getPosition());
            }
            //System.out.println("the evader is not ahead and/or facing the agent");

            //Not considered ahead so we predict where the evader will be.
            //the look-ahead time is proportional to the distance between the evader
            //and the pursuer; and is inversely proportional to the sum of the
            //agents' velocities.
            double LookAheadTime = ToEvader.getNorm()
                    / (this.primaryAgent.getMaxSpeed() + evader.getVelocity().getNorm());
            //System.out.println(LookAheadTime);

            //now seek to the predicted future position of the evader.
            Vector3D predictedFuturePosition = evader.getPosition().add(
                    evader.getVelocity().scalarMultiply(LookAheadTime));
            //System.out.println(secondaryGoal.getPosition());
            this.secondaryGoal.setPosition(predictedFuturePosition);
            //System.out.println(predictedFuturePosition);
            return this.seek(predictedFuturePosition);
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
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() - .75d);

                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() - .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() + .75d);

                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() + .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() + .75d);

                    //triangle 2
                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() - .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() - .75d);

                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() + .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() + .75d);

                    gl.glVertex3d(
                            this.secondaryGoal.getPosition().getX() + .75d,
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.secondaryGoal.getPosition().getZ() - .75d);
                }
                gl.glEnd();
            }
            gl.glEnable(GL_LIGHTING);
        }
    }
}
