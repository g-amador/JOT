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

import static java.lang.Math.pow;
import static java.lang.Math.signum;
import static java.lang.Math.sqrt;
import java.util.Random;
import static java.util.logging.Level.OFF;
import jot.manager.SceneManager;
import jot.math.geometry.shape.AbstractRayTracerShape;
import static jot.math.geometry.shape.AbstractRayTracerShape.Shape.TRIANGLE;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.dotProduct;

/**
 * Class that implements a refractive material.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Refractive extends AbstractRayTracerMaterial {

    static {
        log.setLevel(OFF);
    }

    private final Specular specular = new Specular();

    private final ThreadLocal<Random> random;

    /**
     * Constructor.
     *
     * @param random
     */
    public Refractive(ThreadLocal<Random> random) {
        this.random = random;
    }

    @Override
    public Vector3D getBSDF(
            Sampler sampler,
            SceneManager sceneManager,
            Ray ray, int depth,
            IntersectionResult intersection) {
        AbstractRayTracerShape obj = (AbstractRayTracerShape) intersection.getObject();
        Vector3D f = obj.color;
        Vector3D intersectionPoint = intersection.getIntersectionPoint();
        Vector3D normal = intersection.getNormal();
        Vector3D incident = ray.getDirection();
        Vector3D nl = dotProduct(normal, incident) < 0 ? normal : normal.scalarMultiply(-1);
        double into = dotProduct(normal, nl); // Ray from outside going in?
        double refractiveIndexAir = 1;
        double refractiveIndexGlass = 1.5;
        double refractiveIndexRatio = pow(refractiveIndexAir / refractiveIndexGlass, signum(into));
        double cosI = 0;
        if (obj.shape != TRIANGLE) {
            cosI = dotProduct(incident, nl);
        }
        double cos2t = 1 - refractiveIndexRatio * refractiveIndexRatio * (1 - cosI * cosI);
        if (cos2t < 0) {
            return this.specular.getBSDF(sampler, sceneManager, ray, depth, intersection);
        }
        Vector3D refractedDirection = incident.scalarMultiply(
                refractiveIndexRatio).subtract(normal.scalarMultiply(
                        signum(into) * (cosI * refractiveIndexRatio + sqrt(cos2t)))).normalize();

        // calculate fresnel reflectance, but in a funny way for russian roulette
        double a = refractiveIndexGlass - refractiveIndexAir;
        double b = refractiveIndexGlass + refractiveIndexAir;
        double R0 = a * a / (b * b);
        double c = 1 - (into > 0 ? -cosI : dotProduct(refractedDirection, normal));
        double Re = R0 + (1 - R0) * pow(c, 5);

        Vector3D radiance = this.russianRouletteBSDF(obj, sampler, sceneManager,
                depth, intersectionPoint, incident, nl, refractedDirection, Re);
        return obj.emission.add(new Vector3D(
                f.getX() * radiance.getX(),
                f.getY() * radiance.getY(),
                f.getZ() * radiance.getZ()));
    }

    private Vector3D russianRouletteBSDF(AbstractRayTracerShape obj,
            Sampler sampler, SceneManager sceneManager, int depth,
            Vector3D intersectionPoint, Vector3D incident,
            Vector3D normal, Vector3D refractedDirection, double Re) {
        Ray reflectionRay = this.specular.getReflectionRay(obj, intersectionPoint,
                incident, normal);
        Ray refractionRay = new Ray(intersectionPoint, refractedDirection);
        double Tr = 1 - Re;
        if (depth > 2) {
            double P = .25 + .5 * Re;
            if (this.random.get().nextDouble() < P) { // Russian roulette
                Vector3D recursiveReflectionRadiance = sampler
                        .radiance(sceneManager, reflectionRay, depth);
                double RP = Re / P;
                return recursiveReflectionRadiance.scalarMultiply(RP);
            } else {
                Vector3D recursiveRefactionRadiance = sampler
                        .radiance(sceneManager, refractionRay, depth);
                double TP = Tr / (1 - P);
                return recursiveRefactionRadiance.scalarMultiply(TP);
            }
        } else {
            Vector3D recursiveReflectionRadiance = sampler
                    .radiance(sceneManager, reflectionRay, depth);
            Vector3D recursiveRefactionRadiance = sampler
                    .radiance(sceneManager, refractionRay, depth);
            return recursiveReflectionRadiance.scalarMultiply(Re).add(recursiveRefactionRadiance.scalarMultiply(Tr));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOpaque() {
        return false;
    }
}
