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

import static java.lang.Double.POSITIVE_INFINITY;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.math.geometry.shape.AbstractShape;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Class that implements the a ray-shape intersection result.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class IntersectionResult extends AbstractIntersectionResult {

    static final Logger log = getLogger("IntersectionResult");

    /**
     * Specification of the IntersectionResult of the type MISS.
     */
    public static IntersectionResult MISS = new IntersectionResult(null, POSITIVE_INFINITY, null);

    static {
        log.setLevel(OFF);
    }

    protected final AbstractRay ray;
    protected double t;
    protected AbstractShape object;

    /**
     * Constructor.
     *
     * @param ray a ray to test.
     * @param t target of this intersection result.
     * @param object associated with this intersection result.
     */
    public IntersectionResult(AbstractRay ray, double t, AbstractShape object) {
        this.ray = ray;
        this.t = t;
        this.object = object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractShape getObject() {
        return this.object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getIntersectionPoint() {
        return this.ray.getOrientation(this.t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Vector3D getNormal() {
        return this.object.getNormal(this.getIntersectionPoint());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTarget() {
        return this.t;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHit() {
        return this != MISS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean closerThan(AbstractIntersectionResult other) {
        return this.t < other.getTarget();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMiss() {
        return this == MISS;
    }
}
