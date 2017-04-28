/*
 * This file is part of the JOT game engine core toolkit component.
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
package jot.math.graph;

import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Float.floatToIntBits;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import java.util.Objects;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.Distance.getDistance;
import static jot.util.CoreOptions.coreOptions;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a graph node for usage in path finders.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Node implements Comparable<Node> {

    static final Logger log = getLogger("GraphNode");

    static {
        log.setLevel(OFF);
    }

    /**
     * The node position is also its Id.
     */
    public Vector3D position;

    /**
     * This node parent/predecessor.
     */
    public Node parent;
    /**
     * Real weight/cost from the source.
     */
    public float g;
    /**
     * Heuristic/estimated weight/cost from node to goal.
     */
    public float h;
    /**
     * Cost function: f = g + h
     */
    public float f;

    /**
     * Constructor.
     *
     * @param position of the node in the graph in Cartesian coordinates.
     */
    public Node(Vector3D position) {
        this.position = position;
        this.g = POSITIVE_INFINITY;
        this.h = POSITIVE_INFINITY;
        this.f = POSITIVE_INFINITY;
        this.parent = null;
    }

    /**
     * Constructor.
     *
     * @param n to copy from.
     */
    public Node(Node n) {
        this.position = n.position;
        this.g = n.g;
        this.h = n.h;
        this.f = n.f;
        this.parent = n.parent;
    }

    /**
     * Get the cost of an edge connecting this node to another node of the
     * weighted search graph.
     *
     * @param n another graph node.
     * @return the cost of an edge formed by this graph node and another graph
     * node n,
     */
    public float getCost(Node n) {
        return coreOptions.get("useUniformRegularGridCosts")
                ? this.position.getX() != n.position.getX()
                && this.position.getZ() != n.position.getZ()
                        ? (float) sqrt(2) : 1.0f
                : (float) getDistance(this.position, n.position);
    }

    @Override
    public int compareTo(Node other) {
        float otherCost = other.f;
        float thisCost = this.f;

        return (int) signum(thisCost - otherCost);
    }

    @Override
    public boolean equals(Object other) {
        return other != null && other instanceof Node
                && this.position.equals(((Node) other).position);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.position);
        hash = 53 * hash + Objects.hashCode(this.parent);
        hash = 53 * hash + floatToIntBits(this.g);
        hash = 53 * hash + floatToIntBits(this.h);
        hash = 53 * hash + floatToIntBits(this.f);
        return hash;
    }
}
