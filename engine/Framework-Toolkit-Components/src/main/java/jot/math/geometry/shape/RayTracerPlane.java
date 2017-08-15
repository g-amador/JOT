/*
 * This file is part of the JOT game engine geometry framework toolkit
 * component. 
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
package jot.math.geometry.shape;

import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import static jot.math.geometry.shape.AbstractRayTracerShape.Shape.PLANE;
import jot.physics.IntersectionResult;
import static jot.physics.IntersectionResult.MISS;
import jot.physics.Material;
import jot.physics.Ray;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.dotProduct;

/**
 * Class that implements a plane shape.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class RayTracerPlane extends AbstractRayTracerShape {

    static final Logger log = getLogger("RayTracerPlane");

    static {
        log.setLevel(OFF);
    }

    private final Vector3D normal;
    private final double offset;

    /**
     * Constructor.
     *
     * @param normal the plane normal.
     * @param offset the plane XYZ offsets.
     * @param emission the plane emission.
     * @param color the plane color.
     * @param material the plane material.
     */
    public RayTracerPlane(Vector3D normal, double offset,
            Vector3D emission, Vector3D color, Material material) {
        this.emission = emission;
        this.color = color;
        this.material = material;
        this.normal = normal.normalize();
        this.offset = offset;
        this.shape = PLANE;
    }

    @Override
    public IntersectionResult intersect(Ray ray) {
        double denom = dotProduct(this.normal, ray.getDirection());
        if (denom > 0) {
            return MISS;
        }

        return new IntersectionResult(ray, (dotProduct(ray.getOrigin(), this.normal) + this.offset) / -denom, this);
    }

    @Override
    public Vector3D getNormal(Vector3D intersectionPoint) {
        return this.normal;
    }
}
