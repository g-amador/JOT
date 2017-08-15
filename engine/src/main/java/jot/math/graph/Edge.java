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

import static java.lang.Math.signum;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;

/**
 * Class that implements an edge connecting to graph nodes.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Edge implements Comparable<Edge> {

    static final Logger log = getLogger("GraphEdge");

    static {
        log.setLevel(OFF);
    }

    public Node n1;
    public Node n2;

    /**
     * Constructor.
     *
     * @param n1 a node that is connected to n2.
     * @param n2 a node that is connected to n1.
     */
    public Edge(Node n1, Node n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    /**
     * Get the cost of this edge of the weighted search graph.
     *
     * @return the cost of this edge of the weighted search graph. node n,
     */
    public float getCost() {
        return this.n1.getCost(this.n2);
    }

    @Override
    public int compareTo(Edge other) {
        float otherCost = other.getCost();
        float thisCost = this.getCost();

        return (int) signum(thisCost - otherCost);
    }
}
