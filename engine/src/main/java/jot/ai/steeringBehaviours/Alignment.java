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
 * Class that implements Craig Reynolds Alignment steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Alignment extends AbstractSteeringBehavior {

    /**
     * Tagged neighbor entities.
     */
    protected ArrayList<Agent> taggedNeighborEntities;

    /**
     * Default constructor.
     */
    public Alignment() {
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
        return this.alignment(this.taggedNeighborEntities);
    }

    /**
     * Alignment attempts to keep a vehicle’s heading aligned with its
     * neighbors. The force is calculated by first iterating through all the
     * neighbors and averaging their heading vectors. This value is the desired
     * heading, so we just subtract the vehicle’s heading to get the steering
     * force.
     *
     * @param taggedNeighborEntities
     * @return the steering force.
     */
    protected Vector3D alignment(ArrayList<Agent> taggedNeighborEntities) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            //used to record the average heading of the neighbors.
            Vector3D AverageHeading = ZERO;

            //used to count the number of vehicles in the neighborhood
            int NeighborCount = 0;

            //iterate through all the tagged vehicles and sum their heading vectors.
            for (Agent neighbor : taggedNeighborEntities) {
                //make sure *this* agent isn't included in the calculations and that
                //the agent being examined is close enough.
                if (!neighbor.getId().equals(this.primaryAgent.getId())) {
                    //if (neighbor.getId().equals(primaryAgent.getId())) {
                    //    System.out.println("neighbor.getId() = primaryAgent.getId()");
                    //}

                    AverageHeading = AverageHeading.add(neighbor.getRotation());

                    ++NeighborCount;
                }
            }
            //System.out.println(NeighborCount);
            //System.out.println(AverageHeading);

            //if the neighborhood contained one or more vehicles, average their
            //heading vectors.
            if (NeighborCount > 0) {
                AverageHeading = AverageHeading.scalarMultiply(
                        1.0 / NeighborCount);//(double) NeighborCount);
                //System.out.println(AverageHeading);

                AverageHeading = AverageHeading.subtract(this.primaryAgent.getRotation());
                //System.out.println(AverageHeading);
            }

            return AverageHeading;
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
