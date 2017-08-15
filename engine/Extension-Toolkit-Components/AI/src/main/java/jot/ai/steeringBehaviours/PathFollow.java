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
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.util.logging.Level.INFO;
import jot.ai.Agent;
import static jot.util.CoreOptions.coreOptions;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds path follow steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class PathFollow extends Arrive {

    protected float lineWitdh;

    /**
     * The index of the next target node in the path to follow.
     */
    protected int pathIndex;

    /**
     * Pointer to the display list used to render the steering behavior.
     */
    protected int listPath;

    /**
     * The path to follow.
     */
    protected Vector3D[] path = {
        new Vector3D(-25, 0, 40),
        new Vector3D(30, 0, 40),
        new Vector3D(30, 0, -40),
        new Vector3D(-35, 0, -40),
        new Vector3D(-35, 0, -35),
        new Vector3D(-25, 0, -35),
        new Vector3D(-25, 0, 0),
        new Vector3D(-35, 0, 20),
        new Vector3D(-25, 0, 40),}; //The default path to use
//    protected Vector3D[] path = {
//        new Vector3D(-87, 0, 32),
//        new Vector3D(-56, 0, 45),
//        new Vector3D(-40, 0, 75),
//        new Vector3D(-15, 0, 12),
//        new Vector3D(0, 0, 35),
//        new Vector3D(55, 0, 55),
//        new Vector3D(70, 0, 30),
//        new Vector3D(50, 0, 10),
//        new Vector3D(50, 0, -50)
//    }; //Alternative test path to use    
//    protected Vector3D[] path = {new Vector3D(-50, 0, -50),
//        new Vector3D(50, 0, 50),}; //Alternative test path to use
//    protected Vector3D[] path = {new Vector3D(0, 0, -50),
//        new Vector3D(0, 0, 50),}; //Alternative test path to use

    /**
     * Default pathColor is red;
     */
    protected Vector3D pathColor = new Vector3D(0.0f, 0.0f, 1.0f);

    /**
     * Default constructor.
     */
    public PathFollow() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }

        //Since the path follow primary target Agent initially is null we setup
        //the primary target.
        this.primaryGoal = new Agent("pathFollowTarget", 0, 0, ZERO, ZERO, ZERO);

        this.pathIndex = 0;
        this.lineWitdh = 1.5f;
    }

    /**
     * Get the A.I. controlled agent current path to follow.
     *
     * @return path which corresponds to the A.I. controlled agent path to
     * follow.
     */
    public Vector3D[] getPath() {
        return extensionAIOptions.get("useSteeringBehaviors") ? this.path : null;
    }

    /**
     * Set a new path to follow, for the A.I. controlled agent.
     *
     * @param path the new path for the A.I. controlled agent to follow.
     */
    public void setPath(Vector3D[] path) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.pathIndex = 0;
            this.path = path;
        }
    }

    /**
     * Set the path to follow render color.
     *
     * @param color the color to render the path to follow lines formed by each
     * pair of nodes.
     */
    public void setPathColor(Vector3D color) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.pathColor = color;
        }
    }

    /**
     * Set the line width of the path to render.
     *
     * @param lineWitdh to set.
     */
    public void setPathLineWitdh(float lineWitdh) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.lineWitdh = lineWitdh;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.pathFollow();
    }

    /**
     * Update the position of the A.I. controlled agent, using path follow
     * logic.
     *
     * @return the steering velocity.
     */
    protected Vector3D pathFollow() {
        if (extensionAIOptions.get("useSteeringBehaviors")
                && extensionAIOptions.get("useSteeringBehaviorsPathFollow")
                && this.path != null) {
            //set primary agent rotation in the direction of target to seek.   
            //System.out.println(primaryAgent.getRotation());
            //primaryAgent.setRotation(new Vector3D(
            //        primaryAgent.getRotation().getX(),
            //        getDirection2Target(primaryAgent.getPosition(), primaryGoal.getPosition()),            
            //        getDirection2Target(targetPosition, primaryAgent.getPosition()),
            //        primaryAgent.getRotation().getZ()));
            //System.out.println(primaryAgent.getRotation());

            //if (!primaryTarget.getPosition().equals(path[pathIndex])) {                
            //    System.out.println(pathIndex);
            //    System.out.println(primaryTarget.getPosition());
            //}
            this.primaryGoal.setPosition(this.path[this.pathIndex]);
            //if (!primaryTarget.getPosition().equals(path[pathIndex])) {                
            //    System.out.println(pathIndex);
            //    System.out.println(primaryTarget.getPosition());
            //}

            //move to next target if close enough to current target (working in
            //distance squared space)            
            if (this.isTargetCloseEnough(this.primaryGoal.getPosition())) {
                if (extensionAIOptions.get("useSteeringBehaviorsLoopPathFollow")) {
                    //Next entry in path array
                    this.pathIndex++;

                    //Cycle the path
                    this.pathIndex %= this.path.length;
                } else if (this.path.length - 1 > this.pathIndex) { //Cycle the path and when arrived at ending position stop.
                    //Next entry in path array
                    this.pathIndex++;
                }
            }

            if (!extensionAIOptions.get("useSteeringBehaviorsLoopPathFollow")
                    && this.path.length - 1 == this.pathIndex) {
                //System.out.println("arrive");
                return this.arrive(this.primaryGoal.getPosition(), this.deceleration);
            } else {
                //System.out.println("seek");
                return this.seek(this.primaryGoal.getPosition());
            }
        }
        return ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        if (extensionAIOptions.get("useSteeringBehaviors")
                && this.path != null && extensionAIOptions.get("showPath")) {
            //render steer direction.
            if (!extensionAIOptions.get("usePathFinders")) {
                this.renderDirection(gl);
            }

            //render path.
            gl.glPushMatrix();
            {
                gl.glDeleteLists(this.listPath, 1);
                this.listPath = gl.glGenLists(1);
                gl.glNewList(this.listPath, GL_COMPILE);
                {
                    gl.glColor3d(
                            this.pathColor.getX(),
                            this.pathColor.getY(),
                            this.pathColor.getZ());
                    gl.glLineWidth(this.lineWitdh);
                    gl.glBegin(GL_LINES);
                    {
                        for (int i = this.pathIndex; i < this.path.length - 1; i++) {
                            if (this.path[i] != null && this.path[i + 1] != null) {
                                gl.glVertex3d(
                                        this.path[i].getX(),
                                        coreOptions.get("HOG2Maps")
                                        ? 0.3d : this.path[i].getY() + 0.3d,
                                        this.path[i].getZ());
                                gl.glVertex3d(
                                        this.path[i + 1].getX(),
                                        coreOptions.get("HOG2Maps")
                                        ? 0.3d : this.path[i + 1].getY() + 0.3d,
                                        this.path[i + 1].getZ());
                            }
                        }
                    }
                    gl.glEnd();
                }
                gl.glEndList();

                gl.glDisable(GL_LIGHTING);
                gl.glCallList(this.listPath);
                gl.glEnable(GL_LIGHTING);
            }
            gl.glPopMatrix();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(GL2 gl) {
        if (extensionAIOptions.get("useSteeringBehaviors") && gl != null) {
            log.info("Dispose listPath.");
            gl.glDeleteLists(this.listPath, 1);
        }
    }
}
