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
import static com.jogamp.opengl.GL2.GL_COMPILE;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static java.lang.Double.POSITIVE_INFINITY;
import java.util.ArrayList;
import java.util.Iterator;
import static java.util.logging.Level.INFO;
import jot.ai.Agent;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds hide steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Hide extends AbstractSteeringBehavior {

    /**
     * The sceneSize of a squared scene or the radius of a spherical scene.
     */
    protected float sceneSize;

    /**
     * The available hiding spots.
     */
    protected ArrayList<Vector3D> hidingSpots;

    /**
     * Terrain obstacles.
     */
    protected Iterator<GameObject> obstacles;

    /**
     * The steering behavior to use.
     */
    protected AbstractSteeringBehavior steeringBehavior;

    /**
     * Pointer to the display list used to render the steering behavior.
     */
    protected int listHidingSpots;

    /**
     * Default constructor.
     */
    public Hide() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }

        this.steeringBehavior = null;
        this.hidingSpots = new ArrayList<>();
    }

    /**
     * Set the terrain obstacles iterator.
     *
     * @param obstacles iterator.
     */
    public void setObstacles(Iterator<GameObject> obstacles) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.obstacles = obstacles;
        }
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
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.hide(this.primaryGoal, this.obstacles);
    }

    /**
     * The distance to each of these spots is determined. The vehicle then uses
     * the arrive behavior to steer toward the closest. If no appropriate
     * obstacles can be found, the vehicle evades the target.
     *
     * @param target to hide/evade from.
     * @param obstacles in the game world map.
     * @return the steering force.
     */
    protected Vector3D hide(Agent target, Iterator<GameObject> obstacles) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            double DistanceToClosest = POSITIVE_INFINITY;
            Vector3D BestHidingSpot = ZERO;
            this.hidingSpots.clear();

            while (obstacles != null && obstacles.hasNext()) {
                GameObject curOb = obstacles.next();

                //calculate the position of the hiding spot for this obstacle.
                Vector3D HidingSpot = this.GetHidingPosition(curOb.getPosition(),
                        curOb.getBoundingRadius(), target.getPosition());
                //System.out.println(curOb.getPosition());
                //System.out.println(curOb.getBoundingRadius());
                //System.out.println(HidingSpot);
                this.hidingSpots.add(HidingSpot);

                //work in distance-squared space to find the closest hiding
                //spot to the agent.
                double distance = HidingSpot.distanceSq(this.primaryAgent.getPosition());
                //System.out.println(distance);

                if (distance < DistanceToClosest) {
                    DistanceToClosest = distance;
                    BestHidingSpot = HidingSpot;
                    //System.out.println(DistanceToClosest);
                    //System.out.println(BestHidingSpot);
                }
            }

            //if no suitable obstacles found then evade the target.
            if (DistanceToClosest == POSITIVE_INFINITY) {
                //System.out.println("Evade");
                this.steeringBehavior = new Evade();
                this.steeringBehavior.setPrimaryAgent(this.primaryAgent);
                this.steeringBehavior.setPrimaryGoal(target);
                this.steeringBehavior.setDistance2TargetThreshold(this.sceneSize / 5);
                return ((Evade) this.steeringBehavior).evade(target);
            }

            //else use Arrive on the hiding spot.
            //System.out.println("Arrive");
            target.setPosition(BestHidingSpot);
            this.primaryGoal.setPosition(BestHidingSpot);
            this.steeringBehavior = new Arrive();
            this.steeringBehavior.setPrimaryAgent(this.primaryAgent);
            this.steeringBehavior.setPrimaryGoal(target);
            this.steeringBehavior.setDistance2TargetThreshold(this.sceneSize / 20);
            return ((Arrive) this.steeringBehavior).arrive(BestHidingSpot, 1);
        }
        return ZERO;
    }

    /**
     * Determine a hiding spot for an obstacle.
     *
     * @param posOb location of an obstacle.
     * @param radiusOb radius of an obstacle.
     * @param posTarget from which we want to hide.
     * @return a hiding spot for an obstacle.
     */
    protected Vector3D GetHidingPosition(Vector3D posOb, double radiusOb,
            Vector3D posTarget) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //calculate how far away the agent is to be from the chosen 
            //obstacle’s bounding radius.
            double DistanceFromBoundary = radiusOb * 2; //30.0;
            //System.out.println(DistanceFromBoundary);

            double DistAway = radiusOb + DistanceFromBoundary;
            //System.out.println(DistAway);

            //calculate the heading toward the object from the target
            Vector3D ToOb = posOb.subtract(posTarget);
            ToOb = ToOb.equals(ZERO) ? ToOb : ToOb.normalize();
            //System.out.println(ToOb);

            //scale it to size and add to the obstacle's position to get
            //the hiding spot.
            //System.out.println(ToOb.scalarMultiply(DistAway).add(posOb));
            return ToOb.scalarMultiply(DistAway).add(posOb);
        }
        return ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        this.renderDirection(gl);

        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //render path.
            gl.glPushMatrix();
            {
                gl.glDeleteLists(this.listHidingSpots, 1);
                this.listHidingSpots = gl.glGenLists(1);
                gl.glNewList(this.listHidingSpots, GL_COMPILE);
                {
                    gl.glBegin(GL_TRIANGLES);
                    {
                        /* hiding spot position */
                        gl.glColor3f(0.0f, 0.0f, 1.0f);
                        this.hidingSpots.stream().forEach(hidingSpot -> {
                            //triangle 1
                            gl.glVertex3d(
                                    hidingSpot.getX() - .75d,
                                    this.primaryAgent.getPosition().getY() + .01d,
                                    hidingSpot.getZ() - .75d);

                            gl.glVertex3d(
                                    hidingSpot.getX() - .75d,
                                    this.primaryAgent.getPosition().getY() + .01d,
                                    hidingSpot.getZ() + .75d);

                            gl.glVertex3d(
                                    hidingSpot.getX() + .75d,
                                    this.primaryAgent.getPosition().getY() + .01d,
                                    hidingSpot.getZ() + .75d);

                            //triangle 2
                            gl.glVertex3d(
                                    hidingSpot.getX() - .75d,
                                    this.primaryAgent.getPosition().getY() + .01d,
                                    hidingSpot.getZ() - .75d);

                            gl.glVertex3d(
                                    hidingSpot.getX() + .75d,
                                    this.primaryAgent.getPosition().getY() + .01d,
                                    hidingSpot.getZ() + .75d);

                            gl.glVertex3d(
                                    hidingSpot.getX() + .75d,
                                    this.primaryAgent.getPosition().getY() + .01d,
                                    hidingSpot.getZ() - .75d);
                        });
                    }
                    gl.glEnd();
                }
                gl.glEndList();

                gl.glDisable(GL_LIGHTING);
                gl.glCallList(this.listHidingSpots);
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
            log.info("Dispose listHidingSpots.");
            gl.glDeleteLists(this.listHidingSpots, 1);
        }
    }
}
