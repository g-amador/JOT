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
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds flee steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Flee extends AbstractSteeringBehavior {

    /**
     * Default constructor.
     */
    public Flee() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.flee(this.primaryGoal.getPosition());
    }

    /**
     * The flee method.
     *
     * @param targetPosition
     * @return the steering velocity.
     */
    protected Vector3D flee(Vector3D targetPosition) {
        //if target is far away enough stop fleeing.
        if (extensionAIOptions.get("useSteeringBehaviors")
                && !this.isTargetFarEnough(targetPosition)) {
            //retreating is just like chasing in reverse             
            //set primary agent rotation in the oposite direction of target to flee.
            //System.out.println(primaryAgent.getRotation());
            this.primaryAgent.setRotation(new Vector3D(
                    this.primaryAgent.getRotation().getX(),
                    //getDirection2Target(targetPosition, primaryAgent.getPosition()),
                    this.getDirection2Target(this.primaryAgent.getPosition(), targetPosition),
                    this.primaryAgent.getRotation().getZ()));
            //System.out.println(primaryAgent.getRotation());

            //get the steering velocity.
            Vector3D distance = this.primaryAgent.getPosition().subtract(targetPosition);
            Vector3D DesiredVelocity = distance.equals(ZERO)
                    ? distance
                    : distance.normalize().scalarMultiply(this.primaryAgent.getMaxSpeed());
            //System.out.println(DesiredVelocity);
            //System.out.println(primaryAgent.getVelocity());
            //System.out.println(DesiredVelocity.subtract(primaryAgent.getVelocity()));
            return DesiredVelocity.subtract(this.primaryAgent.getVelocity());
        }
        return ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        this.renderDirection(gl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(GL2 gl) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
