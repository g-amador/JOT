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
package jot.io.device;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Interface that specifies the core methods to implement for a generic
 * trackable input handler.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public interface TrackableInput extends Input {

    /**
     * Get the acceleration in of a generic device cursor, e.g., for a Wiimote
     * its roll, pitch, and yaw, etc.
     *
     * @return X, Y, and Z coordinates of generic cursor.
     */
    Vector3D getAcceleration();

    /**
     * Get the position in 3D Cartesian space of a generic device cursor, e.g.,
     * for a Wiimote its a X, Y, Z coordinate, for a mouse its the mouse cursor
     * X and Y values, etc.
     *
     * @return X, Y, and Z coordinates of generic cursor.
     */
    Vector3D getPosition();

    /**
     * Get the difference between a generic device cursor current and old
     * position.
     *
     * @return the difference between generic device cursor current and old
     * position.
     */
    Vector3D getPositionShift();
}
