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

import jot.physics.IntersectionResult;
import jot.physics.Material;
import jot.physics.Ray;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 * Abstract class that each shape must extend.
 *
 * @author G. Amador {@literal &} A. Gomes
 */
public abstract class AbstractShape {

    /**
     * The material type of the shape.
     */
    protected Material material;

    /**
     * Test this shape intersect a given Ray.
     *
     * @param ray a given ray.
     * @return the IntersectionResult of the test.
     */
    public abstract IntersectionResult intersect(Ray ray);

    /**
     * Get this shape normal.
     *
     * @param intersectionPoint from a previous intersect with a ray test.
     * @return the shape normal.
     */
    public abstract Vector3D getNormal(Vector3D intersectionPoint);

    /**
     *
     * Get the material of this plane.
     *
     * @return this plane material.
     */
    public Material getMaterial() {
        return this.material;

    }

    /**
     * Set the material of this plane.
     *
     * @param material the material to set to this plane.
     */
    public void setMaterial(Material material) {
        this.material = material;
    }
}
