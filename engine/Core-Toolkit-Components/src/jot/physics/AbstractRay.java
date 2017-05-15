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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that a Ray Class must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractRay {

    /**
     * Get the Ray direction.
     *
     * @return the Ray direction.
     */
    public abstract Vector3D getDirection();

    /**
     * Get the Ray orientation.
     *
     * @param t a scalar to multiply by the ray direction.
     * @return the ray origin plus the scalar multiplication of the ray
     * direction by t.
     */
    public abstract Vector3D getOrientation(double t);

    /**
     * Get the Ray origin.
     *
     * @return the ray origin .
     */
    public abstract Vector3D getOrigin();

    /**
     * Get the minimum value of the threshold to test between which a object
     * intersection may be tested.
     *
     * @return the threshold minimum.
     */
    public abstract float getTmin();

    /**
     * Set the minimum value of the threshold to test between which a object
     * intersection may be tested.
     *
     * @param tmin the minimum value of the threshold.
     */
    public abstract void setTmin(float tmin);

    /**
     * Get the minimum value of the threshold to test between which a object
     * intersection may be tested.
     *
     * @return the threshold maximum.
     */
    public abstract float getTmax();

    /**
     * Set the maximum value of the threshold to test between which a object
     * intersection may be tested.
     *
     * @param tmax the maximum value of the threshold.
     */
    public abstract void setTmax(float tmax);
}
