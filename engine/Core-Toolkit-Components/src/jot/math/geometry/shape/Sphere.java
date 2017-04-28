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
package jot.math.geometry.shape;

import static java.lang.Math.sqrt;
import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.IntersectionResult;
import static jot.physics.IntersectionResult.MISS;
import jot.physics.Material;
import jot.physics.Ray;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.dotProduct;

/**
 * Class that implements a sphere shape.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Sphere extends AbstractShape {

    static final Logger log = getLogger("ShapeSphere");

    static {
        log.setLevel(OFF);
    }

    /**
     * The center of the sphere.
     */
    Vector3D center;

    /**
     * The radius of the sphere and the tolerance to test ray-sphere
     * intersection.
     */
    double radius, eps = 1e-8;

    /**
     * Constructor.
     *
     * @param radius the radius of the sphere.
     * @param center the center of the sphere.
     * @param material the material of the sphere.
     */
    public Sphere(double radius, Vector3D center, Material material) {
        this.radius = radius;
        this.center = center;
        this.material = material;
    }

    @Override
    public IntersectionResult intersect(Ray ray) {
        Vector3D v = this.center.subtract(ray.getOrigin());
        if (this.material.isOpaque() && dotProduct(v, v) < this.radius * this.radius) {
            return MISS;
        }
        double b = dotProduct(v, ray.getDirection());
        double discriminant = b * b - dotProduct(v, v) + this.radius * this.radius;
        if (discriminant < 0) {
            return MISS;
        }
        double d = sqrt(discriminant);
        double tfar = b + d;
        if (tfar <= this.eps) {
            return MISS;
        }
        double tnear = b - d;
        if (tnear <= this.eps) {
            return new IntersectionResult(ray, tfar, this);
        }
        return new IntersectionResult(ray, tnear, this);
    }

    @Override
    public Vector3D getNormal(Vector3D intersectionPoint) {
        return intersectionPoint.subtract(this.center).normalize();
    }
}
