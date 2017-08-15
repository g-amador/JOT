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

import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_LINE_SMOOTH;
import static com.jogamp.opengl.GL.GL_LINE_STRIP;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.random;
import static java.lang.Math.sin;
import java.util.ArrayList;
import static java.util.logging.Level.INFO;
import jot.ai.Agent;
import static jot.ai.steeringBehaviours.AbstractSteeringBehavior.log;
import static jot.math.Distance.getDistance;
import static jot.physics.Kinematics.translatePolar;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds wander steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Wander extends Seek {

    /**
     * The sceneSize of a squared scene or the radius of a spherical scene.
     */
    protected float sceneSize;

    protected Vector3D wanderTarget;
    //protected float wanderAngle;

    /**
     * The distance the wander circle is projected in front of the agent.
     */
    protected float wanderDistance;

    /**
     * The maximum amount of random displacement that can be added to the target
     * each second.
     */
    protected float wanderJitter;

    /**
     * The radius of the constraining circle.
     */
    protected float wanderRadius;

    /**
     * Time left to move in certain direction when using wander curve logic.
     */
    protected long wanderTimeCount;

    /**
     * Maximum time in milliseconds to move in certain direction when using
     * wander curve logic.
     */
    protected long wanderTime;

    /**
     * Default constructor.
     */
    public Wander() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }

        //Since the wander primary target Agent initially is null we setup the 
        //primary target.
        this.primaryGoal = new Agent("wanderAreaTarget", 0, 0, ZERO, ZERO, ZERO);
        this.wanderTarget = ZERO;

        //Default parameters values.
        //wanderAngle = (float) PI;
        this.wanderDistance = 30;
        this.wanderJitter = 8;
        this.wanderRadius = 5;
        this.wanderTimeCount = 10;
    }

    /**
     * Set the size of a game world map with size x size x size, i.e., assuming
     * a map with the same height, length, and width.
     *
     * @param sceneSize of a game world map with size x size x size.
     */
    public void setSceneSize(float sceneSize) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.sceneSize = sceneSize;
        }
    }

    /**
     * Set the wander distance for Craig Reynolds wander steering behavior.
     *
     * @param wanderDistance
     */
    public void setWanderDistance(float wanderDistance) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.wanderDistance = wanderDistance;
        }
    }

    /**
     * Set the wander jitter for Craig Reynolds wander steering behavior.
     *
     * @param wanderJitter
     */
    public void setWanderJitter(float wanderJitter) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.wanderJitter = wanderJitter;
        }
    }

    /**
     * Set the wander radius for Craig Reynolds wander steering behavior.
     *
     * @param wanderRadius
     */
    public void setWanderRadius(float wanderRadius) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.wanderRadius = wanderRadius;
        }
    }

    /**
     * Set the time until choosing the next wander direction.
     *
     * @param wanderTime until choosing the next wander direction.
     */
    public void setWanderTime(long wanderTime) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.wanderTime = wanderTime;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.wander();
    }

    /**
     * The wander method.
     *
     * @return the steering velocity.
     */
    protected Vector3D wander() {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //if agent already reached the target reset wanderTime and choose 
            //another target.
            if (this.isTargetCloseEnough(this.primaryGoal.getPosition())) {
                this.wanderTimeCount = 0;
            }

            //if time already passed choose new target and set time max time to 
            //try to get there.
            if (this.wanderTimeCount < 1) {
                this.wanderTimeCount = (long) (random() * this.wanderTime);
                //System.out.println(wanderTimeCount);

                //first, add a small random vector to the target’s position.               
                this.wanderTarget = this.wanderTarget.add(new Vector3D(
                        (random() * 2 - 1) * this.wanderJitter,
                        0,
                        (random() * 2 - 1) * this.wanderJitter));
                //System.out.println(wanderTarget);

                //reproject this new vector back onto a unit circle.
                this.wanderTarget = this.wanderTarget.equals(ZERO)
                        ? this.wanderTarget : this.wanderTarget.normalize();
                //System.out.println(wanderTarget);

                //increase the length of the vector to the same as the radius
                //of the wander circle.
                this.wanderTarget = this.wanderTarget.scalarMultiply(this.wanderRadius);
                //System.out.println(wanderTarget);

                //move the target into a position WanderDist in front of the 
                //agent.
                Vector3D targetLocal = this.wanderTarget.add(new Vector3D(
                        this.wanderDistance, 0, 0));
                //System.out.println(targetLocal);

                //project the target into world space.
                double rotation = this.getDirection2Target(ZERO, targetLocal);
                //System.out.println(rotation);

                Vector3D targetWorld = translatePolar(this.primaryAgent.getPosition(), this.wanderDistance,
                        (float) (this.primaryAgent.getRotation().getY() + rotation),
                        //(float) primaryAgent.getRotation().getY(),
                        //(float) rotation,
                        0, 1);
                //System.out.println(targetWorld);

                //clamp the new target position within scene bounds.
                float area = (this.sceneSize - this.sceneSize / 10);
                targetWorld = targetWorld.equals(ZERO)
                        ? targetWorld
                        : targetWorld.normalize().scalarMultiply(area - area * 0.5f);
                //System.out.println(targetWorld);

                //System.out.println(primaryGoal.getPosition());    
                //primaryGoal.setPosition(targetWorld.subtract(
                //    primaryAgent.getPosition()));
                this.primaryGoal.setPosition(targetWorld);
                //System.out.println(primaryGoal.getPosition());                           

                /*
                    //Alternative
                    //Calculate the circle center.
                    Vector3D circleCenter = primaryAgent.getVelocity();
                    //System.out.println(circleCenter);
                    circleCenter = circleCenter.equals(ZERO)
                            ? circleCenter
                            : circleCenter.normalize().scalarMultiply(wanderDistance);
                    //System.out.println(circleCenter);

                    //Calculate the displacement force.
                    Vector3D displacement
                            = new Vector3D(-1, 0, 0).scalarMultiply(wanderRadius);
                    //System.out.println(displacement);

                    //Randomly change the vector direction by making it change its 
                    //current angle.           
                    float len = (float) displacement.getNorm();
                    displacement = new Vector3D(cos(wanderAngle) * len, 0, sin(wanderAngle) * len);
                    //System.out.println(displacement);
                
                    //Change wanderAngle just a bit, so it won't have the same value 
                    //in the next game frame.
                    wanderAngle += ((random() * wanderJitter) - wanderJitter * .5);
                    //System.out.println(wanderAngle);

                    //Finally calculate and return the wander force
                    Vector3D wanderForce = circleCenter.add(displacement);
                    wanderTarget = primaryGoal.getPosition();

                    //clamp the new target position within scene bounds.
                    float area = (sceneSize - sceneSize / 10);
                    wanderForce = wanderForce.equals(ZERO)
                            ? wanderForce
                            : wanderForce.normalize().scalarMultiply(area - area * 0.5f);
                    //System.out.println(targetWorld);

                    //System.out.println(primaryGoal.getPosition());            
                    primaryGoal.setPosition(wanderForce);
                    //System.out.println(primaryGoal.getPosition());
                 */
            }

            this.wanderTimeCount--;

            //and steer toward it
            return this.seek(this.primaryGoal.getPosition());

        }
        return ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            gl.glDisable(GL_LIGHTING);
            {
                int num_segments = 20;
                float[] blue = {0.0f, 0.0f, 0.6f};
                float[] red = {0.6f, 0.0f, 0.0f};

                //Render the displacement arc.
                this.renderCurve(gl, GL_LINE_STRIP, red, num_segments, .02f, true);

                //Render the wander circle.
                this.renderCurve(gl, GL_LINE_STRIP, blue, num_segments, .01f, false);
                //Render the steering force vector.
                gl.glColor3f(blue[0], blue[1], blue[2]);
                gl.glBegin(GL_LINES);
                {
                    gl.glVertex3d(
                            this.primaryAgent.getPosition().getX(),
                            this.primaryAgent.getPosition().getY() + .01d,
                            this.primaryAgent.getPosition().getZ());
                    gl.glVertex3d(
                            this.primaryGoal.getPosition().getX(),
                            this.primaryGoal.getPosition().getY() + .01d,
                            this.primaryGoal.getPosition().getZ());
                }
                gl.glEnd();
            }
            gl.glEnable(GL_LIGHTING);
        }
    }

    /**
     * Render a curve, i.e., a circle or a arc.
     *
     * @param gl
     * @param mode the primitive used to render.
     * @param color the color to render the curve-
     * @param num_segments the number of line segments used to render the curve.
     * @param y
     * @param renderArc toggle on/off render arc.
     */
    protected void renderCurve(GL2 gl, int mode, float[] color,
            float num_segments, float y, boolean renderArc) {
        Vector3D velocity = new Vector3D(1, 1, 1)
                .scalarMultiply(-this.wanderRadius);
        Vector3D target = translatePolar(
                this.primaryGoal.getPosition(),
                velocity, this.getDirection2Target(this.primaryGoal.getPosition()),
                0, 1);

        int startIndex = 0, endIndex = (int) num_segments;

        if (renderArc) {
            ArrayList<Vector3D> vertexes = new ArrayList<>();
            float minOldTargetPosition = POSITIVE_INFINITY;
            float minTargetPosition = POSITIVE_INFINITY;

            for (int i = startIndex; i < endIndex; i++) {
                float theta = (float) (2.0f * PI * i / num_segments);//get the current angle 

                float x = (float) (this.wanderRadius * cos(theta));//calculate the x component 
                float z = (float) (this.wanderRadius * sin(theta));//calculate the y component 

                Vector3D vertex = new Vector3D(
                        x + target.getX(),
                        y + target.getY(),
                        z + target.getZ());
                vertexes.add(vertex);
            }

            for (int i = 0; i < vertexes.size(); i++) {
                Vector3D vertex = vertexes.get(i);
                //float distance1 = (float) getDistance(oldTargetPosition, vertex);
                float distance1 = (float) getDistance(this.wanderTarget, vertex);
                float distance2 = (float) getDistance(this.primaryGoal.getPosition(), vertex);

                if (distance1 < minOldTargetPosition) {
                    minOldTargetPosition = distance1;
                    startIndex = i;
                }

                if (distance2 < minTargetPosition) {
                    minTargetPosition = distance2;
                    endIndex = i;
                }
            }

            //make sure that the start and end indexes are in right order.
            if (startIndex > endIndex) {
                int temp = startIndex;
                startIndex = endIndex;
                endIndex = temp;
            }

            //make sure that the start and end indexes differ.
            endIndex++;

        }

        gl.glEnable(GL_LINE_SMOOTH);
        {
            gl.glBegin(mode);
            {
                gl.glColor3f(color[0], color[1], color[2]);
                for (int i = startIndex; i < endIndex; i++) {
                    float theta = (float) (2.0f * PI * i / num_segments);//get the current angle 

                    float x = (float) (this.wanderRadius * cos(theta));//calculate the x component 
                    float z = (float) (this.wanderRadius * sin(theta));//calculate the y component 

                    gl.glVertex3d(
                            x + target.getX(),
                            y + target.getY(),
                            z + target.getZ()); //output vertex 
                }

                if (!renderArc) {
                    float theta = (float) (2.0f * PI * startIndex / num_segments);//get the current angle 

                    float x = (float) (this.wanderRadius * cos(theta));//calculate the x component 
                    float z = (float) (this.wanderRadius * sin(theta));//calculate the y component 

                    gl.glVertex3d(
                            x + target.getX(),
                            y + target.getY(),
                            z + target.getZ()); //output vertex                 
                }
            }
            gl.glEnd();
        }
        gl.glDisable(GL_LINE_SMOOTH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(GL2 gl) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
