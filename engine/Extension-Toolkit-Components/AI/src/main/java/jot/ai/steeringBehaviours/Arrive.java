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

import static java.lang.Math.min;
import static java.util.logging.Level.INFO;
import static jot.ai.steeringBehaviours.AbstractSteeringBehavior.log;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds arrive steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Arrive extends Seek {

    /**
     * Deceleration factor.
     */
    protected float deceleration;

    /**
     * Default constructor.
     */
    public Arrive() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }

        this.deceleration = 3;
    }

    /**
     * Set the deceleration value.
     *
     * @param deceleration factor to apply to the arrive behavior.
     */
    public void setDeceleration(float deceleration) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.deceleration = deceleration;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.arrive(this.primaryGoal.getPosition(), this.deceleration);
    }

    /**
     * The arrive method.
     *
     * @param targetPosition
     * @param deceleration
     * @return the steering velocity.
     */
    protected Vector3D arrive(Vector3D targetPosition, float deceleration) {
        //if target close enough stop seeking.
        if (extensionAIOptions.get("useSteeringBehaviors")
                && !this.isTargetCloseEnough(targetPosition)) {
            //set primary agent rotation in the direction of target to seek.       
            //System.out.println(primaryAgent.getRotation());
            this.primaryAgent.setRotation(new Vector3D(
                    this.primaryAgent.getRotation().getX(),
                    //getDirection2Target(primaryAgent.getPosition(), targetPosition),
                    this.getDirection2Target(targetPosition, this.primaryAgent.getPosition()),
                    this.primaryAgent.getRotation().getZ()));
            //System.out.println(primaryAgent.getRotation());

            //get the steering velocity.
            Vector3D ToTarget = targetPosition.subtract(this.primaryAgent.getPosition());

            //calculate the distance to the target position
            double distance = ToTarget.getNorm();

            //calculate the speed required to reach the target given the desired
            //deceleration            
            double speed = distance / deceleration;
            //System.out.println(speed);

            //make sure the velocity does not exceed the max            
            speed = min(speed, this.primaryAgent.getMaxSpeed());
            //System.out.println(speed);

            //from here proceed just like Seek except we don't need to normalize
            //the ToTarget vector because we have already gone to the trouble
            //of calculating its length: dist.
            Vector3D DesiredVelocity = ToTarget.scalarMultiply(speed / distance);
            //System.out.println(DesiredVelocity);
            //System.out.println(primaryAgent.getVelocity());
            //System.out.println(DesiredVelocity.subtract(primaryAgent.getVelocity()));
            return DesiredVelocity.subtract(this.primaryAgent.getVelocity());
        }
        return ZERO;
    }
}
