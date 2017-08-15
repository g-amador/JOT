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

import static java.util.logging.Level.OFF;
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.physics.IntersectionResult;
import static jot.physics.IntersectionResult.MISS;
import jot.physics.Material;
import jot.physics.Ray;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.crossProduct;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.dotProduct;

/**
 * Class that implements a triangle shape.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Triangle extends AbstractShape {

    static final Logger log = getLogger("ShapeTriangle");

    static {
        log.setLevel(OFF);
    }

    /**
     * Triangle vertexes and edges v0v1 and v0v2.
     */
    public Vector3D v0, v1, v2, e1, e2;//, temp_normal; //, n0, n1, n2;
    private final Vector3D normal;
    private final double eps = 1e-8; //,offset; 

    /**
     * Constructor.
     *
     * @param v0 vertex 0.
     * @param v1 vertex 1.
     * @param v2 vertex 2.
     * @param material the plane material.
     */
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Material material) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.material = material;

        //n0 = v0.normalize();
        //n1 = v1.normalize();
        //n2 = v2.normalize();
        this.e1 = v1.subtract(v0);
        this.e2 = v2.subtract(v0);
        Vector3D crossProduct = crossProduct(this.e1, this.e2);
        this.normal = crossProduct.equals(ZERO)
                ? crossProduct
                : crossProduct.normalize();
        //offset = -dotProduct(v0, normal);
    }

    @Override
    public IntersectionResult intersect(Ray ray) {
        // From the paper "Fast Minimum Storage Ray Triangle Intersection"
        // Tomas Möller & Ben Trumbore
        Vector3D tvec, pvec, qvec;
        double det, inv_det;
        double u, v, t;

        //begin calculating determinant - also used to calculate U parameter
        pvec = crossProduct(ray.getDirection(), this.e2);

        //if determinant is near zero, ray lies in plane of triangle
        det = dotProduct(this.e1, pvec);
        if (det > -this.eps && det < this.eps) {
            return MISS; // false, no intersection
        }

        inv_det = 1.0 / det;

        //distance from vertex 0 to ray origin
        tvec = ray.getOrigin().subtract(this.v0);

        //u parameter calculation + bounds testing
        u = dotProduct(tvec, pvec) * inv_det;
        if (u < 0.0 || u > 1.0) {
            return MISS; // false, no intersection
        }
        qvec = crossProduct(tvec, this.e1);

        //calculate V parameter and test bounds
        v = dotProduct(ray.getDirection(), qvec) * inv_det;
        if (v < 0.0 || (u + v) > 1.0) {
            return MISS; // false, no intersection
        }

        //calculate t: ray intersects triangle
        t = dotProduct(this.e2, qvec) * inv_det;

//        {
//            temp_normal = (v0.scalarMultiply(1 - v - u))
//                    .add(v1.scalarMultiply(v))
//                    .add(v2.scalarMultiply(u));
//            temp_normal = normal
//                    .add(v0.scalarMultiply(1 - v - u))
//                    .add(v1.scalarMultiply(v))
//                    .add(v2.scalarMultiply(u));
//            temp_normal = (v0.normalize().scalarMultiply(1 - v - u))
//                    .add(v1.normalize().scalarMultiply(v))
//                    .add(v2.normalize().scalarMultiply(u));
//            temp_normal = normal
//                    .add(v0.normalize().scalarMultiply(1 - v - u))
//                    .add(v1.normalize().scalarMultiply(v))
//                    .add(v2.normalize().scalarMultiply(u));
//            temp_normal = normal.normalize()
//                    .add(v0.normalize().scalarMultiply(1 - v - u))
//                    .add(v1.normalize().scalarMultiply(v))
//                    .add(v2.normalize().scalarMultiply(u));
//            temp_normal = normal.normalize()
//                    .add(v0.normalize().scalarMultiply(1 - v - u))
//                    .add(v1.normalize().scalarMultiply(v))
//                    .add(v2.normalize().scalarMultiply(u));
//            if (t >= 0) {
//                temp_normal = temp_normal.negate();
//            }
//        }
        double denom = dotProduct(this.normal, ray.getDirection());
        if (denom > 0) {
            return MISS;
        }

        //return new IntersectionResult(ray, (dotProduct(ray.origin, normal) + offset) / -dotProduct(normal, ray.direction), this);
        return new IntersectionResult(ray, t >= 0 ? t : -1, this);
        //return new IntersectionResult(ray, t, this);
    }

    @Override
    public Vector3D getNormal(Vector3D intersectionPoint) {
        return this.normal;
        //return temp_normal.normalize();
    }
};
