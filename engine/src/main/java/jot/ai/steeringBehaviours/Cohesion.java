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
import java.util.ArrayList;
import static java.util.logging.Level.INFO;
import jot.ai.Agent;
import static jot.util.ExtensionAIOptions.extensionAIOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements Craig Reynolds flock steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Cohesion extends Seek {

    /**
     * Tagged neighbor entities.
     */
    protected ArrayList<Agent> taggedNeighborEntities;

    /**
     * Default constructor.
     */
    public Cohesion() {
        if (extensionAIOptions.get("useSteeringBehaviorsDebug")) {
            log.setLevel(INFO);
        }

        this.taggedNeighborEntities = new ArrayList<>();
    }

    /**
     * Set the tagged neighbor entities.
     *
     * @param taggedNeighborEntities
     */
    public void setTaggetNeighborEntities(ArrayList<Agent> taggedNeighborEntities) {
        this.taggedNeighborEntities = taggedNeighborEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D steer() {
        return this.cohesion(this.taggedNeighborEntities);
    }

    /**
     * Cohesion produces a steering force that moves a vehicle toward the center
     * of mass of its neighbors. A sheep running after its flock is
     * demonstrating cohesive behavior. Use this force to keep a group of
     * vehicles together.
     *
     * @param taggedNeighborEntities
     * @return a steering force that moves a vehicle toward the center of mass
     * of its neighbors.
     */
    protected Vector3D cohesion(ArrayList<Agent> taggedNeighborEntities) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //first find the center of mass of all the entities.
            Vector3D CenterOfMass, SteeringForce;
            CenterOfMass = SteeringForce = ZERO;

            int NeighborCount = 0;

            //iterate through the neighbors and sum up all the position vectors.
            for (Agent neighbor : taggedNeighborEntities) {
                //make sure *this* agent isn't included in the calculations and that
                //the agent being examined is a neighbor
                if (!neighbor.getId().equals(this.primaryAgent.getId())) {
                    //if (neighbor.getId().equals(primaryAgent.getId())) {
                    //    System.out.println("neighbor.getId() = primaryAgent.getId()");
                    //}

                    CenterOfMass = CenterOfMass.add(neighbor.getPosition());

                    ++NeighborCount;
                }
            }
            //System.out.println(NeighborCount);
            //System.out.println(CenterOfMass);

            if (NeighborCount > 0) {
                //the center of mass is the average of the sum of positions
                CenterOfMass = CenterOfMass.scalarMultiply(
                        1.0 / NeighborCount);//(double) NeighborCount);
                //System.out.println(CenterOfMass);

                //now seek toward that position
                SteeringForce = this.seek(CenterOfMass);
                //System.out.println(SteeringForce);
            }

            return SteeringForce;

        }
        return ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(GL2 gl) {
        //renderDirection(gl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose(GL2 gl) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
