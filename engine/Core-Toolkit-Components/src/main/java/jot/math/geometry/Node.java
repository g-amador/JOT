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
package jot.math.geometry;

import com.jogamp.opengl.GL2;

/**
 * Interface to specify the default methods to implement for each geometry node.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface Node {

    /**
     * Get the identifier associated with this Node.
     *
     * @return the Node identifier.
     */
    String getId();

    /**
     * Get the identifier associated with this Node.
     *
     * @param Id associated with this Node.
     */
    void setId(String Id);

    /**
     * Method to return a geometry node object.
     *
     * @return the implementing Node object.
     */
    Node getNode();

    /**
     * Method to render all the geometries associated with each individual node.
     *
     * @param gl
     */
    void render(GL2 gl);

    /**
     * Method to dispose any data buffers or lists if any.
     *
     * @param gl
     */
    void dispose(GL2 gl);
}
