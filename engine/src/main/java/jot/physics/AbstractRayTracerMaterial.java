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

import jot.manager.SceneManager;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that each RayTracer material must implement.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractRayTracerMaterial extends Material {

    /**
     *
     * @param sampler
     * @param sceneManager
     * @param ray
     * @param depth
     * @param intersection
     * @return
     */
    public abstract Vector3D getBSDF(
            Sampler sampler,
            SceneManager sceneManager,
            Ray ray, int depth,
            IntersectionResult intersection);
}
