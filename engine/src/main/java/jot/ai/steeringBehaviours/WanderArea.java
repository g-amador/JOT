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

import static java.lang.Math.random;
import static java.lang.Math.round;
import static java.util.logging.Level.INFO;
import jot.ai.Agent;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements non Craig Reynolds wander steering behavior. Easy
 * solution just generate next target position randomly and go that way for some
 * random time.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class WanderArea extends Seek {

    /**
     * The sceneSize of a squared scene or the radius of a spherical scene.
     */
    protected float sceneSize;

    /**
     * Time left to move in certain direction when using wander area logic.
     */
    protected long wanderTimeCount;

    /**
     * Maximum time in milliseconds to move in certain direction when using
     * wander area logic.
     */
    protected long wanderTime;

    /**
     * Default constructor.
     */
    public WanderArea() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }

        //Since the wander primary target Agent initially is null we setup the 
        //primary target.
        this.primaryGoal = new Agent("wanderAreaTarget", 0, 0, ZERO, ZERO, ZERO);

        this.wanderTimeCount = this.wanderTime = 240;
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
     * Set the time until choosing the next wander direction.
     *
     * @param wanderTime until choosing the next wander direction.
     */
    public void setWanderTime(long wanderTime) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.wanderTime = wanderTime;
            this.wanderTimeCount = wanderTime;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.wanderArea();
    }

    /**
     * The wander area method.
     *
     * @return the steering velocity.
     */
    protected Vector3D wanderArea() {
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

                //choose new target position within scene bounds if time got to 
                //zero. 
                float area = (this.sceneSize - this.sceneSize / 10)
                        - (this.sceneSize - this.sceneSize / 10) * 0.5f;
                //System.out.println(primaryGoal.getPosition());
                this.primaryGoal.setPosition(new Vector3D(
                        round((float) (random() * area * 2 - area)),
                        this.primaryGoal.getPosition().getY(),
                        round((float) (random() * area * 2 - area))));
                //System.out.println(primaryGoal.getPosition());
            }
            this.wanderTimeCount--;

            //go to the last random determined target position.
            return this.seek(this.primaryGoal.getPosition());
        }
        return ZERO;
    }
}
