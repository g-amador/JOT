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

import com.jogamp.opengl.GL2;
import static java.util.logging.Level.INFO;
import jot.ai.Agent;
import static jot.physics.Kinematics.translatePolar;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds offset pursuit steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class OffsetPursuit extends Arrive {

    Vector3D offset;

    /**
     * Default constructor.
     */
    public OffsetPursuit() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * Set the offSet of this A.I. agent.
     *
     * @param offSet of this A.I. agent.
     */
    public void setOffset(Vector3D offSet) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            this.offset = offSet;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.offsetPursuit(this.primaryGoal, this.offset);
    }

    /**
     * Calculates the steering force required to keep a vehicle positioned at a
     * specified offset from a target vehicle. This is particularly useful for
     * creating formations. When you watch an air display, such as the British
     * Red Arrows, many of the spectacular maneuvers require that the aircraft
     * remain in the same relative positions to the lead aircraft.
     *
     * @param leader of the group formation.
     * @param offset to the leader of the group.
     * @return the steering velocity.
     */
    protected Vector3D offsetPursuit(Agent leader, Vector3D offset) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //calculate the offset’s position in world space.
            double r = offset.getNorm();
            //System.out.println(offset);
            //System.out.println(r);

            double rotation = this.getDirection2Target(offset, ZERO);
            //System.out.println(rotation);

            Vector3D WorldOffsetPos = translatePolar(leader.getPosition(),
                    (float) r, (float) (leader.getRotation().getY() + rotation),
                    0, 1);
            //System.out.println(WorldOffsetPos);

            Vector3D ToOffset = WorldOffsetPos.subtract(this.primaryAgent.getPosition());
            //System.out.println(ToOffset);

            //the look-ahead time is proportional to the distance between the leader
            //and the pursuer; and is inversely proportional to the sum of both
            //agent's velocities.
            double LookAheadTime = ToOffset.getNorm()
                    / (this.primaryAgent.getMaxSpeed()
                    + leader.getVelocity().getNorm());
            //System.out.println(LookAheadTime);            

            //now arrive at the predicted future position of the offset.
            Vector3D predictedFuturePosition = WorldOffsetPos.add(
                    leader.getVelocity().scalarMultiply(LookAheadTime));
            //System.out.println(predictedFuturePosition);
            return this.arrive(predictedFuturePosition, 1);
        }
        return ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
    }
}
