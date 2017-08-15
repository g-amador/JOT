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

import java.util.Iterator;
import jot.util.GameObject;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that each steering behavior class should implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractSteeringBehaviorsManager extends AbstractAI {

    /**
     * Get the A.I. controlled bot current path to follow.
     *
     * @return path which corresponds to the A.I. controlled bot path to follow.
     */
    public abstract Vector3D[] getPath();

    /**
     * Set a new path to follow, for the A.I. controlled bot.
     *
     * @param path the new path for the A.I. controlled bot to follow.
     */
    public abstract void setPath(Vector3D[] path);

    /**
     * Set the path to follow render color.
     *
     * @param color the color to render the path to follow lines formed by each
     * pair of nodes.
     */
    public abstract void setPathColor(Vector3D color);

    /**
     * Set the line width of the path to render.
     *
     * @param lineWitdh to set.
     */
    public abstract void setPathLineWitdh(float lineWitdh);

    /**
     * Set the threshold distance after which is considered that a target is in
     * attack range.
     *
     * @param threshold the distance after which a target is in attack range.
     */
    public abstract void setTargetInAttackRangeThreshold(float threshold);

    /**
     * Set the secondary goal. This can be either a secondary target
     * position/rotation, an identifier for a goal stored in a transform group,
     * etc., not necessarily a player.
     *
     * @param secondaryGoal
     */
    public abstract void setSecondaryGoal(GameObject secondaryGoal);

    /**
     * Set the entities iterator.
     *
     * @param entities iterator.
     */
    public abstract void setEntities(Iterator<GameObject> entities);

    /**
     * Set this entity offset.
     *
     * @param offset of this entity.
     */
    public abstract void setEntityOffset(Vector3D offset);

    /**
     * Set the terrain obstacles iterator.
     *
     * @param obstacles iterator.
     */
    public abstract void setObstacles(Iterator<GameObject> obstacles);

    /**
     * Test if while running A.I. logic the A.I. controlled bot began to attack.
     *
     * @param timeStamp
     * @param lastTimeStamp
     * @return TRUE if the A.I. controlled bot is attacking, FALSE otherwise.
     */
    public abstract boolean isAttacking(long timeStamp, long lastTimeStamp);

    /**
     * Test if at least one steering behavior logic is on.
     *
     * @return TRUE if sat least one steering behavior logic is on, FALSE
     * otherwise.
     */
    public abstract boolean isSteeringBehaviorOn();

    /**
     * Test if steering behavior logic is on/off.
     *
     * @param logic to test if is on/off.
     * @return TRUE if steering behavior logic is on, FALSE otherwise.
     */
    public abstract boolean isSteeringBehaviorOn(SteeringBehavior logic);

    /**
     * Set to on a steering behavior logic.
     *
     * @param logic to set to on.
     */
    public abstract void SteeringBehaviorOn(SteeringBehavior logic);

    /**
     * Set to off a steering behavior logic.
     *
     * @param logic to set to off.
     */
    public abstract void SteeringBehaviorOff(SteeringBehavior logic);

    /**
     * Update the A.I. behavior, at each game loop update.
     *
     * @param sceneManager a copy of the local player sceneManager.
     * @param dt A.I. update time step.
     */
    public abstract void update(Object sceneManager, float dt);

    /**
     * Available steering behaviors.
     */
    public enum SteeringBehavior {

        SEEK, FLEE, ARRIVE,
        PURSUIT, EVADE,
        WANDER, WANDER_AREA, WANDER_CURVE,
        OBSTACLE_AVOIDANCE, WALL_AVOIDANCE, //TODO: Need these? I think not! If true remove.
        INTERPOSE, HIDE,
        PATH_FOLLOW, OFFSET_PURSUIT,
        COHESION, SEPARATION, ALIGNMENT,
    }
}
