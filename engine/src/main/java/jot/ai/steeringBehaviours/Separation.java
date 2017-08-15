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
 * Class that implements Craig Reynolds Separation steering behavior.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Separation extends AbstractSteeringBehavior {

    /**
     * Tagged neighbor entities.
     */
    protected ArrayList<Agent> taggedNeighborEntities;

    /**
     * Default constructor.
     */
    public Separation() {
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
        return this.separation(this.taggedNeighborEntities);
    }

    /**
     * Separation iterates through the tagged vehicles, examining each one. The
     * vector to each vehicle under consideration is normalized, divided by the
     * distance to the neighbor, and added to the steering force.
     *
     * @param taggedNeighborEntities
     * @return the steering force.
     */
    protected Vector3D separation(ArrayList<Agent> taggedNeighborEntities) {
        if (extensionAIOptions.get("useSteeringBehaviors")) {
            Vector3D SteeringForce = ZERO;

            for (Agent neighbor : taggedNeighborEntities) {
                //make sure this agent isn't included in the calculations and that
                //the agent being examined is close enough.
                if (!neighbor.getId().equals(this.primaryAgent.getId())) {
                    //if (neighbor.getId().equals(primaryAgent.getId())) {
                    //    System.out.println("neighbor.getId() = primaryAgent.getId()");
                    //}

                    Vector3D ToAgent = this.primaryAgent.getPosition()
                            .subtract(neighbor.getPosition());
                    //System.out.println(ToAgent);

                    //scale the force inversely proportional to the agent's distance
                    //from its neighbor.
                    SteeringForce = SteeringForce.add(ToAgent.equals(ZERO)
                            ? ZERO
                            : ToAgent.normalize().scalarMultiply(1.0 / ToAgent.getNorm()));
                    //System.out.println(SteeringForce);
                }
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
