/*
 * This file is part of the JOT game engine raytracer framework toolkit
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
import java.util.logging.Logger;
import static java.util.logging.Logger.getLogger;
import jot.manager.SceneManager;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import static org.apache.commons.math3.geometry.euclidean.threed.Vector3D.ZERO;

/**
 * Class that implements the RayTracer sampler.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public class Sampler {

    static final Logger log = getLogger("Sampler");

    static {
        log.setLevel(OFF);
    }

    /**
     * Method to calculate the radiance/color of a pixel.
     *
     * @param sceneManager a copy of the manager scene.
     * @param ray a provided Ray.
     * @param depth the search depth to test if the provided Ray intersects an
     * object in the scene.
     * @return if an object in the scene intersects a provided Ray above a given
     * depth a pixel color, ZERO vector otherwise.
     */
    public Vector3D radiance(SceneManager sceneManager, Ray ray, int depth) {
        if (depth > 5) {
            return ZERO;
        }
        IntersectionResult intersection = sceneManager.intersect(ray);
        if (intersection.isMiss()) {
            return ZERO;
        }
        return ((AbstractRayTracerMaterial) intersection.getObject().getMaterial())
                .getBSDF(this, sceneManager, ray, depth + 1, intersection);
    }
}
