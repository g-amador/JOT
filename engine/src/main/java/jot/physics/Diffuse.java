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

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import java.util.Random;
import static java.util.logging.Level.OFF;
import jot.manager.SceneManager;
import jot.math.geometry.shape.AbstractRayTracerShape;
import jot.math.geometry.shape.RayTracerSphere;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.PLUS_I;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.PLUS_J;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.crossProduct;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.dotProduct;

/**
 * Class that implements a diffuse material.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Diffuse extends AbstractRayTracerMaterial {

    static {
        log.setLevel(OFF);
    }

    private final ThreadLocal<Random> random;

    /**
     * Constructor.
     *
     * @param random
     */
    public Diffuse(final ThreadLocal<Random> random) {
        this.random = random;
    }

    @Override
    public Vector3D getBSDF(final Sampler sampler,
            final SceneManager sceneManager,
            final Ray ray, final int depth,
            final IntersectionResult intersection) {
        final AbstractRayTracerShape obj
                = (AbstractRayTracerShape) intersection.getObject();
        final Vector3D f = obj.color;
        final Vector3D intersectionPoint = intersection.getIntersectionPoint();
        final Vector3D normal = intersection.getNormal();
        final Vector3D d = this.sampleAroundNormal(normal);

        final Vector3D lightRadiance = this.getLightContribution(sceneManager,
                intersectionPoint, normal, f);
        final Vector3D recursiveRadiance = sampler.radiance(sceneManager,
                new Ray(intersectionPoint, d), depth);
        return obj.emission.add(lightRadiance).add(new Vector3D(
                f.getX() * recursiveRadiance.getX(),
                f.getY() * recursiveRadiance.getY(),
                f.getZ() * recursiveRadiance.getZ()));
    }

    private Vector3D getLightContribution(
            final SceneManager sceneManager,
            final Vector3D intersectionPoint,
            final Vector3D normal, final Vector3D f) {
        // Loop over any lights
        Vector3D lightRadiance = ZERO;
        for (final RayTracerSphere light : sceneManager.lightSources) {
            final Vector3D intersection2lightDirection
                    = light
                    .getNormal(intersectionPoint)
                    .scalarMultiply(-1);
            final IntersectionResult lightIntersection = sceneManager.intersect(
                    new Ray(intersectionPoint, intersection2lightDirection));
            if (lightIntersection.isHit() && lightIntersection.getObject() == light) {
                final Vector3D light2intersectionDirection
                        = intersectionPoint.subtract(light.center);
                final double cos_a_max
                        = sqrt(1 - light.radius * light.radius
                                / dotProduct(light2intersectionDirection, light2intersectionDirection));
                final double omega = 2 * (1 - cos_a_max);
                Vector3D v = light.emission.scalarMultiply(
                        dotProduct(intersection2lightDirection, normal) * omega);
                lightRadiance = lightRadiance.add(new Vector3D(
                        f.getX() * v.getX(),
                        f.getY() * v.getY(),
                        f.getZ() * v.getZ()));
            }
        }
        return lightRadiance;
    }

    /**
     * Sample around a given normal.
     *
     * @param normal a given normal.
     * @return the given normal sampling result.
     */
    public Vector3D sampleAroundNormal(final Vector3D normal) {
        final Vector3D sampleCosineHemisphere = this.sampleCosineHemisphere();

        final Vector3D d = this.mapUnitZVector3D(sampleCosineHemisphere, normal);
        return d;
    }

    /**
     * Applies the rotation required from Unit Z to destinations to source.
     */
    private Vector3D mapUnitZVector3D(final Vector3D source, final Vector3D destination) {
        final Vector3D w = destination;
        final Vector3D u = (abs(w.getX()) > .1 ? PLUS_J : PLUS_I)
                .crossProduct(w).normalize();
        final Vector3D v = crossProduct(w, u);
        final Vector3D r = u.scalarMultiply(source.getX())
                .add(v.scalarMultiply(source.getY()))
                .add(w.scalarMultiply(source.getZ()))
                .normalize();
        return r;
    }

    private Vector3D sampleCosineHemisphere() {
        final double u2 = this.random.get().nextDouble();
        final double u1 = this.random.get().nextDouble();
        final double theta = 2 * PI * u2;
        final double r = sqrt(u1);
        final double x = r * cos(theta);
        final double y = r * sin(theta);
        final double z = sqrt(1 - u1);
        final Vector3D d = new Vector3D(x, y, z).normalize();
        return d;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpaque() {
        return true;
    }
}
