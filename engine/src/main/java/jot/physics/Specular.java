/*
 * This file is part of the JOT game engine materials framework toolkit
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
package jot.physics;

import static java.util.logging.Level.OFF;
import jot.manager.SceneManager;
import jot.math.geometry.shape.AbstractRayTracerShape;
import static jot.math.geometry.shape.AbstractRayTracerShape.Shape.TRIANGLE;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.dotProduct;

/**
 * Class that implements a specular material.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Specular extends AbstractRayTracerMaterial {

    static {
        log.setLevel(OFF);
    }

    @Override
    public Vector3D getBSDF(Sampler sampler, SceneManager sceneManager, Ray ray, int depth, IntersectionResult intersection) {
        AbstractRayTracerShape obj = (AbstractRayTracerShape) intersection.getObject();
        Vector3D f = obj.color;
        Vector3D intersectionPoint = intersection.getIntersectionPoint();
        Vector3D normal = intersection.getNormal();
        Ray reflectionRay = this.getReflectionRay(obj, intersectionPoint, ray.getDirection(), normal);
        Vector3D recursiveReflectionRadiance = sampler.radiance(sceneManager, reflectionRay, depth);
        return obj.emission.add(new Vector3D(
                f.getX() * recursiveReflectionRadiance.getX(),
                f.getY() * recursiveReflectionRadiance.getY(),
                f.getZ() * recursiveReflectionRadiance.getZ()));
    }

    /**
     * Get a reflected the ray reflected by a provided a shape.
     *
     * @param obj a provided a shape.
     * @param intersectionPoint the point of a intersection test.
     * @param incident the incident vector.
     * @param normal a normal.
     * @return the reflected ray.
     */
    public Ray getReflectionRay(
            AbstractRayTracerShape obj,
            Vector3D intersectionPoint,
            Vector3D incident, Vector3D normal) {
        return new Ray(intersectionPoint, this.getReflectionDirection(obj, incident, normal));
    }

    /**
     * Get the direction of a reflected ray reflected by a provided a shape.
     *
     * @param obj a provided a shape.
     * @param incident the incident vector.
     * @param normal a normal.
     * @return the reflected ray direction.
     */
    public Vector3D getReflectionDirection(
            AbstractRayTracerShape obj,
            Vector3D incident, Vector3D normal) {
        double cosI = -0.5;
        if (obj.shape != TRIANGLE) {
            cosI = dotProduct(normal, incident);
        }
        Vector3D reflectedDirection = incident.subtract(normal.scalarMultiply(2 * cosI));
        return reflectedDirection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpaque() {
        return true;
    }
}
