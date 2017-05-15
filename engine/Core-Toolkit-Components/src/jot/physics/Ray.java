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

import static java.lang.Float.NEGATIVE_INFINITY;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.String.format;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements a ray.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Ray extends AbstractRay {

    static final Logger log = getLogger("Ray");

    static {
        log.setLevel(OFF);
    }

    /**
     * Ray origin.
     */
    protected Vector3D origin;

    /**
     * Ray direction.
     */
    protected Vector3D direction;

    /**
     * Threshold to test between which a object intersection may be tested.
     */
    protected float tmin, tmax;

    /**
     * Constructor.
     *
     * @param origin of the ray.
     * @param direction of the ray.
     */
    public Ray(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        this.direction = direction;
        //TODO: test if possible values!!!!!!
        this.tmin = NEGATIVE_INFINITY;
        this.tmax = POSITIVE_INFINITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getDirection() {
        return this.direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getOrientation(double t) {
        return this.origin.add(this.direction.scalarMultiply(t));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getOrigin() {
        return this.origin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return format("Ray[%s, %s]", this.origin, this.direction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getTmin() {
        return this.tmin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTmin(float tmin) {
        this.tmin = tmin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getTmax() {
        return this.tmax;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTmax(float tmax) {
        this.tmax = tmax;
    }
}
