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
package jot.physics;

import jot.math.geometry.shape.AbstractShape;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that must be implemented by a class that implements the a
 * ray-shape intersection result.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractIntersectionResult {

    /**
     * Get the abstract shape which collided with this intersection result
     * associated ray.
     *
     * @return the abstract shape which collided with a ray.
     */
    public abstract AbstractShape getObject();

    /**
     * Get the point where this intersection result ray intersected something.
     *
     * @return the point where this intersection result ray intersected
     * something.
     */
    public abstract Vector3D getIntersectionPoint();

    /**
     * Get the normal of this intersection result shape.
     *
     * @return the normal of this intersection result shape.
     */
    public abstract Vector3D getNormal();

    /**
     * Get the target value of this intersection result.
     *
     * @return the target value of this intersection result.
     */
    public abstract double getTarget();

    /**
     * Test if this intersection result equals hit.
     *
     * @return TRUE if intersection result not equals MISS, FALSE otherwise.
     */
    public abstract boolean isHit();

    /**
     * Test if this t is lower that another intersection result t,
     *
     * @param other another intersection result.
     * @return TRUE this t is lower that another intersection result t, FALSE
     * otherwise.
     */
    public abstract boolean closerThan(AbstractIntersectionResult other);

    /**
     * Test if this intersection result equals MISS.
     *
     * @return TRUE if intersection result equals MISS, FALSE otherwise.
     */
    public abstract boolean isMiss();
}
